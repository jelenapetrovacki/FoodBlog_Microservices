FROM openjdk:11.0.10

EXPOSE 8081

ADD ./build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]