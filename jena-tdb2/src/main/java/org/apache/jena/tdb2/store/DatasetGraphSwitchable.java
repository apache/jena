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

    /**
     * The dataset to use for redirection.
     * <p>
     * Outside a transaction this is the current database. Inside a transaction it is the
     * database the transaction began on, even if the container has been switched since.
     * It is also guaranteed that this is called only once per delegated call.
     * Changes to the wrapped object can be made based on that contract.
     */
    @Override
    public DatasetGraph get() {
        DatasetGraph dsg = pinned();
        return dsg != null ? dsg : dsgx.get();
    }

    /**
     * Transactions on this thread run against the database that was current when the
     * transaction began. {@link #set} and {@link #change} (TDB2 compaction) can switch the
     * container while a transaction is active. Without this, the delegated
     * {@code commit}/{@code abort}/{@code end} would go to the <em>new</em> database, so the
     * transaction on the old one would never finish and compaction, waiting for the old
     * database to become idle, would never complete.
     */
    private final ThreadLocal<DatasetGraph> txnDsg = new ThreadLocal<>();

    /** The database of this thread's active transaction, or null if there is none. */
    private DatasetGraph pinned() {
        DatasetGraph dsg = txnDsg.get();
        if ( dsg == null )
            return null;
        if ( dsg.isInTransaction() )
            return dsg;
        // The transaction has finished (commit/abort without end()): forget it.
        txnDsg.remove();
        return null;
    }

    @Override
    protected void beginTransaction(TxnType txnType) {
        // Pin at the point the transaction is actually started, after admission control
        // (for example, after a writer held up while compaction copies and switches the database).
        DatasetGraph dsg = dsgx.get();
        dsg.begin(txnType);
        txnDsg.set(dsg);
    }

    @Override
    public void commit() {
        super.commit();
        // The transaction is complete. If commit fails, the transaction is still to be ended
        // or aborted and that must go to the same database, so the pin stays.
        txnDsg.remove();
    }

    @Override
    public void abort() {
        super.abort();
        // The transaction is complete. If abort fails, a subsequent end() must still go to
        // the same database, so the pin stays.
        txnDsg.remove();
    }

    @Override
    public void end() {
        try {
            super.end();
        } finally {
            txnDsg.remove();
        }
    }

    /** Is this {@code DatasetGraphSwitchable} just a holder for a {@code DatasetGraph}?
     *  If so, it does not have a location on disk.
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
