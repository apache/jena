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
import java.util.List ;

import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.lib.NotImplemented ;
import org.apache.jena.sparql.engine.main.OpExecutor ;
import org.apache.jena.sparql.expr.ExprList ;
import org.seaborne.dboe.engine.* ;

/** API to various join algorithms */
public class Join {
    // See also package org.apache.jena.sparql.engine.index
    // The anti-join code for MINUS
    
    private final static boolean useNestedLoopJoin      = false ;
    private final static boolean useNestedLoopLeftJoin  = false ;

    /**
     * Standard entry point to a join of two streams.
     * This is not a substitution/index join.
     * (See {@link OpExecutor} for streamed execution using substitution).
     * @param left
     * @param right
     * @param builder
     * @return QueryIterator
     */
    public static <X> RowList<X> join(RowList<X> left, RowList<X> right, RowBuilder<X> builder) {
        if ( false )
            return debug(left, right, builder,
                         (_left, _right)->hashJoin(_left, _right, builder)) ;
        if ( useNestedLoopJoin )
            return nestedLoopJoin(left, right, builder) ;
        return hashJoin(left, right, builder) ;
    }
   
//    /** Standard entry point to a left join of two streams.
//     * This is not a substitution/index join.
//     * (See {@link OpExecutor} for streamed execution using substitution).
//     *
//     * @param left
//     * @param right
//     * @param conditions
//     * @param builder
//     * @return QueryIterator
//     */
//    public static <X> RowList<X> leftJoin(RowList<X> left, RowList<X> right, ExprList conditions, RowBuilder<X> builder) {
//        if ( false )
//            return debug(left, right, builder, 
//                         (_left, _right)->hashLeftJoin(_left, _right, conditions, builder)) ;
//        if ( useNestedLoopLeftJoin )
//            return nestedLoopLeftJoin(left, right, conditions, builder) ;
//        return hashLeftJoin(left, right, conditions, builder) ;
//    }

    /* Debug.
     * Print inputs and outputs.
     * This involves materializing the iterators.   
     */
    private interface JoinOp<X> { 
        public RowList<X> exec(RowList<X> left, RowList<X> right) ;
    }
    
    /** Inner loop join.
     *  Cancellable.
     * @param left      Left hand side
     * @param right     Right hand side
     * @param builder       ExecutionContext
     * @return          QueryIterator
     */ 
    public static <X> RowList<X> nestedLoopJoin(RowList<X> left, RowList<X> right, RowBuilder<X> builder) {
        return create(left, right, new QueryIterNestedLoopJoin<>(left, right, builder)) ;
    }
    
//    
//    /** Inner loop join.
//     *  Cancellable.
//     * @param left      Left hand side
//     * @param right     Right hand side
//     * @param builder       ExecutionContext
//     * @return          QueryIterator
//     */ 
//    public static <X> RowList<X> nestedLoopLeftJoin(RowList<X> left, RowList<X> right, ExprList conditions, RowBuilder<X> builder) {
//        return new QueryIterNestedLoopLeftJoin(left, right, conditions, builder) ;
//    }

    /** Evaluate using a hash join.
     * 
     * @param left      Left hand side
     * @param right     Right hand side
     * @param builder   Row builder  
     * @return          QueryIterator
     */
    public static <X> RowList<X> hashJoin(RowList<X> left, RowList<X> right, RowBuilder<X> builder) {
        return QueryIterHashJoin.create(left, right, builder) ;
    }

