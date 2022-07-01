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

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator ;
import org.apache.jena.sparql.expr.ExprList ;

/**
 * Hash left join.
 * 
 * This code materializes the right hand side into a probe table then hash joins
 * from the left.
 * 
 * See {@link QueryIterHashLeftJoin_Left} for one that uses the right hand side
 * to make the probe table.
 */

//* This code materializes the left into a probe table
//* then hash joins from the right.

public class QueryIterHashLeftJoin_Right extends AbstractIterHashJoin {
    // Left join conditions
    private final ExprList conditions;   
    
    /**
     * Create a hashjoin QueryIterator.
     * @param joinKey  Join key - if null, one is guessed by snooping the input QueryIterators
     * @param left
     * @param right
     * @param conditions 
     * @param execCxt
     * @return QueryIterator
     */
    public static QueryIterator create(JoinKey joinKey, QueryIterator left, QueryIterator right, ExprList conditions, ExecutionContext execCxt) {
        // Easy cases.
        if ( ! left.hasNext() ) {
            left.close() ;
            right.close() ;
            return QueryIterNullIterator.create(execCxt) ;
        }
        if ( ! right.hasNext() ) {
            right.close() ;
            return left ;
        }

        if ( joinKey != null && joinKey.length() > 1 )
            Log.warn(QueryIterHashLeftJoin_Right.class, "Multivariable join key") ; 
        
        return new QueryIterHashLeftJoin_Right(joinKey, left, right, conditions, execCxt) ; 
    }
    
    /**
     * Create a hashjoin QueryIterator.
     * @param left
     * @param right
     * @param execCxt
     * @return QueryIterator
     */
    public static QueryIterator create(QueryIterator left, QueryIterator right, ExprList conditions, ExecutionContext execCxt) {
        return create(null, left, right, conditions, execCxt) ;
    }
    
    private QueryIterHashLeftJoin_Right(JoinKey joinKey, QueryIterator left, QueryIterator right, ExprList conditions, ExecutionContext execCxt) {
        // NB Right. Left
        super(joinKey, right, left, execCxt) ;
        this.conditions = conditions ;
    }

    @Override
    protected Binding yieldOneResult(Binding rowCurrentProbe, Binding rowStream, Binding rowResult) {
        if ( conditions != null && ! conditions.isSatisfied(rowResult, getExecContext()) )
            return null ;
        return rowResult ; 
    }

    @Override
    protected Binding noYieldedRows(Binding rowCurrentProbe) {
        return rowCurrentProbe;
    }
    
    @Override
    protected QueryIterator joinFinished() {
        return null ;
    }
}


