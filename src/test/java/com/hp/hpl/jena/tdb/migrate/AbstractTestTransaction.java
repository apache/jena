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

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public abstract class AbstractTestTransaction extends BaseTest
{
    //MIGRATE
    protected abstract Dataset create() ;
    
    private static Triple triple1 = SSE.parseTriple("(<s> <p> <o>)") ;  

    @Test public void factoryTxn0()
    {
        Dataset ds = create() ;
        assertTrue(ds.supportsTransactions()) ;
    }
    
    @Test public void factoryTxn1()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.READ) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.end() ;
        assertFalse(ds.isInTransaction()) ; 
    }

    @Test public void factoryTxn2()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ; 
    }

    @Test public void factoryTxn3()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ; 
    }

    @Test public void factoryTxn4()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.commit() ;
        assertFalse(ds.isInTransaction()) ; 
        ds.end() ;
        assertFalse(ds.isInTransaction()) ; 
    }

    @Test public void factoryTxn5()
    {
        Dataset ds = create() ;
        ds.begin(ReadWrite.WRITE) ;
        assertTrue(ds.isInTransaction()) ; 
        ds.abort() ;
        assertFalse(ds.isInTransaction()) ; 
        ds.end() ;
        assertFalse(ds.isInTransaction()) ; 
    }


    @Test public void factoryTxn10()
    {
        Dataset ds1 = create() ;
        Dataset ds2 = create() ;
        
        ds1.begin(ReadWrite.WRITE) ;
        ds1.getDefaultModel().getGraph().add(triple1) ; 
        
        ds2.begin(ReadWrite.READ) ;
        assertTrue(ds2.getDefaultModel().isEmpty()) ;
        ds2.commit() ;
        
        ds1.commit() ;

        ds2.begin(ReadWrite.READ) ;
        assertFalse(ds2.getDefaultModel().isEmpty()) ;
        assertEquals(1, ds2.getDefaultModel().size()) ;
        ds2.commit() ;

    }

}

