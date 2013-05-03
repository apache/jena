/**
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

package org.apache.jena.riot.adapters;

import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;

import com.hp.hpl.jena.rdf.model.Model ;

// Legacy support.
class FileManagerModelCache {

    private static final int CacheSize = 500 ; 
    public FileManagerModelCache() {}
    
    boolean cacheModelLoads = false ;
    Cache<String, Model> modelCache = null ;
    // -------- Cache operations
    
    /** Reset the model cache */
    public void resetCache()
    {
        if ( modelCache != null )
            modelCache.clear() ;
    }
    
    /** Change the state of model cache : does not clear the cache */ 
    public void setModelCaching(boolean state)
    {
        cacheModelLoads = state ;
        if ( cacheModelLoads && modelCache == null )
            modelCache = CacheFactory.createCache(CacheSize) ;
    }
    
    /** return whether caching is on of off */
    public boolean isCachingModels() { return cacheModelLoads ; }
    
    /** Read out of the cache - return null if not in the cache */ 
    public Model getFromCache(String filenameOrURI)
    { 
        if ( ! isCachingModels() )
            return null; 
        return modelCache.get(filenameOrURI) ;
    }
    
    public boolean hasCachedModel(String filenameOrURI)
    { 
        if ( ! isCachingModels() )
            return false ; 
        return modelCache.containsKey(filenameOrURI) ;
    }
    
    public void addCacheModel(String uri, Model m)
    { 
        if ( isCachingModels() )
            modelCache.put(uri, m) ;
    }

    public void removeCacheModel(String uri)
    { 
        if ( isCachingModels() )
            modelCache.remove(uri) ;
    }


}
