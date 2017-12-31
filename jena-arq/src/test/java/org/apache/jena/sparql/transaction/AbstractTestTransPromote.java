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

package org.apache.jena.sparql.transaction ;

import static org.junit.Assert.* ;
import static org.junit.Assert.fail ;

import java.util.concurrent.* ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.system.ThreadAction ;
import org.apache.jena.system.ThreadTxn ;
import org.apache.jena.system.Txn ;
import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

/** Tests for transactions that start read and then promote to write */
public abstract class AbstractTestTransPromote {

    // Currently, this feature is off in the relevant uses in TIM and TDB.
    // It needs to be enabled via setPromotion.
    // Promotion happenes implicitly when a write happens.

    // See before() / after().

    // Loggers.
    private final Logger[] loggers ;
    private Level[] levels ;
    private boolean stdPromotion ;
    private boolean stdReadCommitted ;
    
    @Before
    public void beforeLoggersNoWarnings() {
        int N = loggers.length ;
        levels = new Level[N] ;
        for ( int i = 0 ; i < N ; i++ ) {
            levels[i] = loggers[i].getLevel() ;
            loggers[i].setLevel(Level.ERROR) ;
        }
    }

    @After
    public void afterResetLoggers() {
        int N = loggers.length ;
        for ( int i = 0 ; i < N ; i++ ) {
            loggers[i].setLevel(levels[i]) ;
        }
    }

    // The exact class used by exceptions of the system under test.
    // TDB transctions are in the TDBException hierarchy
    // so can't be JenaTransactionException.
    protected abstract Class<? extends Exception> getTransactionExceptionClass() ;
    
    protected AbstractTestTransPromote(Logger[] loggers) {
        this.loggers = loggers ;
    }
    
    protected final static Quad q1 = SSE.parseQuad("(_ :s :p1 1)") ;
    protected final static Quad q2 = SSE.parseQuad("(_ :s :p2 2)") ;
    protected final static Quad q3 = SSE.parseQuad("(_ :s :p3 3)") ;

    protected abstract DatasetGraph create() ;

    protected static void assertCount(long expected, DatasetGraph dsg) {
        dsg.begin(ReadWrite.READ) ;
        long x = Iter.count(dsg.find()) ;
        dsg.end() ;
        assertEquals(expected, x) ;
    }

    // "strict" = don't see intermedioate changes.
    // "readCommitted" = do see

    // Subclass / parameterized
    
    @Test public void promote_snapshot_01()         { run_01(TxnType.READ_PROMOTE) ; }
    @Test public void promote_readCommitted_01()    { run_01(TxnType.READ_COMMITTED_PROMOTE) ; }
    
    // READ-add
    private void run_01(TxnType txnType) {
        DatasetGraph dsg = create() ;
        dsg.begin(txnType) ;
        dsg.add(q1) ;
        dsg.commit() ;
        dsg.end() ;
    }
    
    @Test public void promote_snapshot_02()         { run_02(TxnType.READ_PROMOTE) ; }
    @Test public void promote_readCommitted_02()    { run_02(TxnType.READ_COMMITTED_PROMOTE) ; }
    
    // Previous transaction then READ-add
    private void run_02(TxnType txnType) {
        DatasetGraph dsg = create() ;
        
        dsg.begin(txnType) ;dsg.end() ;
        
        dsg.begin(txnType) ;
        dsg.add(q1) ;
        dsg.commit() ;
        dsg.end() ;
    }
    
    @Test public void promote_snapshot_03()         { run_03(TxnType.READ_PROMOTE) ; }
    @Test public void promote_readCommitted_03()    { run_03(TxnType.READ_COMMITTED_PROMOTE) ; }

    private void run_03(TxnType txnType) {
        DatasetGraph dsg = create() ;
        
        dsg.begin(TxnType.WRITE) ;dsg.commit() ; dsg.end() ;
        
        dsg.begin(txnType) ;
        dsg.add(q1) ;
        dsg.commit() ;
        dsg.end() ;
    }
    
    @Test public void promote_snapshot_04()         { run_04(TxnType.READ_PROMOTE) ; }
    @Test public void promote_readCommitted_04()    { run_04(TxnType.READ_COMMITTED_PROMOTE) ; }

    private void run_04(TxnType txnType) {
        DatasetGraph dsg = create() ;
        
        dsg.begin(ReadWrite.WRITE) ;dsg.abort() ; dsg.end() ;
        
        dsg.begin(txnType) ;
        dsg.add(q1) ;
        dsg.commit() ;
        dsg.end() ;
    }

    @Test public void promote_snapshot_05()         { run_05(TxnType.READ_PROMOTE) ; }
    @Test public void promote_readCommitted_05()    { run_05(TxnType.READ_COMMITTED_PROMOTE) ; }
    
