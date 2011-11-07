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

import java.util.Map;

import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;

/**
    An implementation of BunchMap that delegates to a [Hashed]Map.
*/
public class WrappedHashMap implements BunchMap
    {
    protected final Map<Object, TripleBunch> map = CollectionFactory.createHashedMap();
    
    @Override
    public void clear()
        { map.clear(); }
    
    @Override
    public long size()
        { return map.size(); }

    @Override
    public TripleBunch get( Object key )
        { return map.get( key ); }

    @Override
    public void put( Object key, TripleBunch value )
        { map.put( key, value ); }

    @Override
    public void remove( Object key )
        { map.remove( key ); }

    @Override
    public ExtendedIterator<Object> keyIterator()
        { return WrappedIterator.create( map.keySet().iterator() ); }
    }
