/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.iterator;

import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.serializer.SerializationContext;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.query.util.Utils;


/** Iterator over another QueryIterator, applying a converter function
 *  to each object that is returned by .next()
 * 
 * @author Andy Seaborne
 * @version $Id: QueryIterConvert.java,v 1.4 2007/02/06 17:06:01 andy_seaborne Exp $
 */

public class QueryIterConvert extends QueryIter1
{
    public interface Converter
    {
        public Binding convert(Binding obj) ;
    }
    
    Converter converter ; 
    
    public QueryIterConvert(QueryIterator iter, Converter c, ExecutionContext context)
    { 
        super(iter, context) ;
        converter = c ;
    }
    
    protected void releaseResources()
    {
        QueryIter1.releaseResources(getInput()) ;
    }

    public boolean hasNextBinding()
    {
        if ( isFinished() )
            return false ;
        boolean r = getInput().hasNext() ;
        if ( !r )
            close() ;
        return r ;
    }

    public Binding moveToNextBinding()
    {
        return converter.convert(getInput().nextBinding()) ;
    }

    protected void details(IndentedWriter out, SerializationContext cxt)
    { 
        out.println(Utils.className(this)) ;
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