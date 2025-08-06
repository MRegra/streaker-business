# Build a custom Caddy with the rate limiting plugin
FROM caddy:builder AS builder

RUN xcaddy build \
    --with github.com/mholt/caddy-ratelimit

# Build the Angular app
FROM node:20-alpine AS build-frontend
WORKDIR /app
COPY ../../frontend/package*.json ./
RUN npm install
COPY ../../frontend ./
RUN npm install -g @angular/cli
RUN ng build --configuration=production

# Final Caddy image
FROM scratch

COPY --from=builder /usr/bin/caddy /usr/bin/caddy
COPY --from=build-frontend /app/dist/* /usr/share/caddy
COPY infra/caddy/Caddyfile /etc/caddy/Caddyfile

ENTRYPOINT ["/usr/bin/caddy"]
CMD ["run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