    private void run_05(TxnType txnType) {
        DatasetGraph dsg = create() ;
        dsg.begin(txnType) ;
        dsg.add(q1) ;
        
        try {
            dsg.end() ;
            fail("begin(W);end() did not throw an exception");
        } catch ( JenaTransactionException ex) {}

        assertCount(0, dsg) ;
    }

    // LOCK UP
    //@Test public void promote_snapshot_06()         { run_06(TxnMode.READ_PROMOTE) ; }
    //@Test public void promote_readCommitted_06()    { run_06(TxnMode.READ_COMMITTED_PROMOTE) ; }
    
    // Async writer after promotion.
    private void run_06(TxnType txnType) {
        DatasetGraph dsg = create() ;
        AtomicInteger a = new AtomicInteger(0) ;

        Semaphore sema = new Semaphore(0) ;
        Thread t = new Thread(() -> {
            sema.release() ;
            Txn.executeWrite(dsg, () -> dsg.add(q3)) ;
            sema.release() ;
        }) ;

        dsg.begin(txnType) ;
        // Promote
        dsg.add(q1) ;
        t.start() ;
        // First release.
        sema.acquireUninterruptibly() ;
        // Thread blocked.
        dsg.add(q2) ;
        dsg.commit() ;
        dsg.end() ;

        // Until thread exits.
        sema.acquireUninterruptibly() ;
        assertCount(3, dsg) ;
    }

    @Test public void promote_snapshot_07()         { run_07(TxnType.READ_PROMOTE) ; }
    @Test public void promote_readCommitted_07()    { run_07(TxnType.READ_COMMITTED_PROMOTE) ; }
    
    // Async writer after promotion.
    private void run_07(TxnType txnType) {
        DatasetGraph dsg = create() ;
        // Start long running reader.
        ThreadAction tt = ThreadTxn.threadTxnRead(dsg, () -> {
            long x = Iter.count(dsg.find()) ;
            if ( x != 0 )
                throw new RuntimeException() ;
        }) ;

        // Start R->W here
        dsg.begin(txnType) ;
        dsg.add(q1) ;
        dsg.add(q2) ;
        dsg.commit() ;
        dsg.end() ;
        tt.run() ;
    }
    
    @Test public void promote_snapshot_08()         { run_08(TxnType.READ_PROMOTE); }
    @Test public void promote_readCommitted_08()    { run_08(TxnType.READ_COMMITTED_PROMOTE) ; }
    
    // Async writer after promotion trasnaction ends.
    private void run_08(TxnType txnType) {
        DatasetGraph dsg = create() ;
        // Start R->W here
        dsg.begin(txnType) ;
        dsg.add(q1) ;
        dsg.add(q2) ;
        dsg.commit() ;
        dsg.end() ;
        Txn.executeRead(dsg, () -> {
            long x = Iter.count(dsg.find()) ;
            assertEquals(2, x) ;
        }) ;
    }

    @Test
    public void promote_10() { promote_readCommit_txnCommit(TxnType.READ_COMMITTED_PROMOTE, true) ; }

    @Test
    public void promote_11() { promote_readCommit_txnCommit(TxnType.READ_COMMITTED_PROMOTE, false) ; }
    
    @Test
    public void promote_12() { 
        expect(()->promote_readCommit_txnCommit(TxnType.READ_PROMOTE, true) ,
               getTransactionExceptionClass()) ;
    }
    
    @SafeVarargs
    private final void expect(Runnable runnable, Class<? extends Exception>...classes) {
        try {
            runnable.run(); 
            fail("Exception expected") ;
        } catch (Exception e) {
            for ( Class<?> c : classes) {
                if ( e.getClass().equals(c) )
                    return ;
            }
            throw e ;
        }
    }

    @Test
    public void promote_13() { promote_readCommit_txnCommit(TxnType.READ_PROMOTE, false) ; }

    private void promote_readCommit_txnCommit(TxnType txnType, boolean asyncCommit) {
        DatasetGraph dsg = create() ;
        
        ThreadAction tt = asyncCommit?
            ThreadTxn.threadTxnWrite(dsg, () -> dsg.add(q3) ) :
            ThreadTxn.threadTxnWriteAbort(dsg, () -> dsg.add(q3)) ;

        dsg.begin(txnType) ;
        // Other runs
        tt.run() ;
        // Can promote if readCommited
        // Can't promote if not readCommited
        dsg.add(q1) ;
        if ( txnType == TxnType.READ_PROMOTE && asyncCommit )
            fail("Should not be here") ;
        // read commited - we should see the ThreadAction change.
        assertEquals(asyncCommit, dsg.contains(q3)) ;
        dsg.commit() ;
        dsg.end() ;
        //logger2.setLevel(level2);
    }
    
    // Active writer commits -> no promotion.
    @Test
    public void promote_active_writer_1() throws InterruptedException, ExecutionException {
        expect(()->promote_active_writer(true) ,
               getTransactionExceptionClass()) ;
    }
    
