/*
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

package org.apache.jena.sparql.engine.iterator;

import java.util.Iterator;

import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;

/** SemiJoin and AntiJoin */
public class QueryIterHalfJoin extends QueryIter2LoopOnLeft {

    public static QueryIterator semiJoin(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        return new QueryIterHalfJoin(HALF_JOIN.SEMI, left, right, execCxt);
    }

    public static QueryIterator antiJoin(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        return new QueryIterHalfJoin(HALF_JOIN.ANTI, left, right, execCxt);
    }

    private final HALF_JOIN halfJoin;

    private QueryIterHalfJoin(HALF_JOIN halfJoin, QueryIterator left, QueryIterator right, ExecutionContext qCxt) {
        super(left, right, qCxt);
        this.halfJoin = halfJoin;
    }

    private enum HALF_JOIN {
        SEMI {
            @Override public Binding onOneMatch(Binding bindingLeft) { return bindingLeft; }
            @Override public Binding onNoMatches(Binding bindingLeft) { return null; }
        } ,
        ANTI {
            @Override public Binding onOneMatch(Binding bindingLeft) { return null; }
            @Override public Binding onNoMatches(Binding bindingLeft) { return bindingLeft; }
        }
        ;
        public abstract Binding onOneMatch(Binding bindingLeft);
        public abstract Binding onNoMatches(Binding bindingLeft);
    };

    // If this is to become serious it needs improving - take the hash join code from QueryIterMinus.
    @Override
    protected Binding getNextSlot(Binding bindingLeft) {
        boolean accept = true;
        for ( Iterator<Binding> iter = tableRight.iterator(null) ; iter.hasNext() ; ) {
            Binding bindingRight = iter.next();
            boolean matches = Algebra.compatible(bindingLeft, bindingRight);
            if ( matches )
                return halfJoin.onOneMatch(bindingLeft);
        }
        return halfJoin.onNoMatches(bindingLeft);
    }
}
