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

import java.net.URI;

import arq.examples.ExamplesServer;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;

/** Examples of Authentication setup. */
public class ExAuth00_Setup {

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
            System.out.println();

            exampleAuth();

        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            // The server is in the background so explicitly exit the process
            System.exit(0);
        }
    }

    public static void exampleAuth() {


        System.out.println("No auth");
        operation();

        System.out.println("Register user/password (with challenge)");
        AuthEnv.get().registerUsernamePassword(URI.create(dataURL), "u", "p");
        operation();

        AuthEnv.get().clearAuthEnv();

        System.out.println("Operation after environment cleared");
        operation();

        System.out.println("Register basic auth user/password (without challenge)");
        // Pre-registration. No challenge step. Send password on all request, including the first request.
        AuthEnv.get().registerBasicAuthModifier(dataURL, "u", "p");
        operation();

        // Clear active
        System.out.println("Operation, no basic auth registered");
        AuthEnv.get().clearActiveAuthentication();
        operation();

    }

    /**
     * Operation - print whether it succeed or there was an authentication rejection.
     */
    public static void operation() {
        try ( QueryExecution qExec = QueryExecutionHTTP.service(dataURL).query("ASK{}").build() ) {
            qExec.execAsk();
            System.out.println("Operation succeeded");
        }  catch (QueryException ex) {
            System.out.println("Operation failed: "+ex.getMessage());
        }
        System.out.println();
    }
}
