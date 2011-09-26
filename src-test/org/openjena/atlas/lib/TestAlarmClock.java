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
