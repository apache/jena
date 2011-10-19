/*
  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: SimpleReifierTripleMap.java,v 1.1 2009-06-29 08:55:43 castagna Exp $
*/
package com.hp.hpl.jena.graph.impl;

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
    protected Map<Triple, Set<Node>> inverseMap = CollectionFactory.createHashedMap();
    
    protected Map<Node, Triple> forwardMap = CollectionFactory.createHashedMap();    
    
    @Override
    public Triple getTriple( Node tag )
        { return forwardMap.get( tag ); }

    @Override
    public void clear()
        {
        forwardMap.clear();
        inverseMap.clear();
        }
    /**
         Answer true iff we have a reified triple <code>t</code>.
    */
    @Override
    public boolean hasTriple( Triple t )
        { return inverseMap.containsKey( t ); }
    
    @Override
    public Triple putTriple( Node key, Triple value )
        {
        forwardMap.put( key, value );
        inversePut( value, key );
        return value;
        }
    
    @Override
    public void removeTriple( Node key )
        {
        Object t = forwardMap.get( key );
        forwardMap.remove( key );
        if (t instanceof Triple) inverseRemove( (Triple) t, key );
        }
    
    @Override
    public void removeTriple( Node key, Triple value )
        {
        forwardMap.remove( key );
        inverseRemove( value, key );
        }
    
    @Override
    public void removeTriple( Triple t )
        {
        ExtendedIterator<Node> it = tagIterator( t );
        Set<Node> nodes = CollectionFactory.createHashedSet();
        while (it.hasNext()) nodes.add( it.next() );
        Iterator<Node> them = nodes.iterator();
        while (them.hasNext()) removeTriple( them.next() );
        }
    
    protected void inverseRemove( Triple value, Node key )
        {
        Set<Node> s = inverseMap.get( value );
        if (s != null)
            {
            s.remove( key );
            if (s.isEmpty()) inverseMap.remove( value );
            }
        }
    
    protected void inversePut( Triple value, Node key )
        {
        Set<Node> s = inverseMap.get( value );
        if (s == null) inverseMap.put( value, s = new HashSet<Node>() );
        s.add( key );
        }            

    @Override
    public ExtendedIterator<Node> tagIterator( Triple t )
        { 
        Set<Node> s = inverseMap.get( t );
        return s == null
            ? NullIterator.<Node>instance()
            : WrappedIterator.create( s.iterator() );
        }

    protected ExtendedIterator<Triple> allTriples( TripleMatch tm )
        {
        if (forwardMap.isEmpty()) return NullIterator.instance();
        Triple pattern = tm.asTriple();
        Node tag = pattern.getSubject();
        if (tag.isConcrete())
            {
            Triple x = getTriple( tag );  
            return x == null ? NullIterator.<Triple>instance() : explodeTriple( pattern, tag, x ); 
            }
        else
            {
            final Iterator<Map.Entry<Node, Triple>> it = forwardMap.entrySet().iterator();   
            return new FragmentTripleIterator<Triple>( pattern, it )
                {
                @Override public void fill( GraphAdd ga, Node n, Triple t )
                    {
                    SimpleReifier.graphAddQuad( ga, n, t );         
                    }
                };
            }
        }
    
    /**
         Answer an interator over all of the quadlets of <code>toExplode</code> with
         the reifying node <code>tag</code> that match <code>pattern</code>.
    */
    public static ExtendedIterator<Triple> explodeTriple( Triple pattern, Node tag, Triple toExplode )
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
            { @Override public ExtendedIterator<Triple> graphBaseFind( TripleMatch tm ) { return allTriples( tm ); } };
        }
    
    @Override
    public ExtendedIterator<Triple> find( TripleMatch m )
        { return allTriples( m ); }
    
    @Override
    public int size()
        { return forwardMap.size() * 4; }
    
    /**
         Answer an iterator over all the fragment tags in this map.
    */
    @Override
    public ExtendedIterator<Node> tagIterator()
        { return WrappedIterator.create( forwardMap.keySet().iterator() ); }
    }

/*
    (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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