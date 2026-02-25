# Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0

FROM eclipse-temurin:21-jre-alpine

WORKDIR /fuseki

COPY jena-fuseki-server.jar .
COPY config.ttl .
COPY data/ data/

EXPOSE 3030

ENTRYPOINT ["java", "-jar", "jena-fuseki-server.jar", "--config", "config.ttl"]
