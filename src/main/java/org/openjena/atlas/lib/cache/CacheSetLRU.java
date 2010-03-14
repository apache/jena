/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib.cache;

import org.openjena.atlas.lib.Action ;
import org.openjena.atlas.lib.ActionKeyValue ;
import org.openjena.atlas.lib.CacheSet ;


/** Cache set - tracks LRU of objects */
public class CacheSetLRU<T> implements CacheSet<T>
{
    //LinkHashSet does not have LRU support.
    static Object theOnlyValue = new Object() ;
    CacheImpl<T, Object> cacheMap = null ;
    
    public CacheSetLRU(int maxSize)
    {
        this(0.75f, maxSize) ;
    }
    
    public CacheSetLRU(float loadFactor, int maxSize)
    {
        cacheMap = new CacheImpl<T, Object>(loadFactor, maxSize) ;
    }

//    /** Callback for entries when dropped from the cache */
//    public void setDropHandler(Action<T> dropHandler)
//    {
//        cacheMap.setDropHandler(new Wrapper<T>(dropHandler)) ;
//    }
    
    // From map action to set action.
    static class Wrapper<T>  implements ActionKeyValue<T, Object>
    {
        Action<T> dropHandler ;
        public Wrapper(Action<T> dropHandler)
        { this.dropHandler = dropHandler ; }

        //@Override
        public void apply(T key, Object value)
        { dropHandler.apply(key) ; }

    }
    
    //@Override
    synchronized
    public void add(T e)
    {
        cacheMap.put(e, theOnlyValue) ;
    }


    synchronized
    public void clear()
    { 
        cacheMap.clear() ;
    }


    synchronized
    public boolean contains(T obj)
    {
        return cacheMap.containsKey(obj) ;
    }


    synchronized
    public boolean isEmpty()
    {
        return cacheMap.isEmpty() ;
    }


//    public Iterator<T> iterator()
//    {
//        return cacheMap.keySet().iterator() ;
//    }


    synchronized
    public void remove(T obj)
    {
        cacheMap.remove(obj);
    }


    synchronized
    public long size()
    {
        return cacheMap.size() ;
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