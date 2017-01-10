FROM azul/zulu-openjdk:latest
MAINTAINER Max De Marzi<maxdemarzi@gmail.com>
EXPOSE 8080
ADD /conf/application.conf /conf/application.conf
ADD /target/GuancialeDB-1.0-SNAPSHOT.jar GuancialeDB-1.0-SNAPSHOT.jar
CMD ["java", "-jar", "GuancialeDB-1.0-SNAPSHOT.jar"]

