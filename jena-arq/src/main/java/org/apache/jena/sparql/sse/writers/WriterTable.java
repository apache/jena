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
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.Tags;

public class WriterTable
{
    public static void output(IndentedWriter out, Table table, SerializationContext sCxt) {
        SSEWriteLib.start(out, Tags.tagTable, SSEWriteLib.NoNL);
        WriterNode.outputVars(out, table.getVars(), sCxt);
        out.println();
        outputPlain(out, table, sCxt);
        SSEWriteLib.finish(out, Tags.tagTable);
    }

    public static void outputPlain(IndentedWriter out, Table table, SerializationContext sCxt) {
        QueryIterator qIter = table.iterator(null);
        for ( ; qIter.hasNext() ; ) {
            Binding b = qIter.nextBinding();
            output(out, b, sCxt);
            out.println();
        }
        qIter.close();
    }

    public static void output(IndentedWriter out, Binding binding, SerializationContext sCxt) {
        SSEWriteLib.start(out, Tags.tagRow, SSEWriteLib.NoSP);
        for ( Iterator<Var> iter = binding.vars() ; iter.hasNext() ; ) {
            Var v = iter.next();
            Node n = binding.get(v);
            out.print(" ");
            SSEWriteLib.start2(out);
            WriterNode.output(out, v, sCxt);
            out.print(" ");
            WriterNode.output(out, n, sCxt);
            SSEWriteLib.finish2(out);
        }
        SSEWriteLib.finish(out, Tags.tagRow);
    }
}
