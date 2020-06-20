/**
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

package org.apache.jena.sparql.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.Transactional.Promote;
import org.junit.Test;

/**
 * Dataset transaction lifecycle. 
 */
public abstract class AbstractTestTransactionLifecycle
{
    protected abstract Dataset create();
    
    protected boolean supportsAbort()   { return true; } 
    protected boolean supportsPromote() { return true; }

    @Test
    public void transaction_00() {
        Dataset ds = create();
        assertTrue(ds.supportsTransactions());
    }

    @Test
    public void transaction_r01() {
        Dataset ds = create();
        ds.begin(TxnType.READ);
        assertTrue(ds.isInTransaction());
        ds.end();
        assertFalse(ds.isInTransaction());
    }

    @Test
    public void transaction_r02() {
        Dataset ds = create();
        ds.begin(TxnType.READ);
        assertTrue(ds.isInTransaction());
        ds.commit();
        assertFalse(ds.isInTransaction());
        ds.end();
        assertFalse(ds.isInTransaction());
    }

    @Test
    public void transaction_r03() {
        Dataset ds = create();
        ds.begin(TxnType.READ);
        assertTrue(ds.isInTransaction());
        ds.abort();
        assertFalse(ds.isInTransaction());
        ds.end();
        assertFalse(ds.isInTransaction());
    }

    @Test
    public void transaction_r04() {
        Dataset ds = create();
        ds.begin(ReadWrite.READ);
        assertTrue(ds.isInTransaction());
        ds.end();
        assertFalse(ds.isInTransaction());
    }

    @Test
    public void transaction_w01() {
        Dataset ds = create();
        ds.begin(TxnType.WRITE);
        assertTrue(ds.isInTransaction());
        ds.commit();
        assertFalse(ds.isInTransaction());
    }
    
    @Test
    public void transaction_w02() {
        Dataset ds = create();
        ds.begin(ReadWrite.WRITE);
        assertTrue(ds.isInTransaction());
        ds.commit();
        assertFalse(ds.isInTransaction());
    }

    @Test
    public void transaction_w03() {
        assumeTrue(supportsAbort());
        Dataset ds = create();
        ds.begin(TxnType.WRITE);
        assertTrue(ds.isInTransaction());
        ds.abort();
        assertFalse(ds.isInTransaction());
    }

    @Test
    public void transaction_w04() {
        Dataset ds = create();
        ds.begin(TxnType.WRITE);
        assertTrue(ds.isInTransaction());
        ds.commit();
        assertFalse(ds.isInTransaction());
        ds.end();
        assertFalse(ds.isInTransaction());
    }

    @Test
    public void transaction_w05() {
        assumeTrue(supportsAbort());
        Dataset ds = create();
        ds.begin(TxnType.WRITE);
        assertTrue(ds.isInTransaction());
        ds.abort();
        assertFalse(ds.isInTransaction());
        ds.end();
        assertFalse(ds.isInTransaction());
    }

    @Test
    public void transaction_w06() {
        assumeTrue(supportsAbort());
        // .end is not necessary
        Dataset ds = create();
        ds.begin(TxnType.WRITE);
        assertTrue(ds.isInTransaction());
        ds.abort();
        assertFalse(ds.isInTransaction());

        ds.begin(TxnType.WRITE);
        assertTrue(ds.isInTransaction());
        ds.abort();
        assertFalse(ds.isInTransaction());
    }

//    TxnType.READ_PROMOTE
//    TxnType.READ_COMMITTED_PROMOTE
    
    @Test
    public void transaction_p01() {
        assumeTrue(supportsPromote());
        Dataset ds = create();
        ds.begin(TxnType.READ_PROMOTE);
        assertEquals(TxnType.READ_PROMOTE, ds.transactionType());
        assertTrue(ds.isInTransaction());
        assertEquals(ReadWrite.READ, ds.transactionMode());
        ds.promote();
        assertEquals(ReadWrite.WRITE, ds.transactionMode());
        ds.commit();
        ds.end();
    }
    
