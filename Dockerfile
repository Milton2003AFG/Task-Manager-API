# ---- Build stage ----
FROM docker.io/library/eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy only what is needed to resolve dependencies first (better layer cache)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -DskipTests package

# ---- Runtime stage ----
FROM docker.io/library/eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring --no-create-home spring
COPY --from=build /app/target/*.jar app.jar
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]