/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package engine3.iterators;

import com.hp.hpl.jena.query.core.ARQInternalErrorException;
import com.hp.hpl.jena.query.engine.Binding;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine2.Table;
import com.hp.hpl.jena.query.engine2.TableFactory;
import com.hp.hpl.jena.query.expr.Expr;

/** Left join by materializing the RHS */
public class QueryIterLeftJoin extends QueryIter
{
    QueryIterator left ;
    QueryIterator current ;
    Table tableRight ;
    Expr expr ;
    private Binding nextBinding = null ;
    
    public QueryIterLeftJoin(QueryIterator left, QueryIterator right, Expr expr, ExecutionContext qCxt)
    {
        super(qCxt) ;
        this.left = left ;
        tableRight = TableFactory.create(right) ;
        tableRight.materialize() ;              // Delay this?
        right.close();
        this.expr = expr ;
    }

    protected void closeIterator()
    {
        left.close() ;
        tableRight = null ;
    }
    
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

    protected Binding moveToNextBinding()
    {
        if ( nextBinding == null )
            throw new ARQInternalErrorException("moveToNextBinding: slot empty but hasNext was true)") ;
        
        Binding b = nextBinding ;
        nextBinding = null ;
        return b ;
    }

    // Move on regardless.
    private Binding moveToNext()
    {
        if ( current != null && current.hasNext() )
            return current.nextBinding() ;

        if ( current != null )
            current.close();
        current = joinWorker() ;
        if ( current == null )
            return null ;
        return current.nextBinding() ;
    }
    
    private QueryIterator joinWorker()
    {
        if ( !left.hasNext() )
            return null ;
        Binding b =  left.nextBinding() ;
        QueryIterator x = tableRight.matchRightLeft(b, true, expr, getExecContext()) ;
        return x ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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