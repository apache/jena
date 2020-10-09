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

import java.io.IOException;

import org.apache.http.entity.EntityTemplate;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.FusekiVocab;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.web.HttpCaptureResponse;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.riot.web.HttpResponseLib;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class Shacl03_FusekiValidationService {

    public static void main(String ...a) throws IOException {
        FusekiLogging.setLogging();
        // If not standard registration...
        Operation op = Operation.alloc(FusekiVocab.NS+"shacl", "shacl", "SHACL valdiation");
//        FusekiExt.registerOperation(op, new SHACL_Validation());
//        FusekiExt.addDefaultEndpoint(op, "shacl");
        //Operation op = Operation.Shacl;
        FusekiServer server =
            FusekiServer.create()
                .port(3030)
                .add("/ds", DatasetGraphFactory.createTxnMem(), true)
                .addEndpoint("/ds", "shacl", op)
                .build();
        try {
            server.start();
            try ( RDFConnection conn = RDFConnectionFactory.connect("http://localhost:3030/ds")) {
                conn.put("fu-data.ttl");
            }

            ValidationReport report = validateReport("http://localhost:3030/ds/shacl?graph=default", "fu-shapes.ttl");

            System.out.println();
            ShLib.printReport(report);
            System.out.println();

            System.out.println("- - - - - - - - - - - - - - - - - -");

            System.out.println();
            RDFDataMgr.write(System.out,  report.getGraph(), Lang.TTL);
            System.out.println();
        } finally {
            server.stop();
        }
    }

    static ValidationReport validateReport(String url, String shapesFile) {
        Graph shapesGraph = RDFDataMgr.loadGraph(shapesFile);
        EntityTemplate entity = new EntityTemplate((out)->RDFDataMgr.write(out, shapesGraph, Lang.TTL));
        String ct = Lang.TTL.getContentType().getContentTypeStr();
        entity.setContentType(ct);

        HttpCaptureResponse<Graph> graphResponse = HttpResponseLib.graphHandler();
        HttpOp.execHttpPost(url, entity, "*/*", graphResponse);
        return ValidationReport.fromGraph(graphResponse.get());
    }
}
