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

package org.apache.jena.tdb2.store;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.storage.system.DatasetGraphStorage;
import org.apache.jena.dboe.transaction.txn.TransactionalSystem;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.lib.NodeLib;
import org.apache.jena.tdb2.params.StoreParams;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;

final
public class DatasetGraphTDB extends DatasetGraphStorage
{
    private final StorageTDB storageTDB;
    private final Location location;
    private final TransactionalSystem txnSystem;
    private final StoreParams storeParams;
    private final ReorderTransformation reorderTransformation;
    private boolean isClosed = false;

    public DatasetGraphTDB(Location location, StoreParams params, ReorderTransformation reorderTransformation,
                           StorageTDB storage, StoragePrefixes prefixes, TransactionalSystem txnSystem) {
        super(storage, prefixes, txnSystem);
        this.storageTDB = storage;
        this.location = location;
        this.storeParams = params;
        this.txnSystem = txnSystem;
        this.reorderTransformation = reorderTransformation;
    }

    private void checkNotClosed() {
        if ( isClosed )
            throw new TDBException("dataset closed");
    }

    @Override
    public boolean supportsTransactionAbort() {
        return true;
    }
    
    public Location getLocation() {
        return location;
    }

    public QuadTable getQuadTable() {
        checkNotClosed();
        return storageTDB.getQuadTable();
    }

    public TripleTable getTripleTable() {
        checkNotClosed();
        return storageTDB.getTripleTable();
    }

    public TransactionalSystem getTxnSystem() {
        return txnSystem;
    }

    public StoreParams getStoreParams() {
        return storeParams;
    }

    public ReorderTransformation getReorderTransform() {
        return reorderTransformation;
    }

    @Override
    public void close() {
        isClosed = true;
        super.close();
    }

    public void shutdown() {
        close();
        txnSystem.getTxnMgr().shutdown();
    }

    @Override
    public Graph getDefaultGraph() {
        return getDefaultGraphTDB();
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return getGraphTDB(graphNode);
    }

    @Override
    public Graph getUnionGraph() {
        return getUnionGraphTDB();
    }

    public GraphTDB getDefaultGraphTDB() {
        checkNotClosed();
        return GraphTDB.tdb_createDefaultGraph(this, getStoragePrefixes());
    }

    public GraphTDB getGraphTDB(Node graphNode) {
        checkNotClosed();
        return GraphTDB.tdb_createNamedGraph(this, graphNode, getStoragePrefixes());
    }

    public GraphTDB getUnionGraphTDB() {
        checkNotClosed();
        return GraphTDB.tdb_createUnionGraph(this, getStoragePrefixes());
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        checkNotClosed();
        NodeTupleTable quads = getQuadTable().getNodeTupleTable();
        Iterator<Tuple<NodeId>> x = quads.findAll();
        // XXX Future: Ensure we scan a G??? index and use distinctAdjacent.
        // See TupleTable.chooseScanAllIndex
        Iterator<NodeId> z = Iter.iter(x).map(t -> t.get(0)).distinct();
        Iterator<Node> r = NodeLib.nodes(quads.getNodeTable(), z);
        return r;
    }

    public NodeTupleTable chooseNodeTupleTable(Node graphNode) {
        checkNotClosed();
        if ( graphNode == null || Quad.isDefaultGraph(graphNode) )
            return getTripleTable().getNodeTupleTable();
        else
            // Includes Node.ANY and union graph
            return getQuadTable().getNodeTupleTable();
    }
}
