# Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY jena-fuseki-server.jar .
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
