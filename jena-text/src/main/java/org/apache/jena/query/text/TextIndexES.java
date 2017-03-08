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

package org.apache.jena.query.text;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Elastic Search V-5.2.1 Implementation of {@link TextIndex}
 *
 */
public class TextIndexES implements TextIndex {

    /**
     * The definition of the Entity we are trying to Index
     */
    private final EntityDefinition docDef ;

    /**
     * ElasticSearch Java Client to perform Index operations
     */
    private static Client client;

    /**
     * The name of the index.
     *
     */
    private String INDEX_NAME;

    static final String CLUSTER_NAME = "cluster.name";

    static final String NUM_OF_SHARDS = "number_of_shards";

    static final String NUM_OF_REPLICAS = "number_of_replicas";

    private static Logger LOGGER      = LoggerFactory.getLogger(TextIndexES.class) ;

    public TextIndexES(TextIndexConfig config, ESSettings esSettings) throws Exception{

        if(client == null) {
            LOGGER.debug("Initializing the Elastic Search Java Client with settings: " + esSettings);
            Settings settings = Settings.builder()
                    .put(CLUSTER_NAME, esSettings.getClusterName()).build();
            List<InetSocketTransportAddress> addresses = new ArrayList<>();
            for(String host: esSettings.getHostToPortMapping().keySet()) {
                InetSocketTransportAddress addr = new InetSocketTransportAddress(InetAddress.getByName(host), esSettings.getHostToPortMapping().get(host));
                addresses.add(addr);
            }

            InetSocketTransportAddress socketAddresses[] = new InetSocketTransportAddress[addresses.size()];
            client = new PreBuiltTransportClient(settings).addTransportAddresses(addresses.toArray(socketAddresses));
            LOGGER.debug("Successfully initialized the client");
        }

        this.docDef = config.getEntDef();


        IndicesExistsResponse exists = client.admin().indices().exists(new IndicesExistsRequest(INDEX_NAME)).get();
        if(!exists.isExists()) {
            Settings indexSettings = Settings.builder()
                    .put(NUM_OF_SHARDS, esSettings.getShards())
                    .put(NUM_OF_REPLICAS, esSettings.getReplicas())
                    .build();
            LOGGER.debug("Index with name " + INDEX_NAME + " does not exist yet. Creating one with settings: " + indexSettings.toString());
            client.admin().indices().prepareCreate(INDEX_NAME).setSettings(indexSettings).get();
        }

        this.INDEX_NAME = esSettings.getIndexName();

    }


    /**
     * Constructor used mainly for performing Integration tests
     * @param config an instance of {@link TextIndexConfig}
     * @param client an instance of {@link TransportClient}. The client should already have been initialized with an index
     */
    public TextIndexES(TextIndexConfig config, Client client, String indexName) {
        this.docDef = config.getEntDef();
        this.client = client;
        this.INDEX_NAME = indexName;
    }

    @Override
    public void prepareCommit() {
        //Do Nothing

    }

    @Override
    public void commit() {
        // Do Nothing
    }

    @Override
    public void rollback() {
       //Not sure what to do here

    }

    @Override
    public void close() {
        // Do Nothing

    }

    @Override
    public void updateEntity(Entity entity) {
        //Since Add entity also updates the indexed document in case it already exists,
        // we can simply call the addEntity from here.
        addEntity(entity);
    }


