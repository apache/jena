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

import static org.apache.jena.atlas.lib.Lib.sleep ;

import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.base.Sys ;
import org.junit.Test ;

public class TestAlarmClock extends BaseTest {
    /* Issues with MS Windows.
     * 
     * Running some of these tests on windows is unreliable; sometimes they pass,
     * sometimes one fails.
     *  
     * This seems to be that when the CI server (ASF Jenkins, Windows VM)
     * is under load then the ScheduledThreadPoolExecutor used by AlarmClock 
     * is unreliable.  
     * 
     * But setting the times so high for this slows the tests down a lot
     * and makes some of them fairly pointless.
     * 
     * alarm_03 is very sensitive.  A sleep of 200 is still not stable
     * the callback is not called (10ms callback).  It usually passses if there is
     * no other job on the machines, otherwise it fails >50% of the time.
     * 
     * Failures are masking the success/failure of unrelated development changes.
     * 
     * So skip some tests on windows.  
     */

    private AtomicInteger count    = new AtomicInteger(0) ;
    private Runnable      callback = ()->count.getAndIncrement() ;
    
    // Loaded CI.
    private static boolean mayBeErratic = Sys.isWindows ;
    
    private int timeout(int time1, int time2) {
        return mayBeErratic ? time2 : time1 ;
    }
    @Test
    public void alarm_01() {
        AlarmClock alarmClock = new AlarmClock() ;
        // Very long - never happens.
        Alarm a = alarmClock.add(callback, 10000000) ;
        alarmClock.cancel(a) ;
        assertEquals(0, count.get()) ;
        alarmClock.release() ;
    }

    @Test
    public void alarm_02() {
        AlarmClock alarmClock = new AlarmClock() ;
        // Short - happens.
        Alarm a = alarmClock.add(callback, 10) ;
        sleep(timeout(100, 250)) ;
        assertEquals(1, count.get()) ;
        // try to cancel anyway.
        alarmClock.cancel(a) ;
        alarmClock.release() ;
    }

    @Test
    public void alarm_03() {
        AlarmClock alarmClock = new AlarmClock() ;
        Alarm a1 = alarmClock.add(callback, 10) ;
        Alarm a2 = alarmClock.add(callback, 1000000) ;
        sleep(timeout(100, 300)) ;
        // ping1 went off.
        assertEquals(1, count.get()) ;
        alarmClock.cancel(a2) ;
        alarmClock.release() ;
    }

    @Test
    public void alarm_04() {
        AlarmClock alarmClock = new AlarmClock() ;
        Alarm a1 = alarmClock.add(callback, 10) ;
        Alarm a2 = alarmClock.add(callback, 20) ;
        sleep(timeout(150, 300)) ;
        // ping1 went off. ping2 went off.
        assertEquals(2, count.get()) ;
        alarmClock.release() ;
    }

    @Test
    public void alarm_05() {
        AlarmClock alarmClock = new AlarmClock() ;
        Alarm a = alarmClock.add(callback, 50) ;
        alarmClock.reset(a, 20000) ;
        sleep(timeout(100, 250)) ;
        alarmClock.cancel(a);
        alarmClock.release() ;
    }
}
