/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.engine.join;

import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.lib.NotImplemented ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.JoinType ;
import org.apache.jena.sparql.algebra.Table ;
import org.apache.jena.sparql.algebra.TableFactory ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterFilterExpr ;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import org.apache.jena.sparql.engine.main.OpExecutor ;
import org.apache.jena.sparql.engine.ref.TableJoin ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;

/** API to various join algorithms */
public class Join {
    // See also package org.apache.jena.sparql.engine.index
    // The anti-join code for MINUS
    
    private final static boolean useNestedLoopJoin = false ;
    private final static boolean useNestedLoopLeftJoin = false ;

    /**
     * Standard entry point to a join of two streams.
     * This is not a substitution/index join.
     * (See {@link OpExecutor} for streamed execution using substitution).
     * @param left
     * @param right
     * @param execCxt
     * @return QueryIterator
     */
    public static QueryIterator join(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        if ( useNestedLoopJoin )
            return nestedLoopJoin(left, right, execCxt) ;
        return hashJoin(left, right, execCxt) ;
    }
   
    /** Standard entry point to a left join of two streams.
     * This is not a substitution/index join.
     * (See {@link OpExecutor} for streamed execution using substitution).
     *
     * @param left
     * @param right
     * @param conditions
     * @param execCxt
     * @return QueryIterator
     */
    public static QueryIterator leftJoin(QueryIterator left, QueryIterator right, ExprList conditions, ExecutionContext execCxt) {
//        if ( useNestedLoopJoin )
//            return nestedLoopLeftJoin(left, right, conditions, execCxt) ;
//        return hashLeftJoin(left, right, execCxt) ;
        return nestedLoopLeftJoin(left, right, conditions, execCxt) ;
    }
   

    /**
     * Standard entry point to a join of two streams.
     * This is not a substitution/index join.
     * (See {@link OpExecutor} for streamed execution using substitution).
     * @deprecated Do not use directly.
     * 
     * @param left
     * @param right
     * @param joinType
     * @param conditions
     * @param execCxt
     * @return QueryIterator
     */
    @Deprecated
    public static QueryIterator joinWorker(QueryIterator left, QueryIterator right, 
                                           JoinType joinType, ExprList conditions,
                                           ExecutionContext execCxt) {
        if ( false ) {
            // Safe, and slow.
            switch(joinType) {
                case INNER: return nestedLoopJoin(left, right, execCxt) ;
                case LEFT:  return nestedLoopLeftJoin(left, right, conditions, execCxt) ;
                default:    throw new NotImplemented("JoinType "+joinType+" not implemented") ;
            }
        }
        if ( false ) {
            // Very safe, and slow.
            switch(joinType) {
                case INNER: return nestedLoopJoinBasic(left, right, execCxt) ;
                case LEFT:  return nestedLoopLeftJoinBasic(left, right, conditions, execCxt) ;
                default:    throw new NotImplemented("JoinType "+joinType+" not implemented") ;
            }
        }

        if ( false ) {
            // Very safe, well used old code.  And slow.
            Table t = TableFactory.create(right) ;
            return TableJoin.joinWorker(left, t, joinType, conditions, execCxt) ;
        }
        
        // Normal.
        switch(joinType) {
            case INNER: return join(left, right, execCxt) ;
            case LEFT:  return leftJoin(left, right, conditions, execCxt) ;
            default:    throw new NotImplemented("JoinType "+joinType+" not implemented") ;
        }
    }

    /** Inner loop join.
     *  Cancellable.
     * @param left      Left hand side
     * @param right     Right hand side
     * @param execCxt       ExecutionContext
     * @return          QueryIterator
     */ 
    public static QueryIterator nestedLoopJoin(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        return new QueryIterNestedLoopJoin(left, right, execCxt) ;
    }

    /** Inner loop join.
     *  Cancellable.
     * @param left      Left hand side
     * @param right     Right hand side
     * @param execCxt       ExecutionContext
     * @return          QueryIterator
     */ 
    public static QueryIterator nestedLoopLeftJoin(QueryIterator left, QueryIterator right, ExprList conditions, ExecutionContext execCxt) {
        return new QueryIterNestedLoopLeftJoin(left, right, conditions, execCxt) ;
    }


    /** Evaluate using a hash join.
     * 
     * @param left      Left hand side
     * @param right     Right hand side
     * @param execCxt   ExecutionContext
     * @return          QueryIterator
     */
    public static QueryIterator hashJoin(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        return QueryIterHashJoin.create(left, right, execCxt) ;
    }

    /** Evaluate using a hash join.
     * 
     * @param joinKey   The key for the probe table.
     * @param left      Left hand side
     * @param right     Right hand side
     * @param execCxt   ExecutionContext
     * @return          QueryIterator
     */
    public static QueryIterator hashJoin(JoinKey joinKey, QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        return QueryIterHashJoin.create(joinKey, left, right, execCxt) ;
    }

    /** Very simple, materializing version - useful for debugging.
     *  Builds output early. Materializes left, streams right.
     *  Does <b>not</b> scale. 
     *  No cancelation, no stats.
     * 
     * @see #nestedLoopJoin
     */
    public static QueryIterator nestedLoopJoinBasic(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        List<Binding> leftRows = Iter.toList(left) ;
        List<Binding> output = DS.list() ;
        for ( ; right.hasNext() ; ) {
            Binding row2 = right.next() ;
            for ( Binding row1 : leftRows ) {
                Binding r = Algebra.merge(row1, row2) ;
                if ( r != null )
                    output.add(r) ;
            }
        }
        return new QueryIterPlainWrapper(output.iterator(), execCxt) ;
    }

    /** Very simple, materializing version for leftjoin - useful for debugging.
     *  Builds output early. Materializes right, streams left.
     *  Does <b>not</b> scale. 
     */
    public static QueryIterator nestedLoopLeftJoinBasic(QueryIterator left, QueryIterator right, ExprList condition, ExecutionContext execCxt) {
        // Stream from left, materialize right.
        List<Binding> rightRows = Iter.toList(right) ;
        List<Binding> output = DS.list() ;
        long count = 0 ;
        for ( ; left.hasNext() ; ) {
            Binding row1 = left.next() ;
            boolean match = false ;
            for ( Binding row2 : rightRows ) {
                Binding r = Algebra.merge(row1, row2) ;
                if ( r != null ) {
                    output.add(r) ;
                    match = true ;
                }
            }
            if ( ! match )
                output.add(row1) ;
        }
        QueryIterator qIter = new QueryIterPlainWrapper(output.iterator(), execCxt) ;
        qIter = applyConditions(qIter, condition, execCxt) ;
        return qIter ;
    }

    // apply conditions.
    private static QueryIterator applyConditions(QueryIterator qIter, ExprList conditions, ExecutionContext execCxt) {
        if ( conditions == null )
            return qIter ;
        for (Expr expr : conditions)
            qIter = new QueryIterFilterExpr(qIter, expr, execCxt) ;
        return qIter ;
    }
}

