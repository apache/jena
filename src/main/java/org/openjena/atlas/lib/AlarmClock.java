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

package org.openjena.atlas.lib;

import java.util.HashSet ;
import java.util.Set ;
import java.util.Timer ;

/** An AlarmClock is an object that will make a call back at a preset time.
 * It addes to java.util.Timer by having an active Timer (and its thread)
 * only when callbacks are outstanding.  The Timer's thread can stop the JVM exiting.
 */
public class AlarmClock
{
    // Our callback-later instance
    // Wrap a TimerTask so that the TimerTask.cancel operation can not be called
    // directly by the app. We need to go via AlarmClock tracking of callbacks so
    // we can release the Timer in AlarmClock.

    public Timer timer = null ;
    public Set<Pingback<?>> outstanding = new HashSet<Pingback<?>>() ;
    
    public AlarmClock() {}
    
    static private AlarmClock singleton = new AlarmClock() ; ;
    /** Global singleton for general use */ 
    static public AlarmClock get()
    {
        return singleton ;
    }

    synchronized public long getCount() { return outstanding.size() ; }
    
    synchronized public Pingback<?> add(Callback<?> callback, long delay)
    {
        return add(callback, null, delay) ;
    }
    
    synchronized public <T> Pingback<T> add(Callback<T> callback, T argument, long delay)
    {    
        Pingback<T> x = new Pingback<T>(this, callback, argument) ;
        add$(x, delay) ;
        return x ;
    }
    
    private <T> void add$(Pingback<T> pingback, long delay)
    {
        if ( outstanding.contains(pingback) )
            throw new InternalErrorException("Pingback already in use") ;
        getTimer().schedule(pingback.timerTask, delay) ;
        outstanding.add(pingback) ;
    }

    synchronized public <T> Pingback<T> reset(Pingback<T> pingback, long delay)
    {
        if ( timer != null )
            cancel$(pingback, false) ;
        // Experimentation shows we need to create a new TimerTask. 
        pingback = new Pingback<T>(this, pingback.callback, pingback.arg) ;
        add$(pingback, delay) ;
        return pingback ;
    }

    synchronized public void cancel(Pingback<?> pingback)
    {
        if ( pingback == null )
            return ;
        cancel$(pingback, true) ;
    }
    
    private void cancel$(Pingback<?> pingback, boolean clearTimer)
    {
        if ( timer == null )
            // Nothing outstanding.
            return ;
        outstanding.remove(pingback) ;
        // Throw timer, and it's thread, away if no outstanding pingbacks.
        // This helps apps exit properly (daemon threads don't always seem to be as clean as porimised)
        // but may be troublesome in large systems.  May reconsider. 
        if ( clearTimer && getCount() == 0 )
        {
            timer.cancel();
            timer = null ;
        }
    }
    
    /*synchronized*/ private Timer getTimer()
    {
        if ( timer == null )
            timer = new Timer(true) ;
        return timer ;
    }
    
    public long timeStart = System.currentTimeMillis() ;
}
