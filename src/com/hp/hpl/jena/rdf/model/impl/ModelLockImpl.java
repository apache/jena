/*
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.model.impl ;
import com.hp.hpl.jena.rdf.model.ModelLock ;
import com.hp.hpl.jena.util.JenaException ;
import EDU.oswego.cs.dl.util.concurrent.*;
import java.util.*;

/**
 * Model lock implmenetation using Multiple Reader, Single Writer.
 * @see com.hp.hpl.jena.rdf.model.ModelLock
 *   
 * @author		Andy Seaborne
 * @version 	$Id: ModelLockImpl.java,v 1.2 2003-04-29 10:52:15 andy_seaborne Exp $
 */

public class ModelLockImpl implements ModelLock
{
	// Model locks impose some extra policy on re-entrant MRSW locks.
	// 1 - The same thread that acquired a lock should release it.
	// 2 - Lock promotion (turing read locsk into write locks is deteched as an error. 

    public static final boolean DEBUG = false ;
    public static final boolean DEBUG_OBJ = true ;
    // Only needed if DEBUG is true.
    static java.io.PrintStream out = System.err ;
	
    // Map of threads to lock state for this model lock
	Map threadStates = new HashMap() ;
    int threadStatesSize = threadStates.size() ;
    int LIMIT = 100 ;

	//ReentrantWriterPreferenceReadWriteLock lock = new ReentrantWriterPreferenceReadWriteLock();
    WriterPreferenceReadWriteLock lock = new WriterPreferenceReadWriteLock();
    
    SynchronizedInt activeReadLocks = new SynchronizedInt(0);
    SynchronizedInt activeWriteLocks = new SynchronizedInt(0);

	ModelLockImpl() {
        if ( DEBUG && DEBUG_OBJ ) 
            out.println("ModelLockImpl() : "+this) ;
    }


	/** Application controlled locking - enter a critical section.
	 *  Locking is reentrant so an application can have nested critical sections.
	 *  Typical code:
	 *  <pre>
	 *  try {
	 *     enterCriticalSection(ModelLock.READ) ;
	 *     ... application code ...
	 *  } finally { leaveCriticalSection() ; }
	 * </pre>
     * @see com.hp.hpl.jena.rdf.model.ModelLock
	 */

	final public void enterCriticalSection(boolean readLockRequested)
	{
        // Don't make {enter|leave}CriticalSection synchronized - deadlock will occur.
        // The current thread will hold the model lock thread
        // and will attempt to grab the MRSW lock.
        // But if it waits, no other thread will even get 
        // to release the lock as it can't enter leaveCriticalSection
        
		ModelLockState state = getLockState() ;

		if (DEBUG)
			out.println(Thread.currentThread().getName()+" >> enterCS: "+report(state)) ;
			
		// If we have a read lock, but no write locks, then the thread is attempting
		// a lock promotion.  We do not allow this.
		if (state.readLocks > 0 && state.writeLocks == 0 && !readLockRequested)
		{
			// Increment the readlock so a later leaveCriticialSection
            // keeps the counters aligned.
    		synchronized(state) { state.readLocks++ ; }
            activeReadLocks.increment() ;

			if ( DEBUG )
				out.println(Thread.currentThread().getName()+" << enterCS: promotion attempt: "+report(state)) ;
			throw new JenaException("enterCriticalSection: Write lock request while holding read lock - potential deadlock");
		}

        // Trying to get a read lock after a write lock - get a write lock instead.
        if ( state.writeLocks > 0 && readLockRequested )
            readLockRequested = false ;

        try {
            if (readLockRequested)
            {
                if (state.readLocks == 0)
                    lock.readLock().acquire();
				state.readLocks ++ ;
                activeReadLocks.increment() ;
            }
			else
			{
                if (state.writeLocks == 0)
                    lock.writeLock().acquire();
				state.writeLocks ++ ;
                activeWriteLocks.increment() ;
			}
		}
		catch (InterruptedException intEx)
		{
		}
        finally
        {
    		if (DEBUG)
    			out.println(Thread.currentThread().getName()+" << enterCS: "+report(state)) ;
        }
	}

