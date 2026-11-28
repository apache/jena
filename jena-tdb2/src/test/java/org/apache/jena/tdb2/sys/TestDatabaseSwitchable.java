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

package org.apache.jena.tdb2.sys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.PrintStream;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.ThreadLib;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.sys.IO_DB;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.ConfigTest;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.store.DatasetGraphSwitchable;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.awaitility.Awaitility;
import org.opentest4j.AssertionFailedError;

public class TestDatabaseSwitchable {
    private static PrintStream out = System.err;

    @AfterEach public void afterTest() {
        TDBInternal.reset();
    }

    @Test public void switchable_pinned_database_different_replacement() {
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        DatasetGraphTDB dsg1 = TDBInternal.getDatasetGraphTDB(dsg);
        DatasetGraphTDB dsg2 = TDBInternal.getDatasetGraphTDB(DatabaseMgr.createDatasetGraph());
        // Manually created ...
        //DatasetGraphSwitchable dsgSwitchable = new DatasetGraphSwitchable(null, null, dsg1);
        DatasetGraphSwitchable dsgSwitchable = (DatasetGraphSwitchable)dsg;
        switchover(dsgSwitchable, dsg2);
    }

    @Test public void switchable_pinned_database_same_replacement() {
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        DatasetGraphTDB dsg1 = TDBInternal.getDatasetGraphTDB(dsg);
        DatasetGraphSwitchable dsgSwitchable = (DatasetGraphSwitchable)dsg;
        switchover(dsgSwitchable, dsg1);
    }

    // NB A compaction goes exclusive mode so the reader must have ended.
    // This makes the testing logic different.
    @Test public void switchable_pinned_database_compaction() {
     // Must be a disk-based database.
        String DIR = ConfigTest.getCleanDir();
        FileOps.ensureDir(DIR);
        FileOps.clearAll(DIR);
        Location dir = Location.create(DIR);

        try {
            switchable_pinned_database_compaction_test(dir);
        } finally {
            TDBInternal.reset();
            FileUtils.deleteQuietly(IO_DB.asFile(dir));
        }
    }

    private void switchable_pinned_database_compaction_test(Location dir) {
        // Must be a disk-based database.
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(dir);
        DatasetGraphSwitchable dsgSwitchable = (DatasetGraphSwitchable)dsg;
        switchover_compact(dsgSwitchable);
    }

