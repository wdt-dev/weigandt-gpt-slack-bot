FROM amazoncorretto:17.0.7
WORKDIR /usr/src/gptslackbot
#VOLUME /tmp
COPY target/*.jar gptslackbot.jar
ENTRYPOINT ["java","-jar","./gptslackbot.jar"]
EXPOSE 3000