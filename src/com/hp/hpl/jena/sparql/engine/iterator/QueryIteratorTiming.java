/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.util.Timer;

public class QueryIteratorTiming extends QueryIteratorWrapper
{
    static private Logger log = LoggerFactory.getLogger(QueryIteratorTiming.class) ;
    
    static final public int NotStarted = -2 ;
    static final public int NotFinished = -1 ;
    
    public static QueryIteratorTiming time(QueryIterator iter) { return new QueryIteratorTiming(iter) ; }
    
    private QueryIteratorTiming(QueryIterator iter)
    {
        super(iter) ;
    }
    
    @Override
    protected boolean hasNextBinding() { start() ; return super.hasNextBinding() ; }
    
    @Override
    protected Binding moveToNextBinding() { start() ; return super.moveToNextBinding() ; }
    
    @Override
    protected void closeIterator()
    {
        super.closeIterator() ;
        stop() ;
    }

    private Timer timer = null ; 
    private long milliseconds = NotStarted ;
    
    private void start()
    {
        if ( timer == null )
        {
            timer = new Timer() ;
            timer.startTimer() ;
            milliseconds = NotFinished ;
        }
    }

    private void stop()
    {
        if ( timer == null )
        {
            milliseconds = 0 ; 
            return ;
        }
            
        milliseconds = timer.endTimer() ;
        
//        if ( log.isDebugEnabled() )
//            log.debug("Iterator: {} milliseconds", milliseconds) ;
        log.info("Execution: {} milliseconds", milliseconds) ;
    }
    
    /** Return the elapsed time, in milliseconds, between the first call to this iterator and the close call.
     *  Returns the time, or NotStarted (-2) or NotFinished (-1).
     */
    public long getMillis() { return milliseconds ; }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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