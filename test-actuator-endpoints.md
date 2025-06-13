# Test Actuator Endpoints

## Basic Health Check
curl http://localhost:8080/actuator/health

## Application Info  
curl http://localhost:8080/actuator/info

## All Available Endpoints
curl http://localhost:8080/actuator

## Metrics (if running on port 8081 as configured)
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/info
curl http://localhost:8081/actuator
