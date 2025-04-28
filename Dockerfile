FROM openjdk:17
ARG JAR_FILE=target/kartingrm-backend.jar
COPY ${JAR_FILE} kartingrm-backend.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/kartingrm-backend.jar"]