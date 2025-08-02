# Rebuild services
docker compose build --no-cache

# Relaunch selected profiles
docker compose --profile local --profile nginx --profile watchtower -f docker-compose.yml up -d
