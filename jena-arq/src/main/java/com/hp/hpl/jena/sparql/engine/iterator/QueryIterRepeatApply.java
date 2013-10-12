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
import java.util.NoSuchElementException ;

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.sparql.util.Utils ;

/** Repeatedly execute the subclass operation for each Binding in the input iterator. */
 
public abstract class QueryIterRepeatApply extends QueryIter1
{
    int count = 0 ; 
    private QueryIterator currentStage ; 
    private volatile boolean cancelRequested = false;   // [CANCEL] needed? super.cancelRequest?
    
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
            
            if ( cancelRequested )
                // Pass on the cancelRequest to the active stage.
                performRequestCancel(currentStage);
            
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
            currentStage.cancel() ; // [CANCEL]
        cancelRequested = true;
    }
}
