/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
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