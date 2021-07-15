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

import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.web.AuthSetup;

/** Run a Fuseki server with HTTPS and Authentication, programmatic. */
public class ExFuseki_Https_3_Auth {
    // Setup
    // Aligned with the testing/Access/passwd file.
    static String    USER     = "user1";
    static String    PASSWORD = "pw1";
    // When using "digest", this must agree with the password file
    // for MD5 and CRYPT entries.
    static String    REALM    = "TripleStore";
    static String    HOST     = "localhost";
    static int       PORT     = 3443;
    static AuthSetup auth     = new AuthSetup(HOST, PORT, USER, PASSWORD, REALM);

    // curl -k -d 'query=ASK{}' --basic --user 'user:password' https://localhost:3443/ds

    public static void main(String...argv) {
        try {
            // By code, with client.
            codeHttpsAuth();
            client();
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    public static FusekiServer codeHttpsAuth() {
        FusekiLogging.setLogging();
        // Some empty dataset
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer server = FusekiServer.create()
            //.verbose(true)
            .https(3443, /*certStore*/"testing/Access/certs/mykey.jks", /*certStorePassword*/"cert-pw")
            .port(3030)
            .auth(AuthScheme.BASIC)
            .passwordFile("testing/Access/passwd")
            .add("/ds", dsg)
            .build();
        server.start();
        //server.join();
        return server;
    }

    private static void client() {
        //RDFConnection connSingle = RDFConnectionFactory.connect("https://localhost:3443/ds");

        // Allow self-signed
        HttpClient hc = ExamplesLib.httpClient(USER, PASSWORD);

        RDFConnection connSingle = RDFConnectionFuseki.create()
            .httpClient(hc)
            .destination("https://localhost:3443/ds")
            .build();

        try ( RDFConnection conn = connSingle ) {
            QueryExecution qExec = conn.query("ASK{}");
            QueryExecUtils.executeQuery(qExec);
        }

        HttpClient hc2 = ExamplesLib.httpClient("user1", "wrong-password");
        try ( RDFConnection conn = RDFConnectionFuseki.create()
                                    .httpClient(hc2)
                                    .destination("https://localhost:3443/ds")
                                    .build(); ) {
            QueryExecution qExec = conn.query("ASK{}");
            QueryExecUtils.executeQuery(qExec);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
