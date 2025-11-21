package models

type NearestLRDeparturesRequest = struct {
	Coord           Coord `json:"coord"`
	TargetStopCoord Coord `json:"stop_coord"` // If the target stop is known, return departures for this stop instead of the nearest one
}

type NearestLRDeparturesResponse struct {
	Stop     Stop   `json:"stop"`
	AllStops []Stop `json:"all_stops"` // All nearby stops, without departures populated
}

type Coord struct {
	Latitude  float64 `json:"latitude"`
	Longitude float64 `json:"longitude"`
}

// A stop can contain multiple stop points (platforms). For light rail, this is usually 2.
type Stop struct {
	ID             string      `json:"id"`   // e.g. "2000168"
	Name           string      `json:"name"` // e.g. "Haymarket"
	StopPoints     []StopPoint `json:"stop_points"`
	DistanceMeters int64       `json:"distance_meters"` // Meters
	DistanceTime   int64       `json:"distance_time"`   // Seconds
}

type StopPoint struct {
	ID         string      `json:"id"` // e.g. "2000446"
	Coord      Coord       `json:"coord"`
	Departures []Departure `json:"departures"` // Empty if not populated
}

type Departure struct {
	Time        int64  `json:"time"` // Epoch time in seconds
	IsRealTime  bool   `json:"is_real_time"`
	Description string `json:"description"` // Empty if not populated, e.g. "Randwick to Circular Quay"
	Destination string `json:"destination"` // Empty is not populated e.g. "Circular Quay"
}

// Structures for Trip Planner API responses

type CoordResponse struct {
	Locations []Location `json:"locations"`
}

type DepartureMonResponse struct {
	StopEvents []StopEvent `json:"stopEvents"`
}

type Location struct {
	ID         string    `json:"id"` // Same as STOPPOINT_GLOBAL_ID
	Name       string    `json:"name"`
	Coord      []float64 `json:"coord"` // [lat, lon]
	Properties struct {
		StopGlobalID      string  `json:"STOP_GLOBAL_ID"`
		StopPointGlobalID string  `json:"STOPPOINT_GLOBAL_ID"`
		StopAreaName      string  `json:"STOP_AREA_NAME"`      // e.g. "LR1", "LR2", "LR3"
		StopName          string  `json:"STOP_NAME"`           // e.g. Central Station
		StopLongName      string  `json:"STOP_POINT_LONGNAME"` // e.g. Central Chalmers Street Light Rail
		Distance          float64 `json:"distance"`
	} `json:"properties"`
	Parent struct {
		Parent struct {
			Name string `json:"name"`
		}
	} `json:"parent"`
}

type StopEvent struct {
	DepartureTimePlanned   string  `json:"departureTimePlanned"`
	DepartureTimeEstimated *string `json:"departureTimeEstimated"` // Optional when no realtime data
	Transportation         struct {
		Description string `json:"description"`
		Destination struct {
			Name string `json:"name"`
		} `json:"destination"`
	} `json:"transportation"`
}

// Structures for Route API

type LatLng struct {
	Latitude  float64 `json:"latitude"`
	Longitude float64 `json:"longitude"`
}

type RouteAPILocation struct {
	LatLng LatLng `json:"latLng"`
}

type Waypoint struct {
	Location RouteAPILocation `json:"location"`
}

type Origin struct {
	Waypoint Waypoint `json:"waypoint"`
}

type Destination struct {
	Waypoint Waypoint `json:"waypoint"`
}

type RouteRequest struct {
	Origins      []Origin      `json:"origins"`
	Destinations []Destination `json:"destinations"`
	TravelMode   string        `json:"travelMode"`
}

type RouteResponse []struct {
	DistanceMeters int64  `json:"distanceMeters"`
	Duration       string `json:"duration"`
}
