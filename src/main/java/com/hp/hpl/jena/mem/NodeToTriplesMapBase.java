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

package com.hp.hpl.jena.mem;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.Field;
import com.hp.hpl.jena.util.iterator.*;

/**
    A base class for in-memory graphs
*/
public abstract class NodeToTriplesMapBase
    {
    /**
         The map from nodes to Bunch(Triple).
    */
     public BunchMap bunchMap = new HashedBunchMap();

    /**
         The number of triples held in this NTM, maintained incrementally 
         (because it's a pain to compute from scratch).
    */
    protected int size = 0;

    protected final Field indexField;
    protected final Field f2;
    protected final Field f3;
    
    public NodeToTriplesMapBase( Field indexField, Field f2, Field f3 )
        { this.indexField = indexField; this.f2 = f2; this.f3 = f3; }
    
    /**
         Add <code>t</code> to this NTM; the node <code>o</code> <i>must</i>
         be the index node of the triple. Answer <code>true</code> iff the triple
         was not previously in the set, ie, it really truly has been added. 
    */
    public abstract boolean add( Triple t );

    /**
         Remove <code>t</code> from this NTM. Answer <code>true</code> iff the 
         triple was previously in the set, ie, it really truly has been removed. 
    */
    public abstract boolean remove( Triple t );

    public abstract Iterator<Triple> iterator( Object o, HashCommon.NotifyEmpty container );

    /**
         Answer true iff this NTM contains the concrete triple <code>t</code>.
    */
    public abstract boolean contains( Triple t );
    
    public abstract boolean containsBySameValueAs( Triple t );

    /**
        The values (usually nodes) which appear in the index position of the stored triples; useful
        for eg listSubjects().
    */
    public final Iterator<Object> domain()
        { return bunchMap.keyIterator(); }

    protected final Object getIndexField( Triple t )
        { return indexField.getField( t ).getIndexingValue(); }

    /**
        Clear this NTM; it will contain no triples.
    */
    public void clear()
        { bunchMap.clear(); size = 0; }

    public int size()
        { return size; }

    public void removedOneViaIterator()
        { size -= 1; /* System.err.println( ">> rOVI: size := " + size ); */ }

    public boolean isEmpty()
        { return size == 0; }

    public abstract ExtendedIterator<Triple> iterator( Node index, Node n2, Node n3 );
    
    /**
        Answer an iterator over all the triples that are indexed by the item <code>y</code>.
        Note that <code>y</code> need not be a Node (because of indexing values).
    */
    public abstract Iterator<Triple> iteratorForIndexed( Object y );
    
    /**
        Answer an iterator over all the triples in this NTM.
    */
    public ExtendedIterator<Triple> iterateAll()
        {
        final Iterator<Object> nodes = domain();
        return new NiceIterator<Triple>() 
            {
            private Iterator<Triple> current = NullIterator.instance();
            private NotifyMe emptier = new NotifyMe();
            
            @Override public Triple next()
                {
                if (hasNext() == false) noElements( "NodeToTriples iterator" );
                return current.next();
                }

            class NotifyMe implements HashCommon.NotifyEmpty
                {
                @Override
                public void emptied()
                    { nodes.remove(); }
                }
            
            @Override public boolean hasNext()
                {
                while (true)
                    {
                    if (current.hasNext()) return true;
                    if (nodes.hasNext() == false) return false;
                    Object next = nodes.next();
                    current = NodeToTriplesMapBase.this.iterator( next, emptier );
                    }
                }

            @Override public void remove()
                { current.remove(); }
            };
        }
    }
