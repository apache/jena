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

package org.apache.jena.atlas.lib;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A key {@literal ->} value 'map' which reference counts entries.
 *
 * <p>
 *   The same (key,value) pair can be added to the map several times and then
 *   removed several times.  A reference count is incremented for each addition
 *   and, provided the count is greater than 0, decremented on removal.
 * </p>
 *
 * <p>
 *   The pair is removed from the map when a remove decrements the reference count to 0.
 * </p>
 *
 * <p>
 *   This class is thread safe.
 * </p>
 *
 * @param <K>
 * @param <T>
 */
public class RefCountingMap<K, T> {

	/*
	 * Uses CountedRef instances which are pairs of an integer and
	 * and a reference.  These instances are immutable - a new instance
	 * is created on each increment/decrement operation.  This could
	 * result in churn in the garbage collector under heavy use.
	 */
	protected Map<K, CountedRef<T>> map = new ConcurrentHashMap<>() ;

    public RefCountingMap() {}

    public boolean contains(K key)         { return map.containsKey(key) ; }
    public Collection<K> keys()            { return map.keySet() ; }
    public int size()                      { return map.size() ; }
    public boolean isEmpty()               { return map.isEmpty() ; }

    /** Clear the map of all keys regardless of reference counts. */
    public void clear()                    { map.clear() ; }

	public Set<K> keySet()                 { return map.keySet(); }
	public boolean containsKey(Object key) { return map.containsKey(key); }

	/**
	 * Add a key value pair to the map.
	 *
	 * <p>
	 *   if there is no entry in the map for the key, then a key value pair is added
	 *   to the map with a reference count of 1.
	 * </p>
	 *
	 * <p>
	 *   If there is already an entry in the map for the same key and value,
	 *   the reference count for that entry is incremented.
	 * </p>
	 *
	 * <p>
	 *   if there is an entry in the map for the key, but with a different value,
	 *   then that entry is replaced with a new entry for the key and value with
	 *   a reference count of 1.
	 * </p>
	 *
	 * @param key
	 * @param value
	 */
    public void add(K key, T value) {
    	// map.compute is atomic
    	map.compute(key,
    			(k, v) -> {
    				int refCount = 1 ;
    				if (v != null && ( v.getRef().equals(value) ) ) {
    					refCount = v.getCount() + 1;
    				}
    				return new CountedRef<>(value, refCount );
    			});
    }

    /**
     * Decrement the reference count for a key, and remove the corresponding entry from the map
     * if the result is 0.
     *
     * <p>
     *   Do nothing if there is no entry in the map corresponding to the key.
     * </p>
     * @param key
     */
    public void remove(K key) {
    	// map.compute is atomic
    	map.compute(key,
    			(k, v) -> {
    				if (v == null)
    					return null ;
    				int refCount = v.getCount() - 1 ;
    				if ( refCount == 0 )
    				    return null ;
    				else
    				    return new CountedRef<>(v.getRef(), refCount);
    			});
    }

    /**
     * Remove the entry corresponding to the key from the map completely.
     *
     * <p>
     *   This method ignores the reference count.
     * </p>
     *
     * @param key
     */
    public void removeAll(K key) {
    	map.remove(key);
    }

    /**
     * Return the reference count for the entry corresponding to a key in the map.
     *
     * <p>
     *   Returns 0 if there is no entry in the map corresponding to the key.
     * </p>
     * @param key
     * @return the reference count for the entry corresponding to key in the map,
     *         or 0 if there is no corresponding entry.
     */
    public int refCount(K key) {
    	CountedRef<T> ref = map.get(key);
    	if (ref == null) {
    		return 0 ;
    	} else {
    		return ref.getCount();
    	}
    }

    /**
     * Return the value associated with a key in the map.
     *
     * @param key
     * @return the value associated with the key, or null if there is no such value.
     */
	public T get(Object key) {
		CountedRef<T> ref = map.get(key);
		if ( ref == null ) return null ;
		return ref.getRef();
	}

	/*
	 * An immutable pair of an integer count and an object reference
	 */
    class CountedRef<R> {
  	    final int refCount;
	    final R    ref;

	    CountedRef(R ref, int refCount) {
	    	this.refCount = refCount;
	    	this.ref = ref;
	    }

	    int getCount()  { return refCount ; }
	    R   getRef()    { return ref; }
    }
}
