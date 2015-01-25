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
    // p540.base, p540.index, ...

    // == Legion
    
    // Rename packages
	// *** Clean LICENSE, pom.xml and java headers
    // Move Quack

    // == Base 
    // Move LocationLock next to StoreConnection.
    
    // Weaker but usable version of BlockMgrTracker.
    // Start with batches and BlockMgrs 
    
    // == Transactions
    
    // Tests 
    //   Promotion.
    //   Writer epoch tests
    //   Single writer
    
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
    //    Update nofitifer
    
    // == Index

    // BPlusTree.start/finishBatch - take a R/W flag?  Make transactional?
    // Transactional++ 
    
    // Upgrade BPTree to TransactionMVCC
}

