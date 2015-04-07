#   Licensed to the Apache Software Foundation (ASF) under one or more
#   contributor license agreements.  See the NOTICE file distributed with
#   this work for additional information regarding copyright ownership.
#   The ASF licenses this file to You under the Apache License, Version 2.0
#   (the "License"); you may not use this file except in compliance with
#   the License.  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

FROM maven:3.3-jdk-7

# Config and data
VOLUME /fuseki
ENV FUSEKI_BASE /fuseki

# Installation folder
ENV FUSEKI_HOME /jena-fuseki

ENV DEPS pwgen
RUN export DEBIAN_FRONTEND=noninteractive && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get update && \
    apt-get install -y $DEPS && \
    apt-get autoremove -y && \
    apt-get autoclean && \
    rm -rf /var/lib/apt/lists/* && \
    rm -rf /tmp/*


WORKDIR /tmp
COPY . /tmp/
# Use target/ from host as-is, or build inside if missing
RUN if [ ! -d target/dist ] ; then mvn install ; fi && \
    mv target/dist/* $FUSEKI_HOME && rm -rf /tmp/*


# As "localhost" is often inaccessible within Docker container,
# we'll enable basic-auth with a random admin password 
# (which we'll generate on start-up)
COPY shiro.ini /jena-fuseki/shiro.ini
COPY docker-entrypoint.sh /
RUN chmod 755 /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]

# Loader scripts
COPY load.sh /jena-fuseki/
COPY tdbloader /jena-fuseki/
RUN chmod 755 /jena-fuseki/load.sh /jena-fuseki/tdbloader
#VOLUME /staging


# Where we start our server from
WORKDIR /jena-fuseki
EXPOSE 3030
CMD ["/jena-fuseki/fuseki-server"]

