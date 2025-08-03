#!/bin/bash

set -e

echo "🛡️ Starting server provisioning..."

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
usermod -aG docker root

# === Install latest Docker Compose ===
DOCKER_COMPOSE_VERSION="2.24.6"
curl -SL "https://github.com/docker/compose/releases/download/v$DOCKER_COMPOSE_VERSION/docker-compose-linux-x86_64" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# === Enable and configure UFW + fail2ban ===
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw default deny incoming
ufw default allow outgoing
ufw --force enable

systemctl enable fail2ban
systemctl start fail2ban

# === Create non-root deploy user ===
if ! id "deploy" &>/dev/null; then
  adduser deploy --disabled-password --gecos ""
  usermod -aG sudo deploy
  usermod -aG docker deploy
fi

# === Setup SSH keys for deploy user ===
mkdir -p /home/deploy/.ssh
cp /root/.ssh/authorized_keys /home/deploy/.ssh/
chown -R deploy:deploy /home/deploy/.ssh
chmod 700 /home/deploy/.ssh
chmod 600 /home/deploy/.ssh/authorized_keys

# === Harden SSH ===
sed -i 's/#\?PermitRootLogin.*/PermitRootLogin no/' /etc/ssh/sshd_config
sed -i 's/#\?PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config
# Optional: sed -i 's/#Port 22/Port 2222/' /etc/ssh/sshd_config
# Optional: ufw allow 2222/tcp && ufw delete allow OpenSSH

systemctl reload sshd

echo "✅ Provisioning complete. Server hardened and ready for deploy."
