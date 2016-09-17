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

import static org.apache.jena.query.ReadWrite.READ ;
import static org.apache.jena.query.ReadWrite.WRITE ;

import java.util.ArrayList ;
import java.util.List ;
import java.util.concurrent.* ;
import java.util.concurrent.atomic.AtomicLong ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.JenaTransactionException ;
import static org.junit.Assume.* ;
import org.junit.Test ;

public abstract class AbstractTestTransactionLifecycle extends BaseTest
{
    protected abstract Dataset create() ;
    
    protected boolean supportsAbort() { return true ; } 

    @Test
    public void transaction_00() {
        Dataset ds = create() ;
        assertTrue(ds.supportsTransactions()) ;
    }

    @Test
    public void transaction_r01() {
        Dataset ds = create() ;
        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ;
        ds.end() ;
        assertFalse(ds.isInTransaction()) ;
    }

    @Test
    public void transaction_r02() {
        Dataset ds = create() ;
        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ;
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
        assertFalse(ds.isInTransaction()) ;
    }

    @Test
    public void transaction_r03() {
        Dataset ds = create() ;
        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ;
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
        assertFalse(ds.isInTransaction()) ;
    }

    @Test
    public void transaction_w01() {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ;
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
    }

    @Test
    public void transaction_w02() {
        assumeTrue(supportsAbort()) ;
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ;
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ;
    }

    @Test
    public void transaction_w03() {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ;
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
        assertFalse(ds.isInTransaction()) ;
    }

    @Test
    public void transaction_w04() {
        assumeTrue(supportsAbort()) ;
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ;
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
        assertFalse(ds.isInTransaction()) ;
    }

    @Test
    public void transaction_w05() {
        assumeTrue(supportsAbort()) ;
        // .end is not necessary
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ;
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ;

        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ;
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ;
    }

    // Patterns.
    @Test
    public void transaction_pattern_01() {
        Dataset ds = create() ;
        read1(ds) ;
        read1(ds) ;
    }

    @Test
    public void transaction_pattern_02() {
        Dataset ds = create() ;
        read2(ds) ;
        read2(ds) ;
    }

    @Test
    public void transaction_pattern_03() {
        Dataset ds = create() ;
        write(ds) ;
        write(ds) ;
    }

    @Test
    public void transaction_pattern_04() {
        Dataset ds = create() ;
        write(ds) ;
        read2(ds) ;
        read2(ds) ;
        write(ds) ;
        read2(ds) ;
    }
    
    // Cycle misalignment.
    // test : commit
    // test : abort
    // Permit explain .end() - the case of "end" when not sure:  begin...end.end. 
    
    @Test(expected=JenaTransactionException.class)
    public void transaction_err_nontxn_commit_1() { 
        Dataset ds = create() ;
        ds.commit() ;
    }    
    
    @Test(expected=JenaTransactionException.class)
    public void transaction_err_nontxn_commit_2() { 
        Dataset ds = create() ;
        ds.begin(READ) ;
        ds.end() ;
        ds.commit() ;
    }    
    
    @Test(expected=JenaTransactionException.class)
    public void transaction_err_nontxn_commit_3() { 
        Dataset ds = create() ;
        ds.begin(WRITE) ;
        ds.end() ;
        ds.commit() ;
    }    

    @Test(expected=JenaTransactionException.class)
    public void transaction_err_nontxn_abort_1() { 
        Dataset ds = create() ;
        ds.abort() ;
    }    

    @Test(expected=JenaTransactionException.class)
    public void transaction_err_nontxn_abort_2() { 
        Dataset ds = create() ;
        ds.begin(READ) ;
        ds.end() ;
        ds.abort() ;
    }    

    @Test(expected=JenaTransactionException.class)
    public void transaction_err_nontxn_abort_3() { 
        Dataset ds = create() ;
        ds.begin(WRITE) ;
        ds.end() ;
        ds.abort() ;
    }    
    
    @Test
    public void transaction_err_01()    { testBeginBegin(WRITE, WRITE) ; }

    @Test
    public void transaction_err_02()    { testBeginBegin(WRITE, READ) ; }

    @Test
    public void transaction_err_03()    { testBeginBegin(READ, READ) ; }

    @Test
    public void transaction_err_04()    { testBeginBegin(READ, WRITE) ; }

    @Test 
    public void transaction_err_05()    { testCommitCommit(READ) ; }

