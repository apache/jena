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

package org.apache.jena.atlas.lib;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

/** Misc class */
public class ThreadLib {

    private static ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Run asynchronously on another thread; the thread has started
     * when this function returns.
     */
    public static void async(Runnable r) {
        Semaphore semaStart = new Semaphore(0, true);
        Runnable r2 = () -> {
            semaStart.release(1);
            r.run();
        };
        executor.execute(r2);
        semaStart.acquireUninterruptibly();
    }

    /** Run synchronously but on another thread. */
    public static void syncOtherThread(Runnable r) {
        runCallable(()->{
            r.run();
            return null;
        });
    }

    /** Run synchronously but on another thread. */
    public static <T> T syncCallThread(Supplier<T> r) {
        return runCallable(() -> {
            T t = r.get();
            return t;
        });
    }

    private static <T> T runCallable(Callable<T> action) {
        try { return executor.submit(action).get(); }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Execute. Perform the "before" action, then main action.
     *  Always call the "after" runnable if the "before" succeeded.
     *  Be careful about argument order.
     * @param action
     * @param before
     * @param after
     */
    public static void withBeforeAfter(Runnable action, Runnable before, Runnable after) {
        before.run();
        try { action.run(); }
        finally { after.run();  }
    }

    /** Execute. Perform the "before" action, then main action.
     *  Always call the "after" runnable if the "before" succeeded.
     *  Be careful about argument order.
     * @param action
     * @param before
     * @param after
     */
    public static <V> V callWithBeforeAfter(Supplier<V> action, Runnable before, Runnable after) {
        before.run();
        try { return action.get(); }
        finally { after.run();  }
    }

    /** Execute; always call the "after" runnable */
    public static void withAfter(Runnable action, Runnable after) {
        try { action.run(); }
        finally { after.run();  }
    }

    /** Execute and return a value; always call the "after" runnable */
    public static <V> V callWithAfter(Supplier<V> action, Runnable after) {
        try { return action.get(); }
        finally { after.run();  }
    }

    /** Run inside a Lock */
    public static  <V> V callWithLock(Lock lock, Supplier<V> r) {
        return callWithBeforeAfter(r, ()->lock.lock(), ()->lock.unlock());
    }

    /** Run inside a Lock */
    public static void withLock(Lock lock, Runnable r) {
        withBeforeAfter(r, ()->lock.lock(), ()->lock.unlock());
    }
}
