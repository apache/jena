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
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.http.auth.AuthLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTP;
import org.apache.jena.update.UpdateExecution;

/** Examples of UpdateExecution with user/password */
public class ExAuth03_UpdateExecutionPW {

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

            exampleUpdateAuthWithHttpClient();
            exampleUpdateAuth();

        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            // The server is in the background so explicitly exit the process
            System.exit(0);
        }
    }

    // HttpClient
    public static void exampleUpdateAuthWithHttpClient() {
        System.out.println();
        System.out.println("HttpClient + UpdateExecutionHTTP");
        Authenticator authenticator = AuthLib.authenticator("u", "p");
        HttpClient httpClient = HttpClient.newBuilder()
                //.followRedirect
                .connectTimeout(Duration.ofSeconds(10))
                .authenticator(authenticator)
                .build();

        UpdateExecutionHTTP.service(dataURL)
                .httpClient(httpClient)
                .update("CLEAR ALL")
                .execute();
    }

    public static void exampleUpdateAuth() {
        System.out.println();
        UpdateExecution uExec1 = UpdateExecutionHTTP.service(dataURL).update("INSERT DATA{}").build();
        try {
            // Expect failed because there is no authentication credentials.
            System.out.println("Update/no auth");
            uExec1.execute();
        } catch (HttpException ex) {
            System.out.println("Failed: "+ex.getMessage());
        }

        System.out.println("Register user/password + UpdateExecution");
        AuthEnv.get().registerUsernamePassword(URI.create(dataURL), "u", "p");

        UpdateExecution uExec3 = UpdateExecutionHTTP.service(dataURL)
                .update("INSERT DATA{}")
                .build();
        // Expect success because there will be a challenge and Jena will resend with the auth information.
        uExec3.execute();
        AuthEnv.get().clearActiveAuthentication();
    }
}
