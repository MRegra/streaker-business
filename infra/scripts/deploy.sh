#!/bin/bash

set -e

echo "🚀 Starting deployment..."

# === Validate required env variables ===
if [[ -z "$CR_PAT" || -z "$DISCORD_STREAKER_WEBHOOK" ]]; then
  echo "❌ Error: CR_PAT or DISCORD_STREAKER_WEBHOOK not set."
  exit 1
fi

GIT_USERNAME="mregra"

# === Login to GHCR ===
mkdir -p ~/.docker
echo "$CR_PAT" | docker login ghcr.io -u "$GIT_USERNAME" --password-stdin || true

# === Docker network (shared) ===
docker network create streaker-net || true

# === Watchtower env for Discord notifications ===
mkdir -p infra
cat <<EOF > infra/.env
DISCORD_STREAKER_WEBHOOK=$DISCORD_STREAKER_WEBHOOK
EOF

# === Launch Watchtower ===
docker-compose -f infra/docker-compose.watchtower.yml --env-file infra/.env up -d

# === Launch Caddy + Backend ===
docker-compose -f infra/docker-compose.caddy.yml --profile prod up -d --build

# === Optional: Discord Notification ===
curl -H "Content-Type: application/json" \
     -X POST \
     -d "{\"content\": \"✅ Streaker production deployed successfully.\"}" \
     "$DISCORD_STREAKER_WEBHOOK" || echo "⚠️ Discord notification failed."

echo "✅ Deployment complete. Visit https://streaker.com"
