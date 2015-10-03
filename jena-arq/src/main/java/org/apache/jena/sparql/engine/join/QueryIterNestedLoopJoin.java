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

/**
 * Nested Loop Join (materializing on the left, streaming on the right)
 * A simple, dependable join.
 * <p>
 * See {@link Join#nestedLoopLeftJoinBasic} for a very simple implementation for 
 * testing purposes only. 
 */
public class QueryIterNestedLoopJoin extends QueryIter2 {
    private long s_countLHS     = 0;
    private long s_countRHS     = 0;
    private long s_countResults = 0;

    private final List<Binding> leftRows;
    private Iterator<Binding>   left     = null;
    private QueryIterator       right;
    private Binding             rowRight = null;

    private Binding slot     = null;
    private boolean finished = false;

    public QueryIterNestedLoopJoin(QueryIterator left, QueryIterator right, ExecutionContext cxt) {
        super(left, right, cxt);
        leftRows = Iter.toList(left);
        s_countLHS = leftRows.size();
        this.right = right;
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
                Binding rowLeft = left.next();
                Binding r = Algebra.merge(rowLeft, rowRight);
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
    protected void requestSubCancel() {}

    @Override
    protected void closeSubIterator() {
        if ( JoinLib.JOIN_EXPLAIN ) {
            String x = String.format("InnerLoopJoin: LHS=%d RHS=%d Results=%d", s_countLHS, s_countRHS, s_countResults);
            System.out.println(x);
        }
    }
}
