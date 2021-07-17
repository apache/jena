/*
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

package org.apache.jena.fuseki.main.examples;

import java.net.http.HttpClient;

import javax.net.ssl.SSLContext;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.util.QueryExecUtils;

/** Run a Fuseki server with HTTPS, programmatic. */
public class ExFuseki_10_Https_Setup {

    // curl -k -d 'query=ASK{}' https://localhost:3443/ds

    public static void main(String...argv) {
        try {
            // By code, with client.
            codeHttps();
            try {
                client();
            } catch (Exception ex){
                ex.printStackTrace();
            }
            Lib.sleep(30000);
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    private static void client() {
        // Need to provide a suitable HttpClient that can handle https.
        // Allow self-signed
        HttpClient hc = trustLocalhostUnsigned().build();

        RDFConnection connSingle = RDFConnectionFuseki.create()
            .httpClient(hc)
            .destination("https://localhost:3443/ds")
            .build();

        try ( RDFConnection conn = connSingle ) {
            QueryExecution qExec = conn.query("ASK{}");
            QueryExecUtils.executeQuery(qExec);
        }
    }

    public static FusekiServer codeHttps() {
        FusekiLogging.setLogging();
        // Some empty dataset
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer server = FusekiServer.create()
            .https(3443, ExConst.KEYSTORE, ExConst.KEYSTOREPASSWORD)
            .port(3030)
            .add("/ds", dsg)
            .build();
        server.start();
        //Lib.sleep(1000);
        //server.join();
        return server;
    }

    /** Create an {@code HttpClient.Builder} that trusts self-signed, localhost https connections. */
    public static HttpClient.Builder trustLocalhostUnsigned() {
        SSLContext sslContext = ExamplesLib.trustOneCert(ExConst.KEYSTORE, ExConst.KEYSTOREPASSWORD);
        return HttpClient.newBuilder().sslContext(sslContext);
    }
}
