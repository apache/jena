/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import static org.openjena.atlas.lib.Lib.sleep; 
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

public class TestAlarmClock extends BaseTest
{
    Callback<?> callback = new Callback<Object>() { public void proc(Object arg) {} } ; 
    
    @Test public void alarm_01()
    {
        AlarmClock alarmClock = new AlarmClock() ;
        assertEquals(0, alarmClock.getCount()) ;
        // Very long - never happens.
        Pingback<?> ping = alarmClock.add(callback, 10000000) ;
        assertEquals(1, alarmClock.getCount()) ;
        alarmClock.cancel(ping) ;
        assertEquals(0, alarmClock.getCount()) ;
    }
    
    @Test public void alarm_02()
    {
        AlarmClock alarmClock = new AlarmClock() ;
        assertEquals(0, alarmClock.getCount()) ;
        // Short - happens.
        Pingback<?> ping = alarmClock.add(callback, 10) ;
        sleep(100) ;
        assertEquals(0, alarmClock.getCount()) ;
        
        // try to cancel anyway.
        alarmClock.cancel(ping) ;
        assertEquals(0, alarmClock.getCount()) ;
    }

    @Test public void alarm_03()
    {
        AlarmClock alarmClock = new AlarmClock() ;
        assertEquals(0, alarmClock.getCount()) ;
        Pingback<?> ping1 = alarmClock.add(callback, 100) ;
        Pingback<?> ping2 = alarmClock.add(callback, 100000) ;
        assertEquals(2, alarmClock.getCount()) ;
        sleep(200) ;
        // ping1 went off.
        assertEquals(1, alarmClock.getCount()) ;
        alarmClock.cancel(ping1) ;
        assertEquals(1, alarmClock.getCount()) ;
        alarmClock.cancel(ping2) ;
        assertEquals(0, alarmClock.getCount()) ;
    }

    @Test public void alarm_04()
    {
        AlarmClock alarmClock = new AlarmClock() ;
        assertEquals(0, alarmClock.getCount()) ;
        Pingback<?> ping1 = alarmClock.add(callback, 100) ;
        assertEquals(1, alarmClock.getCount()) ;
        alarmClock.reset(ping1, 2000) ;
        assertEquals(1, alarmClock.getCount()) ;
        sleep(100) ;
        assertEquals(1, alarmClock.getCount()) ;
    }
    
    @Test public void alarm_05()
    {
        AlarmClock alarmClock = new AlarmClock() ;
        assertEquals(0, alarmClock.getCount()) ;
        Pingback<?> ping1 = alarmClock.add(callback, 50) ;
        Pingback<?> ping2 = alarmClock.add(callback, 100) ;
        assertEquals(2, alarmClock.getCount()) ;
        alarmClock.reset(ping1, 2000) ;
        assertEquals(2, alarmClock.getCount()) ;
        sleep(200) ;    // ping2 goes off.
        assertEquals(1, alarmClock.getCount()) ;
    }
    
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