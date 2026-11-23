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

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.progress.MonitorOutput;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.loader.BulkLoaderException;
import org.apache.jena.tdb2.loader.base.CoLib;
import org.apache.jena.tdb2.loader.base.BulkStartFinish;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdFactory;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.apache.jena.tdb2.sys.TDBInternal;

/**
 * A failure on one of the worker threads of {@link LoaderMain} ({@link Indexer}, {@link DataToTuples}) must reach
 * the caller through {@code finishBulk()}, and must not leave any stage of the pipeline blocked.
 */
public class TestLoaderMainFailures {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    // More chunks than a pipe between the stages holds, so a stage that stops consuming blocks its producer.
    private static final int CHUNKS = 5 * Math.max(LoaderConst.QueueSizeTuples, LoaderConst.QueueSizeData);
    private static final int CHUNK_SIZE = 10;

    private static final MonitorOutput output = (fmt, args) -> {};

    private DatasetGraph dsg;
    private DatasetGraphTDB dsgtdb;

    @BeforeEach
    public void before() {
        dsg = DatabaseMgr.createDatasetGraph();
        dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
    }

    @AfterEach
    public void after() {
        TDBInternal.expel(dsg);
    }

    @Test
    public void indexer_ok() {
        TupleIndex[] indexes = tripleIndexes();
        Indexer indexer = new Indexer(output, indexes);
        assertTimeoutPreemptively(TIMEOUT, () -> {
            indexer.startBulk();
            feed(indexer.index(), CHUNKS);
            indexer.finishBulk();
        });
        for ( TupleIndex idx : indexes )
            assertEquals(CHUNKS * CHUNK_SIZE, count(idx), "index " + idx.getName());
    }

    @Test
    public void indexer_failure_while_loading() {
        // The failed index stops early: before the fix it stopped taking from its pipe and the producer blocked.
        TupleIndex[] indexes = tripleIndexes();
        indexes[1] = failing(indexes[1], 1, () -> new RuntimeException("Injected failure"));
        assertIndexerFails(indexes);
    }

    @Test
    public void indexer_failure_on_last_tuple() {
        // The data has all been delivered: before the fix finishBulk() returned normally, with this index empty.
        TupleIndex[] indexes = tripleIndexes();
        indexes[2] = failing(indexes[2], CHUNKS * CHUNK_SIZE, () -> new RuntimeException("Injected failure"));
        assertIndexerFails(indexes);
    }

    @Test
    public void indexer_error() {
        // An Error is not an Exception: before the fix the thread died without releasing its termination permit.
        TupleIndex[] indexes = tripleIndexes();
        indexes[0] = failing(indexes[0], 1, () -> new OutOfMemoryError("Injected error"));
        assertIndexerFails(indexes);
    }

    @Test
    public void indexer_failure_on_startup() {
        // Failure during coordinator/transaction setup must still drain the pipe, release termination, and report.
        TupleIndex[] indexes = tripleIndexes();
        indexes[1] = failingOnStartup(indexes[1]);
        Indexer indexer = new Indexer(output, indexes);
        assertTimeoutPreemptively(TIMEOUT, () -> {
            indexer.startBulk();
            feed(indexer.index(), CHUNKS);
            BulkLoaderException ex = assertThrows(BulkLoaderException.class, indexer::finishBulk);
            assertNotNull(ex.getCause());
        });
    }

    @Test
    public void dataToTuples_failure() {
        // The downstream destination fails: DataToTuples must still drain its input (the parser side must not
        // block), send the end markers to the other destination, and report the failure.
        List<List<Tuple<NodeId>>> quadChunks = new CopyOnWriteArrayList<>();
        Destination<Tuple<NodeId>> failing3 = chunk -> { throw new RuntimeException("Injected failure"); };
        Destination<Tuple<NodeId>> recording4 = quadChunks::add;
        DataToTuples dataToTuples = new DataToTuples(dsgtdb, failing3, recording4, output);

        assertTimeoutPreemptively(TIMEOUT, () -> {
            dataToTuples.startBulk();
            for ( int i = 0 ; i < CHUNKS ; i++ )
                dataToTuples.data().accept(new DataBlock(triples(i), null));
            dataToTuples.data().accept(DataBlock.END);
            BulkLoaderException ex = assertThrows(BulkLoaderException.class, dataToTuples::finishBulk);
            assertEquals("Injected failure", ex.getCause().getMessage());
        });
        assertEquals(List.of(LoaderConst.END_TUPLES), quadChunks, "the quad indexers must get their end marker");
    }

    @Test
    public void dataToTuples_failure_on_end_marker() {
        // If dest3 fails on END_TUPLES, dest4 must still receive END_TUPLES so quad indexers do not block forever.
        List<List<Tuple<NodeId>>> quadChunks = new CopyOnWriteArrayList<>();
        Destination<Tuple<NodeId>> failing3OnEnd = chunk -> {
            if ( chunk == LoaderConst.END_TUPLES )
                throw new RuntimeException("Injected end failure");
        };
        Destination<Tuple<NodeId>> recording4 = quadChunks::add;
        DataToTuples dataToTuples = new DataToTuples(dsgtdb, failing3OnEnd, recording4, output);

        assertTimeoutPreemptively(TIMEOUT, () -> {
            dataToTuples.startBulk();
            dataToTuples.data().accept(new DataBlock(triples(0), null));
            dataToTuples.data().accept(DataBlock.END);
            BulkLoaderException ex = assertThrows(BulkLoaderException.class, dataToTuples::finishBulk);
            assertEquals("Injected end failure", ex.getCause().getMessage());
        });
        assertEquals(List.of(LoaderConst.END_TUPLES), quadChunks, "the quad indexers must get their end marker");
    }

