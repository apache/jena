/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

/** Like a triple, except the property is a path */

package com.hp.hpl.jena.sparql.core;

import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.path.P_Link;
import com.hp.hpl.jena.sparql.path.P_Reverse;
import com.hp.hpl.jena.sparql.path.P_Seq;
import com.hp.hpl.jena.sparql.path.Path;

public final class TriplePath
{
    private final Node subject ;
    private final Path path ;
    private final Node object ;
    private Triple triple = null ;
    private int hash = -1 ;

    public TriplePath(Node s, Path p, Node o)
    {
        this.subject = s ;
        this.path = p ;
        this.object = o ;
    }
    
    public TriplePath(Triple triple)
    {
        this.subject = triple.getSubject() ;
        this.path = new P_Link(triple.getPredicate()) ;
        this.object = triple.getObject() ;
        this.triple = triple ; 
    }

    public Node getSubject()    { return subject ; }
    public Path getPath()       { return path ; }
    public Node getObject()     { return object ; }

    /** Return as a triple when the path is a simple, 1-link, else return null */ 
    public Triple asTriple()
    { 
        if ( triple != null )
            return triple ;
        
        if ( path instanceof P_Link )
        {
            triple = new Triple(subject, ((P_Link)path).getNode(), object) ;
            return triple ;
        }
        return triple ;
    }

    public int hashCode()
    {
        if ( hash == -1 )
            hash = (subject.hashCode()<<2) ^ path.hashCode() ^ (object.hashCode()<<1) ;
        return hash ;
    }
    
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof TriplePath) )
            return false ;
        TriplePath tp = (TriplePath)other ;
        return subject.equals(tp.subject) && object.equals(tp.object) && path.equals(tp.path) ;
               
    }
    
    public String toString()
    {
        return subject+" "+path+" "+object ;
    }
    
    /** Simplify the triple path, add Triple/TriplePaths to a list*/ 
    public void reduce(List x)
    {
        reduce(x, getSubject(), getPath(), getObject()) ;
    }
    
    private static void reduce(List x, Node startNode, Path path, Node endNode)
    {
        VarAlloc varAlloc = VarAlloc.getVarAllocator() ;
        
        if ( path instanceof P_Link )
        {
            Node pred = ((P_Link)path).getNode() ;
            Triple t = new Triple(startNode, pred, endNode) ; 
            x.add(t) ;
            return ;
        }
        
        if ( path instanceof P_Seq )
        {
            P_Seq ps = (P_Seq)path ;
            Node v = varAlloc.allocVar() ;
            reduce(x, startNode, ps.getLeft(), v) ;
            reduce(x, v, ps.getRight(), endNode) ;
            return ;
        }
        
        if ( path instanceof P_Reverse )
        {
            reduce(x, endNode, ((P_Reverse)path).getSubPath(), startNode) ;
            return ;
        }
        
        // Nothing can be done.
        x.add(new TriplePath(startNode, path, endNode)) ;
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