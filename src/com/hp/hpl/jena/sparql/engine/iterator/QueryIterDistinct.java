/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingImmutable;

/** A QueryIterator that surpresses items already seen. 
 * Like com.hp.hpl.jena.util.iterators.UniqueExtendedIterator
 * except this one works on QueryIterators (and hence ClosableIterators)  
 * 
 * @author Andy Seaborne
 * @version $Id: QueryIterDistinct.java,v 1.4 2007/01/02 11:19:31 andy_seaborne Exp $
 */

public class QueryIterDistinct extends QueryIter1
{
    Set seen ; 
    Binding nextBinding = null ;
    
    // Expects the input QueryIterator to be BindingImmutable. 
    
    public QueryIterDistinct(QueryIterator iter, ExecutionContext context)
    {
        super(iter, context) ;
        seen = new HashSet() ;
    }

    protected boolean hasNextBinding()
    {
        if ( isFinished() )
            return false ;
        if ( nextBinding == null )
            nextBinding = moveToNext() ;
        return ( nextBinding != null ) ;
    }

    private Binding moveToNext()
    {
        Binding binding = null ;
        do {
            if ( ! getInput().hasNext() )
                return null ;
            binding = getInput().nextBinding() ;
            if ( ! ( binding instanceof BindingImmutable ) )
                LogFactory.getLog(QueryIterDistinct.class).warn("Not a BindingImmutable (incorrect .hashCode/.equals likely for DISTINCT)") ;
            
        } while ( seen.contains(binding) ) ;
        seen.add(binding) ;
        return binding ;
    }
    
    /** Moves onto the next object. */
    protected Binding moveToNextBinding()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException(this.getClass().getName()+".nextBinding") ;
        
        Binding ret = nextBinding ;
        nextBinding = null ;
        return ret ;
    }
    
    protected void releaseResources()
    {
        seen = null ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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