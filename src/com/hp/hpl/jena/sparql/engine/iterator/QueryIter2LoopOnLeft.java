/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 * Includes software from the Apache Software Foundation - Apache Software License (JENA-29)
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.TableFactory ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

/** Binary operation done by looping on the left, and materializing the right - this is not streamed on the right
 * See also QueryIterRepeatApply */
public abstract class QueryIter2LoopOnLeft extends QueryIter2
{
    Table tableRight ; 
    Binding slot = null ;
    
    public QueryIter2LoopOnLeft(QueryIterator left, QueryIterator right, ExecutionContext qCxt)
    {
        super(left, right, qCxt) ;
        
        // Materialized right.
        tableRight = TableFactory.create(getRight()) ;
        getRight().close();
    }

    @Override
    protected final void closeSubIterator()
    { tableRight.close(); }
    
    @Override
    protected void requestSubCancel()
    { tableRight.close() ; }
   
    @Override
    protected final boolean hasNextBinding()
    {
        if ( slot != null )
            return true ;
        
        while ( getLeft().hasNext() )
        {
            Binding bindingLeft = getLeft().nextBinding() ;
            slot = getNextSlot(bindingLeft) ;
            if ( slot != null )
            {
                slot = bindingLeft ; 
                return true ;
            }
        }
        getLeft().close() ;
        return false ;
    }

    protected abstract Binding getNextSlot(Binding bindingLeft) ;

    @Override
    protected final Binding moveToNextBinding()
    {
        if ( ! hasNextBinding() )
            return null ;
        Binding x = slot ;
        slot = null ;
        return x ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
 * (c) Copyright 2011 Epimorphics Ltd.
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