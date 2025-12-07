package services

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"net/url"
	"time"

	"when2go/src/models"
)

const (
	baseUrl              = "https://api.transport.nsw.gov.au/v1/tp"
	coordAPIEndpoint     = baseUrl + "/coord"
	departureAPIEndpoint = baseUrl + "/departure_mon"
)

func CoordToNearestLRStop(lat float64, lon float64) (stop *models.Stop, allStops []models.Stop, err error) {
	radii := []int{1000, 2000, 3000}
	return findNearestLRStop(lat, lon, radii)
}

func findNearestLRStop(lat, lon float64, radii []int) (stop *models.Stop, allStops []models.Stop, err error) {
	for _, radius := range radii {
		stop, allStops, err := callCoordWithRadius(lat, lon, radius)
		if err == nil {
			return stop, allStops, nil
		}
	}
	return nil, nil, fmt.Errorf("no light rail locations found within %v m", radii)
}

func callCoordWithRadius(lat float64, lon float64, radius int) (stop *models.Stop, allStops []models.Stop, err error) {
	apiKey, err := getAPIKey("TRIP_PLANNER_API_KEY")
	if err != nil {
		log.Printf("%v", err)
		return nil, nil, err
	}

	params := make(url.Values)
	params.Set("outputFormat", "rapidJSON")
	params.Set("coord", fmt.Sprintf("%f:%f:EPSG:4326", lon, lat))
	params.Set("coordOutputFormat", "EPSG:4326")
	params.Set("inclFilter", "1")
	params.Set("type_1", "BUS_POINT")
	params.Set("radius_1", fmt.Sprintf("%d", radius))
	params.Set("PoisOnMapMacro", "true")
	params.Set("version", "10.2.1.42")

	urlStr := fmt.Sprintf("%s?%s", coordAPIEndpoint, params.Encode())

	req, err := http.NewRequest("GET", urlStr, nil)
	if err != nil {
		return nil, nil, err
	}
	req.Header.Set("accept", "application/json")
	req.Header.Set("Authorization", apiKey)

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		log.Printf("Error making request to Coord API: %v", err)
		return nil, nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		log.Printf("Error response from Coord API: %s", resp.Status)
		return nil, nil, fmt.Errorf("failed to get stop ID: %s", resp.Status)
	}

	var apiResp models.CoordResponse
	if err := json.NewDecoder(resp.Body).Decode(&apiResp); err != nil {
		log.Printf("Error decoding Coord API response: %v", err)
		return nil, nil, err
	}

	lrNames := map[string]bool{"LR": true, "LR1": true, "LR2": true, "LR3": true}
	var lrLocations []models.Location
	for _, loc := range apiResp.Locations {
		if lrNames[loc.Properties.StopAreaName] {
			lrLocations = append(lrLocations, loc)
		}
	}

	if len(lrLocations) == 0 {
		log.Printf("No light rail locations found")
		return nil, nil, fmt.Errorf("no light rail locations found")
	}

	// Structure the stop points under their parent stops
	stops := make([]models.Stop, 0)
	stopIndex := 0
	for i := 0; i < len(lrLocations); i++ {
		if i > 0 && lrLocations[i].Properties.StopGlobalID == lrLocations[i-1].Properties.StopGlobalID {
			stops[stopIndex-1].StopPoints = append(stops[stopIndex-1].StopPoints, models.StopPoint{
				ID:         lrLocations[i].Properties.StopPointGlobalID,
				Coord:      models.Coord{Latitude: lrLocations[i].Coord[0], Longitude: lrLocations[i].Coord[1]},
				Departures: []models.Departure{},
			})
		} else {
			stops = append(stops, models.Stop{
				ID:   lrLocations[i].Properties.StopGlobalID,
				Name: lrLocations[i].Properties.StopLongName,
				StopPoints: []models.StopPoint{
					{
						ID:         lrLocations[i].Properties.StopPointGlobalID,
						Coord:      models.Coord{Latitude: lrLocations[i].Coord[0], Longitude: lrLocations[i].Coord[1]},
						Departures: []models.Departure{},
					},
				},
			})
			stopIndex++
		}
	}

	return &stops[0], stops, nil
}

