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

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Convert the incoming print stream into batches. */
abstract class WriterStreamRDFBatched extends WriterStreamRDFBase
{
    // This has nothing to do with printing except it's under WriterStreamRDFBase
    // so the operation is "print"
    private Node         currentSubject ;
    private Node         currentGraph ;
    private List<Triple> batchTriples ;
    private List<Quad>   batchQuads ;

    public WriterStreamRDFBatched(OutputStream output)
    { super(output) ; }
    
    public WriterStreamRDFBatched(Writer output)
    { super(output) ; }

    public WriterStreamRDFBatched(IndentedWriter output)
    { super(output) ; }

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

    @Override
    protected final void print(Quad quad) {
        if ( false ) {
            // Merge to a triple stream.
            triple(quad.asTriple()) ;
            return ;
        }

        Node g = quad.getGraph() ;
        Node s = quad.getSubject() ;

        if ( !Lib.equal(g, currentGraph) || !Lib.equal(s, currentSubject) ) {
            if ( currentSubject != null ) {
                if ( currentGraph == null )
                    finishBatchTriples(currentSubject) ;
                else
                    finishBatchQuad(currentGraph, currentSubject) ;
            }
            startBatchQuad(g, s) ;
            currentGraph = g ;
            currentSubject = s ;
        }
        processQuad(quad) ;
    }

    @Override
    protected final void print(Triple triple) {
        Node s = triple.getSubject() ;
        if ( !Lib.equal(s, currentSubject) ) {
            if ( currentSubject != null )
                finishBatchTriples(currentSubject) ;
            startBatchTriple(s) ;

            currentGraph = null ;
            currentSubject = s ;
        }
        processTriple(triple) ;
    }

    private void startBatchTriple(Node subject) {
        batchTriples = new ArrayList<>() ;
    }

    private void processTriple(Triple triple) {
        batchTriples.add(triple) ;
    }

    private void finishBatchTriples(Node subject) {
        if ( batchTriples != null && batchTriples.size() > 0 ) {
            printBatchTriples(currentSubject, batchTriples) ;
            batchTriples.clear() ;
        }
    }

    private void startBatchQuad(Node graph, Node subject) {
        batchQuads = new ArrayList<>() ;
    }

    private void processQuad(Quad Quad) {
        batchQuads.add(Quad) ;
    }

    private void finishBatchQuad(Node graph, Node subject) {
        if ( batchQuads != null && batchQuads.size() > 0 ) {
            printBatchQuads(currentGraph, currentSubject, batchQuads) ;
            batchQuads.clear() ;
        }
    }

    protected abstract void printBatchQuads(Node g, Node s, List<Quad> batch) ;

    protected abstract void printBatchTriples(Node s, List<Triple> batch) ;

    protected  abstract void finalizeRun() ;

    
}

