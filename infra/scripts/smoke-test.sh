#!/bin/bash
set -e

echo "\n ✅ Checking Caddy routing..."
curl -sSf http://localhost/health

echo "\n ✅ Checking Backend health endpoint..."
curl -sSf http://localhost:8080/api/actuator/health

echo "\n ✅ Checking Redis..."
docker exec streaker-redis redis-cli ping | grep PONG

echo "\n ✅ Checking Postgres..."
docker exec streaker-postgres pg_isready

echo "\n ✅ Checking Frontend load..."
curl -sSf http://localhost:4200 | grep "<app-root"

echo "\n ✅ All services are healthy"
