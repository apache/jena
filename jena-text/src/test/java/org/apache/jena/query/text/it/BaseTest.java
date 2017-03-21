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
package org.apache.jena.query.text.it;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.BeforeClass;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by LT-Mac-Akumar on 21/03/2017.
 */
public abstract class BaseTest {

    protected static TransportClient transportClient;

    private final static String ADDRESS = "127.0.0.1";
    private final static int PORT = 9300;
    private final static String CLUSTER_NAME = "testCluster";
    protected final static String INDEX_NAME = "myIndex";

    @BeforeClass
    public static void setupTransportClient() {
        Settings settings = Settings.builder().put("cluster.name", CLUSTER_NAME).build();
        transportClient = new PreBuiltTransportClient(settings);
        try {
            transportClient.addTransportAddress(
                    new InetSocketTransportAddress(InetAddress.getByName(ADDRESS), PORT)
            );
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }


//    @BeforeClass
//    public static void testElasticsearchIsRunning() {
//        try {
//            client = new PreBuiltTransportClient(Settings.builder().put("client.transport.ignore_cluster_name", true).build())
//                    .addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress("127.0.0.1", 9300)));
//            client.admin().cluster().prepareHealth().setTimeout(TimeValue.timeValueMillis(200)).get();
//        } catch (Exception e) {
//            e.printStackTrace();
////            assumeNoException(e);
//        }
//    }

//    @AfterClass
//    public static void stopClient() {
//        if (client != null) {
//            client.close();
//            client = null;
//        }
//    }
}
