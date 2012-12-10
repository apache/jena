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

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

public class HashedTripleBunch extends HashCommon<Triple> implements TripleBunch
    {    
    public HashedTripleBunch( TripleBunch b )
        {
        super( nextSize( (int) (b.size() / loadFactor) ) );
        for (Iterator<Triple> it = b.iterator(); it.hasNext();) add( it.next() );        
        changes = 0;
        }

    @Override protected Triple[] newKeyArray( int size )
        { return new Triple[size]; }

    @Override
    public boolean contains( Triple t )
        { return findSlot( t ) < 0; }    
    
    protected int findSlotBySameValueAs( Triple key )
        {
        int index = initialIndexFor( key );
        while (true)
            {
            Object current = keys[index];
            if (current == null) return index;
            if (key.matches( (Triple) current )) return ~index;
            if (--index < 0) index += capacity;
            }
        }
    
    @Override
    public boolean containsBySameValueAs( Triple t )
        { return findSlotBySameValueAs( t ) < 0; }
    
    /**
        Answer the number of items currently in this TripleBunch. 
        @see com.hp.hpl.jena.mem.TripleBunch#size()
    */
    @Override
    public int size()
        { return size; }
    
    /**
        Answer the current capacity of this HashedTripleBunch; for testing purposes
        only. [Note that the bunch is resized when it is more than half-occupied.] 
    */
    public int currentCapacity()
        { return capacity; }
    
    @Override
    public void add( Triple t )
        {
        keys[findSlot( t )] = t;
        changes += 1;
        if (++size > threshold) grow();
        }
    
    protected void grow()
        {
        Object [] oldContents = keys;
        final int oldCapacity = capacity;
        growCapacityAndThreshold();
        Object [] newKeys = keys = new Triple[capacity];
        for (int i = 0; i < oldCapacity; i += 1)
            {
            Triple t = (Triple) oldContents[i];
            if (t != null) newKeys[findSlot( t )] = t;
            }
        }
    
    @Override public void remove( Triple t )
        {
        super.remove( t );
        changes += 1;
        }
    
    @Override
    public ExtendedIterator<Triple> iterator()
        { return iterator( NotifyEmpty.ignore ); }
    
    @Override
    public ExtendedIterator<Triple> iterator( final NotifyEmpty container )
        { return keyIterator( container ); }
    
}