    @Test
    public void transaction_p02() {
        assumeTrue(supportsPromote());
        Dataset ds = create();
        ds.begin(TxnType.READ_COMMITTED_PROMOTE);
        assertEquals(TxnType.READ_COMMITTED_PROMOTE, ds.transactionType());
        assertTrue(ds.isInTransaction());
        assertEquals(ReadWrite.READ, ds.transactionMode());
        boolean b = ds.promote();
        assertTrue(b);
        assertEquals(ReadWrite.WRITE, ds.transactionMode());
        ds.commit();
        ds.end();
    }
    
    @Test
    public void transaction_p03() {
        assumeTrue(supportsPromote());
        Dataset ds = create();
        ds.begin(TxnType.READ_PROMOTE);
        assertTrue(ds.isInTransaction());
        assertEquals(ReadWrite.READ, ds.transactionMode());
        boolean b = ds.promote();
        assertTrue(b);
        assertEquals(ReadWrite.WRITE, ds.transactionMode());
        ds.abort();
        ds.end();
        assertFalse(ds.isInTransaction());
    }

    @Test
    public void transaction_p04() {
        assumeTrue(supportsPromote());
        Dataset ds = create();
        ds.begin(TxnType.READ_COMMITTED_PROMOTE);
        assertTrue(ds.isInTransaction());
        assertEquals(ReadWrite.READ, ds.transactionMode());
        boolean b = ds.promote();
        assertTrue(b);
        assertEquals(ReadWrite.WRITE, ds.transactionMode());
        ds.abort();
        ds.end();
        assertFalse(ds.isInTransaction());
    }

    @Test
    public void transaction_p05() {
        assumeTrue(supportsPromote());
        Dataset ds = create();
        ds.begin(TxnType.READ_COMMITTED_PROMOTE);
        assertTrue(ds.isInTransaction());
        boolean b1 = ds.promote();
        assertTrue(b1);
        boolean b2 = ds.promote();
        assertTrue(b2);
        ds.commit();
        ds.end();
    }
    
    @Test
    public void transaction_p06_err() {
        assumeTrue(supportsPromote());
        Dataset ds = create();
        ds.begin(TxnType.READ);
        assertTrue(ds.isInTransaction());
        boolean b1 = ds.promote();
        assertFalse(b1);
        boolean b2 = ds.promote();
        assertFalse(b2);
        ds.end();
    }
    
    // JENA-1469
    @Test
    public void transaction_p10() {
        transaction_promote_write(TxnType.READ_COMMITTED_PROMOTE);
    }

    @Test
    public void transaction_p11() {
        transaction_promote_write(TxnType.READ_PROMOTE);
    }

    // XXX Refactor the above code.
    
    // promotion type specified
    private void testPromote(TxnType txnType , Promote promoteMode, boolean succeeds) {
        Dataset ds = create();
        ds.begin(txnType);
        assertTrue(ds.isInTransaction());
        boolean b1 = ds.promote(promoteMode);
        assertEquals(succeeds, b1);
        boolean b2 = ds.promote(promoteMode);
        assertEquals("Try same promote again", b1, b2);
        ds.commit();
        ds.end();
        
    }
    
    @Test
    public void transaction_promote_write_isolated() {
        assumeTrue(supportsPromote());
        testPromote(TxnType.WRITE, Promote.ISOLATED, true);
    }
    
    @Test
    public void transaction_promote_write_readCommitted() {
        assumeTrue(supportsPromote());
        testPromote(TxnType.WRITE, Promote.READ_COMMITTED, true);
    }
        
    @Test
    public void transaction_promote_read_isolated() {
        assumeTrue(supportsPromote());
        testPromote(TxnType.READ, Promote.ISOLATED, false);
    }
    
    @Test
    public void transaction_promote_read_readCommitted() {
        assumeTrue(supportsPromote());
        testPromote(TxnType.READ, Promote.READ_COMMITTED, false);
    }
        
    @Test
    public void transaction_promote_readPromote_isolated() {
        assumeTrue(supportsPromote());
        testPromote(TxnType.READ_PROMOTE, Promote.ISOLATED, true);
    }
    
    @Test
    public void transaction_promote_readPromote_committed() {
        assumeTrue(supportsPromote());
        testPromote(TxnType.READ_PROMOTE, Promote.READ_COMMITTED, true);
    }
        
    @Test
    public void transaction_promote_readCommitted_isolated() {
        assumeTrue(supportsPromote());
        testPromote(TxnType.READ_COMMITTED_PROMOTE, Promote.ISOLATED, true);
    }
    
