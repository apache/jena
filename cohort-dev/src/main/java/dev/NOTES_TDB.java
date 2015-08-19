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

import java.util.Iterator ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.Quad ;


public class NOTES_TDB {
    // Reaper.

    // GraphTDB from 
    // Needs to work with the switching DatasetGraphTDB.
    //   Abstract DatasetGraphTDB as an interface!
    //     Storage unit to have indexes and node table.
    
    

    // Loader: Try with StreamRDFBatchSplit and a parallel index update.
    //   Needs multi-threaded transaction control.
    //   Mantis -> exclusive mode.  MRSW.
    // Bulk loader from zero:
    //  load SPO, then parallel load POS, OSP, PSO etc.
    
    
    // ** NodeTableCache and aborts.
    // NodeTableCache + abort -> clean out?
    // Abort notification.
    // Or NodeTableCache part of the transaction.

    // DatasetGraphTDB has begin/commit/abort/end --> Not used?
    
    // Quack clean / split into general and TDB
    // Quack and SPO, POS (fast load mode)
    //   Index to index copy pogram.
    //   Work wit Lizard?
    
    // DatasetGraph.exec(op)
    //   Interface ExecuteOp + generic registration.
    // DatasetGraph.getBaseDatasetGraph
    
    // ++ DatasetGraphTriplesQuads
}

interface StorageRDF {
    void add(Triple triple) ; // addToDftGraph -- DatasetGraphTriplesQuads
    void addTriple(Node s, Node p, Node o) ; // addToDftGraph -- DatasetGraphTriplesQuads
    
    void add(Quad quad) ;     // addToNamedGraph
    void addQuad(Node g, Node s, Node p, Node o) ;
    
    void delete(Triple triple) ;
    void delete(Quad quad) ;
    Iterator<Triple> find(Node s, Node p, Node o) ;
    Iterator<Quad> find(Node g, Node s, Node p, Node o) ;
}

// Like Transactional(System) except not part of the transaction.  Called after main calls.
interface TxnEvent {
    default void startBegin(ReadWrite mode) {}
    default void finishBegin(ReadWrite mode) {}

    default void startCommit()  {}
    default void finishCommit() {}

    default void startAbort()   {}
    default void finishAbort()  {}

    default void startEnd()     {}
    default void finishEnd()    {}
}