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

import static java.lang.String.format;
import static org.apache.jena.fuseki.server.DataServiceStatus.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap;
import org.apache.jena.ext.com.google.common.collect.ListMultimap;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.query.TxnType;
import org.apache.jena.query.text.DatasetGraphText;
import org.apache.jena.sparql.core.DatasetGraph;

public class DataService {
    private DatasetGraph dataset;

    private ListMultimap<Operation, Endpoint> operations  = ArrayListMultimap.create();
    private Map<String, Endpoint> endpoints               = new HashMap<>();
    private AuthPolicy authPolicy                         = null;

    /**
     * Record which {@link DataAccessPoint DataAccessPoints} this {@code DataService} is
     * associated with. This is mainly for checking and development.
     * Usually, one {@code DataService} is associated with one {@link DataAccessPoint}.
     */
    private List<DataAccessPoint> dataAccessPoints      = new ArrayList<>(1);

    private volatile DataServiceStatus state            = UNINITIALIZED;

    // DataService-level counters.
    private final CounterSet    counters                = new CounterSet();
    private final AtomicBoolean offlineInProgress       = new AtomicBoolean(false);
    private final AtomicBoolean acceptingRequests       = new AtomicBoolean(true);

    /** Create a {@code DataService} for the given dataset. */
    public DataService(DatasetGraph dataset) {
        this.dataset = dataset;
        counters.add(CounterName.Requests);
        counters.add(CounterName.RequestsGood);
        counters.add(CounterName.RequestsBad);
        // Start ACTIVE. Registration controls visibility. 
        goActive();
    }

    /**
     * Create a {@code DataService} that has the same dataset, same operations and
     * endpoints as another {@code DataService}. Counters are not copied, not
     * DataAccessPoint associations.
     */
    private DataService(int dummy, DataService other) {
        // Copy non-counter state of 'other'.
        this.dataset = other.dataset;
        this.operations = ArrayListMultimap.create(other.operations);
        this.endpoints = new HashMap<>(other.endpoints);
        this.state = UNINITIALIZED;
    }

    /*package*/ void noteDataAccessPoint(DataAccessPoint dap) {
        this.dataAccessPoints.add(dap);
    }
    
    private String label() {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        dataAccessPoints.stream()
            .map(DataAccessPoint::getName)
            .filter(x->!x.isEmpty())
            .forEach(sj::add);
        return sj.toString();
    }
    
    public DatasetGraph getDataset() {
        return dataset; 
    }
    
    public void addEndpoint(Operation operation, String endpointName) {
        addEndpoint(operation, endpointName, null);
    }
    
    public void addEndpoint(Operation operation, String endpointName, AuthPolicy authPolicy) {
        Endpoint endpoint = new Endpoint(operation, endpointName, authPolicy);
        endpoints.put(endpointName, endpoint);
        operations.put(operation, endpoint);
    }
    
    public Endpoint getEndpoint(String endpointName) {
        return endpoints.get(endpointName);
    }

    public List<Endpoint> getEndpoints(Operation operation) {
        List<Endpoint> x = operations.get(operation);
        if ( x == null )
            x = Collections.emptyList();
        return x;  
    }

    /** Return the operations available here.
     *  @see #getEndpoints(Operation) to get the endpoint list
     */
    public Collection<Operation> getOperations() {
        return operations.keySet();
    }

    //@Override
    public boolean allowUpdate()    { return true; }

    public void goOffline() {
        offlineInProgress.set(true);
        acceptingRequests.set(false);
        state = OFFLINE;
    }

    public void goActive() {
        offlineInProgress.set(false);
        acceptingRequests.set(true);
        state = ACTIVE;
    }

    // Due to concurrency, call isAcceptingRequests().
//    public boolean isActive() {
//        return state != DatasetStatus.ACTIVE;
//    }

    public boolean isAcceptingRequests() {
        return acceptingRequests.get();
    }
    
    //@Override
    public  CounterSet getCounters() { return counters; }
    
    //@Override 
    public long getRequests() { 
        return counters.value(CounterName.Requests);
    }

    //@Override
    public long getRequestsGood() {
        return counters.value(CounterName.RequestsGood);
    }
    //@Override
    public long getRequestsBad() {
        return counters.value(CounterName.RequestsBad);
    }

    /** Counter of active transactions */
    public AtomicLong   activeTxn           = new AtomicLong(0);

    /** Cumulative counter of transactions */
    public AtomicLong   totalTxn            = new AtomicLong(0);

    public void startTxn(TxnType mode) {
        check(DataServiceStatus.ACTIVE);
        activeTxn.getAndIncrement();
        totalTxn.getAndIncrement();
    }

    private void check(DataServiceStatus status) {
        if ( state != status ) {
            String msg = format("DataService %s: Expected=%s, Actual=%s", label(), status, state);
            throw new FusekiException(msg);
        }
    }

    public void finishTxn() {
        activeTxn.decrementAndGet();
    }

    /** Shutdown and never use again. */
    public synchronized void shutdown() {
        if ( state == CLOSING )
            return;
        Fuseki.serverLog.info(format("Shutting down data service for %s", endpoints.keySet()));
        expel(dataset);
        dataset = null; 
        state = CLOSED;
    }
    
    private void expel(DatasetGraph database) {
        // Text databases.
        // Close the in-JVM objects for Lucene index and databases. 
        if ( database instanceof DatasetGraphText ) {
            DatasetGraphText dbtext = (DatasetGraphText)database;
            database = dbtext.getBase();
            dbtext.getTextIndex().close();
        }
    
        boolean isTDB1 = org.apache.jena.tdb.sys.TDBInternal.isTDB1(database);
        boolean isTDB2 = org.apache.jena.tdb2.sys.TDBInternal.isTDB2(database);
        
        if ( ( isTDB1 || isTDB2 ) ) {
            // JENA-1586: Remove database from the process.
            if ( isTDB1 )
                org.apache.jena.tdb.sys.TDBInternal.expel(database);
            if ( isTDB2 )
                org.apache.jena.tdb2.sys.TDBInternal.expel(database);
        } else
            dataset.close();
    }

    public void setAuthPolicy(AuthPolicy authPolicy) { this.authPolicy = authPolicy; }
    
    /** Returning null implies no authorization control */
    public AuthPolicy authPolicy() { return authPolicy; }

}

