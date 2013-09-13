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
import org.junit.Test ;

public class TestAlarmClock extends BaseTest {
    AtomicInteger count    = new AtomicInteger(0) ;
    Runnable      callback = new Runnable() {
                               @Override
                               public void run() {
                                   count.getAndIncrement() ;
                               }
                           } ;

    @Test
    public void alarm_01() {
        AlarmClock alarmClock = new AlarmClock() ;
        // Very long - never happens.
        alarmClock.add(callback, 10000000) ;
        alarmClock.cancel(callback) ;
        assertEquals(0, count.get()) ;
        alarmClock.release() ;
    }

    @Test
    public void alarm_02() {
        AlarmClock alarmClock = new AlarmClock() ;
        // Short - happens.
        alarmClock.add(callback, 10) ;
        sleep(150) ;
        assertEquals(1, count.get()) ;
        // try to cancel anyway.
        alarmClock.cancel(callback) ;
        alarmClock.release() ;
    }

    @Test
    public void alarm_03() {
        AlarmClock alarmClock = new AlarmClock() ;
        alarmClock.add(callback, 10) ;
        alarmClock.add(callback, 100000) ;
        sleep(150) ;
        // ping1 went off.
        assertEquals(1, count.get()) ;
        alarmClock.cancel(callback) ;
        alarmClock.release() ;
    }

    @Test
    public void alarm_04() {
        AlarmClock alarmClock = new AlarmClock() ;
        alarmClock.add(callback, 10) ;
        alarmClock.add(callback, 20) ;
        sleep(200) ;
        // ping1 went off. ping2 went off.
        assertEquals(2, count.get()) ;
        alarmClock.release() ;
    }

    @Test
    public void alarm_05() {
        AlarmClock alarmClock = new AlarmClock() ;
        alarmClock.add(callback, 100) ;
        alarmClock.reset(callback, 2000) ;
        sleep(50) ;
        assertEquals(0, count.get()) ;
        alarmClock.release() ;
    }
}
