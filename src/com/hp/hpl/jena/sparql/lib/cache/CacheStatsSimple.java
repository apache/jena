/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lib.cache;

import com.hp.hpl.jena.sparql.lib.ActionKeyValue;
import com.hp.hpl.jena.sparql.lib.Cache;
import com.hp.hpl.jena.sparql.lib.CacheStats;

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

        //@Override
        public void apply(Key key, Value thing)
        { 
            cacheEjects++ ;
            if ( other != null )
                other.apply(key, thing) ;
        }
    } ;
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
    
    public final long getCacheEntries() { return cacheEntries ; }
    public final long getCacheHits()    { return cacheHits ; }
    public final long getCacheMisses()  { return cacheMisses ; }
    public final long getCacheEjects()  { return cacheEjects ; }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */