/**
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

package tdb;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.optimizer.StatsMatcher;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformationSubstitution;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.sse.writers.WriterNode;
import org.apache.jena.tdb1.sys.Names;

public class tdbreorder {
    public static void main(String...args) {
        if ( args.length != 2 ) {
            System.err.println("Usage: PATTERN STATS");
            System.exit(1);
        }
        LogCtl.enable(StatsMatcher.class);
        LogCtl.enable(ReorderTransformationSubstitution.class);

        if ( args.length != 2 ) {
            System.err.println("Usage: op stats");
            System.exit(1);
        }

        String pattern = args[0];
        String statsFile = args[1];

        Op op = SSE.readOp(pattern);

        BasicPattern bgp;
        if ( op instanceof OpQuadPattern opq ) {
            bgp = opq.getBasicPattern();
        } else if ( op instanceof OpBGP opbgp) {
            bgp = opbgp.getPattern();
        } else {
            System.err.println("Not a quad or triple pattern");
            System.exit(2);
            bgp = null;
        }

        ReorderTransformation reorder = chooseReorder(statsFile);
        // ReorderTransformation reorder = ReorderLib.fixed() ;
        BasicPattern bgp2 = reorder.reorder(bgp);

        System.out.println();

        print(bgp);
        System.out.println();
        System.out.println(" ======== >>>>>>>>");
        print(bgp2);
        System.out.println();
    }

    private static void print(BasicPattern bgp) {
        IndentedWriter out = IndentedWriter.stdout;
        PrefixMapping pmap = SSE.getPrefixMapWrite();
        SerializationContext sCxt = SSE.sCxt(pmap);

        boolean first = true;
        for ( Triple t : bgp ) {
            if ( !first )
                out.print("\n");
            else
                first = false;
            // Adds (triple ...)
            // SSE.write(buff.getIndentedWriter(), t) ;
            out.print("(");
            WriterNode.outputPlain(out, t, sCxt);
            out.print(")");
        }
        out.flush();
    }

    private static ReorderTransformation chooseReorder(String filename) {
        if ( filename.equals(Names.optFixed) )
            return ReorderLib.fixed();
        if ( filename.equals(Names.optNone) )
            return ReorderLib.identity();
        if ( FileOps.exists(filename) )
            return ReorderLib.weighted(filename);
        else
            throw new RuntimeException("No such file: " + filename);
    }
}
