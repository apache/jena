/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package dev;

public class NOTES {
    // ARQ :: extract Atlas.

    // Systematic tests involving recovery: e.g. TransObjectFile.
    // Pseudo fake a crash.

    // Clean journal interface.
    
    // Insert/delet -> return null?

    // Bulk operations:
    // Index 
    //   insertMany(Collection<Record> record)
    //   deleteMany(Collection<Record> record)
    // patch support?
    
    // ---------------------
    // Split READ and WRITE internally to be two different lifecycles.
    // See TransactionalComponentLifecycle
    
    // Constants organisation: SystemBase, SystemFile, SystemBPTree simple, SystemBPTreeTxt
    
    // Combine BptTxnState and BPTStateMgr?
    // StateMgr = mgr and state.  Split concepts.
    // RangeIndexBuilderBPTree and BPlusTreeFactory. Sort out BPlusTreeFactory.
    
    // Transactional tests on BPTrees
    //   AbstractTestTxnRangeIndex from TestBPlusTreeTxn
    
    // Fast clear (new tree).  Fast clear for dft graph.
    
    // Tests of txn after txn
    
    // BPTreeRangeIterator
    //   - use indexes, delay reading blocks.
    //   - check for concurrency violation
    
    // Ideal: ("v3")
    //   Per operation mgt struct - collects pages touched etc.
    
    // Just reset the free block chain per transaction.
    // Us
    

    // Remove CheckingTree - broken.

    // Delete BlockMgrSync - used by BlockMgrCache
    
    // TransObjectFile should do the on-disk state thing.
    
    // == Base 
    // Move LocationLock next to StoreConnection.
    
    // == Quack 
    // Quack is just the join(->rel op) library.
    // Move TDB-isms to tdb2.
    
    // == Transactions
    
    // TransactionCoordinator.start() -- helper ?
    
    // Delayed write back by leaving stuff in the journal.
    //   Avoids needing to sync the BPTs (leave to natural file caching)
    //   Avoids need to flush the new root to disk.
    
    // TransactionCoordinator.haltedMode
    // TransactionCoordinator.activeMode
    
    // Recovery: recover(Redo/undo, data);
    // Prepare: journal.writeREDO, journal.writeUNDO
    // Journal for components: only  writeREDO, writeUNDO
    
    // Promotable transactions:
    //    Two counters, writer leading and trailing edge.
    //    R transactions note their start generation.
    //    Can promote IFF that is still the generation at the point of promotion.
}