    private void switchover_compact(DatasetGraphSwitchable dsgSwitchable) {
        Objects.requireNonNull(dsgSwitchable);

        // Signal reader running, inside R transaction
        Semaphore signalReaderTxnStarted = new Semaphore(0);
        // Signal reader has called .end();
        Semaphore signalReaderTxnEnded = new Semaphore(0);
        // Signal switchover has occurred
        Semaphore signalCompaction = new Semaphore(0);

        CountDownLatch threadsFinishedGate = new CountDownLatch(2);

        AtomicReference<AssertionFailedError> readerAssertionFailed = new AtomicReference<AssertionFailedError>(null);
        AtomicReference<Throwable> unexpected = new AtomicReference<Throwable>(null);
        DatasetGraphTDB initialDSG = (DatasetGraphTDB)dsgSwitchable.get();

        ThreadLib.async(() ->{
            try {
                Txn.executeRead(dsgSwitchable, ()->{
                    signalReaderTxnStarted.release();
                    // Random pause so the compact can reach "go exclusive".
                    // Ideally control with signalling from the compact code.
                    Lib.sleep(1000);
                });

                signalReaderTxnEnded.release();
                signalCompaction.acquire();

                DatasetGraphTDB afterSwitch = (DatasetGraphTDB)dsgSwitchable.get();
                assertNotSame(initialDSG, afterSwitch);

                long rCount1 = countActiveReaders(initialDSG);
                long rCount2 = countActiveReaders(afterSwitch);
                long rCountSW = countActiveReaders(dsgSwitchable);

                // Failures caught and passed to the main thread.
                assertEquals(0, rCount1, "Reader thread should not see transaction on initial database");
                assertEquals(0, rCount2, "Reader thread should not see transaction on replacement database");
                assertEquals(0, rCountSW, "Reader thread should not see tranaction on switchable database");
            } catch (AssertionFailedError ex) {
                // Pass back to the main thread
                ex.fillInStackTrace();
                readerAssertionFailed.set(ex);
            } catch (Throwable th) {
                // Pass back to the main thread
                th.fillInStackTrace();
                unexpected.set(th);
            } finally {
                threadsFinishedGate.countDown();
            }
        });

        // Compaction thread
        try {
            signalReaderTxnStarted.acquire();

            // ---- Compaction.
            // This should not block forever when it tries to go into exclusive mode.

            //??
            // XXXAlso simulate the steps of compact - execDatabaseRead ***
            // 1. Compaction task a callback "about to go exclusive"
            // 2. Compaction polls (does about to go exclusive?)

            final AtomicBoolean compactFinished = new AtomicBoolean(false);
            ThreadLib.async(() ->{
                DatabaseOps.compact(dsgSwitchable);
                compactFinished.set(true);}
            );
            Awaitility.await().timeout(10, TimeUnit.SECONDS).untilTrue(compactFinished);

            signalReaderTxnEnded.acquire();
            DatasetGraphTDB afterSwitch = (DatasetGraphTDB)dsgSwitchable.get();
            assertNotSame(initialDSG, afterSwitch);

            // Announce switchover
            signalCompaction.release();

            {
                long rCount = countActiveReaders(initialDSG);
                assertEquals(0, rCount, "Reader count on initial database should be 0 after reader signalled end()");
            }

            threadsFinishedGate.countDown();
        } catch (InterruptedException ex) {
            throw new RuntimeException("InterruptedException", ex);
        }

        // ---
        try { threadsFinishedGate.await(); } catch (InterruptedException ex) { throw new RuntimeException("InterruptedException", ex); }

        // Check for assertion failures and unexpected throwables.
        AssertionFailedError error = readerAssertionFailed.get();
        if ( error != null )
            throw error;
        Throwable unexpectedEvent = unexpected.get();
        if ( unexpectedEvent != null )
            throw new RuntimeException(unexpectedEvent);
    }

    /**
     * Active readers as seen by the argument dataset.
     * Works for switchable and storage DatastGraph.
     * Only works for TDB2.
     */
    private static long countActiveReaders(DatasetGraph dsgtdb) {
        if ( dsgtdb == null )
            return -1;
        TransactionCoordinator txnCoord = TDBInternal.getTransactionCoordinator(dsgtdb);
        return txnCoord.countActiveReaders();
    }

    @FunctionalInterface
    interface SwitchOver { void action(DatasetGraphSwitchable dsgSwitchable, DatasetGraphTDB current, DatasetGraphTDB replacement); }

