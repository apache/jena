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
    
    // BlockMgrBuilderStd -> dboe-base
    // Rename/interface for *BuilderStd.
    // log4j.properties in all src/test/resources
    // dboe-base-test ? 

    // == Mantis
    // dboe-test ?  See BufferTestLib
    // log4j.properties.
    // rename org.seaborne.jena.*
    // Tests in quack.
    
    // == Base 
    // Move LocationLock next to StoreConnection.
    
    // == Transactions
    
    // TransactionCoordinator.start() -- helper ?
    
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
    
    // ComponentId
    //   Registry
    //   Base bytes and index. (6 bytes + 2 bytes (=64k))?
    // TransactionalComponentLifecycle.super(ComponentId)
    
    // == Index

    // BPlusTree.start/finishBatch - take a R/W flag?  Make transactional?
    // Transactional++ 
    
    // Upgrade BPTree to TransactionMVCC
    
    // Block : development code added to enforece read-only use. Marked [[Dev-RO]]
    //   Remove readonly flag?  A block in a txn may be read in one place and write in another.
    //   Not actually checked so useless.
    //   

}

