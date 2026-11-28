/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.tdb2.store;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.storage.system.DatasetGraphTxnCtl;
import org.apache.jena.dboe.transaction.txn.TransactionalSystemControl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

final
public class DatasetGraphSwitchable extends DatasetGraphTxnCtl
{
    private final AtomicReference<DatasetGraph> dsgx = new AtomicReference<>();
    // Null for in-memory datasets.
    private final Path basePath;
    private final Location location;
    private final PrefixMapSwitchable prefixes;

    public DatasetGraphSwitchable(Path base, Location location, DatasetGraph dsg) {
        // Don't use the slot in DatasetGraphWrapper - use the AtomicReference
        super(null, dsg.getContext());
        dsgx.set(dsg);
        this.basePath = base;
        this.location = location;
        this.prefixes = new PrefixMapSwitchable(this);
    }

    ThreadLocal<DatasetGraph> txnDataset = ThreadLocal.withInitial(()->null);

    /**
     * The dataset to use for redirection - overrides DatasetGraphWrapper.get().
     * <p>
     * The dataset for a transaction is kept in a {@link ThreadLocal} so that even if
     * a {@code #change} or {@link #set} occurs, operations, especially transaction
     * operations go the original dataset.
     * </p>
     * <p>
     * Beware - the code using {@code DatasetGraphSwitchable} is responsible for
     * managing a switchover. For example, in TDB2, compact only calls
     * {@link #change} on a read-only database, i.e. there are only read transactions
     * active using {@link TransactionalSystemControl#execReadOnlyDatabase}
     * </p>
     * <p>
     * Other use cases, such as reloading Fuseki configuration, might switchover in
     * any transaction state and the reload code would need control cases where
     * certain configurations need addition pre-swithcover setup.
     * </p>
     */
    @Override
    public DatasetGraph get() {
        // Touching a ThreadLocal initializes it and creates a ThreadLocalMap entry.
        DatasetGraph txnDSG = txnDataset.get();
        if ( txnDSG != null )
            return txnDSG;
        // ThreadLocalMap attempts to recover slots if it needs to grow the table.
        // AFS understanding is that this means a slot set to null will be released
        // by slot recovery and not by eventual pressure on the heap.
        // Calling txnDataset.remove() causes a ThreadLocal access.
        // get() may be called frequently.
//        else
//            txnDataset.remove();
        return dsgx.get();
    }

    /**
     * Is this {@code DatasetGraphSwitchable} just a holder for a {@code DatasetGraph}?
     * If so, it does not have a location on disk.
     */
    public boolean hasContainerPath() { return basePath != null; }

    public Path getContainerPath() { return basePath; }

    public Location getLocation() { return location; }

    /**
     * Set the base {@link DatasetGraph}.
     * Returns the old value.
     */
    public DatasetGraph set(DatasetGraph dsg) {
        return dsgx.getAndSet(dsg);
    }

    @Override
    public PrefixMap prefixes() {
        return prefixes;
    }

    @Override
    protected void notifyEnterTransaction(TxnType txnType, boolean canBlock) {
        txnDataset.set(dsgx.get());
    }

    @Override
    protected void notifyExitTransaction() {
        txnDataset.remove();
    }

    /**
     * Don't do anything on close.
     * This would not be safe across switches.
     */
    @Override
    public void close() {}

//    /** Don't do anything on sync.Transactions only. */
//    @Override
//    public void sync() { }

    /**
     * If and only if the current value is the given old value, set the base {@link DatasetGraph}
     * Returns true if a swap happened.
     */
    public boolean change(DatasetGraph oldDSG, DatasetGraph newDSG) {
        // No need to clear. ngCache.clear();
        return dsgx.compareAndSet(oldDSG, newDSG);
    }

    private Graph dftGraph = GraphViewSwitchable.createDefaultGraphSwitchable(this);

    @Override
    public Graph getDefaultGraph() {
        return dftGraph;
    }

    @Override
    public Graph getUnionGraph() {
        return GraphViewSwitchable.createUnionGraphSwitchable(this);
    }

//    private Cache<Node, Graph> ngCache = CacheFactory.createCache(10);
    private Cache<Node, Graph> ngCache = CacheFactory.createOneSlotCache();

    @Override
    public Graph getGraph(Node gn) {
        Node key = ( gn != null ) ? gn : Quad.defaultGraphNodeGenerated;
        return ngCache.get(key, (k)->GraphViewSwitchable.createNamedGraphSwitchable(this, k));
    }
}
