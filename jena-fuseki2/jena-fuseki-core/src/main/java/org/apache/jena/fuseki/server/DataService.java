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
import static org.apache.jena.tdb.sys.TDBInternal.isTDB1;
import static org.apache.jena.tdb2.sys.TDBInternal.isTDB2;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap;
import org.apache.jena.ext.com.google.common.collect.ListMultimap;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.fuseki.build.FusekiConfig;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;

public class DataService {
    // Not final - it is null'ed if closed to release the dataset state.
    private DatasetGraph dataset;

    private final Map<String, EndpointSet> endpoints;
    private final ListMultimap<Operation, Endpoint> operationsMap;

    // Dataset-level authorization policy.
    private final AuthPolicy authPolicy;

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

    /** Builder for a new DataService. */
    public static Builder newBuilder() { return new Builder(); }

    /** Builder for a new DataService, with a dataset. */
    public static Builder newBuilder(DatasetGraph dsg) {
        return newBuilder().dataset(dsg);
    }

    /** Return a new builder, populated by an existing DatasetService */
    public static Builder newBuilder(DataService dSrv) {
        return new Builder(dSrv.dataset, dSrv.endpoints, dSrv.operationsMap, dSrv.authPolicy);
    }

    /** Create a {@code DataService} for the given dataset. */
    private DataService(DatasetGraph dataset, Map<String, EndpointSet> endpoints, ListMultimap<Operation, Endpoint> operationsMap, AuthPolicy authPolicy) {
        this.dataset = dataset;
        this.endpoints = Map.copyOf(endpoints);
        this.operationsMap = ArrayListMultimap.create(operationsMap);
        this.authPolicy = authPolicy;
        counters.add(CounterName.Requests);
        counters.add(CounterName.RequestsGood);
        counters.add(CounterName.RequestsBad);
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

    /** Return the {@linkplain EndpointSet} for the operations for named use. */
    public EndpointSet getEndpointSet(String endpointName) {
        return endpoints.get(endpointName);
    }

    /** Return the {@linkplain EndpointSet} for the operations for unnamed use. */
    public EndpointSet getEndpointSet() {
        return endpoints.get("");
    }

    /** Return a collection of all endpoints for this {@linkplain DataService}. */
    public Collection<Endpoint> getEndpoints() {
        // A copy
        Set<Endpoint> x = new HashSet<>();
        endpoints.forEach((k,eps)->{
            eps.forEach((op,ep)->x.add(ep));
        });
        return x;
    }

    /** Execute an action for each {@link Endpoint}. */
    public void forEachEndpoint(Consumer<Endpoint> action) {
        endpoints.forEach((k,eps)->{
            eps.forEach((op,ep)->action.accept(ep));
        });
    }

    public List<Endpoint> getEndpoints(Operation operation) {
        List<Endpoint> x = operationsMap.get(operation);
        return x;
    }

    /**
     * Return the operations available here.
     * @see #getEndpoints(Operation) to get the endpoint list
     */
    public Collection<Operation> getOperations() {
        return operationsMap.keySet();
    }

    /**
     * Return the operations available here.
     * @see #getEndpoints(Operation) to get the endpoint list
     */
    public boolean hasOperation(Operation operation) {
        return operationsMap.keySet().contains(operation);
    }

    public boolean allowUpdate()    { return true; }

    public void goOffline() {
        offlineInProgress.set(true);
        acceptingRequests.set(false);
        state = OFFLINE;
    }

    /** Set any {@link ActionService} processors that are currently unset. */
    public void setEndpointProcessors(OperationRegistry operationRegistry) {
        // Make sure the processor is set for each endpoint.
        forEachEndpoint(ep->{
            if ( ep.getProcessor() == null )
                ep.setProcessor(operationRegistry.findHandler(ep.getOperation()));
        });
    }

    private void ensureEnpointProcessors() {
        forEachEndpoint(ep->{
            if ( ep.getProcessor() == null ) {
                String x = NodeFmtLib.strNT(ep.getOperation().getId());
                Fuseki.configLog.warn("No processor for operation "+x);
            }
        });
    }

    public void goActive() {
        ensureEnpointProcessors();
        offlineInProgress.set(false);
        acceptingRequests.set(true);
        state = ACTIVE;
    }

    public boolean isAcceptingRequests() {
        return acceptingRequests.get();
    }

    public  CounterSet getCounters() { return counters; }

    public long getRequests() {
        return counters.value(CounterName.Requests);
    }

    public long getRequestsGood() {
        return counters.value(CounterName.RequestsGood);
    }
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
        expel(dataset);
        dataset = null;
        state = CLOSED;
    }

