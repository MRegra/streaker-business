# Build Angular frontend
FROM node:20-alpine AS build-frontend
WORKDIR /app
COPY ../../frontend/package*.json ./
RUN npm install
COPY ../../frontend ./
RUN npm install -g @angular/cli
RUN ng build --configuration=production

# Final Caddy image (official Caddy Alpine image with binary pre-installed)
FROM caddy:2.10.0-alpine

RUN apk add --no-cache bash wget

# Copy Angular build output to Caddy's default web root
COPY --from=build-frontend /app/dist/* /usr/share/caddy

# Copy your custom Caddyfile
COPY infra/caddy/Caddyfile /etc/caddy/Caddyfile

# Set ownership (default user is caddy)
RUN chown -R caddy:caddy /usr/share/caddy /etc/caddy /var/log/caddy

USER root
RUN mkdir -p /var/log/caddy \
 && touch /var/log/caddy/access.log \
 && chown -R caddy:caddy /var/log/caddy \
 && chmod 750 /var/log/caddy \
 && chmod 640 /var/log/caddy/access.log
USER caddy

EXPOSE 80 443

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --spider http://localhost:80 || exit 1

ENTRYPOINT ["caddy"]
CMD ["run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
