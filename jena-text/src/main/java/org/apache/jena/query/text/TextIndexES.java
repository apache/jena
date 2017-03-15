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
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Elastic Search Implementation of {@link TextIndex}
 *
 */
public class TextIndexES implements TextIndex {

    /**
     * The definition of the Entity we are trying to Index
     */
    private final EntityDefinition docDef ;

    /**
     * Thread safe ElasticSearch Java Client to perform Index operations
     */
    private static Client client;

    /**
     * The name of the index. Defaults to 'test'
     */
    private final String INDEX_NAME;

    static final String CLUSTER_NAME = "cluster.name";

    static final String NUM_OF_SHARDS = "number_of_shards";

    static final String NUM_OF_REPLICAS = "number_of_replicas";

    private boolean isMultilingual ;

    private static final Logger LOGGER      = LoggerFactory.getLogger(TextIndexES.class) ;

    public TextIndexES(TextIndexConfig config, ESSettings esSettings) throws Exception{

        this.INDEX_NAME = esSettings.getIndexName();
        this.docDef = config.getEntDef();


        this.isMultilingual = config.isMultilingualSupport();
        if (this.isMultilingual &&  config.getEntDef().getLangField() == null) {
            //multilingual index cannot work without lang field
            docDef.setLangField("lang");
        }
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


        IndicesExistsResponse exists = client.admin().indices().exists(new IndicesExistsRequest(INDEX_NAME)).get();
        if(!exists.isExists()) {
            Settings indexSettings = Settings.builder()
                    .put(NUM_OF_SHARDS, esSettings.getShards())
                    .put(NUM_OF_REPLICAS, esSettings.getReplicas())
                    .build();
            LOGGER.debug("Index with name " + INDEX_NAME + " does not exist yet. Creating one with settings: " + indexSettings.toString());
            client.admin().indices().prepareCreate(INDEX_NAME).setSettings(indexSettings).get();
        }



    }


    /**
     * Constructor used mainly for performing Integration tests
     * @param config an instance of {@link TextIndexConfig}
     * @param client an instance of {@link TransportClient}. The client should already have been initialized with an index
     */
    public TextIndexES(TextIndexConfig config, Client client, String indexName) {
        this.docDef = config.getEntDef();
        this.isMultilingual = true;
        this.client = client;
        this.INDEX_NAME = indexName;
    }

    /**
     * We do not have any specific logic to perform before committing
     */
    @Override
    public void prepareCommit() {
        //Do Nothing

    }

    /**
     * Commit happens in the individual get/add/delete operations
     */
    @Override
    public void commit() {
        // Do Nothing
    }

    /**
     * not really sure what we need to roll back.
     */
    @Override
    public void rollback() {
       //Not sure what to do here

    }

    /**
     * We don't have resources that need to be closed explicitely
     */
    @Override
    public void close() {
        // Do Nothing

    }

    /**
     * Update an Entity. Since we are doing Upserts in add entity anyways, we simply call {@link #addEntity(Entity)}
     * method that takes care of updating the Entity as well.
     * @param entity the entity to update.
     */
    @Override
    public void updateEntity(Entity entity) {
        //Since Add entity also updates the indexed document in case it already exists,
        // we can simply call the addEntity from here.
        addEntity(entity);
    }


