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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
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
            if ( txnType == TxnType.WRITE && finish == Finish.END ) {
                // A write transaction must be committed or aborted before end(): see end_of_write_without_commit.
                continue;
            }
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
}
