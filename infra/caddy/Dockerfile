# Build Angular frontend
FROM node:20-alpine AS build-frontend
WORKDIR /app
COPY ../../frontend/package*.json ./
RUN npm install
COPY ../../frontend ./
RUN npm install -g @angular/cli
RUN ng build --configuration=production

# Final Caddy image (use minimal Alpine)
FROM alpine:3.22.1

RUN apk add --no-cache bash wget

# Create directories if they don't exist (probably already exist)
RUN mkdir -p /usr/share/caddy /etc/caddy /var/log/caddy

# Copy Angular build output to Caddy's default web root
COPY --from=build-frontend /app/dist/* /usr/share/caddy

# Copy your custom Caddyfile
COPY infra/caddy/Caddyfile /etc/caddy/Caddyfile

# Ensure caddy user and group exist (usually they do)
RUN id caddy || (addgroup -S caddy && adduser -S -G caddy caddy)

# Set ownership on copied files and config, and log file
RUN chown -R caddy:caddy /usr/share/caddy /etc/caddy /var/log/caddy \
 && touch /var/log/caddy/access.log \
 && chown caddy:caddy /var/log/caddy/access.log

# Run as non-root user
USER caddy

EXPOSE 80 443

# Healthcheck to verify HTTP is responding on port 80
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --spider http://localhost:80 || exit 1

ENTRYPOINT ["caddy"]
CMD ["run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
