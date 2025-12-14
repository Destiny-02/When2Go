package main

import (
	"log"
	"net/http"

	"when2go/src/handlers"

	"github.com/aws/aws-lambda-go/lambda"
	"github.com/awslabs/aws-lambda-go-api-proxy/httpadapter"
)

func main() {
	mux := http.NewServeMux()
	mux.HandleFunc("/", handlers.HandleNearestLRDepartures)

	// Uncomment for local development
	// if err := http.ListenAndServe(":8080", handler); err != nil {
	// 	log.Fatalf("Could not start server: %s\n", err)
	// }

	adapter := httpadapter.New(mux)

	log.Println("Starting Lambda handler")
	lambda.Start(adapter.ProxyWithContext)
}
