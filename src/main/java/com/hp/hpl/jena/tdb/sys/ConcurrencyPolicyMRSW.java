/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.sys;

import static java.lang.String.format ;

import java.util.ConcurrentModificationException ;
import java.util.Iterator ;
import java.util.NoSuchElementException ;
import java.util.concurrent.atomic.AtomicLong ;

/** A policy that checks, but does not enforce, single writer or multiple writer locking policy */ 
public class ConcurrencyPolicyMRSW implements ConcurrencyPolicy
{
    private final boolean concurrencyChecking = true ;
    private final AtomicLong epoch = new AtomicLong(5) ;                // Update counters, used to check iterators. No need to start at 0.
    private final AtomicLong readCounter = new AtomicLong(0) ;
    private final AtomicLong writeCounter = new AtomicLong(0) ;
    
    public ConcurrencyPolicyMRSW()
    { }

    //@Override
    public void startRead()
    {
        readCounter.getAndIncrement() ;
        checkConcurrency() ;
    }

    //@Override
    public void finishRead()
    {
        readCounter.decrementAndGet() ;
    }

    //@Override
    public void startUpdate()
    {
        epoch.getAndIncrement() ;
        writeCounter.getAndIncrement() ;
        checkConcurrency() ;
    }

    //@Override
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
    
    //@Override
    public <T> Iterator<T> checkedIterator(Iterator<T> iter) { return new IteratorCheckNotConcurrent<T>(iter, epoch) ; }
    
    private static class IteratorCheckNotConcurrent<T> implements Iterator<T>
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
        
        public boolean hasNext()
        {
            checkCourrentModification() ;
            boolean b = iter.hasNext() ;
            if ( ! b )
                finished = true ; 
            return b ;
        }

        public T next()
        {
            checkCourrentModification() ;
            try { 
            return iter.next();
            } catch (NoSuchElementException ex) { finished = true ; throw ex ; }
        }

        public void remove()
        {
            checkCourrentModification() ;
            iter.remove() ;
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

/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */