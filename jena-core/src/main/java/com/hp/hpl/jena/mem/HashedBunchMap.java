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

import com.hp.hpl.jena.shared.*;

/**
    An implementation of BunchMap that does open-addressed hashing.
*/
public class HashedBunchMap extends HashCommon<Object> implements BunchMap
    {
    protected TripleBunch [] values;
    
    public HashedBunchMap()
        {
        super( 10 );
        values = new TripleBunch[capacity];
        }

    @Override protected Object[] newKeyArray( int size )
        { return new Object[size]; }
    
    /**
        Clear this map: all entries are removed. The keys <i>and value</i> array 
        elements are set to null (so the values may be garbage-collected).
    */
    @Override
    public void clear()
        {
        size = 0;
        for (int i = 0; i < capacity; i += 1) keys[i] = values[i] = null; 
        }  
    
    @Override
    public long size()
        { return size; }
        
    @Override
    public TripleBunch get( Object key )
        {
        int slot = findSlot( key );
        return slot < 0 ? values[~slot] : null;
        }

    @Override
    public void put( Object key, TripleBunch value )
        {
        int slot = findSlot( key );
        if (slot < 0)
            values[~slot] = value;
        else
            {
            keys[slot] = key;
            values[slot] = value; 
            size += 1;
            if (size == threshold) grow();
            }
        }

    protected void grow()
        {
        Object [] oldContents = keys;
        TripleBunch [] oldValues = values;
        final int oldCapacity = capacity;
        growCapacityAndThreshold();
        keys = newKeyArray( capacity );
        values = new TripleBunch[capacity];
        for (int i = 0; i < oldCapacity; i += 1)
            {
            Object key = oldContents[i];
            if (key != null) 
                {
                int j = findSlot( key );
                if (j < 0) 
                    {
                    throw new BrokenException( "oh dear, already have a slot for " + key  + ", viz " + ~j );
                    }
                keys[j] = key;
                values[j] = oldValues[i];
                }
            }
        }

    /**
        Called by HashCommon when a key is removed: remove
        associated element of the <code>values</code> array.
    */
    @Override protected void removeAssociatedValues( int here )
        { values[here] = null; }
    
    /**
        Called by HashCommon when a key is moved: move the
        associated element of the <code>values</code> array.
    */
    @Override protected void moveAssociatedValues( int here, int scan )
        { values[here] = values[scan]; }
    }
