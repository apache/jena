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

package org.apache.jena.sparql.syntax;

import java.util.Iterator ;

import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.PathBlock ;
import org.apache.jena.sparql.core.TriplePath ;
import org.apache.jena.sparql.util.NodeIsomorphismMap ;

/** A SPARQL BasicGraphPattern (SPARQL 1.1) */

public class ElementPathBlock extends Element implements TripleCollectorMark
{
    private PathBlock pattern = new PathBlock() ; 

    public ElementPathBlock()
    {  }
    
    public ElementPathBlock(BasicPattern bgp)
    {  
        for ( Triple t : bgp.getList() )
            addTriple(t) ;
    }

    public ElementPathBlock(Triple... triples)
    {
        this() ;
        for(Triple t : triples) {
            this.addTriple(t) ;
        }
    }

    public ElementPathBlock(TriplePath... triplePaths)
    {
        this() ;
        for(TriplePath t : triplePaths) {
            this.addTriplePath(t) ;
        }
    }

    public boolean isEmpty() { return pattern.isEmpty() ; }
    
    public void addTriple(TriplePath tp)
    { pattern.add(tp) ; }
    
    @Override
    public int mark() { return pattern.size() ; }
    
    @Override
    public void addTriple(Triple t)
    { addTriplePath(new TriplePath(t)) ; }

    @Override
    public void addTriple(int index, Triple t)
    { addTriplePath(index, new TriplePath(t)) ; }

    @Override
    public void addTriplePath(TriplePath tPath)
    { pattern.add(tPath) ; }

    @Override
    public void addTriplePath(int index, TriplePath tPath)
    { pattern.add(index, tPath) ; }
    
    public PathBlock getPattern() { return pattern ; }
    public Iterator<TriplePath> patternElts() { return pattern.iterator(); }
    
    @Override
    public int hashCode()
    { 
        int calcHashCode = Element.HashBasicGraphPattern ;
        calcHashCode ^= pattern.hashCode() ; 
        return calcHashCode ;
    }

    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap)
    {
        if ( ! ( el2 instanceof ElementPathBlock) )
            return false ;
        ElementPathBlock eg2 = (ElementPathBlock)el2 ;
        return this.pattern.equiv(eg2.pattern, isoMap) ; 
    }

    @Override
    public void visit(ElementVisitor v) { v.visit(this) ; }
}
