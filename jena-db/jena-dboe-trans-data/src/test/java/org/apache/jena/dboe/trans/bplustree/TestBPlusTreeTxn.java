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

package org.apache.jena.dboe.trans.bplustree;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.index.test.IndexTestLib;
import org.apache.jena.sys.Txn;
import org.apache.jena.dboe.test.RecordLib;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeFactory;
import org.apache.jena.dboe.transaction.Transactional;
import org.apache.jena.dboe.transaction.TransactionalFactory;
import org.apache.jena.dboe.transaction.txn.TransactionalComponent;
import org.apache.jena.query.ReadWrite ;
import org.junit.Assert ;
import org.junit.Test ;

/** Tests of B+Tree and transactions */ 
public class TestBPlusTreeTxn extends Assert {
    
    static BPlusTree createBPTree() { 
        return BPlusTreeFactory.makeMem(2, 2, RecordLib.TestRecordLength, 0) ;
    }
    
    static Transactional transactional(TransactionalComponent ... components) {
        return transactional(Location.mem(), components) ;
    }
    
    static Transactional transactional(Location location, TransactionalComponent ... components) {
        return TransactionalFactory.createTransactional(location, components) ;
    }
    
    // Commit
    @Test public void bptree_txn_01() {
        BPlusTree bpt = createBPTree() ;
        assertNotNull(bpt.getComponentId()) ;
        int outerRootIdx1 = bpt.getRootId() ;
        Transactional thing = transactional(bpt) ;
        Txn.executeWrite(thing, () -> { 
            IndexTestLib.add(bpt, 1, 2, 3, 4) ;
        } ); 
        int outerRootIdx2 = bpt.getRootId() ;
        assertNotEquals("After txn", outerRootIdx1, outerRootIdx2); 
    }
 
    // Commit - only the first changes the root.
    @Test public void bptree_txn_02() {
        BPlusTree bpt = createBPTree() ;
        int outerRootIdx1 = bpt.getRootId() ;
        Transactional thing = transactional(bpt) ;
        Txn.executeWrite(thing, () -> { 
            int rootIdx1 = bpt.getRootId() ;
            assertEquals("Inside txn (1)", outerRootIdx1, rootIdx1);
            IndexTestLib.add(bpt, 1) ;
            int rootIdx2 = bpt.getRootId() ;
            assertNotEquals("Inside txn (2)", rootIdx1, rootIdx2);
            IndexTestLib.add(bpt, 2, 3, 4) ;
            int rootIdx3 = bpt.getRootId() ;
            assertEquals("Inside txn (3)", rootIdx2, rootIdx3);
        } ) ; 
        int outerRootIdx2 = bpt.getRootId() ;
        assertNotEquals("After txn", outerRootIdx1, outerRootIdx2); 
    }

    // Abort
    @Test public void bptree_txn_03() {
        BPlusTree bpt = createBPTree() ;
        int outerRootIdx1 = bpt.getRootId() ;
        Transactional thing = transactional(bpt) ;
        thing.begin(ReadWrite.WRITE);
        IndexTestLib.add(bpt, 1, 2, 3, 4) ;
        thing.abort() ;
        thing.end() ;
        int outerRootIdx2 = bpt.getRootId() ;
        assertEquals("After txn", outerRootIdx1, outerRootIdx2); 
    }
    
    // Two transactions
    @Test public void bptree_txn_04() {
        BPlusTree bpt = createBPTree() ;
        int outerRootIdx1 = bpt.getRootId() ;
        Transactional thing = transactional(bpt) ;
        Txn.executeWrite(thing, () -> { 
            IndexTestLib.add(bpt, 1, 2, 3, 4) ;
        } ); 
        int outerRootIdx2 = bpt.getRootId() ;
        assertNotEquals("After txn(1)", outerRootIdx1, outerRootIdx2); 
        Txn.executeWrite(thing, () -> { 
            IndexTestLib.add(bpt, 5, 6) ;
        } ); 
        int outerRootIdx3 = bpt.getRootId() ;
        assertNotEquals("After txn (2)", outerRootIdx1, outerRootIdx3); 
        assertNotEquals("After txn (3)", outerRootIdx2, outerRootIdx3); 
    }
    
