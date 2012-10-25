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

package org.openjena.riot.process.inf;

import org.openjena.atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Quad ;

class InferenceProcessorQuads implements Sink<Quad>
{
    private final Sink<Quad> output ;
    private final InferenceSetupRDFS rdfsSetup ;
    private final InferenceProcessorRDFS rdfs ;
    private Node g ;

    public InferenceProcessorQuads(Sink<Quad> output, InferenceSetupRDFS rdfsSetup)
    {
        this.output = output ;
        this.rdfsSetup = rdfsSetup ;
        this.rdfs = new InferenceProcessorRDFS(rdfsSetup)
        {
            @Override
            public void derive(Node s, Node p, Node o)
            { InferenceProcessorQuads.this.output.send(new Quad(g,s,p,o)) ; }
        } ;
    }

    
    @Override
    public void send(Quad quad)
    {
        output.send(quad) ;
        // Take a note of the graph, so the derive operation has access to it.
        g = quad.getGraph() ;
        rdfs.process(quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }

    @Override
    public void flush()
    { output.flush() ; }

    @Override
    public void close()
    { output.close() ; }
}
