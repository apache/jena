FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY jena-fuseki-server.jar .
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
