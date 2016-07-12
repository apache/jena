/*
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
    // B+Trees - use a random access, zero copy encoding like Cap'n Proto, FlatBuffers, SBE (via real-logic) or messagebuffers
    // Definitely from files: flatmessage
    
    // Check TestTxn*
    
    // Multi-threaded transactions with per-thread over the top.
    
    // Logging to JUL by default.
    // Update to jena.sparql.core DSG changes.
    //   TransctionLock
    //   DatasetGraphWithLock.
    
    // TransactionComponentWholeFile 
    
    // See also NOTES_TDB
    // Log<X> over BinaryDataFile
    //   TransLog<X>
    // BDF -- update javadoc
    
    // Transactions .getTxnState() -> READ, WRITE.
    
    // Iterators -> streams.
    //   May wish to do higher level parallelism and retain control.
    
    // NodeCache and abort
    
    // Reduce overheads
    //   
    //  Record if change zero cost abort/commit.
    //  Shared state files / less sync's.
    
    // Split read lifecycle completely from write lifecycle : read commit from write commit:
    // R_commit, R_abort, R_end, W_Prepare, W_Commit, W_Abort, W_End
    //   Transaction/Mantis -> Component driver.
    
    // Document
    
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
}

