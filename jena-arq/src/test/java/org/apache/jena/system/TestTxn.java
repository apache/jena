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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Transactional;

public class TestTxn {

    TxnCounter counter = new TxnCounter(0) ;

    @Test
    public void txn_basic_01() {
        long v1 = counter.get();
        assertEquals(0, v1);
        Txn.executeRead(counter, () -> {
            assertEquals(0, counter.get());
        });
    }

    @Test
    public void txn_basic_02() {
        long x = Txn.calculateRead(counter, () -> {
            assertEquals(0, counter.value(), () -> "In R, value()");
            assertEquals(0, counter.get(), () -> "In R, get()");
            return counter.get();
        });
        assertEquals(0, x, () -> "Outside R");
    }

    @Test
    public void txn_basic_03() {
        Txn.executeWrite(counter, counter::inc);
        long x = Txn.calculateRead(counter, () -> {
            assertEquals(1, counter.value(), () -> "In R, value()");
            assertEquals(1, counter.get(), () -> "In R, get()");
            return counter.get();
        });
        assertEquals(1, x, () -> "Outside R");
    }

    @Test
    public void txn_basic_05() {
        long x = Txn.calculateWrite(counter, () -> {
            counter.inc();
            assertEquals(0, counter.value(), () -> "In W, value()");
            assertEquals(1, counter.get(), () -> "In W, get()");
            return counter.get();
        });
        assertEquals(1, x, () -> "Outside W");
    }

    @Test
    public void txn_write_01() {
        long x = Txn.calculateWrite(counter, () -> {
            counter.inc();
            assertEquals(0, counter.value(), () -> "In W, value()");
            assertEquals(1, counter.get(), () -> "In W, get()");
            long z = counter.get();
            counter.commit();
            return z;
        });
        assertEquals(1, x, () -> "Outside W");
    }

    @Test
    public void txn_write_02() {
        long x = Txn.calculateWrite(counter, () -> {
            counter.inc();
            assertEquals(0, counter.value(), () -> "In W, value()");
            assertEquals(1, counter.get(), () -> "In W, get()");
            long z = counter.get();
            counter.abort();
            return z;
        });
        assertEquals(1, x, () -> "Outside W");
    }

    @Test
    public void txn_write_03() {
        Txn.executeWrite(counter, () -> {
            counter.inc();
            assertEquals(0, counter.value(), () -> "In W, value()");
            assertEquals(1, counter.get(), () -> "In W, get()");
            counter.commit();
        });
        assertEquals(1, counter.value(), () -> "Outside W");
    }

    @Test
    public void txn_write_04() {
        Txn.executeWrite(counter, () -> {
            counter.inc();
            assertEquals(0, counter.value(), () -> "In W, value()");
            assertEquals(1, counter.get(), () -> "In W, get()");
            counter.abort();
        });
        assertEquals(0, counter.value(), () -> "Outside W");
    }

    @Test
    public void txn_rw_1() {
        assertEquals(0, counter.get());

        Txn.executeWrite(counter, () -> {
            counter.inc();
            assertEquals(0, counter.value(), () -> "In W, value()");
            assertEquals(1, counter.get(), () -> "In W, get()");
        });

        assertEquals(1, counter.value(), () -> "Direct value()");
        assertEquals(1, counter.get(), () -> "Direct get()");

        Txn.executeRead(counter, () -> {
            assertEquals(1, counter.value(), () -> "In R, value()");
            assertEquals(1, counter.get(), () -> "In R, get()");
        });
    }

    @Test
    public void txn_rw_2() {
        Txn.executeRead(counter, () -> {
            assertEquals(0, counter.value(), () -> "In R, value()");
            assertEquals(0, counter.get(), () -> "In R, get()");
        });

        Txn.executeWrite(counter, () -> {
            counter.inc();
            assertEquals(0, counter.value(), () -> "In W, value()");
            assertEquals(1, counter.get(), () -> "In W, get()");
        });

        assertEquals(1, counter.get(), () -> "Direct value()");
        assertEquals(1, counter.get(), () -> "Direct get()");

        Txn.executeRead(counter, () -> {
            assertEquals(1, counter.value(), () -> "In R, value()");
            assertEquals(1, counter.get(), () -> "In R, get()");
        });
    }

    @Test
    public void txn_continue_1() {
        Txn.executeWrite(counter, () -> counter.set(91));

        Txn.executeWrite(counter, () -> {
            assertEquals(91, counter.value(), () -> "In txn, value()");
            assertEquals(91, counter.read(), () -> "In txn, read()");
            counter.inc();
            Txn.executeWrite(counter, () -> {
                assertEquals(91, counter.value(), () -> "In txn, value()");
                assertEquals(92, counter.read(), () -> "In txn, get()");
            });
        });
        assertEquals(92, counter.value());
    }