    /** Evaluate using a hash join.
     * 
     * @param joinKey   The key for the probe table.
     * @param left      Left hand side
     * @param right     Right hand side
     * @param builder   Row builder
     * @return          QueryIterator
     */
    public static <X> RowList<X> hashJoin(JoinKey joinKey, RowList<X> left, RowList<X> right, RowBuilder<X> builder) {
        return QueryIterHashJoin.create(joinKey, left, right, builder) ;
    }

//    /**
//     * Left outer join by using hash join. Normally, this is
//     * hashing the right hand side and streaming the left.  The reverse
//     * implementation (hash left, stream right) is also available.   
//     * @param left
//     * @param right
//     * @param conditions
//     * @param builder
//     * @return QueryIterator
//     */
//    public static <X> RowList<X> hashLeftJoin(RowList<X> left, RowList<X> right, ExprList conditions, RowBuilder<X> builder) {
//        return QueryIterHashLeftJoin_Right.create(left, right, conditions, builder) ;
//    }
//
//    /**
//     * Left outer join by using hash join. Normally, this is
//     * hashing the right hand side and streaming the left.  The reverse
//     * implementation (hash left, stream right) is also available.   
//     * @param joinKey
//     * @param left
//     * @param right
//     * @param conditions
//     * @param builder
//     * @return QueryIterator
//     */
//    public static <X> RowList<X> hashLeftJoin(JoinKey joinKey, RowList<X> left, RowList<X> right, ExprList conditions, RowBuilder<X> builder) {
//        return QueryIterHashLeftJoin_Right.create(joinKey, left, right, conditions, builder) ;
//    }

    /** Very simple, materializing version - useful for debugging.
     *  Builds output early. Materializes left, streams right.
     *  Does <b>not</b> scale. 
     *  No cancelation, no stats.
     * 
     * @see #nestedLoopJoin
     */
    public static <X> RowList<X> nestedLoopJoinBasic(RowList<X> left, RowList<X> right, RowBuilder<X> builder) {
        List<Row<X>> leftRows = left.toList() ;
        List<Row<X>> output = DS.list() ;
        for ( Row<X> row2 : right) {
            for ( Row<X> row1 : leftRows ) {
                Row<X> r = RowLib.mergeRows(row1, row2, builder) ;
                if ( r != null )
                    output.add(r) ;
            }
        }
        return create(left, right, output.iterator()) ;
    }
    
//
    /** Very simple, materializing version for leftjoin - useful for debugging.
     *  Builds output early. Materializes right, streams left.
     *  Does <b>not</b> scale. 
     */
    public static <X> RowList<X> nestedLoopLeftJoinBasic(RowList<X> left, RowList<X> right, ExprList conditions, RowBuilder<X> builder) {
        // Stream from left, materialize right.
        List<Row<X>> rightRows = right.toList() ;
        
        List<Row<X>> output = DS.list() ;
        long count = 0 ;
        for ( Row<X> row1 : left ) {
            boolean match = false ;
            for ( Row<X> row2 : rightRows ) {
                Row<X> r = RowLib.mergeRows(row1, row2, builder) ;
                if ( r != null && applyConditions(r, conditions)) {
                    output.add(r) ;
                    match = true ;
                }
            }
            if ( ! match )
                output.add(row1) ;
        }
        return create(left, right, output.iterator()) ;
    }

    private static <X> RowList<X> create(RowList<X> left, RowList<X> right, Iterator<Row<X>> rows) {
        return RowLib.createRowList(left.vars(), right.vars(), rows) ; 
    }

    // apply conditions.
    private static <X> boolean applyConditions(Row<X> row, ExprList conditions) {
        if ( conditions == null )
            return true ;
        //return conditions.isSatisfied(row, null) ;
        throw new NotImplemented() ;
    }

    private static <X> RowList<X> debug(RowList<X> left, RowList<X> right, RowBuilder<X> builder, JoinOp<X> action) {
        return null ;
//            Table t1 = TableFactory.create(left) ;
//            Table t2 = TableFactory.create(right) ;
//    
//            left = t1.iterator(builder) ;
//            right = t2.iterator(builder) ;
//    
//            QueryIterator qIter = action.exec(left, right) ;
//            Table t3 = TableFactory.create(qIter) ;
//            System.out.println("** Left") ;
//            System.out.println(t1) ;
//            System.out.println("** Right") ;
//            System.out.println(t2) ;
//            System.out.println("** ") ;
//            System.out.println(t3) ;
//    //        // Could do again here, different algoithm for comparison.
//    //        left = t1.iterator(builder) ;
//    //        right = t2.iterator(builder) ;
//    //        System.out.println("** nestedLoopJoin") ;
//    //        Table t4 = TableFactory.create(?????) ;
//    //        System.out.println(t4) ;
//            return t3.iterator(builder) ;
        }
}
