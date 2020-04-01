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

package org.apache.jena.riot.writer;

import static org.apache.jena.riot.writer.WriterConst.*;

import java.util.Set;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.util.Context ;

/** TriG pretty writer */
public class TriGWriter extends TriGWriterBase
{
    @Override
    protected void output(IndentedWriter iOut, DatasetGraph dsg, PrefixMap prefixMap, String baseURI, Context context) {
        TriGWriter$ w = new TriGWriter$(iOut, prefixMap, baseURI, context) ;
        w.write(dsg) ;
    }

    private static class TriGWriter$ extends TurtleShell
    {
        TriGWriter$(IndentedWriter out, PrefixMap prefixMap, String baseURI, Context context) {
            super(out, prefixMap, baseURI, context) ;
        }

        private void write(DatasetGraph dsg) {
            writeBase(baseURI) ;
            writePrefixes(prefixMap) ;
            if ( !prefixMap.isEmpty() && !dsg.isEmpty() )
                out.println() ;

            Set<Node> graphNames = Iter.toSet(dsg.listGraphNodes());

            boolean anyGraphOutput = writeGraphTriG(dsg, null, graphNames) ;

            for ( Node gn : graphNames ) {
                if ( anyGraphOutput )
                    out.println() ;
                anyGraphOutput |= writeGraphTriG(dsg, gn, graphNames) ;
            }
        }

        /** Return true if anything written */
        private boolean writeGraphTriG(DatasetGraph dsg, Node name, Set<Node> graphNames) {
            boolean dftGraph = ( name == null || name == Quad.defaultGraphNodeGenerated  ) ;

            if ( dftGraph && dsg.getDefaultGraph().isEmpty() )
                return false ;

            if ( dftGraph && ! GDFT_BRACE ) {
                // Non-empty default graph, no braces.
                // No indenting.
                writeGraphTTL(dsg, name, graphNames) ;
                return true ;
            }

            // The graph will go in braces, whether non-empty default graph or a named graph.
            boolean NL_START =  ( dftGraph ? NL_GDFT_START : NL_GNMD_START ) ;
            boolean NL_END =    ( dftGraph ? NL_GDFT_END : NL_GNMD_END ) ;
            int INDENT_GRAPH =  ( dftGraph ? INDENT_GDFT : INDENT_GNMD ) ;

            if ( !dftGraph ) {
                writeNode(name) ;
                out.print(" ") ;
            }

            out.print("{") ;
            if ( NL_START )
                out.println() ;
            else
                out.print(" ") ;

            out.incIndent(INDENT_GRAPH) ;
            writeGraphTTL(dsg, name, graphNames) ;
            out.decIndent(INDENT_GRAPH) ;

            if ( NL_END )
                out.ensureStartOfLine() ;
            out.println("}") ;
            return true ;
        }
    }
}

