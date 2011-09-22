/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 * Includes software from the Apache Software Foundation - Apache Software Licnese (JENA-29)
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

/** Implementation skeleton for DISTINCT and REDUCED. */

public abstract class QueryIterDistinctReduced extends QueryIter1
{
    Binding slot = null ;       // ready to go.
    
    public QueryIterDistinctReduced(QueryIterator iter, ExecutionContext context)
    { super(iter, context)  ; }

    // Subclasses will want to implement this as well. 
    @Override
    protected void closeSubIterator()
    { slot = null ; }

    // Subclasses will want to implement this as well. 
    @Override
    protected void requestSubCancel()
    { }
    
    @Override
    final
    protected boolean hasNextBinding()
    {
        // Already waiting to go.
        if ( slot != null )
            return true ;
        
        // Always moves.
        for ( ; getInput().hasNext() ; )
        {
            Binding b = getInput().nextBinding() ;
            if ( ! isDuplicate(b) )
            {
                // new - remember and return
                remember(b) ;
                slot = b ;
                return true ;
            }
        }
        return false ;
    }

    @Override
    final
    protected Binding moveToNextBinding()
    {
        Binding r = slot ;
        slot = null ;
        return r ;
    }
    
    protected abstract boolean isDuplicate(Binding binding) ;
    
    protected abstract void remember(Binding binding) ;
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