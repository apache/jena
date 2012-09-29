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

/** An interface for controlling the behaviour of a cache.
 *
 * <p>This is separated from the main {@link Cache } interface
 * so that methods return an object that can set control
 * parameters on a cache, without granting read/write access
 * to the cache itself.</p>
 *
 * <p>Cache's may be enabled or disabled.  A disabled cache
 * is a silent cache; it will silently not return objects
 * from its store and not update its store.  It will operate
 * as if the cache always missed.</p>
 *
 * <p>Cache's keep statistics on their accesses.  On a long
 * running cache the numbers may exceeed the size of the
 * variables counting the statistics, in which case, the
 * fields counting gets hits and puts are reduced
 * proportionately.</p>
 */
public interface CacheControl {
    
    /** Get the enabled state of the cache
     * @return The enabled state of the cache
     */    
    public boolean getEnabled();
    
    /** Set the enabled state of a cache
     * @param enabled the new enabled state of the cache
     * @return the previous enabled state of the cache
     */    
    public boolean setEnabled(boolean enabled);
    
    /** Clear the cache's store
     */    
    public void clear();
    
    /** Return number of gets on this cache.
     *
     *
     * @return The number of gets on this cache.
     */    
    public long getGets();
    /** Get the number of puts on this cache
     * @return the number of puts
     */    
    public long getPuts();
    /** Get the number of hits on this cache
     * @return the number of hits
     */    
    public long getHits();
}
