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

package com.hp.hpl.jena.tdb.sys;

import java.util.Iterator ;
import java.util.concurrent.atomic.AtomicLong ;

import com.hp.hpl.jena.tdb.TDBException ;

/** A policy that provide read-only access */ 
public class DatasetControlReadOnly implements DatasetControl
{
    private final AtomicLong readCounter = new AtomicLong(0) ;
    
    public DatasetControlReadOnly()
    { }

    @Override
    public void startRead()
    {
        readCounter.getAndIncrement() ;
    }

    @Override
    public void finishRead()
    {
        readCounter.decrementAndGet() ;
    }

    @Override
    public void startUpdate()
    {
        throw new TDBException("Read-only") ;
    }

    @Override
    public void finishUpdate()
    {
        throw new TDBException("Read-only") ;
    }

    @Override
    public <T> Iterator<T> iteratorControl(Iterator<T> iter) { return iter ; }
}
