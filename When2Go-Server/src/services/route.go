package services

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log"
	"net/http"

	"when2go/src/models"
)

func GetWalkingDistanceAndTime(latitudeFrom float64, longitudeFrom float64, latitudeTo float64, longitudeTo float64) (int64, int64, error) {
	apiKey, err := getAPIKey("ROUTE_API_KEY")
	if err != nil {
		log.Printf("%v", err)
		return 0, 0, err
	}

	routeRequest := models.RouteRequest{
		Origins: []models.Origin{
			{
				Waypoint: models.Waypoint{
					Location: models.RouteAPILocation{
						LatLng: models.LatLng{
							Latitude:  latitudeFrom,
							Longitude: longitudeFrom,
						},
					},
				},
			},
		},
		Destinations: []models.Destination{
			{
				Waypoint: models.Waypoint{
					Location: models.RouteAPILocation{
						LatLng: models.LatLng{
							Latitude:  latitudeTo,
							Longitude: longitudeTo,
						},
					},
				},
			},
		},
		TravelMode: "WALK",
	}
	requestBody, err := json.Marshal(routeRequest)
	if err != nil {
		log.Printf("Error marshalling route request: %v", err)
		return 0, 0, err
	}
	req, err := http.NewRequest("POST", "https://routes.googleapis.com/distanceMatrix/v2:computeRouteMatrix", bytes.NewBuffer(requestBody))
	if err != nil {
		log.Printf("Error creating request to Route API: %v", err)
		return 0, 0, err
	}
	req.Header.Set("accept", "application/json")
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("X-Goog-FieldMask", "duration,distanceMeters")
	req.Header.Set("X-Goog-Api-Key", apiKey)

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		log.Printf("Error making request to Route API: %v", err)
		return 0, 0, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		log.Printf("Route API returned non-OK status: %d", resp.StatusCode)
		return 0, 0, fmt.Errorf("route api returned status: %d", resp.StatusCode)
	}

	var routeResponse models.RouteResponse
	if err := json.NewDecoder(resp.Body).Decode(&routeResponse); err != nil {
		log.Printf("Error decoding Route API response: %v", err)
		return 0, 0, err
	}

	if len(routeResponse) == 0 {
		log.Printf("No results in Route API response")
		return 0, 0, fmt.Errorf("no results in route api response")
	}

	duration, err := convertDurationStringToSeconds(routeResponse[0].Duration)
	if err != nil {
		log.Printf("Error converting duration string to seconds: %v", err)
		return 0, 0, err
	}

	return routeResponse[0].DistanceMeters, duration, nil
}

func convertDurationStringToSeconds(durationStr string) (int64, error) {
	var durationSeconds int64
	_, err := fmt.Sscanf(durationStr, "%ds", &durationSeconds)
	if err != nil {
		return 0, fmt.Errorf("invalid duration format: %s", durationStr)
	}
	return durationSeconds, nil
}
