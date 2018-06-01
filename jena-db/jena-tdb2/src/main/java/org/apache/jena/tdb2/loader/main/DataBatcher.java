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

package org.apache.jena.tdb2.loader.main;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.lang.StreamRDFCounting;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.loader.base.BulkStartFinish;
import org.apache.jena.tdb2.loader.base.MonitorOutput;

/**
 * A {@link StreamRDF} that groups triples and quads and dispatches them in batches. This
 * class is a {@link StreamRDF} and runs on the calling thread; it does not create any
 * threads.
 */   
public class DataBatcher implements StreamRDFCounting, BulkStartFinish {
    
    private List<Triple> triples = null;
    private List<Quad> quads = null;
    private long countTriples;
    private long countQuads;
    private final MonitorOutput output;
    private  BiConsumer<String, String> prefixHandler;
    private Consumer<DataBlock> batchDestination;
    
    public DataBatcher(Consumer<DataBlock> batchDestination,
                       BiConsumer<String, String> prefixHandler,
                       MonitorOutput output) {
        this(batchDestination, prefixHandler, LoaderParallel.DataTickPoint, LoaderParallel.DataSuperTick, output);
    }
    
    public DataBatcher(Consumer<DataBlock> batchDestination,
                       BiConsumer<String, String> prefixHandler, 
                       int tickPoint, int superTick, MonitorOutput output) {
        this.batchDestination = batchDestination;
        this.output = output;
        this.prefixHandler = prefixHandler;
    }
    
    @Override
    public void startBulk() {}

    @Override
    public void finishBulk() {
        DataBlock lastData = null;
        if ( ! isEmpty(triples) || ! isEmpty(quads) ) {
            lastData = new DataBlock(triples, quads);
            dispatch(lastData);
            triples = null;
            quads = null;
        }
        dispatch(DataBlock.END);
    }
    
    private <X> boolean isEmpty(List<X> list) {
        return list == null || list.isEmpty() ;
    }
    
    @Override public void start() {}

    @Override public void finish() {}

    @Override public long count()           { return countTriples() + countQuads(); }

    @Override public long countTriples()    { return countTriples; }

    @Override public long countQuads()      { return countQuads; }
    
    @Override
    public void triple(Triple triple) {
        if ( triples == null )
            triples = allocChunkTriples();
        triples.add(triple);
        countTriples++;
        maybeDispatch();
    }

    @Override
    public void quad(Quad quad) {
        if ( quad.isTriple() || quad.isDefaultGraph() ) {
            // Shame about the object creation.
            triple(quad.asTriple());
            return;
        }
        if ( quads == null )
            quads = allocChunkQuads();
        quads.add(quad);
        countQuads++;
        maybeDispatch();
    }

    private void maybeDispatch() {
        long x = 0;
        if ( triples != null )
            x += triples.size();
        if ( quads != null )
            x += quads.size();
        if ( x <= LoaderConst.ChunkSize )
            return ;
        
        DataBlock block = new DataBlock(triples, quads) ;
        // Dispatch.
        dispatch(block);
        triples = null;
        quads = null;
    }

    private void dispatch(DataBlock datablock) {
        batchDestination.accept(datablock);
    }

    
//    private void maybeDispatch3() {
//        if ( triples.size() >= LoaderConst.ChunkSize ) {
//            dispatchTriples(triples);
//            triples = null;
//        }
//    }
//    
//    private void maybeDispatch4() {
//        if ( quads.size() >= LoaderConst.ChunkSize ) {
//            dispatchQuads(quads);
//            quads = null;
//        }
//    }
//
//    private void dispatchTriples(List<Triple> triples) {
//        destTriples.deliver(triples);
//    }
//
//    private void dispatchQuads(List<Quad> quads) {
//        destQuads.deliver(quads);
//    }

    @Override
    public void base(String base) {}

    @Override
    public void prefix(String prefix, String iri) {
        if ( prefixHandler != null )
            prefixHandler.accept(prefix, iri);
    }

    private List<Triple>  allocChunkTriples() {
        return new ArrayList<>(LoaderConst.ChunkSize); 
    } 

    private List<Quad>  allocChunkQuads() {
        return new ArrayList<>(LoaderConst.ChunkSize); 
    }

}
