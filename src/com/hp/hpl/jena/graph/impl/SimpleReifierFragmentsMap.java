/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: SimpleReifierFragmentsMap.java,v 1.5 2004-09-17 15:00:39 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.impl;

import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.Fragments.Slot;
import com.hp.hpl.jena.util.HashUtils;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDF.Nodes;

/**
    SimpleReifierFragmentsMap - a map from nodes to the incompleteb(or 
    overcomplete) reification quadlets.
    
    @author kers
*/
public class SimpleReifierFragmentsMap implements ReifierFragmentsMap 
    {
    protected Map forwardMap = HashUtils.createMap();
    
    public Fragments getFragments( Node tag )
        { return (Fragments) forwardMap.get( tag ); }
    
    public void removeFragments( Node key )
        { forwardMap.remove( key ); }
    
    /**
    update the map with (node -> fragment); return the fragment.
    */
    public Fragments putFragments( Node key, Fragments value )
        {
        forwardMap.put( key, value );
        return value;
        }                    
    
    public ExtendedIterator allTriples( TripleMatch tm )
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
            return new FragmentTripleIterator( t, it );
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

    /**
        Return the fragment map as a read-only Graph of triples. We rely on the
        default code in GraphBase which allows us to only implement find(TripleMatch)
        to present a Graph. All the hard work is done by allTriples.
    */
    public Graph asGraph()
        {
        return new GraphBase()
            { public ExtendedIterator find( TripleMatch tm ) { return allTriples( tm ); } };
        }

    /**
        given a triple t, see if it's a reification triple and if so return the internal seelctor;
        oterwise return null.
    */ 
    public Fragments.Slot getFragmentSelector( Triple t )
        {
        Node p = t.getPredicate();
        Fragments.Slot x = (Fragments.Slot) Fragments.selectors.get( p );
        if (x == null || (p.equals( RDF.Nodes.type ) && !t.getObject().equals( RDF.Nodes.Statement ) ) ) return null;
        return x;
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