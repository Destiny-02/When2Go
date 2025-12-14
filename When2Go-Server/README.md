## When2Go Server

## Prerequisites
- Create a Trip Planner API key from [here](https://opendata.transport.nsw.gov.au/developers/api-basics)
- Create a [Open Route Service API](https://openrouteservice.org/) key
- Add a `.env` file with `TRIP_PLANNER_API_KEY=apiKey xxx` and `OPEN_ROUTE_SERVICE_API_KEY=xxx`


## Run the Server
- Uncomment the `ListenAndServe` code, and comment to AWS lambda code below
- Run the server
```
go run src/main.go
```


## Call the API
```
http://localhost:8080/
```
__Body__
```
{
    "latitude": -33.881621828090495,
    "longitude": 151.20590441515088
}
```

## Create the Deployment ZIP
```
GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -o bootstrap main.go
zip function.zip bootstrap  
```