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

import java.io.OutputStream ;
import java.io.Writer ;
import java.util.Collection ;
import java.util.List ;
import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.other.G;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.util.Context;

/** An output of triples / quads that print batches of same subject / same graph, same subject.
 *  It writes something that is easier to read than
 *  N-triples, N-quads but it's not full pretty printing
 *  which usually requires analysing the data before any output.
 *
 *  If fed quads and triples, the output is valid TriG.
 *  If fed only triples, the output is valid Turtle.
 */

public class WriterStreamRDFBlocks extends WriterStreamRDFBatched
{
    protected static final boolean NL_GDFT_START    =  WriterConst.NL_GDFT_START ;
    protected static final boolean NL_GNMD_START    =  WriterConst.NL_GNMD_START ;
    protected static final boolean NL_GDFT_END      =  WriterConst.NL_GDFT_END ;
    protected static final boolean NL_GNMD_END      =  WriterConst.NL_GNMD_END ;

    // Subject column - normal width
    protected static final int INDENT_PREDICATE     = WriterConst.INDENT_PREDICATE ;
    protected static final int MIN_PREDICATE        = 6 ; //WriterConst.MIN_PREDICATE ;
    protected static final int LONG_PREDICATE       = WriterConst.LONG_PREDICATE ;
    protected static final int LONG_SUBJECT         = WriterConst.LONG_SUBJECT ;

    protected static final int INDENT_MIN_S         = 6 ;         // Range of subject indent
    protected static final int INDENT_MAX_S         = 14 ;
    protected static final int GAP_S_P              = 2 ;
    protected static final int GAP_P_O              = 2 ;

    protected static final int INDENT_GDFT          = 2 ;           // Default graph indent
    protected static final int INDENT_GNMD          = 4 ;           // Named graph indent

    // Quad output
    protected Node lastGraph            = null ;
    protected Node lastSubject          = null ;
    protected int currentGraphIndent    = 0;
    // Set true if we see a prefix setting for the RDF namespace.
    protected boolean hasRDFprefix      = false;

    public WriterStreamRDFBlocks(OutputStream output, Context context) {
        super(output, context) ;
    }

    public WriterStreamRDFBlocks(Writer output, Context context) {
        super(output, context) ;
    }

    public WriterStreamRDFBlocks(IndentedWriter output, Context context) {
        super(output, context) ;
    }

    @Override
    protected void printBatchQuads(Node g, Node s, List<Quad> quads) {
        if ( g == null )
            // print as Triples has currentGraph == null.
            g = Quad.defaultGraphNodeGenerated ;
        if ( Objects.equals(g, lastGraph) ) {
            // Same graph, different subject.
            out.println(" .") ;
        } else {
            // Different graph
            endGraph(g) ;
            startGraph(g) ;
            lastGraph = g ;
        }
        List<Triple> triples = G.quads2triples(quads.iterator()).toList() ;
        printBatch(s, triples) ;
        // No trailing "." has been printed.
        lastSubject = s ;
    }

    private void startBatch() {
        // Any output so far? prefixes or a previous graph.
        if ( out.getRow() > 1 )
            out.println() ;
        out.flush();
    }

    private void gap(int gap) {
        out.print(' ', gap) ;
    }

    @Override
    public void prefixSetup(String prefix, String iri) {
        // Newline between blocks.
        // After flush, before writing PREFIX
        boolean addNL = ( activeTripleData || activeQuadData );
        if ( addNL )
            out.println();
    }

    @Override
    protected void printBatchTriples(Node s, List<Triple> triples) {
        startBatch();
        printBatch(s, triples) ;
        // End of cluster.
        out.println(" .") ;
        lastGraph = null;
    }

    private void printBatch(Node s, List<Triple> triples) {
        outputNode(s) ;
        if ( out.getCol() > LONG_SUBJECT )
            out.println() ;
        else
            gap(GAP_S_P) ;
        out.incIndent(INDENT_PREDICATE) ;
        out.pad() ;
        writePredicateObjectList(triples) ;
        out.decIndent(INDENT_PREDICATE) ;
    }

    private void writePredicateObjectList(Collection<Triple> triples) {
        // Find width
        // We may have a prefix for RDF otherwise we use the 'a' abbreviation for rdf:type.
        boolean writeKeyWordType = countPrefixesForRDF <= 0 ;
        int predicateMaxWidth = Widths.calcWidthTriples(pMap, baseURI, triples, MIN_PREDICATE, LONG_PREDICATE, writeKeyWordType) ;
        boolean first = true ;
        for ( Triple triple : triples ) {
            if ( !first )
                out.println(" ;") ;
            else
                first = false ;

            Node p = triple.getPredicate() ;
            printProperty(p);
            out.pad(predicateMaxWidth) ;
            out.print(' ', GAP_P_O) ;
            Node o = triple.getObject() ;
            outputNode(o) ;
        }
    }

    @Override
    protected void finalizeRun() {
        if ( lastGraph != null )
            // last was a quad
            endGraph(null) ;
    }

    protected boolean dftGraph()        { return lastGraph == Quad.defaultGraphNodeGenerated ; }
    protected boolean dftGraph(Node g)  { return g == Quad.defaultGraphNodeGenerated ; }

    protected void startGraph(Node g) {
        startBatch();
        // Start graph
        if ( lastGraph == null ) {
            boolean NL_START = (dftGraph(g) ? NL_GDFT_START : NL_GNMD_START) ;

            lastSubject = null ;
            if ( !dftGraph(g) ) {
                outputNode(g) ;
                out.print(" ") ;
            }

            if ( NL_START )
                out.println("{") ;
            else
                out.print("{ ") ;

            if ( dftGraph() )
                setGraphIndent(INDENT_GDFT) ;
            else {
                int x = NL_START ? INDENT_GNMD : out.getCol() ;
                setGraphIndent(x) ;
            }
            out.incIndent(graphIndent()) ;
        }
        lastGraph = g ;
    }

    protected void endGraph(Node g) {
        if ( lastGraph == null )
            return ;

        // End of graph
        if ( !Objects.equals(lastGraph, g) ) {
            boolean NL_END = (dftGraph(g) ? NL_GDFT_END : NL_GNMD_END) ;

            if ( lastSubject != null )
                out.print(" .") ;
            if ( NL_END ) {
                // } on a new line.
                out.decIndent(graphIndent()) ;
                out.println() ;
                out.println("}") ;
            } else {
                // Possibly on same line as last quad/triple.
                out.decIndent(graphIndent()) ;
                if ( out.atLineStart() )
                    out.println("}") ;
                else
                    out.println(" }") ;
            }
            lastSubject = null ;
            lastGraph = null ;
        }
    }

    protected void setGraphIndent(int x)    { currentGraphIndent = x ; }
    protected int graphIndent()             { return currentGraphIndent ; }
}
