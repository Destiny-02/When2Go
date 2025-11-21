## When2Go Server

## Prerequisites
- Create a Trip Planner API key from [here](https://opendata.transport.nsw.gov.au/developers/api-basics)
- Create a [Route API](https://console.cloud.google.com/marketplace/product/google/routes.googleapis.com) key
- Add a `.env` file with `TRIP_PLANNER_API_KEY=apiKey xxx` and `ROUTE_API_KEY=xxx`


## Run the Server
```
go run src/main.go
```


## Call the API
```
http://localhost:8080/nearest-lr-departures
```
__Body__
```
{
    "latitude": -33.881621828090495,
    "longitude": 151.20590441515088
}
```