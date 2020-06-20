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

package org.apache.jena.tdb.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.tdb.ConfigTest ;
import org.apache.jena.tdb.StoreConnection ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.sys.TDBInternal;
import org.apache.jena.tdb.transaction.DatasetGraphTxn ;
import org.apache.jena.tdb.transaction.TDBTransactionException ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

public abstract class AbstractStoreConnections
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
        TDBInternal.reset() ;
        DIR = ConfigTest.getCleanDir() ;
    }

    @After public void after() {} 

    protected StoreConnection getStoreConnection() {
        return StoreConnection.make(DIR) ;
    }

    @Test
    public void store_0() {
        // Expel.
        StoreConnection sConn = getStoreConnection() ;
        assertTrue(sConn.isValid());
        DatasetGraphTxn dsgW1 = sConn.begin(TxnType.WRITE) ;
        dsgW1.commit() ;
        dsgW1.end() ;
        StoreConnection.release(sConn.getLocation()) ;
        StoreConnection sConn2 = getStoreConnection() ;
    }

    @Test
    public void store_1() {
        // Expel.
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgW1 = sConn.begin(TxnType.WRITE) ;
        dsgW1.add(q1) ;
        dsgW1.commit() ;
        dsgW1.end() ;

        assertTrue(sConn.isValid());
        StoreConnection.release(sConn.getLocation()) ;
        assertFalse(sConn.isValid());
        sConn = null ;

        StoreConnection sConn2 = getStoreConnection() ;
        assertTrue(sConn2.isValid());
    }

    @Test(expected = TDBTransactionException.class)
    public void store_2() {
        // Expel.
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(TxnType.READ) ;
        StoreConnection.release(sConn.getLocation()) ;
    }

    @Test(expected = TDBTransactionException.class)
    public void store_3() {
        // Expel.
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(TxnType.WRITE) ;
        StoreConnection.release(sConn.getLocation()) ;
    }

    @Test
    public void store_4() {
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgW1 = sConn.begin(TxnType.WRITE) ;
        dsgW1.add(q1) ;
        dsgW1.commit() ;
        dsgW1.end() ;

        StoreConnection.release(sConn.getLocation()) ;
        sConn = null ;

        StoreConnection sConn2 = getStoreConnection() ;
        DatasetGraphTxn dsgW2 = sConn2.begin(TxnType.WRITE) ;
        dsgW2.add(q2) ;
        dsgW2.commit() ;
        dsgW2.end() ;

        DatasetGraphTxn dsgR2 = sConn2.begin(TxnType.READ) ;
        long x = Iter.count(dsgR2.find()) ;
        assertEquals(2, x) ;
    }

    @Test
    public void store_5() {
        // No transaction. Make sure StoreConnection.release cleans up OK.
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
    public void store_6() {
        // Transaction - release - reattach
        // This tests that the dataset is sync'ed when going into transactional mode.

        StoreConnection sConn = getStoreConnection() ;
        Location loc = sConn.getLocation() ;

        DatasetGraphTxn dsgTxn = sConn.begin(TxnType.WRITE) ;

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

        DatasetGraphTxn dsgTxn2 = sConn.begin(TxnType.READ) ;
        assertTrue(dsgTxn2.contains(q1)) ;
        dsgTxn2.end() ;
    }

    @Test
    public void store_7() {
        // No transaction, plain update, then transaction.
        // This tests that the dataset is sync'ed when going into transactional mode.

        boolean nonTxnData = true ;

        StoreConnection sConn = getStoreConnection() ;
        Location loc = sConn.getLocation() ;
        DatasetGraph dsg = sConn.getBaseDataset() ;
        if ( nonTxnData ) {
            dsg.add(q) ;
            TDB.sync(dsg) ;
            assertTrue(dsg.contains(q)) ;
        }

        DatasetGraphTxn dsgTxn = sConn.begin(TxnType.WRITE) ;
        if ( nonTxnData )
            assertTrue(dsgTxn.contains(q)) ;
        dsgTxn.add(q1) ;
        assertTrue(dsgTxn.contains(q1)) ;
        if ( nonTxnData )
            assertTrue(dsgTxn.contains(q)) ;
        dsgTxn.commit() ;
        dsgTxn.end() ;

        // Should have flushed to disk.
        if ( nonTxnData ) {
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

        DatasetGraphTxn dsgTxn2 = sConn2.begin(TxnType.READ) ;
        if ( nonTxnData )
            assertTrue(dsgTxn2.contains(q)) ;
        assertTrue(dsgTxn2.contains(q1)) ;
        dsgTxn2.end() ;

        // Check API methods work.
        Dataset ds = TDBFactory.createDataset(loc) ;
        ds.begin(TxnType.READ) ;
        Model m = (q.isDefaultGraph() ? ds.getDefaultModel() : ds.getNamedModel("g")) ;
        assertEquals(nonTxnData ? 2 : 1, m.size()) ;
        ds.end() ;
    }
    
}

