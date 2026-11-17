FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY ../../backend/pom.xml .
RUN mvn -B -q dependency:go-offline
COPY ../../backend/src src
RUN mvn -B -q package -DskipTests

# Mise à jour de sécurité des paquets de l'image de base (dont dash)
RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/*

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
