/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.transaction;

import static org.junit.Assert.fail ;
import org.junit.Test ;
import org.seaborne.dboe.transaction.txn.TransactionException ;

import org.apache.jena.query.ReadWrite ;

/**
 * Tests of transaction lifecycle in one JVM.
 * Journal independent.
 * Not testing recovery or writing to the journal. 
 */
public class TestTransactionLifecycle extends AbstractTestTxn {
    @Test public void txn_read_end() {
        unit.begin(ReadWrite.READ);
        unit.end() ;
        checkClear() ;
    }

    @Test public void txn_read_end_end() {
        unit.begin(ReadWrite.READ);
        unit.end() ;
        unit.end() ;
        checkClear() ;
    }

    @Test public void txn_read_abort() {
        unit.begin(ReadWrite.READ);
        unit.abort() ;
        checkClear() ;
    }

    @Test public void txn_read_commit() {
        unit.begin(ReadWrite.READ);
        unit.commit() ;
        checkClear() ;
    }

    @Test public void txn_read_abort_end() {
        unit.begin(ReadWrite.READ);
        unit.abort() ;
        unit.end() ;
        checkClear() ;
    }

    @Test public void txn_read_commit_end() {
        unit.begin(ReadWrite.READ);
        unit.commit() ;
        unit.end() ;
        checkClear() ;
    }
    
    @Test public void txn_read_commit_abort() {
        unit.begin(ReadWrite.READ);
        unit.commit() ;
        try { unit.abort() ; fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end() ;
        checkClear() ;
    }
    
    @Test public void txn_read_commit_commit() {
        unit.begin(ReadWrite.READ);
        unit.commit() ;
        try { unit.commit() ; fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end() ;
        checkClear() ;
    }

    @Test public void txn_read_abort_commit() {
        unit.begin(ReadWrite.READ);
        unit.abort() ;
        try { unit.commit() ; fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end() ;
        checkClear() ;
    }

    @Test public void txn_read_abort_abort() {
        unit.begin(ReadWrite.READ);
        unit.abort() ;
        try { unit.abort() ; fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end() ;
        checkClear() ;
    }

    @Test(expected=TransactionException.class)
    public void txn_begin_read_begin_read() {
        unit.begin(ReadWrite.READ);
        unit.begin(ReadWrite.READ);
    }

    @Test(expected=TransactionException.class)
    public void txn_begin_read_begin_write() {
        unit.begin(ReadWrite.READ);
        unit.begin(ReadWrite.WRITE);
    }

    @Test(expected=TransactionException.class)
    public void txn_begin_write_begin_read() {
        unit.begin(ReadWrite.WRITE);
        unit.begin(ReadWrite.READ);
    }

    @Test(expected=TransactionException.class)
    public void txn_begin_write_begin_write() {
        unit.begin(ReadWrite.WRITE);
        unit.begin(ReadWrite.WRITE);
    }

    @Test(expected=TransactionException.class) 
    public void txn_write_begin_end() {
        unit.begin(ReadWrite.WRITE);
        unit.end() ;
        checkClear() ;
    }
    
    @Test public void txn_write_abort() {
        unit.begin(ReadWrite.WRITE);
        unit.abort() ;
        checkClear() ;
    }

    @Test public void txn_write_commit() {
        unit.begin(ReadWrite.WRITE);
        unit.commit() ;
        checkClear() ;
    }

    @Test public void txn_write_abort_end() {
        unit.begin(ReadWrite.WRITE);
        unit.abort() ;
        unit.end() ;
        checkClear() ;
    }

    @Test public void txn_write_abort_end_end() {
        unit.begin(ReadWrite.WRITE);
        unit.abort() ;
        unit.end() ;
        unit.end() ;
        checkClear() ;
    }
    
    @Test public void txn_write_commit_end() {
        unit.begin(ReadWrite.WRITE);
        unit.commit() ;
        unit.end() ;
        checkClear() ;
    }
    
    @Test public void txn_write_commit_end_end() {
        unit.begin(ReadWrite.WRITE);
        unit.commit() ;
        unit.end() ;
        unit.end() ;
        checkClear() ;
    }

    @Test public void txn_write_commit_abort() {
        // commit-abort
        unit.begin(ReadWrite.WRITE);
        unit.commit() ;
        try { unit.abort() ; fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end() ;
        checkClear() ;
    }
    
    @Test public void txn_write_commit_commit() {
        // commit-commit
        unit.begin(ReadWrite.WRITE);
        unit.commit() ;
        try { unit.commit() ; fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end() ;
        checkClear() ;
    }

    @Test public void txn_write_abort_commit() {
        // abort-commit
        unit.begin(ReadWrite.WRITE);
        unit.abort() ;
        try { unit.commit() ; fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end() ;
        checkClear() ;
    }

    @Test public void txn_write_abort_abort() {
        // abort-abort
        unit.begin(ReadWrite.WRITE);
        unit.abort() ;
        try { unit.abort() ; fail() ; }
        catch (TransactionException ex) { /* Expected : can continue */ }
        unit.end() ;
        checkClear() ;
    }
    
    private void read() {
        unit.begin(ReadWrite.READ);
        unit.end() ;
        checkClear() ;
    }
    
    private void write() {
        unit.begin(ReadWrite.WRITE);
        unit.commit() ;
        unit.end() ;
        checkClear() ;
    }

    @Test public void txn_read_read() {
        read() ; 
        read() ;
    }

    @Test public void txn_write_read() {
        write() ; 
        read() ;
    }

    @Test public void txn_read_write() {
        read() ;
        write() ; 
    }

    @Test public void txn_write_write() {
        write() ; 
        write() ; 
    }

    @Test public void txn_www() {
        write() ; 
        write() ; 
        write() ;
        checkClear();
    }

}

