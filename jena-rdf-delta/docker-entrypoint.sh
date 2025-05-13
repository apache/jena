#!/bin/bash
#
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
#

set -e

# Find the JAR file
JAR_FILE=$(find $DELTA_HOME/lib -name "jena-rdf-delta-*.jar" | sort | tail -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "ERROR: No JAR file found in $DELTA_HOME/lib"
    exit 1
fi

# Check if we're running the server command
if [ "$1" = "server" ]; then
    # Handle server-specific environment variables
    if [ -n "$DELTA_CLUSTER_ENABLED" ] && [ "$DELTA_CLUSTER_ENABLED" = "true" ]; then
        if [ -z "$DELTA_CLUSTER_ZOOKEEPER_CONNECT" ]; then
            echo "ERROR: DELTA_CLUSTER_ENABLED is true but DELTA_CLUSTER_ZOOKEEPER_CONNECT is not set"
            exit 1
        fi
        
        # Add the --zk option
        set -- "$@" "--zk" "$DELTA_CLUSTER_ZOOKEEPER_CONNECT"
    fi
    
    # Add the --store option if not in the arguments
    if ! echo "$@" | grep -q -- "--store"; then
        set -- "$@" "--store" "$DELTA_STORAGE_PATH"
    fi
    
    # Add the --port option if not in the arguments
    if ! echo "$@" | grep -q -- "--port"; then
        set -- "$@" "--port" "$DELTA_SERVER_PORT"
    fi
    
    # Add JMX option if metrics are enabled
    if [ -n "$DELTA_METRICS_ENABLED" ] && [ "$DELTA_METRICS_ENABLED" = "true" ]; then
        set -- "$@" "--jmx"
    fi
    
    echo "Starting RDF Delta server: java $JAVA_OPTS -jar $JAR_FILE $@"
else
    echo "Running RDF Delta command: java $JAVA_OPTS -jar $JAR_FILE $@"
fi

# Execute the command
exec java $JAVA_OPTS -jar $JAR_FILE "$@"