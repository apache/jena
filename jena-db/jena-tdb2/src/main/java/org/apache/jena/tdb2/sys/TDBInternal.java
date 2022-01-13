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

package org.apache.jena.tdb2.sys;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.store.DatasetGraphSwitchable;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;

/**
 * A collection of helpers to abstract away from calling code knowing the
 * internal details of TDB.
 * <p>
 * Use with care.
 * <p>{@link DatabaseOps#compact Compaction} invalidates any previous objects.
 *
 */
public class TDBInternal {

    /**
     * Return true if this is a TDB2 backed DatasetGraph.
     */
    public static boolean isTDB2(DatasetGraph dsg) {
        return ( dsg instanceof DatasetGraphSwitchable );
    }

    /**
     * Return the NodeId for a node. Returns NodeId.NodeDoesNotExist when the node is not
     * found. Returns null when not a TDB-backed dataset.
     */
    public static NodeId getNodeId(Dataset ds, Node node) {
        return getNodeId(ds.asDatasetGraph(), node);
    }

    /**
     * Return the NodeId for a node. Returns NodeId.NodeDoesNotExist when the node is not
     * found. Returns null when not a TDB-backed dataset.
     */
    public static NodeId getNodeId(DatasetGraph ds, Node node) {
        DatasetGraphTDB dsg = getDatasetGraphTDB(ds);
        return getNodeId(dsg, node);
    }

    /**
     * Return the NodeId for a node. Returns NodeId.NodeDoesNotExist when the node is not
     * found. Returns null when not a TDB-backed dataset.
     */
    public static NodeId getNodeId(DatasetGraphTDB dsg, Node node) {
        if ( dsg == null )
            return null;
        NodeTable nodeTable = dsg.getQuadTable().getNodeTupleTable().getNodeTable();
        NodeId nodeId = nodeTable.getNodeIdForNode(node);
        return nodeId;
    }

    /**
     * Return the node for a NodeId (if any). Returns null if the NodeId does not exist in
     * the dataset.
     */
    public static Node getNode(Dataset ds, NodeId nodeId) {
        return getNode(ds.asDatasetGraph(), nodeId);
    }

    /**
     * Return the node for a NodeId (if any). Returns null if the NodeId does not exist in
     * the dataset.
     */
    public static Node getNode(DatasetGraph ds, NodeId nodeId) {
        DatasetGraphTDB dsg = getDatasetGraphTDB(ds);
        return getNode(dsg, nodeId);
    }

    /**
     * Return the node for a NodeId (if any). Returns null if the NodeId does not exist in
     * the dataset.
     */
    public static Node getNode(DatasetGraphTDB dsg, NodeId nodeId) {
        if ( dsg == null )
            return null;
        NodeTable nodeTable = dsg.getQuadTable().getNodeTupleTable().getNodeTable();
        Node node = nodeTable.getNodeForNodeId(nodeId);
        return node;
    }

    /**
     * Return the DatasetGraphTDB for a Dataset, or null.
     * Use the {@link DatasetGraphTDB} with care.
     */
    public static DatasetGraphTDB getDatasetGraphTDB(Dataset ds) {
        return getDatasetGraphTDB(ds.asDatasetGraph());
    }

    /**
     * Return the DatasetGraphTDB for a DatasetGraph, or null.
     * Use the {@link DatasetGraphTDB} with care.
     */
    public static DatasetGraphSwitchable getDatabaseContainer(DatasetGraph dsg) {
        if ( dsg instanceof DatasetGraphSwitchable )
            return (DatasetGraphSwitchable)dsg;
        throw new TDBException("Not a TDB database container");
    }

    /**
     * Return the {@link TransactionCoordinator} for a TDB2-backed DatasetGraph
     * or null, if not backed by TDB2.
     */
    public static TransactionCoordinator getTransactionCoordinator(DatasetGraph dsg) {
        DatasetGraphTDB dsgtdb = getDatasetGraphTDB(dsg);
        if ( dsgtdb == null )
            return null;
        return dsgtdb.getTxnSystem().getTxnMgr();
    }

    /**
     * Return the DatasetGraphTDB for a DatasetGraph, or null.
     * Use the {@link DatasetGraphTDB} with care.
     */
    public static DatasetGraphTDB getDatasetGraphTDB(DatasetGraph dsg) {
        return unwrap(dsg);
    }

    /**
     * Return the DatasetGraphTDB for a DatasetGraph, or throw an exception.
     */
    public static DatasetGraphTDB requireStorage(DatasetGraph dsg) {
        DatasetGraphTDB dsgtdb = unwrap(dsg);
        if ( dsgtdb == null )
            throw new TDBException("Not a TDB database (argument is neither a switchable nor direct TDB DatasetGraph)");
        return dsgtdb;
    }

    private static DatasetGraphTDB unwrap(DatasetGraph datasetGraph) {
        DatasetGraph dsg = datasetGraph;
        if ( dsg instanceof DatasetGraphSwitchable )
            dsg = ((DatasetGraphSwitchable)datasetGraph).get();
        if ( dsg instanceof DatasetGraphTDB )
            return ((DatasetGraphTDB)dsg);
        return null;
    }


    /** Stop managing a DatasetGraph. Use with great care. */
    public static synchronized void expel(DatasetGraph dsg) {
        Location locContainer = null;
        Location locStorage = null;

        if ( dsg instanceof DatasetGraphSwitchable ) {
            locContainer = ((DatasetGraphSwitchable)dsg).getLocation();
            dsg = ((DatasetGraphSwitchable)dsg).getWrapped();
        }
        if ( dsg instanceof DatasetGraphTDB )
            locStorage = ((DatasetGraphTDB)dsg).getLocation();

        if ( locContainer != null )
            DatabaseConnection.internalExpel(locContainer, false);
        StoreConnection.internalExpel(locStorage, false);
    }

    /** Stop managing a DatasetGraph. Use with great care. */
    public static synchronized void expel(DatasetGraph dsg, boolean force) {
        Location locContainer = null;
        Location locStorage = null;

        if ( dsg instanceof DatasetGraphSwitchable ) {
            locContainer = ((DatasetGraphSwitchable)dsg).getLocation();
            dsg = ((DatasetGraphSwitchable)dsg).getWrapped();
        }
        if ( dsg instanceof DatasetGraphTDB )
            locStorage = ((DatasetGraphTDB)dsg).getLocation();

        DatabaseConnection.internalExpel(locContainer, force);
        StoreConnection.internalExpel(locStorage, force);
    }


    /**
     * Reset the whole TDB system.
     * Use with great care.
     */
    public static void reset() {
        DatabaseConnection.internalReset();
        StoreConnection.internalReset();
    }

    public static boolean isBackedByTDB(DatasetGraph datasetGraph) {
        if ( datasetGraph instanceof DatasetGraphSwitchable )
            return true;
        if ( datasetGraph instanceof DatasetGraphTDB )
            return true;
        return false;
    }
}
