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

package com.hp.hpl.jena.sparql.transaction;

import static com.hp.hpl.jena.query.ReadWrite.READ ;
import static com.hp.hpl.jena.query.ReadWrite.WRITE ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.JenaTransactionException ;

public abstract class AbstractTestTransaction extends BaseTest
{
    protected abstract Dataset create() ;
    
    @Test
    public void transaction_err_00() {
        Dataset ds = create() ;
        assertTrue(ds.supportsTransactions()) ;
    }

    @Test
    public void transaction_01() {
        Dataset ds = create() ;
        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ;
        ds.end() ;
        assertFalse(ds.isInTransaction()) ;
    }

    @Test
    public void transaction_02() {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ;
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
    }

    @Test
    public void transaction_03() {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ;
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ;
    }

    @Test
    public void transaction_04() {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ;
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
        assertFalse(ds.isInTransaction()) ;
    }

    @Test
    public void transaction_05() {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ;
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
        assertFalse(ds.isInTransaction()) ;
    }

    @Test
    public void transaction_06() {
        // .end is not necessary
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ;
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ;

        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ;
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ;
    }

    // Patterns.
    @Test
    public void transaction_07() {
        Dataset ds = create() ;
        read1(ds) ;
        read1(ds) ;
    }

    @Test
    public void transaction_08() {
        Dataset ds = create() ;
        read2(ds) ;
        read2(ds) ;
    }

    @Test
    public void transaction_09() {
        Dataset ds = create() ;
        write(ds) ;
        write(ds) ;
    }

    @Test
    public void transaction_10() {
        Dataset ds = create() ;
        write(ds) ;
        read2(ds) ;
        read2(ds) ;
        write(ds) ;
        read2(ds) ;
    }

    @Test
    public void transaction_err_01()    { testBeginBegin(WRITE, WRITE) ; }

    @Test
    public void transaction_err_02()    { testBeginBegin(WRITE, READ) ; }

    @Test
    public void transaction_err_03()    { testBeginBegin(READ, READ) ; }

    @Test
    public void transaction_err_04()    { testBeginBegin(READ, WRITE) ; }

    @Test 
    public void transaction_err_05()    { testCommitCommit(READ) ; }

    @Test 
    public void transaction_err_06()    { testCommitCommit(WRITE) ; }

    @Test 
    public void transaction_err_07()    { testCommitAbort(READ) ; }

    @Test 
    public void transaction_err_08()    { testCommitAbort(WRITE) ; }

    @Test 
    public void transaction_err_09()    { testAbortAbort(READ) ; }

    @Test 
    public void transaction_err_10()    { testAbortAbort(WRITE) ; }

    @Test 
    public void transaction_err_11()    { testAbortCommit(READ) ; }

    @Test 
    public void transaction_err_12()    { testAbortCommit(WRITE) ; }

    private void read1(Dataset ds) {
        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ;
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
    }

    private void read2(Dataset ds) {
        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ;
        ds.end() ;
        assertFalse(ds.isInTransaction()) ;
    }

    private void write(Dataset ds) {
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ;
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
    }

    // Error conditions that should be detected.

    private void testBeginBegin(ReadWrite mode1, ReadWrite mode2) {
        Dataset ds = create() ;
        ds.begin(mode1) ;
        try {
            ds.begin(mode2) ;
            fail("Expected transaction exception - begin-begin (" + mode1 + ", " + mode2 + ")") ;
        }
        catch (JenaTransactionException ex) {
            ds.end() ;
        }
    }
    
    private void testCommitCommit(ReadWrite mode) {
        Dataset ds = create() ;
        ds.begin(mode) ;
        ds.commit() ;
        try {
            ds.commit() ;
            fail("Expected transaction exception - commit-commit(" + mode + ")") ;
        }
        catch (JenaTransactionException ex) {
            ds.end() ;
        }
    }

    private void testCommitAbort(ReadWrite mode) {
        Dataset ds = create() ;
        ds.begin(mode) ;
        ds.commit() ;
        try {
            ds.abort() ;
            fail("Expected transaction exception - commit-abort(" + mode + ")") ;
        }
        catch (JenaTransactionException ex) {
            ds.end() ;
        }
    }

    private void testAbortAbort(ReadWrite mode) {
        Dataset ds = create() ;
        ds.begin(mode) ;
        ds.abort() ;
        try {
            ds.abort() ;
            fail("Expected transaction exception - abort-abort(" + mode + ")") ;
        }
        catch (JenaTransactionException ex) {
            ds.end() ;
        }
    }

    private void testAbortCommit(ReadWrite mode) {
        Dataset ds = create() ;
        ds.begin(mode) ;
        ds.abort() ;
        try {
            ds.commit() ;
            fail("Expected transaction exception - abort-commit(" + mode + ")") ;
        }
        catch (JenaTransactionException ex) {
            ds.end() ;
        }
    }    
}

