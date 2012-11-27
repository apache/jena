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

package org.apache.jena.atlas.lib.cache;

import java.util.LinkedHashMap ;
import java.util.Map ;

import org.apache.jena.atlas.lib.ActionKeyValue ;


/** Cache implementation - use as implementation inheritance - hides the LinkedHashMap methods */  
class CacheImpl<K,V> extends LinkedHashMap<K, V>
{
    int maxEntries ; 
    ActionKeyValue<K,V> dropHandler = null ;

    public CacheImpl(int maxSize)
    {
        this(0.75f, maxSize) ;
    }

    public CacheImpl(float loadFactor, int maxSize)
    {
        // True => Access order, which is what makes it LRU

        // Initial size is max size + slop, rounded up, for the load factor
        // i.e. it allocate the space needed once at create time.

        super( Math.round(maxSize/loadFactor+0.5f)+1, loadFactor, true) ;
        // which is also (int)Math.floor(a + 1f)
        // and hence can be one larger than needed.  But safer than one less.
        // +1 is the need for the added entry before the removing the "eldest"
        maxEntries = maxSize ;
    }

    /** Callback for entries when dropped from the cache */
    public void setDropHandler(ActionKeyValue<K,V> dropHandler)
    {
        this.dropHandler = dropHandler ;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) 
    {
        // Overshoots by one - the new entry is added, then this called.
        // Initial capacity adjusted to allow for this.

        boolean b = ( size() > maxEntries ) ;
        if ( b && dropHandler != null )
            // Should not delete the entry - LinkedHashMap will do that.
            dropHandler.apply(eldest.getKey(), eldest.getValue()) ;
        return b ;
    }
}
