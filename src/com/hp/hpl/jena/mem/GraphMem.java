/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: GraphMem.java,v 1.21 2003-09-08 11:28:23 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.*;

/**
    A memory-backed graph with S/P/O indexes. A GraphMem maintains a 
    reference count, set to one when it is created, and incremented by the method
    <code>openAgain()</code>. When the graph is closed, the count is decrememented,
    and when it reaches 0, the tables are trashed and GraphBase.close() called.
    Thus in normal use one close is enough, but GraphMakers using GraphMems
    can arrange to re-use the same named graph.
    
 * @author  bwm, kers
 */
public class GraphMem extends GraphBase implements Graph {

    HashSet triples = new HashSet();

    NodeMap subjects = new NodeMap();
    NodeMap predicates = new NodeMap();
    NodeMap objects = new NodeMap();

    /** Creates new Store */
    public GraphMem() 
        { this( ReificationStyle.Minimal ); }
    
    public GraphMem( ReificationStyle style )
        { 
        super( style );
        count = 1; 
        }

    protected int count;
    
    public void close()
        {
        if (--count == 0)
            {
            triples = null;
            subjects = predicates = objects = null;
            super.close();
            }
        }
        
    public GraphMem openAgain()
        { 
        count += 1; 
        return this;
        }
        
    public void performAdd( Triple t )
        {
        if (getReifier().handledAdd( t ) || triples.contains( t ))
            return;
        else
            {
            triples.add( t );
            subjects.add( t.getSubject(), t );
            predicates.add( t.getPredicate(), t );
            objects.add( t.getObject(), t );
            }
        }

    public void performDelete( Triple t )
        {
        if (getReifier().handledRemove( t ))
            return;
        else
            {
            triples.remove( t );
            subjects.remove( t.getSubject(),t );
            predicates.remove( t.getPredicate(), t );
            objects.remove( t.getObject(), t );
            }
        }

    public int size()  
        {
        checkOpen();
        return triples.size();
        }

    public boolean isEmpty()
        {
        checkOpen();
        return triples.isEmpty();
        }
        
    private QueryHandler q;
    
    public QueryHandler queryHandler()
        {
        if (q == null) q = new GraphMemQueryHandler( this );
        return q;
        }
        
    private static class GraphMemQueryHandler extends SimpleQueryHandler
        {
        GraphMemQueryHandler( GraphMem graph ) 
            { super( graph ); }
        }
        
    /**
        Answer true iff t matches some triple in the graph. If t is concrete, we
        can use a simple membership test; otherwise we resort to the generic
        method using find.
    */
    public boolean contains( Triple t ) {
        checkOpen();
        return t.isConcrete() ? triples.contains( t ) : containsByFind( t );
    }

    /**
        Answer true if there's some triple in the graph that (s, p, o) matches.
        Ensures that nulls are not present and then defers to contains(Triple). 
    */
    public boolean contains( Node s, Node p, Node o ) {
        checkOpen();
        if (s == null || p == null || o == null) throw new JenaException( "null not allowed" );
        return contains( Triple.create( s, p, o ) );
    }

    /** Returns an iterator over Triple.
     */
    public ExtendedIterator find( TripleMatch m ) {
        checkOpen();
        Triple tm = m.asTriple();
        Node p = m.getMatchPredicate();
        Node o = m.getMatchObject();
        Node ms = tm.getSubject();
        // @@ some redundant compares in this code which could be improved
        if (ms.isConcrete()) {
            return new TripleMatchIterator(tm, subjects.iterator( ms ));
        } else if (o != null && !o.isLiteral()) {
            // der - added guard on isLiteral to support typed literal semantics
            return new TripleMatchIterator(tm, objects.iterator(o));
        } else if (p != null) {
            return new TripleMatchIterator(tm, predicates.iterator(p));
        } else {
            return new TripleMatchIterator(tm, triples.iterator());
        }
    }

    protected class NodeMap {
        HashMap map = new HashMap();

        protected void add(Node o, Triple t) {
            LinkedList l = (LinkedList) map.get(o);
            if (l==null) {
                l = new LinkedList();
                map.put(o,l);
            }
            l.add(t);
        }

        protected void remove(Node o, Triple t ) {
            LinkedList l = (LinkedList) map.get(o);
            if (l != null) {
                l.remove(t);
                if (l.size() == 0) {
                    map.put(o, null);
                }
            }
        }

        protected Iterator iterator(Node o) {
            LinkedList l = (LinkedList) map.get(o);
            if (l==null) {
                return (new LinkedList()).iterator();
            } else {
                return l.iterator();
            }
        }
    }
    

}

/*
 *  (c) Copyright 2000, 2001 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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