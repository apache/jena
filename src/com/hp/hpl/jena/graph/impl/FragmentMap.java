/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: FragmentMap.java,v 1.3 2003-07-25 09:03:41 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import java.util.HashMap;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

/**
    a FragmentMap is a Map where the domain elements are Nodes
    and the range elements are Triples or Fragments. The specialised
    put methods return the range element that has been put, because
    the context of use is usually of the form:
<p>    
    return map.putThingy( node, fragmentExpression )
<p>
    @author kers
*/

public class FragmentMap extends HashMap
    {
    public FragmentMap() { super(); }
    
    /**
        update the map with (node -> triple); return the triple
    */
    public Triple putTriple( Node key, Triple value )
        {
        put( key, value );
        return value;
        }
        
    /**
        update the map with (node -> fragment); return the fragment.
    */
    public Fragments putFragments( Node key, Fragments value )
        {
        put( key, value );
        return value;
        }        
        
    public ExtendedIterator allTriples()
        {
        final Iterator it = this.entrySet().iterator();
        return new NiceIterator()
            {
            private List pending = new ArrayList();
            
            public boolean hasNext() { return pending.size() > 0 || checkNext(); }
            
            private boolean checkNext()
                {
                if (it.hasNext() == false) return false;
                refill();
                return pending.size() > 0 || checkNext();    
                }
            
            private void refill()
                {
                Map.Entry e  = (Map.Entry) it.next();
                Node n = (Node) e.getKey();
                Object x = e.getValue();
                if (x instanceof Triple)
                    {
                    Triple t = (Triple) x;
                    pending.add( Triple.create( n, RDF.Nodes.subject, t.getSubject() ) );
                    pending.add( Triple.create( n, RDF.Nodes.predicate, t.getPredicate() ) );
                    pending.add( Triple.create( n, RDF.Nodes.object, t.getObject() ) );
                    pending.add( Triple.create( n, RDF.Nodes.type, RDF.Nodes.Statement ) );
                    }
                else
                    {
                    Fragments f = (Fragments) x;
                    Graph temp = new GraphMem();
                    f.includeInto( temp );
                    Iterator xx = temp.find( Node.ANY, Node.ANY, Node.ANY );
                    while (xx.hasNext()) pending.add( xx.next() );
                    }
                }
                
            public Object next()
                {
                if (pending.size() > 0)
                    {
                    return pending.remove( pending.size() - 1 );
                    }
                else
                    {
                    refill();
                    return next();
                    }
                }
            };
        }
        
    public Graph asGraph()
        {
        return new GraphBase()
            {
            public int size()
                {
                ExtendedIterator it = allTriples();
                int result = 0;
                while (it.hasNext()) { it.next(); result += 1; }
                return result;    
                }
                
            public ExtendedIterator find( TripleMatch tm )
                {
                return new TripleMatchIterator( tm.asTriple(), allTriples() );
                }
            };
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
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