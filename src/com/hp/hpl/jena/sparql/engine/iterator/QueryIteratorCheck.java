/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/** Query iterator that checks everything was closed correctly 
 * 
 * @author Andy Seaborne
 * @version $Id$
 */

public class QueryIteratorCheck extends QueryIteratorWrapper
{
    private static Log log = LogFactory.getLog(QueryIteratorCheck.class) ;
    private ExecutionContext execCxt ;
    
    private QueryIteratorCheck(QueryIterator qIter, ExecutionContext execCxt)
    {
        super(qIter) ;
        this.execCxt = execCxt ;
        
    }
    public void close()
    {
        super.close() ;
        checkForOpenIterators(execCxt) ;
    }
    public void abort()
    {
        super.abort() ;
        checkForOpenIterators(execCxt) ;
    }
    
    // Be silent about ourselves.
    public void output(IndentedWriter out, SerializationContext sCxt)
    { iterator.output(out, sCxt) ; }
    
    public static void checkForOpenIterators(ExecutionContext execContext)
    {
        execContext.dump();
//        Iterator iter = execContext.listOpenIterators() ;
//        while(iter.hasNext())
//        {
//            QueryIterator qIterOpen = (QueryIterator)iter.next() ;
//            if ( qIterOpen instanceof QueryIteratorBase )
//            {
//                if ( qIterOpen instanceof QueryIter )
//                {
//                    QueryIter qIterBase = (QueryIter)qIterOpen ;
//                    log.warn("Open iterator: "+qIterBase.getIteratorNumber()+" "+qIterOpen+" "+qIterBase.debug()) ;
//                }
//                else 
//                {
//                    QueryIteratorBase qIterBase = (QueryIteratorBase)qIterOpen ;
//                    log.warn("Open iterator: "+qIterOpen+" "+qIterBase.debug()) ;
//                }
//            }
//            else
//                log.warn("Open iterator: "+qIterOpen) ;
//        }
    }
    
    public static QueryIteratorCheck check(QueryIterator qIter, ExecutionContext execCxt)
    {
        if ( qIter instanceof QueryIteratorCheck )
            return (QueryIteratorCheck)qIter ;
        return new QueryIteratorCheck(qIter, execCxt) ;
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