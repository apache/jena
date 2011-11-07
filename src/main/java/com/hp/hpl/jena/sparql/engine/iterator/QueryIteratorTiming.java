/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.util.Timer ;

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
