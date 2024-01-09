FROM openjdk:bullseye AS device

VOLUME /tmp

RUN useradd -g users -m -s /bin/bash nestwave

WORKDIR /opt/Nestwave
COPY target/device-0.0.1-SNAPSHOT.jar appDevice.jar
COPY --chown=nestwave:users target/security/ security/
COPY start.sh .

ENTRYPOINT exec su nestwave ./start.sh
