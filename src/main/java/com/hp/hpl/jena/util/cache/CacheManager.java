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

package com.hp.hpl.jena.util.cache;

/** A factory for creating cache objects
 */

public class CacheManager {

    public static final String RAND = "RAND";
    
    public static final String ENHNODECACHE = "ENHNODECACHE";

    /** Creates new Manager */
    private CacheManager() {
    }

    /** Create a new cache
     * @param type The type of cache to create.  This should be one
     * of the standard cache types defined in this class.
     * @param name A name for the cache.  This should be unique and
     * may be used to identify the cache in logging and
     * other operations.  To ensure uniqueness it is
     * suggested that cache's be given names similar to
     * full java names such as
     * com.hp.hpl.jena.graph.Node.NodeCache.
     * @param size Teh size of the cache in terms of the number of
     * objects it can store.
     * @return a newly created cache object
     *
     */
    public static Cache createCache(String type, String name, int size) {
        // for now we just have one type
        if (type.equals(RAND)) return new RandCache( name, size );
        if (type.equals(ENHNODECACHE)) return new EnhancedNodeCache( name, size );
        throw new Error( "Bad cache type: " + type );
    }
}
