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

/** A caching store for objects.
 *
 * <p>A caching store will hold on to some objects for some
 * time, but may fail to store them.  It is used as an
 * optimization, so that objects that have already been
 * constructed, need not be made again.  The null object
 * should not be stored under a key as there is no way
 * to distingish this from a missing object.</p>
 *
 * <p>Cache objects are usually created using the {@link CacheManager }.</p>
 *
 * <p>An object is associated with a key which is used to
 * identify the object on retrieval.  Only one object may be
 * associated with a key.</p>
 */
public interface Cache extends CacheControl {
    /** Get and object from the cache, if it is there.
     * @param key the key for the object sought
     * @return the object associated with the key, or null if
     * the key is not found in the cache
     */
    public Object get(Object key);
    /** Store an object in the cache
     * @param key the key for the object being stored
     * @param value the object stored under the key
     *
     */
    public void put(Object key, Object value);
}
