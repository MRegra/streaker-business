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

EXPOSE 80 443

ENTRYPOINT ["/usr/bin/caddy"]
CMD ["run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