	/** Application controlled locking - leave a critical section.
	 *  @see #enterCriticalSection
     *  @see com.hp.hpl.jena.rdf.model.ModelLock
	 */

	final public void leaveCriticalSection()
	{
         
		ModelLockState state = getLockState() ;

		if (DEBUG)
			out.println(Thread.currentThread().getName()+" >> leaveCS: "+report(state)) ;

        try {
            if ( state.readLocks > 0)
            {
                state.readLocks -- ;
                activeReadLocks.decrement() ;
                
                if ( state.readLocks == 0 )
                    lock.readLock().release() ;
                
                state.clean() ;
                return ;
            }
        
            if ( state.writeLocks > 0)
            {
                state.writeLocks -- ;
                activeWriteLocks.decrement() ;
                
                if ( state.writeLocks == 0 )
                    lock.writeLock().release() ;

                state.clean() ;
                return ;
            }
        
            // No lock held.
        
        	throw new JenaException("leaveCriticalSection: No lock held") ;
        } finally 
        {
            if (DEBUG)
			     out.println(Thread.currentThread().getName()+" << leaveCS: "+report(state)) ;
        }
	}

    private String report(ModelLockState state)
    {
        StringBuffer sb = new StringBuffer() ;
        sb.append("Thread R/W: ") ;
        sb.append(Integer.toString(state.readLocks)) ;
        sb.append("/") ;
        sb.append(Integer.toString(state.writeLocks)) ;
        sb.append(" :: Model R/W: ") ;
        sb.append(Integer.toString(activeReadLocks.get())) ;
        sb.append("/") ;
        sb.append(Integer.toString(activeWriteLocks.get())) ;
        if ( DEBUG_OBJ )
        {
            sb.append(" (lock:") ;
            sb.append(this) ;
            sb.append(")") ;
        }
        return sb.toString() ;
    }

	// Model internal functions -----------------------------

    synchronized ModelLockState getLockState()
    {
        Thread thisThread = Thread.currentThread() ;
        ModelLockState state = (ModelLockState)threadStates.get(thisThread) ;
        if ( state == null )
        {
            state = new ModelLockState(this) ;
            threadStates.put(thisThread, state) ;
            threadStatesSize = threadStates.size() ;
        }
        return state ;              
    }


	class ModelLockState
	{
		// Counters for this lock object
        // These do not need to be atmoic because a thread is the 
        // only accessor of its own counters
         
        int readLocks = 0 ;
        int writeLocks = 0 ;
        ModelLock modelLock ;

        // Need to pass in the containing model lock
        // because we want a lock on it. 
		ModelLockState(ModelLock lock) { modelLock = lock ;}

        void clean()
        {
            if (activeReadLocks.get() == 0 && activeWriteLocks.get() == 0)
            {
                // This thread has no locks - think about some cleaning up.
                if (threadStatesSize > LIMIT)
                    clean2();
                // Simple - but it churns.
                //remove(Thread.currentThread()) ;
            }
        }
        
        
		private void remove(Thread thread) { threadStates.remove(thread) ; }
		
		// Clear out all threads not currently active.
		// Does not matter if they reenter - we just allocate a control block for them
		// This policy avoids churn when a thread keeps entering
		// and leaving critical sections.
		
		void clean2()
		{
            // This *reads* other threads lock states.
            // Only does this if there are no readers or writers
            // Keeps new one out as it synchronizes on the model lock
            // forcing getLockState to wait.

            synchronized(modelLock)
            {
                // check the condition again.
                if (activeReadLocks.get() != 0 || activeWriteLocks.get() != 0)
                    return ;
                
    			int cleared = 0 ;
    			for ( Iterator iter = threadStates.keySet().iterator() ; iter.hasNext() ;)
    			{
    				ModelLockState state = (ModelLockState)iter.next() ;
    				if ( state.readLocks == 0 && state.writeLocks == 0 )
    				{
    					iter.remove() ;
    					cleared++ ;
    				}
    			}
    			
    			if ( cleared < LIMIT/10 )
    				// Lots active.
    				LIMIT = LIMIT*2 ;
    			threadStatesSize = threadStates.size() ;
            }
		}
	}
}   

/*
 *  (c) Copyright Hewlett-Packard Company 2003
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

