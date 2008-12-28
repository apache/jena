/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package lib.cache;

import iterator.SingletonIterator;

import java.util.Iterator;

import lib.ActionKeyValue;
import lib.Cache;

/** A one-slot cache.*/
public class Cache1<K, V> implements Cache<K,V>
{
    private K cacheKey ;
    private V cacheValue ;
    
    public Cache1() { clear() ; }
    
    @Override
    public boolean contains(K key)
    {
        if ( cacheKey == null )
            return false ;
        return cacheKey.equals(key) ;
    }

    @Override
    public V getObject(K key)
    {
        if ( cacheKey == null ) return null ;
        if ( cacheKey.equals(key) ) return cacheValue ;
        return null ;
    }

    @Override
    public void clear()
    { 
        cacheKey = null ;
        cacheValue = null ;
    }

    @Override
    public boolean isEmpty()
    {
        return cacheKey == null ;
    }

    @Override
    public Iterator<K> keys()
    {
        return new SingletonIterator<K>(cacheKey) ;
    }

    @Override
    public void putObject(K key, V thing)
    {
        // Displaces any existing cached key/value pair
        cacheKey = key ;
        cacheValue = thing ;
    }

    @Override
    public void removeObject(K key)
    {
        if ( cacheKey == null ) return ;
        if ( cacheKey.equals(key) )
        {
            cacheKey = key ;
            cacheValue = null ;
        }
    }

    @Override
    public void setDropHandler(ActionKeyValue<K, V> dropHandler)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long size()
    {
        return (cacheKey == null) ? 0 : 1 ;
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