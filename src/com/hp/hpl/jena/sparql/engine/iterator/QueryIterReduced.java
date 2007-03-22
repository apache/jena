/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/** 
 * @author Andy Seaborne
 * @version $Id: QueryIterDistinct.java,v 1.4 2007/01/02 11:19:31 andy_seaborne Exp $
 */

public class QueryIterReduced extends QueryIter1
{
    Binding lastSeen = null ;
    Binding slot = null ;       // ready to go.
    
    public QueryIterReduced(QueryIterator iter, ExecutionContext context)
    { super(QueryIterFixed.create(iter, context), context)  ; }

    protected void releaseResources()
    { slot = null ; lastSeen = null ; }

    protected boolean hasNextBinding()
    {
        // Already waiting to go.
        if ( slot != null )
            return true ;
        
        // Always moves.
        for ( ; getInput().hasNext() ; )
        {
            Binding b = getInput().nextBinding() ;
            if ( lastSeen == null || ! b.equals(lastSeen) )
            {
                lastSeen = b ;
                slot = b ;
                return true ;
            }
        }
        lastSeen = null ;
        return false ;
    }

    protected Binding moveToNextBinding()
    {
        Binding r = slot ;
        slot = null ;
        return r ;
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