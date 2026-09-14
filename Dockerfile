# Stage 1: Build the Spring Boot application
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
# mvnw isn't tracked as executable in git (same root cause as the CI fix),
# so a fresh clone/checkout needs this before it can be invoked at all.
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw package -DskipTests -B

# Stage 2: Run the packaged jar
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
