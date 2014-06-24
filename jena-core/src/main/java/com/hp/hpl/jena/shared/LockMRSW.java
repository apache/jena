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

package com.hp.hpl.jena.shared ;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lock implemenetation using a Multiple Reader, Single Writer policy.
 * All the locking work is done by the imported WriterPreferenceReadWriteLock.
 * Ths class adds:
 * <ul>
 *   <li>The same thread that acquired a lock should release it</li>
 *   <li>Lock promotion (turning read locks into write locks) is 
 *   deteched as an error</li>
 *  <ul>
 */

public class LockMRSW implements Lock 
{
    static Logger log = LoggerFactory.getLogger(LockMRSW.class) ;
    
    // Map of threads to lock state for this lock
    Map<Thread, LockState> threadStates = new HashMap<>() ;
    // We keep this is a variable because it is tested outside of a lock.
    int threadStatesSize = threadStates.size() ;
    
    ReadWriteLock mrswLock = new ReentrantReadWriteLock() ;
    // WriterPreferenceReadWriteLock mrswLock = new WriterPreferenceReadWriteLock();
    
    AtomicInteger activeReadLocks = new AtomicInteger(0);
    AtomicInteger activeWriteLocks = new AtomicInteger(0);
    
    public LockMRSW() {
        if ( log.isDebugEnabled() )
            log.debug("Lock : "+Thread.currentThread().getName()) ;
    }
    
    
    /** Application controlled locking - enter a critical section.
     *  Locking is reentrant so an application can have nested critical sections.
     *  Typical code:
     *  <pre>
     *  try {
     *     enterCriticalSection(Lock.READ) ;
     *     ... application code ...
     *  } finally { leaveCriticalSection() ; }
     * </pre>
     */
    
    @Override
    final public void enterCriticalSection(boolean readLockRequested)
    {
        // Don't make {enter|leave}CriticalSection synchronized - deadlock will occur.
        // The current thread will hold the model lock thread
        // and will attempt to grab the MRSW lock.
        // But if it waits, no other thread will even get 
        // to release the lock as it can't enter leaveCriticalSection
        
        LockState state = getLockState() ;
        
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
            activeReadLocks.incrementAndGet() ;
            
            if ( log.isDebugEnabled() )
                log.debug(Thread.currentThread().getName()+" << enterCS: promotion attempt: "+report(state)) ;
            
            throw new JenaException("enterCriticalSection: Write lock request while holding read lock - potential deadlock"+report(state));
        }
        
        // Trying to get a read lock after a write lock - get a write lock instead.
        if ( state.writeLocks > 0 && readLockRequested )
            readLockRequested = false ;
        
        try {
            if (readLockRequested)
            {
                if (state.readLocks == 0)
                    mrswLock.readLock().lock();
                state.readLocks ++ ;
                activeReadLocks.incrementAndGet() ;
            }
            else
            {
                if (state.writeLocks == 0)
                    mrswLock.writeLock().lock();
                state.writeLocks ++ ;
                activeWriteLocks.incrementAndGet() ;
            }
        }
        finally
        {
            if ( log.isDebugEnabled() )
                log.debug(Thread.currentThread().getName()+" << enterCS: "+report(state)) ;
        }
    }
    
    /** Application controlled locking - leave a critical section.
     *  @see #enterCriticalSection
     */
    
    @Override
    final public void leaveCriticalSection()
    {
        
        LockState state = getLockState() ;
        
        if ( log.isDebugEnabled() )
            log.debug(Thread.currentThread().getName()+" >> leaveCS: "+report(state)) ;
        
        try {
            if ( state.readLocks > 0)
            {
                state.readLocks -- ;
                activeReadLocks.getAndDecrement() ;
                
                if ( state.readLocks == 0 )
                    mrswLock.readLock().unlock() ;
                
                state.clean() ;
                return ;
            }
            
            if ( state.writeLocks > 0)
            {
                state.writeLocks -- ;
                activeWriteLocks.getAndDecrement() ;
                
                if ( state.writeLocks == 0 )
                    mrswLock.writeLock().unlock() ;
                
                state.clean() ;
                return ;
            }
            
            // No lock held.
            
            throw new JenaException("leaveCriticalSection: No lock held ("+Thread.currentThread().getName()+") "+report(state)) ;
        } finally 
        {
            if ( log.isDebugEnabled() )
                log.debug(Thread.currentThread().getName()+" << leaveCS: "+report(state)) ;
        }
    }
    
    synchronized
    private String report(LockState state)
    {
        StringBuilder sb = new StringBuilder() ;
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
    
    synchronized LockState getLockState()
    {
        Thread thisThread = Thread.currentThread() ;
        LockState state = threadStates.get(thisThread) ;
        if ( state == null )
        {
            state = new LockState(this) ;
            threadStates.put(thisThread, state) ;
            threadStatesSize = threadStates.size() ;
        }
        return state ;              
    }
    
    synchronized void removeLockState(Thread thread)
    {
        threadStates.remove(thread) ;
    }
    
    static class LockState
    {
        // Counters for this lock object.
        // Instances of ModelLockState are held per thread per model.
        // These do not need to be atomic because a thread is the 
        // only accessor of its own counters
        
        int readLocks = 0 ;
        int writeLocks = 0 ;
        LockMRSW lock ;
        Thread thread ;
        
        // Need to pass in the containing model lock
        // because we want a lock on it. 
        LockState(LockMRSW theLock)
        {
            lock = theLock ;
            thread = Thread.currentThread() ;
        }
        
        void clean()
        {
            if (lock.activeReadLocks.get() == 0 && lock.activeWriteLocks.get() == 0)
            {
                // A bit simple - but it churns (LockState creation) in the
                // case of a thread looping around a critical section.
                // The alternative, to delay now and do a more sophisticated global GC
                // could require a global pause which is worse.
                lock.removeLockState(thread) ;
            }
        }
    }
}
