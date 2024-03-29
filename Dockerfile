FROM eclipse-temurin:8u372-b07-jre

EXPOSE 8980

ADD scripts/client /client
ADD scripts/server /server

ADD build /build

RUN GRPC_HEALTH_PROBE_VERSION=v0.4.18 && \
    wget -qO/bin/grpc_health_probe https://github.com/grpc-ecosystem/grpc-health-probe/releases/download/${GRPC_HEALTH_PROBE_VERSION}/grpc_health_probe-linux-amd64 && \
    chmod +x /bin/grpc_health_probe

