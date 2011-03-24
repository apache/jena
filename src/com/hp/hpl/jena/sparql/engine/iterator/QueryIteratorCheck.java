/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.Iterator ;

import org.openjena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import org.openjena.atlas.logging.Log ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** Query iterator that checks everything was closed correctly */

public class QueryIteratorCheck extends QueryIteratorWrapper
{
    private ExecutionContext execCxt ;
    
    private QueryIteratorCheck(QueryIterator qIter, ExecutionContext execCxt)
    {
        super(qIter) ;
        if ( qIter instanceof QueryIteratorCheck )
            Log.warn(this, "Checking checked iterator") ;
        
        this.execCxt = execCxt ;
        
    }
    @Override
    public void close()
    {
        super.close() ;
        checkForOpenIterators(execCxt) ;
    }
    
    // Remove me sometime.
    @Deprecated
    @Override
    public void abort()
    {
        super.abort() ;
        checkForOpenIterators(execCxt) ;
    }
    
    // Be silent about ourselves.
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    { iterator.output(out, sCxt) ; }
    
    public static void checkForOpenIterators(ExecutionContext execContext)
    { dump(execContext, false); }
    
    public static QueryIteratorCheck check(QueryIterator qIter, ExecutionContext execCxt)
    {
        if ( qIter instanceof QueryIteratorCheck )
            return (QueryIteratorCheck)qIter ;
        return new QueryIteratorCheck(qIter, execCxt) ;
    }
    
    private static void dump(ExecutionContext execContext, boolean includeAll)
    {
        if ( includeAll )
        {
            Iterator<QueryIterator> iterAll = execContext.listAllIterators() ;

            if ( iterAll != null )
                while(iterAll.hasNext())
                {
                    QueryIterator qIter = iterAll.next() ;
                    warn(qIter, "Iterator: ") ;
                }
        }

        Iterator<QueryIterator> iterOpen = execContext.listOpenIterators() ;
        while(iterOpen.hasNext())
        {
            QueryIterator qIterOpen = iterOpen.next() ;
            warn(qIterOpen, "Open iterator: ") ;
        }
    }

    private static void warn(QueryIterator qIter, String str)
    {
        str = str + Utils.className(qIter) ;

        if ( qIter instanceof QueryIteratorBase )
        {
            QueryIteratorBase qIterBase = (QueryIteratorBase)qIter ;
            {
                QueryIter qIterLN = (QueryIter)qIter ;
                str = str+"/"+qIterLN.getIteratorNumber() ;
            }
            String x = qIterBase.debug() ;
            if ( x.length() > 0 )
                str = str+" : "+x ;
        }
        Log.warn(QueryIteratorCheck.class, str) ;
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