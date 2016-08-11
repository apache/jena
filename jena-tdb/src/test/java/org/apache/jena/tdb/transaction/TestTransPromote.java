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

package org.apache.jena.tdb.transaction;

import static org.junit.Assert.* ;

import java.util.concurrent.Semaphore ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.system.ThreadTxn ;
import org.apache.jena.system.Txn ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.sys.SystemTDB ;
import org.apache.jena.tdb.transaction.DatasetGraphTransaction ;
import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

/** Tests for transactions that start read and then promote to write */ 
public class TestTransPromote {

    // Currently,
    //    this feature is off and needs enabling via DatasetGraphTransaction.promotion
    //    promotiion is implicit whe a write happens.  
    
    
    
    // See beforeClass / afterClass.
    
    private static Logger logger = Logger.getLogger(SystemTDB.errlog.getName()) ;
    private static Level  level ;
    static boolean oldPromotion ;
    
    @BeforeClass static public void beforeClass() {
        oldPromotion = DatasetGraphTransaction.promotion ;
        DatasetGraphTransaction.promotion = true ;
        level  = logger.getLevel() ;
        //logger.setLevel(Level.ERROR) ;
    }
    
    @AfterClass static public void afterClass() {
        // Restore logging setting.
        logger.setLevel(level); 
        DatasetGraphTransaction.promotion = oldPromotion ;
    }
    
    private static Quad q1 = SSE.parseQuad("(_ :s :p1 1)") ;
    private static Quad q2 = SSE.parseQuad("(_ :s :p2 2)") ;
    private static Quad q3 = SSE.parseQuad("(_ :s :p3 3)") ;
    
    protected DatasetGraph create() { return TDBFactory.createDatasetGraph() ; } 
    
    protected static void assertCount(long expected, DatasetGraph dsg) {
        dsg.begin(ReadWrite.READ);
        long x = Iter.count(dsg.find()) ;
        dsg.end() ;
        assertEquals(expected, x) ;
    }
    
    @Test public void promote_01() {
        DatasetGraph dsg = create() ;
        dsg.begin(ReadWrite.READ); 
        dsg.add(q1) ;
        dsg.commit();
        dsg.end() ;
    }
    
    @Test public void promote_02() {
        DatasetGraph dsg = create() ;
        dsg.begin(ReadWrite.READ); 
        dsg.add(q1) ;
        dsg.add(q2) ;
        dsg.commit();
        dsg.end() ;
        assertCount(2, dsg) ;
    }

    // Causes the warning.
    @Test public void promote_03() {
        DatasetGraph dsg = create() ;
        dsg.begin(ReadWrite.READ); 
        dsg.add(q1) ;
        
        // bad - forced abort.
        // Causes a WARN.
        logger.setLevel(Level.ERROR) ;
        dsg.end() ;
        logger.setLevel(level)  ;
        
        assertCount(0, dsg) ;
    }
    
    @Test public void promote_04() {
        DatasetGraph dsg = create() ;
        AtomicInteger a = new AtomicInteger(0) ;
        
        Semaphore sema = new Semaphore(0) ;
        Thread t = new Thread(()->{
            sema.release();
            Txn.execWrite(dsg, ()->dsg.add(q3)) ;   
            sema.release();
        }) ;
        
        dsg.begin(ReadWrite.READ);
        // Promote
        dsg.add(q1) ;
        t.start(); 
        // First release.
        sema.acquireUninterruptibly();
        // Thread blocked. 
        dsg.add(q2) ;
        dsg.commit();
        dsg.end() ;
        
        // Until thread exits.
        sema.acquireUninterruptibly();
        assertCount(3, dsg) ;
    }
    
    @Test public void promote_05() {
        DatasetGraph dsg = create() ;
        // Start long running reader.
        ThreadTxn tt = ThreadTxn.threadTxnRead(dsg, ()->{
            long x = Iter.count(dsg.find()) ;
            if ( x != 0 ) 
                throw new RuntimeException() ;
        }) ;
    
        // Start R->W here
        dsg.begin(ReadWrite.READ); 
        dsg.add(q1) ;
        dsg.add(q2) ;
        dsg.commit();
        dsg.end() ;
        tt.run();
    }
    
    @Test public void promote_06() {
        promoteRC(true) ;
    }
        
    @Test(expected=TDBTransactionException.class)
    public void promote_07() {
        promoteRC(false) ;
    }
     
    private void promoteRC(boolean allowReadCommitted) {
        DatasetGraphTransaction.readCommittedPromotion = allowReadCommitted ;    
        DatasetGraph dsg = create() ;

        ThreadTxn tt = ThreadTxn.threadTxnWrite(dsg, ()->{dsg.add(q3) ;}) ;
        
        dsg.begin(ReadWrite.READ);
        // Other runs
        tt.run(); 
        // Can  promote if readCommited
        // Can't promote if not readCommited
        dsg.add(q1) ;
        assertTrue(dsg.contains(q3)) ;
        dsg.commit();
        dsg.end() ;
    }

}
