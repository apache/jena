/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


/** A class whose purpose is to give a name to a collection of quads
 * Reduces the use of bland "List" in APIs (Java 1.4) 
 */ 

public class QuadPattern
{
    private List quads = new ArrayList() ; 

    public QuadPattern() {}
    public QuadPattern(QuadPattern other) {quads.addAll(other.quads) ; }
    
    public void add(Quad q) { quads.add(q) ; }
    public void addAll(QuadPattern other) { quads.addAll(other.quads) ; }
    public void add(int i, Quad q) { quads.add(i, q) ; }
    
    public Quad get(int i) { return (Quad)quads.get(i) ; }
    public ListIterator iterator() { return quads.listIterator() ; } 
    public int size() { return quads.size() ; }
    public boolean isEmpty() { return quads.isEmpty() ; }
    
    public List getList() { return quads ; } 
    
    public int hashCode() { return quads.hashCode() ; } 
    public boolean equals(Object other)
    { 
        if ( ! ( other instanceof QuadPattern) ) 
            return false ;
        QuadPattern bp = (QuadPattern)other ;
        return quads.equals(bp.quads) ;
    }
    
    public String toString() { return quads.toString() ; } 
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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