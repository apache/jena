/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Fragments.java,v 1.4 2003-07-25 11:41:57 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import java.util.*;

import com.hp.hpl.jena.graph.*;

import com.hp.hpl.jena.vocabulary.RDF;

/**
    A _Fragments_ object represents the reification triples that have been
    asserted about some node. The triples may form an incomplete or
    inconsistent reification. They may even form a complete, single
    reification. Rather than keeping the triples, we keep the subject node
    (the one that has ref:subject etc properties) and sets of objects for
    each of the four relevant properties.
<p>
    See SimpleReifier for the use of Fragments. A longer and more
    explanatory name was considered for Fragments, but the effect of
    picking one of those seemed to make the code that *uses* them
    excessively verbose. 
<p>
    @author kers 
*/

public class Fragments
    { 
    /**
        a Fragments object is represented by four sets, one for each of the reification
        predicates. The slots are array elements because, sadly, it's easier to dynamically
        choose a slot by number than any other way I could think of.
    */
    private final Set [] slots = {new HashSet(), new HashSet(), new HashSet(), new HashSet()};
    
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
        remove the node _n_ from the set specified by slot _which_.
    */
    public void remove( int which, Node n )
        { slots[which].remove( n ); }
        
    /**
        add the node _n_ to the slot identified by _which).
   */
    public void add( int which, Node n )
        { slots[which].add( n ); }
        
    /**
        include into g all of the reification components that this Fragments
        represents.
    */
    public void includeInto( GraphAdd g )
        {
        includeInto( g, RDF.Nodes.subject, SUBJECTS );
        includeInto( g, RDF.Nodes.predicate, PREDICATES );
        includeInto( g, RDF.Nodes.object, OBJECTS );
        includeInto( g, RDF.Nodes.type, TYPES );
        }
        
    /**
        include into g all of the (n, p[which], o) triples for which
        o is an element of the slot _which_ corresponding to
        _predicate_.
    */
    private void includeInto( GraphAdd g, Node predicate, int which )
        {
        Iterator it = slots[which].iterator();
        while (it.hasNext())
            g.add( new Triple( anchor, predicate, (Node) it.next() ) );
        }
        
    /**
        add to this Fragments the entire reification quad needed to
        reify the triple t.
        @param t: Triple the (S, P, O) triple to reify
        @return this with the quad for (S, P, O) added
    */
    public Fragments addTriple( Triple t )
        {
        slots[SUBJECTS].add( t.getSubject() );
        slots[PREDICATES].add( t.getPredicate() );
        slots[OBJECTS].add( t.getObject() );
        slots[TYPES].add( RDF.Nodes.Statement );
        return this;
        }
        
    /** 
        precondition: isComplete() 
    <p>
        return the single Triple that this Fragments represents; only legal if
        isComplete() is true.    
    */        
    Triple asTriple()
        { return new Triple( only( slots[SUBJECTS] ), only( slots[PREDICATES] ), only( slots[OBJECTS] ) ); }
               
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
        { return anchor + " s:" + slots[SUBJECTS] + " p:" + slots[PREDICATES] + " o:" + slots[OBJECTS] + " t:" + slots[TYPES]; }
       
    /**
        given a triple t, see if it's a reification triple and if so return the internal seelctor;
        oterwise return -1.
    */ 
    public static int getFragmentSelector( Triple t )
        {
        Node p = t.getPredicate();
        Integer x = (Integer) selectors.get( p );
        if (x == null || (p.equals( RDF.Nodes.type ) && !t.getObject().equals( RDF.Nodes.Statement ) ) ) return -1;
        return x.intValue();
        }
        
    /*
        the magic numbers for the slots. The order doesn't matter, but that they're
        some permutation of {0, 1, 2, 3} does. 
    */
    private static final int TYPES = 0;
    private static final int SUBJECTS = 1;
    private static final int PREDICATES = 2;
    private static final int OBJECTS = 3;

    private static final HashMap selectors = makeSelectors();
          
    /**
        make the selector mapping.
    */
    private static HashMap makeSelectors()
        {
        HashMap result = new HashMap();
        result.put( RDF.Nodes.subject, new Integer( Fragments.SUBJECTS ) );
        result.put( RDF.Nodes.predicate, new Integer( Fragments.PREDICATES ) );
        result.put( RDF.Nodes.object, new Integer( Fragments.OBJECTS ) );
        result.put( RDF.Nodes.type, new Integer( Fragments.TYPES ) );
        return result;
        }
    }
    
/*
    (c) Copyright Hewlett-Packard Company 2002, 2003
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
