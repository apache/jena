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

import java.util.Iterator ;

import org.apache.jena.atlas.logging.Log ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.join.Hasher ;
import org.seaborne.dboe.engine.join.JL ;

/** Hash left join. 
 * This code materializes the right into a probe table
 * then hash joins from the left.
 */

//* This code materializes the left into a probe table
//* then hash joins from the right.

public class QueryIterHashJoin<X> extends AbstractIterHashJoin<X> {
    
    /**
     * Create a hashjoin QueryIterator.
     * @param joinKey  Join key - if null, one is guessed by snooping the input QueryIterators
     * @param left
     * @param right
     * @param builder
     * @return QueryIterator
     */
    public static <X> RowList<X> create(JoinKey joinKey, RowList<X> left, RowList<X> right, RowBuilder<X> builder) {
//        // Easy cases.
//        if ( ! left.hasNext() || ! right.hasNext() ) {
//            left.close() ;
//            right.close() ;
//            return QueryIterNullIterator.create(execCxt) ;
//        }
        if ( joinKey != null && joinKey.length() > 1 )
            Log.warn(QueryIterHashJoin.class, "Multivariable join key") ; 
        Iterator<Row<X>> r = new QueryIterHashJoin<>(joinKey, left, right, JL.hash(), builder) ;
        return RowLib.createRowList(left.vars(), right.vars(), r) ;
    }
    
    /**
     * Create a hashjoin QueryIterator.
     * @param left
     * @param right
     * @param builder
     * @return QueryIterator
     */
 
    public static <X> RowList<X> create(RowList<X> left, RowList<X> right, RowBuilder<X> builder) {
        return create(null, left, right, builder) ;
    }
    
    private QueryIterHashJoin(JoinKey joinKey, RowList<X> left, RowList<X> right, Hasher<X> hasher, RowBuilder<X> builder) {
        super(joinKey, left, right, hasher, builder) ;
    }

    @Override
    protected Row<X> yieldOneResult(Row<X> rowCurrentProbe, Row<X> rowStream, Row<X> rowResult) {
        return rowResult ;
    }

    @Override
    protected Row<X> noYieldedRows(Row<X> rowCurrentProbe) {
        return null;
    }
    
    @Override
    protected Iterator<Row<X>> joinFinished() {
        return null;
    }

}
