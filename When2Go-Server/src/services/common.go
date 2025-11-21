package services

import (
	"fmt"
	"log"
	"os"

	"github.com/joho/godotenv"
)

func getAPIKey(keyKey string) (string, error) {
	if err := godotenv.Load(); err != nil {
		log.Println("No .env file found or error loading .env")
	}
	apiKey := os.Getenv(keyKey)
	if apiKey == "" {
		return "", fmt.Errorf("API key not set in environment variable %s", keyKey)
	}
	return apiKey, nil
}
