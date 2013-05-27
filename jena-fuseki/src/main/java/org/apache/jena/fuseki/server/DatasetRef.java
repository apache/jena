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

import java.util.concurrent.atomic.AtomicLong ;

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
    
//    public List<String> queryEP                 = new ArrayList<String>() ;
//    public List<String> updateEP                = new ArrayList<String>() ;
//    public List<String> uploadEP                = new ArrayList<String>() ;
//    public List<String> readGraphStoreEP        = new ArrayList<String>() ;
//    public List<String> readWriteGraphStoreEP   = new ArrayList<String>() ;
 
    /** Counter of active read transactions */
    public AtomicLong   activeReadTxn           = new AtomicLong(0) ;
    
    /** Counter of active write transactions */
    public AtomicLong   activeWriteTxn          = new AtomicLong(0) ;

    /** Cumulative counter of read transactions */
    public AtomicLong   totalReadTxn            = new AtomicLong(0) ;

    /** Cumulative counter of writer transactions */
    public AtomicLong   totalWriteTxn           = new AtomicLong(0) ;
    
    /** Count of requests received - any service */
    public AtomicLong   countServiceRequests    = new AtomicLong(0) ;
    /** Count of requests received that fail in some way */
    public AtomicLong   countServiceRequestsBad = new AtomicLong(0) ;
    /** Count of requests received that fail in some way */
    public AtomicLong   countServiceRequestsOK  = new AtomicLong(0) ;

    // SPARQL Query
    
    /** Count of SPARQL Queries successfully executed */
    public AtomicLong   countQueryOK            = new AtomicLong(0) ;
    /** Count of SPARQL Queries with syntax errors */
    public AtomicLong   countQueryBadSyntax     = new AtomicLong(0) ;
    /** Count of SPARQL Queries with timeout on execution */
    public AtomicLong   countQueryTimeout       = new AtomicLong(0) ;
    /** Count of SPARQL Queries with execution errors (not timeouts) */
    public AtomicLong   countQueryBadExecution  = new AtomicLong(0) ;

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
