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

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.syntax.TripleCollector ;

/** Accumulate quads (including allowing variables) during parsing. */
public class QuadAcc implements TripleCollector
{
    // A lists of Pairs: Node and Triple connector
    
    private Node graphNode = Quad.defaultGraphNodeGenerated ;
    private List<Quad> quads = new ArrayList<Quad>() ;
    private List<Quad> quadsView = Collections.unmodifiableList(quads) ;
    
    public QuadAcc()     {}
    
    protected void check(Triple triple) {} 
    protected void check(Quad quad) {} 
    
    public void setGraph(Node n) 
    { 
        graphNode = n ;
    }
    
    public Node getGraph()    { return graphNode ; }
    
    public List<Quad> getQuads()
    {
        return quadsView ;
    }
    
    public void addQuad(Quad quad)
    {
        check(quad) ;
        quads.add(quad) ;
    }

    @Override
    public void addTriple(Triple triple)
    {
        check(triple) ;
        quads.add(new Quad(graphNode, triple)) ;
    }

    @Override
    public void addTriple(int index, Triple triple)
    {
        check(triple) ;
        quads.add(index, new Quad(graphNode, triple)) ;
    }

    @Override
    public void addTriplePath(TriplePath tPath)
    { throw new UnsupportedOperationException("Can't add paths to quads") ; }

    @Override
    public void addTriplePath(int index, TriplePath tPath)
    { throw new UnsupportedOperationException("Can't add paths to quads") ; }

    @Override
    public int mark()
    {
        return quads.size() ;
    }
    
    @Override
    public int hashCode() { return quads.hashCode() ; }

    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof QuadAcc ) ) return false ;
        QuadAcc acc = (QuadAcc)other ;
        return quads.equals(acc.quads) ; 
    }

}
