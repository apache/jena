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

import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
    A pruned (and slightly stewed) version of Map, containing just those operations
    required by NodeToTriplesMaps. BunchMaps contain only TripleBunch's.

*/
public interface BunchMap
    {
    /**
        Clear this map: all entries are removed.
    */
    public void clear();
    
    /**
        The number of items in the bunch.
    */
    public long size();

    /**
        Answer the TripleBunch associated with <code>key</code>, or 
        <code>null</code> if there isn't one.
    */
    public TripleBunch get( Object key );

    /**
        Associate <code>key</code> and <code>value</code>. Any existing
        association of <code>key</code> is lost. <code>get</code> on this key
        will now deliver this value.
    */
    public void put( Object key, TripleBunch value );

    /**
        Remove any association for <code>key</code>; <code>get</code> on this
        key will now deliver <code>null</code>.
    */
    public void remove( Object key );

    /**
        Answer an iterator over all the keys in this map.
    */
    public ExtendedIterator<Object> keyIterator();
    }
