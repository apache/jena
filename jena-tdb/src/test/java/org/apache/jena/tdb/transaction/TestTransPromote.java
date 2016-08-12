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

package org.apache.jena.tdb.transaction ;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.fail ;

import java.util.concurrent.Semaphore ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.system.ThreadTxn ;
import org.apache.jena.system.Txn ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.sys.SystemTDB ;
import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.junit.* ;

/** Tests for transactions that start read and then promote to write */
public class TestTransPromote {

    // Currently,
    // this feature is off and needs enabling via DatasetGraphTransaction.promotion
    // promotiion is implicit whe a write happens.

    // See beforeClass / afterClass.

    private static Logger logger1 = Logger.getLogger(SystemTDB.errlog.getName()) ;
    private static Level  level1 ;
    private static Logger logger2 = Logger.getLogger(TDB.logInfoName) ;
    private static Level  level2 ;
    static boolean        stdPromotion ;
    static boolean        stdReadCommitted ;

    @BeforeClass
    static public void beforeClass() {
        stdPromotion = DatasetGraphTransaction.promotion ;
        stdReadCommitted = DatasetGraphTransaction.readCommittedPromotion ;
        level1 = logger1.getLevel() ;
        level2 = logger2.getLevel() ;
        
        // logger1.setLevel(Level.ERROR) ;
        // logger2.setLevel(Level.ERROR) ;
    }

    @AfterClass
    static public void afterClass() {
        // Restore logging setting.
        logger2.setLevel(level2) ;
        logger1.setLevel(level1) ;
        DatasetGraphTransaction.promotion = stdPromotion ;
        DatasetGraphTransaction.readCommittedPromotion = stdReadCommitted ;
    }

    @Before
    public void before() {
        DatasetGraphTransaction.promotion = true ;
        DatasetGraphTransaction.readCommittedPromotion = true ;
    }

    @After
    public void after() {
        DatasetGraphTransaction.promotion = true ;
        DatasetGraphTransaction.readCommittedPromotion = true ;
    }
    
    
    private static Quad q1 = SSE.parseQuad("(_ :s :p1 1)") ;
    private static Quad q2 = SSE.parseQuad("(_ :s :p2 2)") ;
    private static Quad q3 = SSE.parseQuad("(_ :s :p3 3)") ;

    protected DatasetGraph create() {
        return TDBFactory.createDatasetGraph() ;
    }

    protected static void assertCount(long expected, DatasetGraph dsg) {
        dsg.begin(ReadWrite.READ) ;
        long x = Iter.count(dsg.find()) ;
        dsg.end() ;
        assertEquals(expected, x) ;
    }

    // "strict" = don't see intermedioate changes.
    // "readCommitted" = do see

    // Subclass / parameterized
    
    @Test public void promote_snapshot_01()         { run_01(false) ; }
    @Test public void promote_readCommitted_01()    { run_01(true) ; }
    
    // READ-add
    private void run_01(boolean b) {
        DatasetGraphTransaction.readCommittedPromotion = b ;
        DatasetGraph dsg = create() ;
        
        dsg.begin(ReadWrite.READ) ;
        dsg.add(q1) ;
        dsg.commit() ;
        dsg.end() ;
    }
    
    @Test public void promote_snapshot_02()         { run_02(false) ; }
    @Test public void promote_readCommitted_02()    { run_02(true) ; }
    
    // Previous transaction then READ-add
    private void run_02(boolean b) {
        DatasetGraphTransaction.readCommittedPromotion = b ;
        DatasetGraph dsg = create() ;
        
        dsg.begin(ReadWrite.READ) ;dsg.end() ;
        
        dsg.begin(ReadWrite.READ) ;
        dsg.add(q1) ;
        dsg.commit() ;
        dsg.end() ;
    }
    
    @Test public void promote_snapshot_03()         { run_03(false) ; }
    @Test public void promote_readCommitted_03()    { run_03(true) ; }

    private void run_03(boolean b) {
        DatasetGraphTransaction.readCommittedPromotion = b ;
        DatasetGraph dsg = create() ;
        
        dsg.begin(ReadWrite.WRITE) ;dsg.commit() ; dsg.end() ;
        
        dsg.begin(ReadWrite.READ) ;
        dsg.add(q1) ;
        dsg.commit() ;
        dsg.end() ;
    }
    
    @Test public void promote_snapshot_04()         { run_04(false) ; }
    @Test public void promote_readCommitted_04()    { run_04(true) ; }

    private void run_04(boolean b) {
        DatasetGraphTransaction.readCommittedPromotion = b ;
        DatasetGraph dsg = create() ;
        
        dsg.begin(ReadWrite.WRITE) ;dsg.abort() ; dsg.end() ;
        
        dsg.begin(ReadWrite.READ) ;
        dsg.add(q1) ;
        dsg.commit() ;
        dsg.end() ;
    }

    @Test public void promote_snapshot_05()         { run_05(false) ; }
    @Test public void promote_readCommitted_05()    { run_05(true) ; }
    
