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

import java.util.TimerTask ;

/** Wrapper around a TimerTask, adding a callback with argument. */
public class Pingback<T>
{
    private final AlarmClock alarmClock ;
    final TimerTask timerTask ;
    final Callback<T> callback ;
    final T arg ;
    // As good as an AtomicBoolean which is implemented as a volative int for get/set.
    private volatile boolean cancelled = false ;

    Pingback(final AlarmClock alarmClock, final Callback<T> callback, T argument)
    {
        this.alarmClock = alarmClock ;
        this.callback = callback ;
        this.arg = argument ;
        this.timerTask = new TimerTask() {
            @Override
            public void run()
            {
                if ( cancelled )
                    return ;
                cancelled = true ;
                alarmClock.remove$(Pingback.this) ;
                callback.proc(arg) ;
            }
        } ;
    }
    
    void cancel()
    {
        timerTask.cancel() ;
        cancelled = true ;
        alarmClock.remove$(this) ;
    }
}