    private static void expel(DatasetGraph database) {
        // This should not be necessary.
        // When created by assembler, "closeIndexOnClose" should be set true.
        // so this happen automatically (otherwise we need either reflection
        // or make jena-text a dependency).
//        // Close the in-JVM objects for Lucene index and databases.
//        if ( database instanceof DatasetGraphText ) {
//            DatasetGraphText dbtext = (DatasetGraphText)database;
//            database = dbtext.getBase();
//            dbtext.getTextIndex().close();
//        }

        // Find possible TDB1, TDB2.
        DatasetGraph base = findTDB(database);
        database.close();

        boolean isTDB1 = isTDB1(base);
        boolean isTDB2 = isTDB2(base);

        if ( isTDB1 || isTDB2 ) {
            // JENA-1586: Remove database from the process.
            if ( isTDB1 )
                org.apache.jena.tdb.sys.TDBInternal.expel(base);
            if ( isTDB2 )
                org.apache.jena.tdb2.sys.TDBInternal.expel(base);
        }
    }

    /** Unwrap until a TDB database is encountered */
    private static DatasetGraph findTDB(DatasetGraph dsg) {
        DatasetGraph dsgw = dsg;
        while (dsgw instanceof DatasetGraphWrapper) {
            if ( isTDB1(dsgw) )
                return dsgw;
            if ( isTDB2(dsgw) )
                return dsgw;
            dsgw = ((DatasetGraphWrapper)dsgw).getWrapped();
        }
        return dsgw;
    }

    /** Returning null implies no authorization control */
    public AuthPolicy authPolicy() { return authPolicy; }

    public static class Builder {
        private DatasetGraph dataset = null;

        private Map<String, EndpointSet> endpoints              = new HashMap<>();
        private ListMultimap<Operation, Endpoint> operationsMap = ArrayListMultimap.create();

        // Dataset-level authorization policy.
        private AuthPolicy authPolicy = null;

        private Builder() {}

        private Builder(DatasetGraph dataset, Map<String, EndpointSet> endpoints, ListMultimap<Operation, Endpoint> operationsMap,AuthPolicy authPolicy) {
            this();
            this.dataset = dataset;
            this.endpoints.putAll(endpoints);
            this.operationsMap.putAll(operationsMap);
            this.authPolicy = authPolicy;
        }

        public Builder dataset(DatasetGraph dsg) { this.dataset = dsg; return this; }
        public DatasetGraph dataset()            { return this.dataset; }

        public Builder withStdServices(boolean withUpdate) {
            FusekiConfig.populateStdServices(this, withUpdate);
            return this;
        }

        // For now, don't provide ...
        //        public DatasetGraph dataset() { return this.dataset; }
        //
        //        public AuthPolicy authPolicy() { return this.authPolicy; }

        public Builder addEndpoint(Operation operation) {
            return addEndpoint(operation, null, null);
        }

        public Builder addEndpoint(Operation operation, AuthPolicy authPolicy) {
            return addEndpoint(operation, null, authPolicy);
        }

        public Builder addEndpoint(Operation operation, String endpointName) {
            return addEndpoint(operation, endpointName, null);
        }

        public Builder addEndpoint(Operation operation, String endpointName, AuthPolicy authPolicy) {
            Endpoint endpoint = Endpoint.create(operation, endpointName, authPolicy);
            return addEndpoint(endpoint);
        }

        public Builder addEndpoint(Endpoint endpoint) {
            return addEndpoint$(endpoint);
        }

        private Builder addEndpoint$(Endpoint endpoint) {
            EndpointSet eps = endpoints.computeIfAbsent(endpoint.getName(), (k)->new EndpointSet(k));
            eps.put(endpoint);
            // Cleaner not to have duplicates. But nice to have a (short) list that keeps the create order.
            if ( ! operationsMap.containsEntry(endpoint.getOperation(), endpoint) )
                operationsMap.put(endpoint.getOperation(), endpoint);
            return this;
        }

        private void removeEndpoint$(Endpoint endpoint) {
            EndpointSet eps = endpoints.get(endpoint.getName());
            if ( eps == null )
                return;
            eps.remove(endpoint);
            operationsMap.remove(endpoint.getOperation(), endpoint);
        }

        public Builder setAuthPolicy(AuthPolicy authPolicy) {
            this.authPolicy = authPolicy;
            return this;
        }

        public DataService build() {
            return new DataService(dataset, endpoints, operationsMap, authPolicy);
        }
    }
}

