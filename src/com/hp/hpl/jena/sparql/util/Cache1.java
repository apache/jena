/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import com.hp.hpl.jena.util.cache.Cache;

/** A one slot cache */

public class Cache1 implements Cache
{
    boolean isEnabled = true ;
    Object cacheKey = null ;
    Object cacheValue = null ;
    
    int numGet = 0 ;
    int numPut = 0 ;
    int numHits = 0 ;
    
    public Object get(Object key)
    {
        if ( ! isEnabled )
            return null ;
        
        numGet ++ ;
        
        if ( cacheKey == null )
            return null ;
        
        if ( cacheKey.equals(key) )
        {
            numHits ++ ;
            return cacheValue ;
        }
        
        return null ;
    }

    public void put(Object key, Object value)
    {
        if ( ! isEnabled )
            return ;
        numPut ++ ;
        cacheKey = key ; 
        cacheValue = value ;
    }

    public boolean getEnabled()
    {
        return isEnabled ;
    }

    public boolean setEnabled(boolean enabled)
    {
        boolean b = isEnabled ;
        isEnabled = enabled ;
        return b ;
    }

    public void clear()
    {
        cacheKey = null ;
        cacheValue = null ;
    }

    public long getGets() { return numGet ; }

    public long getPuts() { return numPut ; }

    public long getHits() { return numHits ; }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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