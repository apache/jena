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
/**
* Interface signature for cache implementations. Instances of this are
* used to cache resources and literals loaded from a database.
* <p>The type of the stored objects is left generic in case other caches
* are needed but could specialize it to RDFNodes.
*
* @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
* @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:37 $
*/


public interface ICache<K, V> {

    /**
     * Add an entry to the cache
     * @param id the database ID to be used as an index
     * @param val the literal or resources to be stored
     */
    public void put(K id, V val);

    /**
     * Retreive an object from the cache
     * @param id the database ID of the object to be retrieved
     * @return the object or null if it is not in the cache
     */
    public V get(K id);

    /**
     * Set a threshold for the cache size in terms of the count of cache entries.
     * For literals a storage limit rather than a count might be more useful but
     * counts are easier, more general and sufficient for the current use.
     *
     * @param threshold the cache size limit, use 0 for no cache, -1 for
     * unlimited cache growth; any other number indicates the number of cache entries
     */
    public void setLimit(int threshold);

    /**
     * Return the current threshold limit for the cache size.
     */
    public int getLimit();
}
