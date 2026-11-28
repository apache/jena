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

package org.apache.jena.rdfpatch.filelog;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdfpatch.PatchException;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.rdfpatch.changes.PatchSummary;
import org.apache.jena.rdfpatch.filelog.rotate.ManagedOutput;
import org.apache.jena.rdfpatch.filelog.rotate.OutputManagedFile;
import org.apache.jena.rdfpatch.system.DatasetGraphChanges;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.system.Txn;

public class TestRDFChangesManagedOutput {

    private static final String BASE = "log.rdfp";

    private static List<Path> listPatchFiles(Path dir, String prefix) throws IOException {
        try ( Stream<Path> stream = Files.list(dir) ) {
            return stream
                .filter(p -> p.getFileName().toString().startsWith(prefix + "."))
                .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                .toList();
        }
    }

    private static void awaitWaiting(Thread thread, long timeoutMs) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
        while ( System.nanoTime() < deadline ) {
            Thread.State state = thread.getState();
            if ( state == Thread.State.WAITING || state == Thread.State.BLOCKED || state == Thread.State.TERMINATED )
                return;
            Thread.yield();
        }
    }

    /**
     * Standalone deterministic reproducer: thread B calls begin(WRITE) while thread A
     * holds a write transaction on DatasetGraphChanges. Because DatasetGraphChanges.begin
     * calls changesMonitor.txnBegin() before acquiring the underlying dataset write lock,
     * thread B logs txnBegin and then blocks waiting for the write lock until thread A commits.
     */
    @Test
    public void secondWriterBeginsWhileWriteActive(@TempDir Path dir) throws Exception {
        ManagedOutput mout = new OutputManagedFile(dir, BASE, FilePolicy.INDEX);
        CountDownLatch aInWrite = new CountDownLatch(1);
        CountDownLatch bLoggedTxnBegin = new CountDownLatch(1);
        RDFChangesManagedOutput changes = new RDFChangesManagedOutput(mout) {
            @Override
            public void txnBegin() {
                super.txnBegin();
                if ( aInWrite.getCount() == 0 )
                    bLoggedTxnBegin.countDown();
            }
        };
        DatasetGraph dsg = new DatasetGraphChanges(DatasetGraphFactory.createTxnMem(), changes);

        Node sA = NodeFactory.createURI("http://example/sA");
        Node sB = NodeFactory.createURI("http://example/sB");
        Node p = NodeFactory.createURI("http://example/p");
        Node oA = NodeFactory.createLiteralString("a");
        Node oB = NodeFactory.createLiteralString("b");

        AtomicReference<Throwable> error = new AtomicReference<>();

        Thread threadB = new Thread(() -> {
            try {
                assertTrue(aInWrite.await(10, TimeUnit.SECONDS));
                dsg.begin(TxnType.WRITE);
                try {
                    dsg.add(Quad.defaultGraphIRI, sB, p, oB);
                    dsg.commit();
                } finally {
                    dsg.end();
                }
            } catch (Throwable ex) {
                error.compareAndSet(null, ex);
            }
        }, "writer-B");

        Thread threadA = new Thread(() -> {
            try {
                dsg.begin(TxnType.WRITE);
                try {
                    dsg.add(Quad.defaultGraphIRI, sA, p, oA);
                    aInWrite.countDown();
                    assertTrue(bLoggedTxnBegin.await(10, TimeUnit.SECONDS), "Thread B did not reach txnBegin");
                    awaitWaiting(threadB, 10_000);
                    dsg.commit();
                } finally {
                    dsg.end();
                }
            } catch (Throwable ex) {
                error.compareAndSet(null, ex);
            }
        }, "writer-A");

        threadA.start();
        threadB.start();
        threadA.join(10_000);
        threadB.join(10_000);

        assertFalse(threadA.isAlive(), "Thread A deadlocked");
        assertFalse(threadB.isAlive(), "Thread B deadlocked");
        assertNull(error.get());

        List<Path> patchFiles = listPatchFiles(dir, BASE);
        assertEquals(2, patchFiles.size());

        DatasetGraph replay = DatasetGraphFactory.createTxnMem();
        Node prevId = null;
        for ( Path file : patchFiles ) {
            RDFPatch patch = RDFPatchOps.read(file.toString());
            PatchSummary summary = RDFPatchOps.summary(patch);
            assertEquals(1, summary.getCountTxnBegin(), () -> "Expected single TX in " + file.getFileName());
            assertEquals(1, summary.getCountTxnCommit(), () -> "Expected single TC in " + file.getFileName());
            assertEquals(0, summary.getCountTxnAbort(), () -> "Unexpected TA in " + file.getFileName());

            Node id = patch.getId();
            assertNotNull(id);
            assertEquals(prevId, patch.getPrevious());
            prevId = id;

            RDFPatchOps.applyChange(replay, patch);
        }

        assertTrue(IsoMatcher.isomorphic(dsg, replay));
    }

    @Test
    public void concurrentWriteTransactions(@TempDir Path dir) throws Exception {
        ManagedOutput mout = OutputMgr.create(dir, BASE, FilePolicy.INDEX);
        DatasetGraph dsg = new DatasetGraphChanges(DatasetGraphFactory.createTxnMem(), new RDFChangesManagedOutput(mout));

        Node sharedS = NodeFactory.createURI("http://example/shared");
        Node sharedP = NodeFactory.createURI("http://example/value");
        Txn.executeWrite(dsg, () ->
            dsg.add(Quad.defaultGraphIRI, sharedS, sharedP, NodeFactory.createLiteralString("initial")));

        int threads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        List<Future<?>> futures = new ArrayList<>();
        try {
            for ( int i = 0 ; i < threads ; i++ ) {
                final int id = i;
                futures.add(executor.submit(() -> {
                    barrier.await(10, TimeUnit.SECONDS);
                    Txn.executeWrite(dsg, () -> {
                        // Delete existing value and replace it, plus add a per-thread quad.
                        dsg.deleteAny(Quad.defaultGraphIRI, sharedS, sharedP, Node.ANY);
                        dsg.add(Quad.defaultGraphIRI, sharedS, sharedP, NodeFactory.createLiteralString("v-" + id));
                        Node s = NodeFactory.createURI("http://example/s/" + id);
                        Node p = NodeFactory.createURI("http://example/p");
                        dsg.add(Quad.defaultGraphIRI, s, p, NodeFactory.createLiteralString("o-" + id));
                    });
                    return null;
                }));
            }
            for ( Future<?> f : futures ) {
                f.get(30, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        List<Path> patchFiles = listPatchFiles(dir, BASE);
        assertEquals(1 + threads, patchFiles.size());

        DatasetGraph replay = DatasetGraphFactory.createTxnMem();
        Node prevId = null;
        for ( Path file : patchFiles ) {
            RDFPatch patch = RDFPatchOps.read(file.toString());
            PatchSummary summary = RDFPatchOps.summary(patch);
            assertEquals(1, summary.getCountTxnBegin(), () -> "Expected single TX in " + file.getFileName());
            assertEquals(1, summary.getCountTxnCommit(), () -> "Expected single TC in " + file.getFileName());
            assertEquals(0, summary.getCountTxnAbort(), () -> "Unexpected TA in " + file.getFileName());

            Node id = patch.getId();
            assertNotNull(id);
            assertEquals(prevId, patch.getPrevious());
            prevId = id;

            RDFPatchOps.applyChange(replay, patch);
        }

        assertTrue(IsoMatcher.isomorphic(dsg, replay));
    }

    @Test
    public void emptyCommitAndAbortTransactions(@TempDir Path dir) throws Exception {
        ManagedOutput mout = OutputMgr.create(dir, BASE, FilePolicy.INDEX);
        DatasetGraph dsg = new DatasetGraphChanges(DatasetGraphFactory.createTxnMem(), new RDFChangesManagedOutput(mout));

        dsg.begin(TxnType.WRITE);
        dsg.commit();
        dsg.end();

        dsg.begin(TxnType.WRITE);
        dsg.abort();
        dsg.end();

        List<Path> patchFiles = listPatchFiles(dir, BASE);
        assertEquals(2, patchFiles.size());

        RDFPatch commitPatch = RDFPatchOps.read(patchFiles.get(0).toString());
        PatchSummary commitSummary = RDFPatchOps.summary(commitPatch);
        assertEquals(1, commitSummary.getCountTxnBegin());
        assertEquals(1, commitSummary.getCountTxnCommit());
        assertEquals(0, commitSummary.getCountTxnAbort());
        assertNotNull(commitPatch.getId());
        assertNull(commitPatch.getPrevious());

        RDFPatch abortPatch = RDFPatchOps.read(patchFiles.get(1).toString());
        PatchSummary abortSummary = RDFPatchOps.summary(abortPatch);
        assertEquals(1, abortSummary.getCountTxnBegin());
        assertEquals(0, abortSummary.getCountTxnCommit());
        assertEquals(1, abortSummary.getCountTxnAbort());
        assertNotNull(abortPatch.getId());
        assertEquals(commitPatch.getId(), abortPatch.getPrevious());
    }

    @Test
    public void readPromoteWithConcurrentWriter(@TempDir Path dir) throws Exception {
        ManagedOutput mout = OutputMgr.create(dir, BASE, FilePolicy.INDEX);
        CountDownLatch t1Promoted = new CountDownLatch(1);
        CountDownLatch t2Began = new CountDownLatch(1);
        RDFChangesManagedOutput changes = new RDFChangesManagedOutput(mout) {
            @Override
            public void txnBegin() {
                super.txnBegin();
                // Only the competing writer begins after promotion completes.
                if ( t1Promoted.getCount() == 0 )
                    t2Began.countDown();
            }
        };
        DatasetGraph dsg = new DatasetGraphChanges(DatasetGraphFactory.createTxnMem(), changes);

        Node s1 = NodeFactory.createURI("http://example/promoted");
        Node s2 = NodeFactory.createURI("http://example/writer");
        Node p = NodeFactory.createURI("http://example/p");
        Node o = NodeFactory.createLiteralString("v");

        AtomicReference<Throwable> error = new AtomicReference<>();

        Thread t2 = new Thread(() -> {
            try {
                assertTrue(t1Promoted.await(10, TimeUnit.SECONDS));
                Txn.executeWrite(dsg, () -> dsg.add(Quad.defaultGraphIRI, s2, p, o));
            } catch (Throwable ex) {
                error.compareAndSet(null, ex);
            }
        }, "writer-t2");

        Thread t1 = new Thread(() -> {
            try {
                dsg.begin(TxnType.READ_PROMOTE);
                try {
                    assertTrue(dsg.promote());
                    t1Promoted.countDown();
                    assertTrue(t2Began.await(10, TimeUnit.SECONDS), "Writer did not reach txnBegin");
                    dsg.add(Quad.defaultGraphIRI, s1, p, o);
                    dsg.commit();
                } finally {
                    dsg.end();
                }
            } catch (Throwable ex) {
                error.compareAndSet(null, ex);
            }
        }, "promoter-t1");

        t1.start();
        t2.start();
        t1.join(10_000);
        t2.join(10_000);

        assertFalse(t1.isAlive(), "Promoter thread deadlocked");
        assertFalse(t2.isAlive(), "Writer thread deadlocked");
        assertNull(error.get());

        List<Path> patchFiles = listPatchFiles(dir, BASE);
        assertEquals(2, patchFiles.size());

        DatasetGraph replay = DatasetGraphFactory.createTxnMem();
        Node prevId = null;
        for ( Path file : patchFiles ) {
            RDFPatch patch = RDFPatchOps.read(file.toString());
            assertEquals(prevId, patch.getPrevious());
            prevId = patch.getId();
            RDFPatchOps.applyChange(replay, patch);
        }
        assertTrue(IsoMatcher.isomorphic(dsg, replay));
    }

    @Test
    public void lazyOpenPreservesTxnOrderingForPrefixesHeaderAndSegment(@TempDir Path dir) throws Exception {
        ManagedOutput mout = OutputMgr.create(dir, BASE, FilePolicy.INDEX);
        RDFChangesManagedOutput changes = new RDFChangesManagedOutput(mout);

        // Prefix add
        changes.txnBegin();
        changes.addPrefix(null, "ex", "http://example/");
        changes.txnCommit();

        // Prefix delete
        changes.txnBegin();
        changes.deletePrefix(null, "ex");
        changes.txnCommit();

        // Custom header inside transaction
        changes.txnBegin();
        changes.header("custom", NodeFactory.createLiteralString("val"));
        changes.txnCommit();

        // Segment inside transaction
        changes.txnBegin();
        changes.segment();
        changes.txnCommit();

        List<Path> patchFiles = listPatchFiles(dir, BASE);
        assertEquals(4, patchFiles.size());

        String text0 = Files.readString(patchFiles.get(0), StandardCharsets.UTF_8);
        assertTrue(text0.indexOf("TX .") < text0.indexOf("PA "));
        PatchSummary sum0 = RDFPatchOps.summary(RDFPatchOps.read(patchFiles.get(0).toString()));
        assertEquals(1, sum0.getCountTxnBegin());
        assertEquals(1, sum0.getCountAddPrefix());
        assertEquals(1, sum0.getCountTxnCommit());

        String text1 = Files.readString(patchFiles.get(1), StandardCharsets.UTF_8);
        assertTrue(text1.indexOf("TX .") < text1.indexOf("PD "));
        PatchSummary sum1 = RDFPatchOps.summary(RDFPatchOps.read(patchFiles.get(1).toString()));
        assertEquals(1, sum1.getCountTxnBegin());
        assertEquals(1, sum1.getCountDeletePrefix());
        assertEquals(1, sum1.getCountTxnCommit());

        String text2 = Files.readString(patchFiles.get(2), StandardCharsets.UTF_8);
        assertTrue(text2.indexOf("H id ") < text2.indexOf("TX ."));
        assertTrue(text2.indexOf("TX .") < text2.indexOf("H custom "));
        PatchSummary sum2 = RDFPatchOps.summary(RDFPatchOps.read(patchFiles.get(2).toString()));
        assertEquals(1, sum2.getCountTxnBegin());
        assertEquals(1, sum2.getCountTxnCommit());

        String text3 = Files.readString(patchFiles.get(3), StandardCharsets.UTF_8);
        assertTrue(text3.indexOf("TX .") < text3.indexOf("Z ."));
        PatchSummary sum3 = RDFPatchOps.summary(RDFPatchOps.read(patchFiles.get(3).toString()));
        assertEquals(1, sum3.getCountTxnBegin());
        assertEquals(1, sum3.getCountSegment());
        assertEquals(1, sum3.getCountTxnCommit());
    }

    @Test
    public void differentThreadDuringActiveSectionThrows(@TempDir Path dir) throws Exception {
        ManagedOutput mout = OutputMgr.create(dir, BASE, FilePolicy.INDEX);
        RDFChangesManagedOutput changes = new RDFChangesManagedOutput(mout);

        Node s1 = NodeFactory.createURI("http://example/s1");
        Node s2 = NodeFactory.createURI("http://example/s2");
        Node p = NodeFactory.createURI("http://example/p");
        Node o = NodeFactory.createLiteralString("v");

        CountDownLatch t1Opened = new CountDownLatch(1);
        CountDownLatch t2Done = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Thread t1 = new Thread(() -> {
            try {
                changes.txnBegin();
                changes.add(Quad.defaultGraphIRI, s1, p, o);
                t1Opened.countDown();
                assertTrue(t2Done.await(10, TimeUnit.SECONDS));
                changes.txnCommit();
            } catch (Throwable ex) {
                error.compareAndSet(null, ex);
            }
        }, "owner-t1");

        Thread t2 = new Thread(() -> {
            try {
                assertTrue(t1Opened.await(10, TimeUnit.SECONDS));
                assertThrows(PatchException.class, () -> changes.add(Quad.defaultGraphIRI, s2, p, o));
                assertThrows(PatchException.class, changes::txnAbort);
            } catch (Throwable ex) {
                error.compareAndSet(null, ex);
            } finally {
                t2Done.countDown();
            }
        }, "other-t2");

        t1.start();
        t2.start();
        t1.join(10_000);
        t2.join(10_000);

        assertNull(error.get());
        assertEquals(1, listPatchFiles(dir, BASE).size());
    }

    @Test
    public void finishAndCloseWhenInactiveDoNotThrow(@TempDir Path dir) {
        ManagedOutput mout = OutputMgr.create(dir, BASE, FilePolicy.INDEX);
        RDFChangesManagedOutput changes = new RDFChangesManagedOutput(mout);

        assertDoesNotThrow(changes::finish);
        assertDoesNotThrow(changes::close);

        changes.txnBegin();
        changes.add(Quad.defaultGraphIRI,
                    NodeFactory.createURI("http://example/s"),
                    NodeFactory.createURI("http://example/p"),
                    NodeFactory.createLiteralString("o"));
        changes.txnCommit();

        assertDoesNotThrow(changes::finish);
        assertDoesNotThrow(changes::close);
    }
}
