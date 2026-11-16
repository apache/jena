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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional.Promote;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;

/**
 * Tests for {@link DatasetGraphSwitchable}: a transaction stays on the database it began on
 * when the container is switched (as TDB2 compaction does), and that choice does not outlive
 * the transaction.
 */
public class TestDatasetGraphSwitchable {

    private static final Quad quad = SSE.parseQuad("(_ <s> <p> 1)");
    private static final Triple triple = SSE.parseTriple("(<s> <p> 2)");
    private static final Node graphName = NodeFactory.createURI("http://example/g");

    private DatasetGraphSwitchable container;
    private DatasetGraph first;
    private DatasetGraph second;

    @BeforeEach
    public void before() {
        container = (DatasetGraphSwitchable)DatabaseMgr.createDatasetGraph();
        first = container.get();
        second = ((DatasetGraphSwitchable)DatabaseMgr.createDatasetGraph()).get();
    }

    /** The ways a transaction can finish. */
    enum Finish {
        COMMIT { @Override void run(DatasetGraph dsg) { dsg.commit(); } },
        ABORT { @Override void run(DatasetGraph dsg) { dsg.abort(); } },
        END { @Override void run(DatasetGraph dsg) { dsg.end(); } },
        COMMIT_END { @Override void run(DatasetGraph dsg) { dsg.commit(); dsg.end(); } },
        ABORT_END { @Override void run(DatasetGraph dsg) { dsg.abort(); dsg.end(); } };

        abstract void run(DatasetGraph dsg);
    }

    @ParameterizedTest
    @EnumSource(TxnType.class)
    public void stays_on_database_and_is_released(TxnType txnType) {
        for ( Finish finish : Finish.values() ) {
            // A write transaction must be committed or aborted before end(): see end_of_write_without_commit.
            if ( txnType == TxnType.WRITE && finish == Finish.END )
                continue;
            container.begin(txnType);
            assertSame(first, container.get());

            container.set(second);
            assertSame(first, container.get(), "pinned for " + txnType + " then " + finish);
            assertTrue(container.isInTransaction());

            finish.run(container);

            assertFalse(container.isInTransaction(), "after " + txnType + " then " + finish);
            assertFalse(first.isInTransaction(), "the old database must have no transaction left");
            assertSame(second, container.get(), "pin not released for " + txnType + " then " + finish);
            // Container-level admission control is released: nothing is held, so exclusive mode is possible
            // and writers can start.
            assertTrue(container.tryExclusiveMode(), "transaction lock leaked for " + txnType + " then " + finish);
            container.finishExclusiveMode();
            assertTimeoutPreemptively(Duration.ofSeconds(10), ()->container.execReadOnlyDatabase(()->{}),
                                      "write admission leaked for " + txnType + " then " + finish);

            // Next round starts again on the other database.
            DatasetGraph tmp = first;
            first = second;
            second = tmp;
        }
    }

    @Test
    public void end_of_write_without_commit_still_releases_the_pin() {
        container.begin(TxnType.WRITE);
        container.set(second);

        assertThrows(TransactionException.class, ()->container.end());

        assertFalse(first.isInTransaction());
        assertSame(second, container.get());
    }

    @Test
    public void read_sees_the_database_it_began_on() {
        Txn.executeWrite(container, ()->container.add(quad));

        container.begin(TxnType.READ);
        container.set(second);
        assertTrue(container.contains(quad), "the transaction keeps reading its own database");
        container.end();

        Txn.executeRead(container, ()->assertFalse(container.contains(quad), "new transactions use the new database"));
    }

    @Test
    public void nested_begin_fails_and_keeps_the_pin() {
        container.begin(TxnType.READ);
        container.set(second);

        assertThrows(TransactionException.class, ()->container.begin(TxnType.READ));
        assertSame(first, container.get());

        container.end();
        assertSame(second, container.get());
    }

    @Test
    public void commit_without_end_does_not_pin_the_next_transaction() {
        container.begin(TxnType.READ);
        container.set(second);
        container.commit();
        assertSame(second, container.get());

        container.begin(TxnType.READ);
        assertSame(second, container.get());
        container.end();
    }

