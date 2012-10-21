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

/**
 * A lock.
 * Critical section support for multithreaed access to a model
 * within a single JVM. See also transactions support.
 *
 * Examples if application code:
 * <pre>
 * try {
 *   model.enterCriticalSection(ModelLock.READ) ;
 *   ...
 * } finally { model.leaveCriticalSection() ; }
 * </pre>
 *
 * Nested locks are provided for:
 * <pre>
 * try {
 *   model.enterCriticalSection(ModelLock.WRITE) ;
 *   libraryCall() ;
 *   ...
 * } finally { model.leaveCriticalSection() ; }
 *
 * void libraryCall()
 * {
 *   try {
 *     model.enterCriticalSection(ModelLock.READ) ;
 *     ... do library stuff ...
 *   } finally { model.leaveCriticalSection() ; }
 * }
 * </pre>
 * Iterators should be used inside a critical section and not passed outside
 * the concurrency controlled block.
 * <pre>
 * try {
 *   model.enterCriticalSection(ModelLock.READ) ;
 *   StmtIterator sIter = ... ;
 *   for ( ; sIter.next; )
 *   {
 *       ...
 *   }
 *   sIter.close() ;
 * } finally { model.leaveCriticalSection() ; }
 * </pre>
 *
 * Note that if a library operation needs a write lock, the application must either have no
 * locks or a write lock when calling.  Lock promotion is not supported - it can lead to
 * deadlock.
 *
 * The hard work of locking is done by the
 * <a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">util.concurrent</a>.
 * This is a convenience wrapper that provides nested locks, a special case of reentrant locks,
 * that does some checking.
 */

package com.hp.hpl.jena.shared;

public interface Lock
{
    /** Descriptive name for lock requests - read lock */
    public static final boolean READ = true ;
    
    /** Descriptive name for lock requests - write lock */
    public static final boolean WRITE = false ;
    
    
    /** Enter a critical section.
     *  The application must call leaveCriticialSection.
     *  @see #leaveCriticalSection
     *
     * @param readLockRequested true implies a read lock,false implies write lock.
     */
    
    public void enterCriticalSection(boolean readLockRequested) ;
    
    /** Leave a critical section.  Releases the lock form the matching enterCriticalSection
     *  @see #enterCriticalSection
     */
    
    public void leaveCriticalSection() ;
}
