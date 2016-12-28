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

package dev;

public class NOTES {
    // Check TestTxn* from Jena.
    // **** Logging to JUL by default. fu-t2.

    // Longer NodeIds
    //   And a mode to record datatype?
    //   Hash version
    // See NOTES_TDB3

    // *** Reaper
    //   Switchable StorageTDB (exc mode)

    // *** Transactions:
    //   NestedTransactions by counting (only).
    //   Transaction object
    //     Caches e.g. find()cache
    //     Block touched log
    // Check NodeTableCache and aborts. : clean out aborted?

    // Split read lifecycle completely from write lifecycle : read commit from write
    // commit:
    //    R_commit, R_abort, R_end, W_Prepare, W_Commit, W_Abort, W_End
    // Open mode: 
    //TxnMode(WRITE, READ(=READ_ISOLATED), READ_COMMITED, READ_ONLY, READ_ISOLATED , ISOLATED)
    //  begin() == begin(READ_ISOLATED) ;
    // Autocommit via DatasetBuffered+on-disk patch.
    
    // *** Components:
    // Check build a Transction with arbitrary transaction set
    // TransactionalComponentN -- N-way
    //   Two datasets
    //   Dataset + file.
    // TransactionComponentWholeFile
    // Log<X> over BinaryDataFile
    // TransLog<X>
    // BDF -- update javadoc
    
    // *** B+Tree
    //   use a random access, zero copy encoding like Cap'n Proto, FlatBuffers,
    //    SBE (via real-logic) or messagebuffers
    //    Definitely from files: flatmessage
    // Combine BptTxnState and BPTStateMgr?
    // RangeIndexBuilderBPTree and BPlusTreeFactory. Sort out BPlusTreeFactory.
    // Fast clear (new tree). Fast clear for dft graph.
    // BPTreeRangeIterator - check for concurrency violation
    // Free block chain and transactions.

    // *** BlockMgr
    // Multiple block mgrs per file (less files, less sync costs)
    
    // *** Bulk loader
    // Loader: Try with StreamRDFBatchSplit and a parallel index update.
    //   Needs multi-threaded transaction control.
    //   On a live system!!
    //   Mantis -> exclusive mode; MRSW mode.
    // Bulk loader from zero:
    //  load SPO, then parallel load POS, OSP, PSO etc.

    // Constants organisation: SystemBase, SystemFile, SystemBPTree simple, SystemBPTreeTxt

    // DatasetGraph.exec(op)
    //   Interface ExecuteOp + generic registration.
    // DatasetGraph.getBaseDatasetGraph
}