    @Test
    public void transaction_promote_readCommitted_readCommitted() {
        assumeTrue(supportsPromote());
        testPromote(TxnType.READ_COMMITTED_PROMOTE, Promote.READ_COMMITTED, true);
    }
        
    
    @Test
    public void transaction_read_promote() {
        assumeTrue(supportsPromote());
        Dataset ds = create();
        ds.begin(TxnType.READ);
        boolean b = ds.promote();   // Fails
        assertFalse(b);
        ds.commit();
        ds.end();
    }

    private void transaction_promote_write(TxnType txnType) {
        Dataset ds = create();
        ds.begin(txnType);
        ds.promote();
        ds.commit();
        ds.end();
        ds.begin(TxnType.WRITE);
        ds.commit();
        ds.end(); 
    }
    
    // Patterns.
    @Test
    public void transaction_pattern_01() {
        Dataset ds = create();
        read1(ds);
        read1(ds);
    }

    @Test
    public void transaction_pattern_02() {
        Dataset ds = create();
        read2(ds);
        read2(ds);
    }

    @Test
    public void transaction_pattern_03() {
        Dataset ds = create();
        write(ds);
        write(ds);
    }

    @Test
    public void transaction_pattern_04() {
        Dataset ds = create();
        write(ds);
        read2(ds);
        read2(ds);
        write(ds);
        read2(ds);
    }
    
    // Cycle misalignment.
    // test : commit
    // test : abort
    // Permit explain .end() - the case of "end" when not sure:  begin...end.end. 
    
    @Test(expected=JenaTransactionException.class)
    public void transaction_err_nontxn_commit_1() { 
        Dataset ds = create();
        ds.commit();
    }    
    
    @Test(expected=JenaTransactionException.class)
    public void transaction_err_nontxn_commit_2() { 
        Dataset ds = create();
        ds.begin(TxnType.READ);
        ds.end();
        ds.commit();
    }    
    
    @Test(expected=JenaTransactionException.class)
    public void transaction_err_nontxn_commit_3() { 
        Dataset ds = create();
        ds.begin(TxnType.WRITE);
        ds.end();
        ds.commit();
    }    

    @Test(expected=JenaTransactionException.class)
    public void transaction_err_nontxn_abort_1() { 
        Dataset ds = create();
        ds.abort();
    }    

    @Test(expected=JenaTransactionException.class)
    public void transaction_err_nontxn_abort_2() { 
        Dataset ds = create();
        ds.begin(TxnType.READ);
        ds.end();
        ds.abort();
    }    

    @Test(expected=JenaTransactionException.class)
    public void transaction_err_nontxn_abort_3() { 
        Dataset ds = create();
        ds.begin(TxnType.WRITE);
        ds.end();
        ds.abort();
    }    
    
    @Test
    public void transaction_err_01()    { testBeginBegin(TxnType.WRITE, TxnType.WRITE); }

    @Test
    public void transaction_err_02()    { testBeginBegin(TxnType.WRITE, TxnType.READ); }

    @Test
    public void transaction_err_03()    { testBeginBegin(TxnType.READ, TxnType.READ); }

    @Test
    public void transaction_err_04()    { testBeginBegin(TxnType.READ, TxnType.WRITE); }

    @Test 
    public void transaction_err_05()    { testCommitCommit(TxnType.READ); }

    @Test 
    public void transaction_err_06()    { testCommitCommit(TxnType.WRITE); }

    @Test 
    public void transaction_err_07()    { testCommitAbort(TxnType.READ); }

    @Test 
    public void transaction_err_08()    { testCommitAbort(TxnType.WRITE); }

    @Test 
    public void transaction_err_09()    { testAbortAbort(TxnType.READ); }

    @Test 
    public void transaction_err_10()    { testAbortAbort(TxnType.WRITE); }

    @Test 
    public void transaction_err_11()    { testAbortCommit(TxnType.READ); }

    @Test 
    public void transaction_err_12()    { testAbortCommit(TxnType.WRITE); }

    private void read1(Dataset ds) {
        ds.begin(TxnType.READ);
        assertTrue(ds.isInTransaction());
        ds.commit();
        assertFalse(ds.isInTransaction());
        ds.end();
    }

