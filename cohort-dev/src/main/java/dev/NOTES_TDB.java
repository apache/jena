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
    
    // ** NodeTableCache and aborts.
    // NodeTableCache + abort -> clean out?
    // Abort notification.
    // Or NodeTableCache part of the transaction.
    
    
    // TxnEvent or actually a TransactionComponent
    //   TC - a lot of baggage.
    //   TxnEvet - separate part of the Transaction coordinator?
    
    // Transaction state changes notification API (TripleTable, QuadTable, Prefixes -> NodeTables) 
    
    // DatasetGraphTDB has begin/commit/abort/end --> Not used?
    
    // Quack clean / split into general and TDB
    
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

// Like Transactional(System) except not part of the transaction.  Called after  
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