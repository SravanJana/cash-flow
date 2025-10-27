# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# cache deps layer
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn -DskipTests package -B

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Use the PORT env injected by Railway
ENV PORT=8080
EXPOSE 8080

# Provide an easy way to set JVM options at runtime
ENV JAVA_OPTS=""

CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar /app/app.jar"]
