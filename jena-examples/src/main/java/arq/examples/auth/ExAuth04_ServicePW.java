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
import org.apache.jena.query.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ContextAccumulator;

/**
 * Example of SERVICE to an endpoint which has a password.
 */
public class ExAuth04_ServicePW {

    static String dsName = "data";
    static DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
    static FusekiServer server;
    static String serverURL;
    static String dataURL;
    public static  String rdfString = StrUtils.strjoinNL
                                        ("PREFIX : <http://example>"
                                        , ":s :p 123 ."
                                        );

    public static void main(String ...args) {
        try {
            FusekiLogging.setLogging();
            server = ExamplesServer.startServerWithAuth(dsName, dsg, true, "u", "p");
            serverURL = "http://localhost:"+server.getPort()+"/";
            dataURL = "http://localhost:"+server.getPort()+"/"+dsName;
            RDFParser.fromString(rdfString, Lang.TTL).parse(dsg);

            exampleServiceByRegistry();

            exampleServiceByHttpClient();

        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            // The server is in the background so explicitly exit the process
            System.exit(0);
        }
    }

    private static void exampleServiceByRegistry() {
        System.out.println();
        System.out.println("Register user/password, then make SERVICE call");
        AuthEnv.get().registerUsernamePassword(URI.create(dataURL), "u", "p");
        // Local query that calls out.
        Dataset emptyLocal = DatasetFactory.empty();

        // Registration applies to SERVICE.
        Query query = QueryFactory.create("SELECT * { SERVICE <"+dataURL+"> { ?s ?p ?o } }");
        try ( QueryExecution qExec = QueryExecution.create().query(query).dataset(emptyLocal).build() ) {
            System.out.println("Call using SERVICE...");
            // Expect success because there will be a challenge and Jena will resend with the auth information.
            // Then when auth is setup, it wil be sent each time (the challenge is only on the first time).
            ResultSet rs = qExec.execSelect();
            ResultSetFormatter.out(rs);
        }

        AuthEnv.get().clearActiveAuthentication();

    }

    private static void exampleServiceByHttpClient() {
        System.out.println();
        System.out.println("Custom HttpClient + SERVICE call");
        Authenticator authenticator = AuthLib.authenticator("u", "p");
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .authenticator(authenticator)
                .build();
        Context cxt = ContextAccumulator.newBuilder().set(ARQ.httpQueryClient, httpClient).context();

        Query query = QueryFactory.create("SELECT * { SERVICE <"+dataURL+"> { ?s ?p ?o } }");
        Dataset emptyLocal = DatasetFactory.empty();
        try ( QueryExecution qExec = QueryExecution.create().query(query)
                                                            .dataset(emptyLocal)
                                                            .context(cxt)
                                                            .build() ) {
            System.out.println("Call using SERVICE...");
            ResultSet rs = qExec.execSelect();
            ResultSetFormatter.out(rs);
        }
    }
}
