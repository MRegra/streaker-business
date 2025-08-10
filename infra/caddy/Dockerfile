# --- Step 1: Build Angular (Angular 20) ---
FROM node:20-alpine AS build-frontend
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npx @angular/cli@20 build -c production --output-path=/app/build

# --- Step 2: Build Caddy from source with patched x/crypto ---
FROM golang:1.24.4-alpine AS caddy-builder
ARG CADDY_VERSION=v2.8.4
ARG XCRYPTO_VERSION=v0.38.0
RUN apk add --no-cache git build-base
# get caddy source
RUN git clone --depth 1 --branch ${CADDY_VERSION} https://github.com/caddyserver/caddy.git /src/caddy
WORKDIR /src/caddy/cmd/caddy
# force patched x/crypto, tidy, and build
RUN go mod edit -replace golang.org/x/crypto=golang.org/x/crypto@${XCRYPTO_VERSION} \
 && go mod tidy
ENV CGO_ENABLED=0 GOFLAGS="-trimpath -buildvcs=false" GOPROXY="https://proxy.golang.org,direct" GOSUMDB="sum.golang.org"
RUN go build -o /usr/bin/caddy .

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

ENTRYPOINT ["/usr/bin/caddy"]
CMD ["run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
