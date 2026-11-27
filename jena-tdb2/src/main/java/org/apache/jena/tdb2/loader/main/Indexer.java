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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.jena.atlas.lib.ArrayUtils;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.query.TxnType;
import org.apache.jena.tdb2.loader.base.BulkStartFinish;
import org.apache.jena.tdb2.loader.base.CoLib;
import org.apache.jena.system.progress.MonitorOutput;
import org.apache.jena.tdb2.loader.BulkLoaderException;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;

/**
 * Build index(es).
 * Provides a function {@link #index()} that should be called from another thread
 * to deliver chunks of tuples ({@code  List<Tuple<NodeId>>}).
 * Each chunk should be the same Tuyple length and this must correspond to the length of the {@link TupleIndex}s being loaded.
 * <p>
 * This class creates one thread per {@link TupleIndex}.
 */
public class Indexer implements BulkStartFinish {

    private BlockingQueue<List<Tuple<NodeId>>>[] pipesTripleIndexers;
    private final int N;
    private final MonitorOutput output;
    private TupleIndex[] indexes;
    private final Semaphore termination = new Semaphore(0);
    // First failure of any index thread; rethrown by finishBulk() so the caller sees it.
    private final AtomicReference<Throwable> failure = new AtomicReference<>();

    @SuppressWarnings("unchecked")
    public Indexer(MonitorOutput output, TupleIndex... idxTriples) {
        pipesTripleIndexers = ArrayUtils.alloc(BlockingQueue.class, idxTriples.length);
        this.N = idxTriples.length;
        this.indexes = Arrays.copyOf(idxTriples, N);
        this.output = output;

        for ( int i = 0; i < N ; i++ ) {
            pipesTripleIndexers[i] = new ArrayBlockingQueue<List<Tuple<NodeId>>>(LoaderConst.QueueSizeTuples);
        }
    }

    /** Return a function that delivers multiple {@code List<Tuple<NodeId>>>} to this indexer */
    public Destination<Tuple<NodeId>> index() {
        return this::index;
    }

    private void index(List<Tuple<NodeId>> chunk) {
        for ( int i = 0; i < N ; i++ ) {
            try {
                pipesTripleIndexers[i].put(chunk);
            }
            catch (InterruptedException e) {
                String name = indexes[i].getName();
                Log.error(this, "Interrupted: "+name, e);
                throw new RuntimeException(e);
            }
        }
    }

    /** Start the threads that will do the indexing */
    @Override
    public void startBulk() {
        for ( int i = 0; i < N ; i++ ) {
            TupleIndex idx = indexes[i];
            BlockingQueue<List<Tuple<NodeId>>> pipe = pipesTripleIndexers[i];
            new Thread(()->stageIndex(pipe, idx)).start();
        }
    }

    /** Wait for all the indexing threads to complete. */
    @Override
    public void finishBulk() {
        //output.print("Wait for %d indexers", N);
        acquire(termination, N);
        Throwable th = failure.get();
        if ( th != null )
            throw new BulkLoaderException("Index build failed", th);
    }

    private void stageIndex(BlockingQueue<List<Tuple<NodeId>>> pipe, TupleIndex idx) {
        TransactionCoordinator coordinator = null;
        Transaction transaction = null;
        boolean started = false;
        boolean workHasBeenDone = false;
        boolean endOfInput = false;
        Throwable threadFailure = null;
        try {
            try {
                coordinator = CoLib.newCoordinator();
                CoLib.add(coordinator, idx);
                CoLib.start(coordinator);
                started = true;
                transaction = coordinator.begin(TxnType.WRITE);

                Destination<Tuple<NodeId>> loader = loadTuples(idx);
                for (;;) {
                    List<Tuple<NodeId>> tuples = pipe.take();
                    if ( tuples.isEmpty() ) {
                        endOfInput = true;
                        break;
                    }
                    loader.deliver(tuples);
                }
                workHasBeenDone = ! idx.isEmpty();
                transaction.commit();
            } catch (Throwable th) {
                // Catch Errors too (e.g. OutOfMemoryError): otherwise termination is never released and
                // finishBulk() waits forever.
                threadFailure = th;
                recordFailure(idx, th);
                if ( transaction != null ) {
                    try { transaction.abort(); } catch (Throwable th2) { if ( th2 != th ) th.addSuppressed(th2); }
                }
                workHasBeenDone = false;
                // Keep consuming, so the producer is not blocked on a full queue, until the end marker.
                if ( ! endOfInput )
                    drain(pipe);
            }
            if ( transaction != null ) {
                try {
                    transaction.end();
                } catch (Throwable th) {
                    threadFailure = recordCleanupFailure(idx, threadFailure, th);
                }
            }
            if ( started ) {
                try {
                    CoLib.finish(coordinator);
                } catch (Throwable th) {
                    threadFailure = recordCleanupFailure(idx, threadFailure, th);
                }
            }
            if ( workHasBeenDone && threadFailure == null )
                output.print("Finish - index %s", idx.getName());
        } finally {
            termination.release();
        }
    }

    private void recordFailure(TupleIndex idx, Throwable th) {
        failure.compareAndSet(null, th);
        Log.error(this, "Index "+idx.getName()+" failed", th);
    }

    private Throwable recordCleanupFailure(TupleIndex idx, Throwable threadFailure, Throwable th) {
        if ( threadFailure == null ) {
            failure.compareAndSet(null, th);
            Log.error(this, "Index "+idx.getName()+" cleanup failed", th);
            return th;
        }
        if ( th != threadFailure )
            threadFailure.addSuppressed(th);
        return threadFailure;
    }

    private static void drain(BlockingQueue<List<Tuple<NodeId>>> pipe) {
        try {
            for (;;) {
                if ( pipe.take().isEmpty() )
                    return;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static Destination<Tuple<NodeId>> loadTuples(TupleIndex index) {
        return (List<Tuple<NodeId>> tuples) -> {
            for(Tuple<NodeId> tuple : tuples)
                index.add(tuple);
        };
    }
}