    @Test 
    public void transaction_err_06()    { testCommitCommit(WRITE) ; }

    @Test 
    public void transaction_err_07()    { testCommitAbort(READ) ; }

    @Test 
    public void transaction_err_08()    { testCommitAbort(WRITE) ; }

    @Test 
    public void transaction_err_09()    { testAbortAbort(READ) ; }

    @Test 
    public void transaction_err_10()    { testAbortAbort(WRITE) ; }

    @Test 
    public void transaction_err_11()    { testAbortCommit(READ) ; }

    @Test 
    public void transaction_err_12()    { testAbortCommit(WRITE) ; }

    private void read1(Dataset ds) {
        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ;
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
    }

    private void read2(Dataset ds) {
        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ;
        ds.end() ;
        assertFalse(ds.isInTransaction()) ;
    }

    private void write(Dataset ds) {
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ;
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
    }

    private static void safeEnd(Dataset ds) {
        try { ds.end() ; } catch (JenaTransactionException ex) {}
    }
    
    // Error conditions that should be detected.

    private void testBeginBegin(ReadWrite mode1, ReadWrite mode2) {
        Dataset ds = create() ;
        ds.begin(mode1) ;
        try {
            ds.begin(mode2) ;
            fail("Expected transaction exception - begin-begin (" + mode1 + ", " + mode2 + ")") ;
        }
        catch (JenaTransactionException ex) {
            safeEnd(ds) ;
        }
    }
    
    private void testCommitCommit(ReadWrite mode) {
        Dataset ds = create() ;
        ds.begin(mode) ;
        ds.commit() ;
        try {
            ds.commit() ;
            fail("Expected transaction exception - commit-commit(" + mode + ")") ;
        }
        catch (JenaTransactionException ex) {
            safeEnd(ds) ;
        }
    }

    private void testCommitAbort(ReadWrite mode) {
        assumeTrue(supportsAbort()) ;
        Dataset ds = create() ;
        ds.begin(mode) ;
        ds.commit() ;
        try {
            ds.abort() ;
            fail("Expected transaction exception - commit-abort(" + mode + ")") ;
        }
        catch (JenaTransactionException ex) {
            safeEnd(ds) ;
        }
    }

    private void testAbortAbort(ReadWrite mode) {
        assumeTrue(supportsAbort()) ;
        Dataset ds = create() ;
        ds.begin(mode) ;
        ds.abort() ;
        try {
            ds.abort() ;
            fail("Expected transaction exception - abort-abort(" + mode + ")") ;
        }
        catch (JenaTransactionException ex) {
            ds.end() ;
        }
    }

    private void testAbortCommit(ReadWrite mode) {
        assumeTrue(supportsAbort()) ;
        Dataset ds = create() ;
        ds.begin(mode) ;
        ds.abort() ;
        try {
            ds.commit() ;
            fail("Expected transaction exception - abort-commit(" + mode + ")") ;
        }
        catch (JenaTransactionException ex) {
            safeEnd(ds) ;
        }
    }

    // ---- Concurrency tests.
    @Test
    public synchronized void transaction_concurrency_writer() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        AtomicLong counter = new AtomicLong(0) ;
        try {
            final Dataset ds = create() ;

            Callable<Boolean> callable = new Callable<Boolean>() {

                @Override
                public Boolean call() {
                    ds.begin(ReadWrite.WRITE);
                    long x = counter.incrementAndGet() ;
                    // Hold the lock for a short while.
                    // The W threads will take the sleep serially.
                    Lib.sleep(500) ;
                    long x1 = counter.get() ;
                    assertEquals("Two writers in the transaction", x, x1);
                    ds.commit();
                    return true;
                }
            };

            // Fire off two threads
            Future<Boolean> f1 = executor.submit(callable);
            Future<Boolean> f2 = executor.submit(callable);
            // Wait longer than the cumulative threads sleep
            assertTrue(f1.get(4, TimeUnit.SECONDS));
            assertTrue(f2.get(1, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public synchronized void transaction_concurrency_reader() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newCachedThreadPool();
        AtomicLong counter = new AtomicLong(0) ;
        
        try {
            final Dataset ds = create() ;

            Callable<Boolean> callable = new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    ds.begin(ReadWrite.READ);
                    long x = counter.incrementAndGet() ;
                    // Hold the lock for a few seconds - these should be in parallel.
                    Lib.sleep(1000) ;
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

