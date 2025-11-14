# syntax=docker/dockerfile:1

FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package dependency:copy-dependencies -DincludeScope=runtime

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/telegram-listener-1.0-SNAPSHOT.jar app.jar
COPY --from=build /workspace/target/dependency ./dependency
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -cp /app/app.jar:/app/dependency/* com.example.telegram.BotLauncher"]
