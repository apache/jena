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

package org.openjena.atlas.lib;

import java.util.Stack ;

/** A Pool of objects. Base implements a non-blocking pool (returns null on no entry)
 * with infinite upper bound.  Set effective size by creating the right number of
 * entries when created.
 */ 
public class PoolBase<T> implements Pool<T>
{
    // For convenience we operate a LIFO policy.
    // This not part of the extenal contract of a "pool"
    
    //Deque<T> pool = new ArrayDeque<T>(); Better but Java6
    Stack<T> pool = new Stack<T>() ;
    int maxSize = -1 ;  // Unbounded
    
    public PoolBase() {} 
    //public Pool(int maxSize) { this.maxSize = maxSize ; }
    
    public void put(T item)
    {
        // Currently, unbounded
        if ( maxSize >= 0 && pool.size() == 0 )
        {}
        pool.push(item) ;
    }
    
    /** Get an item from the pool - return null if the pool is empty */
    public T get()              
    { 
        if ( pool.size() == 0 ) return null ;
        return pool.pop();
    }
    
    public boolean isEmpty()    { return pool.size() == 0 ; } 
}
