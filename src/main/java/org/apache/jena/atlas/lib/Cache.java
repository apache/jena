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

package org.apache.jena.atlas.lib;

import java.util.Iterator ;

/** A cache */
public interface Cache<Key, Value>
{
    /** Does the cache contain the key? */
    public boolean containsKey(Key key) ;
    
    /** Get from cache - or return null.  
     * Implementations should state whether
     * they are thread-safe or not. */ 
    public Value get(Key key) ;
    
    /** Insert into from cache and return old value (or null if none) */
    public Value put(Key key, Value thing) ;

    /** Remove from cache - return true if key referenced an entry */
    public boolean remove(Key key) ;
    
    /** Iterate over all keys. Iteratering over the keys requires the caller be thread-safe. */ 
    public Iterator<Key> keys() ;
    
    public boolean isEmpty() ;
    public void clear() ;
    
    /** Current size of cache */
    public long size() ;
    
    /** Register a callback - called when an object is dropped from the cache (optional operation) */ 
    public void setDropHandler(ActionKeyValue<Key,Value> dropHandler) ;
}
