FROM gradle:9.1-jdk25-alpine as build
WORKDIR /app
COPY . .
run gradle build --no-daemon

FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY --from=build /app/build/libs/usuario-0.0.1-SNAPSHOT.jar  app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]