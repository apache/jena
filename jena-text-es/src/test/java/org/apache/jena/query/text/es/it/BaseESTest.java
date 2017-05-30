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
package org.apache.jena.query.text.es.it;

import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextIndexConfig;
import org.apache.jena.query.text.es.TextIndexES;
import org.apache.jena.vocabulary.RDFS;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Base Class for ElasticSearch based Integration tests.
 */
public abstract class BaseESTest {

    protected static TransportClient transportClient;

    private final static String ADDRESS = "127.0.0.1";
    private final static int PORT = 9500;
    private final static String CLUSTER_NAME = "elasticsearch";
    protected final static String INDEX_NAME = "jena-text";

    protected static TextIndexES classToTest;

    static final String DOC_TYPE = "text";

    /**
     * Make sure that we have connectivity to the locally running ES node.
     * The ES is started during the pre-integration-test phase
     */
    @BeforeClass
    public static void setupTransportClient() {

        Settings settings = Settings.builder().put("cluster.name", CLUSTER_NAME).build();
        transportClient = new PreBuiltTransportClient(settings);
        try {
            transportClient.addTransportAddress(
                    new InetSocketTransportAddress(InetAddress.getByName(ADDRESS), PORT)
            );
        } catch (UnknownHostException ex) {
            Assert.fail("Failed to create transport client" + ex.getMessage());
        }
        classToTest = new TextIndexES(config(), transportClient, INDEX_NAME);
        Assert.assertNotNull("Transport client was not created successfully", transportClient);


    }

    /**
     * Make sure that we always start we a clean index.
     * This will help keep the tests isolated
     * @throws Exception
     */
    @Before
    public void beforeTest() throws Exception{
        //Create Index
        transportClient.admin().indices().prepareCreate(INDEX_NAME).get();
        Assert.assertTrue(transportClient.admin().indices().exists(new IndicesExistsRequest(INDEX_NAME)).get().isExists());

    }

    /**
     * Make sure that we always delete the index when completed with the test
     * This will help keep the tests isolated
     * @throws Exception
     */
    @After
    public void afterTest() throws Exception{
        //Delete Index
        transportClient.admin().indices().delete(new DeleteIndexRequest(INDEX_NAME)).get();
    }

    /**
     * Simple Config for text index
     * @return
     */
    private static TextIndexConfig config() {
        EntityDefinition ed = new EntityDefinition(DOC_TYPE, "label", RDFS.label);
        ed.set("comment", RDFS.comment.asNode());
        ed.setLangField("lang");
        TextIndexConfig config = new TextIndexConfig(ed);
        return config;
    }
}
