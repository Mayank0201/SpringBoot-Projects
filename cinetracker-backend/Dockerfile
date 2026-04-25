# Stage 1: Build stage
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy gradle files first for better caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Give execution permission to gradlew
RUN chmod +x gradlew

# Download dependencies (caching layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code and build
COPY src src
RUN ./gradlew bootJar --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# The application will use the PORT environment variable provided by Render

# Run the application with optimized memory settings for 512MB RAM
ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-Xss256k", "-XX:TieredStopAtLevel=1", "-XX:MaxMetaspaceSize=128m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]