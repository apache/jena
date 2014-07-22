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

package org.apache.jena.riot.other;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.riot.system.StreamRDF ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Batch a stream into triples and/or quads.
 *  Triples are batched on subject
 *  Quads are batched on (graph, subject). 
 *  
 */
public class BatchedStreamRDF implements StreamRDF
{
    // Extract the state machine ?
    private final StreamRDFBatchHandler batchProc ;
    private Node currentSubject         = null ;
    private Node currentGraph           = null ;
    private List<Triple> batchTriples   = null ;
    private List<Quad>   batchQuads     = null ;
    
    public BatchedStreamRDF(StreamRDFBatchHandler batchProc)
    {
        this.batchProc = batchProc ;
    }
    
    @Override
    public final void start()
    {
        currentSubject  = null ;
        currentGraph    = null ;
        batchTriples    = null ;
        batchQuads      = null ;
        batchProc.start() ;
    }
    
    // ---- Triples 
    @Override
    public void triple(Triple triple)
    {
        Node s = triple.getSubject() ;
//        Node p = triple.getPredicate() ;
//        Node o = triple.getObject() ;

        if ( ! Lib.equal(s, currentSubject) )
        {
            if ( currentSubject != null )
                finishBatchTriple(currentSubject) ;
            startBatchTriple(s) ;
            
            currentGraph = null ;
            currentSubject = s ;
        }
        
        processTriple(triple) ;
    }

    private void startBatchTriple(Node subject)
    {
        batchTriples = new ArrayList<>() ;
    }
    
    private void finishBatchTriple(Node subject)
    {
        if ( batchTriples != null && batchTriples.size() > 0 )
            batchProc.batchTriples(currentSubject, batchTriples) ;
    }

    private void processTriple(Triple triple)
    {
        batchTriples.add(triple) ;
    }

    // ---- Quads 
    @Override
    public void quad(Quad quad)
    {
        if ( false )
        {
            // Merge to a triple stream.
            triple(quad.asTriple()) ;
            return ;
        }
        
        Node g = quad.getGraph() ;
        Node s = quad.getSubject() ;
        
//            Node p = triple.getPredicate() ;
//            Node o = triple.getObject() ;
        
        if ( ! Lib.equal(g, currentGraph) || ! Lib.equal(s,  currentSubject) )
        {
            if ( currentSubject != null )
                finishBatchQuad(currentGraph, currentSubject) ;
            startBatchQuad(g, s) ;
            currentGraph = g ;
            currentSubject = s ;
        }
        processQuad(quad) ;
    }

    private void startBatchQuad(Node graph, Node subject)
    {
        batchQuads = new ArrayList<>() ;
    }
    
    private void finishBatchQuad(Node graph, Node subject)
    {
        if ( batchQuads != null && batchQuads.size() > 0 )
            batchProc.batchQuads(currentGraph, currentSubject, batchQuads) ;
    }

    private void processQuad(Quad Quad)
    {
        batchQuads.add(Quad) ;
    }
    
    private void flush()
    {
        finishBatchTriple(currentSubject) ;
        finishBatchQuad(currentGraph, currentSubject) ;
    }
    
    @Override
    public final void finish()
    {
        flush() ;
        batchProc.finish() ;

    }

    @Override
    public void base(String base)                   { batchProc.base(base) ; }

    @Override
    public void prefix(String prefix, String iri)   { batchProc.prefix(prefix, iri) ; }
}

