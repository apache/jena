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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

/**
     Iterator which delivers all the triples from a Fragment's map. Subclasses
     must override <code>fill(GraphAdd,Node,T)</code> to fill the GraphAdd
     object with the triples implied by the unrefied <code>T</code> object.
     In the codebase, T is instantiated as either a Triple or a Fragments.     
*/
public abstract class FragmentTripleIterator<T> extends NiceIterator<Triple>
    {
    private final GraphAddList pending;
    private final Iterator<Map.Entry<Node, T>> it;
    
    /**
        An iterator over all the reification triples buried in <code>it</code> that match
        <code>match</code>. The elements of the iterator are either Triples
        or Fragmentss.
    */
    public FragmentTripleIterator( Triple match, Iterator<Map.Entry<Node, T>> it )
        {
        super();
        this.it = it;
        this.pending = new GraphAddList( match );
        }

            
    /**
        Answer true iff there are any triples left, ie, there are some triples in the pending
        list once we've refilled.
        @return true iff there are more triples to come
    */
    @Override public boolean hasNext() 
        { 
        refill();
        return pending.size() > 0; 
        }
        
    /**
        Answer the next triple in the iteration, if one exists. The triples are stored in the
        pending list and removed from its end (which we hope is efficient).
        @return the next triple
        @throws NoSuchElementException if there isn't one
    */
    @Override public Triple next()
        {
        ensureHasNext();
        return pending.removeLast();
        }
        
    /**
        Add all the [implied, matching] triples from the Object into the GraphAdd
        entity (which will be our list). It would be nice if we could create an interface
        for the fragmentObject's type.
    */
    protected abstract void fill( GraphAdd ga, Node n, T fragmentsObject );
        
    /**
        Refill the pending list. Keep trying until either there are some elements
        to be found in it, or we've run out of elements in the original iterator.
    */
    private void refill()
        { while (pending.size() == 0 && it.hasNext()) refillFrom( pending, it.next() );  }

    /**
         Refill the buffer <code>pending</code> from the iterator element 
         <code>next</code>. The default behaviour is to assume that this object is 
         a Map.Entry; over-ride if it's something else.
    */
    protected void refillFrom( GraphAdd pending, Map.Entry<Node, T> x )
        { fill( pending, x.getKey(), x.getValue() ); }
}
