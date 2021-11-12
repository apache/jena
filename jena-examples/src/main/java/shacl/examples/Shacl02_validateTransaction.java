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

package shacl.examples;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.*;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.sse.SSE;

/**
 * Use a graph transaction for an update.
 * <p>
 * If the proposed changes would make the
 * graph not conform to the shapes, abort the transaction.
 */

public class Shacl02_validateTransaction {
    static { LogCtl.setLogging(); }

    public static void main(String...args) {
        String SHAPES = "shapes.ttl";
        String DATA = "data2.ttl";

        Graph shapesGraph = RDFDataMgr.loadGraph(SHAPES);
        Graph dataGraph = DatasetGraphFactory.createTxnMem().getDefaultGraph();
        RDFDataMgr.read(dataGraph, DATA);
        Shapes shapes = Shapes.parse(shapesGraph);

        // Check initially valid.
        ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);
        ShLib.printReport(report);
        if ( ! report.conforms() ) {
            System.out.println("** Initial data does not validate");
            System.exit(0);
        }

        System.out.println("++ First transaction");
        try {
            GraphValidation.update(shapes, dataGraph, () -> {
                // Invalidate the data.
                Triple t = SSE.parseTriple("(:t1 <http://example/ns#p> 'more text')");
                dataGraph.add(t);
            });
            System.out.println("** OK");
        } catch (ShaclValidationException ex) {
            System.out.println("** Validation error");
            ShLib.printReport(ex.getReport());
            //RDFDataMgr.write(System.out, ex.getReport().getModel(), Lang.TTL);
        }
        System.out.println();
        System.out.println("++ Second transaction");
        try {
            GraphValidation.update(shapes, dataGraph, () -> {
                // Add validdata.
                Triple t = SSE.parseTriple("(:t2 <http://example/ns#p> 'second')");
                dataGraph.add(t);
            });
            System.out.println("** OK");
        } catch (ShaclValidationException ex) {
            System.out.println("** Validation error");
            ShLib.printReport(ex.getReport());
            //RDFDataMgr.write(System.out, ex.getReport().getModel(), Lang.TTL);
        }

        System.out.println();
        System.out.println("++ After:");
        RDFDataMgr.write(System.out, dataGraph, Lang.TTL);
    }
}
