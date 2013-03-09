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

package org.apache.jena.riot.process.inf;

import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFWrapper ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Receive triples and quads (incoming because this is a StreamRDF);
 *  allow RDFS; output to place provided. 
 */
public class InferenceProcessorStreamRDF extends StreamRDFWrapper
{
    private final InferenceSetupRDFS rdfsSetup ;
    private final InferenceProcessorRDFS rdfs ;
    private boolean isTriple = true ;
    private Node g ;

    public InferenceProcessorStreamRDF(final StreamRDF output, InferenceSetupRDFS rdfsSetup)
    {
        super(output) ;
        this.rdfsSetup = rdfsSetup ;
        this.rdfs = new InferenceProcessorRDFS(rdfsSetup)
        {
            @Override
            public void derive(Node s, Node p, Node o)
            {
                if ( isTriple )
                    output.triple(Triple.create(s,p,o)) ;
                else
                    output.quad(Quad.create(g,s,p,o)) ;
            }
        } ;
    }
    
    @Override
    public void triple(Triple triple)
    { 
        super.triple(triple) ;
        isTriple = true ;
        g = null ;
        rdfs.process(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }

    @Override
    public void quad(Quad quad)
    {
        super.quad(quad) ;
        isTriple = false ;
        g = quad.getGraph() ;
        rdfs.process(quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }
    
}

