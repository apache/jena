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

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.seaborne.dboe.engine.Row ;
import org.seaborne.dboe.engine.RowBuilder ;
import org.seaborne.dboe.engine.RowLib ;
import org.seaborne.dboe.engine.RowList ;

/**
 * Nested Loop Join (materializing on the left, streaming on the right)
 * A simple, dependable join.
 * <p>
 */

public class QueryIterNestedLoopJoin<X> extends IteratorSlotted<Row<X>> {
    private long s_countLHS     = 0;
    private long s_countRHS     = 0;
    private long s_countResults = 0;

    private final List<Row<X>> leftRows;
    private Iterator<Row<X>>   left     = null;
    private Iterator<Row<X>>   right;
    private Row<X>             rowRight = null;
    private RowBuilder<X> builder;

    public QueryIterNestedLoopJoin(RowList<X> left, RowList<X> right, RowBuilder<X> builder) {
        leftRows = Iter.toList(left.iterator());
        s_countLHS = leftRows.size();
        this.right = right.iterator() ;
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

        for ( ;; ) { // For rows from the right.
            if ( rowRight == null ) {
                if ( right.hasNext() ) {
                    rowRight = right.next();
                    s_countRHS++;
                    left = leftRows.iterator();
                } else
                    return null;
            }

            // There is a rowRight
            while (left.hasNext()) {
                Row<X> rowLeft = left.next();
                Row<X> r = RowLib.mergeRows(rowLeft, rowRight, builder);
                if ( r != null ) {
                    s_countResults++;
                    return r;
                }
            }
            // Nothing more for this rowRight.
            rowRight = null;
        }
    }

    @Override
    protected void closeIterator() {
        if ( JoinLib.JOIN_EXPLAIN ) {
            String x = String.format("InnerLoopJoin: LHS=%d RHS=%d Results=%d", s_countLHS, s_countRHS, s_countResults);
            System.out.println(x);
        }
    }
}
