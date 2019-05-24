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

package org.apache.jena.tdb2.loader.main;

import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.TxnType;
import org.apache.jena.tdb2.loader.base.BulkStartFinish;
import org.apache.jena.tdb2.loader.base.CoLib;
import org.apache.jena.tdb2.loader.base.MonitorOutput;
import org.apache.jena.tdb2.store.StoragePrefixesTDB;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;

/**
 * Prefix handler.
 * <p>
 * This class is not multithreaded - prefixes are usually few enough in number
 * and so forking a thread so that work can be done in parallel is not beneficial.
 */
public class PrefixHandlerBulk implements BulkStartFinish {
    private Transaction transaction;
    private TransactionCoordinator coordinator;
    private final StoragePrefixesTDB prefixes;
    private final MonitorOutput output;
    private final NodeTupleTable nodeTupleTable;

    public PrefixHandlerBulk(StoragePrefixesTDB prefixes, MonitorOutput output) {
        this.prefixes = prefixes;
        this.output = output;
        this.nodeTupleTable = prefixes.getNodeTupleTable();
    }

    // Inline, not a separate thread.
    @Override
    public void startBulk() {
        coordinator = CoLib.newCoordinator();
        CoLib.add(coordinator, nodeTupleTable.getNodeTable());
        CoLib.add(coordinator, nodeTupleTable.getTupleTable().getIndexes());
        coordinator.start();
        transaction = coordinator.begin(TxnType.WRITE);
    }

    @Override
    public void finishBulk() {
        transaction.commit();
        // Do not coordinator.shutdown() - that will close/shut the components.
    }

    public PrefixHandler handler() {
        return (prefix, uriStr) -> {
            Node p = NodeFactory.createLiteral(prefix);
            Node u = NodeFactory.createURI(uriStr);
            prefixes.add_ext(StoragePrefixes.nodeDefaultGraph, prefix, uriStr);
        };
    }
}
