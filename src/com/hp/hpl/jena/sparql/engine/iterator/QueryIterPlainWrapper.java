/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 * Includes software from the Apache Software Foundation - Apache Software Licnese (JENA-29)
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.Iterator ;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.iterator.Iter ;

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.util.iterator.NiceIterator ;

/** Turn an normal java.util.Iterator (of Bindings) into a QueryIterator */

public class QueryIterPlainWrapper extends QueryIter
{
    Iterator<Binding> iterator = null ;
    
    public QueryIterPlainWrapper(Iterator<Binding> iter)
    { this(iter, null) ; }
    
    public QueryIterPlainWrapper(Iterator<Binding> iter, ExecutionContext context)
    {
        super(context) ;
        iterator = iter ;
    }

    /** Preferrable to use a constructor - but sometimes that is inconvenient 
     *  so pass null in the constructor and then call this before the iterator is
     *  used.   
     */
    public void setIterator(Iterator<Binding> iterator) { this.iterator = iterator ; }
    
    @Override
    protected boolean hasNextBinding() { return iterator.hasNext() ; } 
    
    @Override
    protected Binding moveToNextBinding() { return iterator.next() ; }

    @Override
    protected void closeIterator()
    {
        if ( iterator != null )
        {
            NiceIterator.close(iterator) ;
            // In case we wrapped, for example, another QueryIterator.
            Iter.close(iterator) ;
            iterator = null ;
        }
    }
    
    @Override
    protected void requestCancel()
    { }
    
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    { out.print(Utils.className(this)) ; }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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