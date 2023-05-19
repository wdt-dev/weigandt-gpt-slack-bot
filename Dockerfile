FROM maven:3.9-amazoncorretto-17 as build
WORKDIR /builddir/app
USER root
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests

FROM amazoncorretto:17.0.7
ARG DEPENDENCY=/builddir/app/target
COPY --from=build ${DEPENDENCY}/*.jar /usr/src/gptslackbot/gptslackbot.jar
WORKDIR /usr/src/gptslackbot
ENTRYPOINT ["java","-jar","gptslackbot.jar"]
EXPOSE 3000