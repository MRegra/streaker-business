#!/bin/bash

set -e

echo "🔐 Starting full production setup..."

DISCORD_WEBHOOK=${DISCORD_STREAKER_WEBHOOK}
GIT_USERNAME="mregra"
REPO="streaker"

# === Install base packages ===
apt update && apt install -y docker.io docker-compose curl ufw fail2ban

# === Login to GHCR ===
mkdir -p ~/.docker
echo "$CR_PAT" | docker login ghcr.io -u $GIT_USERNAME --password-stdin || true

# === Watchtower environment ===
mkdir -p infra
cat <<EOF > infra/.env
DISCORD_STREAKER_WEBHOOK=$DISCORD_WEBHOOK
EOF

# === Enable firewall + fail2ban ===
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw default deny incoming
ufw default allow outgoing
ufw --force enable

systemctl enable fail2ban
systemctl start fail2ban

# === Docker network (shared) ===
docker network create streaker-net || true

# === Launch Watchtower ===
docker compose -f infra/docker-compose.watchtower.yml --env-file infra/.env up -d

# === Launch Caddy + Backend ===
docker compose -f infra/docker-compose.caddy.yml --profile prod up -d --build

echo "✅ Production setup complete. Visit https://streaker.com"
