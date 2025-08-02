FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm install -g @angular/cli
RUN ng build --configuration=production

# Just keep build output
FROM alpine:latest
WORKDIR /dist
COPY --from=builder /app/dist/streaker .

# No need to run anything — just makes files available
CMD ["sh", "-c", "echo Frontend build complete"]
