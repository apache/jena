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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.text.*;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.lucene.queryparser.classic.QueryParserBase;
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
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.engine.DocumentMissingException;
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
     * The name of the index. Defaults to 'jena-text'
     */
    private final String indexName;

    /**
     * The parameter representing the cluster name key
     */
    static final String CLUSTER_NAME_PARAM = "cluster.name";

    /**
     * The parameter representing the number of shards key
     */
    static final String NUM_OF_SHARDS_PARAM = "number_of_shards";

    /**
     * The parameter representing the number of replicas key
     */
    static final String NUM_OF_REPLICAS_PARAM = "number_of_replicas";

    private static final String DASH = "-";

    private static final String UNDERSCORE = "_";

    private static final String COLON = ":";

    private static final String ASTERISK = "*";

    /**
     * ES Script for adding/updating the document in the index.
     * The main reason to use scripts is because we want to modify the values of the fields that contains an array of values
     */
    private static final String ADD_UPDATE_SCRIPT = "if((ctx._source == null) || (ctx._source.<fieldName> == null) || (ctx._source.<fieldName>.empty == true)) " +
            "{ctx._source.<fieldName>=[params.fieldValue] } else {ctx._source.<fieldName>.add(params.fieldValue)}";

    /**
     * ES Script for deleting a specific value in the field for the given document in the index.
     * The main reason to use scripts is because we want to delete specific value of the field that contains an array of values
     */
    private static final String DELETE_SCRIPT = "if((ctx._source != null) && (ctx._source.<fieldToRemove> != null) && (ctx._source.<fieldToRemove>.empty != true) " +
            "&& (ctx._source.<fieldToRemove>.indexOf(params.valueToRemove) >= 0)) " +
            "{ctx._source.<fieldToRemove>.remove(ctx._source.<fieldToRemove>.indexOf(params.valueToRemove))}";

    /**
     * Number of maximum results to return in case no limit is specified on the search operation
     */
    static final Integer MAX_RESULTS = 10000;

    private static final Logger LOGGER      = LoggerFactory.getLogger(TextIndexES.class) ;

    /**
     * Construct an instance of {@link TextIndexES} based on provided {@link TextIndexConfig} and {@link ESSettings}
     * The constructor is responsible for initializing a {@link TransportClient} based on the provided configs
     * and create index based on the provided {@link ESSettings}
     * @param config an instance of {@link TextIndexConfig}
     * @param esSettings an instance of {@link ESSettings}
     */
    public TextIndexES(TextIndexConfig config, ESSettings esSettings) {

        this.indexName = esSettings.getIndexName();
        this.docDef = config.getEntDef();
        docDef.setLangField("lang");

        try {
            if(client == null) {

                LOGGER.debug("Initializing the Elastic Search Java Client with settings: " + esSettings);
                Settings settings = Settings.builder()
                        .put(CLUSTER_NAME_PARAM, esSettings.getClusterName()).build();
                List<TransportAddress> addresses = new ArrayList<>();
                for(String host: esSettings.getHostToPortMapping().keySet()) {
                    TransportAddress addr = new TransportAddress(InetAddress.getByName(host), esSettings.getHostToPortMapping().get(host));
                    addresses.add(addr);
                }

                TransportAddress socketAddresses[] = new TransportAddress[addresses.size()];
                TransportClient tc = new PreBuiltTransportClient(settings);
                tc.addTransportAddresses(addresses.toArray(socketAddresses));
                client = tc;
                LOGGER.debug("Successfully initialized the client");
            }

            IndicesExistsResponse exists = client.admin().indices().exists(new IndicesExistsRequest(indexName)).get();
            if(!exists.isExists()) {
                Settings indexSettings = Settings.builder()
                        .put(NUM_OF_SHARDS_PARAM, esSettings.getShards())
                        .put(NUM_OF_REPLICAS_PARAM, esSettings.getReplicas())
                        .build();
                LOGGER.debug("Index with name " + indexName + " does not exist yet. Creating one with settings: " + indexSettings.toString());
                client.admin().indices().prepareCreate(indexName).setSettings(indexSettings).get();
            }
        }catch (Exception e) {
            throw new TextIndexException("Exception occurred while instantiating ElasticSearch Text Index", e);
        }
    }


    /**
     * Constructor used mainly for performing Integration tests
     * @param config an instance of {@link TextIndexConfig}
     * @param client an instance of {@link TransportClient}. The client should already have been initialized with an index
     */
    public TextIndexES(TextIndexConfig config, Client client, String indexName) {
        this.docDef = config.getEntDef();
        TextIndexES.client = client;
        this.indexName = indexName;
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
     * We do not do rollback
     */
    @Override
    public void rollback() {
       //Do Nothing

    }

    /**
     * We don't have resources that need to be closed explicitly
     */
    @Override
    public void close() {
        // Do Nothing

    }

    /**
     * Update an Entity. Since we are doing Upserts in add entity anyway, we simply call {@link #addEntity(Entity)}
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
        String fieldValueToAdd = null;
        try {
            XContentBuilder builder = jsonBuilder()
                    .startObject();

            for(String field: docDef.fields()) {
                if(entity.get(field) != null) {
                    if(entity.getLanguage() != null && !entity.getLanguage().isEmpty()) {
                        //We make sure that the field name contains all underscore and no dash (for eg. when the lang value is en-GB)
                        //The reason to do this is because the script fails with exception in case we have "-" in field name.
                        fieldToAdd = normalizeFieldName(field, entity.getLanguage());
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
            IndexRequest indexRequest = new IndexRequest(indexName, docDef.getEntityField(), entity.getId())
                    .source(builder);

            String addUpdateScript = ADD_UPDATE_SCRIPT.replaceAll("<fieldName>", fieldToAdd);
            Map<String, Object> params = new HashMap<>();
            params.put("fieldValue", fieldValueToAdd);

            UpdateRequest upReq = new UpdateRequest(indexName, docDef.getEntityField(), entity.getId())
                    .script(new Script(Script.DEFAULT_SCRIPT_TYPE, Script.DEFAULT_SCRIPT_LANG, addUpdateScript, params))
                    .upsert(indexRequest);

            UpdateResponse response = client.update(upReq).get();

            LOGGER.debug("Received the following Update response : " + response + " for the following entity: " + entity);

        } catch(Exception e) {
            throw new TextIndexException("Unable to Index the Entity in ElasticSearch.", e);
        }
    }

    /**
     * Delete the value of the entity from the existing document, if any.
     * The document itself will never get deleted. Only the value will get deleted.
     * @param entity entity whose value needs to be deleted
     */
    @Override
    public void deleteEntity(Entity entity) {

        String fieldToRemove = null;
        String valueToRemove = null;
        for(String field : docDef.fields()) {
            if(entity.get(field) != null) {
                fieldToRemove = field;
                if(entity.getLanguage()!= null && !entity.getLanguage().isEmpty()) {
                    fieldToRemove = normalizeFieldName(fieldToRemove, entity.getLanguage());
                }
                valueToRemove = (String)entity.get(field);
                break;
            }
        }

        if(fieldToRemove != null && valueToRemove != null) {

            LOGGER.debug("deleting content related to entity: " + entity.getId());
            String deleteScript = DELETE_SCRIPT.replaceAll("<fieldToRemove>", fieldToRemove);
            Map<String,Object> params = new HashMap<>();
            params.put("valueToRemove", valueToRemove);

            UpdateRequest updateRequest = new UpdateRequest(indexName, docDef.getEntityField(), entity.getId())
                    .script(new Script(Script.DEFAULT_SCRIPT_TYPE, Script.DEFAULT_SCRIPT_LANG,deleteScript,params));

            try {
                client.update(updateRequest).get();
            }catch(Exception e) {
                if( ExceptionUtils.getRootCause(e) instanceof DocumentMissingException) {
                    LOGGER.debug("Trying to delete values from a missing document. Ignoring deletion of entity: ", entity);
                } else {
                    throw new TextIndexException("Unable to delete entity.", e);
                }
            }
        }
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
            response = client.prepareGet(indexName, docDef.getEntityField(), uri).get();
            if(response != null && !response.isSourceEmpty()) {
                String entityField = response.getId();
                Node entity = NodeFactory.createURI(entityField) ;
                result.put(docDef.getEntityField(), entity);
                Map<String, Object> source = response.getSource();
                for (String field: docDef.fields()) {
                    Object fieldResponse = source.get(field);

                    if(fieldResponse == null) {
                        //We wont return it.
                        continue;
                    }
                    else if(fieldResponse instanceof List<?>) {
                        //We are storing the values of fields as a List always.
                        //If there are values stored in the list, then we return the first value,
                        // else we do not include the field in the returned Map of Field -> Node Mapping
                        List<?> responseList = (List<?>)fieldResponse;
                        if(responseList != null && responseList.size() > 0) {
                            String fieldValue = (String)responseList.get(0);
                            Node fieldNode = NodeFactoryExtra.createLiteralNode(fieldValue, null, null);
                            result.put(field, fieldNode);
                        }
                    }
                }
            }
        }

        return result;
    }


    @Override
    public List<TextHit> query(Node property, String qs, String graphURI, String lang) {
        return query(property, qs, graphURI, lang, MAX_RESULTS);
    }

    @Override
    public List<TextHit> query(Node property, String qs, String graphURI, String lang, int limit, String highlight) {
        return query(property, qs, graphURI, lang, limit);
    }

    /**
     * Query the ElasticSearch for the given Node, with the given query String and limit.
     * @param property the node property to make a search for
     * @param qs the query string
     * @param limit limit on the number of records to return
     * @return List of {@link TextHit}s containing the documents that have been found
     */
    @Override
    public List<TextHit> query(Node property, String qs, String graphURI, String lang, int limit) {
        if(property != null) {
            qs = parse(property.getLocalName(), qs, lang);
        } else {
            qs = parse(null, qs, lang);
        }

        LOGGER.debug("Querying ElasticSearch for QueryString: " + qs);
        SearchResponse response = client.prepareSearch(indexName)
                .setTypes(docDef.getEntityField())
                .setQuery(QueryBuilders.queryStringQuery(qs))
                // Not fetching the source because we are currently not interested
                // in the actual values but only Id of the document. This will also speed up search
                .setFetchSource(false)
                .setFrom(0).setSize(limit)
                .get();

        List<TextHit> results = new ArrayList<>() ;
        for (SearchHit hit : response.getHits()) {

            //It has been decided to return NULL literal values for now.
            String entityField = hit.getId();
            Node entityNode = TextQueryFuncs.stringToNode(entityField);
            Float score = hit.getScore();
            TextHit textHit = new TextHit(entityNode, score, null);
            results.add(textHit);

        }
        return results;
    }

    @Override
    public List<TextHit> query(List<Resource> props, String qs, String graphURI, String lang, int limit, String highlight) {
        return query((String) null, props, qs, graphURI, lang, limit, highlight);
    }

    @Override
    public List<TextHit> query(Node subj, List<Resource> props, String qs, String graphURI, String lang, int limit, String highlight) {
        String subjectUri = subj == null || Var.isVar(subj) || !subj.isURI() ? null : subj.getURI();
        return query(subjectUri, props, qs, graphURI, lang, limit, highlight);
    }

    @Override
    public List<TextHit> query(String uri, List<Resource> props, String qs, String graphURI, String lang, int limit, String highlight) {
        Node property = props == null || props.isEmpty() ? null : props.get(0).asNode();
        return query(property, qs, graphURI, lang, limit);
    }

    @Override
    public EntityDefinition getDocDef() {
        return docDef ;
    }

    private String parse(String fieldName, String qs, String lang) {
        //Escape special characters if any in the query string
        qs = QueryParserBase.escape(qs);

        if(fieldName != null && !fieldName.isEmpty()) {
            if(lang != null && !lang.equals("none")) {
                if (!ASTERISK.equals(lang)) {
                    fieldName = fieldName + UNDERSCORE + lang.replaceAll(DASH, UNDERSCORE);
                    qs = fieldName + COLON + qs;
                } else {
                    if(!qs.contains("\\*")) {
                        fieldName = fieldName + ASTERISK;
                        qs = fieldName + COLON + qs;
                    }
                }

                } else {
                //Lang is null, but field name is not null
                qs = fieldName + COLON + qs;

            }
        }
        //We do this to enable wild card search
        return qs.replaceAll("\\*", "\\\\*");

    }

    private String normalizeFieldName(String fieldName, String lang) {
        //We know that the lang field is not null already
        StringBuilder sb = new StringBuilder(fieldName);
        return sb.append(UNDERSCORE).append(lang.replaceAll(DASH,UNDERSCORE)).toString();


    }

}
