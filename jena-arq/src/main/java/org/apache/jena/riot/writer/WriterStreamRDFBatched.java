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
import java.util.ArrayList ;
import java.util.List ;
import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.util.Context;

/** Convert the incoming print stream into batches. */
abstract class WriterStreamRDFBatched extends WriterStreamRDFBase
{
    // This has nothing to do with printing except it's under WriterStreamRDFBase
    // so the operation is "print"
    private Node         currentSubject ;
    private Node         currentGraph ;
    private List<Triple> batchTriples ;
    private List<Quad>   batchQuads ;

    public WriterStreamRDFBatched(OutputStream output, Context context)
    { super(output, context) ; }
    
    public WriterStreamRDFBatched(Writer output, Context context)
    { super(output, context) ; }

    public WriterStreamRDFBatched(IndentedWriter output, Context context)
    { super(output, context) ; }

    @Override
    protected final void startData()    { reset() ; }

    @Override
    protected final void endData()      { flush() ; }

    private void flush() {
        finishBatchTriples(currentSubject) ;
        finishBatchQuad(currentGraph, currentSubject) ;
        finalizeRun() ;
    }

    @Override
    protected final void reset() {
        currentSubject = null ;
        currentGraph = null ;
        batchTriples = null ;
        batchQuads = null ;
    }

    private void batch(Node g, Node s, boolean forTriples) {
        if ( !Objects.equals(g, currentGraph) || !Objects.equals(s, currentSubject) ) {
            finishBatchTriples(currentSubject) ;
            finishBatchQuad(currentGraph, currentSubject) ;
            if ( forTriples )
                startBatchTriple(s);
            else
                startBatchQuad(g, s);
            currentGraph = g ;
            currentSubject = s ;
        }
    }

    @Override
    protected final void print(Triple triple) {
        Node s = triple.getSubject() ;
        batch(null, s, true);
        processTriple(triple) ;
    }

    private void startBatchTriple(Node subject) {
        batchTriples = new ArrayList<>() ;
        this.currentGraph = null;
        this.currentSubject = subject;
    }

    private void finishBatchTriples(Node subject) {
        if ( batchTriples != null && !batchTriples.isEmpty() ) {
            printBatchTriples(currentSubject, batchTriples) ;
            batchTriples.clear() ;
        }
    }

    private void processTriple(Triple triple) {
        batchTriples.add(triple) ;
    }

    @Override
    protected final void print(Quad quad) {
        Node g = quad.getGraph() ;
        Node s = quad.getSubject() ;
        batch(g, s, false);
        processQuad(quad) ;
    }

    private void startBatchQuad(Node graph, Node subject) {
        batchQuads = new ArrayList<>() ;
        this.currentGraph = graph;
        this.currentSubject = subject;
    }

    private void finishBatchQuad(Node graph, Node subject) {
        if ( batchQuads != null && !batchQuads.isEmpty() ) {
            printBatchQuads(currentGraph, currentSubject, batchQuads) ;
            batchQuads.clear() ;
        }
    }

    private void processQuad(Quad quad) {
        batchQuads.add(quad) ;
    }

    protected abstract void printBatchQuads(Node g, Node s, List<Quad> batch) ;

    protected abstract void printBatchTriples(Node s, List<Triple> batch) ;

    protected  abstract void finalizeRun() ;

    
}

