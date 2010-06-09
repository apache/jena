/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib.cache;

import java.util.concurrent.atomic.AtomicLong ;

import org.openjena.atlas.lib.ActionKeyValue ;
import org.openjena.atlas.lib.Cache ;
import org.openjena.atlas.lib.CacheStats ;


/** Capture statisics for a cache - this class is thread safe (you can read the stats while the cache is active) */ 
public class CacheStatsAtomic<Key,Value> extends CacheWrapper<Key,Value> implements CacheStats<Key,Value>
{
    // Overall statistics 
    // AtomicLong?
//    private long cacheEntries = 0 ;
//    private long cacheHits = 0 ;
//    private long cacheMisses = 0 ; 
//    private long cacheEjects = 0 ;

    private final AtomicLong cacheEntries = new AtomicLong(0) ;
    private final AtomicLong cacheHits    = new AtomicLong(0) ;
    private final AtomicLong cacheMisses  = new AtomicLong(0) ; 
    private final AtomicLong cacheEjects  = new AtomicLong(0) ;
    
    // ----
    private class EjectMonitor implements ActionKeyValue<Key,Value>
    {
     
        private ActionKeyValue<Key, Value> other ;

        // Wrap any real drop handler. 
        EjectMonitor(ActionKeyValue<Key,Value> other) { this.other = other ; }

        //@Override
        public void apply(Key key, Value thing)
        { 
            cacheEjects.getAndIncrement() ;
            if ( other != null )
                other.apply(key, thing) ;
        }
    } ;
    // ----

    
    public CacheStatsAtomic(Cache<Key,Value> cache)
    { 
        super(cache) ;
        cache.setDropHandler(new EjectMonitor(null)) ;
    }
    
    @Override
    public Value get(Key key)
    { 
        Value x = cache.get(key) ;
        if ( x == null )
            cacheMisses.getAndIncrement() ;
        else
            cacheHits.getAndIncrement() ;
        return x ;
    }
    
    @Override
    public Value put(Key key, Value t)
    {
        Value v = cache.put(key, t) ;
        if ( v == null )
            // Was not there before
            cacheEntries.getAndIncrement() ;
        return v ;
    }
    
    @Override
    public boolean remove(Key key)
    {
        boolean b = cache.remove(key) ;
        if ( b )
            cacheEntries.getAndDecrement() ;
        return b ;
    }
    
    @Override
    public void clear()
    { 
        cache.clear();
        cacheEntries.set(0) ;
    }
    
    @Override
    public void setDropHandler(ActionKeyValue<Key,Value> dropHandler)
    {
        cache.setDropHandler(new EjectMonitor(dropHandler)) ;
    }
    
    public final long getCacheEntries() { return cacheEntries.get() ; }
    public final long getCacheHits()    { return cacheHits.get() ; }
    public final long getCacheMisses()  { return cacheMisses.get() ; }
    public final long getCacheEjects()  { return cacheEjects.get() ; }
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