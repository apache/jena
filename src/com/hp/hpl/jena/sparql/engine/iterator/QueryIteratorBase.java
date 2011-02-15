/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 * Includes software from the Apache Software Foundation - Apache Software License (JENA-29)
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.NoSuchElementException ;

import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.query.QueryFatalException ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import org.openjena.atlas.logging.Log ;
import com.hp.hpl.jena.sparql.util.PrintSerializableBase ;
import com.hp.hpl.jena.sparql.util.Utils ;

/**
 * This class provides the general machinary for iterators.  This includes:
 * <ul>
 * <li>autoclose when the iterator runs out</li>
 * <li>ensuring query iterators only contain Bindings</li>
 * </ul> */

public abstract class QueryIteratorBase 
    extends PrintSerializableBase
    implements QueryIterator
{
    // Can this keep the next look ahead Binding
    // so we have only "nextElement()" => null or Binding
    public static boolean traceIterators = false ; 
    private boolean finished = false ;

    // === Cancellation
    // This can happen asynchronously.
    // The cancellation process has 2 phases
    // 1) Notification of cancellation  - called on thread of caller of cancel()
    // 2) Actual cancelation we only actively set the iterator as cancelled when the nextBinding() is taken
    // this is required to guarantee thread safety because cancel() will be called from another
    // thread than the executing thread

    private boolean cancelled = false ;
    private boolean requestingCancel = false;
    Throwable stackTrace = null ; 

    public QueryIteratorBase()
    {
        if ( traceIterators )
            stackTrace = new Throwable() ;
    }

    // -------- The contract with the subclasses 
    
    /** Implement this, not hasNext() */
    protected abstract boolean hasNextBinding() ;

    /** Implement this, not next() or nextBinding()
        Returning null is turned into NoSuchElementException 
        Does not need to call hasNext (can presume it is true) */
    protected abstract Binding moveToNextBinding() ;
    
    /** Close the iterator. */
    protected abstract void closeIterator() ;
   
    /** Propagates the cancellation request - called asynchronously with the iterator itself */
    protected abstract void requestCancel();
    
    // -------- The contract with the subclasses 

    protected boolean isFinished() { return finished ; }

    /** final - subclasses implement hasNextBinding() */
    public final boolean hasNext()
    {
        try {
        	if (cancelled) {
        		close() ;
        		return false;
        	}
            if ( finished )
                return false ;

            boolean r = hasNextBinding() ; 
                
            if ( r == false )
                close() ;
            return r ;
        } catch (QueryFatalException ex)
        { 
            Log.fatal(this, "Fatal exception: "+ex.getMessage() ) ;
            abort() ;       // Abort this iterator.
            throw ex ;      // And pass on up the exception.
        }
    }
    
    /** final - autoclose and registration relies on it - implement moveToNextBinding() */
    public final Binding next()
    {
        return nextBinding() ;
    }

    /** final - implement moveToNextBinding() instead */
    public final Binding nextBinding()
    {
        try {
            if ( finished )
                throw new NoSuchElementException(Utils.className(this)) ;
            
            if ( ! hasNextBinding() )
                throw new NoSuchElementException(Utils.className(this)) ;
    
            Binding obj = moveToNextBinding() ;
            if ( obj == null )
                throw new NoSuchElementException(Utils.className(this)) ;
            
            if ( requestingCancel ) {
        		cancelled = true;
        	}
            
            return obj ;
        } catch (QueryFatalException ex)
        { 
            Log.fatal(this, "QueryFatalException", ex) ; 
            abort() ;
            throw ex ; 
        }

    }
    
    public final void remove()
    {
        Log.warn(this, "Call to QueryIterator.remove() : "+Utils.className(this)+".remove") ;
        throw new UnsupportedOperationException(Utils.className(this)+".remove") ;
    }
    
    public void close()
    {
        if ( finished )
            return ;
        try { closeIterator() ; }
        catch (QueryException ex)
        { Log.warn(this, "QueryException in close()", ex) ; } 
        finished = true ;
    }
    
    public void abort()
    {
        if ( finished )
            return ;
        try { closeIterator() ; }
        catch (QueryException ex) { } 
        
        finished = true ;
    }
    
    public void cancel() {
    	if (!this.requestingCancel) {
    		// the requestCancel may throw QueryIterAbortCancellationRequestException
    		try { this.requestCancel() ; } catch (QueryIterAbortCancellationRequestException ex) {}
    		this.requestingCancel = true; 		
    	}
    }

    /** close an iterator */
    protected static void performClose(QueryIterator iter)
    {
        if ( iter == null ) return ;
        iter.close() ;
    }
    
    /** cancel an iterator */
    protected static void performRequestCancel(QueryIterator iter)
    {
        if ( iter == null ) return ;
        iter.cancel() ;
    }
    
    @SuppressWarnings("serial")
	public class QueryIterAbortCancellationRequestException extends RuntimeException {
        public QueryIterAbortCancellationRequestException() {}
    }
    
    public String debug()
    {
        String s = "" ;
        if ( stackTrace != null )
        {
            for ( int i = 0 ; i < stackTrace.getStackTrace().length ; i++ )
            {
                StackTraceElement e = stackTrace.getStackTrace()[i] ;
                // <init> or <clinit>
                // Find first non-constructor
                if ( e.getMethodName().equals("<init>") )
                    continue ;
                // Use this so Eclipse can find the code
                s = s + e.toString() ;
                // Looks like:
                //s = s + e.getClassName()+"."+e.getMethodName()+"("+e.getFileName()+":"+e.getLineNumber()+")" ;
                // Too short for Eclipse.
                //s = s +"("+e.getFileName()+":"+e.getLineNumber()+")" ;
                break ;
            }
        }
        return s ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
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