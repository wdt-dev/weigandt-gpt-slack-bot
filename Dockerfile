FROM maven:3.9-amazoncorretto-17 as build
USER root
ENV HOME=/builddir/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD pom.xml $HOME
RUN mvn verify --fail-never
ADD . $HOME
RUN mvn package -DskipTests

FROM amazoncorretto:17.0.7
ARG BUILD_FOLDER=/builddir/app/target
COPY --from=build ${BUILD_FOLDER}/*.jar /usr/gptslackbot/gptslackbot.jar
WORKDIR /usr/gptslackbot
ENTRYPOINT ["java","-jar","gptslackbot.jar"]
EXPOSE 3000