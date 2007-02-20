/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.iterator;

import java.util.NoSuchElementException;

import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.util.Utils;

/** An iterator that returns at least one element from another iterator
 *  or a default value (once) if the wrapped iterator returns nothing.
 * 
 * @author Andy Seaborne
 * @version $Id: QueryIterDefaulting.java,v 1.6 2007/02/06 17:06:03 andy_seaborne Exp $
 */ 

public class QueryIterDefaulting extends QueryIter 
{
    Binding defaultObject ;
    QueryIterator cIter ;
    
    boolean returnDefaultObject = false ;
    boolean haveReturnedSomeObject = false ; 

    public QueryIterDefaulting(QueryIterator _cIter, Binding _defaultObject, ExecutionContext qCxt) 
    {
        super(qCxt) ;
        cIter = _cIter ;
        defaultObject = _defaultObject ;
    }

    protected boolean hasNextBinding()
    {
        if ( isFinished() )
            return false ;

        if ( cIter != null && cIter.hasNext() )
            return true ;
        
        // Wrapped iterator has ended (or does not exist).  Have we returned anything yet? 
        
        if ( haveReturnedSomeObject )
            return false ;
        
        returnDefaultObject = true ;
        return true ;
    }

    protected Binding moveToNextBinding()
    {
        if ( isFinished() )
            throw new NoSuchElementException(Utils.className(this)) ;
        
        if ( returnDefaultObject )
        {
            haveReturnedSomeObject = true ;
            return defaultObject ;
        }

        Binding binding = null ;
        if ( cIter != null && cIter.hasNext() )
            binding = (Binding)cIter.next() ;
        else
        {
            if ( haveReturnedSomeObject )
                throw new NoSuchElementException("DefaultingIterator - without hasNext call first") ;
            binding = defaultObject ;
        }
        
        haveReturnedSomeObject = true ;
        return binding ;
    }

    protected void closeIterator()
    {
        if ( cIter != null )
        {
            cIter.close() ;
            cIter = null ;
        }
    }
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
