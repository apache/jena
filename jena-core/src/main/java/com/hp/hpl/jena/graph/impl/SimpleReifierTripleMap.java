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

package com.hp.hpl.jena.graph.impl;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;

/**
    SimpleReifierTripleMap - a map storing complete node -> triple maps.

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