    @Override
    public void addEntity(Entity entity) {
        LOGGER.debug("Adding/Updating the entity in ES");

        try {
            XContentBuilder builder = jsonBuilder()
                    .startObject();

            if (docDef.getGraphField() != null) {
                builder = builder.field(docDef.getGraphField(), entity.getGraph());
            }

            for(String field: docDef.fields()) {
                builder = builder.field(field, entity.get(field));
            }

            builder = builder.endObject();
            IndexRequest indexRequest = new IndexRequest(INDEX_NAME, docDef.getEntityField(), entity.getId())
                    .source(builder);

            /**
             * We are creating an upsert request here instead of a simple insert request.
             * The reason is we want to add a document if it does not exist with the given Subject Id (URI).
             * But if the document exists with the same Subject Id, we want to do an update to it instead of deleting it and
             * then creating it with only the latest field values.
             * This functionality is called Upsert functionality and more can be learned about it here:
             * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html#upserts
             */
            UpdateRequest upReq = new UpdateRequest(INDEX_NAME, docDef.getEntityField(), entity.getId())
                    .doc(builder).upsert(indexRequest);

            UpdateResponse response = client.update(upReq).get();

            LOGGER.debug("Received the following Update response : " + response + " for the following entity: " + entity);

        } catch(Exception e) {
            throw new TextIndexException("Unable to Index the Entity in ElasticSearch.", e);
        }


    }

    /**
     * Delete an entity.
     * Since we are storing different predicate values within the same indexed document,
     * deleting the document using entity Id is sufficient to delete all the related contents for a given entity.
     * @param entity entity to delete
     */
    @Override
    public void deleteEntity(Entity entity) {

        LOGGER.debug("deleting content related to entity: " + entity.getId());
        client.prepareDelete(INDEX_NAME, docDef.getEntityField(), entity.getId()).get();

    }

    /**
     * Get an Entity given the subject Id
     * @param uri the subject Id of the entity
     * @return a map of field name and field values;
     */
    @Override
    public Map<String, Node> get(String uri) {

        GetResponse response;
        Map<String, Node> result = new HashMap<>();

        if(uri != null) {
            response = client.prepareGet(INDEX_NAME, docDef.getEntityField(), uri).get();
            if(response != null) {
                String entityField = response.getId();
                Node entity = NodeFactory.createURI(entityField) ;
                result.put(docDef.getEntityField(), entity);
                for (String field: docDef.fields()) {
                    GetField fieldResponse = response.getField(field);

                    if(fieldResponse == null || fieldResponse.getValue() == null) {
                        //We wont return it.
                        continue;
                    }
                    if(fieldResponse instanceof List<?>) {
                        //We are only interested in literal values
                        continue;
                    }
                    //We assume it will always be a String value.
                    String fieldValue = (String)fieldResponse.getValue();
                    Node fieldNode = NodeFactoryExtra.createLiteralNode(fieldValue, null, null);
                    result.put(field, fieldNode);

                }

            }
        }

        return result;
    }

    @Override
    public List<TextHit> query(Node property, String qs) {

        return query(property, qs, 0);
    }

    /**
     * Query the ElasticSearch for the given Node, with the given query String and limit.
     * @param property
     * @param qs
     * @param limit
     * @return
     */
    @Override
    public List<TextHit> query(Node property, String qs, int limit) {

        LOGGER.debug("Querying ElasticSearch for QueryString: " + qs);
        SearchResponse response = client.prepareSearch(INDEX_NAME)
                .setTypes(docDef.getEntityField())
                .setQuery(QueryBuilders.queryStringQuery(qs))
                .setFrom(0).setSize(limit)
                .get();

        List<TextHit> results = new ArrayList<>() ;
        for (SearchHit hit : response.getHits()) {

            Node literal = null;
            String field = (property != null) ? docDef.getField(property) : docDef.getPrimaryField();
            String value = (String)hit.getSource().get(field);
            if(value != null) {
                literal = NodeFactory.createLiteral(value);
            } else {
                LOGGER.debug("The value for field :" + field + " is null. Not creating a TextHit instance. Moving to the next one.");
                continue;
            }

            String entityField = hit.getId();
            Node entityNode = TextQueryFuncs.stringToNode(entityField);
            Float score = hit.getScore();
            TextHit textHit = new TextHit(entityNode, score, literal);
            results.add(textHit);

        }
        return results;
    }

    @Override
    public EntityDefinition getDocDef() {

        System.out.println("getDocDef");
        return docDef ;
    }

}
