/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class QueryIterGroup extends QueryIter
{
    // Base implementation - single group over the whole result set.
    // Maybe this should not be a QueryIter at all.  Just the grouping 
    private List groups ;
    private Iterator groupIter ;
    private QueryIterator current ;
    
    public QueryIterGroup(List iterators, ExecutionContext execCxt)
    {
        super(execCxt) ;
        this.groups = iterators ;
        this.groupIter = iterators.iterator() ;
        this.current = null ;
    }

    public boolean hasNextGroup()
    { return groupIter.hasNext() ; }

    public QueryIterator nextGroup()
    {
        if ( ! hasNextGroup() )
            throw new NoSuchElementException("QueryIterGroup.nextGroup") ;
        return (QueryIterator)groupIter.next();
    }
    
    protected void closeIterator()
    {
        if ( current != null )
            current.close() ;
        current = null ;
    }

    protected boolean hasNextBinding()
    {
        while ( ! current.hasNext() )
        {
            if ( current != null )
                current.close() ;
            if ( ! hasNextGroup() )
                return false ;
            current = nextGroup() ;
        }
        return true ;
    }

    protected Binding moveToNextBinding()
    {
        if ( ! hasNextBinding() )
            return null ;
        return current.nextBinding() ;
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