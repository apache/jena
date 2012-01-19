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

package dev;

import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.FileOps ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.* ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;

public class TestTxn extends BaseTest
{
    static final String DIR = ConfigTest.getTestingDirDB() ; 
    
//    @Before public void before()
//    {
//        FileOps.clearDirectory(DIR) ; 
//    }
//    
//    @After public void after()
//    {
//        StoreConnection.reset() ;
//        FileOps.clearDirectory(DIR) ; 
//    }
    
    protected Dataset create()
    { 
        DatasetGraph dsg = TDBFactory.createDatasetGraph() ;
        //DatasetGraph dsg = TDBFactoryTxn.XcreateDatasetGraph(Location.mem(DIR)) ;
        
        if ( dsg instanceof DatasetGraphTDB )
            dsg = new DatasetGraphTransaction((DatasetGraphTDB)dsg) ;    
        return DatasetFactory.create(dsg) ;
    }

    private static Triple triple1 = SSE.parseTriple("(<s> <p> <o>)") ; 
    
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
        DatasetGraph dsg1 = ds2.asDatasetGraph() ;
        
        ds1.begin(ReadWrite.WRITE) ;
        ds1.getDefaultModel().getGraph().add(triple1) ; 
        
        ds2.begin(ReadWrite.READ) ;
        assertTrue(ds2.getDefaultModel().isEmpty()) ;
        ds2.commit() ;
        
        ds1.commit() ;
        
        ds1.end() ;
        ds2.end() ;
        ds2.begin(ReadWrite.READ) ;
        assertFalse(ds2.getDefaultModel().isEmpty()) ;
        assertEquals(1, ds2.getDefaultModel().size()) ;
        ds2.commit() ;

    }

    // Non-Txn then Txn.
    
    
    
}

