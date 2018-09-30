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

import static org.awaitility.Awaitility.await ;
import static org.apache.jena.atlas.lib.Lib.sleep ;

import java.util.concurrent.TimeUnit ;
import static java.util.concurrent.TimeUnit.* ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

public class TestAlarmClock extends BaseTest {
    /* Issues with MS Windows.
     * 
     * Running some of these tests on windows is unreliable; sometimes they pass,
     * sometimes one or more fails.
     *  
     * This seems to be that when the CI server (ASF Jenkins, Windows VM)
     * is under load then the ScheduledThreadPoolExecutor used by AlarmClock 
     * is unreliable.  
     * 
     * But setting the times so high for this slows the tests down a lot
     * and makes some of them fairly pointless.
     * 
     * The use of awaitility helps this - the timeouts can be set quite long
     * and the polling done means the full wait time does not happen normally.  
     */

    private AtomicInteger count    = new AtomicInteger(0) ;
    private Runnable      callback = ()->count.getAndIncrement() ;
    
    @Test
    public void alarm_01() {
        AlarmClock alarmClock = new AlarmClock() ;
        // Very long - never happens.
        Alarm a = alarmClock.add(callback, 10000000) ;
        alarmClock.cancel(a) ;
        assertEquals(0, count.get()) ;
        alarmClock.release() ;
    }

    private void awaitUntil(int value, long timePeriod, TimeUnit units) {
        await()
        .atMost(timePeriod, units)
        .until(() -> {
            return count.get() == value ;
        }) ;
    }
    
    @Test
    public void alarm_02() {
        AlarmClock alarmClock = new AlarmClock() ;
        // Short - happens.
        Alarm a = alarmClock.add(callback, 10) ;
        
        awaitUntil(1, 500, MILLISECONDS) ;
        
        // try to cancel anyway.
        alarmClock.cancel(a) ;
        alarmClock.release() ;
    }

    @Test
    public void alarm_03() {
        AlarmClock alarmClock = new AlarmClock() ;
        Alarm a1 = alarmClock.add(callback, 10) ;
        Alarm a2 = alarmClock.add(callback, 1000000) ;
        
        awaitUntil(1, 500, MILLISECONDS) ;
        
        alarmClock.cancel(a2) ;
        alarmClock.release() ;
    }

    @Test
    public void alarm_04() {
        AlarmClock alarmClock = new AlarmClock() ;
        Alarm a1 = alarmClock.add(callback, 10) ;
        Alarm a2 = alarmClock.add(callback, 20) ;
        
        awaitUntil(2, 500, MILLISECONDS) ;

        alarmClock.release() ;
    }

    @Test
    public void alarm_05() {
        AlarmClock alarmClock = new AlarmClock() ;
        Alarm a = alarmClock.add(callback, 50) ;
        alarmClock.reset(a, 20000) ;
        
        sleep(150) ;
        
        // Did not go off.
        assertEquals(0, count.get()) ;
        alarmClock.cancel(a);
        alarmClock.release() ;
    }
}
