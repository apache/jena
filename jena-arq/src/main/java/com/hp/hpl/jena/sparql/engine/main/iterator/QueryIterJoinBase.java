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

package com.hp.hpl.jena.sparql.engine.main.iterator;

import com.hp.hpl.jena.sparql.algebra.JoinType ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.TableFactory ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.TableJoin ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter ;
import com.hp.hpl.jena.sparql.expr.ExprList ;

/** Join or LeftJoin by calculating both sides, then doing the join
 *  It usually better to use substitute algorithm (not this
 *  QueryIterator in other words) as that is effectively indexing
 *  from one side into the other. */ 
public class QueryIterJoinBase extends QueryIter
{
    // This should be converted to a hash or sort-merge join.
    private final QueryIterator left ;
    private final QueryIterator right ;
    private final QueryIterator result ;
    
    protected QueryIterJoinBase(QueryIterator left, QueryIterator right, JoinType joinType, ExprList exprs, ExecutionContext execCxt)
    {
        super(execCxt) ;
        this.left = left ;
        this.right = right ;
        this.result = calc(left, right, joinType, exprs, execCxt) ; 
    }

    private static QueryIterator calc(QueryIterator left, QueryIterator right, JoinType joinType, ExprList exprs, ExecutionContext execCxt) {
        Table tableRight = TableFactory.create(right) ;
        return TableJoin.joinWorker(left, tableRight, joinType, exprs, execCxt) ;

    }
    
    @Override
    protected boolean hasNextBinding() {
        return result.hasNext() ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        return result.nextBinding() ;
    }

    @Override
    protected void closeIterator() {
        left.close() ;
        right.close() ;
        result.close() ;
    }

    @Override
    protected void requestCancel() {
        left.cancel() ;
        right.cancel() ;
        result.cancel() ;
    }
}
