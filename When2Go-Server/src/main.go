package main

import (
	"log"
	"net/http"
	"when2go/src/handlers"

	"github.com/rs/cors"
)

func main() {
	c := cors.New(cors.Options{
		AllowedOrigins:   []string{"https://when2go.netlify.app"},
		AllowedMethods:   []string{"POST"},
		AllowedHeaders:   []string{"Content-Type", "Authorization"},
		AllowCredentials: true,
	})

	log.Println("Starting server on :8080")

	mux := http.NewServeMux()
	mux.HandleFunc("/nearest-lr-departures", handlers.HandleNearestLRDepartures)

	handler := c.Handler(mux)

	if err := http.ListenAndServe(":8080", handler); err != nil {
		log.Fatalf("Could not start server: %s\n", err)
	}
}
