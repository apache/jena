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

package com.hp.hpl.jena.sparql.syntax;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** The syntax eleemnt for a SPARQL BasicGraphPattern */

public class ElementTriplesBlock extends Element implements TripleCollectorMark
{
    private final BasicPattern pattern ; 

    public ElementTriplesBlock()
    { 
        pattern = new BasicPattern() ; 
    }
    
    public ElementTriplesBlock(BasicPattern bgp)
    { 
        pattern = bgp ;
    }

    public boolean isEmpty() { return pattern.isEmpty() ; }
    
    @Override
    public void addTriple(Triple t)
    { pattern.add(t) ; }
    
    @Override
    public int mark() { return pattern.size() ; }
    
    @Override
    public void addTriple(int index, Triple t)
    { pattern.add(index, t) ; }
    
    @Override
    public void addTriplePath(TriplePath path)
    { throw new ARQException("Triples-only collector") ; }

    @Override
    public void addTriplePath(int index, TriplePath path)
    { throw new ARQException("Triples-only collector") ; }
    
    public BasicPattern getPattern() { return pattern ; }
    public Iterator<Triple> patternElts() { return pattern.iterator(); }
    
    @Override
    public int hashCode()
    { 
        int calcHashCode = Element.HashBasicGraphPattern ;
        calcHashCode ^=  pattern.hashCode() ; 
        return calcHashCode ;
    }

    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap)
    {
        if ( ! ( el2 instanceof ElementTriplesBlock) )
            return false ;
        ElementTriplesBlock eg2 = (ElementTriplesBlock)el2 ;
        return this.pattern.equiv(eg2.pattern, isoMap) ; 
    }

    @Override
    public void visit(ElementVisitor v) { v.visit(this) ; }

 
}
