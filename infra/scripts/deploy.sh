#!/bin/bash

set -e

echo "🔐 Starting full production setup..."

# === Validate required env variables ===
if [[ -z "$CR_PAT" || -z "$DISCORD_STREAKER_WEBHOOK" ]]; then
  echo "❌ Error: CR_PAT or DISCORD_STREAKER_WEBHOOK not set."
  exit 1
fi

GIT_USERNAME="mregra"
REPO="streaker"

# === Install base packages ===
apt update && apt install -y \
  curl \
  ca-certificates \
  gnupg \
  lsb-release \
  ufw \
  fail2ban

# === Install Docker (official way) ===
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
usermod -aG docker root  # Ensure root is in docker group

# === Install latest Docker Compose ===
DOCKER_COMPOSE_VERSION="2.24.6"
curl -SL "https://github.com/docker/compose/releases/download/v$DOCKER_COMPOSE_VERSION/docker-compose-linux-x86_64" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# === Login to GHCR ===
mkdir -p ~/.docker
echo "$CR_PAT" | docker login ghcr.io -u "$GIT_USERNAME" --password-stdin || true

# === Watchtower environment ===
mkdir -p infra
cat <<EOF > infra/.env
DISCORD_STREAKER_WEBHOOK=$DISCORD_STREAKER_WEBHOOK
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

# === Create deploy user ===
if ! id "deploy" &>/dev/null; then
  adduser deploy --disabled-password --gecos ""
  usermod -aG sudo deploy
  usermod -aG docker deploy
fi

# Copy root's SSH keys (assumes keys already added)
mkdir -p /home/deploy/.ssh
cp /root/.ssh/authorized_keys /home/deploy/.ssh/
chown -R deploy:deploy /home/deploy/.ssh
chmod 700 /home/deploy/.ssh
chmod 600 /home/deploy/.ssh/authorized_keys

# === Harden SSH ===
sed -i 's/#\?PermitRootLogin.*/PermitRootLogin no/' /etc/ssh/sshd_config
sed -i 's/#\?PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config
# Optional: Change SSH Port (and update UFW)
# sed -i 's/#Port 22/Port 2222/' /etc/ssh/sshd_config
# ufw allow 2222/tcp
# ufw delete allow OpenSSH

systemctl reload sshd

# === Docker network (shared) ===
docker network create streaker-net || true

# === Launch Watchtower ===
docker-compose -f infra/docker-compose.watchtower.yml --env-file infra/.env up -d

# === Launch Caddy + Backend ===
docker-compose -f infra/docker-compose.caddy.yml --profile prod up -d --build

# === Optional: Discord Notification ===
curl -H "Content-Type: application/json" \
     -X POST \
     -d "{\"content\": \"✅ Streaker production deployed successfully.\"}" \
     "$DISCORD_STREAKER_WEBHOOK" || echo "⚠️ Discord notification failed."

echo "✅ Production setup complete. Visit https://streaker.com"
