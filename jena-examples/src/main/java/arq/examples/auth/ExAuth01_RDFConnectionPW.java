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

package arq.examples.auth;

import java.net.Authenticator;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

import arq.examples.ExamplesServer;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.http.auth.AuthLib;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

/** Examples of RDFConnection with user/password */
public class ExAuth01_RDFConnectionPW {

    static String dsName = "data";
    static DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
    static FusekiServer server;
    static String serverURL;
    static String dataURL;
    public static  String rdfString = StrUtils.strjoinNL
                                        ("PREFIX : <http://example>"
                                        , ":s :p 123 ."
                                        );
    static Model someData = ModelFactory.createDefaultModel();

    public static void main(String ...args) {
        try {
            // Setup a server.
            FusekiLogging.setLogging();
            server = ExamplesServer.startServerWithAuth(dsName, dsg, true, "u", "p");
            serverURL = "http://localhost:"+server.getPort()+"/";
            dataURL = "http://localhost:"+server.getPort()+"/"+dsName;
            RDFParser.fromString(rdfString).lang(Lang.TTL).parse(someData);

            // Examples
            exampleConnectionAuthWithHttpClient();

            exampleConnectionAuthByRegistration();

        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            // The server is in the background so explicitly exit the process
            System.exit(0);
        }
    }

    // HttpClient
    public static void exampleConnectionAuthWithHttpClient() {
        System.out.println();
        System.out.println("HttpClient + RDFConnectionRemote");

        // Custom HttpClient
        Authenticator authenticator = AuthLib.authenticator("u", "p");
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .authenticator(authenticator)
                .build();
        try ( RDFConnection conn = RDFConnectionRemote.service(dataURL)
                .httpClient(httpClient) // Custom HttpClient
                .build()) {
            conn.update("INSERT DATA{}");
            conn.queryAsk("ASK{}");
        }
    }

    public static void exampleConnectionAuthByRegistration() {
        System.out.println();
        System.out.println("Register user/password + RDFConnectionRemote");
        // Register authentication.
        AuthEnv.get().registerUsernamePassword(URI.create(dataURL), "u", "p");

        // RDFConnection - no special extra setup.
        try ( RDFConnection conn1 = RDFConnectionRemote.service(dataURL).build() ) {
            System.out.println("conn1: Try to PUT model ...");
            conn1.put(someData);
        }
    }
}
