# Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0

ARG BASE_IMAGE=fuseki-ai
ARG BASE_TAG=6.1.0-SNAPSHOT

FROM ${BASE_IMAGE}:${BASE_TAG}

COPY demo/loader/entrypoint.sh /fuseki/entrypoint.sh
RUN chmod +x /fuseki/entrypoint.sh

ENTRYPOINT ["/fuseki/entrypoint.sh"]
