/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: GraphMem.java,v 1.4 2003-01-28 13:02:35 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphBase;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.TripleMatchIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;


/**
 *
 * @author  bwm
 * @version 
 */
public class GraphMem extends GraphBase implements Graph {
    
    HashSet triples = new HashSet();
    
    NodeMap subjects = new NodeMap();
    NodeMap predicates = new NodeMap();
    NodeMap objects = new NodeMap();

    /** Creates new Store */
    public GraphMem() {}

    public void add(Triple t) {
        if (triples.contains(t)) {
            return;
        }
        triples.add(t);
        subjects.add(t.getSubject(), t);
        predicates.add(t.getPredicate(), t);
        objects.add(t.getObject(), t);
    }
    
    public void delete(Triple t) {
        triples.remove(t);
        subjects.remove(t.getSubject(),t);
        predicates.remove(t.getPredicate(), t);
        objects.remove(t.getObject(), t);
    }
    
    public int size() throws UnsupportedOperationException {
        return triples.size();
    }
    
    public boolean contains(Triple t) {
        return triples.contains(t);
    }
        
    public boolean contains(Node s, Node p, Node o) {
        return contains(new Triple(s, p, o));
    }
    
    /** Returns an iterator over Triple.
     */
    public ExtendedIterator find(TripleMatch m) {
        Node s = m.getSubject();
        Node p = m.getPredicate();
        Node o = m.getObject();
        
        // @@ some redundant compares in this code which could be improved
        if (s != null) {
            return new TripleMatchIterator(m, subjects.iterator(s));
        } else if (o != null) {
            return new TripleMatchIterator(m, objects.iterator(o));
        } else if (p != null) {
            return new TripleMatchIterator(m, predicates.iterator(p));
        } else {
            return new TripleMatchIterator(m, triples.iterator());
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
 *  (c) Copyright Hewlett-Packard Company 2000, 2001
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