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

package com.hp.hpl.jena.tdb.transaction;


import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** Basic tests and tests of ordering (single thread) */
public abstract class AbstractTestTransSeq extends BaseTest
{
    @BeforeClass public static void beforeClassLoggingOff() { LogCtl.disable(SystemTDB.errlog.getName()) ; } 
    @AfterClass public static void afterClassLoggingOn()    { LogCtl.setInfo(SystemTDB.errlog.getName()) ; }
    
    // Per test unique-ish.
    long x = System.currentTimeMillis() ;

    Quad q  = SSE.parseQuad("(<g> <s> <p> '0-"+x+"') ") ;
    Quad q1 = SSE.parseQuad("(<g> <s> <p> '1-"+x+"')") ;
    Quad q2 = SSE.parseQuad("(<g> <s> <p> '2-"+x+"')") ;
    Quad q3 = SSE.parseQuad("(<g> <s> <p> '3-"+x+"')") ;
    Quad q4 = SSE.parseQuad("(<g> <s> <p> '4-"+x+"')") ;
   
    private StoreConnection sConn ;

    protected abstract StoreConnection getStoreConnection() ;

    // Basics.
    
    
    
    @Test public void trans_01()
    {
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.READ) ;
        dsg.end() ;
    }
    

    @Test public void trans_02()
    {
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
        try {
            dsg.add(q) ;
            assertTrue(dsg.contains(q)) ;
            dsg.commit() ;
        } finally { dsg.end() ; }
    }
    
    @Test public void trans_03()
    {
        // WRITE-commit-READ-check
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgW = sConn.begin(ReadWrite.WRITE) ;
        
        dsgW.add(q) ;
        assertTrue(dsgW.contains(q)) ;
        dsgW.commit() ;
        dsgW.end() ;
        
        DatasetGraphTxn dsg2 = sConn.begin(ReadWrite.READ) ;
        assertTrue(dsg2.contains(q)) ;
        dsg2.end() ;
        
        sConn.flush() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        assertTrue(dsg.contains(q)) ;
        
    }
    
    @Test public void trans_04()
    {
        // WRITE-abort-READ-check
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgW = sConn.begin(ReadWrite.WRITE) ;
        
        dsgW.add(q) ;
        assertTrue(dsgW.contains(q)) ;
        dsgW.abort() ;
        dsgW.end() ;
        
        DatasetGraphTxn dsg2 = sConn.begin(ReadWrite.READ) ;
        assertFalse(dsg2.contains(q)) ;
        dsg2.end() ;
        
        sConn.flush() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        assertFalse(dsg.contains(q)) ;
    }
    
    @Test public void trans_05()
    {
        // WRITE(commit)-WRITE(commit)-READ-check
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.add(q1) ;
        dsgW1.commit() ;
        dsgW1.end() ;

        DatasetGraphTxn dsgW2 = sConn.begin(ReadWrite.WRITE) ;
        dsgW2.add(q2) ;
        dsgW2.commit() ;
        dsgW2.end() ;

        DatasetGraphTxn dsgR2 = sConn.begin(ReadWrite.READ) ;
        assertTrue(dsgR2.contains(q1)) ;
        assertTrue(dsgR2.contains(q2)) ;
        dsgR2.end() ;
        
        sConn.flush() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        assertTrue(dsg.contains(q1)) ;
        assertTrue(dsg.contains(q2)) ;
    }
    
    @Test public void trans_06()
    {
        // READ(start)-READ(finish)-WRITE(start)-WRITE(commit)-check
        StoreConnection sConn = getStoreConnection() ;
        
        DatasetGraphTxn dsgR2 = sConn.begin(ReadWrite.READ) ;
        assertFalse(dsgR2.contains(q1)) ;
        assertFalse(dsgR2.contains(q2)) ;
        dsgR2.end() ;
        
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.add(q1) ;
        dsgW1.add(q2) ;
        dsgW1.commit() ;
        dsgW1.end() ;

        sConn.forceRecoverFromJournal() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        assertTrue(dsg.contains(q1)) ;
        assertTrue(dsg.contains(q2)) ;
    }


    
    @Test public void trans_readBlock_01()
    {
        // READ(start)-WRITE(commit)-READ(finish)-check
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        
        DatasetGraphTxn dsgW = sConn.begin(ReadWrite.WRITE) ;
        
        dsgW.add(q) ;
        dsgW.commit() ;
        dsgW.end() ;
        
        assertFalse(dsgR1.contains(q)) ;
        dsgR1.end() ;

        //**** Not hitting the queue ****
        // Order of tweaking counters?
        // also writer and counters?
        
        DatasetGraphTxn dsgR2 = sConn.begin(ReadWrite.READ) ;
        assertTrue(dsgR2.contains(q)) ;
        dsgR2.end() ;
        
        sConn.flush() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        assertTrue(dsg.contains(q)) ;
    }

    
    @Test public void trans_readBlock_02()
    {
        // READ(start)-WRITE(abort)-READ(finish)-check
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        
        DatasetGraphTxn dsgW = sConn.begin(ReadWrite.WRITE) ;
        
        dsgW.add(q) ;
        dsgW.abort() ;
        dsgW.end() ;
        
        assertFalse(dsgR1.contains(q)) ;
        dsgR1.end() ;

        DatasetGraphTxn dsgR2 = sConn.begin(ReadWrite.READ) ;
        assertFalse(dsgR2.contains(q)) ;
        dsgR2.end() ;
        
        DatasetGraph dsg = sConn.getBaseDataset() ;
        assertFalse(dsg.contains(q)) ;
    }
    
    @Test public void trans_readBlock_03()
    {
        // READ(start)-WRITE(commit)-WRITE(commit)-READ(finish)-check
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.add(q1) ;
        dsgW1.commit() ;
        dsgW1.end() ;

        assertFalse(dsgR1.contains(q1)) ;
        
        DatasetGraphTxn dsgW2 = sConn.begin(ReadWrite.WRITE) ;
        dsgW2.add(q2) ;
        dsgW2.commit() ;
        dsgW2.end() ;

        assertFalse(dsgR1.contains(q1)) ;
        assertFalse(dsgR1.contains(q2)) ;

        dsgR1.end() ;

        DatasetGraphTxn dsgR2 = sConn.begin(ReadWrite.READ) ;
        assertTrue(dsgR2.contains(q1)) ;
        assertTrue(dsgR2.contains(q2)) ;
        dsgR2.end() ;
        
        sConn.flush() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        assertTrue(dsg.contains(q1)) ;
        assertTrue(dsg.contains(q2)) ;
    }

    @Test
    public void trans_readBlock_04()
    {
        // READ(block)-WRITE(abort)-WRITE(commit)-READ(close)-check
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        
        DatasetGraphTxn dsgW2 = sConn.begin(ReadWrite.WRITE) ;
        dsgW2.add(q2) ;
        dsgW2.abort() ; // ABORT
        dsgW2.end() ;
        assertFalse(dsgR1.contains(q2)) ;

        DatasetGraphTxn dsgW3 = sConn.begin(ReadWrite.WRITE) ;
        dsgW3.add(q3) ;
        // Can see W1
        assertFalse(dsgW3.contains(q2)) ;
        dsgW3.commit() ;
        dsgW3.end() ;
        assertFalse(dsgR1.contains(q3)) ;
        
        dsgR1.end() ;
        
        sConn.flush() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        assertFalse(dsg.contains(q2)) ;
        assertTrue(dsg.contains(q3)) ;
    }


    @Test
    public void trans_readBlock_05()
    {
        // READ(block)-WRITE(commit)-WRITE(abort)-WRITE(commit)-READ(close)-check
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.add(q1) ;
        dsgW1.commit() ;
        dsgW1.end() ;
        assertFalse(dsgR1.contains(q1)) ;
        
        DatasetGraphTxn dsgW2 = sConn.begin(ReadWrite.WRITE) ;
        dsgW2.add(q2) ;
        dsgW2.abort() ; // ABORT
        dsgW2.end() ;
        assertFalse(dsgR1.contains(q2)) ;

        DatasetGraphTxn dsgW3 = sConn.begin(ReadWrite.WRITE) ;
        dsgW3.add(q3) ;
        // Can see W1
        assertTrue(dsgW3.contains(q1)) ;
        assertFalse(dsgW3.contains(q2)) ;
        dsgW3.commit() ;
        dsgW3.end() ;
        assertFalse(dsgR1.contains(q3)) ;
        
        dsgR1.end() ;
        
        sConn.flush() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        assertTrue(dsg.contains(q1)) ;
        assertFalse(dsg.contains(q2)) ;
        assertTrue(dsg.contains(q3)) ;
    }

    @Test public void trans_readBlock_06()
    {
        // WRITE(start)-READ(start)-WRITE(commit)-READ sees old DSG.
        // READ before WRITE remains seeing old view - READ after WRITE starts 

        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgW = sConn.begin(ReadWrite.WRITE) ;
        DatasetGraphTxn dsgR = sConn.begin(ReadWrite.READ) ;
        
        dsgW.add(q) ;
        dsgW.commit() ;
        dsgW.end() ;
        
        assertFalse(dsgR.contains(q)) ;
        dsgR.end() ;

        DatasetGraphTxn dsgR2 = sConn.begin(ReadWrite.READ) ;
        assertTrue(dsgR2.contains(q)) ;
        dsgR2.end() ;
        
        sConn.flush() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        assertTrue(dsg.contains(q)) ;
    }

    @Test public void trans_readBlock_07()
    {
        // WRITE(start)-READ(start)-add-WRITE(commit)-READ sees old DSG.
        // READ before WRITE remains seeing old view - READ after WRITE starts 

        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgW = sConn.begin(ReadWrite.WRITE) ;
        dsgW.add(q) ;

        DatasetGraphTxn dsgR = sConn.begin(ReadWrite.READ) ;
        dsgW.commit() ;
        dsgW.end() ;
        
        assertFalse(dsgR.contains(q)) ;
        dsgR.end() ;
        
        DatasetGraphTxn dsgR2 = sConn.begin(ReadWrite.READ) ;
        assertTrue(dsgR2.contains(q)) ;
        dsgR2.end() ;
        
        sConn.flush() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        assertTrue(dsg.contains(q)) ;
    }

    @Test public void trans_readBlock_08()
    {
        // WRITE(start)-add-READ(start)-WRITE(commit)-READ sees old DSG.
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgW = sConn.begin(ReadWrite.WRITE) ;
        dsgW.add(q) ;
        
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        assertFalse(dsgR1.contains(q)) ;  
        
        dsgW.commit() ;
        dsgW.end() ;
        
        DatasetGraphTxn dsgR2 = sConn.begin(ReadWrite.READ) ;
        
        assertFalse(dsgR1.contains(q)) ;    // Before view
        assertTrue(dsgR2.contains(q)) ;     // After view
        dsgR1.end() ;
        dsgR2.end() ;
        
        sConn.flush() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        assertTrue(dsg.contains(q)) ;
    }

    @Test public void trans_readBlock_09()
    {
        // WRITE(commit)-READ(start)-WRITE(commit)-READ(finish)-check
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.add(q1) ;
        dsgW1.commit() ;
        dsgW1.end() ;

        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        DatasetGraphTxn dsgW2 = sConn.begin(ReadWrite.WRITE) ;
        dsgW2.add(q2) ;
        dsgW2.commit() ;
        dsgW2.end() ;

        DatasetGraphTxn dsgR2 = sConn.begin(ReadWrite.READ) ;
        assertTrue(dsgR1.contains(q1)) ;
        assertFalse(dsgR1.contains(q2)) ;
        
        assertTrue(dsgR2.contains(q1)) ;
        assertTrue(dsgR2.contains(q2)) ;
        
        dsgR1.end() ;
        dsgR2.end() ;
        
        sConn.flush() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        assertTrue(dsg.contains(q1)) ;
        assertTrue(dsg.contains(q2)) ;
    }
    
    @Test public void trans_readBlock_10()
    {
        // READ(start)-WRITE(start)-WRITE(finish)-WRITE(start)-READ(finish)-WRITE(finish)-check
        StoreConnection sConn = getStoreConnection() ;

        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.add(q1) ;
        dsgW1.commit() ;
        dsgW1.end() ;
        
        DatasetGraphTxn dsgW2 = sConn.begin(ReadWrite.WRITE) ;
        dsgW2.add(q2) ;
        dsgR1.end() ;

        dsgW2.commit() ;
        dsgW2.end() ;
        
        sConn.forceRecoverFromJournal() ;
        DatasetGraphTDB dsg = sConn.getBaseDataset() ; 
        assertTrue(dsg.contains(q1)) ;
        assertTrue(dsg.contains(q2)) ;
    }

    @Test public void trans_readBlock_11()
    {
        // JENA-91
        // READ(start)-WRITE-WRITE-WRITE-READ(finish)-check
        StoreConnection sConn = getStoreConnection() ;

        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.add(q1) ;
        dsgW1.commit() ;
        dsgW1.end() ;
        
        DatasetGraphTxn dsgW2 = sConn.begin(ReadWrite.WRITE) ;
        dsgW2.add(q2) ;
        dsgW2.commit() ;
        dsgW2.end() ;

        DatasetGraphTxn dsgW3 = sConn.begin(ReadWrite.WRITE) ;
        dsgW3.add(q3) ;
        dsgW3.commit() ;
        dsgW3.end() ;

        dsgR1.end() ;
        
        sConn.flush() ;
        DatasetGraphTDB dsg = sConn.getBaseDataset() ; 
        assertTrue(dsg.contains(q1)) ;
        assertTrue(dsg.contains(q2)) ;
        assertTrue(dsg.contains(q3)) ;
    }
    
    
    // Not a test
    //@Test (expected=TDBTransactionException.class)
    public void trans_20()
    {
        // Two WRITE : This would block.
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        DatasetGraphTxn dsgW2 = sConn.begin(ReadWrite.WRITE) ;
    }
    
    @Test (expected=TDBException.class) 
    public void trans_21()
    {
        // READ-add
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.READ) ;
        dsg.add(q) ;
    }
    
    @Test //(expected=TDBException.class) 
    public void trans_22()
    {
        // WRITE-close causes implicit abort
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
        dsg.add(q) ;
        dsg.end() ;
    }
    
    //@Test 
    public void trans_30()
    {
        // WRITE lots
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
        for ( int i = 0 ; i < 600 ; i++ )
        {
            Quad q = SSE.parseQuad("(_ <s> <p> "+i+")") ;
            dsg.add(q) ;
        }
        dsg.commit() ;
        dsg.end() ;
    }
}
