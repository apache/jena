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

package org.apache.jena.tdb2.lib;

import java.util.concurrent.*;

import org.apache.jena.tdb2.TDBException;

/** Asynchronous operations support
 *  This class provides a simple framework for asynchronous operations.
 *  There is a thread pool and a pending queue.
 *  <p>
 *  Default settings are thread pool of one and at most two pending operations.
 *  When a new async operation is added, the pending queue is checked, and the
 *  call blocks until the pending queue is below the threshold.
 *  <p>
 *  With the default setting of a thread pool of one, operations are
 *  executed in the order submitted.
 *  <p>
 *  If the far end is the bottleneck, a longer queue is of no help.
 */
public final class Async {
    // Currently specific to ThriftRunnable
    private static final int THREAD_POOL_SIZE = 1;
    private static final int PENDING_MAX = 2;
    private static final int BlockingQueueSize = PENDING_MAX+2;

    private final int pendingQueueLimit;
    private final int blockingQueueSize;
    private final ExecutorService executorService;
    private final BlockingQueue<Future<Void>> outstanding;

    public Async() {
        this(THREAD_POOL_SIZE, PENDING_MAX);
    }

    public Async(int threadPoolSize, int pendingQueueLimit) {
        this.pendingQueueLimit = pendingQueueLimit;
        this.blockingQueueSize = this.pendingQueueLimit+1;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.outstanding = new ArrayBlockingQueue<>(BlockingQueueSize);
    }

    /** Block until all pending operations has been completed */
    public void completeAsyncOperations() {
        reduceAsyncQueue(0);
    }

    /** Block until the pending operations queue is below the given size. */
    public void reduceAsyncQueue(int reduceSize) {
        while ( outstanding.size() > reduceSize ) {
            try { outstanding.take().get(); }
            catch (Exception ex) {
                throw new TDBException("Exception taking from async queue", ex);
            }
        }
    }

    public void execAsync(Runnable action) {
        reduceAsyncQueue(pendingQueueLimit);
        Future<Void> task = executorService.submit(()-> {
            try { action.run(); }
            catch (Exception ex)    { throw new TDBException("Unexpected exception: "+ex.getMessage(), ex); }
            return null;
        });
        outstanding.add(task);
    }
}