    @Test
    public void bulkProcesses_finish_waits_for_all() {
        List<String> finished = new ArrayList<>();
        BulkStartFinish ok1 = process(() -> finished.add("ok1"));
        BulkStartFinish bad = process(() -> { finished.add("bad"); throw new BulkLoaderException("first"); });
        BulkStartFinish bad2 = process(() -> { finished.add("bad2"); throw new BulkLoaderException("second"); });
        BulkStartFinish ok2 = process(() -> finished.add("ok2"));

        // Finished in reverse order: ok2, bad2, bad, ok1.
        BulkLoaderException ex = assertThrows(BulkLoaderException.class,
                                              () -> BulkProcesses.finish(List.of(ok1, bad, bad2, ok2)));
        assertEquals(List.of("ok2", "bad2", "bad", "ok1"), finished, "every process must be finished");
        assertEquals("second", ex.getMessage());
        assertEquals("first", ex.getSuppressed()[0].getMessage());
    }

    @Test
    public void bulkProcesses_finish_waits_for_all_on_error() {
        List<String> finished = new ArrayList<>();
        BulkStartFinish ok1 = process(() -> finished.add("ok1"));
        BulkStartFinish bad = process(() -> { finished.add("bad"); throw new BulkLoaderException("later"); });
        BulkStartFinish err = process(() -> { finished.add("err"); throw new OutOfMemoryError("oom"); });
        BulkStartFinish ok2 = process(() -> finished.add("ok2"));

        // Finished in reverse order: ok2, err, bad, ok1.
        OutOfMemoryError thrown = assertThrows(OutOfMemoryError.class,
                                               () -> BulkProcesses.finish(List.of(ok1, bad, err, ok2)));
        assertEquals(List.of("ok2", "err", "bad", "ok1"), finished, "every process must be finished");
        assertEquals("oom", thrown.getMessage());
        assertEquals("later", thrown.getSuppressed()[0].getMessage());
    }

    private void assertIndexerFails(TupleIndex[] indexes) {
        Indexer indexer = new Indexer(output, indexes);
        assertTimeoutPreemptively(TIMEOUT, () -> {
            indexer.startBulk();
            feed(indexer.index(), CHUNKS);
            BulkLoaderException ex = assertThrows(BulkLoaderException.class, indexer::finishBulk);
            assertTrue(ex.getCause().getMessage().startsWith("Injected"), () -> "cause: " + ex.getCause());
        });
    }

    /** Count the tuples in {@code idx}, in a read transaction of its own as the loader's stages use. */
    private static long count(TupleIndex idx) {
        TransactionCoordinator coordinator = CoLib.newCoordinator();
        CoLib.add(coordinator, idx);
        CoLib.start(coordinator);
        Transaction transaction = coordinator.begin(TxnType.READ);
        try {
            return Iter.count(idx.all());
        } finally {
            transaction.end();
        }
    }

    private TupleIndex[] tripleIndexes() {
        TupleIndex[] indexes = dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes();
        return indexes.clone();
    }

    /** A view of {@code idx} (same storage) whose {@code n}-th add throws. */
    private static TupleIndex failing(TupleIndex idx, int n, Supplier<Throwable> failure) {
        TupleIndexRecord rec = (TupleIndexRecord)idx;
        RecordFactory factory = new RecordFactory(rec.getTupleLength() * SystemTDB.SizeOfNodeId, 0);
        AtomicInteger count = new AtomicInteger();
        return new TupleIndexRecord(rec.getTupleLength(), rec.getMapping(), rec.getName(), factory, rec.getRangeIndex()) {
            @Override
            protected void performAdd(Tuple<NodeId> tuple) {
                if ( count.incrementAndGet() == n ) {
                    Throwable th = failure.get();
                    if ( th instanceof Error err )
                        throw err;
                    throw (RuntimeException)th;
                }
                super.performAdd(tuple);
            }
        };
    }

    /** A view of {@code idx} with no backing RangeIndex so coordinator setup fails before the write transaction begins. */
    private static TupleIndex failingOnStartup(TupleIndex idx) {
        TupleIndexRecord rec = (TupleIndexRecord)idx;
        RecordFactory factory = new RecordFactory(rec.getTupleLength() * SystemTDB.SizeOfNodeId, 0);
        return new TupleIndexRecord(rec.getTupleLength(), rec.getMapping(), rec.getName(), factory, null);
    }

    private static void feed(Destination<Tuple<NodeId>> dest, int chunks) {
        long id = 1;
        for ( int c = 0 ; c < chunks ; c++ ) {
            List<Tuple<NodeId>> chunk = new ArrayList<>(CHUNK_SIZE);
            for ( int i = 0 ; i < CHUNK_SIZE ; i++, id++ )
                chunk.add(TupleFactory.create3(nodeId(id), nodeId(id + 1_000_000), nodeId(id + 2_000_000)));
            dest.deliver(chunk);
        }
        dest.deliver(LoaderConst.END_TUPLES);
    }

    private static NodeId nodeId(long value) {
        return NodeIdFactory.createPtr(value);
    }

    private static List<Triple> triples(int chunk) {
        List<Triple> triples = new ArrayList<>(CHUNK_SIZE);
        for ( int i = 0 ; i < CHUNK_SIZE ; i++ ) {
            Node s = NodeFactory.createURI("http://example/s" + chunk + "_" + i);
            triples.add(Triple.create(s, NodeFactory.createURI("http://example/p"), NodeFactory.createLiteralString("o")));
        }
        return triples;
    }

    private static BulkStartFinish process(Runnable onFinish) {
        return new BulkStartFinish() {
            @Override public void startBulk() {}
            @Override public void finishBulk() { onFinish.run(); }
        };
    }
}
