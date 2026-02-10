FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY --from=build /app/target/backend-*.jar app.jar

RUN mkdir -p uploads/videos uploads/thumbnails uploads/tmp

EXPOSE 3000
EXPOSE 3002

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
