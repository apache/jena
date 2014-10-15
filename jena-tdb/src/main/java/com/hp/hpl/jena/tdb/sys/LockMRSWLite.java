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

package com.hp.hpl.jena.tdb.sys;

import java.util.concurrent.locks.ReadWriteLock ;
import java.util.concurrent.locks.ReentrantReadWriteLock ;

import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.shared.Lock ;

/** Light weight (?) MRSW lock implementation that assumes 
 * correct use of enterCriticalSection/leaveCriticalSection. 
 * That is, there is no real checking.
 */
public class LockMRSWLite implements Lock
{
    public LockMRSWLite() {}
    
    private ReadWriteLock mrswLock = new ReentrantReadWriteLock() ;
    // >0 for read lock, -1 for write lock.
    private int count = 0 ;
    
    @Override
    public synchronized void enterCriticalSection(boolean readLockRequested)
    {
        // Once we have the lock, we can record the lock state
        // because we know whether the actiev thread (us) is a read or write
        // operation, then a valid leaveCriticalSection can only be read or
        // write.
        if ( readLockRequested )
        {
            mrswLock.readLock().lock() ;
            count++ ;
        }
        else
        {
            mrswLock.writeLock().lock() ;
            count = -1 ;
        }
    }

    @Override
    public synchronized void leaveCriticalSection()
    {
        //mrswLock.readLock().tryLock() ;
        
        if ( count == 0 )
            throw new JenaException("Bad lock release - don't appear to be in a critical section") ;
        
        if ( count < 0 )
        {
            mrswLock.writeLock().unlock() ;
            count = 0 ;
            return ;
        }
        else
        {
            mrswLock.readLock().unlock() ;
            count-- ;
        }
    }

}
