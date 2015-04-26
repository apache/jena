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

package org.seaborne.dboe.engine.extra_tdb;

import org.seaborne.dboe.engine.general.OpExecLib ;

import org.apache.jena.graph.Node ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.store.nodetupletable.NodeTupleTable ;

public class OpExecTDB
{
    // Merge with ELibTDB ?
    // ELibTDB is quack specific operations.
    /* Choose the NodeTupleTable appropriate for the graphNode
     * @deprecated Use {@link StoreageTDB#chooseNodeTupleTable}
     */
    
    @Deprecated
    public static NodeTupleTable chooseNodeTupleTable(DatasetGraphTDB dsgtdb, Node graphNode) {
        if ( OpExecLib.isDefaultGraph(graphNode) )
            return dsgtdb.getTripleTable().getNodeTupleTable() ;
        if ( OpExecLib.isUnionGraph(graphNode) )
            return dsgtdb.getQuadTable().getNodeTupleTable() ;
        return dsgtdb.getQuadTable().getNodeTupleTable() ;
    }
}
