# Use an official OpenJDK image with JDK 17 for building Android projects
FROM eclipse-temurin:17-jdk

RUN apt-get update && apt-get install -y curl
# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Build the application
RUN ./gradlew build

# Expose the port the app runs on
EXPOSE 8080

# Specify the command to run on container start
CMD ["java", "-jar", "build/libs/training_thanhvh_java_spring_jwt_jpa-0.0.1-SNAPSHOT.jar"]
