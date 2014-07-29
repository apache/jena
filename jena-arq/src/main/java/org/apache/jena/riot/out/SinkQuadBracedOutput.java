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

package org.apache.jena.riot.out ;

import java.io.OutputStream ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

/**
 * A class that print quads, SPARQL style (maybe good for Trig too?)
 */
public class SinkQuadBracedOutput implements Sink<Quad>, Closeable
{
    protected static final int           BLOCK_INDENT = 2 ;

    protected final IndentedWriter       out ;
    protected final SerializationContext sCxt ;
    protected boolean                    opened       = false ;

    protected Node                       currentGraph ;

    public SinkQuadBracedOutput(OutputStream out) {
        this(out, null) ;
    }

    public SinkQuadBracedOutput(OutputStream out, SerializationContext sCxt) {
        this(new IndentedWriter(out), sCxt) ;
    }

    public SinkQuadBracedOutput(IndentedWriter out, SerializationContext sCxt) {
        if ( out == null ) { throw new IllegalArgumentException("out may not be null") ; }

        if ( sCxt == null ) {
            sCxt = new SerializationContext() ;
        }

        this.out = out ;
        this.sCxt = sCxt ;
    }

    public void open() {
        out.println("{") ;
        out.incIndent(BLOCK_INDENT) ;
        opened = true ;
    }

    private void checkOpen() {
        if ( !opened ) { throw new IllegalStateException("SinkQuadBracedOutput is not opened.  Call open() first.") ; }
    }

    @Override
    public void send(Quad quad) {
        send(quad.getGraph(), quad.asTriple()) ;
    }

    public void send(Node graphName, Triple triple) {
        checkOpen() ;
        if ( Quad.isDefaultGraph(graphName) ) {
            graphName = null ;
        }

        if ( !Lib.equal(currentGraph, graphName) ) {
            if ( null != currentGraph ) {
                out.decIndent(BLOCK_INDENT) ;
                out.println("}") ;
            }

            if ( null != graphName ) {
                out.print("GRAPH ") ;
                output(graphName) ;
                out.println(" {") ;
                out.incIndent(BLOCK_INDENT) ;
            }
        }

        output(triple) ;
        out.println(" .") ;

        currentGraph = graphName ;
    }

    private void output(Node node) {
        String n = FmtUtils.stringForNode(node, sCxt) ;
        out.print(n) ;
    }

    private void output(Triple triple) {
        String ts = FmtUtils.stringForTriple(triple, sCxt) ;
        out.print(ts) ;
    }

    @Override
    public void flush() {
        out.flush() ;
    }

    @Override
    public void close() {
        if ( opened ) {
            if ( null != currentGraph ) {
                out.decIndent(BLOCK_INDENT) ;
                out.println("}") ;
            }

            out.decIndent(BLOCK_INDENT) ;
            out.print("}") ;

            // Since we didn't create the OutputStream, we'll just flush it
            flush() ;
            opened = false ;
        }
    }
}
