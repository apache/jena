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

package org.apache.jena.atlas.lib ;

import java.util.concurrent.* ;

import org.apache.commons.lang3.concurrent.BasicThreadFactory ;

/**
 * An AlarmClock is an object that will make a callback (with a value) at a
 * preset time. Simple abstraction of add/reset/cancel of a Runnable. Currently,
 * backed by {@link ScheduledThreadPoolExecutor}
 */
public class AlarmClock {
    // A ThreadFactory that creates daemons
    private static ThreadFactory threadFactory = new BasicThreadFactory.Builder()
        .daemon(true)
        .namingPattern("alarm-clock-%d")
        //.uncaughtExceptionHandler(null)
        .build() ;

    private ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1, threadFactory) ;

    /* package */AlarmClock() {}

    static private AlarmClock singleton = new AlarmClock() ;

    /** Global singleton for general use */
    static public AlarmClock get() {
        return singleton ;
    }

    /** Add a task to be called after a delay (in milliseconds) */
    public Alarm add(Runnable task, long delay) {
        if ( task == null )
            throw new IllegalArgumentException("Task is null") ;
        ScheduledFuture<?> future = timer.schedule(task, delay, TimeUnit.MILLISECONDS) ;
        return new Alarm(this, task, future) ;
    }

    /** Reschedule a task to now run after a different delay from now (in milliseconds) */
    public Alarm reset(Alarm alarm, long delay) {
        cancel(alarm) ;
        return add(alarm.task, delay) ;
    }

    /** Cancel a task  */
    public void cancel(Alarm alarm) {
        alarm.future.cancel(false) ;
    }

    /** Clean up */
    public void release() {
        timer.shutdownNow() ;
    }
}
