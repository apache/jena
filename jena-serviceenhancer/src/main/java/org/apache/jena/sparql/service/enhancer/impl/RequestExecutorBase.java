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

package org.apache.jena.sparql.service.enhancer.impl;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.service.enhancer.concurrent.ExecutorServicePool;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbortableIterator;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbortableIteratorBase;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbortableIteratorPeek;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbortableIterators;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbstractAbortableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closer;

/** A flat map iterator that can read ahead in the input iterator and run flat map operations concurrently. */
public abstract class RequestExecutorBase<G, I, O>
    extends AbstractAbortableIterator<O>
{
    private static final Logger logger = LoggerFactory.getLogger(RequestExecutorBase.class);

    /**
     * Whether to order output across all batches by the individual items (ITEM)
     * or whether each batch is processed as one consecutive unit (BATCH).
     *
     * For item-level ordering each item must have an ordinal.
     */
    public enum Granularity {
        ITEM,
        BATCH
    }

    protected static record InternalBatch<G, I>(long batchId, boolean isInNewThread, G groupKey, List<I> batch, List<Long> reverseMap) {}

    /**
     * Transaction-aware interface for the customizing iterator creation.
     * The begin and end methods are intended for transaction management.
     *
     * Each method is only invoked once and all methods are always invoked on the same thread.
     * The end method will be invoked upon closing the iterator or if iterator creation fails.
     */
    public interface IteratorCreator<T> extends Creator<AbortableIterator<T>> {}

    static class EnqueTask<T>
        implements Runnable
    {
        public static final Object POISON = new Object();

        private final IteratorCreator<T> iteratorCreator;
        private final UnaryOperator<T> detacher;
        private final BlockingQueue<Object> queue;
        private final Runnable onThreadEnd;

        // The iterator is obtained from the iteratorCreator during run().
        private AbortableIterator<T> iterator;
        // private CountDownLatch terminationLatch = new CountDownLatch(1);

        // If an error occurs then it is tracked in this field.
        private volatile Throwable throwable;

        private volatile boolean isAborted = false;
        private volatile boolean poisonEnqeued = false;
        private boolean isTerminated = false;

        public EnqueTask(IteratorCreator<T> iteratorCreator, UnaryOperator<T> detacher, BlockingQueue<Object> queue, Runnable onThreadEnd) {
            this.iteratorCreator = iteratorCreator;
            this.detacher = detacher;
            this.queue = queue;
            this.onThreadEnd = onThreadEnd;
        }

        /**
         * Set the flag that the task is considered cancelled.
         * Upon the next interrupt all resources will be freed.
         * Note that you MUST interrupt the thread to abort it prematurely.
         */
        public void setAbortFlag() {
            this.isAborted = true;
        }

        private void enquePoison() {
            if (!poisonEnqeued) {
                while (true) {
                    try {
                        queue.put(POISON);
                        poisonEnqeued = true;
                        break;
                    } catch (Throwable t) {
                        logger.warn("Iterrupted while attempting to place POISON on the queue.", t);
                    }
                }
            }
        }

        public boolean isPoisonEnqueued() {
            return poisonEnqeued;
        }

        protected void setException(Throwable throwable) {
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public void run() {
            // Abort if there already was an error.
            if (throwable != null) {
                throw new RuntimeException(throwable);
            }

            // If the poison was enqueued then all processing is complete -
            // regardless whether normally or exceptionally.
            if (isPoisonEnqueued()) {
                return;
            }

            boolean isPaused = false;
            try {
                runInternal();
            } catch (InterruptedException e) {
                if (!isAborted) {
                    // Exit without exception if interrupted without abort.
                    isPaused = true;
                } else {
                    setException(new CancellationException());
                }
            } catch (Throwable t) {
                setException(t);
            } finally {
                if (!isPaused) {
                    terminate();
                }
            }
        }

        public void runInternal() throws InterruptedException {
            // If cancelled then don't start the iterator.
            if (isAborted) {
                throw new CancellationException();
            }

            if (iterator == null) {
                iterator = iteratorCreator.create();
            }

            // Always reserve one slot for the poison.
            while (iterator.hasNext()) { // Rely on the queue's InterruptedException - no Thread.interrupted() here.
                T item = iterator.next();
                T detachedItem = detacher == null ? item : detacher.apply(item);

                // Rely on InterruptedException here.
                queue.put(detachedItem);
            }
        }

        protected void terminate() {
            if (!isTerminated) {
                isTerminated = true;
                try {
                    if (iterator != null) {
                        iterator.close();
                    }
                } finally {
                    try {
                        enquePoison();
                    } finally {
                        onThreadEnd.run();
                    }
                }

                if (throwable != null) {
                    throw new RuntimeException(throwable);
                }
            }
        }
    }

    static class IteratorWrapperViaThread<T>
        extends AbstractAbortableIterator<T>
    {
        protected EnqueTask<T> enqueTask;

        /**
         * A function to detach items from the life-cycle of the iterator.
         * For example, TDB2 Bindings must be detached from resources that are free'd when the iterator is closed.
         */
        protected UnaryOperator<T> detachFn;


        // protected ExecutorServicePool executorServicePool;
        protected ExecutorService executorService;
        protected Future<?> future;

        /**
         * Deque item type is of type Object in order to be capabable of holding the POISON.
         * All other items are of type T.
         */
        protected BlockingQueue<Object> queue;
        protected volatile Throwable throwable = null;
        protected volatile List<T> buffer;

        public IteratorWrapperViaThread(
                ExecutorService executorSerivce, int maxQueueSize, IteratorCreator<T> iteratorCreator, UnaryOperator<T> detacher,
                Runnable onThreadEnd) {
            if (maxQueueSize < 1) {
                throw new IllegalArgumentException("Queue size must be at least 1.");
            }

            this.executorService = executorSerivce;
            this.queue = new ArrayBlockingQueue<>(maxQueueSize + 1); // Internally add one for the poison.
            this.enqueTask = new EnqueTask<>(iteratorCreator, detacher, queue, onThreadEnd);
            // TODO Probably we need a lambda for when the thread exits
            //   Need to hand the executor service back.
        }

        private void start() {
            if (!isFinished()) {
                if (future == null) {
                    future = executorService.submit(enqueTask);
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public T moveToNext() {
            Object item;
            try {
                item = queue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            T result;
            if (item == EnqueTask.POISON) {
                result = endOfData();
                Throwable t = enqueTask.getThrowable();
                if (t != null) {
                    throw new RuntimeException(t);
                }
            } else {
                result = (T)item;
            }
            return result;
        }

        @Override
        protected void requestCancel() {
            enqueTask.setAbortFlag();
            if (future != null) {
                future.cancel(true);
            }
        }

        public void pause() {
            stop();
        }

        @Override
        protected void closeIteratorActual() {
            // Without the abort flag non-terminated task would only pause and retain their resources.
            // Setting the abort flag ensures that the task terminates upon interruption and frees its resources.
            enqueTask.setAbortFlag();
            stop();
        }

        private void stop() {
            if (future != null) {
                // XXX Java limitation: future.cancel(true) followed by future.get() does not
                // wait for the thread to exit.
                future.cancel(true);
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException | CancellationException e) {
                    // Ignore
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                /*
                try {
                    enqueTask.getTerminationLatch().await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                */
                future = null;
            }
        }

        @Override
        public void output(IndentedWriter out, SerializationContext sCxt) {
            // TODO Auto-generated method stub
        }
    }

    protected static long getCloseId(Granularity granularity, TaskEntry<?> taskEntry) {
        long result = switch (granularity) {
            case ITEM -> taskEntry.getServedInputIds().get(taskEntry.getServedInputIds().size() - 1);
            case BATCH -> taskEntry.getBatchId();
        };
        return result;
    }

    interface TaskEntry<O>
        extends AutoCloseable
    {
        long getBatchId();
        List<Long> getServedInputIds();
        AbortableIteratorPeek<O> stopAndGet();
        @Override void close();
    }

    class TaskEntryEmpty
        implements TaskEntry<O>
    {
        private long closeId;
        private List<Long> servedInputIds;

        public TaskEntryEmpty(long closeId) {
            super();
            this.closeId = closeId;
            this.servedInputIds = List.of(closeId);
        }

        @Override
        public long getBatchId() {
            return closeId;
        }

        @Override
        public List<Long> getServedInputIds() {
            return servedInputIds;
        }

        @Override
        public AbortableIteratorPeek<O> stopAndGet() {
            return new AbortableIteratorPeek<>(AbortableIterators.empty());
        }

        @Override
        public void close() {}
    }

    abstract class TaskEntryBatchBase
        implements TaskEntry<O>
    {
        protected InternalBatch<G, I> batch;

        public TaskEntryBatchBase(InternalBatch<G, I> batch) {
            super();
            this.batch = batch;
        }

        @Override
        public long getBatchId() {
            return batch.batchId();
        }

        @Override
        public List<Long> getServedInputIds() {
            return batch.reverseMap();
        }
    }

    class TaskEntryDirect
        extends TaskEntryBatchBase
    {
        protected IteratorCreator<O> iteratorCreator;
        protected AbortableIteratorPeek<O> createdIterator;

        public TaskEntryDirect(InternalBatch<G, I> batch) {
            super(batch);
        }

        @Override
        public AbortableIteratorPeek<O> stopAndGet() {
            if (iteratorCreator == null) {
                iteratorCreator = processBatch(batch);
                AbortableIterator<O> it = iteratorCreator.create();
                createdIterator = new AbortableIteratorPeek<>(it);
            }
            return createdIterator;
        }

        @Override
        public void close() {
            Iter.close(createdIterator);
        }
    }

    // There are three ways to execute sub-iterators:
    // EmptyTask - a dummy task that has a closeId but does not produce items
    // DirectTask - return an iterator directly on the driver thread
    // AsyncTask - items are prefetched until the driver calls "stop". for scattered data the driver could actually restart the fetch-ahead again... but let's not do that now. probably it would be relatively easy to add later.

    /** Helper class for tracking running prefetch tasks. */
    // TODO Free the task once the thread exits.
    //   What happens if the task itself gives back the executor?! -> The executor will be simply handed back to the pool, but the task will exit only shortly after.
    //   This means that the executor may be briefly occupied which should be harmless.
    class TaskEntryAsync
        extends TaskEntryBatchBase
    {
        protected final ExecutorService executorService;
        protected final Runnable onThreadEnd;

        // Volatile because iterator can be created by any thread.
        protected volatile AbortableIteratorPeek<O> peekIter;

        public TaskEntryAsync(InternalBatch<G, I> batch, ExecutorService executorService, Runnable onThreadEnd) {
            super(batch);
            this.executorService = executorService;
            this.onThreadEnd = onThreadEnd;
        }

        // protected volatile boolean isFreed = false;
        public synchronized void startInNewThread() {
            if (peekIter == null) {
                boolean isInNewThread = true;

                IteratorCreator<O> creator = processBatch(batch);
                UnaryOperator<O> detacher = x -> detachOutput(x, isInNewThread);
                IteratorWrapperViaThread<O> threadIt = new IteratorWrapperViaThread<>(
                    executorService, maxBufferAhead, creator, detacher, onThreadEnd);

                // Start the thread on the iterator.
                threadIt.start();
                peekIter = new AbortableIteratorPeek<>(threadIt);
            }
        }

        /**
         * Stop the task and return an iterator over the buffered items and the remaining ones.
         * Closing the returned iterator also closes the iterator over the remaining items.
         *
         * In case of an exception, this task entry closes itself.
         */
        // Synchronized to protect against the case when the driver wants to consume data from a task before
        // that task was started from the taskQueue.
        @Override
        public synchronized AbortableIteratorPeek<O> stopAndGet() {
            startInNewThread();
            return peekIter;
        }

        @Override
        public synchronized void close() {
            startInNewThread();
            // Make sure to close peekIter.
            // if (peekIter != null) {
                peekIter.close();
            // }
        }
    }

    /**  Ensure that at least there are active requests to serve the next n input bindings */
    protected int maxFetchAhead = 100; // Fetch ahead is for additional task slots once maxConcurrentTasks have completed.

    /**
     * Only the driver is allowed to read from batchIterator because of a possibly running transaction.
     * Buffer ahead is how many items to read (and detach) from batchIterator so that new tasks can be scheduled
     * asynchronously (i.e. without the driver thread having to interfere).
     */
    protected int maxBufferAhead = 100;

    /** If batch granularity is true then output is ordered according to the obtained batches.
     *  Otherwise, output is ordered according to the individual member ids of the batches (assumes ids are unique across batches). */
    protected Granularity granularity;
    protected AbortableIterator<GroupedBatch<G, Long, I>> batchIterator;

    protected long nextBatchId = 0; // Counter of items taken from batchIterator

    // Input iteration.
    protected long currentInputId = -1;
    protected TaskEntry<O> activeTaskEntry = null; // Cached reference for inputToOutputIt.get(currentInputId).
    protected AbortableIteratorPeek<O> activeIter;

    /**
     * Tasks ordered by their input id - regardless of whether they are run concurrently or not.
     * The complexity here is that the multiple inputIds may map to the same task.
     * Conversely: A single task may serve multiple input ids.
     */
    protected Map<Long, TaskEntry<O>> inputToOutputIt = new LinkedHashMap<>();

    /** The task queue is used to submit the tasks in "inputToOutputIt" to executors. */
    protected AtomicInteger taskQueueCapacity;
    protected Deque<TaskEntryAsync> taskQueue = new ArrayDeque<>();

    /* State for tracking concurrent prefetch ----------------------------- */

    protected ExecutorServicePool executorServicePool;

    private final int maxConcurrentTasks;
    private final long concurrentSlotReadAheadCount;

    // Whenever a task is started, the count is incremented.
    // Tasks decrement the count themselves just before exiting.
    private final AtomicInteger freeWorkerSlots = new AtomicInteger();

    private Meter throughputMeter = new Meter(5);
    // private Deque<Entry<Long, Integer>> throughputHistory = new ArrayDeque<>(5);
    // private AtomicInteger completedTasksSinceLastNext = new AtomicInteger();
    // private int numRecentScheduledTasks;

    /** The concurrently running tasks. Indexed once by the last input id (upon which to close). */
    // private final Map<Long, TaskEntry<O, AbortableIterator<O>>> openConcurrentTaskEntriesRunning = new ConcurrentHashMap<>();

    /**
     * Completed tasks are moved from the running map to this one by the driver thread.
     * This map is used to count the number of completed tasks and the remaining task slots.
     * This map is not used for resource management.
     */
    // private final Map<Long, TaskEntry<O, AbortableIterator<O>>> openConcurrentTaskEntriesCompleted = new ConcurrentHashMap<>();

    /* Actual implementation ---------------------------------------------- */

    public RequestExecutorBase(
            AtomicBoolean cancelSignal,
            Granularity granularity,
            AbortableIterator<GroupedBatch<G, Long, I>> batchIterator,
            int maxConcurrentTasks,
            long concurrentSlotReadAheadCount) {
        super(cancelSignal);
        this.granularity = Objects.requireNonNull(granularity);
        this.batchIterator = Objects.requireNonNull(batchIterator);

        // Set up a dummy task with an empty iterator as the active one
        // (currentInputId set to -1) and ensure it gets properly closed.
        activeTaskEntry = new TaskEntryEmpty(currentInputId); // emptyTaskEntry(currentInputId); //TaskEntry.empty(currentInputId);
        inputToOutputIt.put(currentInputId, activeTaskEntry);

        this.activeIter = activeTaskEntry.stopAndGet();
        this.maxConcurrentTasks = maxConcurrentTasks;
        this.concurrentSlotReadAheadCount = concurrentSlotReadAheadCount;

        this.executorServicePool = new ExecutorServicePool();

        this.freeWorkerSlots.set(maxConcurrentTasks);
        this.taskQueueCapacity = new AtomicInteger(maxConcurrentTasks * 2);
    }


    protected void autoScale() {
        // XXX Scale the task queue size based on the number of processed items since last time coming here.
        // I think we want the maximum number of threads that finish per tick (in a window).
        // throughputMeter.tick();

        // If the task queue is empty then scale up the size of it.
        if (taskQueue.isEmpty()) {
            // TODO get current max capacity and double it.
            // taskQueueCapacity.
        }

        // If the task queue is non-empty then we can measure the number of
        // We can actually measure the min/max/avg time spent per concurrent thread until it was finished.
        // We can use this to scale the task queue and the threads at the same time
        // If more threads make it slower then we scale the thread count down again
        // through we might need to take into account whether a producer thread was blocked on the queue...


    }

    @Override
    protected O moveToNext() {
        O result = null;

        // Check whether to scale up or down the size of the task queue or the number of workers.
        autoScale();

        boolean didAdvanceActiveIter = false;
        // Peek the next binding on the active iterator and verify that it maps to the current
        // partition key.
        while (true) { // Note: Cancellation is handled by base class before calling moveToNext().
            if (activeIter.hasNext()) {
                O peek = activeIter.peek();
                // The iterator returns null if it was aborted.
                if (peek == null) {
                    break;
                }

                // On batch granularity always take the lowest input id served by a task.
                long peekOutputId = switch(granularity) {
                    case ITEM -> extractInputOrdinal(peek);
                    case BATCH -> activeTaskEntry.getBatchId();
                };

                boolean inputIdMatches = peekOutputId == currentInputId;
                if (inputIdMatches) {
                    result = activeIter.next();
                    break;
                }
            }

            // XXX Potential optimization: If activeIter of activeTask is not yet consumed then prefetching could be restarted.

            // If we come here then we need to advance the lhs.
            didAdvanceActiveIter = true;

            // Free up no longer needed resources.
            long closeId = getCloseId(granularity, activeTaskEntry);
            boolean isClosePoint = currentInputId == closeId;
            if (isClosePoint) {
                activeIter.close();
                activeTaskEntry.close();
            }

            // Remote the just processed entry for currentInputId.
            inputToOutputIt.remove(currentInputId);

            // Move to the next inputId
            ++currentInputId; // TODO peekOutputId may not have matched currentInputId -
                  // in this case we still get an entry in inputToOutputIt but the iterator will be empty.

            activeTaskEntry = inputToOutputIt.get(currentInputId);
            if (activeTaskEntry == null) {
                // No entry for the advanced inputId - check whether further batches need to be executed.
                prepareNextBatchExecs();

                activeTaskEntry = inputToOutputIt.get(currentInputId);

                // If there is still no further batch then we must have reached the end.
                if (activeTaskEntry == null) {
                    break;
                }
            }

            // Stop any concurrent prefetching and get the iterator.
            activeIter = activeTaskEntry.stopAndGet();
        }

        if (result == null) {
            result = endOfData();
        } else {
            if (!didAdvanceActiveIter) {
                // Check whether to schedule any further concurrent tasks.
                // Check was already performed if activeIter was advanced.
                prepareNextBatchExecs();
            }
        }

        return result;
    }

    protected void registerTaskEntry(TaskEntry<O> taskEntry) {
        switch (granularity) {
        case ITEM:
            List<Long> servedInputIds = taskEntry.getServedInputIds();
            for (Long e : servedInputIds) {
                inputToOutputIt.put(e, taskEntry);
            }
            break;
        case BATCH:
            long batchId = taskEntry.getBatchId();
            inputToOutputIt.put(batchId, taskEntry);
            break;
        default:
            throw new IllegalStateException("Should never come here.");
        }
    }

    public void prepareNextBatchExecs() {
        // FIXME There may be a task in the task queue.
        // Ideally the driver would already create the entry in inputToOutputMap.
        // and the executors would just call a start() method on the tasks.

        if (!inputToOutputIt.containsKey(currentInputId)) {
            // We need the task's iterator right away - do not start concurrent retrieval
            if (batchIterator.hasNext()) {
                InternalBatch<G, I> batch = nextBatch(false);
                TaskEntry<O> taskEntry = new TaskEntryDirect(batch);
                registerTaskEntry(taskEntry);
            }
        }

        fillTaskQueue();
        processTaskQueue();
    }

    /** This method is only called from the driver - but worker threads may take items from the queue. */
    protected void fillTaskQueue() {
        // Fail to schedule tasks if promoted to a write transaction.
        checkCanExecInNewThread();

        // TODO Do not fill the task queue when aborted.
        while (batchIterator.hasNext()) {
            // Check the capacity of the task queue before .
            int remainingCapacity = taskQueueCapacity.getAndUpdate(i -> Math.max(0, i - 1));
            if (remainingCapacity == 0) {
                break;
            }

            // Set up tasks that will be run asynchronously.
            InternalBatch<G, I> batch = nextBatch(true);
            ExecutorService executorService = executorServicePool.acquireExecutor();
            Runnable onThreadEnd = () -> {
                processTaskQueue();
                // Note: This lambda is run from the executor.
                //   Shutting the executor down hands it back to the executor pool.
                //   If the executor is meanwhile re-acquired it will be briefly blocked
                //   until this method exits. This should be harmless.
                executorService.shutdown();
                freeWorkerSlots.incrementAndGet();
                taskQueueCapacity.incrementAndGet();

//                System.out.println("freeWorkerSlots: " + freeWorkerSlots.get());
//                System.out.println("taskQueueCapacity: " + taskQueueCapacity.get());
            };

            TaskEntryAsync taskEntry = new TaskEntryAsync(batch, executorService, onThreadEnd);
            registerTaskEntry(taskEntry);
            taskQueue.add(taskEntry);
        }
    }

    /** Method called from driver or worker threads to start execution of tasks in the queue. */
    protected void processTaskQueue() {
        // System.err.println("Task queue size: " + taskQueue.size());
        // If there are more than 0 free slots then decrement the count; otherwise stay at 0.
        int freeSlots;
        while ((freeSlots = freeWorkerSlots.getAndUpdate(i -> Math.max(0, i - 1))) > 0) {
            // Need to ensure the queue is not empty.
            TaskEntryAsync taskEntry = taskQueue.poll();
            if (taskEntry == null) {
                // Nothing to execute - free the slot again
                freeWorkerSlots.incrementAndGet();
                break;
            }

            // Launch the task. The task is set up to call "freeTaskSlots.incrementAndGet()" upon completion.
            taskEntry.startInNewThread();
        }
    }

    protected abstract boolean isCancelled();

    protected IteratorCreator<O> processBatch(InternalBatch<G, I> batch) {
        boolean isInNewThread = batch.isInNewThread();
        // long batchId = batch.batchId();
        G groupKey = batch.groupKey();
        List<I> inputs = batch.batch();
        List<Long> reverseMap = batch.reverseMap();
        IteratorCreator<O> result = processBatch(isInNewThread, groupKey, inputs, reverseMap);
        return result;
    }

    protected abstract IteratorCreator<O> processBatch(boolean isInNewThread, G groupKey, List<I> batch, List<Long> reverseMap);

    protected abstract long extractInputOrdinal(O input);
    protected abstract void checkCanExecInNewThread();

    protected I detachInput(I item, boolean isInNewThread) { return item; }
    protected O detachOutput(O item, boolean isInNewThread) { return item; }

    /** Consume the next batch from the batchIterator. */
    protected InternalBatch<G, I> nextBatch(boolean isInNewThread) {
        GroupedBatch<G, Long, I> batchRequest = batchIterator.next();
        long batchId = nextBatchId++;
        Batch<Long, I> batch = batchRequest.getBatch();
        NavigableMap<Long, I> batchItems = batch.getItems();

        G groupKey = batchRequest.getGroupKey();

        // Detach inputs.
        Collection<I> rawInputs = batchItems.values();
        List<I> detachedInputs = new ArrayList<>(rawInputs.size());
        rawInputs.forEach(rawInput -> detachedInputs.add(detachInput(rawInput, isInNewThread)));
        List<I> inputs = Collections.unmodifiableList(detachedInputs);

        List<Long> reverseMap = List.copyOf(batchItems.keySet());

        InternalBatch<G, I> result = new InternalBatch<>(batchId, isInNewThread, groupKey, inputs, reverseMap);
        return result;
    }

    protected void freeResources() {
        // Use closer to free as much as possible in case of failure.
        try (Closer closer = Closer.create()) {
            closer.register(activeIter::close);

            // TODO The some objects may get closed multiple times - because the same task may be registered under multiple
            // input ids - use a IdentityHashSet(values())???
            for (TaskEntry<O> taskEntry : inputToOutputIt.values()) {
                Closeable closable = taskEntry.stopAndGet();
                closer.register(closable::close);
                closer.register(taskEntry::close);
            }

            closer.register(batchIterator::close);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void closeIteratorActual() {
        freeResources();
        if (false) {
            System.out.println("final taskQueueSize: " + taskQueue.size());
            System.out.println("final freeWorkerSlots: " + freeWorkerSlots.get());
            System.out.println("final taskQueueCapacity: " + taskQueueCapacity.get());
        }
    }

    @Override
    protected void requestCancel() {
        batchIterator.cancel();
        AbortableIteratorBase.performRequestCancel(activeIter);
    }
}
