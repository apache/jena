/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package engine3.iterators;

import com.hp.hpl.jena.query.engine.Binding;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;


/** Iterator over another QueryIterator, applying a converter function
 *  to each object that is returned by .next()
 * 
 * @author Andy Seaborne
 * @version $Id: QueryIterConvert.java,v 1.3 2007/01/02 11:19:31 andy_seaborne Exp $
 */

public class QueryIterConvert extends QueryIter
{
    public interface Converter
    {
        public Binding convert(Binding obj) ;
    }
    
    Converter converter ; 
    QueryIterator cIter ;
    boolean finished = false ;
    
    public QueryIterConvert(QueryIterator iter, Converter c, ExecutionContext context)
    { 
        super(context) ;
        cIter = iter ;
        converter = c ;
    }
    
    protected void closeIterator() 
    { 
        if ( !finished )
        {
            finished = true ;
            cIter.close() ;
            cIter = null ;
        }
    }

    public boolean hasNextBinding()
    {
        if ( finished ) return false ;
        boolean r = cIter.hasNext() ;
        if ( !r )
            close() ;
        return r ;
    }

    public Binding moveToNextBinding()
    {
        return converter.convert(cIter.nextBinding()) ;
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