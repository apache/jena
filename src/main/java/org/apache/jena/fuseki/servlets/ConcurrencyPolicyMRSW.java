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

package org.apache.jena.fuseki.servlets;

import java.util.ConcurrentModificationException ;
import java.util.concurrent.atomic.AtomicLong ;

import org.apache.jena.fuseki.Fuseki ;
import org.slf4j.Logger ;

public final class ConcurrencyPolicyMRSW
{
    static private Logger log = Fuseki.requestLog ; //org.slf4j.LoggerFactory.getLogger(ConcurrencyPolicyMRSW.class) ;
    static private final boolean logging = false ; //log.isDebugEnabled() ;
    
    // This is a simplified version of ConcurrencyPolicyMRSW from TDB. 
    private final AtomicLong readCounter = new AtomicLong(0) ;
    private final AtomicLong writeCounter = new AtomicLong(0) ;
    static private final AtomicLong policyCounter = new AtomicLong(0) ;

    public ConcurrencyPolicyMRSW()
    { policyCounter.incrementAndGet() ; }

    // Loggin -inside the operation.
    
    //@Override
    public void startRead()
    {
        readCounter.getAndIncrement() ;
        log() ;
        checkConcurrency() ;
    }

    //@Override
    public void finishRead()
    {
        log() ;
        readCounter.decrementAndGet() ;
        checkConcurrency() ;
    }

    //@Override
    public void startUpdate()
    {
        writeCounter.getAndIncrement() ;
        log() ;
        checkConcurrency() ;
    }

    //@Override
    public void finishUpdate()
    {
        log() ;
        writeCounter.decrementAndGet() ;
        checkConcurrency() ;
    }

    private synchronized void checkConcurrency()
    {
        long R = readCounter.get() ;
        long W = writeCounter.get() ;
        long id = policyCounter.get();
        if ( R > 0 && W > 0 )
            policyError(id, R, W) ;
        if ( W > 1 )
            policyError(id, R, W) ;
    }

    private void log()
    {
        if ( ! logging ) 
            return ;
        long R , W , id ;
        synchronized(this)
        {
            R = readCounter.get() ;
            W = writeCounter.get() ;
            id = policyCounter.get();
        }
        log.info(format(id, R, W)) ;
    }
    
    private static void policyError(long id, long R, long W)
    {
        policyError(format(id, R, W)) ;
    }

    private static void policyError(String message)
    {
        throw new ConcurrentModificationException(message) ;
    }
    
    private static String format(long id, long R, long W)
    {
        return String.format("(lock=%d) Reader = %d, Writer = %d", id, R, W) ;
    }
}
