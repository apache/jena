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

package com.hp.hpl.jena.tdb.store;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.transaction.TDBTransactionException ;

public abstract class AbstractStoreConnections extends BaseTest
{
    // Subclass to give direct and mapped versions.
    
    // Per-test unique-ish.
    static int count = 0 ;
    long x = System.currentTimeMillis()+(count++) ;
    
    Quad q  = SSE.parseQuad("(<g> <s> <p> '000-"+x+"') ") ;
    Quad q1 = SSE.parseQuad("(<g> <s> <p> '111-"+x+"')") ;
    Quad q2 = SSE.parseQuad("(<g> <s> <p> '222-"+x+"')") ;
    Quad q3 = SSE.parseQuad("(<g> <s> <p> '333-"+x+"')") ;
    Quad q4 = SSE.parseQuad("(<g> <s> <p> '444-"+x+"')") ;
    
    String DIR = null ;

    @Before public void before()
    {
        StoreConnection.reset() ;
        DIR = ConfigTest.getCleanDir() ;
    }

    @After public void after() {} 

    protected StoreConnection getStoreConnection()
    {
        return StoreConnection.make(DIR) ;
    }
    
    @Test 
    public void store_0()
    {
        // Expel.
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.commit() ;
        dsgW1.end() ;
        StoreConnection.release(sConn.getLocation()) ;
        StoreConnection sConn2 = getStoreConnection() ;
    }
    
    @Test
    public void store_1()
    {
        // Expel.
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.add(q1) ;
        dsgW1.commit() ;
        dsgW1.end() ;
        dsgR1.end();
        
        StoreConnection.release(sConn.getLocation()) ;
        sConn = null ;
        
        StoreConnection sConn2 = getStoreConnection() ;
    }
    
    @Test(expected=TDBTransactionException.class)
    public void store_2()
    {
        // Expel.
        // Only applies to non-memory.
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        StoreConnection.release(sConn.getLocation()) ;
    }

    @Test(expected=TDBTransactionException.class)
    public void store_3()
    {
        // Expel.
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.WRITE) ;
        StoreConnection.release(sConn.getLocation()) ;
    }
    
    @Test
    public void store_4()
    {
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.add(q1) ;
        dsgW1.commit() ;
        dsgW1.end() ;
        dsgR1.end();
        
        StoreConnection.release(sConn.getLocation()) ;
        sConn = null ;
        
        StoreConnection sConn2 = getStoreConnection() ;
        DatasetGraphTxn dsgW2 = sConn2.begin(ReadWrite.WRITE) ;
        dsgW2.add(q2) ;
        dsgW2.commit() ;
        dsgW2.end() ;
        
        DatasetGraphTxn dsgR2 = sConn2.begin(ReadWrite.READ) ;
        long x = Iter.count(dsgR2.find()) ;
        assertEquals(2, x) ;
    }

    @Test 
    public void store_5()
    {
        // No transaction.  Make sure StoreConnection.release cleans up OK.  
        StoreConnection sConn = getStoreConnection() ;
        Location loc = sConn.getLocation() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        dsg.add(q) ;
        assertTrue(dsg.contains(q)) ;
        
        StoreConnection.release(loc) ;
        sConn = StoreConnection.make(loc) ;
        dsg = sConn.getBaseDataset() ;
        assertTrue(dsg.contains(q)) ;
    }

    @Test 
    public void store_6()
    {
        // Transaction - release - reattach 
        // This tests that the dataset is sync'ed when going into transactional mode. 
        
        StoreConnection sConn = getStoreConnection() ;
        Location loc = sConn.getLocation() ;

        DatasetGraphTxn dsgTxn = sConn.begin(ReadWrite.WRITE) ;

        dsgTxn.add(q1) ;
        assertTrue(dsgTxn.contains(q1)) ;
        dsgTxn.commit() ;
        dsgTxn.end() ;

        sConn.forceRecoverFromJournal() ;
        assertTrue(sConn.getBaseDataset().contains(q1)) ;
        
        StoreConnection.release(loc) ;
        sConn = StoreConnection.make(loc) ;
        DatasetGraph dsg2 = sConn.getBaseDataset() ;
        assertTrue(dsg2.contains(q1)) ;
        
        DatasetGraphTxn dsgTxn2 = sConn.begin(ReadWrite.READ) ;
        assertTrue(dsgTxn2.contains(q1)) ;
        dsgTxn2.end() ;
    }

    @Test
    public void store_7()
    {
        // No transaction, plain update, then transaction.
        // This tests that the dataset is sync'ed when going into transactional mode. 
        
        boolean nonTxnData = true ;
        
        StoreConnection sConn = getStoreConnection() ;
        Location loc = sConn.getLocation() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        if ( nonTxnData ) 
        {
            dsg.add(q) ;
            TDB.sync(dsg) ;
            assertTrue(dsg.contains(q)) ;
        }

        DatasetGraphTxn dsgTxn = sConn.begin(ReadWrite.WRITE) ;
        if ( nonTxnData ) 
            assertTrue(dsgTxn.contains(q)) ;
        dsgTxn.add(q1) ;
        assertTrue(dsgTxn.contains(q1)) ;
        if ( nonTxnData ) 
            assertTrue(dsgTxn.contains(q)) ;
        dsgTxn.commit() ;
        dsgTxn.end() ;

        // Should have flushed to disk.
        if ( nonTxnData ) 
        {
            sConn.forceRecoverFromJournal() ;
            assertTrue(dsg.contains(q)) ;
        }
        assertTrue(dsg.contains(q1)) ;
        
        // release via the transactional machinery 
        StoreConnection.release(loc) ;
        sConn = null ;
        
        StoreConnection sConn2 = StoreConnection.make(loc) ;
        DatasetGraph dsg2 = sConn2.getBaseDataset() ;
        
        if ( nonTxnData ) 
            assertTrue(dsg2.contains(q)) ;
        assertTrue(dsg2.contains(q1)) ;
        
        DatasetGraphTxn dsgTxn2 = sConn2.begin(ReadWrite.READ) ;
        if ( nonTxnData ) 
            assertTrue(dsgTxn2.contains(q)) ;
        assertTrue(dsgTxn2.contains(q1)) ;
        dsgTxn2.end() ;

        // Check API methods work. 
        Dataset ds = TDBFactory.createDataset(loc) ;
        ds.begin(ReadWrite.READ) ;
        Model m = (q.isDefaultGraph() ? ds.getDefaultModel() : ds.getNamedModel("g")) ; 
        assertEquals( nonTxnData ? 2 : 1 , m.size()) ;
        ds.end() ;
    }

    
}

