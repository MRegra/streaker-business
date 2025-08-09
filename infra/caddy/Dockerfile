# --- Step 1: Build Angular (Angular 20) ---
FROM node:20-alpine AS build-frontend
WORKDIR /app

# Paths are relative to the build context root (your repo root in compose)
COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
# Deterministic output path
RUN npx @angular/cli@20 build -c production --output-path=/app/build

# --- Step 2: Build Caddy (xcaddy) ---
FROM golang:1.24.4-alpine AS caddy-builder
ARG XCADDY_VERSION=v0.3.5
RUN apk add --no-cache git build-base curl
RUN curl -L "https://github.com/caddyserver/xcaddy/releases/download/${XCADDY_VERSION}/xcaddy_${XCADDY_VERSION#v}_linux_amd64.tar.gz" \
  | tar -xz -C /usr/local/bin
ENV CGO_ENABLED=0 GOFLAGS="-trimpath -buildvcs=false"
RUN xcaddy build --output /usr/bin/caddy

# --- Step 3: Minimal runtime (non-root) ---
FROM alpine:3.22
RUN apk add --no-cache ca-certificates libcap curl
RUN addgroup -S caddy && adduser -S -G caddy caddy && \
    mkdir -p /usr/share/caddy /etc/caddy
COPY --from=caddy-builder /usr/bin/caddy /usr/bin/caddy
RUN setcap 'cap_net_bind_service=+ep' /usr/bin/caddy

# Copy built SPA + Caddyfile
COPY --from=build-frontend /app/build/ /usr/share/caddy/
COPY infra/caddy/Caddyfile /etc/caddy/Caddyfile

RUN chown -R caddy:caddy /usr/share/caddy /etc/caddy
USER caddy

EXPOSE 80 443

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -fsS http://localhost/health || curl -fsS http://localhost/ || exit 1

ENTRYPOINT ["caddy"]
CMD ["run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]