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


/** Intercept and print iterator operations */ 

public class QueryIteratorLogging extends QueryIteratorWrapper
{
    private Logger log = null ;  
    private boolean logging = true ;        // Fine grain control of logging.

    public QueryIteratorLogging(QueryIterator input)
    {
        super(input) ;
        log = LoggerFactory.getLogger(input.getClass()) ;
    }
    
    @Override
    protected boolean hasNextBinding()
    { 
        boolean b = super.hasNextBinding() ;
        if ( logging )
            log.info("hasNextBinding: "+b) ;
        return b ;
    }
         
    
    @Override
    protected Binding moveToNextBinding()
    { 
        Binding binding = super.moveToNextBinding() ;
        if ( logging )
            log.info("moveToNextBinding: "+binding) ;
        return binding ;
    }

    @Override
    protected void closeIterator()
    {
        if ( logging )
            log.info("closeIterator") ;
        super.closeIterator();
    }
    
    public void loggingOn()                 { logging(true) ; }
    public void loggingOff()                { logging(false) ; }
    public void logging(boolean state)      { logging = state ; } 
}
