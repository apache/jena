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
    
    @Test public void transaction_0()
    {
        Dataset ds = create() ;
        assertTrue(ds.supportsTransactions()) ;
    }
    
    @Test public void transaction_1()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.end() ;
        assertFalse(ds.isInTransaction()) ; 
    }

    @Test public void transaction_2()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ; 
    }

    @Test public void transaction_3()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ; 
    }

    @Test public void transaction_4()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ; 
        ds.end() ;
        assertFalse(ds.isInTransaction()) ; 
    }

    @Test public void transaction_5()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ; 
        ds.end() ;
        assertFalse(ds.isInTransaction()) ; 
    }

    @Test public void transaction_6()
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

    @Test public void transaction_7()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;

        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
    }
    
    @Test public void transaction_8()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;

        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
    }
    
    @Test public void transaction_9()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
        assertFalse(ds.isInTransaction()) ;

        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
        assertFalse(ds.isInTransaction()) ;

        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ;
        ds.end() ;
        assertFalse(ds.isInTransaction()) ;
    }


}

