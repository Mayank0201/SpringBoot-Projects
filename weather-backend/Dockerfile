FROM gradle:8.14.2-jdk17 AS builder
#docker downloads image with linux java and gradle , check for proper versioning

WORKDIR /app

# Copy only gradle files first
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle gradle

# Download dependencies (cached layer)
RUN ./gradlew build -x test --no-daemon || true

#these 3 lines above make sure that dependencies downloaded before , because if we do docker up before
#running these and their are any code changes , it will again take forever to run

COPY . .
#copies whole project into container

RUN gradle build -x test --no-daemon
#runs gradlew build , so it will create the jar rather than depending on us

FROM eclipse-temurin:17-jdk-alpine
#downloads Linux image with java 17 on it

WORKDIR /app
#working directory

#COPY build/libs/*.jar app.jar
#Copies the JAR you built into the container.

COPY --from=builder /app/build/libs/*.jar app.jar
#copies jar from the first stage

EXPOSE 8080
#tells docker that the app uses 8080 port

ENTRYPOINT ["java", "-jar", "app.jar"]
#when container starts , this is auto ran "java -jar app.jar"

#now running docker compose up --build , it will take a longer time
#the first time since gradle dependecies get downloaded