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

import org.apache.jena.atlas.logging.Log ;

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
 * </ul> 
 */

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
    
    // ONLY the requestingCancel variable needs to be volatile. The abortIterator is guaranteed to 
    // be visible because it is written to before requestingCancel, and read from after.

    /** In the process of requesting a cancel, or one has been done */  
    private volatile boolean requestingCancel = false;

    /* If set, any hasNext/next throws QueryAbortedException
     * In normal operation, this is the same setting as requestingCancel.
     * Non-compliant behaviour can result otherwise. 
     * Accessed through cancelAllowContinue()
     */
    private boolean abortIterator = false ;
    private Object cancelLock = new Object();
    
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
    @Override
    public final boolean hasNext()
    {
        if ( finished )
            // Even if aborted. Finished is finished.
            return false ;

        if ( requestingCancel && abortIterator )
        {
            // Try to close first to release resources (in case the user
            // doesn't have a close() call in a finally block)
            close() ;
            throw new QueryCancelledException() ;
        }

        // Handles exceptions
        boolean r = hasNextBinding() ; 

        if ( r == false )
            try {
                close() ;
            } catch (QueryFatalException ex)
            { 
                Log.fatal(this, "Fatal exception: "+ex.getMessage() ) ;
                throw ex ;      // And pass on up the exception.
            }
        return r ;
    }
    
    /** final - autoclose and registration relies on it - implement moveToNextBinding() */
    @Override
    public final Binding next()
    {
        return nextBinding() ;
    }

    /** final - subclasses implement moveToNextBinding() */
    @Override
    public final Binding nextBinding()
    {
        try {
            // Need to make sure to only read this once per iteration
            boolean shouldCancel = requestingCancel;
            
            if ( shouldCancel && abortIterator )
            {
                // Try to close first to release resources (in case the user
                // doesn't have a close() call in a finally block)
                close() ;
                throw new QueryCancelledException() ;
            }

            if ( finished )
                throw new NoSuchElementException(Utils.className(this)) ;
            
            if ( ! hasNextBinding() )
                throw new NoSuchElementException(Utils.className(this)) ;
    
            Binding obj = moveToNextBinding() ;
            if ( obj == null )
                throw new NoSuchElementException(Utils.className(this)) ;
            
            if ( shouldCancel && ! finished ) 
            {
                // But .cancel sets both requestingCancel and abortIterator
                // This only happens with a continuing iterator.
        		close() ;
        	}
            
            return obj ;
        } catch (QueryFatalException ex)
        { 
            Log.fatal(this, "QueryFatalException", ex) ; 
            throw ex ; 
        }
    }
    
    @Override
    public final void remove()
    {
        Log.warn(this, "Call to QueryIterator.remove() : "+Utils.className(this)+".remove") ;
        throw new UnsupportedOperationException(Utils.className(this)+".remove") ;
    }
    
    @Override
    public void close()
    {
        if ( finished )
            return ;
        try { closeIterator() ; }
        catch (QueryException ex)
        { Log.warn(this, "QueryException in close()", ex) ; } 
        finished = true ;
    }
    
    /** Cancel this iterator */
    @Override
    public final void cancel()
    {
        // Call requestCancel() once.
        synchronized (cancelLock)
        {
            if (!this.requestingCancel)
            {
                // Need to set the flags before allowing subclasses to handle requestCancel() in order
                // to prevent a race condition.  We want to be sure that calls to hasNext()/nextBinding()
                // will definitely throw a QueryCancelledException in this class and not allow a
                // situation in which a subclass component thinks it is cancelled, while this class does not.
                this.abortIterator = true ;
                this.requestingCancel = true;
                this.requestCancel() ;
            }
        }
    }

    /** Cancel this iterator but allow it to continue servicing hasNext/next.
     *  Wrong answers are possible (e.g. partial ORDER BY and LIMIT).
     */
    
    private final void cancelAllowContinue()
    {
        // Call requestCancel() once.
        synchronized (cancelLock)
        {
            if (!this.requestingCancel)
            {
                //this.abortIterator = true ;
                this.requestingCancel = true;
                this.requestCancel() ;
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
