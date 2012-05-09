/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
