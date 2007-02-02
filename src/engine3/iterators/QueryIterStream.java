/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package engine3.iterators;
import java.util.* ;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.engine.Binding;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.iterator.QueryIter;
import com.hp.hpl.jena.query.util.Utils;

/** Repeatedly execute the subclass operation for each Binding in the input iterator. 
 * 
 * @author     Andy Seaborne
 * @version    $Id: QueryIterRepeatApply.java,v 1.3 2007/01/02 11:19:31 andy_seaborne Exp $
 */
 
public abstract class QueryIterStream extends QueryIter
{
    int count = 0 ; 
    QueryIterator input ;
    QueryIterator currentStage ;
    
    public QueryIterStream(QueryIterator input, ExecutionContext context)
    {
        super(context) ;
        this.input = input ;
        this.currentStage = null ;
        
        if ( input == null )
        {
            LogFactory.getLog(this.getClass()).fatal("[QueryIterStream] Repeated application to null input iterator") ;
            return ;
        }
    }
    
//    public void setInput(QueryIterator input)
//    { this.input = input ; }
    
    protected abstract QueryIterator nextStage(Binding binding) ;

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

    protected Binding moveToNextBinding()
    {
        if ( ! hasNextBinding() )
            throw new NoSuchElementException(Utils.className(this)+".next()/finished") ;
        return currentStage.nextBinding() ;
        
    }
    
    private QueryIterator makeNextStage()
    {
        count++ ;

        if ( input == null )
            return null ;

        if ( !input.hasNext() )
        {
            input.close() ;
            input = null ;
            return null ; 
        }
        
        Binding binding = (Binding)input.next() ;
        QueryIterator iter = nextStage(binding) ;
        return iter ;
    }
   
    protected void closeIterator()
    {
        if ( ! isFinished() )
        {
            if ( currentStage != null )
                currentStage.close() ;
            if ( input != null )
                input.close() ;
            input = null ;
        }
    }
}

/*
 *  (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
