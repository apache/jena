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

/** Hash left join. 
 * This code materializes the right into a probe table
 * then hash joins from the left.
 */

//* This code materializes the left into a probe table
//* then hash joins from the right.

public class QueryIterHashJoin extends AbstractIterHashJoin {
    
    /**
     * Create a hashjoin QueryIterator.
     * @param joinKey  Join key - if null, one is guessed by snooping the input QueryIterators
     * @param left
     * @param right
     * @param execCxt
     * @return QueryIterator
     */
    public static QueryIterator create(JoinKey joinKey, QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        // Easy cases.
        if ( ! left.hasNext() || ! right.hasNext() ) {
            left.close() ;
            right.close() ;
            return QueryIterNullIterator.create(execCxt) ;
        }
        if ( joinKey != null && joinKey.length() > 1 )
            Log.warn(QueryIterHashJoin.class, "Multivariable join key") ; 
        return new QueryIterHashJoin(joinKey, left, right, execCxt) ; 
    }
    
    /**
     * Create a hashjoin QueryIterator.
     * @param left
     * @param right
     * @param execCxt
     * @return QueryIterator
     */
 
    public static QueryIterator create(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        return create(null, left, right, execCxt) ;
    }
    
    private QueryIterHashJoin(JoinKey joinKey, QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        super(joinKey, left, right, execCxt) ;
    }

    @Override
    protected Binding yieldOneResult(Binding rowCurrentProbe, Binding rowStream, Binding rowResult) {
        return rowResult ;
    }

    @Override
    protected Binding noYieldedRows(Binding rowCurrentProbe) {
        return null;
    }
    
    @Override
    protected QueryIterator joinFinished() {
        return null;
    }

}
