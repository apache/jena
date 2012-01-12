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


import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Transactional ;
import com.hp.hpl.jena.tdb.migrate.DatasetGraphWithLock ;

public class HttpAction
{
    public final long id ;
    private final DatasetGraph dsg ;
    private final Transactional transactional ;
    private DatasetGraph activeDSG ;
    
    public final Lock lock ;
    public final HttpServletRequest request;
    public final HttpServletResponse response ;
    public final boolean verbose ;
    
//    // ---- Concurrency checking.
//    private static Map<Lock, ConcurrencyPolicyMRSW> lockCounters = new HashMap<Lock, ConcurrencyPolicyMRSW>() ;
//    private static ConcurrencyPolicyMRSW getConcurrencyPolicy(Lock lock)
//    {
//        synchronized(lockCounters)
//        {
//            ConcurrencyPolicyMRSW x = lockCounters.get(lock) ;
//            if ( x == null )
//            {
//                x = new ConcurrencyPolicyMRSW() ;
//                lockCounters.put(lock, x) ;
//            }
//            return x ;
//        }
//    }

    public HttpAction(long id, DatasetGraph dsg, HttpServletRequest request, HttpServletResponse response, boolean verbose)
    {
        this.id = id ;
        this.dsg = dsg ;
        this.lock = ( dsg != null ) ? dsg.getLock() : null ;
        if ( dsg instanceof Transactional )
            transactional = (Transactional)dsg ;
        else
        {
            DatasetGraphWithLock dsglock = new DatasetGraphWithLock(dsg) ; 
            transactional = dsglock ;
            dsg = dsglock ;
        }
        this.request = request ;
        this.response = response ;
        this.verbose = verbose ;
    }
    
    public void beginRead()
    {
        transactional.begin(ReadWrite.READ) ;
        activeDSG = dsg ;
    }

    public void endRead()
    {
        transactional.end() ;
        activeDSG = null ;
    }

    public void beginWrite()
    {
        transactional.begin(ReadWrite.WRITE) ;
        activeDSG = dsg ;
    }

    public void commit()
    {
        transactional.commit() ;
        activeDSG = null ;
    }

    public void abort()
    {
        transactional.abort() ;
        activeDSG = null ;
    }

    public void endWrite()
    {
        if (transactional.isInTransaction())
        {
            Log.warn(this, "Transaction still active in endWriter - aborted") ;
            transactional.abort() ;
        }
        activeDSG = null ;
    }

//    public boolean isInTransaction()
//    { return transactional.isInTransaction() ; }

//    public final void beginRead()
//    {
//        transactional.begin(ReadWrite.READ) ;
//        
//        enter(dsg, lock, Lock.READ) ;               // ????
//        getConcurrencyPolicy(lock).startRead() ;    // ????
//        activeDSG = dsg ;
//    }
//
//    public final void endRead()
//    {
//        transactional.end() ;
//        leave(dsg, lock, Lock.READ) ;               // ????
//        getConcurrencyPolicy(lock).finishRead() ;   // ????
//        activeDSG = null ;
//    }
//
//    public final void beginWrite()
//    {
//        if ( transactional != null )
//            transactional.begin(ReadWrite.WRITE) ;
//        else
//        {
//            enter(dsg, lock, Lock.WRITE) ;
//            getConcurrencyPolicy(lock).startUpdate() ;
//        }
//        activeDSG = dsg ;
//    }
//
//    public final void endWrite()
//    {
//        if ( transactional != null )
//        {
//            //XXX Wrong - what about abort?
//            transactional.commit() ;
//        }
//        else
//        {
//            sync() ;
//            getConcurrencyPolicy(lock).finishUpdate() ;
//            leave(dsg, lock, Lock.WRITE) ;
//        }
//        activeDSG = null ;
//    }

    public final DatasetGraph getActiveDSG()
    {
        return activeDSG ;
    }
    
    // External, additional lock.
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
