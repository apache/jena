/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;


/** Intercept and print iterator operations  
 * 
 * @author Andy Seaborne
 */ 

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