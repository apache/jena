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

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.fuseki.DEF ;
import org.apache.jena.fuseki.conneg.ConNeg ;
import org.apache.jena.fuseki.server.DatasetRef ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphWithLock ;
import com.hp.hpl.jena.sparql.core.Transactional ;

public class HttpAction
{
    public final long id ;
    private DatasetGraph dsg ;                  // The data
    private final Transactional transactional ;
    private DatasetRef desc ;
    private DatasetGraph  activeDSG ;           // Set when inside begin/end.
    
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

    public HttpAction(long id, DatasetRef desc, HttpServletRequest request, HttpServletResponse response, boolean verbose)
    {
        this.id = id ;
        this.desc = desc ;
        this.dsg = desc.dataset ;

        if ( dsg instanceof Transactional )
            transactional = (Transactional)dsg ;
        else
        {
            // Non-trsanctional - wrap in something that does locking to give the same 
            // functionality in the absense of errors, with less concurrency.
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
            Log.warn(this, "Transaction still active in endWriter - no commit or abort seen (forced abort)") ;
            try { transactional.abort() ; } 
            catch (RuntimeException ex) { Log.warn(this, "Exception in forced abort (trying to continue)", ex) ;} 
        }
        transactional.end() ;
        activeDSG = null ;
    }

    public final DatasetGraph getActiveDSG()
    {
        return activeDSG ;
    }
    
    public final DatasetRef getDatasetRef()
    {
        return desc ;
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
    
    public static MediaType contentNegotationRDF(HttpAction action)
    {
        MediaType mt = ConNeg.chooseContentType(action.request, DEF.rdfOffer, DEF.acceptRDFXML) ;
        if ( mt == null )
            return null ;
        if ( mt.getContentType() != null )
            action.response.setContentType(mt.getContentType());
        if ( mt.getCharset() != null )
        action.response.setCharacterEncoding(mt.getCharset()) ;
        return mt ;
    }
    
    public static MediaType contentNegotationQuads(HttpAction action)
    {
        return ConNeg.chooseContentType(action.request, DEF.quadsOffer, DEF.acceptNQuads) ;
    }

}
