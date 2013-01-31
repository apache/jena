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

package com.hp.hpl.jena.sparql.modify.request;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.syntax.TripleCollector ;

/** Accumulate quads into a Sink (including allowing variables) during parsing. */
public class QuadAccSink implements TripleCollector, Closeable
{
    protected Node graphNode = Quad.defaultGraphNodeGenerated ;
    private final Sink<Quad> sink;
    
    public QuadAccSink(Sink<Quad> sink)
    {
        this.sink = sink;
    }
    
    protected void check(Triple triple) {} 
    protected void check(Quad quad) {} 
    
    public void setGraph(Node n) 
    { 
        graphNode = n ;
    }
    
    public Node getGraph()    { return graphNode ; }
    
    public void addQuad(Quad quad)
    {
        check(quad) ;
        sink.send(quad) ;
    }

    @Override
    public void addTriple(Triple triple)
    {
        check(triple) ;
        sink.send(new Quad(graphNode, triple)) ;
    }

    @Override
    public void addTriplePath(TriplePath tPath)
    { throw new UnsupportedOperationException("Can't add paths to quads") ; }

    @Override
    public void close()
    {
        sink.close();
    }
}
