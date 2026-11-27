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

package org.apache.jena.tdb2.loader.main;

import static org.apache.jena.tdb2.loader.main.PhasedOps.acquire;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.loader.BulkLoaderException;
import org.apache.jena.tdb2.loader.base.BulkStartFinish;
import org.apache.jena.tdb2.loader.base.CoLib;
import org.apache.jena.system.progress.MonitorOutput;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;

/**
 * Batch processing of {@link DataBlock}s (triples or Quads)
 * converting them to two outputs of
 * blocks of {@code Tuple<NodeId>}.
 * <p>
 * This class runs one task thread which updates
 * <p>
 * Data is deliver into the process by calling the provided functions for {@code Destination<Tuple<NodeId>}.
 * <p>
 * Assumes triples and quads share a node table.
 */

public class DataToTuples implements BulkStartFinish {
    private long countTriples;
    private long countQuads;

    private final Destination<Tuple<NodeId>> dest3;
    private final Destination<Tuple<NodeId>> dest4;
    private final DatasetGraphTDB dsgtdb;
    private final NodeTable nodeTable;

    // Chunk accumulators.
    private List<Tuple<NodeId>> quads = null;
    private List<Tuple<NodeId>> triples = null;
    private final MonitorOutput output;
    private BlockingQueue<DataBlock> input;

    private Thread thread;

    public DataToTuples(DatasetGraphTDB dsgtdb,
                        Destination<Tuple<NodeId>> tuples3,
                        Destination<Tuple<NodeId>> tuples4,
                        MonitorOutput output) {
        this.dsgtdb = dsgtdb;
        this.dest3 = tuples3;
        this.dest4 = tuples4;
        this.input = new ArrayBlockingQueue<>(LoaderConst.QueueSizeData);
        this.nodeTable = dsgtdb.getQuadTable().getNodeTupleTable().getNodeTable();
        this.output = output;

        NodeTable nodeTable2 = dsgtdb.getTripleTable().getNodeTupleTable().getNodeTable();
        if ( nodeTable != nodeTable2 )
            throw new BulkLoaderException("Different node tables");
    }

    private TransactionCoordinator coordinator;
    private Transaction transaction;

    public Consumer<DataBlock> data() {
        return this::index;
    }

    private void index(DataBlock dataBlock) {
        try { input.put(dataBlock); }
        catch (InterruptedException e) {
            throw new BulkLoaderException("InterruptedException", e);
        }
    }

    /** Semaphore for the other thread to indicate it has finished. */
    private final Semaphore termination = new Semaphore(0);
    // Failure of the data thread; rethrown by finishBulk() so the caller sees it.
    private volatile Throwable failure = null;

    @Override
    public void startBulk() {
        thread = new Thread(()->action());
        thread.start();
    }

    @Override
    public void finishBulk() {
        acquire(termination);
        if ( failure != null )
            throw new BulkLoaderException("Data loading failed", failure);
    }

