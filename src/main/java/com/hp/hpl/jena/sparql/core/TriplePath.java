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

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.path.P_Link ;
import com.hp.hpl.jena.sparql.path.Path ;

import org.apache.jena.atlas.logging.Log ;

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
            Log.warn(this, "Triple predicate is null") ;
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
