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

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.dboe.engine.general.OpExecLib ;

import org.apache.jena.graph.Node ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;
import org.seaborne.tdb2.store.nodetable.NodeTable ;
import org.seaborne.tdb2.store.nodetupletable.NodeTupleTable ;
import org.seaborne.tdb2.store.tupletable.TupleIndex ;
import org.seaborne.tdb2.store.tupletable.TupleTable ;

/** Eventually - add this as an interface to DatasetGraphTDB.
 *  This is a collection getters to important parts of a TDB storage dataset. 
 */
public final class StorageTDB {
    /// XXX Add accessors
    private final DatasetGraphTDB dsg ;
    private final TupleTable tripleTuples ;
    private final TupleTable quadTuples ;
    private final NodeTable  nodeTable ;

    public StorageTDB(DatasetGraphTDB dsg) {
        this.dsg = dsg ;
        this.tripleTuples = dsg.getTripleTable().getNodeTupleTable().getTupleTable() ;
        this.quadTuples = dsg.getQuadTable().getNodeTupleTable().getTupleTable() ;
        this.nodeTable = dsg.getTripleTable().getNodeTupleTable().getNodeTable() ;
   }

    public TupleTable getTripleTupleTable() { return tripleTuples ; }
    public TupleTable getQuadTupleTable()   { return quadTuples ; }
    public NodeTable getNodeTable()         { return nodeTable ; }
    
    public TupleTable getTupleTable(Tuple<?> tuple) {
        switch (tuple.size()) {
            case 3: return tripleTuples ;
            case 4: return quadTuples ;
        default: 
            throw new InternalErrorException("Tuple wrong length: "+tuple) ;
        }
    }
    
    /** Get the tuple table depending on the graphNode */
    public TupleTable getTupleTable(Node graphNode) {
        if ( OpExecLib.isDefaultGraph(graphNode) )
            return getTripleTupleTable() ;
        if ( OpExecLib.isUnionGraph(graphNode) )
            return getQuadTupleTable() ;
        return getQuadTupleTable() ;
    }

    public TupleIndex getIndex(String name) {
        switch ( name.length() ) {
            case 3: return getTupleIndexByName(tripleTuples, name) ;
            case 4: return getTupleIndexByName(quadTuples, name) ;
            default: 
                throw new InternalErrorException("Index name '"+name+" wrong length") ;
        }
    }

    private static TupleIndex getTupleIndexByName(TupleTable table, String name) {
//        if ( name.length() != table.getTupleLen() )
//            throw new InternalErrorException("Index name '"+name+"'incompatible with tuple table") ;
        for ( TupleIndex idx : table.getIndexes() ) {
            if ( idx.getName().equals(name) )
                return idx ;
        }
        return null ;
    }
    
    
//    /** Access to the details of TDB storage for one dataset */ 
//    public NodeTable getNodeTable() ;
//    public TupleTable getTriples() ;
//    public TupleTable getQuads() ;
//    public TupleIndex getIndex(String name) ;
//    public TupleIndex getIndex(ColumnMap colMap) ;
//    /** Get the index that woudl be choosen for the pattern */
//    public TupleIndex getIndex(Tuple<NodeId> pattern) ;
//    // Prefix stuff?

    
//    /** Get the tuple table depending on the graphNode */
//    public NodeTupleTable getNodeTupleTable(Node graphNode) {
//        if ( OpExecLib.isDefaultGraph(graphNode) )
//            return dsg.getTripleTable().getNodeTupleTable() ;
//        if ( OpExecLib.isUnionGraph(graphNode) )
//            return dsg.getQuadTable().getNodeTupleTable() ;
//        return dsg.getQuadTable().getNodeTupleTable() ;
//    }

    /* Choose the NodeTupleTable appropriate for the graphNode */ 
    public static NodeTupleTable chooseNodeTupleTable(DatasetGraphTDB dsgtdb, Node graphNode) {
        if ( OpExecLib.isDefaultGraph(graphNode) )
            return dsgtdb.getTripleTable().getNodeTupleTable() ;
        if ( OpExecLib.isUnionGraph(graphNode) )
            return dsgtdb.getQuadTable().getNodeTupleTable() ;
        return dsgtdb.getQuadTable().getNodeTupleTable() ;
    }    

}