    /**
     * Add an Entity to the ElasticSearch Index.
     * The entity will be added as a new document in ES, if it does not already exists.
     * If the Entity exists, then the entity will simply be updated.
     * The entity will never be replaced.
     * @param entity the entity to add
     */
    @Override
    public void addEntity(Entity entity) {
        LOGGER.debug("Adding/Updating the entity in ES");

        //The field that has a not null value in the current Entity instance.
        //Required, mainly for building a script for the update command.
        String fieldToAdd = null;
        String fieldValueToAdd = "";
        try {
            XContentBuilder builder = jsonBuilder()
                    .startObject();

            //Currently ignoring Graph field based indexing
//            if (docDef.getGraphField() != null) {
//                builder = builder.field(docDef.getGraphField(), entity.getGraph());
//            }

            for(String field: docDef.fields()) {
                if(entity.get(field) != null) {
                    if(entity.getLanguage() != null && !entity.getLanguage().isEmpty() && isMultilingual) {
                        fieldToAdd = field + "_" + entity.getLanguage();
                    } else {
                        fieldToAdd = field;
                    }

                    fieldValueToAdd = (String) entity.get(field);
                    builder = builder.field(fieldToAdd, Arrays.asList(fieldValueToAdd));
                    break;
                } else {
                    //We are making sure that the field is at-least added to the index.
                    //This will help us tremendously when we are appending the data later in an already indexed document.
                    builder = builder.field(field, Collections.emptyList());
                }

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

            //First Search of the field exists or not
            SearchResponse existsResponse = client.prepareSearch(INDEX_NAME)
                    .setTypes(docDef.getEntityField())
                    .setQuery(QueryBuilders.existsQuery(fieldToAdd))
                    .get();
            String script;
            if(existsResponse != null && existsResponse.getHits() != null && existsResponse.getHits().totalHits() > 0) {
                //This means field already exists and therefore we should append to it
                script = "ctx._source." + fieldToAdd+".add('"+ fieldValueToAdd + "')";
            } else {
                //The field does not exists. so we create one
                script = "ctx._source." + fieldToAdd+" =['"+ fieldValueToAdd + "']";
            }



            UpdateRequest upReq = new UpdateRequest(INDEX_NAME, docDef.getEntityField(), entity.getId())
                    .script(new Script(script))
                    .upsert(indexRequest);

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

        String fieldToRemove = null;
        String valueToRemove = null;
        for(String field : docDef.fields()) {
            if(entity.get(field) != null) {
                fieldToRemove = field;
                valueToRemove = (String)entity.get(field);
                break;
            }
        }
        //First Search of the field exists or not
        SearchResponse existsResponse = client.prepareSearch(INDEX_NAME)
                .setTypes(docDef.getEntityField())
                .setQuery(QueryBuilders.existsQuery(fieldToRemove))
                .get();

        String script = null;
        if(existsResponse != null && existsResponse.getHits() != null && existsResponse.getHits().totalHits() > 0) {
            //This means field already exists and therefore we should remove from it
            script = "ctx._source." + fieldToRemove+".remove('"+ valueToRemove + "')";
        }

        UpdateRequest updateRequest = new UpdateRequest(INDEX_NAME, docDef.getEntityField(), entity.getId())
                .script(new Script(script));

        try {
            client.update(updateRequest).get();
        }catch(Exception e) {
            throw new TextIndexException("Unable to delete entity.", e);
        }


        LOGGER.debug("deleting content related to entity: " + entity.getId());
//        client.prepareDelete(INDEX_NAME, docDef.getEntityField(), entity.getId()).get();

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
            if(response != null && !response.isSourceEmpty()) {
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
     * @param property the node property to make a search for
     * @param qs the query string
     * @param limit limit on the number of records to return
     * @return List of {@link TextHit}s containing the documents that have been found
     */
    @Override
    public List<TextHit> query(Node property, String qs, int limit) {

        qs = parse(qs);
        LOGGER.debug("Querying ElasticSearch for QueryString: " + qs);
        SearchResponse response = client.prepareSearch(INDEX_NAME)
                .setTypes(docDef.getEntityField())
                .setQuery(QueryBuilders.queryStringQuery(qs))
                .setFrom(0).setSize(limit)
                .get();

        List<TextHit> results = new ArrayList<>() ;
        for (SearchHit hit : response.getHits()) {

            Node literal;
            String field = (property != null) ? docDef.getField(property) : docDef.getPrimaryField();
            List<String> value = (List<String>)hit.getSource().get(field);
            if(value != null) {
                literal = NodeFactory.createLiteral(value.get(0));
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
        return docDef ;
    }

    private String parse(String qs) {
        if (this.isMultilingual) {
            if (qs.contains(getDocDef().getLangField() + ":")) {
                String lang = qs.substring(qs.lastIndexOf(":") + 1);
                if (!"*".equals(lang)) {
                    // splice the language into the field name
                    qs = qs.replaceFirst(":", "_"+ lang + ":");
                    qs = qs.substring(0, qs.indexOf("AND"));
                }
            } else {
                qs = qs.replaceFirst(":", "\\\\*" + ":");
            }
        }
        return qs;

    }

}