    // Two transactions, second an insert no-op.
    // Relies on all blocks not being full and so not being
    // split on the way down due to the early split algorithm. 
    @Test public void bptree_txn_05() {
        BPlusTree bpt = createBPTree() ;
        int outerRootIdx1 = bpt.getRootId() ;
        Transactional thing = transactional(bpt) ;
        Txn.executeWrite(thing, () -> { 
            IndexTestLib.add(bpt, 1, 2, 3) ;
        } ); 
        int outerRootIdx2 = bpt.getRootId() ;
        assertNotEquals("After txn(1)", outerRootIdx1, outerRootIdx2); 
        Txn.executeWrite(thing, () -> { 
            IndexTestLib.add(bpt, 1, 2) ;
        } ); 
        int outerRootIdx3 = bpt.getRootId() ;
        assertNotEquals("After txn (2)", outerRootIdx1, outerRootIdx3); 
        assertEquals("After txn (3)", outerRootIdx2, outerRootIdx3); 
    }

    // Two transactions, second a delete no-op.
    // Relies on all blocks not being min0size so not rebalanced.
    @Test public void bptree_txn_06() {
        BPlusTree bpt = createBPTree() ;
        int outerRootIdx1 = bpt.getRootId() ;
        Transactional thing = transactional(bpt) ;
        Txn.executeWrite(thing, () -> { 
            IndexTestLib.add(bpt, 1, 2, 3) ;
        } ); 
        int outerRootIdx2 = bpt.getRootId() ;
        assertNotEquals("After txn(1)", outerRootIdx1, outerRootIdx2); 
        Txn.executeWrite(thing, () -> { 
            IndexTestLib.delete(bpt, 5, 6) ;
        } ); 
        int outerRootIdx3 = bpt.getRootId() ;
        assertNotEquals("After txn (2)", outerRootIdx1, outerRootIdx3); 
        assertEquals("After txn (3)", outerRootIdx2, outerRootIdx3); 
    }
    
    // Two trees
    @Test public void bptree_txn_10() {
        BPlusTree bpt1 = createBPTree() ;
        BPlusTree bpt2 = createBPTree() ;
        assertNotEquals(bpt1.getComponentId(), bpt2.getComponentId()) ;
        
        Transactional thing = transactional(bpt1, bpt2) ;
        Txn.executeWrite(thing, () -> { 
            IndexTestLib.add(bpt1, 1, 2, 3) ;
            IndexTestLib.add(bpt2, 4, 5) ;
        } );
        Txn.executeRead(thing, ()->{
            IndexTestLib.testIndexContents(bpt2, 4, 5);
            IndexTestLib.testIndexContents(bpt1, 1, 2, 3);
        } );
    }
    
    @Test public void bptree_txn_11() {
        BPlusTree bpt1 = createBPTree() ;
        BPlusTree bpt2 = createBPTree() ;
        assertNotEquals(bpt1.getComponentId(), bpt2.getComponentId()) ;
        
        Transactional thing = transactional(bpt1, bpt2) ;
        
        // Commit 1
        Txn.executeWrite(thing, () -> { 
            IndexTestLib.add(bpt1, 2, 1) ;
            IndexTestLib.add(bpt2, 3, 4, 5) ;
        }) ;
        Txn.executeRead(thing, ()->{
            IndexTestLib.testIndexContents(bpt2, 3, 4, 5);
            IndexTestLib.testIndexContents(bpt1, 1, 2);
        } );
        
        // Abort
        thing.begin(ReadWrite.WRITE);
        IndexTestLib.add(bpt1, 9, 10) ;
        IndexTestLib.delete(bpt2, 3, 11) ;
        thing.abort() ;
        Txn.executeRead(thing, ()->{
            IndexTestLib.testIndexContents(bpt2, 3, 4, 5);
            IndexTestLib.testIndexContents(bpt1, 1, 2);
        } );
        
        // Commit 2
        Txn.executeWrite(thing, () -> { 
            IndexTestLib.delete(bpt1, 1,3) ;
            IndexTestLib.add(bpt1, 4) ;
            IndexTestLib.add(bpt2, 11, 12, 13) ;
        }) ;
        Txn.executeRead(thing, ()->{
            IndexTestLib.testIndexContents(bpt2, 3, 4, 5, 11, 12, 13);
            IndexTestLib.testIndexContents(bpt1, 2, 4);
        } );
    }
    
    
}
