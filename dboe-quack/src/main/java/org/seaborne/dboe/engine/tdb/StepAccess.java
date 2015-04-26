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
import java.util.Set ;

import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.access.AccessRows ;

import org.apache.jena.sparql.core.Var ;
import org.apache.jena.tdb.store.NodeId ;

class StepAccess implements Step<NodeId> {
    private final AccessRows<NodeId> accessor ;
    private final Tuple<Slot<NodeId>> pattern ;
    
    public StepAccess(AccessRows<NodeId> accessor, Tuple<Slot<NodeId>> pattern) {
        this.accessor = accessor ;
        this.pattern = pattern ;
    }
    @Override
    public RowList<NodeId> execute(RowList<NodeId> input) {
        Set<Var> vars = EngLib.vars(pattern) ;
        Iterator<Row<NodeId>> iter = accessor.accessRows(pattern) ;
        return RowLib.createRowList(vars, iter) ;
    }
    
    @Override
    public String toString() { return "Step/Access:"+pattern ; } 
}