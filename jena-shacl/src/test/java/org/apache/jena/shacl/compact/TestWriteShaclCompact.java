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

package org.apache.jena.shacl.compact;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RIOT;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.Context;

public class TestWriteShaclCompact extends AbstractTestShaclCompact {

    // Test by loading, writing and reading again then checking against the graph
    // provided with the graph from the read-back-in Shapes.
    @Override
    protected void runTest(String fn, String ttl, String fileBaseName) {

        boolean DEV = false;

        if ( DEV ) {
            System.out.println("---- "+fn);
            String x = IO.readWholeFileAsUTF8(fn);
            System.out.print(x);
        }

        // All shapes, not some.
        Shapes shapes1 = ShaclcParser.parse(fn, BASE);
        if ( DEV ) {
            ShLib.printShapes(shapes1);
            System.out.println();
        }

        Graph expected = GraphFactory.createDefaultGraph();
        RDFDataMgr.read(expected, ttl, BASE, null);

        // Write the shapes in compact syntax.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ShaclcWriter.print(out, shapes1);

        if ( DEV ) System.out.println(new String(out.toByteArray(), StandardCharsets.UTF_8));

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Shapes shapes2 = ShaclcParser.parse(in, BASE);

        Graph graphGot = shapes2.getGraph();
        Graph graphOther = expected;

        if ( DEV ) System.out.println();

        boolean isomorphic = graphGot.isIsomorphicWith(graphOther);
        if ( ! isomorphic ) {
            Context cxt = RIOT.getContext().copy();
            cxt.set(RIOT.symTurtleDirectiveStyle, "sparql");
            System.err.println("---- "+fn);

            System.err.println("Different (W)");
            System.err.println("graph(jena) = "+graphGot.size());
            System.err.println("graph(ref)  = "+graphOther.size());
            if ( true ) {
                RDFWriter.create().source(graphGot).format(RDFFormat.TURTLE_PRETTY).context(cxt).output(System.err);
                RDFWriter.create().source(graphOther).format(RDFFormat.TURTLE_PRETTY).context(cxt).output(System.err);
            }
        }
        assertTrue("test: "+fileBaseName, isomorphic);
    }

    private void remove(Graph graph, Node s, Node p, Node o) {
        List<Triple> triples = graph.find(s,p,o).toList();
        triples.forEach(graph::delete);
    }
}

