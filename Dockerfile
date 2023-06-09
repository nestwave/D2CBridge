FROM openjdk:bullseye AS device

VOLUME /tmp

ARG JAR_FILE=target/device-0.0.1-SNAPSHOT.jar

WORKDIR /opt/Nestwave
COPY target/security security
COPY start.sh .
COPY ${JAR_FILE} appDevice.jar

ENTRYPOINT exec ./start.sh
