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

package org.seaborne.dboe.engine.join;

import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.lib.SetUtils ;
import org.seaborne.dboe.engine.* ;

import org.apache.jena.sparql.core.Var ;

/** Simple, dependable join.  
 * Two versions: one materializing the whole results, then returning a RowList and
 * one that streams the right-hand-side.  
 */
public class InnerLoopJoin
{
    /** Simple, materializing version - useful for debugging */ 
    public static <X> RowList<X> innerLoopJoinBasic(RowList<X> leftTable, RowList<X> rightTable, RowBuilder<X> builder) {
        Set<Var> vars = SetUtils.union(leftTable.vars(), rightTable.vars()) ;
        Iterable<Row<X>> leftRows = leftTable.toList() ;
        List<Row<X>> output = DS.list() ;
        for ( Row<X> row2 : rightTable ) {
            for ( Row<X> row1 : leftRows ) {
                if ( Join.compatible(row1, row2) ) {
                    builder.reset() ;       
                    output.add(Join.merge(row1, row2, builder)) ;
                }
            }
        }
        return RowLib.createRowList(vars, output.iterator()) ;
    }
    
    /** Streams on the right table, having materialised the left */ 
    public static <X> RowList<X> innerLoopJoin(RowList<X> leftTable, RowList<X> rightTable, RowBuilder<X> builder) {
        Iterator<Row<X>> r = new RowsInnerLoopJoin<X>(leftTable, rightTable, builder) ;
        Set<Var> vars = SetUtils.union(leftTable.vars(), rightTable.vars()) ;
        return RowLib.createRowList(vars, r) ;
    }

    private static class RowsInnerLoopJoin<X> extends IteratorSlotted<Row<X>> implements Iterator<Row<X>> {
        private long s_countLHS = 0 ;
        private long s_countRHS = 0 ;
        private long s_countResults = 0 ;
        
        private final Iterable<Row<X>> leftRows ;
        private Iterator<Row<X>> left ;
        private Iterator<Row<X>> right ;
        private Row<X> rowRight = null ;
        private final RowBuilder<X> builder ;
    
        public RowsInnerLoopJoin(RowList<X> leftTable, RowList<X> rightTable, RowBuilder<X> builder) {
            List<Row<X>> rowsLeftList = leftTable.toList() ;
            leftRows = rowsLeftList ;
            s_countLHS = rowsLeftList.size() ;
            this.right = rightTable.iterator() ;
            this.builder = builder ;
        }
        
        @Override
        protected Row<X> moveToNext() {
            if ( isFinished() )
                return null ;
            
            for ( ;; ) {    // For rows from the right.
                if (rowRight == null ) {
                    if ( right.hasNext() ) {
                        rowRight = right.next();
                        s_countRHS ++ ;
                        left = leftRows.iterator() ;
                    } else 
                        return null ;
                }
    
                // There is a rowRight; it maybe the same as last time.
                while(left.hasNext()) {
                    Row<X> rowLeft = left.next() ;
                    if ( Join.compatible(rowLeft, rowRight) ) {
                        builder.reset() ;
                        s_countResults++ ;
                        return Join.merge(rowLeft, rowRight, builder) ;
                    }
                }
                // Nothing more for this rowRight.
                rowRight = null ;
            }
        }
    
        @Override
        protected boolean hasMore() { return ! isFinished() ; }
        
        @Override
        protected void closeIterator() {
            if ( Quack.JOIN_EXPLAIN ) {
                String x = String.format(
                             "InnerLoopJoin: LHS=%d RHS=%d Results=%d",
                             s_countLHS, s_countRHS, s_countResults) ;
                Quack.joinStatsLog.debug(x);
            }
        }
    }

}

