/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: GraphTripleStore.java,v 1.5 2004-12-03 12:11:34 chris-dollin Exp $
*/
package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.TripleStore;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

/**
    GraphTripleStore - the underlying triple-indexed triple store for GraphMem et al,
    ripped out from the heart of GraphMem as part of simplifying the reification code.
    A GraphTripleStore is a searchable repository for triples. 
    
    @author kers
 */
public class GraphTripleStore implements TripleStore
    {
    protected NodeToTriplesMap subjects = new NodeToTriplesMap()
        { public Node getIndexNode( Triple t ) { return t.getSubject(); } };
        
    protected NodeToTriplesMap predicates = new NodeToTriplesMap()
        { public Node getIndexNode( Triple t ) { return t.getPredicate(); } };
        
    protected NodeToTriplesMap objects = new NodeToTriplesMap()
        { public Node getIndexNode( Triple t ) { return t.getObject(); } };
        
    protected Graph parent;
    
    public GraphTripleStore( Graph parent )
        { this.parent = parent; }
    
    /**
         Destroy this triple store - discard the indexes.
    */
    public void close()
        { subjects = predicates = objects = null; }
    
    /**
         Add a triple to this triple store.
    */
    public void add( Triple t )
        {
        if (subjects.add( t.getSubject(), t ))
            {
            predicates.add( t.getPredicate(), t );
            objects.add( t.getObject(), t ); 
            }
        }
    
    /**
         Remove a triple from this triple store.
    */
    public void delete( Triple t )
        {
        if (subjects.remove( t.getSubject(), t ))
            {
            predicates.remove( t.getPredicate(), t );
            objects.remove( t.getObject(), t ); 
            }
        }
    
    /**
         Answer the size (number of triples) of this triple store.
    */
    public int size()
        { return subjects.size(); }
    
    /**
         Answer true iff this triple store is empty.
    */
    public boolean isEmpty()
        { return subjects.isEmpty(); }
    
    public ExtendedIterator listSubjects()
        { return WrappedIterator.create( subjects.domain() ); }

    public ExtendedIterator listPredicates()
        { return WrappedIterator.create( predicates.domain() ); }
    
    public ExtendedIterator listObjects()
        { return WrappedIterator.create( objects.domain() ); }
    
    /**
         Answer true iff this triple store contains the (concrete) triple <code>t</code>.
    */
    public boolean contains( Triple t )
        { return subjects.contains( t ); }
    
    /** 
        Answer an ExtendedIterator returning all the triples from this store that
        match the pattern <code>m = (S, P, O)</code>.
        
        <p>Because the node-to-triples maps index on each of subject, predicate,
        and (non-literal) object, concrete S/P/O patterns can immediately select
        an appropriate map. Because the match for literals must be by sameValueAs,
        not equality, the optimisation is not applied for literals. [This is probably a
        Bad Thing for strings.]
        
        <p>Practice suggests doing the predicate test <i>last</i>, because there are
        "usually" many more statements than predicates, so the predicate doesn't
        cut down the search space very much. By "practice suggests" I mean that
        when the order went, accidentally, from S/O/P to S/P/O, performance on
        (ANY, P, O) searches on largish models with few predicates declined
        dramatically - specifically on the not-galen.owl ontology.
    */
    public ExtendedIterator find( TripleMatch tm )
        {
        Triple t = tm.asTriple();
        Node pm = t.getPredicate();
        Node om = t.getObject();
        Node sm = t.getSubject();
        if (sm.isConcrete())
            return new StoreTripleIterator( parent, subjects.iterator( sm , t ), predicates, objects );
        else if (om.isConcrete() && !om.isLiteral())
            return new StoreTripleIterator( parent, objects.iterator( om, t ), subjects, predicates );
        else if (pm.isConcrete())
            return new StoreTripleIterator( parent, predicates.iterator( pm, t ), subjects, objects );
        else
            return new StoreTripleIterator( parent, subjects.iterator( t ), predicates, objects );
        }
    
    /**
         Clear this store, ie remove all triples from it.
    */
    public void clear()
        {
        subjects.clear();
        predicates.clear();
        objects.clear();
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