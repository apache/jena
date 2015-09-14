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

package org.seaborne.dboe.engine.join2;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.apache.jena.atlas.lib.NotImplemented ;
import org.apache.jena.sparql.expr.ExprList ;
import org.seaborne.dboe.engine.Row ;
import org.seaborne.dboe.engine.RowBuilder ;
import org.seaborne.dboe.engine.RowLib ;
import org.seaborne.dboe.engine.RowList ;

/**
 * Nested Loop left Join (materializing on the right, streaming on the left)
 * A simple, dependable join.
 * <p>
 * See {@link Join#nestedLoopLeftJoinBasic} for a very simple implementation for 
 * testing purposes only. 
 */
public class QueryIterNestedLoopLeftJoin<X> extends IteratorSlotted<Row<X>> {
    // XXX Can we materialise left instead?
    
    private long s_countLHS     = 0;
    private long s_countRHS     = 0;
    private long s_countResults = 0;

    private final ExprList      conditions;
    private final List<Row<X>>  rightRows;
    private Iterator<Row<X>>    right     = null;
    private Iterator<Row<X>>    left;
    private Row<X>              rowLeft = null;
    private boolean foundMatch ;
    private RowBuilder<X> builder;

    public QueryIterNestedLoopLeftJoin(RowList<X> left, RowList<X> right, ExprList exprList, RowBuilder<X> builder) {
        conditions = exprList ;
        rightRows =  right.toList() ;
        s_countRHS = rightRows.size();
        this.left = left.iterator();
        this.builder = builder ;
    }

    @Override
    protected boolean hasMore() {
        if ( isFinished() )
            return false;
        return true;
    }

    @Override
    protected Row<X> moveToNext() {
        if ( isFinished() )
            return null;

        for ( ;; ) { // For rows from the left
            if ( rowLeft == null ) {
                if ( left.hasNext() ) {
                    rowLeft = left.next();
                    foundMatch = false ;
                    s_countLHS++;
                    right = rightRows.iterator();
                } else
                    return null;
            }

            while (right.hasNext()) {
                Row<X> rowRight = right.next();
                Row<X> r = RowLib.mergeRows(rowLeft, rowRight, builder);
                if ( r != null && applyConditions(r) ) {
                    s_countResults++;
                    foundMatch = true ;
                    return r;
                }
            }
            if ( ! foundMatch ) {
                s_countResults++;
                Row<X> r = rowLeft ;
                rowLeft = null; 
                return r ;
            }
            rowLeft = null;
        }
    }
    
    private boolean applyConditions(Row<X>  row) {
        if ( conditions == null )
            return true ;
        throw new NotImplemented() ;
        //return conditions.isSatisfied(binding, getExecContext()) ;
    }
    
    @Override
    protected void closeIterator() {
        if ( JoinLib.JOIN_EXPLAIN ) {
            String x = String.format("InnerLoopJoin: LHS=%d RHS=%d Results=%d", s_countLHS, s_countRHS, s_countResults);
            System.out.println(x);
        }
    }
}
