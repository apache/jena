/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.NoSuchElementException;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Utils;

/** A query iterator that yields the same thing N times. 
 * 
 * @author Andy Seaborne
 */

public class QueryIterYieldN extends QueryIter
{
    protected int limitYielded ;
    protected int countYielded = 0 ;
    protected Binding binding ;
    
    public QueryIterYieldN(int num, Binding b)
    {
        this(num, b, null) ;
    }
    
    public QueryIterYieldN(int num, Binding b, ExecutionContext context)
    {
        super(context) ;
        binding = b ;
        limitYielded = num ;
    }
    
    public Binding getBinding() { return binding ; }
    
    @Override
    protected boolean hasNextBinding()
    {
        return countYielded < limitYielded ;
    }
    
    @Override
    protected Binding moveToNextBinding()
    {
        if ( ! hasNextBinding() )
            // Try to get the class name as specific as possible for subclasses
            throw new NoSuchElementException(Utils.className(this)) ;
        countYielded++ ;
        return binding ;
    }

    @Override
    protected void closeIterator()
    {
        //binding = null ;
    }
    
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        out.print("QueryIterYieldN: "+limitYielded+" of "+binding);
    }

}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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