    // Active writer aborts -> promotion.
    @Test
    public void promote_active_writer_2() throws InterruptedException, ExecutionException {
        // Active writer aborts -> promotion possible (but not implemented that way).
        promote_active_writer(false) ;
    }
    
    private void promote_active_writer(boolean activeWriterCommit) {
        ExecutorService executor = Executors.newFixedThreadPool(2) ;
        try {
            promote_clash_active_writer(executor, activeWriterCommit) ;
        }
        finally {
            executor.shutdown() ;
        }
    }
    
    private void promote_clash_active_writer(ExecutorService executor, boolean activeWriterCommit) {
        Semaphore semaActiveWriterStart     = new Semaphore(0) ;
        Semaphore semaActiveWriterContinue  = new Semaphore(0) ;
        Semaphore semaPromoteTxnStart       = new Semaphore(0) ;
        Semaphore semaPromoteTxnContinue    = new Semaphore(0) ;

        DatasetGraph dsg = create() ;

        // The "active writer". 
        Callable<Object> activeWriter = ()->{
            dsg.begin(ReadWrite.WRITE) ;
            semaActiveWriterStart.release(1) ;
            // (*1)
            semaActiveWriterContinue.acquireUninterruptibly(1) ;
            if ( activeWriterCommit )
                dsg.commit() ;
            else
                dsg.abort();
            dsg.end() ;
            return null ;
        } ;

        Future<Object> activeWriterFuture = executor.submit(activeWriter) ;
        // Advance "active writer" to (*1), inside a write transaction and waiting.
        // The transaction has been created and started.
        semaActiveWriterStart.acquireUninterruptibly(); 

        Callable<RuntimeException> attemptedPromote = ()->{
            dsg.begin(TxnType.READ_PROMOTE) ;
            semaPromoteTxnStart.release(1) ;
            // (*2)
            semaPromoteTxnContinue.acquireUninterruptibly();
            try { 
                // (*3)
                dsg.add(q1) ;
                return null ;
            } catch (RuntimeException e) {
                Class<?> c = getTransactionExceptionClass() ;
                if ( ! e.getClass().equals(c) ) 
                    throw e ;
                return e ;
            }
        } ;

        Future<RuntimeException> attemptedPromoteFuture = executor.submit(attemptedPromote) ;
        // Advance "attempted promote" to (*2), inside a read transaction, before attempting a promoting write.
        // The transaction has been created and started.
        semaPromoteTxnStart.acquireUninterruptibly();
        
        // Advance "attempted promote" allowing it to go (*3) where it blocks
        // This may happen at any time - as soon as it does, the "attempted promote" blocks.
        semaPromoteTxnContinue.release(1);
        // I don't know of a better way to ensure "attempted promote" is blocked. 
        
        Lib.sleep(100) ;
        // Let the active writer go.
        semaActiveWriterContinue.release(1);
        
        try { 
            // Collect the active writer.
            activeWriterFuture.get();
            
            // (Ideal) and the attempted promotion should advance if the active writer aborts.
            RuntimeException e = attemptedPromoteFuture.get() ;
            if ( e != null )
                throw e ;
        } catch (InterruptedException | ExecutionException e1) { throw new RuntimeException(e1) ; }
    }
    
    // This would locks up because of a WRITE-WRITE deadly embrace.
    //  @Test(expected=JenaTransactionException.class)
    //  public void promote11() { test2(TxnMode.WRITE); }
      
    @Test
    public void promote_thread_writer_1() { test_thread_writer(TxnType.READ_COMMITTED_PROMOTE); }
      
    @Test(expected=JenaTransactionException.class)
    public void promote_thread_writer_2() { test_thread_writer(TxnType.READ_PROMOTE); }
    
    private void test_thread_writer(TxnType txnType) {
        DatasetGraph dsg = create();
        ThreadAction a = ThreadTxn.threadTxnWrite(dsg, ()->dsg.add(q1));
        dsg.begin(txnType);
        assertEquals(txnType, dsg.transactionType());
        a.run();
        dsg.add(q2);
        // TDB1 does not keep the the original TxnType.
        assertEquals(txnType, dsg.transactionType());
        assertEquals(ReadWrite.WRITE, dsg.transactionMode());
        dsg.commit();
        dsg.end();
    }
    
    // With explicit "promote()"
    private void test_promote_thread_writer(TxnType txnType) {
        DatasetGraph dsg = create();
        ThreadAction a = ThreadTxn.threadTxnWrite(dsg, ()->dsg.add(q1));
        dsg.begin(txnType);
        assertEquals(txnType, dsg.transactionType());
        a.run();
        
        boolean b = dsg.promote();
        
        if ( txnType == TxnType.READ_PROMOTE )
            assertFalse(b);
        if ( txnType == TxnType.READ_COMMITTED_PROMOTE )
            assertTrue(b);

        dsg.add(q2);
        assertEquals(txnType, dsg.transactionType());
        assertEquals(ReadWrite.WRITE, dsg.transactionMode());
        dsg.commit();
        dsg.end();
    }

}
