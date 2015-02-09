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
    
    // PageBlockMgr.promote -- always copied.
    // RecordBufferPage._reset -- wrong?? format?
    
    // **** TestBPlusTree.tree_iter_2_01
    
    // ** RecordBuffer.size not geting set.
    
    // write on every records insert - cache and delay..
    // BPlusTree.releaseRootWrite
    
    // Rename/interface for *BuilderStd.
    // dboe-base-test ? 
    
    // BlockAccessBase/BlockAccessMapped needs to track highest block better.
    // Delete BlockMgrSync - used by BlockMgrCahe
    
    // Free chain management for MVCC block mgrs.  BlockMgrFreeChain is enough?
    
    // BPTreePage.isModifiable(int id, BptTxnState)
    //   BPTreeRecords.isModifiable -- look into bptree object
    //   BPTreeNode.isModifiable
    
    // BPlusTree update Mode.
    // Indirects for 
    //   getState
    //   checkWriteTxn
    //   checkTxn
    // on BptTxnState
    // or separate handler object.

    // == Mantis
    // dboe-test ?  See BufferTestLib
    // log4j.properties.
    // Tests in quack.
    
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
    
    // ComponentId
    //   Registry
    //   Base bytes and index. (6 bytes + 2 bytes (=64k))?
    // TransactionalComponentLifecycle.super(ComponentId)
}

