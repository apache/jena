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

package org.seaborne.dboe.engine;

import java.util.Set ;

import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.sparql.core.Var ;
import org.seaborne.dboe.engine.access.AccessRows ;
import org.seaborne.dboe.engine.join.HashJoin ;
import org.seaborne.dboe.engine.row.RowListBase ;

public class StepHashJoin<X> implements Step<X> {

    final private Tuple<Slot<X>> pattern ;
    final private AccessRows<X> accessor ;
    final private RowBuilder<X> builder ;
    final private Set<Var> vars ;

    public StepHashJoin(Tuple<Slot<X>> tuple, AccessRows<X> accessor, RowBuilder<X> builder) { 
        this.pattern = tuple ;
        this.accessor = accessor ;
        this.builder = builder ;
        this.vars = EngLib.vars(tuple) ;
    }
    
    @Override
    public RowList<X> execute(RowList<X> left) {
        RowList<X> right = new RowListBase<X>(vars, accessor.accessRows(pattern)) ;
        JoinKey jk = JoinKey.create(left.vars(), vars) ;
        return HashJoin.hashJoin(jk, left, right, builder) ;
    }
    
    @Override
    public String toString() { return "Step/HashJoin:"+pattern ; } 

}
