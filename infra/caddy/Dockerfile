# --- Step 1: Build Angular frontend ---
FROM node:20-alpine AS build-frontend
WORKDIR /app
COPY ../../frontend/package*.json ./
RUN npm install
COPY ../../frontend ./
RUN npm install -g @angular/cli
RUN ng build --configuration=production

# --- Step 2: Build Caddy with Go 1.24.4 (patched for CVE-2025-22874) ---
FROM golang:1.24.4-alpine AS caddy-builder
ARG XCADDY_VERSION=v0.3.5
RUN apk add --no-cache git build-base curl
# Install xcaddy
RUN curl -L "https://github.com/caddyserver/xcaddy/releases/download/${XCADDY_VERSION}/xcaddy_${XCADDY_VERSION#v}_linux_amd64.tar.gz" \
  | tar -xz -C /usr/local/bin
ENV CGO_ENABLED=0 GOFLAGS="-trimpath -buildvcs=false"
# Build vanilla Caddy (add --with flags here if you need plugins)
RUN xcaddy build --output /usr/bin/caddy

# --- Step 3: Minimal runtime (non-root, binds 80/443 via cap_net_bind_service) ---
FROM alpine:3.22
RUN apk add --no-cache ca-certificates libcap
# Create caddy user/group and dirs
RUN addgroup -S caddy && adduser -S -G caddy caddy && \
    mkdir -p /usr/share/caddy /etc/caddy
# Copy Caddy binary built with Go 1.24.4
COPY --from=caddy-builder /usr/bin/caddy /usr/bin/caddy
# Allow binding to low ports as non-root
RUN setcap 'cap_net_bind_service=+ep' /usr/bin/caddy
# Copy frontend and config
COPY --from=build-frontend /app/dist/ /usr/share/caddy/
COPY infra/caddy/Caddyfile /etc/caddy/Caddyfile
# Ownership
RUN chown -R caddy:caddy /usr/share/caddy /etc/caddy
USER caddy

EXPOSE 80 443
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:80 >/dev/null || exit 1

# Logs go to stdout/stderr
ENTRYPOINT ["caddy"]
CMD ["run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
