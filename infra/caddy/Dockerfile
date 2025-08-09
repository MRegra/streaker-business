# Step 1: Build Angular frontend
FROM node:20-alpine AS build-frontend
WORKDIR /app
COPY ../../frontend/package*.json ./
RUN npm ci
COPY ../../frontend ./
RUN npx @angular/cli@latest build --configuration=production

# Step 2: Final image with official Caddy
FROM caddy:2-alpine

# Copy built frontend
COPY --from=build-frontend /app/dist/ /usr/share/caddy/

# Copy Caddyfile
COPY infra/caddy/Caddyfile /etc/caddy/Caddyfile

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --spider -q http://localhost:80 || exit 1

# Ports
EXPOSE 80 443
