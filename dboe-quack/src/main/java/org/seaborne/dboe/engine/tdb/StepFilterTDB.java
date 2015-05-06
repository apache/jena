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
import java.util.function.Predicate ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprException ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.tdb.store.NodeId ;
import org.apache.jena.tdb.store.nodetable.NodeTable ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.row.RowBuilderBase ;

/** Filter step for TDB (which delays Binding fetching until needed) */ 
public class StepFilterTDB implements Step<NodeId> {

    private final ExprList exprs ;
    private final Predicate<Row<NodeId>> filter ;
    private final NodeTable nodeTable ; 
    private final RowBuilder<NodeId> builder = new RowBuilderBase<>() ;
    
    public StepFilterTDB(ExprList expressions, final NodeTable nodeTable, final FunctionEnv funcEnv) {
        this.exprs = expressions ; 
        this.nodeTable = nodeTable ;
        this.filter = new Predicate<Row<NodeId>>() {
            @Override
            public boolean test(Row<NodeId> row) {
                Binding binding = new BindingRow(row, nodeTable) ;
                for (Expr expr : exprs)
                    if ( ! accept(binding, expr) )
                        return false ;
                return true ;
            }

            private boolean accept(Binding binding, Expr expr) {
                try {
                    if ( expr.isSatisfied(binding, funcEnv) )
                        return true ;
                    return false ;
                } catch (ExprException ex) { // Some evaluation exception
                    return false ;
                } catch (Exception ex) {
                    Log.warn(StepFilterTDB.class, "General exception in " + expr, ex) ;
                    return false ;
                }
            }
        } ; 

    
    }
    
    @Override
    public RowList<NodeId> execute(RowList<NodeId> input) {
        Iterator<Row<NodeId>> iterRowFiltered = Iter.filter(input.iterator(), filter) ;
        RowList<NodeId> rlist = RowLib.createRowList(input.vars(), iterRowFiltered) ;
        return rlist ;
    }
    
    private Binding rowToBinding(Row<NodeId> row) {
        return null ;
    }

    @Override
    public String toString() { return "Step/Filter: "+exprs ; }
}