    @Test
    public void txn_continue_2() {
        Txn.executeWrite(counter, () -> counter.set(91));

        Txn.executeWrite(counter, () -> {
            assertEquals(91, counter.value(), () -> "In txn, value()");
            assertEquals(91, counter.read(), () -> "In txn, read()");
            counter.inc();
            Txn.executeWrite(counter, () -> {
                assertEquals(91, counter.value(), () -> "In txn, value()");
                assertEquals(92, counter.read(), () -> "In txn, get()");
                counter.inc();
            });
            assertEquals(91, counter.value(), () -> "In txn, value()");
            assertEquals(93, counter.read(), () -> "In txn, read()");
            counter.inc();
        });
        assertEquals(94, counter.value());
    }

    @Test
    public void txn_exception_01() {
        Txn.executeWrite(counter, counter::inc);

        assertThrows(ExceptionFromTest.class, () -> {
            Txn.executeWrite(counter, () -> {
                counter.inc();
                assertEquals(1, counter.value(), () -> "In W, value()");
                assertEquals(2, counter.get(), () -> "In W, get()");
                throw new ExceptionFromTest();
            });
        });
    }

    @Test
    public void txn_exception_02() {
        Txn.executeWrite(counter, () -> counter.set(8));

        try {
            Txn.executeWrite(counter, () -> {
                counter.inc();
                assertEquals(8, counter.value(), () -> "In W, value()");
                assertEquals(9, counter.get(), () -> "In W, get()");
                throw new ExceptionFromTest();
            });
        } catch (ExceptionFromTest ex) {}
        assertEquals(8, counter.get(), () -> "After W/abort, get()");
    }

    @Test
    public void txn_exception_03() {
        Txn.executeWrite(counter, () -> counter.set(9));

        try {
            Txn.executeRead(counter, () -> {
                assertEquals(9, counter.value(), () -> "In W, value()");
                assertEquals(9, counter.get(), () -> "In W, get()");
                throw new ExceptionFromTest();
            });
        } catch (ExceptionFromTest ex) {}
        assertEquals(9, counter.get(), () -> "After W/abort, get()");
    }

    @Test
    public void txn_nested_01() {
        Txn.exec(counter, TxnType.READ, () -> {
            Txn.exec(counter, TxnType.READ, () -> {});
        });
    }

    @Test
    public void txn_nested_02() {
        Txn.exec(counter, TxnType.WRITE, () -> {
            Txn.exec(counter, TxnType.READ, () -> {});
        });
    }

    @Test
    public void txn_nested_03() {
        Txn.exec(counter, TxnType.READ_PROMOTE, () -> {
            Txn.exec(counter, TxnType.READ, () -> {});
        });
    }

    @Test
    public void txn_nested_04() {
        Txn.exec(counter, TxnType.READ_COMMITTED_PROMOTE, () -> {
            Txn.exec(counter, TxnType.READ, () -> {});
        });
    }

    @Test
    public void txn_nested_05() {
        Txn.exec(counter, TxnType.READ, () -> {
            assertThrows(JenaTransactionException.class, () -> {
                Txn.exec(counter, TxnType.WRITE, () -> {});
            });
        });
    }

    @Test
    public void txn_nested_06() {
        Txn.exec(counter, TxnType.READ_PROMOTE, () -> {
            boolean b = counter.promote();
            assertTrue(b);
            // Must the same type to nest Txn.
            Txn.exec(counter, TxnType.READ_PROMOTE, () -> {});
        });
    }

    @Test
    public void txn_nested_07() {
        Txn.exec(counter, TxnType.READ_COMMITTED_PROMOTE, () -> {
            boolean b = counter.promote();
            assertTrue(b);
            // Must the same type to nest Txn.
            Txn.exec(counter, TxnType.READ_COMMITTED_PROMOTE, () -> {});
        });
    }

    @Test
    public void txn_nested_08() {
        Txn.exec(counter, TxnType.READ_COMMITTED_PROMOTE, () -> {
            boolean b = counter.promote();
            assertTrue(b);
            assertEquals(ReadWrite.WRITE, counter.transactionMode());
            // Must the same type to nest Txn.
            Txn.exec(counter, TxnType.READ_COMMITTED_PROMOTE, () -> {});
        });
    }

    @Test
    public void txn_nested_09() {
        Txn.exec(counter, TxnType.READ_PROMOTE, () -> {
            boolean b = counter.promote();
            assertTrue(b);
            assertEquals(ReadWrite.WRITE, counter.transactionMode());
            // Must the same type to nest Txn.
            Txn.exec(counter, TxnType.READ_PROMOTE, () -> {});
        });
    }

