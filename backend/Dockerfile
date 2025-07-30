# ----------- Build Stage ----------- #
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /build

# Cache dependencies early
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY . .
RUN ./mvnw clean package -DskipTests && cp target/*.jar target/app.jar

# ----------- Runtime Stage ----------- #
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY --from=builder /build/target/app.jar app.jar

# Expose app port
EXPOSE 8080

# Set JVM options
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Use shell form for entrypoint (allows variable expansion)
CMD java $JAVA_OPTS -jar app.jar