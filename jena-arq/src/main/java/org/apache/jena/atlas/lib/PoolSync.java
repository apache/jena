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


/** Synchronization wrapper for a pool */ 
public class PoolSync<T> implements Pool<T>
{
    private Pool<T> pool ;

    public static <T> Pool<T> create(Pool<T> pool)
    { 
        if ( pool instanceof PoolSync<?>)
        {
            PoolSync<T> p = (PoolSync<T>)pool ;
            return p ;
        }
        return new PoolSync<>(pool) ;
    }
    
    public PoolSync(Pool<T> pool) { this.pool = pool ; } 
    
    @Override
    public final synchronized void put(T item)
    {
        pool.put(item) ;
    }
    
    @Override
    public final synchronized T get()              
    { 
        return pool.get();
    }
    
    @Override
    public final synchronized boolean isEmpty()    { return pool.isEmpty() ; } 
}
