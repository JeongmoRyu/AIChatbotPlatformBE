# Base image for Gradle 8.6 and OpenJDK 17
# Git Guardian test
FROM gradle:8.6-jdk17 as builder

# Set working directory inside the container
WORKDIR /app

# Copy Gradle wrapper and build files
COPY build.gradle settings.gradle /app/

# Copy the source code
COPY src /app/src

# Build the application (this will create the jar file in /app/build/libs)
RUN gradle build --no-daemon

# Second stage: Use a lightweight JDK image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built jar file from the previous stage
COPY --from=builder /app/build/libs/*.jar /app/service.jar

# Create necessary log directories
RUN mkdir -p /data/files/chathub/doc \
  && mkdir -p /data/files/chathub/image \
  && mkdir -p /data/files/chathub/s3 \
  && mkdir -p /data/logs/chathub/info \
  && mkdir -p /data/logs/chathub/err \
  && mkdir -p /data/logs/chathub/backup

# Create start script
RUN echo "#!/bin/sh" >> /app/start.sh \
  && echo "if [ -z \"\${ENV}\" ]; then" >> /app/start.sh \
  && echo "  echo 'No ENV variable set. Exiting...'" >> /app/start.sh \
  && echo "  exit 1" >> /app/start.sh \
  && echo "fi" >> /app/start.sh \
  && echo "java -jar -Dspring.profiles.active=\${ENV} -Djasypt.encryptor.password=\${JASYPT_ENCRYPTOR_PASSWORD} /app/service.jar" >> /app/start.sh

# Make the start script executable
RUN chmod +x /app/start.sh

# Expose necessary ports
EXPOSE 9993 9994

# Set environment variable and run the JAR file
CMD ["/app/start.sh"]