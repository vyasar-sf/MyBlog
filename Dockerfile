# Use OpenJDK 17 as the base image
FROM openjdk:17-jdk-slim

# Install Maven to build the project
RUN apt-get update && apt-get install -y maven

# Set the working directory for the application
WORKDIR /app

# Copy all project files into the container
COPY . .

# Expose the default Spring Boot port
EXPOSE 8080

# Build the application using Maven
RUN ["mvn", "clean", "install", "-DskipTests"]

# Switch to the directory containing the built JAR file
WORKDIR /app/target

# Set the command to run the application
CMD ["java", "-jar", "myblog-0.3.0.jar"]
