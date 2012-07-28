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
import com.hp.hpl.jena.vocabulary.RDF;

/**
    SimpleReifierFragmentsMap - a map from nodes to the incompleteb(or 
    overcomplete) reification quadlets.

*/
public class SimpleReifierFragmentsMap implements ReifierFragmentsMap 
    {
    protected Map<Node, Fragments> forwardMap = CollectionFactory.createHashedMap();
    
    protected Fragments getFragments( Node tag )
        { return forwardMap.get( tag ); }
    
    protected void removeFragments( Node key )
        { forwardMap.remove( key ); }
    
    @Override
    public void clear()
        { forwardMap.clear(); }
    
    /**
    update the map with (node -> fragment); return the fragment.
    */
    protected Fragments putFragments( Node key, Fragments value )
        {
        forwardMap.put( key, value );
        return value;
        }                    
    
    protected ExtendedIterator<Triple> allTriples( TripleMatch tm )
        {
        if (forwardMap.isEmpty())
            return NullIterator.instance();
        Triple t = tm.asTriple();
        Node subject = t.getSubject();
        if (subject.isConcrete())
            {
            Fragments x = forwardMap.get( subject );  
            return x == null
                ? NullIterator.<Triple>instance()
                : explodeFragments( t, subject, x )
                ; 
            }
        else
            {
            final Iterator<Map.Entry<Node, Fragments>> it = forwardMap.entrySet().iterator();   
            return new FragmentTripleIterator<Fragments>( t, it )
                {
                @Override public void fill( GraphAdd ga, Node n, Fragments fragmentsObject )
                    { fragmentsObject.includeInto( ga ); }
                };
            }
        }
    
    /**
     * @param t
     * @param subject
     * @param x
     * @return
     */
    protected ExtendedIterator<Triple> explodeFragments( Triple t, Node subject, Fragments x )
        {
        GraphAddList L = new GraphAddList( t );
        x.includeInto( L );
        return WrappedIterator.create( L.iterator() );
        }

    @Override
    public ExtendedIterator<Triple> find( TripleMatch m )
        { return allTriples( m ); }
    
    @Override
    public int size()
        { 
        int result = 0;
        Iterator<Map.Entry<Node, Fragments>> it = forwardMap.entrySet().iterator();   
        while (it.hasNext())
            {
            Map.Entry<Node, Fragments> e = it.next();
            result += e.getValue().size();
            }
        return result; 
        }
    
    /**
        given a triple t, see if it's a reification triple and if so return the internal selector;
        otherwise return null.
    */ 
    @Override
    public ReifierFragmentHandler getFragmentHandler( Triple t )
        {
        Node p = t.getPredicate();
        ReifierFragmentHandler x = selectors.get( p );
        if (x == null || (p.equals( RDF.Nodes.type ) && !t.getObject().equals( RDF.Nodes.Statement ) ) ) return null;
        return x;
        }

    public void putAugmentedTriple( SimpleReifierFragmentHandler s, Node tag, Node object, Triple reified )
        {
        Fragments partial = new Fragments( tag, reified );
        partial.add( s, object );
        putFragments( tag, partial );
        }
    
    protected Triple reifyCompleteQuad( SimpleReifierFragmentHandler s, Triple fragment, Node tag, Node object )
        {       
        Fragments partial = getFragments( tag );
        if (partial == null) putFragments( tag, partial = new Fragments( tag ) );
        partial.add( s, object );
        if (partial.isComplete())
            {
            removeFragments( fragment.getSubject() );
            return partial.asTriple();
            }
        else
            return null;
        }

    protected Triple removeFragment( SimpleReifierFragmentHandler s, Node tag, Triple already, Triple fragment )
        {
        Fragments partial = getFragments( tag );
        Fragments fs = (already != null ? explode( tag, already )
            : partial == null ? putFragments( tag, new Fragments( tag ) )
            : (Fragments) partial);
        fs.remove( s, fragment.getObject() );
        if (fs.isComplete())
            {
            Triple result = fs.asTriple();
            removeFragments( tag );
            return result;
            }
        else
            {
            if (fs.isEmpty()) removeFragments( tag );
            return null;
            }
        }
    
    protected Fragments explode( Node s, Triple t )
        { return putFragments( s, new Fragments( s, t ) ); }

    @Override
    public boolean hasFragments( Node tag )
        { return getFragments( tag ) != null; }

    protected static final Fragments.GetSlot TYPES_index = new Fragments.GetSlot() 
        { @Override
        public Set<Node> get( Fragments f ) { return f.types; } };
    
    protected static final Fragments.GetSlot SUBJECTS_index = 
        new Fragments.GetSlot() { @Override
        public Set<Node> get( Fragments f ) { return f.subjects; } };
    
    protected static final Fragments.GetSlot OBJECTS_index = 
        new Fragments.GetSlot() { @Override
        public Set<Node> get( Fragments f ) { return f.objects; } };
    
    protected static final Fragments.GetSlot PREDICATES_index = 
        new Fragments.GetSlot() { @Override
        public Set<Node> get( Fragments f ) { return f.predicates; } };
    
    protected final ReifierFragmentHandler TYPES = new SimpleReifierFragmentHandler( this, TYPES_index) 
        { @Override public boolean clashesWith( ReifierFragmentsMap map, Node n, Triple reified ) { return false; } };
    
    protected final ReifierFragmentHandler SUBJECTS = new SimpleReifierFragmentHandler( this, SUBJECTS_index) 
        { @Override public boolean clashesWith( ReifierFragmentsMap map, Node n, Triple reified ) { return !n.equals( reified.getSubject() ); } };
    
    protected final ReifierFragmentHandler PREDICATES = new SimpleReifierFragmentHandler( this, PREDICATES_index) 
        { @Override public boolean clashesWith( ReifierFragmentsMap map, Node n, Triple reified ) { return !n.equals( reified.getPredicate() ); } };
    
    protected final ReifierFragmentHandler OBJECTS = new SimpleReifierFragmentHandler( this, OBJECTS_index) 
        { @Override public boolean clashesWith( ReifierFragmentsMap map, Node n, Triple reified ) { return !n.equals( reified.getObject() ); } };

    public final Map<Node, ReifierFragmentHandler> selectors = makeSelectors();
          
    /**
        make the selector mapping.
    */
    protected Map<Node, ReifierFragmentHandler> makeSelectors()
        {
        Map<Node, ReifierFragmentHandler> result = CollectionFactory.createHashedMap();
        result.put( RDF.Nodes.subject, SUBJECTS );
        result.put( RDF.Nodes.predicate, PREDICATES );
        result.put( RDF.Nodes.object, OBJECTS );
        result.put( RDF.Nodes.type, TYPES );
        return result;
        }
    }
