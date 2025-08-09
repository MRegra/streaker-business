# --- Step 1: Build Angular (Angular 20) ---
FROM node:20-alpine AS build-frontend
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npx @angular/cli@20 build -c production --output-path=/app/build

# --- Step 2: Build Caddy with Go 1.24.4 and patched x/crypto ---
FROM golang:1.24.4-alpine AS caddy-builder
ARG XCADDY_VERSION=v0.3.5
ARG CADDY_VERSION=v2.8.4
# pick a patched x/crypto (>=0.35.0); using 0.38.0 here
ARG XCRYPTO_VERSION=v0.38.0

RUN apk add --no-cache git build-base curl
RUN curl -L "https://github.com/caddyserver/xcaddy/releases/download/${XCADDY_VERSION}/xcaddy_${XCADDY_VERSION#v}_linux_amd64.tar.gz" \
  | tar -xz -C /usr/local/bin

# ensure we fetch the exact versions we want
ENV CGO_ENABLED=0 \
    GOFLAGS="-trimpath -buildvcs=false" \
    GOPROXY="https://proxy.golang.org,direct" \
    GOSUMDB="sum.golang.org" \
    XCADDY_REPLACE="golang.org/x/crypto=golang.org/x/crypto@${XCRYPTO_VERSION}"

# Build a pinned Caddy with a replaced x/crypto
RUN xcaddy build ${CADDY_VERSION} --output /usr/bin/caddy

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
