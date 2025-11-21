package handlers

import (
	"encoding/json"
	"log"
	"net/http"
	"when2go/src/models"
	"when2go/src/services"
)

func HandleNearestLRDepartures(w http.ResponseWriter, r *http.Request) {
	log.Println("HandleNearestLRDepartures")

	var req models.NearestLRDeparturesRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request", http.StatusBadRequest)
		return
	}

	// Find nearest stop, including the stop points
	stopSearchLat := req.Coord.Latitude
	stopSearchLon := req.Coord.Longitude
	// If a target stop coordinate is provided, use that instead
	if req.TargetStopCoord.Latitude != 0 && req.TargetStopCoord.Longitude != 0 {
		stopSearchLat = req.TargetStopCoord.Latitude
		stopSearchLon = req.TargetStopCoord.Longitude
	}
	stop, allStops, err := services.CoordToNearestLRStop(stopSearchLat, stopSearchLon)
	if err != nil || stop == nil {
		http.Error(w, "Failed to get nearest stop ID", http.StatusInternalServerError)
		return
	}
	log.Printf("Nearest stop ID: %s with %d stop points", stop.ID, len(stop.StopPoints))

	// Get the departures at each stop point
	stopWithDepartures, err := services.PopulateDeparturesAtStop(*stop)
	if err != nil {
		http.Error(w, "Failed to get departures", http.StatusInternalServerError)
		return
	}

	// Get walking distance and time to the stop
	distanceMeters, distanceTime, err := services.GetWalkingDistanceAndTime(req.Coord.Latitude, req.Coord.Longitude, stop.StopPoints[0].Coord.Latitude, stop.StopPoints[0].Coord.Longitude)
	if err != nil {
		http.Error(w, "Failed to get walking distance and time", http.StatusInternalServerError)
		return
	}

	stopWithDepartures.DistanceMeters = distanceMeters
	stopWithDepartures.DistanceTime = distanceTime
	response := models.NearestLRDeparturesResponse{
		Stop:     stopWithDepartures,
		AllStops: allStops,
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}
