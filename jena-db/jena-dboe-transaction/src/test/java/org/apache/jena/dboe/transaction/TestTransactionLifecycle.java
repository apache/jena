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

import static org.junit.Assert.*;
import org.junit.Test;
import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;

/**
 * Tests of transaction lifecycle in one JVM.
 * Not testing recovery or writing to the journal.
 * Tests on a "unit", not directly on the TransactionCoordinator.
 */
public class TestTransactionLifecycle extends AbstractTestTxn {

    @Test public void txn_read_end_RW() {
        unit.begin(ReadWrite.READ);
        unit.end();
        checkClear();
    }

    @Test public void txn_read_end() {
        unit.begin(TxnType.READ);
        unit.end();
        checkClear();
    }

    @Test public void txn_read_end_end() {
        unit.begin(TxnType.READ);
        unit.end();
        unit.end();
        checkClear();
    }

    @Test public void txn_read_abort() {
        unit.begin(TxnType.READ);
        unit.abort();
        checkClear();
    }

    @Test public void txn_read_commit() {
        unit.begin(TxnType.READ);
        unit.commit();
        checkClear();
    }

    @Test public void txn_read_abort_end() {
        unit.begin(TxnType.READ);
        unit.abort();
        unit.end();
        checkClear();
    }

    @Test public void txn_read_commit_end() {
        unit.begin(TxnType.READ);
        unit.commit();
        unit.end();
        checkClear();
    }

