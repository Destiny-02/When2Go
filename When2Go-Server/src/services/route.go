package services

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"net/url"

	"when2go/src/models"
)

func GetWalkingDistanceAndTime(latitudeFrom float64, longitudeFrom float64, latitudeTo float64, longitudeTo float64) (int64, int64, error) {
	apiKey, err := getAPIKey("OPEN_ROUTE_SERVICE_API_KEY")
	if err != nil {
		log.Printf("%v", err)
		return 0, 0, err
	}

	params := make(url.Values)
	params.Set("api_key", apiKey)
	params.Set("start", fmt.Sprintf("%f,%f", longitudeFrom, latitudeFrom))
	params.Set("end", fmt.Sprintf("%f,%f", longitudeTo, latitudeTo))

	directionsApiEndpoint := "https://api.openrouteservice.org/v2/directions/foot-walking"
	urlStr := fmt.Sprintf("%s?%s", directionsApiEndpoint, params.Encode())

	req, err := http.NewRequest("GET", urlStr, nil)
	if err != nil {
		log.Printf("Error creating request to ORS API: %v", err)
		return 0, 0, err
	}

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		log.Printf("Error making request to ORS API: %v", err)
		return 0, 0, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		log.Printf("ORS API returned non-OK status: %d", resp.StatusCode)
		return 0, 0, fmt.Errorf("route api returned status: %d", resp.StatusCode)
	}

	var routeResponse models.RouteResponse
	if err := json.NewDecoder(resp.Body).Decode(&routeResponse); err != nil {
		log.Printf("Error decoding ORS API response: %v", err)
		return 0, 0, err
	}

	if len(routeResponse.Features) == 0 {
		log.Printf("No results in ORS API response")
		return 0, 0, fmt.Errorf("no results in route api response")
	}

	summary := routeResponse.Features[0].Properties.Summary

	return int64(summary.Distance), int64(summary.Duration), nil
}
