# Build Angular frontend
FROM node:20-alpine AS build-frontend
WORKDIR /app
COPY ../../frontend/package*.json ./
RUN npm install
COPY ../../frontend ./
RUN npm install -g @angular/cli
RUN ng build --configuration=production

# Final Caddy image (use official image with built-in Caddy)
FROM caddy:2.7.4-alpine

RUN apk add --no-cache bash wget

# Create directories (should already exist but just in case)
RUN mkdir -p /usr/share/caddy /etc/caddy /var/log/caddy

# Copy Angular build output to Caddy's default web root
COPY --from=build-frontend /app/dist/* /usr/share/caddy

# Copy your custom Caddyfile
COPY infra/caddy/Caddyfile /etc/caddy/Caddyfile

# Change ownership to caddy user (default user in official image)
RUN chown -R caddy:caddy /usr/share/caddy /etc/caddy /var/log/caddy

# Run as non-root user (default user in official image is caddy)
USER caddy

EXPOSE 80 443

# Healthcheck (checks if Caddy serves on port 80)
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --spider http://localhost:80 || exit 1

ENTRYPOINT ["caddy"]
CMD ["run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
