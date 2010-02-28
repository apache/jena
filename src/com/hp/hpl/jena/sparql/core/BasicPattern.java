/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.writers.WriterNode ;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;
import com.hp.hpl.jena.sparql.util.Utils ;

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
            
            if ( ! Utils.tripleIso(t1, t2, isoMap) )
                return false ;
        }
        return true ;
    }
    
    @Override
    public String toString() 
    { 
        IndentedLineBuffer out = new IndentedLineBuffer() ;
        
        SerializationContext sCxt = SSE.sCxt((SSE.defaultPrefixMapWrite)) ;
        
        boolean first = true ;
        for ( Triple t : triples )
        {
            if ( !first )
                out.print(" ") ;
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

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */