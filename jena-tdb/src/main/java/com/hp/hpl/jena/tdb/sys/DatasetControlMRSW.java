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

import static java.lang.String.format ;

import java.util.ConcurrentModificationException ;
import java.util.Iterator ;
import java.util.NoSuchElementException ;
import java.util.concurrent.atomic.AtomicLong ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Closeable ;

/** A policy that checks, but does not enforce, single writer or multiple writer locking policy */ 
public class DatasetControlMRSW implements DatasetControl
{
    private final boolean concurrencyChecking = true ;
    private final AtomicLong epoch = new AtomicLong(5) ;                // Update counters, used to check iterators. No need to start at 0.
    private final AtomicLong readCounter = new AtomicLong(0) ;
    private final AtomicLong writeCounter = new AtomicLong(0) ;
    
    public DatasetControlMRSW()
    { }

    @Override
    public void startRead()
    {
        readCounter.getAndIncrement() ;
        checkConcurrency() ;
    }

    @Override
    public void finishRead()
    {
        readCounter.decrementAndGet() ;
    }

    @Override
    public void startUpdate()
    {
        epoch.getAndIncrement() ;
        writeCounter.getAndIncrement() ;
        checkConcurrency() ;
    }

    @Override
    public void finishUpdate()
    {
        writeCounter.decrementAndGet() ;
    }

    private void checkConcurrency()
    {
        long R, W ;
        synchronized (this)
        {
            R = readCounter.get() ;
            W = writeCounter.get() ;
        }

        if ( R > 0 && W > 0 )
            policyError(R, W) ;
        if ( W > 1 )
            policyError(R, W) ;
    }
    
    @Override
    public <T> Iterator<T> iteratorControl(Iterator<T> iter) { return new IteratorCheckNotConcurrent<>(iter, epoch) ; }
    
    private static class IteratorCheckNotConcurrent<T> implements Iterator<T>, Closeable
    {
        private Iterator<T> iter ;
        private AtomicLong eCount ;
        private boolean finished = false ;
        private long startEpoch ; 

        IteratorCheckNotConcurrent(Iterator<T> iter, AtomicLong eCount )
        {
            // Assumes correct locking to set up, i.e. eCount not changing (writer on separate thread).
            this.iter = iter ;
            this.eCount = eCount ;
            this.startEpoch = eCount.get();
        }

        private void checkCourrentModification()
        {
            if ( finished )
                return ;
            
            long now = eCount.get() ;
            if ( now != startEpoch )
            {
                policyError(format("Iterator: started at %d, now %d", startEpoch, now)) ;

            }
        }
        
        @Override
        public boolean hasNext()
        {
            checkCourrentModification() ;
            boolean b = iter.hasNext() ;
            if ( ! b )
                close() ;
            return b ;
        }

        @Override
        public T next()
        {
            checkCourrentModification() ;
            try { 
                return iter.next();
            } catch (NoSuchElementException ex) { close() ; throw ex ; }
        }

        @Override
        public void remove()
        {
            checkCourrentModification() ;
            iter.remove() ;
        }

        @Override
        public void close()
        {
            finished = true ;
            Iter.close(iter) ;
        }
    }

    
    private static void policyError(long R, long W)
    {
        policyError(format("Reader = %d, Writer = %d", R, W)) ;
    }
    
    private static void policyError(String message)
    {
        throw new ConcurrentModificationException(message) ;
    }
    

}
