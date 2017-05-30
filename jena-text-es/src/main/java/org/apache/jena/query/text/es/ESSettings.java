/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.query.text.es;

import java.util.HashMap;
import java.util.Map;

/**
 * Settings for ElasticSearch based indexing
 */
public class ESSettings {

    /**
     * Map of hosts and ports. The host could also be an IP Address
     */
    private Map<String,Integer> hostToPortMapping;

    /**
     * Name of the Cluster. Defaults to 'elasticsearch'
     */
    private String clusterName;

    /**
     * Number of shards. Defaults to '1'
     */
    private Integer shards;

    /**
     * Number of replicas. Defaults to '1'
     */
    private Integer replicas;

    /**
     * Name of the index. Defaults to 'jena-text'
     */
    private String indexName;


    public Map<String, Integer> getHostToPortMapping() {
        return hostToPortMapping;
    }

    public void setHostToPortMapping(Map<String, Integer> hostToPortMapping) {
        this.hostToPortMapping = hostToPortMapping;
    }

    public ESSettings.Builder builder() {
        return new ESSettings.Builder();
    }

    /**
     * Convenient builder class for building ESSettings
     */
    public static class Builder {

        ESSettings settings;

        public Builder() {
            this.settings = new ESSettings();
            this.settings.setClusterName("elasticsearch");
            this.settings.setShards(1);
            this.settings.setReplicas(1);
            this.settings.setHostToPortMapping(new HashMap<>());
            this.settings.setIndexName("jena-text");
        }


        public Builder indexName(String indexName) {
            if(indexName != null && !indexName.isEmpty()) {
                this.settings.setIndexName(indexName);
            }
            return this;
        }

        public Builder clusterName(String clusterName) {
            if(clusterName != null && !clusterName.isEmpty()) {
                this.settings.setClusterName(clusterName);
            }
            return this;

        }

        public Builder shards(Integer shards) {
            if (shards != null) {
                this.settings.setShards(shards);
            }
            return this;
        }

        public Builder replicas(Integer replicas) {
            if(replicas != null) {
                this.settings.setReplicas(replicas);
            }
            return this;
        }

        public Builder hostAndPort(String host, Integer port) {
            if(host != null && port != null) {
                this.settings.getHostToPortMapping().put(host, port);
            }
            return this;

        }

        public Builder hostAndPortMap(Map<String, Integer> hostAndPortMap) {
            if(hostAndPortMap != null) {
                this.settings.getHostToPortMapping().putAll(hostAndPortMap);
            }

            return this;
        }

        public ESSettings build() {
            return this.settings;
        }

    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Integer getShards() {
        return shards;
    }

    public void setShards(Integer shards) {
        this.shards = shards;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }


    @Override
    public String toString() {
        return "ESSettings{" +
                "hostToPortMapping=" + hostToPortMapping +
                ", clusterName='" + clusterName + '\'' +
                ", shards=" + shards +
                ", replicas=" + replicas +
                ", indexName='" + indexName +
                '}';
    }
}
