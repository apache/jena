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

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetPrefixStorage ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.tdb2.store.QuadTable ;
import org.seaborne.tdb2.store.TripleTable ;

public class NOTES_TDB {
    int foo ;
    // ** Reaper.
    // ** Or time-based, commit keeping.  benefit!
    
    // Also per index reaping.
    // Long term: break into parts: separate processes? At least threads.
    
    // see package txnlog

    // GraphTDB from 
    // Needs to work with the switching DatasetGraphTDB.
    //   Abstract DatasetGraphTDB as an interface!
    //     Storage unit to have indexes and node table.
    
    static class StorageTDB {
        private TripleTable tripleTable ;
        private QuadTable quadTable ;
        private DatasetPrefixStorage prefixes ;
        private Location location ;
    }

    // Loader: Try with StreamRDFBatchSplit and a parallel index update.
    //   Needs multi-threaded transaction control.
    //   Mantis -> exclusive mode.  MRSW.
    // Bulk loader from zero:
    //  load SPO, then parallel load POS, OSP, PSO etc.
    
    
    // ** NodeTableCache and aborts.
    // NodeTableCache + abort -> clean out?
    // Abort notification.
    // Or NodeTableCache part of the transaction.

    // Quack clean / split into general and TDB
    // Quack and SPO, POS (fast load mode)
    //   Index to index copy pogram.
    //   Work with Lizard?
    
    // DatasetGraph.exec(op)
    //   Interface ExecuteOp + generic registration.
    // DatasetGraph.getBaseDatasetGraph
    
    // ++ DatasetGraphTriplesQuads


    // really simple - quads/triples only - no 3 or 4 way forms.
    // Or vice versa - 

    // Like Transactional(System) except not part of the transaction.  Called after main calls.
    interface TxnEvent {
        default void startBegin(ReadWrite mode)     {}
        default void finishBegin(ReadWrite mode)    {}

        default void startPromote()     {}
        default void finishPromote()    {}

        default void startCommit()      {}
        default void finishCommit()     {}

        default void startAbort()       {}
        default void finishAbort()      {}

        default void startEnd()         {}
        default void finishEnd()        {}
    }

}