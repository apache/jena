/**
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

package org.openjena.atlas.lib.cache;

import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.ActionKeyValue ;
import org.openjena.atlas.lib.Cache ;

/** A cache that keeps nothing */ 
public final class Cache0<K, V> implements Cache<K,V>
{
    public boolean containsKey(K key)
    {
        return false ;
    }

    public V get(K key)
    {
        return null ;
    }

    public V put(K key, V thing)
    {
        return null ;
    }

    public boolean remove(K key)
    {
        return false ;
    }

    public Iterator<K> keys()
    {
        return Iter.nullIterator() ;
    }

    public boolean isEmpty()
    {
        return true ;
    }

    public void clear()
    {}

    public long size()
    {
        return 0 ;
    }

    public void setDropHandler(ActionKeyValue<K, V> dropHandler)
    {}

}
