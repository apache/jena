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
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.http.auth.AuthLib;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;

/** Examples of QueryExecution with user/password */
public class ExAuth02_QueryExecutionPW {

    static String dsName = "data";
    static DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
    static FusekiServer server;
    static String serverURL;
    static String dataURL;

    public static void main(String ...args) {
        try {
            FusekiLogging.setLogging();
            server = ExamplesServer.startServerWithAuth(dsName, dsg, false, "u", "p");
            serverURL = "http://localhost:"+server.getPort()+"/";
            dataURL = "http://localhost:"+server.getPort()+"/"+dsName;

            exampleQueryAuthWithHttpClient();
            exampleQueryAuth();

        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            // The server is in the background so explicitly exit the process
            System.exit(0);
        }
    }

    // HttpClient
    public static void exampleQueryAuthWithHttpClient() {
        System.out.println();
        System.out.println("HttpClient + QueryExecutionHTTP");
        Authenticator authenticator = AuthLib.authenticator("u", "p");
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .authenticator(authenticator)
                .build();

        try ( QueryExecution qexec = QueryExecutionHTTP.service(dataURL)
                .httpClient(httpClient)
                .endpoint(dataURL)
                .queryString("ASK{}")
                .build()) {
            qexec.execAsk();
        }
    }


    public static void exampleQueryAuth() {
        System.out.println();
        // No auth
        try ( QueryExecution qExec = QueryExecutionHTTP.service(dataURL).query("ASK{}").build() ) {
            try {
                System.out.println("Query/no auth");
                qExec.execAsk();
                // NB QueryException.
            }  catch (QueryException ex) {
                System.out.println("Failed: "+ex.getMessage());
            }
        }

        System.out.println();
        //
        AuthEnv.get().clearAuthEnv();
        // This fails if the server requires digest authentication.
        // java.net.http.HttpClient only provides basic with no challenge.
        System.out.println("Register user/password (with challenge)");

        AuthEnv.get().registerUsernamePassword(URI.create(dataURL), "u", "p");

//        System.out.println("Register basic auth user/password (without challenge)");
//        // Pre-registration. No challenge step. Send password on all request, including the first request.
//        AuthEnv.get().registerBasicAuthModifier(dataURL, "u", "p");

        try ( QueryExecution qExec = QueryExecutionHTTP.service(dataURL).query("ASK{}").build() ) {
            // Expect success because there will be a challenge and Jena will resend with the auth information.
            System.out.println("Query/auth registered");
            qExec.execAsk();
        }
        AuthEnv.get().clearActiveAuthentication();
    }
}