    @Test
    public void txn_nested_10() {
        Txn.exec(counter, TxnType.READ_PROMOTE, () -> {
            // Can promote outer.
            Txn.exec(counter, TxnType.WRITE, () -> {});
        });
    }

    @Test
    public void txn_nested_11() {
        Txn.exec(counter, TxnType.READ_COMMITTED_PROMOTE, () -> {
            // Can promote outer.
            Txn.exec(counter, TxnType.WRITE, () -> {});
        });
    }

    @Test
    public void txn_nested_12() {
        Txn.exec(counter, TxnType.READ_PROMOTE, () -> {
            // Block promote by doing a W txn on another thread.
            ThreadAction.create(() -> Txn.executeWrite(counter, () -> counter.inc())).run();
            assertThrows(JenaTransactionException.class, () -> {
                // Can not promote outer.
                Txn.exec(counter, TxnType.WRITE, () -> {});
            });
        });
    }

    @Test
    public void txn_nested_13() {
        Txn.exec(counter, TxnType.READ_COMMITTED_PROMOTE, () -> {
            // Does not block promote
            ThreadAction.create(() -> Txn.executeWrite(counter, () -> counter.inc())).run();
            // Can promote outer.
            Txn.exec(counter, TxnType.WRITE, () -> {});
        });
    }

    @Test
    public void txn_threaded_01() {
        Txn.exec(counter, TxnType.READ_PROMOTE, () -> {
            ThreadAction a = ThreadTxn.threadTxnWrite(counter, () -> {});
            a.run();
            // Blocks promotion.
            boolean b = counter.promote();
            assertFalse(b);
            assertEquals(ReadWrite.READ, counter.transactionMode());
        });
    }

//    // This would lock up.
//    public void txn_threaded_Not_A_Test() {
//        Txn.exec(counter, TxnType.READ_PROMOTE, () -> {
//            ThreadAction a = ThreadTxn.threadTxnWrite(counter, () -> {});
//            // a is in a W transaction but has not committed or aborted - it's
//            // paused.
//            boolean b = counter.promote();
//            // Never reach here.
//            a.run();
//        });
//    }

    @Test
    public void txn_threaded_02() {
        // Transactional tx = DatasetGraphFactory.createTxnMem();
        Transactional tx = counter;

        // Start and enter the W transaction.
        ThreadAction a = ThreadTxn.threadTxnWrite(tx, () -> {});

        // ThreadAction started ... in W transaction.
        Txn.exec(tx, TxnType.READ_PROMOTE, () -> {
            // ... have the thread action complete.
            a.run();
            // Blocks promotion.
            boolean b = tx.promote();
            assertFalse(b);
            assertEquals(ReadWrite.READ, tx.transactionMode());
        });
    }

    @Test
    public void txn_threaded_03() {
        Transactional tx = DatasetGraphFactory.createTxnMem();
        // Transactional tx = counter;

        // Start and enter the W transaction.
        ThreadAction a = ThreadTxn.threadTxnWriteAbort(tx, () -> {});

        // ThreadAction started ... in W transaction.
        Txn.exec(tx, TxnType.READ_PROMOTE, () -> {
            // ... have the thread action abort..
            a.run();
            // Does not block promotion.
            boolean b = tx.promote();
            assertTrue(b);
            assertEquals(ReadWrite.WRITE, tx.transactionMode());
        });
    }

    @Test
    public void autoTxn_write_01() {
        long actualValue;
        try (AutoTxn txn = Txn.autoTxn(counter, TxnType.WRITE)) {
            counter.inc();
            assertEquals(0, counter.value(), () -> "In W, value()");
            assertEquals(1, counter.get(), () -> "In W, get()");
            actualValue = counter.get();
            counter.commit();
        }
        long expectedValue = counter.get();
        assertEquals(expectedValue, actualValue, () -> "Outside W");
    }

    @Test
    public void autoTxn_write_02() {
        long expectedValue = counter.get();
        try (AutoTxn txn = Txn.autoTxn(counter, TxnType.WRITE)) {
            counter.inc();
            assertEquals(0, counter.value(), () -> "In W, value()");
            assertEquals(1, counter.get(), () -> "In W, get()");
            // Intermediate value will be reverted.
            long intermediateValue = counter.get();
            assertNotEquals(expectedValue, intermediateValue);
            // no commit - auto-close is expected to abort.
        }
        long actualValue = counter.get();
        assertEquals(expectedValue, actualValue, () -> "Outside W");
    }
}

