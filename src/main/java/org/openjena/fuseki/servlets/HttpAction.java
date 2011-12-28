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
    private final DatasetGraph dsg ;
    private DatasetGraph activeDSG ;
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
        this.lock = ( dsg != null ) ? dsg.getLock() : null ;
        this.request = request ;
        this.response = response ;
        this.verbose = verbose ;
    }
    
    public final void beginRead()
    {
        enter(dsg, lock, Lock.READ) ;
        getConcurrencyPolicy(lock).startRead() ;
        activeDSG = dsg ;
    }

    public final void endRead()
    {
        getConcurrencyPolicy(lock).finishRead() ;
        leave(dsg, lock, Lock.READ) ;
        activeDSG = null ;
    }

    public final void beginWrite()
    {
        enter(dsg, lock, Lock.WRITE) ;
        getConcurrencyPolicy(lock).startUpdate() ;
        activeDSG = dsg ;
    }

    public final void endWrite()
    {
        sync() ;
        getConcurrencyPolicy(lock).finishUpdate() ;
        leave(dsg, lock, Lock.WRITE) ;
        activeDSG = null ;
    }

    public final DatasetGraph getActiveDSG()
    {
        return activeDSG ;
    }
    
    private void enter(DatasetGraph dsg, Lock lock, boolean readLock)
    {
        if ( lock == null && dsg == null )
            return ;
        if ( lock == null )
            lock = dsg.getLock() ;
        if ( lock == null )
            return ;
        lock.enterCriticalSection(readLock) ;
    }
    
    private void leave(DatasetGraph dsg, Lock lock, boolean readLock)
    {
        if ( lock == null && dsg == null )
            return ;

        if ( lock == null )
            lock = dsg.getLock() ;
        if ( lock == null )
            return ;
        lock.leaveCriticalSection() ;
    }
    
    public void sync()
    {
        SystemARQ.sync(dsg) ;
    }
}
