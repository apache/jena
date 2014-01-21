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

import static org.apache.jena.fuseki.server.DatasetStatus.* ;

import java.util.* ;
import java.util.concurrent.atomic.AtomicLong ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiException ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;

public class DatasetRef implements DatasetMXBean, Counters
{
    // Split into build and runtime thing?
    
    public final String name ;
    
    // EITHER a link OR a dataset (if offline?)
    // In-use?
    private DatasetGraph dataset                = null ;
    private DatasetRef   link                   = null ;

    public ServiceRef query                     = new ServiceRef("query") ;
    public ServiceRef update                    = new ServiceRef("update") ;
    public ServiceRef upload                    = new ServiceRef("upload") ;
    public ServiceRef readGraphStore            = new ServiceRef("gspRead") ;
    public ServiceRef readWriteGraphStore       = new ServiceRef("gspReadWrite") ; 
    
    // Dataset-level counters.
    private final CounterSet counters           = new CounterSet() ;
    @Override
    public  CounterSet getCounters() { return counters ; }
    
    private Map<String, ServiceRef> endpoints   = new HashMap<String, ServiceRef>() ;
    private List<ServiceRef> serviceRefs        = new ArrayList<ServiceRef>() ;
    private volatile DatasetStatus state = UNINITIALIZED ;
    
    public static DatasetRef nullRef() {
        return new DatasetRef(null, null, null ) ; 
    }

    public static DatasetRef create(String name, DatasetGraph dataset) {
        return new DatasetRef(name, dataset, null ) ; 
    }

    public static DatasetRef createLink(String name, DatasetRef other) {
        return new DatasetRef(name, null, other) ; 
    }

    private DatasetRef(String name, DatasetGraph dsg, DatasetRef link)
    { 
        this.name = DatasetRef.canonicalDatasetPath(name) ;
        this.dataset = dsg ;
        this.link = link ;
    }
    
    public boolean isActive() { return getState() == ACTIVE ; }
    
    /** Follow links */
    public DatasetRef resolve() {
        // Expect chain to be one, or at most two elements.
        List<DatasetRef> x = new ArrayList<DatasetRef>() ;
        DatasetRef here = this ;
        x.add(here) ;
        while ( here.link != null ) {
            DatasetRef next = here.link ;
            if ( x.contains(next) )
                throw new FusekiException("Loop in DatasetRef links from: "+this.name) ; 
            here = here.link ;
        }
            
        return here ;
    }
     
    
    public DatasetStatus getStatus()                    { return state ; }
    public void setStatus(DatasetStatus newStatus)      { setState(newStatus) ; }
    
    private DatasetStatus getState() {
        return state ;
    }

    private void setState(DatasetStatus newState) {
        state = newState ;
    }

    public void enable() {
        if ( getState() == UNINITIALIZED )
            setState(ACTIVE) ;
        if ( serviceRefs.isEmpty() )
            initServices() ;    
    }
    
    public void activate() {
        if ( getState() == ACTIVE )
            Fuseki.serverLog.warn("Already active: dataset = "+name) ;
        if ( getState() == UNINITIALIZED )
            initServices() ;    
        setState(ACTIVE) ;
    }
    
    public void offline() {
        if ( getState() == OFFLINE ) {
            Fuseki.serverLog.warn("Already offline: dataset = "+name) ;
            return ;
        }
        if ( getState() == UNINITIALIZED )
            initServices() ;    
        setState(OFFLINE) ;
        
        if ( dataset instanceof DatasetGraphTransaction )
            StoreConnection.release( ((DatasetGraphTransaction)dataset).getLocation() ) ;
        else 
            dataset.close() ;
    }

    @Override public String toString() { return "DatasetRef:'"+name+"'" ; }  
    
    private void initServices() {
        add(query) ;
        add(update) ;
        add(upload) ;
        add(readGraphStore) ;
        add(readWriteGraphStore) ;
        addCounters() ;
    }
    
    private void add(ServiceRef srvRef) {
        serviceRefs.add(srvRef) ;
        for ( String ep : srvRef.endpoints )
            endpoints.put(ep, srvRef) ;
    }

    /** Get the dataset - this follows links */ 
    public DatasetGraph getDataset() {
        return dataset ;
    }

    public ServiceRef getServiceRef(String service) {
        if ( ! isActive() )
            Fuseki.serverLog.error("Not active: dataset = "+name) ;
        if ( service.startsWith("/") )
            service = service.substring(1, service.length()) ; 
        return endpoints.get(service) ;
    }

    public Collection<ServiceRef> getServiceRefs() {
        return serviceRefs ;
    }

    /** Counter of active read transactions */
    public AtomicLong   activeReadTxn           = new AtomicLong(0) ;
    
