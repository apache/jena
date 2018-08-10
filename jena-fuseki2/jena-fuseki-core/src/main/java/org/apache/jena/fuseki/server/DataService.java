/**
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

import static org.apache.jena.fuseki.server.DatasetStatus.CLOSING ;
import static org.apache.jena.fuseki.server.DatasetStatus.UNINITIALIZED ;

import java.util.* ;
import java.util.concurrent.atomic.AtomicBoolean ;
import java.util.concurrent.atomic.AtomicLong ;

import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap;
import org.apache.jena.ext.com.google.common.collect.ListMultimap;
import org.apache.jena.fuseki.DEF ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.DatasetGraphReadOnly ;
import org.apache.jena.tdb.StoreConnection ;
import org.apache.jena.tdb.transaction.DatasetGraphTransaction ;

public class DataService { //implements DatasetMXBean {
    public static DataService serviceOnlyDataService() {
        return dummy ; 
    }
    
    public static final DataService dummy ;
    static {
        DatasetGraph dsg = new DatasetGraphReadOnly(DatasetGraphFactory.create()) ;
        dummy = new DataService(dsg) ;
        dummy.addEndpoint(Operation.Query, DEF.ServiceQuery) ;
        dummy.addEndpoint(Operation.Query, DEF.ServiceQueryAlt) ;
    }
    
    private DatasetGraph dataset ;

    private ListMultimap<Operation, Endpoint> operations    = ArrayListMultimap.create() ;
    private Map<String, Endpoint> endpoints                     = new HashMap<>() ;
    
    private volatile DatasetStatus state = UNINITIALIZED ;

    // DataService-level counters.
    private final CounterSet    counters                = new CounterSet() ;
    private final AtomicLong    requestCounter          = new AtomicLong(0) ;   
    private final AtomicBoolean offlineInProgress       = new AtomicBoolean(false) ;
    private final AtomicBoolean acceptingRequests       = new AtomicBoolean(true) ;

    /** Create a {@code DataService} for the given dataset. */
    public DataService(DatasetGraph dataset) {
        this.dataset = dataset ;
        counters.add(CounterName.Requests) ;
        counters.add(CounterName.RequestsGood) ;
        counters.add(CounterName.RequestsBad) ;
    }

    /**
     * Create a {@code DataService} that has the same dataset, same operations and
     * endpoints as another {@code DataService}. Counters are not copied.
     */
    public DataService(DataService other) {
        // Copy non-counter state of 'other'.
        this.dataset = other.dataset ;
        this.operations = ArrayListMultimap.create(other.operations) ;
        this.endpoints = new HashMap<>(other.endpoints) ;
    }

    
    public DatasetGraph getDataset() {
        return dataset ; 
    }
    
    public void addEndpoint(Operation operation, String endpointName) {
        Endpoint endpoint = new Endpoint(operation, endpointName) ;
        endpoints.put(endpointName, endpoint) ;
        operations.put(operation, endpoint);
    }
    
    public Endpoint getEndpoint(String endpointName) {
        return endpoints.get(endpointName) ;
    }

    public List<Endpoint> getEndpoints(Operation operation) {
        List<Endpoint> x = operations.get(operation) ;
        if ( x == null )
            x = Collections.emptyList() ;
        return x ;  
    }

    /** Return the operations available here.
     *  @see #getEndpoints(Operation) to get the endpoint list
     */
    public Collection<Operation> getOperations() {
        return operations.keySet() ;
    }

    //@Override
    public boolean allowUpdate()    { return true ; }

    public void goOffline() {
        offlineInProgress.set(true);
        acceptingRequests.set(false);
        state = DatasetStatus.OFFLINE;
    }

    public void goActive() {
        offlineInProgress.set(false);
        acceptingRequests.set(true);
        state = DatasetStatus.ACTIVE;
    }

    // Due to concurrency, call isAcceptingRequests().
//    public boolean isActive() {
//        return state != DatasetStatus.ACTIVE;
//    }

    public boolean isAcceptingRequests() {
        return acceptingRequests.get() ;
    }
    
    //@Override
    public  CounterSet getCounters() { return counters ; }
    
    //@Override 
    public long getRequests() { 
        return counters.value(CounterName.Requests) ;
    }

    //@Override
    public long getRequestsGood() {
        return counters.value(CounterName.RequestsGood) ;
    }
    //@Override
    public long getRequestsBad() {
        return counters.value(CounterName.RequestsBad) ;
    }

    /** Counter of active transactions */
    public AtomicLong   activeTxn           = new AtomicLong(0) ;

    /** Cumulative counter of transactions */
    public AtomicLong   totalTxn            = new AtomicLong(0) ;

    public void startTxn(TxnType mode) {
        activeTxn.getAndIncrement();
        totalTxn.getAndIncrement();
    }

    public void finishTxn() {
        activeTxn.decrementAndGet();
    }

    private void checkShutdown() {
        if ( state == CLOSING ) {
            if ( activeTxn.get() == 0 )
                shutdown() ;
        }
    }

    private void shutdown() {
        Fuseki.serverLog.info("Shutting down dataset") ;
        dataset.close() ;
        if ( dataset instanceof DatasetGraphTransaction ) {
            DatasetGraphTransaction dsgtxn = (DatasetGraphTransaction)dataset ;
            StoreConnection.release(dsgtxn.getLocation()) ;
        }
        dataset = null ; 
    }
}