    private void run_05(boolean b) {
        DatasetGraphTransaction.readCommittedPromotion = b ;
        DatasetGraph dsg = create() ;
        dsg.begin(ReadWrite.READ) ;
        dsg.add(q1) ;

        // bad - forced abort.
        // Causes a WARN.
        logger1.setLevel(Level.ERROR) ;
        dsg.end() ;
        logger1.setLevel(level1) ;

        assertCount(0, dsg) ;
    }

    @Test public void promote_snapshot_06()         { run_06(false) ; }
    @Test public void promote_readCommitted_06()    { run_06(true) ; }
    
    // Async writer after promotion.
    private void run_06(boolean b) {
        DatasetGraphTransaction.readCommittedPromotion = b ;
        DatasetGraph dsg = create() ;
        AtomicInteger a = new AtomicInteger(0) ;

        Semaphore sema = new Semaphore(0) ;
        Thread t = new Thread(() -> {
            sema.release() ;
            Txn.execWrite(dsg, () -> dsg.add(q3)) ;
            sema.release() ;
        }) ;

        dsg.begin(ReadWrite.READ) ;
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

    @Test public void promote_snapshot_07()         { run_07(false) ; }
    @Test public void promote_readCommitted_07()    { run_07(true) ; }
    
    // Async writer after promotion.
    private void run_07(boolean b) {
        DatasetGraphTransaction.readCommittedPromotion = b ;
        DatasetGraph dsg = create() ;
        // Start long running reader.
        ThreadTxn tt = ThreadTxn.threadTxnRead(dsg, () -> {
            long x = Iter.count(dsg.find()) ;
            if ( x != 0 )
                throw new RuntimeException() ;
        }) ;

        // Start R->W here
        dsg.begin(ReadWrite.READ) ;
        dsg.add(q1) ;
        dsg.add(q2) ;
        dsg.commit() ;
        dsg.end() ;
        tt.run() ;
    }
    
    @Test public void promote_snapshot_08()         { run_08(false) ; }
    @Test public void promote_readCommitted_08()    { run_08(true) ; }
    
    // Async writer after promotion trasnaction ends.
    private void run_08(boolean b) {
        DatasetGraphTransaction.readCommittedPromotion = b ;
        DatasetGraph dsg = create() ;
        // Start R->W here
        dsg.begin(ReadWrite.READ) ;
        dsg.add(q1) ;
        dsg.add(q2) ;
        dsg.commit() ;
        dsg.end() ;
        Txn.execRead(dsg, () -> {
            long x = Iter.count(dsg.find()) ;
            assertEquals(2, x) ;
        }) ;
    }

    // Tests for XXX Read-committed yes/no, and whether the other transaction commits or aborts.
    
    @Test
    public void promote_10() { promote_readCommit_txnCommit(true, true) ; }

    @Test
    public void promote_11() { promote_readCommit_txnCommit(true, false) ; }
    
    @Test(expected = TDBTransactionException.class)
    public void promote_12() { promote_readCommit_txnCommit(false, true) ; }

    @Test
    public void promote_13() { promote_readCommit_txnCommit(false, false) ; }

    private void promote_readCommit_txnCommit(boolean allowReadCommitted, boolean asyncCommit) {
        logger2.setLevel(Level.ERROR);
        DatasetGraphTransaction.readCommittedPromotion = allowReadCommitted ;
        DatasetGraph dsg = create() ;
        
        ThreadTxn tt = asyncCommit?
            ThreadTxn.threadTxnWrite(dsg, () -> dsg.add(q3) ) :
            ThreadTxn.threadTxnWriteAbort(dsg, () -> dsg.add(q3)) ;

        dsg.begin(ReadWrite.READ) ;
        // Other runs
        tt.run() ;
        // Can promote if readCommited
        // Can't promote if not readCommited
        dsg.add(q1) ;
        if ( ! allowReadCommitted && asyncCommit )
            fail("Should not be here") ;
        
        assertEquals(asyncCommit, dsg.contains(q3)) ;
        dsg.commit() ;
        dsg.end() ;
        logger2.setLevel(level2);
    }
    
    // Test whether a writer casuses a snapshot isolation
    // promotion to fail like it should
    @Test(expected=TDBTransactionException.class)
    public void promote_clash_1() { 
        DatasetGraphTransaction.readCommittedPromotion = false ;
        DatasetGraph dsg = create() ;
        Semaphore sema1 = new Semaphore(0) ;
        Semaphore sema2 = new Semaphore(0) ;
        Runnable r = ()->{
            dsg.begin(ReadWrite.WRITE) ; 
            sema1.release(1); 
            sema2.acquireUninterruptibly(1) ;
            dsg.commit() ; 
            dsg.end() ; 
        } ;
        
        // Create a writer that waits.
        new Thread(r).start(); 
        sema1.acquireUninterruptibly(); 
        // The thread is in the write.
        dsg.begin(ReadWrite.READ) ;
        // If read commited this will block.
        // If snapshot, this will though an exception due to the active writer.
        dsg.add(q1) ;
        fail("Should not be here") ;
        dsg.commit() ;
        dsg.end() ;
    }
}