    @Test
    public void abort_without_end_does_not_pin_the_next_transaction() {
        container.begin(TxnType.WRITE);
        container.set(second);
        container.abort();
        assertSame(second, container.get());

        container.begin(TxnType.WRITE);
        assertSame(second, container.get());
        container.abort();
    }

    @Test
    public void pin_is_not_visible_to_other_threads() throws Exception {
        container.begin(TxnType.READ);
        container.set(second);

        AtomicReference<DatasetGraph> seen = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread thread = new Thread(()->{
            try {
                seen.set(container.get());
                Txn.executeRead(container, ()->assertSame(second, container.get()));
            } catch (Throwable th) {
                failure.set(th);
            }
        });
        thread.start();
        thread.join(10_000);

        assertNull(failure.get());
        assertSame(second, seen.get());
        assertSame(first, container.get(), "this thread keeps its pin");
        container.end();
    }

    // ---- Promotion: the write after promote() must go to the database the transaction began on.

    @ParameterizedTest
    @EnumSource(value = TxnType.class, names = {"READ_PROMOTE", "READ_COMMITTED_PROMOTE"})
    public void promote_after_switch_writes_to_the_pinned_database(TxnType txnType) {
        container.begin(txnType);
        container.set(second);

        assertTrue(container.promote());
        assertSame(first, container.get());
        container.add(quad);
        container.commit();
        container.end();

        assertOnlyIn(first, quad);
        assertReleased();
    }

    @ParameterizedTest
    @EnumSource(value = TxnType.class, names = {"READ_PROMOTE", "READ_COMMITTED_PROMOTE"})
    public void promote_before_switch_writes_to_the_pinned_database(TxnType txnType) {
        container.begin(txnType);
        assertTrue(container.promote());
        container.set(second);

        container.add(quad);
        container.commit();
        container.end();

        assertOnlyIn(first, quad);
        assertReleased();
    }

    @ParameterizedTest
    @EnumSource(Promote.class)
    public void promote_with_mode_after_switch_writes_to_the_pinned_database(Promote mode) {
        container.begin(TxnType.READ_PROMOTE);
        container.set(second);

        assertTrue(container.promote(mode));
        container.add(quad);
        container.commit();
        container.end();

        assertOnlyIn(first, quad);
        assertReleased();
    }

    @ParameterizedTest
    @EnumSource(value = TxnType.class, names = {"READ_PROMOTE", "READ_COMMITTED_PROMOTE"})
    public void promote_then_abort_changes_neither_database(TxnType txnType) {
        container.begin(txnType);
        container.set(second);
        assertTrue(container.promote());
        container.add(quad);
        container.abort();
        container.end();

        assertNotIn(first, quad);
        assertNotIn(second, quad);
        assertReleased();
    }

    @Test
    public void begin_without_type_is_pinned() {
        // Transactional.begin() is begin(READ_PROMOTE) and must get the same treatment.
        container.begin();
        container.set(second);
        assertSame(first, container.get());

        assertTrue(container.promote());
        container.add(quad);
        container.commit();
        container.end();

        assertOnlyIn(first, quad);
        assertReleased();
    }

    // ---- No leak: every write path inside a transaction goes to the pinned database,
    // and nothing written after the transaction goes there.

    @Test
    public void write_paths_after_switch_go_to_the_pinned_database() {
        container.begin(TxnType.WRITE);
        container.set(second);

        container.add(quad);
        container.getDefaultGraph().add(triple);
        container.getGraph(graphName).add(triple);
        container.prefixes().add("ex", "http://example/");
        container.commit();
        container.end();

        Txn.executeRead(first, ()->{
            assertTrue(first.contains(quad));
            assertTrue(first.getDefaultGraph().contains(triple));
            assertTrue(first.getGraph(graphName).contains(triple));
            assertTrue(first.prefixes().containsPrefix("ex"));
        });
        Txn.executeRead(second, ()->{
            assertTrue(second.isEmpty());
            assertFalse(second.getDefaultGraph().contains(triple));
            assertFalse(second.prefixes().containsPrefix("ex"));
        });
        assertReleased();
    }

