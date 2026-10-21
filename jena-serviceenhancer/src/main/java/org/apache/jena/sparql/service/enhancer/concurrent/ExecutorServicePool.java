/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.service.enhancer.concurrent;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.sparql.service.enhancer.util.IdPool;
import org.apache.jena.sparql.service.enhancer.util.LinkedList;
import org.apache.jena.sparql.service.enhancer.util.LinkedList.LinkedListNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ForwardingExecutorService;

/**
 * A factory for single thread executors. The returned executor services are wrappers.
 * You must eventually call {@link ExecutorService#shutdown()} or {@link ExecutorService#shutdownNow()}
 * on the wrapper in order to return the underlying executor service back to the pool.
 */
public class ExecutorServicePool {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorServicePool.class);

    private class ExecutorServiceWithKey
        extends ForwardingExecutorService
    {
        private final LinkedListNode<ExecutorState> node;
        private ExecutorService delegate;

        public ExecutorServiceWithKey(ExecutorService delegate, LinkedListNode<ExecutorState> node) {
            super();
            this.delegate = delegate;
            this.node = node;
            // TODO executorId could be copied because its final
        }

        @Override
        protected ExecutorService delegate() {
            return delegate;
        }

        public LinkedListNode<ExecutorState> getNode() {
            return node;
        }
    }

    /** This is the view implementation handed out to clients. */
    private class ExecutorServiceInternal
        extends CloseShieldExecutorService<ExecutorServiceWithKey>
    {
        public ExecutorServiceInternal(ExecutorServiceWithKey delegate) {
            super(delegate);
        }

        @Override
        public void shutdown() {
            super.shutdown();
            giveBack(delegate);
        }


        @Override
        public List<Runnable> shutdownNow() {
            super.shutdownNow();
            giveBack(delegate);
            return List.of();
        }
    }

    // Shutting down the pool also shuts down all executors.
    private final ConcurrentHashMap<Integer, ExecutorServiceWithKey> executorMap = new ConcurrentHashMap<>();

    private final AtomicBoolean isShutdown = new AtomicBoolean();
    private final IdPool idPool = new IdPool();
    private final long idleTimeout;
    private final int maxIdleExecutors;

    private final boolean isDaemon = true;

    // private ScheduledExecutorService cleaner;
    private Timer timer;

    private volatile boolean isCleanupScheduled = false;

    private void scheduleCleanup() {
        synchronized (actions) {
            if (!isCleanupScheduled) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cleanup of idle executors scheduled in {} ms", idleTimeout);
                }

                if (timer == null) {
                    timer = new Timer(isDaemon);
                }

                isCleanupScheduled = true;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        doCleanup();
                    }
                }, idleTimeout);
                // If a task has been scheduled AND not yet executed then do nothing; otherwise schedule a new task
            } else {
                // if (logger.isWarnEnabled()) {
                //     logger.warn("Request for cleanup of idle executors ignored because a pending action was already scheduled.");
                // }
            }
        }
    }

    private class ExecutorState {
        int executorId;
        ExecutorServiceWithKey executorService;

        /** When the executor has become idle. Only needs to be set before insert into the idleList. */
        long idleTimestamp;
    }

    /**
     * A doubly linked list to keep track of idle executors in the ExecutorServicePool.
     * Each executor keeps a reference to a single node of this list.
     *
     * If the executor becomes busy then it unlinks itself from the list.
     * If the executor becomes idle then it appends itself to the end of this list with its idle timestamp.
     *
     * Consequently, the executors that have been idle longest are at the beginning of the list.
     * The cleanup task only has to release the idle executors at the beginning of the list.
     * The cleanup task can stop when encountering an executor whose idle time is too recent.
     */
    private LinkedList<ExecutorState> actions = new LinkedList<>();

    public ExecutorServicePool() {
        this(0, 0);
    }

    public ExecutorServicePool(long idleTimeout, int maxIdleExecutors) {
        super();
        this.idleTimeout = idleTimeout;
        this.maxIdleExecutors = maxIdleExecutors;
    }

    private void checkOpen() {
        if (isShutdown.get()) {
            throw new IllegalStateException("Executor pool has been shut down.");
        }
    }

    /** Request an executor (creates new if none is available). */
    public ExecutorService acquireExecutor() {
        checkOpen();

        // Attempt to get an executor from the idle list
        ExecutorServiceWithKey backend = null;
        synchronized (actions) {
            LinkedListNode<ExecutorState> node;
            node = actions.getFirstNode();
            if (node != null) {
                // Synchronized unlinking of the node prevents accidental concurrent cleanup
                node.unlink();
                backend = node.getValue().executorService;
            }
        }

        // If there was no idle executor then allocate a fresh one
        if (backend == null) {
            int executorId = idPool.acquire();
            // FIXME Make sure that the executor with the next free id is not shutting down while we try to claim it
            //   So newBackend should be synchronized with giveBack.
            backend = executorMap.computeIfAbsent(executorId, this::newBackend);
        }

        ExecutorServiceInternal result = new ExecutorServiceInternal(backend);
        if (logger.isDebugEnabled()) {
            logger.debug("Acquired executor #{}.", backend.getNode().getValue().executorId);
        }
        return result;
    }

    protected ExecutorServiceWithKey newBackend(int executorId) {
        ExecutorService core = createSingleThreadExecutor(executorId);
        LinkedListNode<ExecutorState> node = actions.newNode();
        ExecutorServiceWithKey result = new ExecutorServiceWithKey(core, node);
        ExecutorState action = new ExecutorState();
        action.executorId = executorId;
        action.executorService = result;
        node.setValue(action);
        return result;
    }

    protected ExecutorService createSingleThreadExecutor(int executorId) {
        ThreadFactory namingThreadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("single-thread-executor-" + executorId);
            thread.setDaemon(isDaemon); // Daemon threads auto-shutdown with JVM
            return thread;
        };

        ExecutorService executor = Executors.newSingleThreadExecutor(namingThreadFactory);

        return executor;
        // Use MoreExecutors to ensure the executor auto-shuts down after idleTimeout
