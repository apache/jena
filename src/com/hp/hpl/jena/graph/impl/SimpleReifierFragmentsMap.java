/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: SimpleReifierFragmentsMap.java,v 1.16 2004-11-19 14:38:11 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.RDF;

/**
    SimpleReifierFragmentsMap - a map from nodes to the incompleteb(or 
    overcomplete) reification quadlets.
    
    @author kers
*/
public class SimpleReifierFragmentsMap implements ReifierFragmentsMap 
    {
    
    protected Map forwardMap = CollectionFactory.createHashedMap();
    
    protected Fragments getFragments( Node tag )
        { return (Fragments) forwardMap.get( tag ); }
    
    protected void removeFragments( Node key )
        { forwardMap.remove( key ); }
    
    /**
    update the map with (node -> fragment); return the fragment.
    */
    protected Fragments putFragments( Node key, Fragments value )
        {
        forwardMap.put( key, value );
        return value;
        }                    
    
    protected ExtendedIterator allTriples( TripleMatch tm )
        {
        Triple t = tm.asTriple();
        Node subject = t.getSubject();
        if (subject.isConcrete())
            {
            Fragments x = (Fragments) forwardMap.get( subject );  
            return x == null
                ? NullIterator.instance
                : explodeFragments( t, subject, x )
                ; 
            }
        else
            {
            final Iterator it = forwardMap.entrySet().iterator();   
            return new FragmentTripleIterator( t, it )
                {
                public void fill( GraphAdd ga, Node n, Object fragmentsObject )
                    { 
                    ((Fragments) fragmentsObject).includeInto( ga );    
                    }
                };
            }
        }
    
    /**
     * @param t
     * @param subject
     * @param x
     * @return
     */
    protected ExtendedIterator explodeFragments( Triple t, Node subject, Fragments x )
        {
        GraphAddList L = new GraphAddList( t );
        x.includeInto( L );
        return WrappedIterator.create( L.iterator() );
        }

    public ExtendedIterator find( TripleMatch m )
        { return allTriples( m ); }
    
    public int size()
        { 
        int result = 0;
        Iterator it = forwardMap.entrySet().iterator();   
        while (it.hasNext())
            {
            Map.Entry e = (Map.Entry) it.next();
            Fragments f = (Fragments) e.getValue();
            result += f.size();
            }
        return result; 
        }
    
    /**
        given a triple t, see if it's a reification triple and if so return the internal selector;
        otherwise return null.
    */ 
    public ReifierFragmentHandler getFragmentHandler( Triple t )
        {
        Node p = t.getPredicate();
        ReifierFragmentHandler x = (ReifierFragmentHandler) selectors.get( p );
        if (x == null || (p.equals( RDF.Nodes.type ) && !t.getObject().equals( RDF.Nodes.Statement ) ) ) return null;
        return x;
        }

    protected void putAugmentedTriple( SimpleReifierFragmentHandler s, Node tag, Node object, Triple reified )
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

    public boolean hasFragments( Node tag )
        { return getFragments( tag ) != null; }

    protected static class Fragments
        { 
        
        /**
            a Fragments object is represented by four sets, one for each of the reification
            predicates. The slots are array elements because, sadly, it's easier to dynamically
            choose a slot by number than any other way I could think of.
        */
        private final Set [] slots = 
            {CollectionFactory.createHashedSet(), CollectionFactory.createHashedSet(), CollectionFactory.createHashedSet(), CollectionFactory.createHashedSet()};
        
        /**
            the Node the fragments are about. 
        */
        private Node anchor;
        
        /**
            a fresh Fragments object remembers the node n and starts
            off with all sets empty. (In use, at least one of the slots will
            then immediately be updated - otherwise there was no reason
            to create the Fragments in the first place ...)
        */
        public Fragments( Node n ) 
            { this.anchor = n; }
            
        public Fragments( Node n, Triple t )
            {
            this( n );
            addTriple( t ); 
            }
            
        public int size()
            { return slots[0].size() + slots[1].size() + slots[2].size() + slots[3].size(); }
        
        /**
            true iff this is a complete fragment; every component is present with exactly
            one value, so n unambiguously reifies (subject, predicate, object).
        */
        public boolean isComplete()
            { return slots[0].size() == 1 && slots[1].size() == 1 && slots[2].size() == 1 && slots[3].size() == 1; }
            
        /**
            true iff this is an empty fragment; no reificational assertions have been made
            about n. (Hence, in use, the Fragments object can be discarded.)
        */
        public boolean isEmpty()
            { return slots[0].isEmpty() && slots[1].isEmpty() && slots[2].isEmpty() && slots[3].isEmpty(); }
            
        /**
            remove the node n from the set specified by slot which.
        */
        public void remove( SimpleReifierFragmentHandler w, Node n )
            { slots[w.which].remove( n ); }
            
        /**
            add the node n to the slot identified by which).
       */
        public void add( SimpleReifierFragmentHandler w, Node n )
            { slots[w.which].add( n ); }
            
        /**
            include into g all of the reification components that this Fragments
            represents.
        */
        public void includeInto( GraphAdd g )
            {
            includeInto( g, RDF.Nodes.subject, SUBJECTS_index );
            includeInto( g, RDF.Nodes.predicate, PREDICATES_index );
            includeInto( g, RDF.Nodes.object, OBJECTS_index );
            includeInto( g, RDF.Nodes.type, TYPES_index );
            }
            
        /**
            include into g all of the (n, p[which], o) triples for which
            o is an element of the slot <code>which</code> corresponding to
            predicate.
        */
        private void includeInto( GraphAdd g, Node predicate, int which )
            {
            Iterator it = slots[which].iterator();
            while (it.hasNext())
                g.add( Triple.create( anchor, predicate, (Node) it.next() ) );
            }
            
        /**
            add to this Fragments the entire reification quad needed to
            reify the triple t.
            @param t: Triple the (S, P, O) triple to reify
            @return this with the quad for (S, P, O) added
        */
        public Fragments addTriple( Triple t )
            {
            slots[SUBJECTS_index].add( t.getSubject() );
            slots[PREDICATES_index].add( t.getPredicate() );
            slots[OBJECTS_index].add( t.getObject() );
            slots[TYPES_index].add( RDF.Nodes.Statement );
            return this;
            }
            
        /** 
            precondition: isComplete() 
        <p>
            return the single Triple that this Fragments represents; only legal if
            isComplete() is true.    
        */        
        Triple asTriple()
            { return Triple.create( only( slots[SUBJECTS_index] ), only( slots[PREDICATES_index] ), only( slots[OBJECTS_index] ) ); }
                   
        /**
            precondition: s.size() == 1
        <p>
            utiltity method to return the only element of a singleton set.
        */
        private Node only( Set s )
            { return (Node) s.iterator().next(); }
            
        /**
            return a readable representation of this Fragment for debugging purposes.
        */
        public String toString()
            { return anchor + " s:" + slots[SUBJECTS_index] + " p:" + slots[PREDICATES_index] + " o:" + slots[OBJECTS_index] + " t:" + slots[TYPES_index]; }

        }       
    
    /*
        the magic numbers for the slots. The order doesn't matter, but that they're
        some permutation of {0, 1, 2, 3} does. 
    */
    protected static final int TYPES_index = 0;
    protected static final int SUBJECTS_index = 1;
    protected static final int PREDICATES_index = 2;
    protected static final int OBJECTS_index = 3;
    
    protected final ReifierFragmentHandler TYPES = new SimpleReifierFragmentHandler( this, TYPES_index) { public boolean clashesWith( ReifierFragmentsMap map, Node n, Triple reified ) { return false; } };
    protected final ReifierFragmentHandler SUBJECTS = new SimpleReifierFragmentHandler( this, SUBJECTS_index) { public boolean clashesWith( ReifierFragmentsMap map, Node n, Triple reified ) { return !n.equals( reified.getSubject() ); } };
    protected final ReifierFragmentHandler PREDICATES = new SimpleReifierFragmentHandler( this, PREDICATES_index) { public boolean clashesWith( ReifierFragmentsMap map, Node n, Triple reified ) { return !n.equals( reified.getPredicate() ); } };
    protected final ReifierFragmentHandler OBJECTS = new SimpleReifierFragmentHandler( this, OBJECTS_index) { public boolean clashesWith( ReifierFragmentsMap map, Node n, Triple reified ) { return !n.equals( reified.getObject() ); } };

    public final Map selectors = makeSelectors();
          
    /**
        make the selector mapping.
    */
    protected Map makeSelectors()
        {
        Map result = CollectionFactory.createHashedMap();
        result.put( RDF.Nodes.subject, SUBJECTS );
        result.put( RDF.Nodes.predicate, PREDICATES );
        result.put( RDF.Nodes.object, OBJECTS );
        result.put( RDF.Nodes.type, TYPES );
        return result;
        }
    }

/*
    (c) Copyright 2004, Hewlett-Packard Development Company, LP
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