func PopulateDeparturesAtStop(stop models.Stop) (models.Stop, error) {
	// Loop through each stop point and populate departures
	for i, sp := range stop.StopPoints {
		departureResp, err := CallDeparture(sp.ID)
		if err != nil || departureResp == nil {
			log.Printf("Error fetching departures for stop point %s: %v", sp.ID, err)
			return stop, err
		}
		stop.StopPoints[i].Departures = extractDepartures(*departureResp)
	}

	filteredStopPopints := []models.StopPoint{}
	for _, currStop := range stop.StopPoints {
		if len(currStop.Departures) > 0 {
			filteredStopPopints = append(filteredStopPopints, currStop)
		}
	}
	stop.StopPoints = filteredStopPopints

	return stop, nil
}

func extractDepartures(responses models.DepartureMonResponse) []models.Departure {
	var departures []models.Departure
	for _, event := range responses.StopEvents {
		if event.DepartureTimeEstimated != nil {
			// Use estimated time if available
			departures = append(departures, models.Departure{
				Time:        parseTimeToEpoch(*event.DepartureTimeEstimated),
				IsRealTime:  true,
				Description: event.Transportation.Description,
				Destination: event.Transportation.Destination.Name,
			})
		} else {
			// Fallback to planned time
			departures = append(departures, models.Departure{
				Time:        parseTimeToEpoch(event.DepartureTimePlanned),
				IsRealTime:  false,
				Description: event.Transportation.Description,
				Destination: event.Transportation.Destination.Name,
			})
		}
	}

	return departures
}

func parseTimeToEpoch(timeStr string) int64 {
	t, err := time.Parse("2006-01-02T15:04:05Z", timeStr)
	if err != nil {
		log.Printf("Error parsing time: %v", err)
		return 0
	}
	return t.Unix()
}

// return e.g. 1830 for 6:30 PM
func getCurrentTime() string {
	// Load the Sydney time zone
	loc, err := time.LoadLocation("Australia/Sydney")
	if err != nil {
		log.Printf("Error loading Sydney location: %v", err)
		return "0000"
	}

	// Get the current time in Sydney
	sydneyTime := time.Now().In(loc)

	return fmt.Sprintf("%d%02d", sydneyTime.Hour(), sydneyTime.Minute())
}

func CallDeparture(stopPointID string) (*models.DepartureMonResponse, error) {
	apiKey, err := getAPIKey("TRIP_PLANNER_API_KEY")
	if err != nil {
		log.Printf("%v", err)
		return nil, err
	}

	params := make(url.Values)
	params.Set("outputFormat", "rapidJSON")
	params.Set("coordOutputFormat", "EPSG:4326")
	params.Set("mode", "direct")
	params.Set("type_dm", "stop")
	params.Set("name_dm", stopPointID)
	params.Set("itdTime", getCurrentTime())
	params.Set("departureMonitorMacro", "true")
	params.Set("excludedMeans", "checkbox")
	params.Set("exclMOT_1", "1")  // Exclude trains
	params.Set("exclMOT_1", "2")  // Exclude buses
	params.Set("exclMOT_1", "5")  // Exclude metro
	params.Set("exclMOT_1", "7")  // Exclude coaches
	params.Set("exclMOT_1", "9")  // Exclude ferries
	params.Set("exclMOT_1", "11") // Exclude school buses
	params.Set("TfNSWDM", "true")
	params.Set("version", "10.2.1.42")

	urlStr := fmt.Sprintf("%s?%s", departureAPIEndpoint, params.Encode())

	req, err := http.NewRequest("GET", urlStr, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("accept", "application/json")
	req.Header.Set("Authorization", apiKey)

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		log.Printf("Error making request to /departure_mon: %v", err)
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		log.Printf("Error response from /departure_mon: %s", resp.Status)
		return nil, fmt.Errorf("failed to get departures: %s", resp.Status)
	}

	var apiResp models.DepartureMonResponse
	if err := json.NewDecoder(resp.Body).Decode(&apiResp); err != nil {
		log.Printf("Error decoding /departure_mon response: %v", err)
		return nil, err
	}

	return &apiResp, nil
}
