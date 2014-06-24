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

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import com.hp.hpl.jena.sparql.ARQException;

/** Finite capacity pool - capacity is fixed at create time */ 
public class PoolFiniteBlocking<T> implements Pool<T>
{
    BlockingDeque<T> pool  ;
    
    public PoolFiniteBlocking(int size) { pool = new LinkedBlockingDeque<>(size) ; }
    
    @Override
    public final void put(T item)
    {
        pool.addLast(item) ;
    }
    
    @Override
    public T get()              
    { 
        try
        { 
            return pool.takeFirst() ;
        } catch (InterruptedException ex)
        {
            throw new ARQException("Failed to get an item from the pool (InterruptedException): "+ex.getMessage()) ;
        }
    }
    
    @Override
    public boolean isEmpty()    { return pool.isEmpty() ; } 
}
