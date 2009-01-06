/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;
import com.hp.hpl.jena.sparql.util.Utils;

/** A class whose purpose is to give a name to a collection of triple paths. */ 

public class PathBlock implements Iterable<TriplePath>
{
    private List<TriplePath> triplePaths = new ArrayList<TriplePath>() ; 

    public PathBlock() {}
    public PathBlock(PathBlock other) {triplePaths.addAll(other.triplePaths) ; }
    
    public void add(TriplePath tp) { triplePaths.add(tp) ; }
    public void addAll(PathBlock other) { triplePaths.addAll(other.triplePaths) ; }
    public void add(int i, TriplePath tp) { triplePaths.add(i, tp) ; }
    
    public TriplePath get(int i) { return triplePaths.get(i) ; }
    public ListIterator<TriplePath> iterator() { return triplePaths.listIterator() ; } 
    public int size() { return triplePaths.size() ; }
    public boolean isEmpty() { return triplePaths.isEmpty() ; }
    
    public List<TriplePath> getList() { return triplePaths ; } 
    
    @Override
    public int hashCode() { return triplePaths.hashCode() ; } 
    
    @Override
    public boolean equals(Object other)
    { 
        if ( this == other ) return true ;
        if ( ! ( other instanceof PathBlock) ) 
            return false ;
        PathBlock bp = (PathBlock)other ;
        return triplePaths.equals(bp.triplePaths) ;
    }
    
    public boolean equiv(PathBlock other, NodeIsomorphismMap isoMap)
    { 
        if ( this.triplePaths.size() != other.triplePaths.size() )
            return false ;
        
        for ( int i = 0 ; i < this.triplePaths.size() ; i++ )
        {
            TriplePath tp1 = get(i) ;
            TriplePath tp2 = other.get(i) ;
            
            if ( ! Utils.triplePathIso(tp1, tp2, isoMap) )
                return false ;
        }
        return true ;
    }
    
    @Override
    public String toString()
    {
        return triplePaths.toString() ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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