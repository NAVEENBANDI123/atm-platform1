# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd -r atm && useradd -r -g atm atm
COPY --from=build /app/target/*.jar app.jar
USER atm
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
