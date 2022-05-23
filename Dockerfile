FROM eclipse-temurin:11
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} bootcamp.movements-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/bootcamp.movements-0.0.1-SNAPSHOT.jar"]