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

package org.apache.jena.query.text.es.assembler;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextIndex;
import org.apache.jena.query.text.TextIndexConfig;
import org.apache.jena.query.text.TextIndexException;
import org.apache.jena.query.text.assembler.TextVocab;
import org.apache.jena.query.text.es.*;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.apache.jena.query.text.es.TextVocabES.*;

public class TextIndexESAssembler extends AssemblerBase {

    private static Logger LOGGER      = LoggerFactory.getLogger(TextIndexESAssembler.class) ;

    protected static final String COMMA = ",";
    protected static final String COLON = ":";
    /*
    <#index> a :TextIndexES ;
        text:serverList "127.0.0.1:9300,127.0.0.2:9400,127.0.0.3:9500" ; #Comma separated list of hosts:ports
        text:clusterName "elasticsearch"
        text:shards "1"
        text:replicas "1"
        text:entityMap <#endMap> ;
        .
    */
    
    @Override
    public TextIndex open(Assembler a, Resource root, Mode mode) {
        try {
            String listOfHostsAndPorts = GraphUtils.getAsStringValue(root, pServerList) ;
            if(listOfHostsAndPorts == null || listOfHostsAndPorts.isEmpty()) {
                throw new TextIndexException("Mandatory property text:serverList (containing the comma-separated list of host:port) property is not specified. " +
                        "An example value for the property: 127.0.0.1:9300");
            }
            String[] hosts = listOfHostsAndPorts.split(COMMA);
            Map<String,Integer> hostAndPortMapping = new HashMap<>();
            for(String host : hosts) {
                String[] hostAndPort = host.split(COLON);
                if(hostAndPort.length < 2) {
                    LOGGER.error("Either the host or the port value is missing.Please specify the property in host:port format. " +
                            "Both parts are mandatory. Ignoring this value. Moving to the next one.");
                    continue;
                }
                hostAndPortMapping.put(hostAndPort[0], Integer.valueOf(hostAndPort[1]));
            }

            String clusterName = GraphUtils.getAsStringValue(root, pClusterName);
            if(clusterName == null || clusterName.isEmpty()) {
                LOGGER.warn("ClusterName property is not specified. Defaulting to 'elasticsearch'");
                clusterName = "elasticsearch";
            }

            String numberOfShards = GraphUtils.getAsStringValue(root, pShards);
            if(numberOfShards == null || numberOfShards.isEmpty()) {
                LOGGER.warn("shards property is not specified. Defaulting to '1'");
                numberOfShards = "1";
            }

            String replicationFactor = GraphUtils.getAsStringValue(root, pReplicas);
            if(replicationFactor == null || replicationFactor.isEmpty()) {
                LOGGER.warn("replicas property is not specified. Defaulting to '1'");
                replicationFactor = "1";
            }

            String indexName = GraphUtils.getAsStringValue(root, pIndexName);
            if(indexName == null || indexName.isEmpty()) {
                LOGGER.warn("index Name property is not specified. Defaulting to 'jena-text'");
                indexName = "jena-text";
            }

            Resource r = GraphUtils.getResourceValue(root, TextVocab.pEntityMap) ;
            EntityDefinition docDef = (EntityDefinition)a.open(r) ;
            TextIndexConfig config = new TextIndexConfig(docDef);

            //We have to create an ES specific settings class in order to pass the Index Initialization specific properties.
            ESSettings settings = new ESSettings().builder()
                    .clusterName(clusterName)
                    .hostAndPortMap(hostAndPortMapping)
                    .shards(Integer.valueOf(numberOfShards))
                    .replicas(Integer.valueOf(replicationFactor))
                    .indexName(indexName)
                    .build();

            return TextESDatasetFactory.createESIndex(config, settings) ;
        } catch (Exception e) {
            throw new TextIndexException("An exception occurred while trying to open/load the Assembler configuration. ", e);
        }
    }
}