    @Test public void txn_read_commit_abort() {
        unit.begin(TxnType.READ);
        unit.commit();
        try { unit.abort(); fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end();
        checkClear();
    }

    @Test public void txn_read_commit_commit() {
        unit.begin(TxnType.READ);
        unit.commit();
        try { unit.commit(); fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end();
        checkClear();
    }

    @Test public void txn_read_abort_commit() {
        unit.begin(TxnType.READ);
        unit.abort();
        try { unit.commit(); fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end();
        checkClear();
    }

    @Test public void txn_read_abort_abort() {
        unit.begin(TxnType.READ);
        unit.abort();
        try { unit.abort(); fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end();
        checkClear();
    }

    @Test(expected=TransactionException.class)
    public void txn_begin_read_begin_read() {
        unit.begin(TxnType.READ);
        unit.begin(TxnType.READ);
    }

    @Test(expected=TransactionException.class)
    public void txn_begin_read_begin_write() {
        unit.begin(TxnType.READ);
        unit.begin(TxnType.WRITE);
    }

    @Test(expected=TransactionException.class)
    public void txn_begin_write_begin_read() {
        unit.begin(TxnType.WRITE);
        unit.begin(TxnType.READ);
    }

    @Test(expected=TransactionException.class)
    public void txn_begin_write_begin_write() {
        unit.begin(TxnType.WRITE);
        unit.begin(TxnType.WRITE);
    }

    @Test(expected=TransactionException.class)
    public void txn_write_begin_end() {
        unit.begin(TxnType.WRITE);
        unit.end();
        checkClear();
    }

    @Test public void txn_write_abort() {
        unit.begin(TxnType.WRITE);
        unit.abort();
        checkClear();
    }

    @Test public void txn_write_commit() {
        unit.begin(TxnType.WRITE);
        unit.commit();
        checkClear();
    }

    @Test public void txn_write_abort_end() {
        unit.begin(TxnType.WRITE);
        unit.abort();
        unit.end();
        checkClear();
    }

    @Test public void txn_write_abort_end_end() {
        unit.begin(TxnType.WRITE);
        unit.abort();
        unit.end();
        unit.end();
        checkClear();
    }

    @Test public void txn_write_commit_end_RW() {
        unit.begin(ReadWrite.WRITE);
        unit.commit();
        unit.end();
        checkClear();
    }

    @Test public void txn_write_commit_end() {
        unit.begin(TxnType.WRITE);
        unit.commit();
        unit.end();
        checkClear();
    }

    @Test public void txn_write_commit_end_end() {
        unit.begin(TxnType.WRITE);
        unit.commit();
        unit.end();
        unit.end();
        checkClear();
    }

    @Test public void txn_write_commit_abort() {
        // commit-abort
        unit.begin(TxnType.WRITE);
        unit.commit();
        try { unit.abort(); fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end();
        checkClear();
    }

    @Test public void txn_write_commit_commit() {
        // commit-commit
        unit.begin(TxnType.WRITE);
        unit.commit();
        try { unit.commit(); fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end();
        checkClear();
    }

    @Test public void txn_write_abort_commit() {
        // abort-commit
        unit.begin(TxnType.WRITE);
        unit.abort();
        try { unit.commit(); fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end();
        checkClear();
    }

    @Test public void txn_write_abort_abort() {
        // abort-abort
        unit.begin(TxnType.WRITE);
        unit.abort();
        try { unit.abort(); fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end();
        checkClear();
    }

    @Test public void txn_read_promote_commit() {
        unit.begin(TxnType.READ);
        boolean b = unit.promote();
        assertFalse(b);
        unit.end();
        checkClear();
    }

    @Test public void txn_readpromote_promote_commit() {
        unit.begin(TxnType.READ_PROMOTE);
        boolean b = unit.promote();
        assertTrue(b);
        unit.commit();
        unit.end();
        checkClear();
    }

    @Test public void txn_readpromote_promote_promote_commit() {
        unit.begin(TxnType.READ_PROMOTE);
        boolean b = unit.promote();
        assertTrue(b);
        unit.commit();
        unit.end();
        checkClear();
    }

    @Test public void txn_readpromote_promote_abort() {
        unit.begin(TxnType.READ_PROMOTE);
        boolean b = unit.promote();
        assertTrue(b);
        unit.abort();
        unit.end();
        checkClear();
    }

    @Test public void txn_readcomittedpromote_promote_commit() {
        unit.begin(TxnType.READ_COMMITTED_PROMOTE);
        boolean b = unit.promote();
        assertTrue(b);
        unit.commit();
        unit.end();
        checkClear();
    }

    @Test public void txn_readcomittedpromote_promote_abort() {
        unit.begin(TxnType.READ_COMMITTED_PROMOTE);
        boolean b = unit.promote();
        assertTrue(b);
        unit.abort();
        unit.end();
        checkClear();
    }

    @Test public void txn_readpromote_promote_end() {
        unit.begin(TxnType.READ_PROMOTE);
        boolean b = unit.promote();
        assertTrue(b);
        try {  unit.end(); }
        catch (TransactionException ex) { /* Expected : check clearup */ }
        checkClear();
    }

    @Test public void txn_readcomittedpromote_promote_end() {
        unit.begin(TxnType.READ_COMMITTED_PROMOTE);
        boolean b = unit.promote();
        assertTrue(b);
        try {  unit.end(); }
        catch (TransactionException ex) { /* Expected : check clearup */ }
        checkClear();
    }

    @Test public void txn_readpromote_commit_promote() {
        unit.begin(TxnType.READ_PROMOTE);
        unit.commit(); // READ commit.
        try { unit.promote(); }
        catch (TransactionException ex) { /* Expected : check clearup */ }
        checkClear();
    }

    @Test public void txn_readpromote_abort_promote() {
        unit.begin(TxnType.READ_PROMOTE);
        unit.abort(); // READ abort.
        try { unit.promote(); }
        catch (TransactionException ex) { /* Expected : check clearup */ }
        checkClear();
    }

    @Test public void txn_readpromote_end_promote() {
        unit.begin(TxnType.READ_PROMOTE);
        unit.end(); // READ end
        try { unit.promote(); }
        catch (TransactionException ex) { /* Expected : check clearup */ }
        checkClear();
    }

    private void read() {
        unit.begin(TxnType.READ);
        unit.end();
        checkClear();
    }

    private void write() {
        unit.begin(TxnType.WRITE);
        unit.commit();
        unit.end();
        checkClear();
    }

    @Test public void txn_read_read() {
        read();
        read();
    }

    @Test public void txn_write_read() {
        write();
        read();
    }

    @Test public void txn_read_write() {
        read();
        write();
    }

    @Test public void txn_write_write() {
        write();
        write();
    }

    @Test public void txn_www() {
        write();
        write();
        write();
        checkClear();
    }

}

