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

//=======================================================================
// Package
package com.hp.hpl.jena.db.impl;

//=======================================================================
// Imports
import java.util.*;

import com.hp.hpl.jena.util.CollectionFactory;


//=======================================================================
/**
* Trivial implementation of the generic cache interface used to cache
* literals and resources. This implementation simple flushes the cache
* when the threshold limit is exceeded.
*
* @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
* @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:37 $
*/

public class SimpleCache<K, V> implements ICache<K, V> {

    /** The cache itself */
    protected Map<K, V> cache = CollectionFactory.createHashedMap();

    /** The current size limit */
    protected int threshold;

    /** The current number of entries (probably redundant, just use cache.size) */
    protected int count = 0;

    /**
     * Create an empty cache with the given threshold limit.
     *
     * @param threshold the cache size limit, use 0 for no cache, -1 for
     * unlimited cache growth; any other number indicates the number of cache entries
     */
    public SimpleCache(int threshold) {
        this.threshold = threshold;
    }

    /**
     * Add an entry to the cache
     * @param id the database ID to be used as an index
     * @param val the literal or resources to be stored
     */
    @Override
    public void put(K id, V val) {
        if (threshold == 0) return;
        if (threshold > 0 && count >= threshold) {
            cache = CollectionFactory.createHashedMap();
            count = 0;
        }
        count++;
        cache.put(id, val);
    }

    /**
     * Retreive an object from the cache
     * @param id the database ID of the object to be retrieved
     * @return the object or null if it is not in the cache
     */
    @Override
    public V get(K id) {
        return cache.get(id);
    }

    /**
     * Set a threshold for the cache size in terms of the count of cache entries.
     * For literals a storage limit rather than a count might be more useful but
     * counts are easier, more general and sufficient for the current use.
     *
     * @param threshold the cache size limit, use 0 for no cache, -1 for
     * unlimited cache growth; any other number indicates the number of cache entries
     */
    @Override
    public void setLimit(int threshold) {
        this.threshold = threshold;
        if (threshold >= 0 && count > threshold) {
            cache = CollectionFactory.createHashedMap();
            count = 0;
        }
    }

    /**
     * Return the current threshold limit for the cache size.
     */
    @Override
    public int getLimit() {
        return threshold;
    }
}
