# Use an OpenJDK 17 base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the project files into the container
COPY . /app

# Create the databases directory (the SQLite file will be mounted here)
RUN mkdir -p /app/databases && touch /app/databases/labflow.db

# Expose the port that the Spring Boot app will run on
EXPOSE 8080

# Install Maven for building the project, package the Spring Boot app, and copy the JAR file to a known location
# Clear the package cache afterwards to reduce the image size
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests && \
    cp target/*.jar /app/app.jar && \
    rm -rf /var/lib/apt/lists/*

# Set up the entrypoint to run the JAR
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# The volume for the SQLite database file (this will be mounted from the host)
VOLUME ["/app/databases"]
