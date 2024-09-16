#FROM java:8
FROM openjdk:8-jre-alpine

LABEL maintainer="Oodles Technologies"


#HEALTHCHECK --interval=50s --timeout=30s --retries=3 \
# CMD curl --silent --fail localhost:80 || exit 1

RUN apk add git
#COPY /var/lib/jenkins/workspace/stage_account  /opt/
RUN mkdir /opt/account
RUN apk --update add \
    fontconfig \
    ttf-dejavu


COPY target/DashboardAccounting-0.0.1-SNAPSHOT.jar /opt/dashaccounting.jar

RUN md5sum /opt/dashaccounting.jar

COPY ./docker/entrypoint.sh /

ENTRYPOINT java  -jar   /opt/dashaccounting.jar

