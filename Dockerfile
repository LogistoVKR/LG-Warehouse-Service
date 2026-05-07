FROM bellsoft/liberica-openjdk-alpine:21 AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw package -DskipTests -B

FROM bellsoft/liberica-openjdk-alpine:21

WORKDIR /app
COPY --from=build /app/target/lg-warehouse-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java"]
CMD ["-Dspring.profiles.active=docker", "-Duser.timezone=UTC", "-jar", "app.jar"]
