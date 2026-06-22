# Etapa 1: Construcción (Maven con Temurin 17)
FROM maven:3.8.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Temurin 17 JRE - más ligero y seguro)
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082 
# Nota: Cambia a 8082 en el Dockerfile de ms-pets
ENTRYPOINT ["java", "-jar", "app.jar"]