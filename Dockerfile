FROM openjdk:8-jre-alpine

EXPOSE 8980

ADD scripts/client /client
ADD scripts/server /server

ADD build /build