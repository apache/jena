/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2.index;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.sdb.Store;

/** Experimental : cache for int <=> node hash 
 * 
 */
public class IndexCache
{
    
    static int LIMIT = 10 ;
    static private Map<Store, IndexCache> indexes = new HashMap<Store, IndexCache>() ;
    public static IndexCache getIndexCache(Store store)
    {
        IndexCache idx = indexes.get(store) ;
        if ( idx == null )
        {
            idx = new IndexCache() ;
            indexes.put(store, idx) ;
        }
        return idx ;
    }
    
    // Finite cache?
    // Use a reverse structure for cache mgt?
    // cache.entrySet()
    // Later : use Trove on basic types.
    
    private Map<Long, Integer>cache = new HashMap<Long, Integer>() ;
    
    private IndexCache() {}
    
    public Integer get(Long hashCode)
    {
        Integer i = _get(hashCode) ;
        if ( i == null )
        {
            i = fetch(hashCode) ;
            insert(hashCode, i) ;
        }
        
        return i ;
    }
    
    private Integer _get(Long hashCode)
    {
        Integer idx = cache.get(hashCode) ;
        if ( idx != null )
        {
            // Move to end of LRU list. 
        }
        return idx ;
    }
    
    private Integer fetch(Long hashCode)
    {
        
        return -1 ;
    }
    
    private void insert(Long hashCode, Integer idx)
    {
        if ( cache.size() > LIMIT )
        {
            
        }
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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