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

package arq.examples;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.sys.HttpRequestModifier;
import org.apache.jena.http.sys.RegistryRequestModifier;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkHTTP;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.http.GSP;

/** Example modifier of HTTP requests (sets "X-Tracker" header). */
public class ExModification01 {

    static String dsName = "data";
    static DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
    static FusekiServer server;
    static String serverURL;
    static String dataURL;


    public static void main(String ...args) {
        try {
            FusekiLogging.setLogging();
            server = ExamplesServer.startServer(dsName, dsg, true);
            serverURL = "http://localhost:"+server.getPort()+"/";
            dataURL = "http://localhost:"+server.getPort()+"/"+dsName;

            exampleMod();

        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            // The server is in the background so explicitly exit the process
            System.exit(0);
        }
    }

    private static void exampleMod() {
        AtomicLong counter = new AtomicLong(0);

        HttpRequestModifier modifier = (params, headers)->{
            long x = counter.incrementAndGet();
            headers.put("X-Tracker", "Call="+x);
        };
        RegistryRequestModifier.get().addPrefix(serverURL, modifier);
        //RegistryRequestModifier.get().add(dataURL, modifier);

        // GSP : NO MODIFICATION no call to modifyByService
        // GSP : calls HttpRDF
        // Pass in request or at least request type.
        /*
org.apache.jena.http.sys.RegistryRequestModifier.get()
auth.examples.ExModification01.exampleMod()

org.apache.jena.http.HttpLib.modifyByService(String, Context, Params, Map<String, String>)
  org.apache.jena.sparql.exec.http.UpdateExecHTTP.execute()
  org.apache.jena.sparql.exec.http.QueryExecHTTP.query(String)
  ==> SERVICE via QueryExecHTTP
  ==> GSP ?

org.apache.jena.sparql.exec.http.TestService.runWithModifier(String, HttpRequestModifier, Runnable)

         */
        GSP.service(dataURL).defaultGraph().GET();

        try ( RDFLink link = RDFLinkHTTP.service(dataURL).build() ) {
            boolean b = link.queryAsk("ASK{}");
        }
    }
}
