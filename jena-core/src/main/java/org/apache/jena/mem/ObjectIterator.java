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

package org.apache.jena.mem;

import java.util.*;
import java.util.function.Consumer;

import org.apache.jena.graph.* ;
import org.apache.jena.util.CollectionFactory ;
import org.apache.jena.util.iterator.NiceIterator ;

/**
    Helper class for listObjects. Because literal indexing means that the
    domain of the object map is not a node, but an indexing value (shared by
    a bunch of different literal nodes), getting the list of objects requires
    mapping that indexing value to all the triples that use it, and then
    filtering those triples for their objects, removing duplicates.

*/
public abstract class ObjectIterator extends NiceIterator<Node>
    {
    public ObjectIterator( Iterator<?> domain )
        { this.domain = domain; }

    protected abstract Iterator<Triple> iteratorFor( Object y );

    final Iterator<?> domain;
    
    final Set<Node> seen = CollectionFactory.createHashedSet();
    
    final List<Node> pending = new ArrayList<>();
    
    @Override public boolean hasNext()
        {
        while (pending.isEmpty() && domain.hasNext()) refillPending();
        return !pending.isEmpty();                
        }
    
    @Override public Node next()
        {
        if (!hasNext()) throw new NoSuchElementException
            ( "ObjectIterator.next()" );
        return pending.remove( pending.size() - 1 );
        }

    @Override public void forEachRemaining(Consumer<? super Node> action)
        {
            pending.forEach(action);
            domain.forEachRemaining(y ->
                {
                if (y instanceof Node)
                    action.accept( (Node) y );
                else
                    {
                    iteratorFor( y ).forEachRemaining(triple ->
                        {
                        if (seen.add( triple.getObject() )) action.accept( triple.getObject() );
                        });
                    }
                }
            );
        }

    protected void refillPending()
        {
        Object y = domain.next();
        if (y instanceof Node)
            pending.add( (Node) y );
        else
            {
            iteratorFor( y ).forEachRemaining(triple -> {
                if (seen.add( triple.getObject() )) pending.add( triple.getObject() );
            });
            }
        }
    
    @Override public void remove()
        { throw new UnsupportedOperationException( "listObjects remove()" ); }
    }
