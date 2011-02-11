/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.servlets;


import java.util.HashMap ;
import java.util.Map ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

class HttpAction
{
    final long id ;
    final DatasetGraph dsg ;
    final Lock lock ;
    final HttpServletRequest request;
    final HttpServletResponse response ;
    final boolean verbose ;
    
    // ---- Concurrency checking.
    private static Map<Lock, ConcurrencyPolicyMRSW> lockCounters = new HashMap<Lock, ConcurrencyPolicyMRSW>() ;
    private static ConcurrencyPolicyMRSW getConcurrencyPolicy(Lock lock)
    {
        synchronized(lockCounters)
        {
            ConcurrencyPolicyMRSW x = lockCounters.get(lock) ;
            if ( x == null )
            {
                x = new ConcurrencyPolicyMRSW() ;
                lockCounters.put(lock, x) ;
            }
            return x ;
        }
    }

    public HttpAction(long id, DatasetGraph dsg, HttpServletRequest request, HttpServletResponse response, boolean verbose)
    {
        this.id = id ;
        this.dsg = dsg ;
        this.lock = dsg.getLock() ;
        this.request = request ;
        this.response = response ;
        this.verbose = verbose ;
    }
    
    public final void beginRead()
    {
        enter(dsg, lock, Lock.READ) ;
        getConcurrencyPolicy(lock).startRead() ;
    }

    public final void endRead()
    {
        getConcurrencyPolicy(lock).finishRead() ;
        leave(dsg, lock, Lock.READ) ;
    }

    public final void beginWrite()
    {
        enter(dsg, lock, Lock.WRITE) ;
        getConcurrencyPolicy(lock).startUpdate() ;
    }

    public final void endWrite()
    {
        sync() ;
        getConcurrencyPolicy(lock).finishUpdate() ;
        leave(dsg, lock, Lock.WRITE) ;
    }

    private void enter(DatasetGraph dsg, Lock lock, boolean readLock)
    {
        if ( lock == null )
            lock = dsg.getLock() ;
        if ( lock == null )
            return ;
        lock.enterCriticalSection(readLock) ;
    }
    
    private void leave(DatasetGraph dsg, Lock lock, boolean readLock)
    {
        if ( lock == null )
            lock = dsg.getLock() ;
        if ( lock == null )
            return ;
        lock.leaveCriticalSection() ;
    }
    
    public void sync()
    {
        SystemARQ.sync(dsg) ;
        //TDB.sync(dsg) ;
    }
}

/*
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
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