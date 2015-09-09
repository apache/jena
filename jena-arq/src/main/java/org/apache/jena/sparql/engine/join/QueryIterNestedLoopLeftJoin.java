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

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter2;
import org.apache.jena.sparql.expr.ExprList ;

/**
 * Nested Loop left Join (materializing on the right, streaming on the left)
 * A simple, dependable join.
 * <p>
 * See {@link Join#nestedLoopLeftJoinBasic} for a very simple implementation for 
 * testing purposes only. 
 */
public class QueryIterNestedLoopLeftJoin extends QueryIter2 {
    // XXX Can we materialise left instead?
    
    private long s_countLHS     = 0;
    private long s_countRHS     = 0;
    private long s_countResults = 0;

    private final ExprList conditions;
    private final List<Binding> rightRows;
    private Iterator<Binding>   right     = null;
    private QueryIterator       left;
    private Binding             rowLeft = null;
    private boolean foundMatch ;

    private Binding slot     = null;
    private boolean finished = false;

    public QueryIterNestedLoopLeftJoin(QueryIterator left, QueryIterator right, ExprList exprList, ExecutionContext cxt) {
        super(left, right, cxt);
        conditions = exprList ;
        rightRows =  Iter.toList(right);
        s_countRHS = rightRows.size();
        this.left = left;
    }

    @Override
    protected boolean hasNextBinding() {
        if ( finished )
            return false;
        if ( slot == null ) {
            slot = moveToNextBindingOrNull();
            if ( slot == null ) {
                close();
                return false;
            }
        }
        return true;
    }

    @Override
    protected Binding moveToNextBinding() {
        Binding r = slot;
        slot = null;
        return r;
    }

    protected Binding moveToNextBindingOrNull() {
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
                Binding rowRight = right.next();
                Binding r = Algebra.merge(rowLeft, rowRight);
                if ( r != null && applyConditions(r) ) {
                    s_countResults++;
                    foundMatch = true ;
                    return r;
                }
            }
            if ( ! foundMatch ) {
                s_countResults++;
                Binding r = rowLeft ;
                rowLeft = null; 
                return r ;
            }
            rowLeft = null;
        }
    }
    
    private boolean applyConditions(Binding binding) {
        if ( conditions == null )
            return true ;
        return conditions.isSatisfied(binding, getExecContext()) ;
    }
    
    @Override
    protected void requestSubCancel() {}

    @Override
    protected void closeSubIterator() {
        if ( JoinLib.JOIN_EXPLAIN ) {
            String x = String.format("InnerLoopJoin: LHS=%d RHS=%d Results=%d", s_countLHS, s_countRHS, s_countResults);
            System.out.println(x);
        }
    }
}
