/**
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

package org.apache.jena.fuseki.async;

import static java.lang.String.format;

import java.util.*;
import java.util.concurrent.*;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.servlets.ServletOps;

/** The set of currently active and recently completed tasks. */
public class AsyncPool
{
    // Max concurrent tasks.
    private static int nMaxThreads = 4;
    // Number of finished tasks kept.
    private static int MAX_FINISHED = 20;

    // A ThreadPoolExecutor with
    // * 0 to nMaxThreads
    // * no queue of waiting tasks (tasks execute or are rejected)
    // * dormant threads released after 120s.
    //
    // SynchronousQueue is a BlockingQueue that has zero length - it accepts and
    // delivers an item or rejects immediately, no delay by queueing.
    private ExecutorService executor = new ThreadPoolExecutor(0, nMaxThreads,
                                                              120L, TimeUnit.SECONDS,
                                                              new SynchronousQueue<Runnable>(true));
    private final Object mutex = new Object();
    private long counter = 0;
    private Map<String, AsyncTask> runningTasks = new LinkedHashMap<>();
    private Map<String, AsyncTask> finishedTasks = new LinkedHashMap<>();
    // Finite FIFO of finished tasks.
    private LinkedList<AsyncTask> finishedTasksList = new LinkedList<>();

    private static AsyncPool instance = new AsyncPool();

    public static AsyncPool get() { return instance; }

    private AsyncPool() { }

    public AsyncTask submit(Runnable task, String displayName, DataService dataService, long requestId) {
        synchronized(mutex) {
            String taskId = Long.toString(++counter);
            Fuseki.serverLog.info(format("Task : %s : %s",taskId, displayName));
            Callable<Object> c = ()->{
                try { task.run(); }
                catch (Throwable th) {
                    Fuseki.serverLog.error(format("Exception in task %s execution", taskId), th);
                }
                return null;
            };
            AsyncTask asyncTask = new AsyncTask(c, this, taskId, displayName, dataService, requestId);
            try {
                /* Future<Object> future = */ executor.submit(asyncTask);
                runningTasks.put(taskId, asyncTask);
                return asyncTask;
            } catch (RejectedExecutionException ex) {
                ServletOps.errorBadRequest("Async task request rejected - exceeds the limit of "+nMaxThreads+" tasks");
                return null;
            }
        }
    }

    public Collection<AsyncTask> tasks() {
        synchronized(mutex) {
            List<AsyncTask> x = new ArrayList<>(runningTasks.size()+finishedTasks.size());
            x.addAll(runningTasks.values());
            x.addAll(finishedTasks.values());
            return x;
        }
    }

    public void finished(AsyncTask task) {
        synchronized(mutex) {
            String id = task.getTaskId();
            runningTasks.remove(id);
            // Reduce old tasks list
            while ( finishedTasksList.size() >= MAX_FINISHED ) {
                AsyncTask oldTask = finishedTasksList.removeFirst();
                finishedTasks.remove(oldTask.getTaskId());
            }
            finishedTasks.put(id, task);
            finishedTasksList.add(task);
        }
    }

    public AsyncTask getRunningTask(String taskId) {
        synchronized(mutex) {
            return runningTasks.get(taskId);
        }
    }

    /**
     * Get a task - whether running or finished. Return null for no such task and also
     * retired tasks. Only a limited number of old, finished tasks are remembered.
     */
    public AsyncTask getTask(String taskId) {
        synchronized(mutex) {
            AsyncTask task = runningTasks.get(taskId);
            if ( task != null )
                return task;
            return finishedTasks.get(taskId);
        }
    }
}

