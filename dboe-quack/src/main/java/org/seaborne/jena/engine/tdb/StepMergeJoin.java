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

package org.seaborne.jena.engine.tdb;

import java.util.Iterator ;
import java.util.Set ;

import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.jena.engine.* ;
import org.seaborne.jena.engine.join.RowOrder ;
import org.seaborne.jena.engine.row.RowBuilderBase ;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;

class StepMergeJoin implements Step<NodeId>
{
    private final MergeActionIdxIdx mergeStep ;

    public StepMergeJoin(MergeActionIdxIdx mergeStep) {
        this.mergeStep = mergeStep ;
    }
    
    @Override
    public RowList<NodeId> execute(RowList<NodeId> input) {
        // Ignore input.
        JoinKey joinKey = JoinKey.create(mergeStep.getVar()) ;
        Set<Var> vars = DS.setOfOne(mergeStep.getVar()) ;
        
        RowList<NodeId> left = find(mergeStep.getIndex1(), mergeStep.getPattern1(), vars) ;
        RowList<NodeId> right = find(mergeStep.getIndex2(), mergeStep.getPattern2(), vars) ;
        RowBuilder<NodeId> builder = new RowBuilderBase<NodeId>() ;
        return Join.mergeJoin(joinKey, left, right, comparator, builder) ;
    }
    
    RowList<NodeId> find(TupleIndex index, Tuple<Slot<NodeId>> pattern, Set<Var> vars) {
        Tuple<NodeId> tuple = EngLib.convertTupleToAny(pattern, NodeId.NodeIdAny) ;
        Iterator<Tuple<NodeId>> iter = index.find(tuple) ;
        RowBuilder<NodeId> builder = new RowBuilderBase<NodeId>() ;
        Iterator<Row<NodeId>> iter2 = EngLib.convertIterTuple(iter, pattern, builder) ;
        return RowLib.createRowList(vars, iter2) ;
    }
    
    @Override
    public String toString() { return "Step/Mergejoin:"+mergeStep ; }
    
    static RowOrder<NodeId> comparator = new RowOrder<NodeId>(){
        @Override
        public int compare(JoinKey joinKey, Row<NodeId> row1, Row<NodeId> row2) {
            if ( Join.compatible(row1, row2) )
                return 0 ;
            for ( Var v : joinKey ) {
                NodeId x1 = row1.get(v) ; 
                NodeId x2 = row2.get(v) ;
                if ( x1 == null ) 
                    throw new InternalErrorException("comparator: "+v+" : x1 is null") ;
                if ( x2 == null ) 
                    throw new InternalErrorException("comparator: "+v+" : x2 is null") ;
                int z = Long.compare(x1.getId(), x2.getId()) ;
                if ( z != 0 )
                    return z ;
            }
            // Error - they weren't join compatible.
            return 0 ; 
        }} ;
}