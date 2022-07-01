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

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.system.Txn ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.sys.SystemTDB ;
import org.apache.jena.tdb.sys.TDBInternal ;
import org.junit.* ;

/** Tests for transaction controls: batching, flushing on size, flushing on backlog of commits */ 
public class TestTransControl {

    private static String levelInfo;  
    private static String levelErr;  
    
    @BeforeClass public static void beforeClassLogging() {
        levelInfo = LogCtl.getLevel(TDB.logInfoName);
        levelErr = LogCtl.getLevel(SystemTDB.errlog.getName());
        
        LogCtl.setLevel("org.apache.jena.tdb.info", "WARN");
        LogCtl.setLevel("org.apache.jena.tdb.exec", "WARN");
        
    }
    @AfterClass public static void afterClassLogging() {
        LogCtl.setLevel(TDB.logInfoName, levelInfo);
        LogCtl.setLevel(SystemTDB.errlog.getName(), levelErr);
    }
    
    private static int x_QueueBatchSize ;
    private static int x_MaxQueueThreshold ;
    private static int x_JournalThresholdSize ;

    @BeforeClass
    static public void beforeClass() {
        x_QueueBatchSize = TransactionManager.QueueBatchSize ;
        x_MaxQueueThreshold = TransactionManager.MaxQueueThreshold ;
        x_JournalThresholdSize = TransactionManager.JournalThresholdSize ;
    }

    @AfterClass
    static public void afterClass() {
        TransactionManager.QueueBatchSize = x_QueueBatchSize ;
        TransactionManager.MaxQueueThreshold = x_MaxQueueThreshold ;
        TransactionManager.JournalThresholdSize = x_JournalThresholdSize ;
        

    }

    @Before
    public void before() {
        // Set all "off"
        TransactionManager.QueueBatchSize = 1000 ;
        TransactionManager.MaxQueueThreshold = -1 ;
        TransactionManager.JournalThresholdSize = -1 ;
    }

    @After
    public void after() {
    }
    
    
    private static Quad q1 = SSE.parseQuad("(_ :s :p1 1)") ;
    private static Quad q2 = SSE.parseQuad("(_ :s :p2 2)") ;
    private static Quad q3 = SSE.parseQuad("(_ :s :p3 3)") ;

    protected DatasetGraph create() {
        return TDBFactory.createDatasetGraph() ;
    }

    // ---- JournalThresholdSize

    // Flush on journal size / no spill. 
    @Test public void journalThresholdSize_01() {
        TransactionManager.QueueBatchSize = 100 ;
        TransactionManager.MaxQueueThreshold = -1 ;
        TransactionManager.JournalThresholdSize = 1000 ; // More than commit size, less than a block. 
        DatasetGraph dsg = create() ;
        TransactionManager tMgr = TDBInternal.getTransactionManager(dsg) ;

        Txn.executeWrite(dsg,  ()->{});    // About 20 bytes.
        assertEquals(1, tMgr.getQueueLength()) ;
    }
    
    // Flush on journal size / small setting.
    @Test public void journalThresholdSize_02() {
        TransactionManager.QueueBatchSize = 100 ;
        TransactionManager.MaxQueueThreshold = -1 ;
        TransactionManager.JournalThresholdSize = 10 ; // Less than commit size. 
        DatasetGraph dsg = create() ;
        TransactionManager tMgr = TDBInternal.getTransactionManager(dsg) ;
        
        txnAddData(dsg) ;
        assertEquals(0, tMgr.getQueueLength()) ;
    }

    // Intermediate flush journal size.
    @Test public void journalThresholdSize_03() {
        TransactionManager.QueueBatchSize = 100 ;
        TransactionManager.MaxQueueThreshold = -1 ;
        TransactionManager.JournalThresholdSize = 1000 ; // More than commit size, less than block.
        
        DatasetGraph dsg = create() ;
        TransactionManager tMgr = TDBInternal.getTransactionManager(dsg) ;
        
        Txn.executeWrite(dsg,  ()->{});    // About 20 bytes.
        assertEquals(1, tMgr.getQueueLength()) ;
        txnAddData(dsg) ;
        assertEquals(0, tMgr.getQueueLength()) ;
    }
    
    // ---- QueueBatchSize
    
    @Test public void queueBatchSize_01() {
        TransactionManager.QueueBatchSize = 0 ; // Immediate.
        
        DatasetGraph dsg = create() ;
        TransactionManager tMgr = TDBInternal.getTransactionManager(dsg) ;

        Txn.executeWrite(dsg,  ()->{});
        assertEquals(0, tMgr.getQueueLength()) ;
        Txn.executeWrite(dsg,  ()->{});
        assertEquals(0, tMgr.getQueueLength()) ;
    }

    @Test public void queueBatchSize_02() {
        TransactionManager.QueueBatchSize = 1 ;
 
        DatasetGraph dsg = create() ;
        TransactionManager tMgr = TDBInternal.getTransactionManager(dsg) ;

        Txn.executeWrite(dsg,  ()->{});
        assertEquals(1, tMgr.getQueueLength()) ;
        Txn.executeWrite(dsg,  ()->{});
        assertEquals(0, tMgr.getQueueLength()) ;
    }

    @Test public void queueBatchSize_03() {
        TransactionManager.QueueBatchSize = 2 ;

        DatasetGraph dsg = create() ;
        TransactionManager tMgr = TDBInternal.getTransactionManager(dsg) ;

        txnAddData(dsg) ;
        assertEquals(1, tMgr.getQueueLength()) ;
        txnAddData(dsg) ;
        assertEquals(2, tMgr.getQueueLength()) ;
        txnAddData(dsg) ;
        assertEquals(0, tMgr.getQueueLength()) ;
    }

    // ---- MaxQueueThreshold
    
    @Test public void maxQueueThreshold_01() {
        TransactionManager.MaxQueueThreshold = 1 ;
        
        DatasetGraph dsg = create() ;
        TransactionManager tMgr = TDBInternal.getTransactionManager(dsg) ;

        Txn.executeWrite(dsg,  ()->{});
        assertEquals(1, tMgr.getQueueLength()) ;
        Txn.executeWrite(dsg,  ()->{});
        assertEquals(0, tMgr.getQueueLength()) ;
    }
    
    @Test public void maxQueueThreshold_02() {
        TransactionManager.MaxQueueThreshold = 2 ;
 
        DatasetGraph dsg = create() ;
        TransactionManager tMgr = TDBInternal.getTransactionManager(dsg) ;

        txnAddData(dsg) ;
        assertEquals(1, tMgr.getQueueLength()) ;
        txnAddData(dsg) ;
        assertEquals(2, tMgr.getQueueLength()) ;
        Txn.executeWrite(dsg,  ()->{});
        assertEquals(0, tMgr.getQueueLength()) ;
    }

    private static void txnAddData(DatasetGraph dsg) {
        // Unique blank node.
        Quad q = SSE.parseQuad("(_ _:b :p 1)") ;
        Txn.executeWrite(dsg,  ()->dsg.add(q));
    }
}
