/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/** A caching QueryIterator.  On demand, the application can ask for a new
 *  query iterator which will repeat the bindings yielded so far.
 */  

public 
class QueryIteratorCaching extends QueryIteratorWrapper
{
    // Not tracked.
    List<Binding> cache = new ArrayList<Binding>() ;
    
    public QueryIteratorCaching(QueryIterator qIter)
    {
        super(qIter) ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        Binding b = super.moveToNextBinding() ;
        cache.add(b) ;
        return b ;
    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {}
    
    
    public QueryIteratorCaching createRepeat()
    {
        List<Binding> elements = cache ;
        if ( super.hasNext() )
            // If the iterator isn't finished, copy what we have so far.
            elements = new ArrayList<Binding>(cache) ;
        
        return new QueryIteratorCaching(new QueryIterPlainWrapper(elements.iterator(), null)) ;
    }
    
    public static QueryIterator reset(QueryIterator qIter)
    {
        if ( qIter instanceof QueryIteratorCaching )
        {
            QueryIteratorCaching cIter = (QueryIteratorCaching)qIter ;
            return cIter.createRepeat() ;
        }
            
        return qIter ;
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