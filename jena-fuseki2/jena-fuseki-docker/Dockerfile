## Licensed to the Apache Software Foundation (ASF) under one or more
## contributor license agreements.  See the NOTICE file distributed with
## this work for additional information regarding copyright ownership.
## The ASF licenses this file to You under the Apache License, Version 2.0
## (the "License"); you may not use this file except in compliance with
## the License.  You may obtain a copy of the License at
##
##     http://www.apache.org/licenses/LICENSE-2.0
## 
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.

## Apache Jena Fuseki server Dockerfile.

## This Dockefile builds a reduced footprint container.

ARG OPENJDK_VERSION=14
ARG ALPINE_VERSION=3.12.0
ARG JENA_VERSION=""

# Internal, passed between stages.
ARG FUSEKI_DIR=/fuseki
ARG FUSEKI_JAR=jena-fuseki-server-${JENA_VERSION}.jar
ARG JAVA_MINIMAL=/opt/java-minimal

## ---- Stage: Download and build java.
FROM openjdk:${OPENJDK_VERSION}-alpine AS base

ARG JAVA_MINIMAL
ARG JENA_VERSION
ARG FUSEKI_DIR
ARG FUSEKI_JAR
ARG REPO=https://repo1.maven.org/maven2
ARG JAR_URL=${REPO}/org/apache/jena/jena-fuseki-server/${JENA_VERSION}/${FUSEKI_JAR}

RUN [ "${JENA_VERSION}" != "" ] || { echo -e '\n**** Set JENA_VERSION ****\n' ; exit 1 ; }
RUN echo && echo "==== Docker build for Apache Jena Fuseki ${JENA_VERSION} ====" && echo

# Alpine: For objcopy used in jlink
RUN apk add --no-cache curl binutils

## -- Fuseki installed and runs in /fuseki.
WORKDIR $FUSEKI_DIR

## -- Download the jar file.
COPY download.sh .
RUN chmod a+x download.sh

# Download, with check of the SHA1 checksum.
RUN ./download.sh --chksum sha1 "$JAR_URL"

## -- Alternatives to download : copy already downloaded.
## COPY ${FUSEKI_JAR} .

## Use Docker ADD - does not retry, does not check checksum, and may run every build.
## ADD "$JAR_URL"

## -- Make reduced Java JDK
RUN \
  JDEPS="$(jdeps --multi-release base --print-module-deps --ignore-missing-deps ${FUSEKI_JAR})" && \
  jlink \
        --compress 2 --strip-debug --no-header-files --no-man-pages \
        --output "${JAVA_MINIMAL}" \
        --add-modules "${JDEPS}"

ADD entrypoint.sh .
ADD log4j2.properties .

# Run as this user
# -H : no home directorry
# -D : no password

RUN adduser -H -D fuseki fuseki

## ---- Stage: Build runtime
FROM alpine:${ALPINE_VERSION}

## Import ARGs
ARG JENA_VERSION
ARG JAVA_MINIMAL
ARG FUSEKI_DIR
ARG FUSEKI_JAR

COPY --from=base /opt/java-minimal /opt/java-minimal
COPY --from=base /fuseki /fuseki
COPY --from=base /etc/passwd /etc/passwd

WORKDIR $FUSEKI_DIR

ARG LOGS=${FUSEKI_DIR}/logs
ARG DATA=${FUSEKI_DIR}/databases

RUN \
    mkdir -p $LOGS && \
    mkdir -p $DATA && \
    chown -R fuseki ${FUSEKI_DIR} && \
    chmod a+x entrypoint.sh 

## Default environment variables.
ENV \
    JAVA_HOME=${JAVA_MINIMAL}           \
    JAVA_OPTIONS="-Xmx2048m -Xms2048m"  \
    JENA_VERSION=${JENA_VERSION}        \
    FUSEKI_JAR="${FUSEKI_JAR}"          \
    FUSEKI_DIR="${FUSEKI_DIR}"

USER fuseki

EXPOSE 3030

ENTRYPOINT ["./entrypoint.sh" ]
CMD []
