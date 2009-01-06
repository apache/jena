/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/** Iterator until a limit is reached. 
 * 
 * @author Andy Seaborne
 */

public class QueryIterSlice extends QueryIter1
{
    long count = 0 ;
    long limit ;
    long offset ;
    
    /** Create an iterator that limits the number of returns of
     * another CloseableIterator.
     * 
     * @param cIter            The closable iterator to throttle 
     * @param startPosition    Offset of start after - 0 is the no-op.
     * @param numItems         Maximium number of items to yield.  
     */
    
    public QueryIterSlice(QueryIterator cIter, long startPosition, long numItems, ExecutionContext context)
    {
        super(cIter, context) ;
        
        offset = startPosition ;
        if ( offset == Query.NOLIMIT )
            offset = 0 ;
        
        limit = numItems ;
        if ( limit == Query.NOLIMIT )
            limit = Long.MAX_VALUE ;

        if ( limit < 0 )
            throw new QueryExecException("Negative LIMIT: "+limit) ;
        if ( offset < 0 )
            throw new QueryExecException("Negative OFFSET: "+offset) ;
        
        count = 0 ;
        // Offset counts from 0 (the no op).
        for ( int i = 0 ; i < offset ; i++ )
        {
            // Not subtle
            if ( !cIter.hasNext() ) { close() ; break ; }
            cIter.next() ;
        }
    }
    
    @Override
    protected boolean hasNextBinding()
    {
        if ( isFinished() )
            return false;
        
        if ( ! getInput().hasNext() )
            return false ;
        
        if ( count >= limit )
            return false ;

        return true ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        count ++ ;
        return getInput().nextBinding() ;
    }

    @Override
    protected void closeSubIterator() {}
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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