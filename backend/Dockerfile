# ----------- Build Stage ----------- #
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /build

# Cache dependencies early
COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests && cp target/*.jar target/app.jar

# ----------- Runtime Stage ----------- #
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Create non-root user
RUN useradd -m appuser

COPY --from=builder /build/target/app.jar app.jar

# Set ownership to appuser
RUN chown appuser:appuser app.jar

# Drop privileges
USER appuser

# Expose app port
EXPOSE 8080

# Set JVM options
ENV JAVA_OPTS="-Xms256m -Xmx512m -Dlogging.level.root=DEBUG -Dlogging.level.org.springframework.web=TRACE"

# Add healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
  
# Use shell form for entrypoint (allows variable expansion)
CMD java $JAVA_OPTS -jar app.jar