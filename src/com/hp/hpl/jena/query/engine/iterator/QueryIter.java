/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.iterator;

import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.engine1.iterator.QueryIterTracked;
import com.hp.hpl.jena.query.serializer.SerializationContext;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.query.util.Utils;

/**
 * This class provides the general machinary for iterators 
 * within the general-purpose query engine.
 *  This includes tracking open iterators.
 * 
 * @author Andy Seaborne
 * @version $Id: QueryIter.java,v 1.5 2007/02/05 18:14:33 andy_seaborne Exp $
 */
public abstract class QueryIter extends QueryIteratorBase
{
    // volatile just to make it safe to concurrent updates
    // It does not matter if it is wrong.
    volatile static int iteratorCounter = 0 ;
    private int iteratorNumber = (iteratorCounter++) ;
    
    private ExecutionContext tracker ;
    
    public QueryIter(ExecutionContext qCxt)
    { 
        tracker = qCxt ;
        register() ;
    }

    public static QueryIter makeTracked(QueryIterator qIter, ExecutionContext qCxt)
    {
        if ( qIter instanceof QueryIter )
            return (QueryIter)qIter ;
        return new QueryIterTracked(qIter, qCxt) ; 
    }
    
    public final void close()
    {
        super.close() ;
        deregister() ;
    }
    
    public ExecutionContext getExecContext() { return tracker ; }
    
    public int getIteratorNumber() { return iteratorNumber ; }
    
    public void output(IndentedWriter out, SerializationContext sCxt)
    { out.println(getIteratorNumber()+"/"+Utils.className(this)) ; }
    
    private void register()
    {
        if ( tracker != null )
            tracker.openIterator(this) ;
    }
    
    private void deregister()
    {
        if ( tracker != null )
            tracker.closedIterator(this) ;
    }
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