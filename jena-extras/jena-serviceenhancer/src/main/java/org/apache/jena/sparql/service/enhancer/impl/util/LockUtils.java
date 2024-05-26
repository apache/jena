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

package org.apache.jena.sparql.service.enhancer.impl.util;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockUtils {
    private static final Logger logger = LoggerFactory.getLogger(LockUtils.class);

    /**
     * Perform an action which requires acquisition of a lock first.
     * An attempt is made to acquire the lock. If this fails then the action is not run.
     * Upon completion of the action (successful or exceptional) the lock is released again.
     */
    public static <T> T runWithLock(Lock lock, Callable<T> action) {
        T result = null;
        try {
            lock.lock();
            result = action.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        return result;
    }

    /** Run an action after locking; eventually the lock is unlocked in a finally block */
    public static void runWithLock(Lock lock, ThrowingRunnable action) {
        runWithLock(lock, () -> { action.run(); return null; });
    }

    /**
     * Run this action with a short-lived locked. If the lock cannot be acquired
     * within the given time it is considered stale and forcibly unlocked.
     * Subsequently another attempt is made to acquire the lock.
     */
    public static <T, L extends Lock> T runWithMgmtLock(
            L lock,
            Consumer<? super L> forceUnlock,
            Duration duration,
            Callable<T> action) {
        T result = null;
        try {
            long timeout = duration.toMillis();
            boolean isLocked;
            if (!(isLocked = lock.tryLock(timeout, TimeUnit.MILLISECONDS))) {

                logger.warn(String.format("Forcibly unlocking stale lock %s", lock));
                forceUnlock.accept(lock);

                isLocked = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
                if (!isLocked) {
                    throw new RuntimeException("Failed to acquire lock despite forced unlocking");
                }
            }

            result = action.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        return result;
    }

}
