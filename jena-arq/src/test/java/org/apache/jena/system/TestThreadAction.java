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

package org.apache.jena.system;

import static org.junit.Assert.assertEquals ;

import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.atlas.logging.LogCtl;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

public class TestThreadAction {
    
    private static String  level ;

    @BeforeClass
    static public void beforeClass() {
        level = LogCtl.getLevel(ThreadAction.class) ;
        // ThreadAction logs warning on exceptions in before and after.
        LogCtl.setLevel(ThreadAction.class, "ERROR");
    }

    @AfterClass
    static public void afterClass() {
        // Restore logging setting.
        LogCtl.setLevel(ThreadAction.class, level) ;
    }
    
    @Test public void action_01() {
        AtomicInteger x = new AtomicInteger(0) ;
        ThreadAction action = ThreadAction.create(()->x.incrementAndGet()) ;
        assertEquals(0, x.get()) ;
        action.run() ;
        assertEquals(1, x.get()) ;
    }
    
    @Test public void action_02() {
        AtomicInteger x = new AtomicInteger(0) ;
        ThreadAction action = ThreadAction.create(null, ()->x.incrementAndGet(), null) ;
        assertEquals(0, x.get()) ;
        action.run() ;
        assertEquals(1, x.get()) ;
    }
    
    @Test public void action_03() {
        AtomicInteger before = new AtomicInteger(0) ;
        AtomicInteger runnable = new AtomicInteger(0) ;
        AtomicInteger after = new AtomicInteger(0) ;
        ThreadAction action = ThreadAction.create(()->before.incrementAndGet(),
                                                  ()->runnable.incrementAndGet(),
                                                  ()->after.incrementAndGet()) ;
        action.run() ;
        assertEquals(1, before.get()) ;
        assertEquals(1, runnable.get()) ;
        assertEquals(1, after.get()) ;
    }
    
    // Make silent.
    @Test public void action_04() {
        AtomicInteger before = new AtomicInteger(0) ;
        AtomicInteger runnable = new AtomicInteger(0) ;
        AtomicInteger after = new AtomicInteger(0) ;
        ThreadAction action = ThreadAction.create
            (()->{ before.incrementAndGet() ; bang() ; }, 
             ()->runnable.incrementAndGet(),
             ()->after.incrementAndGet()) ;
        action.run() ;
        assertEquals(1, before.get()) ;
        assertEquals(0, runnable.get()) ;
        assertEquals(0, after.get()) ;
    }

    @Test(expected=TestThreadActionException.class)
    public void action_05() {
        AtomicInteger before = new AtomicInteger(0) ;
        AtomicInteger runnable = new AtomicInteger(0) ;
        AtomicInteger after = new AtomicInteger(0) ;
        ThreadAction action = ThreadAction.create(()->before.incrementAndGet(), 
                                                  ()->{ runnable.incrementAndGet() ; bang() ; } ,
                                                  ()->after.incrementAndGet()) ;
        action.run() ;
        assertEquals(1, before.get()) ;
        assertEquals(1, runnable.get()) ;
        assertEquals(0, after.get()) ;
    }

    @Test
    public void action_06() {
        AtomicInteger before = new AtomicInteger(0) ;
        AtomicInteger runnable = new AtomicInteger(0) ;
        AtomicInteger after = new AtomicInteger(0) ;
        ThreadAction action = ThreadAction.create(()->before.incrementAndGet(), 
                                                  ()->{ runnable.incrementAndGet() ; bang() ; } ,
                                                  ()->after.incrementAndGet()) ;
        try { action.run() ; }
        catch (TestThreadActionException ex) {}
        assertEquals(1, before.get()) ;
        assertEquals(1, runnable.get()) ;
        assertEquals(1, after.get()) ;
    }

    @Test
    public void action_07() {
        AtomicInteger before = new AtomicInteger(0) ;
        AtomicInteger runnable = new AtomicInteger(0) ;
        AtomicInteger after = new AtomicInteger(0) ;
        ThreadAction action = ThreadAction.create(()->before.incrementAndGet(), 
                                                  ()->runnable.incrementAndGet() ,
                                                  ()->{ after.incrementAndGet(); bang() ; } ) ;
        action.run() ;
        assertEquals(1, before.get()) ;
        assertEquals(1, runnable.get()) ;
        assertEquals(1, after.get()) ;
    }

    private static void bang() { throw new TestThreadActionException() ; } 

    static class TestThreadActionException extends RuntimeException {}
}
