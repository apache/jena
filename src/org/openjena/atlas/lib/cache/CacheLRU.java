/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib.cache;

import java.util.Iterator;

import org.openjena.atlas.lib.ActionKeyValue ;
import org.openjena.atlas.lib.Cache ;



/** This class is not thread-safe. Add a synchronization wrapper if needed (@link{CacheFcatory.createSync)}  */

public class CacheLRU<K,V> implements Cache<K,V>
{
    // Use an internal class so we don't expose the full LinkedHashMap interface.
    private CacheImpl<K,V> cache ;
    
    public CacheLRU(float loadFactor, int maxSize)
    {
        this.cache = new CacheImpl<K, V>(loadFactor, maxSize) ;
    }

    //@Override
    public void clear()
    { cache.clear() ; }

    //@Override
    public boolean containsKey(K key)
    {
        return cache.containsKey(key) ;
    }

    //@Override
    public V get(K key)
    {
        return cache.get(key) ;
    }

    //@Override
    public V put(K key, V thing)
    {
        return cache.put(key, thing) ;
    }

    //@Override
    public boolean remove(K key)
    {
        V old = cache.remove(key) ;
        return old != null ;
    }

    //@Override
    public long size()
    {
        return cache.size() ;
    }

    //@Override
    // NB Access the iterator must be thread-aware. 
    public Iterator<K> keys()
    {
        return cache.keySet().iterator() ;
    }

    //@Override
    public boolean isEmpty()
    {
        return cache.isEmpty() ;
    }

    /** Callback for entries when dropped from the cache */
    public void setDropHandler(ActionKeyValue<K,V> dropHandler)
    {
        cache.setDropHandler(dropHandler) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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