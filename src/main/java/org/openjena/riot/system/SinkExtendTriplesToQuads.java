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

package org.openjena.riot.system;

import org.apache.jena.atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Take a stream of triples and send down a Sink&lt;Quad>
 *  The quad will have <code>Quad.tripleInQuad</code> in the G field or a specified node..
 *  @see Quad#tripleInQuad
 */   
public class SinkExtendTriplesToQuads implements Sink<Triple>
{
    private final Sink<Quad> quadSink ;
    private final Node graph ;

    public SinkExtendTriplesToQuads(Sink<Quad> quadSink)
    {
        this(Quad.tripleInQuad, quadSink) ;
    }
    
    public SinkExtendTriplesToQuads(Node gn, Sink<Quad> quadSink)
    {
        this.quadSink = quadSink ;
        this.graph = gn ;
    }
    
    @Override
    public void send(Triple triple)
    {
        Quad q = new Quad(graph, triple) ;
        quadSink.send(q) ;
    }

    @Override
    public void flush()
    {
        quadSink.flush() ;
    }
    
    @Override
    public void close()
    {
        // Don't close - the underlying sink may be reused. 
        //quadSink.close() ;
    }
}
