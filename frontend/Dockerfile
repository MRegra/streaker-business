FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm install -g @angular/cli
RUN ng build --configuration=production

# --- Final Image (Static content only) ---
FROM alpine:3.22.1

# Create a non-root user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /dist

# Copy built files and give ownership to the non-root user
COPY --from=builder /app/dist/streaker ./
RUN chown -R appuser:appgroup /dist

# Switch to non-root user
USER appuser

HEALTHCHECK CMD ["true"]

CMD ["sh", "-c", "echo Frontend build complete"]
