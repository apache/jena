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
    // Mantis / cohort / legion
    // ARQ :: extract Atlas.
    
    // Sort out BPlusTreeFactory.
    
    // Transactional tests on BPTrees
    //   AbstractTestTxnRangeIndex
    
    // Transaction node testing.
    // A few operations per tree.
    /*
              BPlusTree bpt = BPlusTreeFactory.makeMem(order, SystemLz.SizeOfInt, 0) ;
              Journal journal = Journal.create(Location.mem()) ;
              Transactional holder = TransactionalFactory.create(journal, bpt) ;
              holder.begin(ReadWrite.WRITE);
              IndexTestLib.randTest(bpt, 5*size, size, true);
              holder.commit() ;
              holder.end() ;
     */

    // B+Tree commit , versioned root, recovery. 
    
    // Free chain - just in the active area.
    // BlockMgr semi-participate in transactions?
    //   Enforce write barrier.
    // Fast clear (new tree).  Fast clear for dft graph.
    
    // Non-transactional:
    // * BPTreeRecords.split can use links in case it was never transactional.
    // * No need for access paths.
    
    // Tests of txn after txn
    // No promote -> no access path.
    
    // BPTreeRangeIterator
    //   - use indexes, delay reading blocks.
    //   - .release pages.
    //   - Need page.getIterator?
    //   - check for concurrency violation
    
    // Ideal: ("v3")
    //   Per operation mgt struct - collects pages touched etc.
    
    // Free block management - just in the active area.
    // Automatic? Because clone-clear happens.
    // Just reset the free block chain per transaction.

    // Remove CheckingTree - broken.

    // Clear up being/end bracketing.
    
    // BlockAccessBase/BlockAccessMapped needs to track highest block better.
    // Delete BlockMgrSync - used by BlockMgrCahe
    // Free chain management for MVCC block mgrs.  BlockMgrFreeChain is enough?
    
    // == Base 
    // Move LocationLock next to StoreConnection.
    
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
    
    // ObjectFile system transactions
    
    // Promotable transactions:
    //    Two counters, writer leading and trailing edge.
    //    R transactions note their start generation.
    //    Can promote IFF that is still the generation at the point of promotion.
}

