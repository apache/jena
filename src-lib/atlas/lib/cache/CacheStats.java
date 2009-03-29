/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package atlas.lib.cache;

import atlas.lib.ActionKeyValue;
import atlas.lib.Cache;

public class CacheStats<Key,T> extends CacheWrapper<Key,T>
{
    // Overall statistics 
    private long cacheEntries = 0 ;
    private long cacheHits = 0 ;
    private long cacheMisses = 0 ; 
    private long cacheEjects = 0 ;

    // ----
    private class EjectMonitor implements ActionKeyValue<Key,T>
    {
     
        private ActionKeyValue<Key, T> other ;

        EjectMonitor(ActionKeyValue<Key,T> other) { this.other = other ; }

        @Override
        public void apply(Key key, T thing)
        { 
            cacheEjects++ ;
            if ( other != null )
                other.apply(key, thing) ;
        }
    } ;
    // ----

    
    public CacheStats(Cache<Key,T> cache)
    { 
        super(cache) ;
        cache.setDropHandler(new EjectMonitor(null)) ;
    }
    
    @Override synchronized
    public T getObject(Key key)
    { 
        if ( cache.contains(key) )
            cacheMisses ++ ;
        else
            cacheHits++ ;
        return cache.getObject(key) ;
    }
    
    @Override synchronized
    public void putObject(Key key, T t)
    {
        T v = getObject(key) ;
        if ( v == null )
        {
            cacheEntries++ ;
            cache.putObject(key, v) ;
            return ;
        }
        //if ( v.equals(t) ) { }
        // Do it anyway to be consistent 
        cache.putObject(key, v) ;
    }
    
    @Override synchronized
    public void removeObject(Key key)
    {
        if ( cache.contains(key) )
            cacheEntries-- ;

        // Do it anyway to be consistent
        cache.removeObject(key) ;
    }
    
    
    @Override synchronized
    public void setDropHandler(ActionKeyValue<Key,T> dropHandler)
    {
        cache.setDropHandler(new EjectMonitor(dropHandler)) ;
    }
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