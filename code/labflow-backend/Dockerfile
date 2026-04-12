##########
# Build stage
##########
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

# Install Maven and copy the project files into the builder image
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
COPY . .

# Build the application JAR and prepare the databases directory
RUN mvn -q -DskipTests clean package && \
    cp target/*.jar /workspace/app.jar && \
    mkdir -p /workspace/databases && \
    touch /workspace/databases/labflow.db

##########
# Runtime stage (Distroless Java 21)
##########
FROM gcr.io/distroless/java21-debian12

WORKDIR /app

# Copy the built JAR and pre-created databases directory from the builder stage
COPY --from=builder /workspace/app.jar /app/app.jar
COPY --from=builder /workspace/databases /app/databases

# Expose the port that the Spring Boot app will run on
EXPOSE 8080

# Volume for the SQLite database file (this will be mounted from the host)
VOLUME ["/app/databases"]

# Run the application with Java 21 on Distroless
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
