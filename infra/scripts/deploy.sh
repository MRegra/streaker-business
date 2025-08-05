#!/bin/bash
set -e

echo "🚀 Starting deployment..."

# === Validate required env variables ===
REQUIRED_VARS=(
  SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD
  ADMIN_USERNAME ADMIN_EMAIL ADMIN_PASSWORD
  POSTGRES_DB POSTGRES_USER POSTGRES_PASSWORD
  VALID_ORIGIN_ENDPOINT DISCORD_STREAKER_WEBHOOK DOMAIN
  GF_SECURITY_ADMIN_USER GF_SECURITY_ADMIN_PASSWORD
)

for var in "${REQUIRED_VARS[@]}"; do
  if [ -z "${!var}" ]; then
    echo "❌ Error: $var is not set."
    exit 1
  fi
done

# === Create .env file ===
mkdir -p infra/env
cat <<EOF > infra/env/.env
SPRING_DATASOURCE_URL=$SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME=$SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD=$SPRING_DATASOURCE_PASSWORD

ADMIN_USERNAME=$ADMIN_USERNAME
ADMIN_EMAIL=$ADMIN_EMAIL
ADMIN_PASSWORD=$ADMIN_PASSWORD

SPRING_PROFILES_ACTIVE=prod
REDIS_HOST=redis
REDIS_PORT=6379

POSTGRES_DB=$POSTGRES_DB
POSTGRES_USER=$POSTGRES_USER
POSTGRES_PASSWORD=$POSTGRES_PASSWORD

VALID_ORIGIN_ENDPOINT=$VALID_ORIGIN_ENDPOINT
DISCORD_STREAKER_WEBHOOK=$DISCORD_STREAKER_WEBHOOK

DOMAIN=$DOMAIN

GF_SECURITY_ADMIN_USER=$GF_SECURITY_ADMIN_USER
GF_SECURITY_ADMIN_PASSWORD=$GF_SECURITY_ADMIN_PASSWORD
GF_SERVER_ROOT_URL=/grafana

WATCHTOWER_POLL_INTERVAL=30
WATCHTOWER_CLEANUP=true
WATCHTOWER_LABEL_ENABLE=true
WATCHTOWER_INCLUDE_RESTARTING=true
WATCHTOWER_NOTIFICATIONS=shoutrrr
EOF

# === Docker login & deployment ===
echo "$CR_PAT" | docker login ghcr.io -u "$GIT_USERNAME" --password-stdin || true
docker network create streaker-net || true
docker compose down -v --remove-orphans || true
docker system prune -af --volumes || true
docker compose pull
docker compose build --no-cache
docker compose --profile prod --profile watchtower -f docker-compose.yml up -d

# === Notify Discord ===
curl -H "Content-Type: application/json" \
     -X POST \
     -d "{\"content\": \"✅ Streaker production deployed successfully.\"}" \
     "$DISCORD_STREAKER_WEBHOOK" || echo "⚠️ Discord notification failed."

echo "✅ Deployment complete."