    /** Counter of active write transactions */
    public AtomicLong   activeWriteTxn          = new AtomicLong(0) ;

    /** Cumulative counter of read transactions */
    public AtomicLong   totalReadTxn            = new AtomicLong(0) ;

    /** Cumulative counter of writer transactions */
    public AtomicLong   totalWriteTxn           = new AtomicLong(0) ;
    
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
        checkShutdown() ;
    }

    private void checkShutdown() {
        if ( getState() == CLOSING ) {
            if ( activeReadTxn.get() == 0 && activeWriteTxn.get() == 0 )
                shutdown() ;
        }
    }
    
    private void shutdown() {
        dataset.close() ;
        if ( dataset instanceof DatasetGraphTransaction ) {
            DatasetGraphTransaction dsgtxn = (DatasetGraphTransaction)dataset ;
            StoreConnection.release(dsgtxn.getLocation()) ;
        }
        setState(OFFLINE) ;
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
    
    public void gracefulShutdown() {
        // In graceful shutdown, we assume it has been unlinked from the registry.
        // We just wait for the transactions to end.
        setState(CLOSING) ;
        checkShutdown() ;
    }
    
    @Override
    public String getName()     { return name ; }

    @Override public long getRequests() { 
        return counters.value(CounterName.Requests) ;
    }

    @Override
    public long getRequestsGood() {
        return counters.value(CounterName.RequestsGood) ;
    }
    @Override
    public long getRequestsBad() {
        return counters.value(CounterName.RequestsBad) ;
    }
    
    private void addCounters() {
        getCounters().add(CounterName.Requests) ;
        getCounters().add(CounterName.RequestsGood) ;
        getCounters().add(CounterName.RequestsBad) ;

        query.getCounters().add(CounterName.Requests) ;
        query.getCounters().add(CounterName.RequestsGood) ;
        query.getCounters().add(CounterName.RequestsBad) ;
        query.getCounters().add(CounterName.QueryTimeouts) ;
        query.getCounters().add(CounterName.QueryExecErrors) ;

        update.getCounters().add(CounterName.Requests) ;
        update.getCounters().add(CounterName.RequestsGood) ;
        update.getCounters().add(CounterName.RequestsBad) ;
        update.getCounters().add(CounterName.UpdateExecErrors) ;

        upload.getCounters().add(CounterName.Requests) ;
        upload.getCounters().add(CounterName.RequestsGood) ;
        upload.getCounters().add(CounterName.RequestsBad) ;

        addCountersForGSP(readWriteGraphStore.getCounters(), false) ;
        if ( readGraphStore != readWriteGraphStore )
            addCountersForGSP(readGraphStore.getCounters(), true) ;
    }

    private void addCountersForGSP(CounterSet cs, boolean readWrite) {
        cs.add(CounterName.Requests) ;
        cs.add(CounterName.RequestsGood) ;
        cs.add(CounterName.RequestsBad) ;

        cs.add(CounterName.GSPget) ;
        cs.add(CounterName.GSPgetGood) ;
        cs.add(CounterName.GSPgetBad) ;

        cs.add(CounterName.GSPhead) ;
        cs.add(CounterName.GSPheadGood) ;
        cs.add(CounterName.GSPheadBad) ;

        // Add anyway.
        // if ( ! readWrite )
        // return ;

        cs.add(CounterName.GSPput) ;
        cs.add(CounterName.GSPputGood) ;
        cs.add(CounterName.GSPputBad) ;

        cs.add(CounterName.GSPpost) ;
        cs.add(CounterName.GSPpostGood) ;
        cs.add(CounterName.GSPpostBad) ;

        cs.add(CounterName.GSPdelete) ;
        cs.add(CounterName.GSPdeleteGood) ;
        cs.add(CounterName.GSPdeleteBad) ;

        cs.add(CounterName.GSPpatch) ;
        cs.add(CounterName.GSPpatchGood) ;
        cs.add(CounterName.GSPpatchBad) ;

        cs.add(CounterName.GSPoptions) ;
        cs.add(CounterName.GSPoptionsGood) ;
        cs.add(CounterName.GSPoptionsBad) ;
    }
    
    public static String canonicalDatasetPath(String datasetPath) {
        if ( datasetPath == null )
            return datasetPath ;
        if ( datasetPath.equals("/") )
            datasetPath = "" ;
        else
            if ( !datasetPath.startsWith("/") )
                datasetPath = "/" + datasetPath ;
        if ( datasetPath.endsWith("/") )
            datasetPath = datasetPath.substring(0, datasetPath.length() - 1) ;
        return datasetPath ;
    }
}