    // Triples.
    private void action() {
        boolean started = false;
        boolean endOfInput = false;
        boolean end3Sent = false;
        boolean end4Sent = false;
        try {
            try {
                coordinator = CoLib.newCoordinator();
                CoLib.add(coordinator, nodeTable);
                CoLib.start(coordinator);
                started = true;
                transaction = coordinator.begin(TxnType.WRITE);

                for (;;) {

                    DataBlock data = input.take();
                    if ( data == DataBlock.END ) {
                        endOfInput = true;
                        break;
                    }
                    if ( data.triples != null ) {
                        List<Tuple<NodeId>> tuples = new ArrayList<>(data.triples.size());
                        for ( Triple t : data.triples ) {
                            countTriples++;
                            accTuples(t, nodeTable, tuples);
                        }
                        dispatchTuples3(tuples);
                    }
                    if ( data.quads != null ) {
                        List<Tuple<NodeId>> tuples = new ArrayList<>(data.quads.size());
                        for ( Quad q : data.quads ) {
                            countQuads++;
                            accTuples(q, nodeTable, tuples);
                        }
                        dispatchTuples4(tuples);
                    }
                }
                dispatchTuples3(LoaderConst.END_TUPLES);
                end3Sent = true;
                dispatchTuples4(LoaderConst.END_TUPLES);
                end4Sent = true;
                transaction.commit();
            } catch (Throwable th) {
                // Catch Errors too, record the failure for finishBulk(), and still let the stages on both sides finish:
                // drain the input (the parser side must not block on a full queue) and send the end markers
                // downstream (the indexers would otherwise wait for them forever).
                failure = th;
                Log.error(this, "Exception during data loading", th);
                if ( transaction != null ) {
                    try { transaction.abort(); } catch (Throwable th2) { if ( th2 != th ) th.addSuppressed(th2); }
                }
                if ( ! endOfInput )
                    drainInput();
                if ( ! end3Sent ) {
                    try { dispatchTuples3(LoaderConst.END_TUPLES); } catch (Throwable th2) { if ( th2 != th ) th.addSuppressed(th2); }
                }
                if ( ! end4Sent ) {
                    try { dispatchTuples4(LoaderConst.END_TUPLES); } catch (Throwable th2) { if ( th2 != th ) th.addSuppressed(th2); }
                }
            }
            if ( transaction != null ) {
                try {
                    transaction.end();
                } catch (Throwable th) {
                    recordCleanupFailure(th);
                }
            }
            if ( started ) {
                try {
                    CoLib.finish(coordinator);
                } catch (Throwable th) {
                    recordCleanupFailure(th);
                }
            }
        } finally {
            termination.release();
        }
    }

    private void recordCleanupFailure(Throwable th) {
        if ( failure == null ) {
            failure = th;
            Log.error(this, "Exception during data loading cleanup", th);
        } else if ( th != failure ) {
            failure.addSuppressed(th);
        }
    }

    private void drainInput() {
        try {
            for (;;) {
                if ( input.take() == DataBlock.END )
                    return;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    //@Override
    public long getCountTriples()   { return countTriples; }
    //@Override
    public long getCountQuads()     { return countQuads; }

    private void dispatchTuples3(List<Tuple<NodeId>> chunk) {
        dest3.deliver(chunk);
    }

    private void dispatchTuples4(List<Tuple<NodeId>> chunk) {
        dest4.deliver(chunk);
    }

    private static void accTuples(Triple triple, NodeTable nodeTable, List<Tuple<NodeId>> acc) {
        acc.add(nodes(nodeTable, triple));
    }

    private static void accTuples(Quad quad, NodeTable nodeTable, List<Tuple<NodeId>> acc) {
        acc.add(nodes(nodeTable, quad));
    }

    // Recycle?
    private List<Tuple<NodeId>> allocChunkTriples() {
        return new ArrayList<>(LoaderConst.ChunkSize);
    }

    private List<Tuple<NodeId>> allocChunkQuads() {
        return new ArrayList<>(LoaderConst.ChunkSize);
    }

    private static Tuple<NodeId> nodes(NodeTable nt, Triple triple) {
        NodeId s = idForNode(nt, triple.getSubject());
        NodeId p = idForNode(nt, triple.getPredicate());
        NodeId o = idForNode(nt, triple.getObject());
        return TupleFactory.tuple(s,p,o);
    }

    private Function<List<Quad>, List<Tuple<NodeId>>> quadsToNodeIds(NodeTable nodeTable) {
        return (List<Quad> quads) -> {
            List<Tuple<NodeId>> x = new ArrayList<>(quads.size());
            for(Quad quad: quads) {
                x.add(nodes(nodeTable, quad));
            }
            return x;
        };
    }

    private static Tuple<NodeId> nodes(NodeTable nt, Quad quad) {
        NodeId g = idForNode(nt, quad.getGraph());
        NodeId s = idForNode(nt, quad.getSubject());
        NodeId p = idForNode(nt, quad.getPredicate());
        NodeId o = idForNode(nt, quad.getObject());
        return TupleFactory.tuple(g,s,p,o);
    }

    private static final NodeId idForNode(NodeTable nodeTable, Node node) {
        return nodeTable.getAllocateNodeId(node);
    }

}
