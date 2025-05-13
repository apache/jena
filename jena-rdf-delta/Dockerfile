# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM eclipse-temurin:11-jre

# Environment variables
ENV DELTA_HOME=/opt/delta
ENV DELTA_DATA=/var/lib/delta-server
ENV DELTA_SERVER_PORT=1066
ENV DELTA_STORAGE_PATH=/var/lib/delta-server/store
ENV JAVA_OPTS="-Xmx1g"

# Create directories
RUN mkdir -p ${DELTA_HOME}/lib ${DELTA_DATA}/store ${DELTA_DATA}/backup ${DELTA_DATA}/config

# Add non-root user
RUN groupadd -r delta && useradd -r -g delta delta
RUN chown -R delta:delta ${DELTA_DATA}

# Install required packages
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy application files
COPY target/jena-rdf-delta-*.jar ${DELTA_HOME}/lib/
COPY src/main/resources/delta-server-config.properties ${DELTA_DATA}/config/
COPY docker-entrypoint.sh /

# Make the entrypoint script executable
RUN chmod +x /docker-entrypoint.sh

# Expose the server port
EXPOSE ${DELTA_SERVER_PORT}

# Set the user to run the container
USER delta

# Set the working directory
WORKDIR ${DELTA_HOME}

# Entrypoint
ENTRYPOINT ["/docker-entrypoint.sh"]

# Default command
CMD ["server", "--config", "/var/lib/delta-server/config/delta-server-config.properties"]