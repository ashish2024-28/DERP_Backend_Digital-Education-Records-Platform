# -------- STAGE 1 : Build --------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

#Inside the container, it creates a folder /app and moves into it.
#Everything from now runs inside /app.
WORKDIR /app

#Step 1: copy only pom.xml.
#Step 2: download all dependencies.
COPY pom.xml .
RUN mvn dependency:go-offline

#Now it copies your actual source code.
COPY src ./src
#Compiles your Java code , Runs Spring Boot build
#Generates: .jar
RUN mvn clean package -DskipTests

# -------- STAGE 2 : Run --------
#FROM eclipse-temurin:21-jdk
FROM eclipse-temurin:21-jdk-jammy

#Again create /app.
WORKDIR /app

#This is the magic line.
#It copies the JAR file #from the builder stage #into this new slim container.
#So: #Maven stays behind #Only the built JAR moves forward
#Final image is clean.
COPY --from=builder /app/target/crud_operation-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "crud_operation-0.0.1-SNAPSHOT.jar"]