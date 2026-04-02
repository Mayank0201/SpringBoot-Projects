#FROM gradle:8.7-jdk17 AS build

#WORKDIR /app

#COPY . .

#RUN chmod +x gradlew
#RUN ./gradlew clean build -x test

FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]