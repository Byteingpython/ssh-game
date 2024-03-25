FROM maven:3.9.6 AS build
ARG VERSION=1.0
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn versions:set -DnewVersion=${VERSION}
RUN mvn clean package
FROM eclipse-temurin:21-jdk-alpine
ARG VERSION=1.0
WORKDIR /app

COPY --from=build /app/target/ssh-game-${VERSION}-jar-with-dependencies.jar /app/ssh-game.jar
CMD ["java", "-jar", "ssh-game.jar"]