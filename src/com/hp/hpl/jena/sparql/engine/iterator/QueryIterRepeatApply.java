/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * Includes software from the Apache Software Foundation - Apache Software Licnese (JENA-29)
 */

package com.hp.hpl.jena.sparql.engine.iterator;
import java.util.NoSuchElementException ;

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import org.openjena.atlas.logging.Log ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** Repeatedly execute the subclass operation for each Binding in the input iterator. */
 
public abstract class QueryIterRepeatApply extends QueryIter1
{
    int count = 0 ; 
    private QueryIterator currentStage ;
    
    public QueryIterRepeatApply( QueryIterator input ,
                                 ExecutionContext context)
    {
        super(input, context) ;
        this.currentStage = null ;
        
        if ( input == null )
        {
            Log.fatal(this, "[QueryIterRepeatApply] Repeated application to null input iterator") ;
            return ;
        }
    }
       
    protected QueryIterator getCurrentStage()
    {
        return currentStage ;
    }
    
    protected abstract QueryIterator nextStage(Binding binding) ;

    @Override
    protected boolean hasNextBinding()
    {
        if ( isFinished() )
            return false ;
        
        for ( ;; )
        {
            if ( currentStage == null  )
                currentStage = makeNextStage() ;
            
            if ( currentStage == null  )
                return false ;
            
            if ( currentStage.hasNext() )
                return true ;
            
            // finish this step
            currentStage.close() ;
            currentStage = null ;
            // loop
        }
        // Unreachable
    }

    @Override
    protected Binding moveToNextBinding()
    {
        if ( ! hasNextBinding() )
            throw new NoSuchElementException(Utils.className(this)+".next()/finished") ;
        return currentStage.nextBinding() ;
        
    }
    
    private QueryIterator makeNextStage()
    {
        count++ ;

        if ( getInput() == null )
            return null ;

        if ( !getInput().hasNext() )
        {
            getInput().close() ;
            return null ; 
        }
        
        Binding binding = getInput().next() ;
        QueryIterator iter = nextStage(binding) ;
        return iter ;
    }
   
    @Override
    protected void closeSubIterator()
    {
        if ( currentStage != null )
            currentStage.close() ;
    }
    
    @Override
    protected void requestSubCancel()
    {
        if ( currentStage != null )
            currentStage.cancel() ;
    }
}

/*
 *  (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
