# Step 1: Build custom Caddy with rate limiting plugin
FROM caddy:builder AS builder
RUN xcaddy build \
    --with github.com/mholt/caddy-ratelimit

# Step 2: Build Angular app
FROM node:20-alpine AS build-frontend
WORKDIR /app
COPY ../../frontend/package*.json ./
RUN npm install
COPY ../../frontend ./
RUN npm install -g @angular/cli
RUN ng build --configuration=production

# Step 3: Final Caddy image
FROM alpine:3.20

RUN mkdir -p /usr/share/caddy /etc/caddy /var/log/caddy

COPY --from=builder /usr/bin/caddy /usr/bin/caddy
COPY --from=build-frontend /app/dist/* /usr/share/caddy
COPY infra/caddy/Caddyfile /etc/caddy/Caddyfile

# Create non-root user and group
RUN addgroup -S caddygroup && adduser -S caddyuser -G caddygroup

# Change ownership so caddyuser can access necessary files
RUN chown -R caddyuser:caddygroup /usr/share/caddy /etc/caddy /var/log/caddy

# Switch to non-root user
USER caddyuser

# Expose ports
EXPOSE 80 443

# Add healthcheck to verify Caddy is serving on port 80
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --spider http://localhost:80 || exit 1

ENTRYPOINT ["/usr/bin/caddy"]
CMD ["run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
