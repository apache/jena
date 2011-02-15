/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 * Includes software from the Apache Software Foundation - Apache Software License (JENA-29)
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.List ;

import org.openjena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;

/** A QueryIterator that copies an iterator.
 *  @see QueryIter#materialize
 */  

class QueryIteratorCopy extends QueryIteratorBase
{
    // Not tracked.
    List<Binding> elements = new ArrayList<Binding>() ;
    QueryIterator iterator ;
    
    QueryIterator original ;        // Keep for debugging - This is closed as it is copied.
    
    public QueryIteratorCopy(QueryIterator qIter)
    {
        for ( ; qIter.hasNext() ; )
            elements.add(qIter.nextBinding()) ;
        qIter.close() ;
        iterator = copy() ;
        original = qIter ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        return iterator.nextBinding() ;
    }

    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        out.print("QueryIteratorCopy") ;
        out.incIndent() ;
        original.output(out, sCxt) ;
        out.decIndent() ;
    }
    
    
    public List<Binding> elements()
    {
        return Collections.unmodifiableList(elements) ;
    }
    
    public QueryIterator copy()
    {
        return new QueryIterPlainWrapper(elements.iterator()) ;
    }

    @Override
    protected void closeIterator()
    { iterator.close() ; }
    
    @Override
    protected void requestCancel()
    { iterator.cancel() ; }

    @Override
    protected boolean hasNextBinding()
    {
        return iterator.hasNext() ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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