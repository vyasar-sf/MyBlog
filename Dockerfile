# Use the official OpenJDK base image
FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y maven

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven build output (JAR file) into the container
COPY target/myblog.jar app.jar

# Expose the application port (default Spring Boot port)
EXPOSE 8080

RUN ["mvn", "clean", "install"]
WORKDIR /app/target

# Run the application
CMD ["java", "-jar", "app.jar"]
