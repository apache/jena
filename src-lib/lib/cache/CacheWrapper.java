/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package lib.cache;

import java.util.Iterator;

import lib.ActionKeyValue;
import lib.Cache;

public class CacheWrapper<K,V> implements Cache<K,V>
{
    protected Cache<K,V> cache ;
    
    public CacheWrapper(Cache<K,V> cache) { this.cache = cache ; }

    @Override
    public void clear()                             { cache.clear(); }

    @Override
    public boolean contains(K key)                  { return cache.contains(key) ; }
    
    @Override
    public V getObject(K key, boolean exclusive)    { return cache.getObject(key, exclusive) ; }

    @Override
    public boolean isEmpty()                        { return cache.isEmpty() ; }

    @Override
    public Iterator<K> keys()                       { return cache.keys(); }

    @Override
    public void promote(K key)                      { cache.promote(key) ; }

    @Override
    public void putObject(K key, V thing)           { cache.putObject(key, thing) ; }

    @Override
    public void removeObject(K key)                 { cache.removeObject(key) ; }

    @Override
    public void returnObject(K key)                 { cache.removeObject(key) ; }

    @Override
    public void setDropHandler(ActionKeyValue<K, V> dropHandler)
    { cache.setDropHandler(dropHandler) ; }

    @Override
    public long size()                              { return cache.size() ; }
    
    
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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