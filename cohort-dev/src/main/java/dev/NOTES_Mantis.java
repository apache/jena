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



public class NOTES_Mantis {
    // Streams, not iterators?

    // For all Trans*, do we need to record both Redo and Undo actions in the
    // journal during prepare? And then recovery is either "forward" or "backward".
    // Recovery: recover(Redo/undo, data);
    // Prepare: journal.writeREDO, journal.writeUNDO
    // Journal for components: only  writeREDO, writeUNDO
    
    // Components to write directly to the Journal during prepare.
    
    // Split READ and WRITE internally to be two different lifecycles.
    //   Revisit TransactionalComponent
    // Split read lifecycle completely from write lifecycle : read commit from write commit:
    // R_commit, R_abort, R_end, W_Prepare, W_Commit, W_Abort, W_End
    //   Transaction/Mantis -> Component driver.
    
    // Document
    
    // Pseudo fake a crash.
    // Clean journal interface.

    // Transaction.TxnState - rename?
    
    // Insert/delete -> return null?

    // Bulk operations:
    // Index
    //    Bulk patch.
    
    // ---------------------
    // See TransactionalComponentLifecycle
    
    // Constants organisation: SystemBase, SystemFile, SystemBPTree simple, SystemBPTreeTxt
    
    // Combine BptTxnState and BPTStateMgr?
    // StateMgr = mgr and state.  Split concepts.
    // RangeIndexBuilderBPTree and BPlusTreeFactory. Sort out BPlusTreeFactory.
    
    // Fast clear (new tree).  Fast clear for dft graph.
    
    // BPTreeRangeIterator
    //   - use indexes, delay reading blocks.
    //   - check for concurrency violation
    
    // Ideal: ("v3")
    //   Per operation mgt struct - collects pages touched etc.
    
    // Free block chain and transactions.

    // == Base 
    
    // == Transactions
    
    // Delayed write back by leaving stuff in the journal.
    //   Avoids needing to sync the BPTs (leave to natural file caching)
    //   Avoids need to flush the new root to disk.
    
    // Promotable transactions:
    //    Two counters, writer leading and trailing edge.
    //    R transactions note their start generation.
    //    Can promote IFF that is still the generation at the point of promotion.
}