//        ExecutorService result = idleTimeout >= 0
//            ? MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)executor, idleTimeout, timeUnit)
//            : MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)executor);

//        return result;
    }

    private void giveBack(ExecutorServiceWithKey executor) {
        LinkedListNode<ExecutorState> node = executor.getNode();
        node.getValue().idleTimestamp = System.currentTimeMillis();
        // Note: Even if there are more than maxIdleExecutors executors right now then
        // we still only clean them up after the idle delay.
        synchronized (actions) {
            node.moveToEnd();
        }
        scheduleCleanup();
    }

    /** Releases the executor (allows custom behavior if needed). Called from the cleanupTask. */
    private void releaseExecutor(ExecutorServiceWithKey executor, boolean updateExecutorMap) {
        LinkedListNode<ExecutorState> node = executor.getNode();
        int executorId = node.getValue().executorId;
        node.unlink();
        if (updateExecutorMap) {
            executorMap.remove(executorId);
        }
        executor.shutdown();
        idPool.giveBack(executorId);
        if (logger.isDebugEnabled()) {
            logger.debug("Releasing executor #{}.", executorId);
        }
    }

    /** Shutdown all executors in the pool; pool should no longer be used then anymore. */
    public void shutdownAll() {
        if (isShutdown.compareAndSet(false, true)) {
            synchronized (actions) {
                if (timer != null) {
                    timer.cancel();
                }
                for (ExecutorServiceWithKey executor : executorMap.values()) {
                    releaseExecutor(executor, false);
                }
                executorMap.clear();
            }
        }
    }

    private void doCleanup() {
        synchronized (actions) {
            isCleanupScheduled = false;
            if (logger.isDebugEnabled()) {
                logger.debug("Cleanup of idle service executors starting.");
            }
            int cleanupCount = 0;
            LinkedListNode<ExecutorState> node = actions.getFirstNode();
            long delta = -1;
            if (node != null) {
                long currentTime = System.currentTimeMillis();
                while(node != null) {
                    ExecutorState action = node.getValue();
                    long timestamp = action.idleTimestamp;
                    delta = currentTime - timestamp;
                    if (delta >= idleTimeout || actions.size() > maxIdleExecutors) {
                        ++cleanupCount;
                        releaseExecutor(action.executorService, true);
                        delta = -1;
                    } else {
                        break;
                    }
                    node = node.getNext();
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Cleanup of idle service executors done - {} idle executors released.", cleanupCount);
            }

            if (delta >= 0) {
                // timer.schedule(this, delta);
                scheduleCleanup();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ExecutorServicePool pool = new ExecutorServicePool();

        ExecutorService es0 = pool.acquireExecutor();
        es0.submit(() -> System.out.println(Thread.currentThread().getName() + " says hi!"));

        // Thread.sleep(5000);

        ExecutorService es1 = pool.acquireExecutor();
        es1.submit(() -> System.out.println(Thread.currentThread().getName() + " says hello!"));
        es1.shutdown();

        es0.shutdown();

        pool.shutdownAll();
    }
}
