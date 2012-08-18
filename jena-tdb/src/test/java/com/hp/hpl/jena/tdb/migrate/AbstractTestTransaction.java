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

package com.hp.hpl.jena.tdb.migrate;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.ReadWrite ;

public abstract class AbstractTestTransaction extends BaseTest
{
    //MIGRATE
    protected abstract Dataset create() ;
    
    @Test public void transaction_00()
    {
        Dataset ds = create() ;
        assertTrue(ds.supportsTransactions()) ;
    }
    
    @Test public void transaction_01()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.end() ;
        assertFalse(ds.isInTransaction()) ; 
    }

    @Test public void transaction_02()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ; 
    }

    @Test public void transaction_03()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ; 
    }

    @Test public void transaction_04()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ; 
        ds.end() ;
        assertFalse(ds.isInTransaction()) ; 
    }

    @Test public void transaction_05()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ; 
        ds.end() ;
        assertFalse(ds.isInTransaction()) ; 
    }

    @Test public void transaction_06()
    {
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
    @Test public void transaction_07()
    {
        Dataset ds = create() ;
        read1(ds) ;
        read1(ds) ;
    }
    
    @Test public void transaction_08()
    {
        Dataset ds = create() ;
        read2(ds) ;
        read2(ds) ;
    }
    
    @Test public void transaction_09()
    {
        Dataset ds = create() ;
        write(ds) ;
        write(ds) ;
    }
    
    @Test public void transaction_10()
    {
        Dataset ds = create() ;
        write(ds) ;
        read2(ds) ;
        read2(ds) ;
        write(ds) ;
        read2(ds) ;
    }

    private void read1(Dataset ds)
    {
        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
    }
    
    private void read2(Dataset ds)
    {
        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.end() ;
        assertFalse(ds.isInTransaction()) ;
    }

    private void write(Dataset ds)
    {
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
    }
}

