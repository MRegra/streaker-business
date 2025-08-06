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

# === Get Deployed Versions ===
VERSION_BACKEND=$(cat VERSION_BACKEND 2>/dev/null || echo "N/A")
VERSION_FRONTEND=$(cat VERSION_FRONTEND 2>/dev/null || echo "N/A")
VERSION_GLOBAL=$(cat VERSION_GLOBAL 2>/dev/null || echo "N/A")

# === Get Image Digests ===
DIGEST_BACKEND=$(docker image inspect ghcr.io/mregra/streaker-backend:latest --format='{{index .RepoDigests 0}}' 2>/dev/null || echo "N/A")
DIGEST_CADDY=$(docker image inspect ghcr.io/mregra/streaker-caddy-prod:latest --format='{{index .RepoDigests 0}}' 2>/dev/null || echo "N/A")

# === Format Discord Message ===
DISCORD_MESSAGE=$(cat <<EOF
**✅ Streaker Deployment Complete**
🗓️ **Date**: $(date -u +"%Y-%m-%d %H:%M:%S UTC")
🌐 **Environment**: \`staging\`

**🔢 Versions Deployed:**
- Backend: \`${VERSION_BACKEND}\`
- Frontend: \`${VERSION_FRONTEND}\`
- Global: \`${VERSION_GLOBAL}\`

**📦 Docker Images Pushed:**
- \`${DIGEST_BACKEND}\`
- \`${DIGEST_CADDY}\`
EOF
)

# === Notify Discord with Rich Info ===
curl -H "Content-Type: application/json" \
     -X POST \
     -d "$(jq -nc --arg content "$DISCORD_MESSAGE" '{content: $content}')" \
     "$DISCORD_STREAKER_WEBHOOK" || echo "⚠️ Discord notification failed."

echo "✅ Deployment & Notification complete."
