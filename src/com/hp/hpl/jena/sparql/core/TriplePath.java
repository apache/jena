/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.path.P_Link;
import com.hp.hpl.jena.sparql.path.Path;
import com.hp.hpl.jena.sparql.util.ALog;

/** Like a triple except it can be a path or a triple.  
 * The triple can have a variable predicate.  
 * There may be no "path".
 */ 

public final class TriplePath
{
    private final Node subject ;
    private final Node predicate ;
    private final Path path ;
    private final Node object ;
    private Triple triple = null ;
    private int hash = -1 ;

    // Maybe "de P_Link" all this.
    
    public TriplePath(Node s, Path path, Node o)
    {
        this.subject = s ;
        this.object = o ;
        if ( path instanceof P_Link )
        {
            this.predicate = ((P_Link)path).getNode() ;
            triple = new Triple(subject, this.predicate , o) ;
        } else
            this.predicate = null ;
        this.path = path ;
    }
    
    public TriplePath(Triple triple)
    {
        this.subject = triple.getSubject() ;
        // Canonicalise: A triple and a path with a using P_Link of a URI 
        Node p = triple.getPredicate() ;
        if ( p.isURI() )
        {
            this.path = new P_Link(triple.getPredicate()) ;
            this.predicate = p ;
        }
        else
        {   
            this.path = null ;
            this.predicate = triple.getPredicate() ;
        }
//        this.path = null ;
//        this.predicate = triple.getPredicate() ;
        this.object = triple.getObject() ;
        this.triple = triple ; 
        if ( triple.getPredicate() == null )
            ALog.warn(this, "Triple predicate is null") ;
    }

    public Node getSubject()    { return subject ; }
    public Path getPath()       { return path ; }       // Maybe null (it's a triple).
    public Node getPredicate()  { return predicate ; }  // Maybe null (it's a path).
    public Node getObject()     { return object ; }

    public boolean isTriple()
    {
        return (triple != null || predicate != null) ;
    }
    
    /** Return as a triple when the path is a simple, 1-link, else return null */ 
    public Triple asTriple()
    { 
        if ( triple != null )
            return triple ;
        
        if ( path instanceof P_Link )
            triple = new Triple(subject, ((P_Link)path).getNode(), object) ;
        return triple ;
    }

    @Override
    public int hashCode()
    {
        if ( hash == -1 )
        {
            if ( isTriple() )
                hash = asTriple().hashCode() ;
            else
                hash = (subject.hashCode()<<2) ^ path.hashCode() ^ (object.hashCode()<<1) ;
        }
        return hash ;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other) return true ;
        if ( ! ( other instanceof TriplePath) )
            return false ;
        TriplePath tp = (TriplePath)other ;

        // True if one is true and one is false
        if ( tp.isTriple() ^ this.isTriple() )
            return false ;
        if ( isTriple() )
            return asTriple().equals(tp.asTriple()) ;
        else        
            return subject.equals(tp.subject) && object.equals(tp.object) && path.equals(tp.path) ;
    }
    
    @Override
    public String toString()
    {
        if ( path != null )
            return subject+" ("+path+") "+object ;
        else
            return subject+" "+predicate+" "+object ;
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