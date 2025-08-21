FROM openjdk:17
WORKDIR simulacaocredito
COPY target/simulacaocredito-0.0.1-SNAPSHOT.jar simulacaocredito.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "simulacaocredito.jar"]