    private void read2(Dataset ds) {
        ds.begin(TxnType.READ);
        assertTrue(ds.isInTransaction());
        ds.end();
        assertFalse(ds.isInTransaction());
    }

    private void write(Dataset ds) {
        ds.begin(TxnType.WRITE);
        assertTrue(ds.isInTransaction());
        ds.commit();
        assertFalse(ds.isInTransaction());
        ds.end();
    }

    private static void safeEnd(Dataset ds) {
        try { ds.end(); } catch (JenaTransactionException ex) {}
    }
    
    // Error conditions that should be detected.

    private void testBeginBegin(TxnType txnType1, TxnType txnType2) {
        Dataset ds = create();
        ds.begin(txnType1);
        try {
            ds.begin(txnType2);
            fail("Expected transaction exception - begin-begin (" + txnType1 + ", " + txnType2 + ")");
        }
        catch (JenaTransactionException ex) {
            safeEnd(ds);
        }
    }
    
    private void testCommitCommit(TxnType txnType) {
        Dataset ds = create();
        ds.begin(txnType);
        ds.commit();
        try {
            ds.commit();
            fail("Expected transaction exception - commit-commit(" + txnType + ")");
        }
        catch (JenaTransactionException ex) {
            safeEnd(ds);
        }
    }

    private void testCommitAbort(TxnType txnType) {
        assumeTrue(supportsAbort());
        Dataset ds = create();
        ds.begin(txnType);
        ds.commit();
        try {
            ds.abort();
            fail("Expected transaction exception - commit-abort(" + txnType + ")");
        }
        catch (JenaTransactionException ex) {
            safeEnd(ds);
        }
    }

    private void testAbortAbort(TxnType txnType) {
        assumeTrue(supportsAbort());
        Dataset ds = create();
        ds.begin(txnType);
        ds.abort();
        try {
            ds.abort();
            fail("Expected transaction exception - abort-abort(" + txnType + ")");
        }
        catch (JenaTransactionException ex) {
            ds.end();
        }
    }

    private void testAbortCommit(TxnType txnType) {
        assumeTrue(supportsAbort());
        Dataset ds = create();
        ds.begin(txnType);
        ds.abort();
        try {
            ds.commit();
            fail("Expected transaction exception - abort-commit(" + txnType + ")");
        }
        catch (JenaTransactionException ex) {
            safeEnd(ds);
        }
    }

    // ---- Concurrency tests.
    @Test
    public synchronized void transaction_concurrency_writer() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        AtomicLong counter = new AtomicLong(0);
        try {
            final Dataset ds = create();

            Callable<Boolean> callable = new Callable<Boolean>() {

                @Override
                public Boolean call() {
                    ds.begin(TxnType.WRITE);
                    long x = counter.incrementAndGet();
                    // Hold the lock for a short while.
                    // The W threads will take the sleep serially.
                    Lib.sleep(500);
                    long x1 = counter.get();
                    assertEquals("Two writers in the transaction", x, x1);
                    ds.commit();
                    return true;
                }
            };

            // Fire off two threads
            Future<Boolean> f1 = executor.submit(callable);
            Future<Boolean> f2 = executor.submit(callable);
            // Wait longer than the cumulative threads sleep
            assertTrue(f1.get(10, TimeUnit.SECONDS));
            assertTrue(f2.get(1, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public synchronized void transaction_concurrency_reader() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newCachedThreadPool();
        AtomicLong counter = new AtomicLong(0);
        
        try {
            final Dataset ds = create();

            Callable<Boolean> callable = new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    ds.begin(TxnType.READ);
                    long x = counter.incrementAndGet();
                    // Hold the lock for a few seconds - these should be in parallel.
                    Lib.sleep(1000);
                    ds.commit();
                    return true;
                }
            };

            // Run the callable a bunch of times
            List<Future<Boolean>> futures = new ArrayList<>();
            for (int i = 0; i < 25; i++) {
                futures.add(executor.submit(callable));
            }

            // Check all the futures come back OK
            // Wait shorter than sum total of all sleep by thread
            // which proves concurrent access.
            for (Future<Boolean> f : futures) {
                assertTrue(f.get(4, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }
    }
}

