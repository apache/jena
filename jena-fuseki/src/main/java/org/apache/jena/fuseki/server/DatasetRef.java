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

package org.apache.jena.fuseki.server;

import java.util.HashMap ;
import java.util.Map ;
import java.util.concurrent.atomic.AtomicLong ;

import org.apache.jena.fuseki.Fuseki ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

public class DatasetRef
{
    public String name                          = null ;
    public DatasetGraph dataset                 = null ;

    public ServiceRef query                     = new ServiceRef("query") ;
    public ServiceRef update                    = new ServiceRef("update") ;
    public ServiceRef upload                    = new ServiceRef("upload") ;
    public ServiceRef readGraphStore            = new ServiceRef("gspRead") ;
    public ServiceRef readWriteGraphStore       = new ServiceRef("gspReadWrite") ; 

    // Dataset-level counters.
    public final CounterSet counters            = new CounterSet() ;
    private Map<String, ServiceRef> serviceRefs = new HashMap<String, ServiceRef>() ;
    private boolean initialized = false ;
    
    // Two step initiation (c.f. Builder pattern)
    // Create object - incrementally set state - call init to calculate internal datastructures.
    public DatasetRef() {}
    public void init() {
        if ( initialized )
            Fuseki.serverLog.warn("Already initialized: dataset = "+name) ;
        initialized = true ;
        initServices() ;
    }
    
    private void initServices() {
        add(serviceRefs, query) ;
        add(serviceRefs, update) ;
        add(serviceRefs, upload) ;
        add(serviceRefs, readGraphStore) ;
        add(serviceRefs, readWriteGraphStore) ;
    }
    
    private static void add(Map<String, ServiceRef> serviceRefs, ServiceRef srvRef)
    {
        for ( String ep : srvRef.endpoints )
            serviceRefs.put(ep, srvRef) ; 
    }

    public ServiceRef getServiceRef(String service) {
        if ( ! initialized )
            Fuseki.serverLog.error("Not initialized: dataset = "+name) ;
        if ( service.startsWith("/") )
            service = service.substring(1, service.length()) ; 
        return serviceRefs.get(service) ;
    }

    /** Counter of active read transactions */
    public AtomicLong   activeReadTxn           = new AtomicLong(0) ;
    
    /** Counter of active write transactions */
    public AtomicLong   activeWriteTxn          = new AtomicLong(0) ;

    /** Cumulative counter of read transactions */
    public AtomicLong   totalReadTxn            = new AtomicLong(0) ;

    /** Cumulative counter of writer transactions */
    public AtomicLong   totalWriteTxn           = new AtomicLong(0) ;
    
//    /** Count of requests received - anyzservice */
//    public AtomicLong   countServiceRequests    = new AtomicLong(0) ;
//    /** Count of requests received that fail in some way */
//    public AtomicLong   countServiceRequestsBad = new AtomicLong(0) ;
//    /** Count of requests received that fail in some way */
//    public AtomicLong   countServiceRequestsOK  = new AtomicLong(0) ;
//
//    // SPARQL Query
//    
//    /** Count of SPARQL Queries successfully executed */
//    public AtomicLong   countQueryOK            = new AtomicLong(0) ;
//    /** Count of SPARQL Queries with syntax errors */
//    public AtomicLong   countQueryBadSyntax     = new AtomicLong(0) ;
//    /** Count of SPARQL Queries with timeout on execution */
//    public AtomicLong   countQueryTimeout       = new AtomicLong(0) ;
//    /** Count of SPARQL Queries with execution errors (not timeouts) */
//    public AtomicLong   countQueryBadExecution  = new AtomicLong(0) ;

    public void startTxn(ReadWrite mode)
    {
        switch(mode)
        {
            case READ:  
                activeReadTxn.getAndIncrement() ;
                totalReadTxn.getAndIncrement() ;
                break ;
            case WRITE:
                activeWriteTxn.getAndIncrement() ;
                totalWriteTxn.getAndIncrement() ;
                break ;
        }
    }
    
    public void finishTxn(ReadWrite mode)
    {
        switch(mode)
        {
            case READ:  
                activeReadTxn.decrementAndGet() ;
                break ;
            case WRITE:
                activeWriteTxn.decrementAndGet() ;
                break ;
        }
    }

    //TODO Need to be able to set this from the config file.  
    public boolean allowDatasetUpdate           = false;
    
    public boolean allowTimeoutOverride         = false;
    public long maximumTimeoutOverride          = Long.MAX_VALUE;
    
    public boolean isReadOnly()
    {
        return ! allowDatasetUpdate &&
               ! update.isActive() && 
               ! upload.isActive() &&
               ! readWriteGraphStore.isActive()
               ;
    }
}
