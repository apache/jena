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

package org.apache.jena.sparql.sse.writers;

import java.util.Iterator;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.Tags;

public class WriterGraph
{
    public static final int NL = SSEWriteLib.NL;
    public static final int NoNL = SSEWriteLib.NoNL;
    public static final int NoSP = SSEWriteLib.NoSP;

    public static void output(IndentedWriter out, Graph graph, SerializationContext naming)
    { writeGraph(out, graph, null, naming); }

    public static void output(IndentedWriter out, DatasetGraph dataset, SerializationContext naming)
    { writeDataset(out, dataset, naming); }

    public static void output(IndentedWriter out, BasicPattern pattern, SerializationContext sCxt) {
        for ( Triple triple : pattern ) {
            WriterNode.output(out, triple, sCxt);
            out.println();
        }
    }

    // ---- Workers

    private static void writeDataset(IndentedWriter out, DatasetGraph ds, SerializationContext naming) {
        SSEWriteLib.start(out, Tags.tagDataset, NL);
        writeGraph(out, ds.getDefaultGraph(), null, naming);
        out.ensureStartOfLine();
        for ( Iterator<Node> iter = ds.listGraphNodes() ; iter.hasNext() ; ) {
            Node node = iter.next();
            out.ensureStartOfLine();
            Graph g = ds.getGraph(node);
            writeGraph(out, g, node, naming);
        }
        SSEWriteLib.finish(out, Tags.tagDataset);
        out.ensureStartOfLine();
    }

    private static void writeGraph(IndentedWriter out, Graph g, Node node, SerializationContext naming) {
        SSEWriteLib.start(out, Tags.tagGraph, NoSP);
        if ( node != null ) {
            out.print(" ");
            WriterNode.output(out, node, naming);
        }

        Iterator<Triple> iter = g.find(Node.ANY, Node.ANY, Node.ANY);
        if ( ! iter.hasNext() )
        {
            // Empty.
            SSEWriteLib.finish(out, Tags.tagGraph);
            return;
        }

//        out.println();
//        out.incIndent();
        boolean first = true;
        for (; iter.hasNext(); ) {
//            if ( ! first )
                out.println();
            first = false;
            Triple triple = iter.next();
            WriterNode.output(out, triple, naming);
        }
//        out.decIndent();
        if ( ! first ) out.println();
        SSEWriteLib.finish(out, Tags.tagGraph);
    }
}
