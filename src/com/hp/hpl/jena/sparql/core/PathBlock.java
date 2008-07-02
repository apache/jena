/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.path.P_Link;
import com.hp.hpl.jena.sparql.path.P_Reverse;
import com.hp.hpl.jena.sparql.path.P_Seq;
import com.hp.hpl.jena.sparql.path.Path;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;
import com.hp.hpl.jena.sparql.util.Utils;

/** A class whose purpose is to give a name to a collection of triple paths. */ 

public class PathBlock
{
    private List triplePaths = new ArrayList() ; 

    public PathBlock() {}
    public PathBlock(PathBlock other) {triplePaths.addAll(other.triplePaths) ; }
    
    public void add(TriplePath tp) { triplePaths.add(tp) ; }
    public void addAll(PathBlock other) { triplePaths.addAll(other.triplePaths) ; }
    public void add(int i, TriplePath tp) { triplePaths.add(i, tp) ; }
    
    public TriplePath get(int i) { return (TriplePath)triplePaths.get(i) ; }
    public ListIterator iterator() { return triplePaths.listIterator() ; } 
    public int size() { return triplePaths.size() ; }
    public boolean isEmpty() { return triplePaths.isEmpty() ; }
    
    public List getList() { return triplePaths ; } 
    
    static VarAlloc varAlloc = VarAlloc.getVarAllocator() ;
    
    /** Simplify : turns constructs in simple triples and simpler TriplePaths where possible */ 
    public PathBlock reduce()
    {
        PathBlock x = new PathBlock() ;
        // No context during algebra generation time.
//        VarAlloc varAlloc = VarAlloc.get(context, ARQConstants.sysVarAllocNamed) ;
//        if ( varAlloc == null )
//            // Panic
//            throw new ARQInternalErrorException("No execution-scope allocator for variables") ;
        
        reduce(x, varAlloc) ;
        return x ;
    }
    
    private void reduce(PathBlock x, VarAlloc varAlloc )
    {
        for ( Iterator iter = iterator() ; iter.hasNext() ; )
        {
            TriplePath tp = (TriplePath)iter.next();
            if ( tp.isTriple() )
            {
                x.add(tp) ;
                continue ;
            }
            reduce(x, varAlloc, tp.getSubject(), tp.getPath(), tp.getObject()) ;
        }
    }
    
    
    private static void reduce(PathBlock x, VarAlloc varAlloc, Node startNode, Path path, Node endNode)
    {
        if ( path instanceof P_Link )
        {
            Node pred = ((P_Link)path).getNode() ;
            Triple t = new Triple(startNode, pred, endNode) ; 
            x.add(new TriplePath(t)) ;
            return ;
        }

        if ( path instanceof P_Seq )
        {
            P_Seq ps = (P_Seq)path ;
            Node v = varAlloc.allocVar() ;
            reduce(x, varAlloc, startNode, ps.getLeft(), v) ;
            reduce(x, varAlloc, v, ps.getRight(), endNode) ;
            return ;
        }

        if ( path instanceof P_Reverse )
        {
            reduce(x, varAlloc, endNode, ((P_Reverse)path).getSubPath(), startNode) ;
            return ;
        }

        // Nothing can be done.
        x.add(new TriplePath(startNode, path, endNode)) ;
    }

    
    public int hashCode() { return triplePaths.hashCode() ; } 
    
    public boolean equals(Object other)
    { 
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
    
    public String toString()
    {
        return triplePaths.toString() ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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