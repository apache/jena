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

package org.seaborne.dboe.engine.tdb ;

import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.dboe.engine.Slot ;

import org.apache.jena.sparql.core.Var ;
import org.apache.jena.tdb.store.NodeId ;
import org.apache.jena.tdb.store.tupletable.TupleIndex ;

/** Two-sided merge of two index accesses */
public class MergeActionIdxIdx {
    private IndexAccess indexAccess1 ;
    private IndexAccess indexAccess2 ;
    private Var         var ;

    public MergeActionIdxIdx(IndexAccess indexAccess1, IndexAccess indexAccess2) {
        super() ;
        this.indexAccess1 = indexAccess1 ;
        this.indexAccess2 = indexAccess2 ;
        if ( !indexAccess1.getVar().equals(indexAccess2.getVar()) )
            // May relax this and allow (?x = ?y AS ?z)
            throw new InternalError("IndexAcceses in a merge must be joining the same variable") ;
        this.var = indexAccess1.getVar() ;
    }

    public TupleIndex getIndex1() {
        return indexAccess1.getIndex() ;
    }

    public TupleIndex getIndex2() {
        return indexAccess2.getIndex() ;
    }

    public Tuple<Slot<NodeId>> getPattern1() {
        return indexAccess1.getPattern() ;
    }

    public Tuple<Slot<NodeId>> getPattern2() {
        return indexAccess2.getPattern() ;
    }

    public Var getVar() {
        return var ;
    }

    public int getPrefixCount() {
        return indexAccess1.getPrefixLen() + indexAccess2.getPrefixLen() ;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder() ;
        builder.append("Merge [") ;
        builder.append(indexAccess1) ;
        builder.append(",") ;
        builder.append(indexAccess2) ;
        // builder.append(",") ;
        // builder.append(var) ;
        builder.append("]") ;
        return builder.toString() ;
    }

}
