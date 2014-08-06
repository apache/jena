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

import static org.apache.jena.riot.writer.WriterConst.INDENT_GDFT ;
import static org.apache.jena.riot.writer.WriterConst.INDENT_GNMD ;
import static org.apache.jena.riot.writer.WriterConst.NL_GDFT_END ;
import static org.apache.jena.riot.writer.WriterConst.NL_GDFT_START ;
import static org.apache.jena.riot.writer.WriterConst.NL_GNMD_END ;
import static org.apache.jena.riot.writer.WriterConst.NL_GNMD_START ;

import java.util.Iterator ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.riot.system.PrefixMap ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** TriG pretty writer */
public class TriGWriter extends TriGWriterBase
{
    @Override
    protected void output(IndentedWriter iOut, DatasetGraph dsg, PrefixMap prefixMap, String baseURI) {
        TriGWriter$ w = new TriGWriter$(iOut, prefixMap, baseURI) ;
        w.write(dsg) ;
    }

    private static class TriGWriter$ extends TurtleShell
    {
        TriGWriter$(IndentedWriter out, PrefixMap prefixMap, String baseURI) {
            super(out, prefixMap, baseURI) ;
        }

        private void write(DatasetGraph dsg) {
            writeBase(baseURI) ;
            writePrefixes(prefixMap) ;
            if ( !prefixMap.isEmpty() && !dsg.isEmpty() )
                out.println() ;

            Iterator<Node> graphNames = dsg.listGraphNodes() ;

            boolean anyGraphOutput = writeGraphTriG(dsg, null) ;

            for ( ; graphNames.hasNext() ; ) {
                if ( anyGraphOutput )
                    out.println() ;
                Node gn = graphNames.next() ;
                anyGraphOutput |= writeGraphTriG(dsg, gn) ;
            }
        }

        /** Return true if anything written */
        private boolean writeGraphTriG(DatasetGraph dsg, Node name) {
            boolean dftGraph =  ( name == null || name == Quad.defaultGraphNodeGenerated  ) ;
            boolean NL_START =  ( dftGraph ? NL_GDFT_START : NL_GNMD_START ) ; 
            boolean NL_END =    ( dftGraph ? NL_GDFT_END : NL_GNMD_END ) ; 
            int INDENT_GRAPH =  ( dftGraph ? INDENT_GDFT : INDENT_GNMD ) ; 

            if ( !dftGraph ) {
                writeNode(name) ;
                out.print(" ") ;
            } else {
                if ( dsg.getDefaultGraph().isEmpty() )
                    return false ;
            }

            out.print("{") ;
            if ( NL_START )
                out.println() ;
            else
                out.print(" ") ;

            out.incIndent(INDENT_GRAPH) ;
            writeGraphTTL(dsg, name) ;
            out.decIndent(INDENT_GRAPH) ;

            if ( NL_END )
                out.ensureStartOfLine() ;
            out.println("}") ;
            return true ;
        }
    }
}

