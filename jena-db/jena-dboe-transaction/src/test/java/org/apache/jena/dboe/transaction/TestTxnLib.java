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

package org.apache.jena.dboe.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import org.apache.jena.system.ThreadTxn;
import org.apache.jena.system.ThreadAction;
import org.apache.jena.system.Txn;
import org.apache.jena.query.ReadWrite;
import org.junit.Test;

/** Txn with DBOE transactions */
public class TestTxnLib extends AbstractTestTxn {

    @Test public void libTxn_1() {
        long v1 = counter1.value();
        long v2 = counter2.value();
        assertEquals(0, v1);
        assertEquals(0, v2);

        Txn.executeRead(unit, () -> {
            assertEquals(0, counter1.get());
            assertEquals(0, counter2.get());
        });
    }

    @Test public void libTxn_2() {
        assertEquals(0, counter1.value());

        Txn.executeWrite(unit, () -> {
            counter1.inc();
            assertEquals("In W, value()", 0, counter1.value());
            assertEquals("In W, get()",1, counter1.get());
        });

        assertEquals("Direct value()", 1, counter1.value());
        assertEquals("Direct get()", 1, counter1.get());

        Txn.executeRead(unit, () -> {
            assertEquals("In R, value()", 1, counter1.get());
            assertEquals("In R, get()", 1, counter1.value());
        });
    }

    @Test public void libTxn_3() {
        Txn.executeRead(unit, () -> {
            assertEquals("In R, value()", 0, counter2.get());
            assertEquals("In R, get()", 0, counter2.value());
        });

        Txn.executeWrite(unit, () -> {
            counter2.inc();
            assertEquals("In W, value()", 0, counter2.value());
            assertEquals("In W, get()",1, counter2.get());
        });

        assertEquals("Direct value()", 1, counter2.value());
        assertEquals("Direct get()", 1, counter2.get());

        Txn.executeRead(unit, () -> {
            assertEquals("In R, value()", 1, counter2.get());
            assertEquals("In R, get()", 1, counter2.value());
        });
    }

    @Test public void libTxn_4() {
        long v1 = counter1.value();
        long v2 = counter2.value();
        assertEquals(0, v1);
        assertEquals(0, v2);

        //Txn.execWrite(unit, () -> {

        unit.begin(ReadWrite.WRITE);
            counter1.inc();
            counter2.inc();
            assertEquals("Counter out of step", counter1.get(), counter2.get());
            assertNotEquals("Counter 1 can see wrong state", counter1.get(), counter1.value() );
            assertNotEquals("Counter 2 can see wrong state", counter2.get(), counter2.value() );
            counter2.inc();
            assertNotEquals("Counter 1 and 2 should differ", counter1.get(), counter2.get() );
        unit.commit();
        unit.end();
        //});
        assertEquals("Component 1 inconsistent", 1, counter1.value());
        assertEquals("Component 2 inconsistent", 2, counter2.value());

        Txn.executeRead(unit, () -> {
            assertEquals("Component 1 inconsistent (R)", 1, counter1.get());
            assertEquals("Component 2 inconsistent (R)", 2, counter2.get());
        });
    }

    @Test public void libTxn_5() {
        long x =
            Txn.calculateRead(unit, () -> {
                assertEquals("In R, value()", 0, counter2.get());
                assertEquals("In R, get()", 0, counter2.value());
                return counter2.get();
            });
        assertEquals("Outside R", 0, x);
    }

    @Test public void libTxn_6() {
        long x =
            Txn.calculateWrite(unit, () -> {
                counter2.inc();
                assertEquals("In W, value()", 0, counter2.value());
                assertEquals("In W, get()",1, counter2.get());
                return counter2.get();
            });
        assertEquals("Outside W",1, x);
    }

    @Test public void libTxn_7() {
        long x1 =
            Txn.calculateWrite(unit, () -> {
                counter2.inc();
                counter2.inc();
                return counter2.get();
            });
        long x2 = Txn.calculateRead(unit, () -> {
            return counter2.get();
        });
        assertEquals("After W and R",x1 , x2);
    }

    // Tests for thread transactions.

    @Test public void libTxnThread_1() {
        ThreadAction t = ThreadTxn.threadTxnRead(unit, ()->{});
        t.run();
    }

    @Test public void libTxnThread_2() {
        ThreadAction t = ThreadTxn.threadTxnWrite(unit, ()-> fail(""));
    }

    @Test(expected=AssertionError.class)
    public void libTxnThread_3() {
        ThreadAction t = ThreadTxn.threadTxnWrite(unit, ()-> fail(""));
        t.run();
    }

    @Test public void libTxnThread_10() {
        long x1 = counter1.get();
        ThreadAction t = ThreadTxn.threadTxnWrite(unit, ()->{ counter1.inc();}) ;
        long x2 = counter1.get();
        assertEquals("x2", x1, x2);
        t.run();
        long x3 = counter1.get();
        assertEquals("x3", x1+1, x3);
    }

    @Test public void libTxnThread_11() {
        long x1 = counter1.get();
        Txn.executeWrite(unit, ()->{
            counter1.inc();
            // Read the "before" state
            ThreadAction t = ThreadTxn.threadTxnRead(unit, ()->{ long z1 = counter1.get(); assertEquals("Thread read", x1, z1) ; }) ;
            counter1.inc();
            t.run();
        });
        long x2 = counter1.get();
        assertEquals("after", x1+2, x2);
    }

    @Test public void libTxnThread_12() {
        long x1 = counter1.get();
        ThreadAction t = ThreadTxn.threadTxnRead(unit, () -> {
            long z1 = counter1.get();
            assertEquals("Thread", x1, z1);
        });
        Txn.executeWrite(unit, ()->counter1.inc());
        t.run();
        long x2 = counter1.get();
        assertEquals("after::", x1+1, x2);
    }
}
