/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 * Includes software from the Apache Software Foundation - Apache Software License (JENA-29)
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.NoSuchElementException ;

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.query.QueryCancelledException ;
import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.query.QueryFatalException ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
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
    public static boolean traceIterators = false ; 
    private boolean finished = false ;

    // === Cancellation
    // .cancel() can be called asynchronously with iterator execution.
    // It causes notification to cancellation to be made, once, by calling .requestCancel()
    // which is called synchronously with .cancel() and asynchronously with iterator execution.

    /** In the process of requesting a cancel, or one has been done */  
    private boolean requestingCancel = false;

    /* If set, any hasNext/next throws QueryAbortedException */
    private volatile boolean abortIterator = false ;
    private Throwable stackTrace = null ; 

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
        if ( finished )
            return false ;

        if ( abortIterator )
            throw new QueryCancelledException() ;

        // Handles exceptions
        boolean r = hasNextBinding() ; 

        if ( r == false )
            try {
                close() ;
            } catch (QueryFatalException ex)
            { 
                Log.fatal(this, "Fatal exception: "+ex.getMessage() ) ;
                abort() ;       // Abort this iterator.
                throw ex ;      // And pass on up the exception.
            }
            return r ;
    }
    
    /** final - autoclose and registration relies on it - implement moveToNextBinding() */
    public final Binding next()
    {
        return nextBinding() ;
    }

    /** final - subclasses implement moveToNextBinding() */
    public final Binding nextBinding()
    {
        try {
            if ( abortIterator )
                throw new QueryCancelledException() ;
            if ( finished )
            {
                // If abortIterator set after finished.
                if ( abortIterator )
                    throw new QueryCancelledException() ;
                throw new NoSuchElementException(Utils.className(this)) ;
            }
            
            if ( ! hasNextBinding() )
                throw new NoSuchElementException(Utils.className(this)) ;
    
            Binding obj = moveToNextBinding() ;
            if ( obj == null )
                throw new NoSuchElementException(Utils.className(this)) ;
            
            if ( requestingCancel && ! finished ) 
            {
                // But .cancel sets both requestingCancel and abortIterator
                // This only happens with a continuing iterator.
        		close() ;
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
    
    @Deprecated
    public void abort()
    {
        if ( finished )
            return ;
        try { closeIterator() ; }
        catch (QueryException ex) { } 
        
        finished = true ;
    }
    
    /** Cancel this iterator */
    public final void cancel() {
        // Call requestCancel() once.
    	if (!this.requestingCancel) {
    	    synchronized (this)
    	    {
    	        this.requestCancel() ;
    	        this.requestingCancel = true;
    	        this.abortIterator = true ;
            }
    	}
    }

    /** Cancel this iterator but allow it to continue servicing hasNext/next.
     *  Wrong answers are possible(e.g. partial ORDER BY and LIMIT).
     *  May be useful for debugging. 
     */
    public final void cancelAllowContinue() {
        // Call requestCancel() once.
        if (!this.requestingCancel) {
            synchronized (this)
            {
                this.requestCancel() ;
                this.requestingCancel = true;
                //this.abortIterator = true ;
            }
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