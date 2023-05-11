FROM openjdk:17-jdk-alpine
WORKDIR /usr/src/gptslackbot
#VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} ./gptslackbot.jar
ENTRYPOINT ["java","-jar","./gptslackbot.jar"]
