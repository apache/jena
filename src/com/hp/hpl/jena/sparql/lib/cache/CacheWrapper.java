/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lib.cache;
import java.util.Iterator;

import com.hp.hpl.jena.sparql.lib.ActionKeyValue;
import com.hp.hpl.jena.sparql.lib.Cache;


public class CacheWrapper<Key,T> implements Cache<Key,T>
{
    protected Cache<Key,T> cache ;
    
    public CacheWrapper(Cache<Key,T> cache)         { this.cache = cache ; }

    //@Override
    public void clear()                             { cache.clear(); }

    //@Override
    public boolean containsKey(Key key)             { return cache.containsKey(key) ; }
    
    //@Override
    //public V getObject(K key, boolean exclusive)    { return cache.getObject(key, exclusive) ; }
    public T get(Key key)                           { return cache.get(key) ; }

    //@Override
    public boolean isEmpty()                        { return cache.isEmpty() ; }

    //@Override
    public Iterator<Key> keys()                     { return cache.keys(); }

    //@Override
    public T put(Key key, T thing)                  { return cache.put(key, thing) ; }

    //@Override
    public boolean remove(Key key)                  { return cache.remove(key) ; }

    //@Override
    public void setDropHandler(ActionKeyValue<Key, T> dropHandler)
    { cache.setDropHandler(dropHandler) ; }

    //@Override
    public long size()                              { return cache.size() ; }

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