/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main.iterator;

import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.TableFactory;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter2;
import com.hp.hpl.jena.sparql.expr.ExprList;

/** Join or LeftJoin by calculating both sides, then doing the join
 *  It usually better to use substitute algorithm (not this
 *  QueryIterator in other words) as that is effectively indexing
 *  from one side into the other.  
 * 
 * @author Andy Seaborne
 */ 
public abstract class QueryIterJoinBase extends QueryIter2
{
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
    protected void releaseResources()
    {
        if ( current != null )
            current.close() ;
        tableRight = null ;
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

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */