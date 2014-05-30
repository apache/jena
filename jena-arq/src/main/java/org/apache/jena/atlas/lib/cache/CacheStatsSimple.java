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

import org.apache.jena.atlas.lib.ActionKeyValue ;
import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheStats ;

/** Collect statistics for a cache - this class is not thread safe (@see{CacheStatsAtomic}) */ 
public class CacheStatsSimple<Key,Value> extends CacheWrapper<Key,Value> implements CacheStats<Key, Value>
{
    // Overall statistics 
    private long cacheEntries = 0 ;
    private long cacheHits = 0 ;
    private long cacheMisses = 0 ; 
    private long cacheEjects = 0 ;

    // ---- trap and count ejections.  Does not update the cache size
    private class EjectMonitor implements ActionKeyValue<Key,Value>
    {
        private ActionKeyValue<Key, Value> other ;

        // Wrap any real drop handler. 
        EjectMonitor(ActionKeyValue<Key,Value> other) { this.other = other ; }

        @Override
        public void apply(Key key, Value thing)
        { 
            cacheEjects++ ;
            if ( other != null )
                other.apply(key, thing) ;
        }
    }
    // ----

    
    public CacheStatsSimple(Cache<Key,Value> cache)
    { 
        super(cache) ;
        cache.setDropHandler(new EjectMonitor(null)) ;
    }
    
    @Override
    public Value get(Key key)
    { 
        if ( cache.containsKey(key) )
            cacheMisses ++ ;
        else
            cacheHits++ ;
        return cache.get(key) ;
    }
    
    @Override
    public Value put(Key key, Value value)
    {
        Value v = cache.put(key, value) ;
        if ( v == null )
            // Was not there before
            cacheEntries ++ ;
        return v ;
    }
    
    @Override
    public boolean remove(Key key)
    {
        boolean b = cache.remove(key) ;
        if ( b )
            cacheEntries-- ;
        return b ;
    }
    
    @Override
    public void clear()
    { 
        cache.clear();
        cacheEntries = 0 ;
    }
    
    @Override
    public void setDropHandler(ActionKeyValue<Key,Value> dropHandler)
    {
        cache.setDropHandler(new EjectMonitor(dropHandler)) ;
    }
    
    @Override
    public final long getCacheEntries() { return cacheEntries ; }
    @Override
    public final long getCacheHits()    { return cacheHits ; }
    @Override
    public final long getCacheMisses()  { return cacheMisses ; }
    @Override
    public final long getCacheEjects()  { return cacheEjects ; }
}
