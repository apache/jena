# Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0

# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --quiet --no-transfer-progress -Dstyle.color=never \
    clean package -pl jena-fuseki2/jena-fuseki-server -am -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /fuseki

COPY --from=builder /build/jena-fuseki2/jena-fuseki-server/target/jena-fuseki-server-*.jar ./jena-fuseki-server.jar
COPY demo/test/config.ttl ./config.ttl
COPY demo/test/data/ ./data/

EXPOSE 3030

ENTRYPOINT ["java", "-jar", "jena-fuseki-server.jar", "--config", "config.ttl"]
