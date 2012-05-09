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

import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.TableFactory ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter2 ;
import com.hp.hpl.jena.sparql.expr.ExprList ;

/** Join or LeftJoin by calculating both sides, then doing the join
 *  It usually better to use substitute algorithm (not this
 *  QueryIterator in other words) as that is effectively indexing
 *  from one side into the other. */ 
public abstract class QueryIterJoinBase extends QueryIter2
{
    // Use QueryIter2LoopOnLeft
    private QueryIterator current ;
    protected Table tableRight ;          // Materialized iterator
    protected ExprList exprs ;
    private Binding nextBinding = null ;
    
    public QueryIterJoinBase(QueryIterator left, QueryIterator right, ExprList exprs, ExecutionContext execCxt)
    {
        super(left, right, execCxt) ;
        tableRight = TableFactory.create(getRight()) ;
        getRight().close();
        this.exprs = exprs ;
    }

    public QueryIterJoinBase(QueryIterator left, Table right, ExprList exprs, ExecutionContext execCxt)
    {
        super(left, right.iterator(execCxt), execCxt) ;
        this.tableRight = right ;
        this.exprs = exprs ;
    }
    
    @Override
    protected boolean hasNextBinding()
    {
        if ( isFinished() )
            return false ;
        if ( nextBinding != null )
            return true ;

        // No nextBinding - only call to moveToNext
        nextBinding = moveToNext() ;
        return ( nextBinding != null ) ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        if ( nextBinding == null )
            throw new ARQInternalErrorException("moveToNextBinding: slot empty but hasNext was true)") ;
        
        Binding b = nextBinding ;
        nextBinding = null ;
        return b ;
    }

    @Override
    protected void closeSubIterator()
    {
        performClose(current) ;
        if ( tableRight != null ) tableRight.close() ;
        tableRight = null ;
    }
    
    @Override
    protected void requestSubCancel()
    { 
        closeSubIterator() ;
    }

    // Move on regardless.
    private Binding moveToNext()
    {

        while(true)
        {
            if ( current != null )
            {
                if ( current.hasNext() )
                    return current.nextBinding() ;
                // curent ends.
                current.close();
                current = null ;
            }
            
            // Move to next worker
            current = joinWorker() ;
            if ( current == null )
                // No next worker. 
                return null ;
        }
    }
    
    // Null iff there is no more results.  
    abstract protected QueryIterator joinWorker() ;
    
    protected QueryIterator leftJoinWorker()
    {
        if ( !getLeft().hasNext() )
            return null ;
        Binding b =  getLeft().nextBinding() ;
        QueryIterator x = tableRight.matchRightLeft(b, true, exprs, getExecContext()) ;
        return x ;
    }

    protected QueryIterator equiJoinWorker()
    {
        if ( !getLeft().hasNext() )
            return null ;
        if ( exprs != null )
            throw new ARQInternalErrorException("QueryIterJoinBase: expression not empty for equiJoin") ;
        
        Binding b =  getLeft().nextBinding() ;
        QueryIterator x = tableRight.matchRightLeft(b, false, null, getExecContext()) ;
        return x ;
    }
}
