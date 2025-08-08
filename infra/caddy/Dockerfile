# Step 1: Build Angular frontend
FROM node:20-alpine AS build-frontend
WORKDIR /app
COPY ../../frontend/package*.json ./
RUN npm install
COPY ../../frontend ./
RUN npm install -g @angular/cli
RUN ng build --configuration=production

# Step 2: Build Caddy from source with Go 1.24.4-alpine
FROM golang:1.24.4-alpine AS builder

RUN apk add --no-cache git

# Clone Caddy source (shallow clone)
RUN git clone --depth 1 https://github.com/caddyserver/caddy /go/src/github.com/caddyserver/caddy

WORKDIR /go/src/github.com/caddyserver/caddy

# Build Caddy binary (fix here: no main.go argument)
RUN go build -o /usr/bin/caddy

# Step 3: Final image, minimal Alpine with Caddy binary and frontend files
FROM alpine:3.22

RUN apk add --no-cache bash wget ca-certificates

# Create caddy user and directories with correct permissions
RUN addgroup -S caddy && adduser -S -G caddy caddy && \
    mkdir -p /usr/share/caddy /etc/caddy /var/log/caddy && \
    chown -R caddy:caddy /usr/share/caddy /etc/caddy /var/log/caddy

# Copy Caddy binary from builder stage
COPY --from=builder /usr/bin/caddy /usr/bin/caddy

# Copy Angular build output from frontend build stage
COPY --from=build-frontend /app/dist/* /usr/share/caddy

# Copy your custom Caddyfile
COPY infra/caddy/Caddyfile /etc/caddy/Caddyfile

# Set ownership again just to be safe
RUN chown -R caddy:caddy /usr/share/caddy /etc/caddy /var/log/caddy

RUN touch /var/log/caddy/access.log \
 && chown caddy:caddy /var/log/caddy/access.log \
 && chmod 640 /var/log/caddy/access.log

USER caddy

EXPOSE 80 443

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --spider http://localhost:80 || exit 1

ENTRYPOINT ["caddy"]
CMD ["run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
