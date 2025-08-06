#!/bin/bash
set -e

echo "📦 Checking for existing Docker installation..."
if command -v docker &> /dev/null; then
  CURRENT_VERSION=$(docker --version | awk '{print $3}' | sed 's/,//')
  echo "🧠 Installed Docker version: $CURRENT_VERSION"
else
  CURRENT_VERSION="none"
  echo "🚫 Docker is not currently installed."
fi

echo "📡 Preparing to fetch latest Docker version info..."

# Prerequisites
sudo apt-get update -qq
sudo apt-get install -y -qq ca-certificates curl gnupg lsb-release

# Add Docker GPG and repo if not already added
if [ ! -f /etc/apt/keyrings/docker.gpg ]; then
  echo "🔐 Adding Docker’s official GPG key..."
  sudo install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
      sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  sudo chmod a+r /etc/apt/keyrings/docker.gpg
fi

# Add Docker repo (safe to overwrite)
echo "📁 Setting up Docker repository..."
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update -qq

echo "🔍 Fetching latest Docker version from apt repo..."
LATEST_VERSION=$(apt-cache madison docker-ce | head -n 1 | awk '{print $3}')

if [ "$CURRENT_VERSION" = "$LATEST_VERSION" ]; then
  echo "✅ Docker is already up to date (v$CURRENT_VERSION). No changes made."
  exit 0
else
  echo "🚀 Installing/updating to Docker v$LATEST_VERSION..."

  # Clean install (in case older version or partial install)
  sudo apt-get remove -y docker docker-engine docker.io containerd runc || true

  sudo apt-get install -y \
      docker-ce="$LATEST_VERSION" \
      docker-ce-cli="$LATEST_VERSION" \
      containerd.io \
      docker-buildx-plugin \
      docker-compose-plugin

  echo "✅ Docker v$LATEST_VERSION installed successfully!"
fi

# Optional: Allow Docker without sudo
if groups $USER | grep &>/dev/null '\bdocker\b'; then
  echo "👤 User '$USER' already in 'docker' group."
else
  echo "👤 Adding '$USER' to 'docker' group..."
  sudo usermod -aG docker $USER
  echo "🔁 Log out and back in or run 'newgrp docker' to apply group change."
fi

# Final test
echo "🧪 Verifying installation..."
docker --version
docker compose version || true

echo "🎉 Done! Docker is ready to use."
