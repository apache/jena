/*
  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: SimpleReifierTripleMap.java,v 1.9 2005-02-21 11:52:11 andy_seaborne Exp $
*/
package com.hp.hpl.jena.graph.impl;

import java.util.HashSet;
import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;

/**
    SimpleReifierTripleMap - a map storing complete node -> triple maps.
    
    @author kers
*/
public class SimpleReifierTripleMap implements ReifierTripleMap 
    {
    protected Map inverseMap = CollectionFactory.createHashedMap();
    
    protected Map forwardMap = CollectionFactory.createHashedMap();    
    
    public Triple getTriple( Node tag )
        { return (Triple) forwardMap.get( tag ); }

    /**
         Answer true iff we have a reified triple <code>t</code>.
    */
    public boolean hasTriple( Triple t )
        { return inverseMap.containsKey( t ); }
    
    public Triple putTriple( Node key, Triple value )
        {
        forwardMap.put( key, value );
        inversePut( value, key );
        return value;
        }
    
    public void removeTriple( Node key )
        {
        Object t = forwardMap.get( key );
        forwardMap.remove( key );
        if (t instanceof Triple) inverseRemove( (Triple) t, key );
        }
    
    public void removeTriple( Node key, Triple value )
        {
        forwardMap.remove( key );
        inverseRemove( value, key );
        }
    
    public void removeTriple( Triple t )
        {
        ExtendedIterator it = tagIterator( t );
        Set nodes = CollectionFactory.createHashedSet();
        while (it.hasNext()) nodes.add( it.next() );
        Iterator them = nodes.iterator();
        while (them.hasNext()) removeTriple( (Node) them.next() );
        }
    
    protected void inverseRemove( Triple value, Node key )
        {
        Set s = (Set) inverseMap.get( value );
        if (s != null)
            {
            s.remove( key );
            if (s.isEmpty()) inverseMap.remove( value );
            }
        }
    
    protected void inversePut( Triple value, Node key )
        {
        Set s = (Set) inverseMap.get( value );
        if (s == null) inverseMap.put( value, s = new HashSet() );
        s.add( key );
        }            

    public ExtendedIterator tagIterator( Triple t )
        { 
        Set s = (Set) inverseMap.get( t );
        return s == null
            ? (ExtendedIterator) NullIterator.instance
            : WrappedIterator.create( s.iterator() );
        }

    protected ExtendedIterator allTriples( TripleMatch tm )
        {
        Triple pattern = tm.asTriple();
        Node tag = pattern.getSubject();
        if (tag.isConcrete())
            {
            Triple x = getTriple( tag );  
            return x == null ? NullIterator.instance : explodeTriple( pattern, tag, x ); 
            }
        else
            {
            final Iterator it = forwardMap.entrySet().iterator();   
            return new FragmentTripleIterator( pattern, it )
                {
                public void fill( GraphAdd ga, Node n, Object fragmentsObject )
                    {
                    SimpleReifier.graphAddQuad( ga, n, (Triple) fragmentsObject );         
                    }
                };
            }
        }
    
    /**
         Answer an interator over all of the quadlets of <code>toExplode</code> with
         the reifying node <code>tag</code> that match <code>pattern</code>.
    */
    public static ExtendedIterator explodeTriple( Triple pattern, Node tag, Triple toExplode )
        {
        GraphAddList L = new GraphAddList( pattern );
        SimpleReifier.graphAddQuad( L, tag, toExplode ); 
        return WrappedIterator.create( L.iterator() );
        }
    
    /**
        Return the fragment map as a read-only Graph of triples. We rely on the
        default code in GraphBase which allows us to only implement find(TripleMatch)
        to present a Graph. All the hard work is done by allTriples.
    */
    public Graph asGraph()
        {
        return new GraphBase()
            { public ExtendedIterator graphBaseFind( TripleMatch tm ) { return allTriples( tm ); } };
        }
    
    public ExtendedIterator find( TripleMatch m )
        { return allTriples( m ); }
    
    public int size()
        { return forwardMap.size() * 4; }
    
    /**
         Answer an iterator over all the fragment tags in this map.
    */
    public ExtendedIterator tagIterator()
        { return WrappedIterator.create( forwardMap.keySet().iterator() ); }
    }

/*
    (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.
    
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:
    
    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
    
    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
    
    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.
    
    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/