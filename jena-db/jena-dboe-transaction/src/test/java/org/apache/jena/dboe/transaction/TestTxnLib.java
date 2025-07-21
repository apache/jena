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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.system.ThreadAction;
import org.apache.jena.system.ThreadTxn;
import org.apache.jena.system.Txn;
import org.opentest4j.AssertionFailedError;

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
            assertEquals(0, counter1.value(), "In W, value()");
            assertEquals(1,counter1.get(), "In W, get()");
        });

        assertEquals(1, counter1.value(), "Direct value()");
        assertEquals(1, counter1.get(), "Direct get()");

        Txn.executeRead(unit, () -> {
            assertEquals(1, counter1.get(), "In R, value()");
            assertEquals(1, counter1.value(), "In R, get()");
        });
    }

    @Test public void libTxn_3() {
        Txn.executeRead(unit, () -> {
            assertEquals(0, counter2.get(), "In R, value()");
            assertEquals(0, counter2.value(), "In R, get()");
        });

        Txn.executeWrite(unit, () -> {
            counter2.inc();
            assertEquals(0, counter2.value(), "In W, value()");
            assertEquals(1,counter2.get(), "In W, get()");
        });

        assertEquals(1, counter2.value(), "Direct value()");
        assertEquals(1, counter2.get(), "Direct get()");

        Txn.executeRead(unit, () -> {
            assertEquals(1, counter2.get(), "In R, value()");
            assertEquals(1, counter2.value(), "In R, get()");
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
            assertEquals(counter1.get(), counter2.get(), "Counter out of step");
            assertNotEquals(counter1.get(), counter1.value(), "Counter 1 can see wrong state" );
            assertNotEquals(counter2.get(), counter2.value(), "Counter 2 can see wrong state" );
            counter2.inc();
            assertNotEquals(counter1.get(), counter2.get(), "Counter 1 and 2 should differ" );
        unit.commit();
        unit.end();
        //});
        assertEquals(1, counter1.value(), "Component 1 inconsistent");
        assertEquals(2, counter2.value(), "Component 2 inconsistent");

        Txn.executeRead(unit, () -> {
            assertEquals(1, counter1.get(), "Component 1 inconsistent (R)");
            assertEquals(2, counter2.get(), "Component 2 inconsistent (R)");
        });
    }

    @Test public void libTxn_5() {
        long x =
            Txn.calculateRead(unit, () -> {
                assertEquals(0, counter2.get(), "In R, value()");
                assertEquals(0, counter2.value(), "In R, get()");
                return counter2.get();
            });
        assertEquals(0, x, "Outside R");
    }

    @Test public void libTxn_6() {
        long x =
            Txn.calculateWrite(unit, () -> {
                counter2.inc();
                assertEquals(0, counter2.value(), "In W, value()");
                assertEquals(1,counter2.get(), "In W, get()");
                return counter2.get();
            });
        assertEquals(1,x, "Outside W");
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
        assertEquals(x1,x2 , "After W and R");
    }

    // Tests for thread transactions.

    @Test public void libTxnThread_1() {
        ThreadAction t = ThreadTxn.threadTxnRead(unit, ()->{});
        t.run();
    }

    @Test public void libTxnThread_2() {
        ThreadAction t = ThreadTxn.threadTxnWrite(unit, ()-> fail(""));
    }

    @Test
    public void libTxnThread_3() {
        ThreadAction t = ThreadTxn.threadTxnWrite(unit, ()-> fail(""));
        assertThrows(AssertionFailedError.class, ()->t.run());
    }

    @Test public void libTxnThread_10() {
        long x1 = counter1.get();
        ThreadAction t = ThreadTxn.threadTxnWrite(unit, ()->{ counter1.inc();}) ;
        long x2 = counter1.get();
        assertEquals(x1, x2, "x2");
        t.run();
        long x3 = counter1.get();
        assertEquals(x1+1, x3, "x3");
    }

    @Test public void libTxnThread_11() {
        long x1 = counter1.get();
        Txn.executeWrite(unit, ()->{
            counter1.inc();
            // Read the "before" state
            ThreadAction t = ThreadTxn.threadTxnRead(unit, ()->{ long z1 = counter1.get(); assertEquals(x1, z1, "Thread read") ; }) ;
            counter1.inc();
            t.run();
        });
        long x2 = counter1.get();
        assertEquals(x1+2, x2, "after");
    }

    @Test public void libTxnThread_12() {
        long x1 = counter1.get();
        ThreadAction t = ThreadTxn.threadTxnRead(unit, () -> {
            long z1 = counter1.get();
            assertEquals(x1, z1, "Thread");
        });
        Txn.executeWrite(unit, ()->counter1.inc());
        t.run();
        long x2 = counter1.get();
        assertEquals(x1+1, x2, "after::");
    }
}
