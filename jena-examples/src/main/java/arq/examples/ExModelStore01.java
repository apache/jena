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

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.ModelStore;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class ExModelStore01 {

    static String dsName = "data";
    static DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
    public static  String rdfString = StrUtils.strjoinNL
            ("PREFIX : <http://example>"
            , ":s :p 123 ."
            );
    static Model someData = ModelFactory.createDefaultModel();

    public static void main(String ...args) {
        try {
            // Setup a server.
            FusekiLogging.setLogging();
            FusekiServer server = ExamplesServer.startServer(dsName, dsg, false);
            String dataURL = "http://localhost:"+server.getPort()+"/"+dsName;
            RDFParser.fromString(rdfString).lang(Lang.TTL).parse(someData);
            exampleModelStore(dataURL);

        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            // The server is in the background so explicitly exit the process
            System.exit(0);
        }
    }

    private static void exampleModelStore(String dataURL) {
        System.out.println();
        System.out.println("ModelStore - PUT");
        ModelStore.service(dataURL).defaultModel().PUT(someData);

        System.out.println();
        System.out.println("ModelStore - GET");
        Model model = ModelStore.service(dataURL).defaultModel().GET();
        System.out.println();
        RDFDataMgr.write(System.out,  model, Lang.TTL);
    }
}
