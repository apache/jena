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

package com.hp.hpl.jena.sparql.engine;

import static com.hp.hpl.jena.sparql.algebra.JoinType.* ;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.JoinType ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.table.TableUnit ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterFilterExpr ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;

/** Table join - this only happens if the patterns can not be streamed. */  
public class TableJoin
{
    public static QueryIterator join(QueryIterator left, Table right, ExprList condition, ExecutionContext execCxt) {
        return joinWorker(left, right, PLAIN, condition, execCxt) ;
    }
    
    public static QueryIterator leftJoin(QueryIterator left, Table right, ExprList condition, ExecutionContext execCxt) {
        return joinWorker(left, right, LEFT, condition, execCxt) ;
    }

    public static QueryIterator joinWorker(QueryIterator left, Table right, JoinType joinType, ExprList conditions, ExecutionContext execCxt) {
        if ( right.isEmpty() ) {
            if ( joinType == PLAIN ) {
                // No rows - no match
                left.close() ;
                return QueryIterNullIterator.create(execCxt) ;
            }
            else
                return left ;
        }
        
        if ( TableUnit.isTableUnit(right) ) {
            if ( joinType == PLAIN )
                return applyConditions(left, conditions, execCxt) ;
            else
                return left ;
        }
        return joinWorkerN(left, right, joinType, conditions, execCxt) ;
    }
            
    private static QueryIterator joinWorkerN(QueryIterator left, Table right, JoinType joinType, ExprList conditions, ExecutionContext execCxt) {       
        // We could hash the right except we don't know much about columns.
        
        List<Binding> out = new ArrayList<>() ;
        for ( ; left.hasNext() ; ) {
            Binding bindingLeft = left.next() ;
            int count = 0 ;
            for (Iterator<Binding> iter = right.rows() ; iter.hasNext();) {
                Binding bindingRight = iter.next() ;
                Binding r = Algebra.merge(bindingLeft, bindingRight) ;
                
                if ( r == null )
                    continue ;
                // This does the conditional part. Theta-join.
                if ( conditions == null || conditions.isSatisfied(r, execCxt) ) {
                    count ++ ;
                    out.add(r) ;
                }
            }

            if ( count == 0 && ( joinType == LEFT)  )
                // Conditions on left?
                out.add(bindingLeft) ;
        }
        
        return new QueryIterPlainWrapper(out.iterator(), execCxt) ;
    }
    
    private static QueryIterator applyConditions(QueryIterator qIter, ExprList conditions, ExecutionContext execCxt) {
        if ( conditions == null )
            return qIter ;
        for (Expr expr : conditions)
            qIter = new QueryIterFilterExpr(qIter, expr, execCxt) ;
        return qIter ;
    }
}
