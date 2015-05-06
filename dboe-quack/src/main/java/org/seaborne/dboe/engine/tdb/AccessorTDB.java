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

package org.seaborne.dboe.engine.tdb;

import java.util.Iterator ;
import java.util.function.Function ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Tuple ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.tdb.store.NodeId ;
import org.apache.jena.tdb.store.nodetable.NodeTable ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.access.Accessor ;
import org.seaborne.dboe.engine.row.RowBuilderBase ;

public class AccessorTDB implements Accessor<NodeId> {
    private final StorageTDB db ;

    /** Create a Accessor */ 
    public static AccessorTDB create(StorageTDB db) {
        //AccessorTDB accessor = new AccessorTDB(db) ;
        AccessorTDB accessor = new AccessorParallel(db) ;
        accessor = new AccessorTDBDebug("AccessorTDB.", accessor) ; 
        return accessor ;
    }

    protected AccessorTDB(StorageTDB db) {
        this.db = db ;
    }

    public StorageTDB getDB() {
        return db;
    }

    public NodeTable getNodeTable() { return db.getNodeTable() ; }  
    
    // ---- AccessData
    @Override
    public Iterator<Tuple<NodeId>> accessTuples(Tuple<NodeId> pattern) {
        return db.getTupleTable(pattern).find(pattern) ;
    }

    //---- AccessRows
    @Override
    public Iterator<Row<NodeId>> accessRows(Tuple<Slot<NodeId>> tuple) {
        int N = tuple.size() ;

        boolean unionMode = false ;
        if ( N == 4 ) {
            Slot<NodeId> graphSlot = tuple.get(0) ;
            // Union if the graph slot is exactly NodeIdAny.
            unionMode = ! graphSlot.isVar() && NodeId.isAny(tuple.get(0).term) ;
        }
        Tuple<NodeId> findPattern = EngLib.convertTupleToAny(tuple, NodeId.NodeIdAny) ;
        
        //TupleTable tupleTable = (N==3)? db.getTripleTupleTable() : db.getQuadTupleTable() ;
        //Iterator<Tuple<NodeId>> iter = tupleTable.find(findPattern) ;
        
        Iterator<Tuple<NodeId>> iter = accessTuples(findPattern) ;  // Accessor

        RowBuilder<NodeId> builder = new RowBuilderBase<>() ; 

        if ( unionMode ) {
            // See StageMatchTuple.makeStage
            iter = Iter.map(iter, quadsToAnyTriples) ;
            // If any slots were set, then the index would be ???G and we can use distinctAdjacent.
            // If all slots are unset, the index is probably GSPO (SPOG would be better in this one case). 
            // This is a safe, if potentially costly, choice. 

            //Guaranteed 
            //iterMatches = Iter.distinct(iterMatches) ;

            // This depends on the way indexes are choose and
            // the indexing pattern. It assumes that the index 
            // chosen ends in G so same triples are adjacent 
            // in a union query.
            // If any slot is defined, then the index will be X??G.
            // if no slot is defined, then the index will be ???G.
            //  See TupleTable.scanAllIndex that ensures the latter.
            //  The former assumes indexes are either G... or ...G.
            //  No G part way through.
            iter = Iter.distinctAdjacent(iter) ;
        }

        Iterator<Row<NodeId>> iter2 = EngLib.convertIterTuple(iter, tuple, builder) ;
        return iter2 ;
    } 

    // -- Mutating "transform in place"
    private static Function<Tuple<NodeId>, Tuple<NodeId>> quadsToAnyTriples = 
        (item)->Tuple.createTuple(NodeId.NodeIdAny, item.get(1), item.get(2), item.get(3)) ;

    // ---- AccessRowList
    // See AccessOps and calls from StepPredicateObjectList
    /** Access with a predicate-object list */ 
    @Override public Iterator<Row<NodeId>> fetch(PredicateObjectList<NodeId> predObjs) {
        Slot<NodeId> gSlot = predObjs.getGraph() ;
        if ( gSlot != null && gSlot.isVar() )
            Log.warn(this, "Graph variable : "+gSlot) ;
        // XXX Union mode.
        NodeId g = (gSlot==null) ? null : gSlot.term ;
        Slot<NodeId> subject = predObjs.getSubject() ;
        
        if ( subject.isVar() )
            return fetchVarSubject(predObjs, g, subject.var) ;
        else
            return fetchTermSubject(predObjs, g, subject.term) ;
    }
    
    protected Iterator<Row<NodeId>> fetchTermSubject(PredicateObjectList<NodeId> predObjs, NodeId g , NodeId subject ) {
        Tuple<NodeId> tuple = ELibTDB.createTuple(g, subject, NodeId.NodeIdAny, NodeId.NodeIdAny) ;
        return AccessOps.executeTermSubject$(tuple, this, predObjs);
    }

    protected Iterator<Row<NodeId>> fetchVarSubject(PredicateObjectList<NodeId> predObjs, NodeId g , Var var ) {
        return AccessOps.executeVarSubject$(var, this, this, predObjs) ;
    }
}