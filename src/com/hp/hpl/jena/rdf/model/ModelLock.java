/*
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

/**
 * A model lock.
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
 *
 * @author      Andy Seaborne
 * @version     $Id: ModelLock.java,v 1.2 2003-08-27 13:05:52 andy_seaborne Exp $
 */


package com.hp.hpl.jena.rdf.model ;

public interface ModelLock
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


/*
 *  (c) Copyright 2003 Hewlett-Packard Development Company, LP
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

