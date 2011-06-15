/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.servlets;

import java.util.ConcurrentModificationException ;
import java.util.concurrent.atomic.AtomicLong ;

import org.openjena.fuseki.Fuseki ;
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