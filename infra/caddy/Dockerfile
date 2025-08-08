FROM node:20-alpine AS build-frontend
WORKDIR /app
COPY ../../frontend/package*.json ./
RUN npm install
COPY ../../frontend ./
RUN npm install -g @angular/cli
RUN ng build --configuration=production

FROM caddy:2.10.0-alpine

RUN apk add --no-cache bash wget

# Create caddy user and group early
RUN addgroup -S caddy && adduser -S -G caddy caddy

# Create required directories explicitly (they might not exist)
RUN mkdir -p /usr/share/caddy /etc/caddy /var/log/caddy

# Copy Angular build output to Caddy's default web root
COPY --from=build-frontend /app/dist/* /usr/share/caddy

# Copy custom Caddyfile
COPY infra/caddy/Caddyfile /etc/caddy/Caddyfile

# Set ownership on directories that definitely exist
RUN chown -R caddy:caddy /usr/share/caddy /etc/caddy /var/log/caddy

# Create and set permissions for log file AFTER ownership set on /var/log/caddy
RUN touch /var/log/caddy/access.log \
 && chown caddy:caddy /var/log/caddy/access.log \
 && chmod 640 /var/log/caddy/access.log

USER caddy

EXPOSE 80 443

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --spider http://localhost:80 || exit 1

ENTRYPOINT ["caddy"]
CMD ["run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
