FROM eclipse-temurin:17-jre

RUN groupadd --system app && useradd --system --gid app --create-home --home-dir /home/app app

WORKDIR /app

COPY build/libs/*.jar app.jar

RUN chown app:app /app/app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-XX:+UseG1GC","-jar","app.jar"]