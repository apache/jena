/*
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.model.impl ;
import com.hp.hpl.jena.rdf.model.ModelLock ;
import com.hp.hpl.jena.shared.JenaException;

import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import java.util.*;
import org.apache.commons.logging.*;

/**
 * Model lock implmenetation using a Multiple Reader, Single Writer policy.
 * All the locking work is done by the imported WriterPreferenceReadWriteLock.
 * Ths class adds:
 * <ul>
 *   <li>The same thread that acquired a lock should release it</li>
 *   <li>Lock promotion (turing read locks into write locks) is deteched as an error</li>
 *  <ul>
 * @see com.hp.hpl.jena.rdf.model.ModelLock
 *   
 * @author		Andy Seaborne
 * @version 	$Id: ModelLockImpl.java,v 1.8 2005-02-21 12:14:35 andy_seaborne Exp $
 */

public class ModelLockImpl implements ModelLock
{
    // One instance per model.
    
    static Log log = LogFactory.getLog(ModelLockImpl.class) ;
    
    // Map of threads to lock state for this model lock
	Map threadStates = new HashMap() ;
    // We keep this is a variable because it is tested outside of a lock.
    int threadStatesSize = threadStates.size() ;
    
	//ReentrantWriterPreferenceReadWriteLock lock = new ReentrantWriterPreferenceReadWriteLock();
    WriterPreferenceReadWriteLock lock = new WriterPreferenceReadWriteLock();
    
    SynchronizedInt activeReadLocks = new SynchronizedInt(0);
    SynchronizedInt activeWriteLocks = new SynchronizedInt(0);

	ModelLockImpl() {
        if ( log.isDebugEnabled() )
            log.debug("ModelLockImpl() : "+Thread.currentThread().getName()) ;
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
        
        // At this point we have the state object which is unique per
        // model per thread.  Thus, we can do updates to via state.???
        // because we know no other thread is active on it.
        
        if ( log.isDebugEnabled() )
            log.debug(Thread.currentThread().getName()+" >> enterCS: "+report(state)) ;
			
		// If we have a read lock, but no write locks, then the thread is attempting
		// a lock promotion.  We do not allow this.
		if (state.readLocks > 0 && state.writeLocks == 0 && !readLockRequested)
		{
			// Increment the readlock so a later leaveCriticialSection
            // keeps the counters aligned.
    		state.readLocks++ ;
            activeReadLocks.increment() ;

            if ( log.isDebugEnabled() )
                log.debug(Thread.currentThread().getName()+" << enterCS: promotion attempt: "+report(state)) ;
            
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
            if ( log.isDebugEnabled() )
                log.debug(Thread.currentThread().getName()+" << enterCS: "+report(state)) ;
        }
	}

	/** Application controlled locking - leave a critical section.
	 *  @see #enterCriticalSection
     *  @see com.hp.hpl.jena.rdf.model.ModelLock
	 */

	final public void leaveCriticalSection()
	{
         
		ModelLockState state = getLockState() ;

        if ( log.isDebugEnabled() )
            log.debug(Thread.currentThread().getName()+" >> leaveCS: "+report(state)) ;

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
        
        	throw new JenaException("leaveCriticalSection: No lock held ("+Thread.currentThread().getName()+")") ;
        } finally 
        {
            if ( log.isDebugEnabled() )
                log.debug(Thread.currentThread().getName()+" << leaveCS: "+report(state)) ;
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
        sb.append(" (thread: ") ;
        sb.append(state.thread.getName()) ;
        sb.append(")") ;
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

    synchronized void removeLockState(Thread thread)
    {
        threadStates.remove(thread) ;
    }

	static class ModelLockState
	{
		// Counters for this lock object.
        // Instances of ModelLockState are held per thread per model.
        // These do not need to be atomic because a thread is the 
        // only accessor of its own counters
         
        int readLocks = 0 ;
        int writeLocks = 0 ;
        ModelLockImpl modelLock ;
        Thread thread ;

        // Need to pass in the containing model lock
        // because we want a lock on it. 
		ModelLockState(ModelLockImpl lock)
        {
            modelLock = lock ;
            thread = Thread.currentThread() ;
        }

        void clean()
        {
            if (modelLock.activeReadLocks.get() == 0 && modelLock.activeWriteLocks.get() == 0)
            {
                // A bit simple - but it churns (ModelLocalState creation) in the
                // case of a thread looping around a critical section.
                // The alternative, to delay now and do a more sophisticated global GC
                // could require a global pause which is worse.
                modelLock.removeLockState(thread) ;
            }
        }
	}
}   

/*
 *  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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

