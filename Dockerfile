FROM openjdk:17-oracle
ARG JAR_FILE=target/*.war
COPY ./target/yet-AnotherDiskOpenAPI-0.0.1.war app.war
ENTRYPOINT ["java", "-jar", "/app.war"]