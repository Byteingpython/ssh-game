FROM gradle:8.12-jdk21 AS build

WORKDIR /app
COPY build.gradle .
COPY settings.gradle .
COPY src ./src
RUN gradle shadowJar

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=build /app/build/libs/ssh-game-all.jar /app/ssh-game.jar
CMD ["java", "-jar", "ssh-game.jar"]