    @Test
    public void graph_obtained_before_switch_writes_to_the_pinned_database() {
        // Graph views are cached by the container and resolve the database on every call.
        var graph = container.getDefaultGraph();
        container.begin(TxnType.WRITE);
        container.set(second);
        graph.add(triple);
        container.commit();
        container.end();

        Txn.executeRead(first, ()->assertTrue(first.getDefaultGraph().contains(triple)));
        Txn.executeRead(second, ()->assertFalse(second.getDefaultGraph().contains(triple)));
    }

    @Test
    public void next_transaction_writes_to_the_new_database() {
        container.begin(TxnType.WRITE);
        container.set(second);
        container.commit();
        container.end();

        Txn.executeWrite(container, ()->container.add(quad));

        assertOnlyIn(second, quad);
    }

    @Test
    public void write_after_commit_without_end_is_not_sent_to_the_old_database() {
        container.begin(TxnType.WRITE);
        container.set(second);
        container.commit();

        // Outside a transaction: TDB2 refuses the write rather than applying it anywhere.
        assertThrows(TransactionException.class, ()->container.add(quad));
        container.end();

        assertNotIn(first, quad);
        assertNotIn(second, quad);
    }

    @Test
    public void write_after_abort_without_end_is_not_sent_to_the_old_database() {
        container.begin(TxnType.WRITE);
        container.set(second);
        container.abort();

        assertThrows(TransactionException.class, ()->container.add(quad));
        container.end();

        assertNotIn(first, quad);
        assertNotIn(second, quad);
    }

    @Test
    public void failed_promote_keeps_the_pin_and_writes_nowhere() {
        // A READ transaction cannot promote: the transaction stays a reader on its database.
        container.begin(TxnType.READ);
        container.set(second);

        assertFalse(container.promote());
        assertSame(first, container.get());
        assertThrows(TransactionException.class, ()->container.add(quad));
        container.end();

        assertNotIn(first, quad);
        assertNotIn(second, quad);
        assertReleased();
    }

    @Test
    public void writer_blocked_during_switch_begins_on_new_database() throws Exception {
        CountDownLatch writerStarting = new CountDownLatch(1);
        AtomicReference<DatasetGraph> writerSeen = new AtomicReference<>();
        AtomicReference<Throwable> writerFailure = new AtomicReference<>();

        Thread writer = new Thread(()->{
            try {
                writerStarting.countDown();
                container.begin(TxnType.WRITE);
                try {
                    writerSeen.set(container.get());
                    container.add(quad);
                    container.commit();
                } finally {
                    container.end();
                }
            } catch (Throwable th) {
                writerFailure.set(th);
            }
        });

        container.execReadOnlyDatabase(()->{
            writer.start();
            try {
                assertTrue(writerStarting.await(10, TimeUnit.SECONDS));
                // Wait until the writer is parked inside admission control waiting for the read-only phase to end.
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                while ( writer.getState() != Thread.State.WAITING && System.nanoTime() < deadline )
                    Thread.sleep(5);
                assertEquals(Thread.State.WAITING, writer.getState(), "writer did not block in begin(WRITE)");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ex);
            }
            assertTrue(container.change(first, second));
        });

        writer.join(10_000);
        assertFalse(writer.isAlive(), "writer did not finish");
        assertNull(writerFailure.get());
        assertSame(second, writerSeen.get(), "blocked writer must begin on the new database");
        assertOnlyIn(second, quad);
        assertReleased();
    }

    private void assertOnlyIn(DatasetGraph dsg, Quad q) {
        DatasetGraph other = (dsg == first) ? second : first;
        Txn.executeRead(dsg, ()->assertTrue(dsg.contains(q), "missing from the expected database"));
        assertNotIn(other, q);
    }

    private static void assertNotIn(DatasetGraph dsg, Quad q) {
        Txn.executeRead(dsg, ()->assertFalse(dsg.contains(q), "written to the wrong database"));
    }

    private void assertReleased() {
        assertFalse(first.isInTransaction());
        assertFalse(second.isInTransaction());
        assertSame(second, container.get(), "pin not released");
        assertTrue(container.tryExclusiveMode(), "transaction lock leaked");
        container.finishExclusiveMode();
        assertTimeoutPreemptively(Duration.ofSeconds(10), ()->container.execReadOnlyDatabase(()->{}),
                                  "write admission leaked");
    }
}
