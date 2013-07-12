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

package com.hp.hpl.jena.sparql.core;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.writers.WriterNode ;
import com.hp.hpl.jena.sparql.util.Iso ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** A class whose purpose is to give a name to a collection of triples.
 * Reduces the use of bland "List" in APIs (Java 1.4) 
 */ 

public class BasicPattern implements Iterable<Triple>
{
    private List<Triple> triples ;

    public BasicPattern() { this(new ArrayList<Triple>()) ; }
    public BasicPattern(BasicPattern other)
    {
        this() ;
        // Copy.
        triples.addAll(other.triples) ;
    }
    private BasicPattern(List<Triple> triples) { this.triples = triples ; }
    
    /** Wrap a list of triples up as a BasicPattern.  Chnaging the list, changes the BasicPattern */ 
    public static BasicPattern wrap(List<Triple> triples)
    {
        return new BasicPattern(triples) ;
    }
    
    
    public void add(Triple t) { triples.add(t) ; }
    public void addAll(BasicPattern other) { triples.addAll(other.triples) ; }
    public void add(int i, Triple t) { triples.add(i, t) ; }
    
    public Triple get(int i) { return triples.get(i) ; }
    @Override
    public Iterator<Triple> iterator() { return triples.listIterator() ; } 
    public int size() { return triples.size() ; }
    public boolean isEmpty() { return triples.isEmpty() ; }
    
    public List<Triple> getList() { return triples ; } 

    @Override
    public int hashCode() { return triples.hashCode() ; } 

    @Override
    public boolean equals(Object other)
    { 
        if ( this == other ) return true ;
        if ( ! ( other instanceof BasicPattern) ) 
            return false ;
        BasicPattern bp = (BasicPattern)other ;
        return triples.equals(bp.triples) ;
    }
    
    public boolean equiv(BasicPattern other, NodeIsomorphismMap isoMap)
    { 
        if ( this.triples.size() != other.triples.size() )
            return false ;
        
        for ( int i = 0 ; i < this.triples.size() ; i++ )
        {
            Triple t1 = get(i) ;
            Triple t2 = other.get(i) ;
            
            if ( ! Iso.tripleIso(t1, t2, isoMap) )
                return false ;
        }
        return true ;
    }
    
    @Override
    public String toString() 
    { 
        IndentedLineBuffer out = new IndentedLineBuffer() ;
        
        SerializationContext sCxt = SSE.sCxt(SSE.defaultPrefixMapWrite) ;
        
        boolean first = true ;
        for ( Triple t : triples )
        {
            if ( !first )
                out.print("\n") ;
            else
                first = false ;
            // Adds (triple ...)
            // SSE.write(buff.getIndentedWriter(), t) ;
            out.print("(") ;
            WriterNode.outputPlain(out, t, sCxt) ;
            out.print(")") ;
        }
        out.flush();
        return out.toString() ;
    }
}
