/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.extension.library;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.engine.iterator.QueryIteratorBase;
import com.hp.hpl.jena.query.extension.Extension;
import com.hp.hpl.jena.query.serializer.SerializationContext;
import com.hp.hpl.jena.query.util.IndentedWriter;


/** Extension that reads its input iterator when first used, 
 *  then yields the contents one solution at a time.  
 * 
 * @author Andy Seaborne
 * @version $Id: cache.java,v 1.18 2007/02/06 17:06:17 andy_seaborne Exp $
 */
public class cache implements Extension
{
    public QueryIterator exec(QueryIterator input, List args, String uri,
                              ExecutionContext execCxt)
    {
        return new CachingIterator(input) ;
    }

    public void build(String uri, List args)
    {
        if ( args.size() != 0 )
            throw new QueryBuildException("Extension 'cache' takes no arguments") ;
    }
}


// Extract
// Should be QueryIter?
class CachingIterator extends QueryIteratorBase
{
    boolean initialized = false ;
    List solutions = null ;
    QueryIterator input ;
    Iterator iterator ;
    
    CachingIterator(QueryIterator qIter) { super() ; input = qIter; }
    
    public void close()
    { 
        //init() ;
        solutions = null ;
        iterator = null ;
        input = null ;
    }
    
    private void init()
    {
        if ( initialized )
            return ;
        solutions = new ArrayList() ;
        for ( ; input.hasNext() ; )
            solutions.add(input.nextBinding()) ;
        input.close() ;
        input = null ; 
        iterator = solutions.iterator() ;
        initialized = true ;
    }

    //@Override
    protected boolean hasNextBinding()
    {
        init() ;
        return iterator.hasNext() ;
    }

    //@Override
    protected Binding moveToNextBinding()
    {
        init() ;
        try {
            return (Binding)iterator.next() ;
        } catch (NoSuchElementException ex) { return null ; }
    }

    //@Override
    protected void closeIterator()
    {
        if ( input != null )
            input.close();
        solutions = null ;
        iterator = null ;
    }

    public void output(IndentedWriter out, SerializationContext sCxt)
    { out.println("CachingIterator") ; }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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