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

import java.util.Arrays;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.query.TxnType;
import org.apache.jena.tdb2.loader.base.BulkStartFinish;
import org.apache.jena.tdb2.loader.base.CoLib;
import org.apache.jena.tdb2.loader.base.MonitorOutput;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;

/**
 * Build index(es).
 * <p>
 * This is an inline indexer, it loads each Tuple<NodeId> on the calling thread. 
 */
public class IndexerInline implements BulkStartFinish {
    private final int N;
    private final MonitorOutput output;
    private TupleIndex[] indexes;
    private TransactionCoordinator coordinator;
    private Transaction transaction;
    
    public IndexerInline(MonitorOutput output, TupleIndex... idxTriples) {
        this.N = idxTriples.length;
        this.indexes = Arrays.copyOf(idxTriples, N); 
        this.output = output; 
    }
    
    @Override
    public void startBulk() { 
        TransactionCoordinator coordinator = CoLib.newCoordinator();
        Arrays.stream(indexes).forEach(idx->CoLib.add(coordinator, idx));
        CoLib.start(coordinator);
        transaction = coordinator.begin(TxnType.WRITE);
    }
    
    @Override
    public void finishBulk() { 
        transaction.commit();
        transaction.end();
        CoLib.finish(coordinator);
    }
    
    public void load(Tuple<NodeId> tuple) {
        for ( TupleIndex idx : indexes )
            idx.add(tuple);
    }
}
