# Build the Angular app
FROM node:20-alpine AS build-frontend
WORKDIR /app
COPY ../../frontend/package*.json ./
RUN npm install
COPY ../../frontend .
RUN npm install -g @angular/cli
RUN ng build --configuration=production

# Final Caddy image
FROM caddy:alpine
COPY infra/caddy/Caddyfile /etc/caddy/Caddyfile
COPY --from=build-frontend /app/dist/* /usr/share/caddy/browser/