    private static void switchover(DatasetGraphSwitchable dsgSwitchable, DatasetGraphTDB replacement) {
        Objects.requireNonNull(dsgSwitchable);
        Objects.requireNonNull(replacement);

        DatasetGraphTDB initialDSG = (DatasetGraphTDB)(dsgSwitchable.get());
        // Consistency check
        {
            DatasetGraphTDB initialDSG_x = TDBInternal.getDatasetGraphTDB(dsgSwitchable);
            if ( initialDSG != initialDSG_x ) {
                // expected same java object
                throw new RuntimeException("Broken setup");
            }
        }

        Consumer<String> printStatus = (label) -> {
            print(label);
            // Get every time.
            long initialR = countActiveReaders(initialDSG);
            long replacementR = countActiveReaders(replacement);
            long switchableR = countActiveReaders(dsgSwitchable);
            String msg = String.format("   initial=%d replacement=%d switchable=%d", initialR, replacementR, switchableR);
            print(msg);
        };

        // Signal reader running, inside R transaction
        Semaphore signalReaderStarted = new Semaphore(0);
        // Signal switchover has occurred
        Semaphore signalSwitchover = new Semaphore(0);
        Semaphore signalReaderEndCalled = new Semaphore(0);

        CountDownLatch threadsFinishedGate = new CountDownLatch(2);

        final AtomicReference<AssertionFailedError> readerAssertionFailed = new AtomicReference<AssertionFailedError>(null);
        final AtomicReference<Throwable> unexpected = new AtomicReference<Throwable>(null);

        try {
            ThreadLib.async(() ->{
                try {
                    dsgSwitchable.begin(TxnType.READ);
                    printStatus.accept("** R begin");
                    signalReaderStarted.release();
                    try {
                        signalSwitchover.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    printStatus.accept("** R after switch");

                    // End read transaction on switchable.
                    dsgSwitchable.end();

                    DatasetGraphTDB afterSwitch = replacement;
                    if ( afterSwitch == null )
                        afterSwitch = (DatasetGraphTDB)dsgSwitchable.get();

                    signalReaderEndCalled.release();

                    long rCount1 = countActiveReaders(initialDSG);
                    long rCount2 = countActiveReaders(afterSwitch);
                    long rCountSW = countActiveReaders(dsgSwitchable);

                    if ( rCount1 != 0 || rCount2 != 0 || rCountSW != 0 ) {
                        print("\n** ** readcounts not all zeros ** **\n");
                    }

                    // Failures caught and passed to the main thread.
                    assertEquals(0, rCount1, "Reader thread should not see transaction on initial database");
                    assertEquals(0, rCount2, "Reader thread should not see transaction on replacement database");
                    assertEquals(0, rCountSW, "Reader thread should not see transaction on switchable database");

                    printStatus.accept("** after dsgSwitchable.end()");

                } catch (AssertionFailedError ex) {
                    // Pass back to the main thread
                    ex.fillInStackTrace();
                    readerAssertionFailed.set(ex);
                } catch (Throwable th) {
                    // Pass back to the main thread
                    th.fillInStackTrace();
                    unexpected.set(th);
                } finally {
                    threadsFinishedGate.countDown();
                    signalReaderEndCalled.release();
                }
            });

            print("-- Switching thread starts");
            try {
                signalReaderStarted.acquire();
                printStatus.accept("-- Reader thread is running");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            printStatus.accept("-- Switching about to switch");

            // ---- Switchover
            // Initial has one R transaction
            // -- Checks before
            {
                long rCount11 = countActiveReaders(initialDSG);
                assertEquals(1, rCount11, "Reader count on initial database should be 1");
                if ( initialDSG != replacement && replacement != null ) {
                    long rCount2 = countActiveReaders(replacement);
                    assertEquals(0, rCount2, "Reader count on replacement database should be 0 before switch over");
                }
            }

            // ---- Action
            dsgSwitchable.change(initialDSG, replacement);

            DatasetGraphTDB afterSwitch = replacement;
            if ( afterSwitch == null )
                afterSwitch = (DatasetGraphTDB)dsgSwitchable.get();


            printStatus.accept("-- Switching thread has switched");

            // Wait for reader thread to call .end()
            // -- Checks immediately after, before releasing the reader.
            {
                long rCount = countActiveReaders(initialDSG);
                assertEquals(1, rCount, "Reader count on initial database should still be 1 after switch over");
                if ( initialDSG != afterSwitch ) {
                    long rCount2 = countActiveReaders(afterSwitch);
                    assertEquals(0, rCount2, "Reader count on replacement database should be 0 after switch over");
                }
            }

            // Announce switchover and wait for reader to call end()
            signalSwitchover.release();
            signalReaderEndCalled.acquire();
            {
                long rCount = countActiveReaders(initialDSG);
                assertEquals(0, rCount, "Reader count on initial database should be 0 after reader signalled end()");
            }

            threadsFinishedGate.countDown();
            // ---
            threadsFinishedGate.await();

            // Check for assertion failures and unexpected throwables.
            AssertionFailedError error = readerAssertionFailed.get();
            if ( error != null )
                throw error;
            Throwable unexpectedEvent  = unexpected.get();
            if ( unexpectedEvent != null )
                throw new RuntimeException(unexpectedEvent);
        } catch (InterruptedException ex) {
            throw new RuntimeException("InterruptedException", ex);
        }
    }

    private static boolean DEBUG = false;

    private static void print(String msg) {
        if ( ! DEBUG )
            return;
        out.println(msg);
        out.flush();
    }

}
