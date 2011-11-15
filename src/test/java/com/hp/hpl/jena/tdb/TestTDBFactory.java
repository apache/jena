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

package com.hp.hpl.jena.tdb;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.FileOps ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
import com.hp.hpl.jena.tdb.sys.DatasetGraphMakerTDB;
import com.hp.hpl.jena.tdb.sys.TDBMaker;

public class TestTDBFactory extends BaseTest
{
    static final String DIR = ConfigTest.getTestingDirDB() ; 
    
    @BeforeClass public static void beforeClass()
    {
        FileOps.clearDirectory(DIR) ; 
    }
    
    @AfterClass public static void afterClass()
    {
        FileOps.clearDirectory(DIR) ; 
    }
    
    @Test public void factory1()
    {
        DatasetGraphTDB dg1 = TDBFactory.createDatasetGraph(Location.mem()) ;
        DatasetGraphTDB dg2 = TDBFactory.createDatasetGraph(Location.mem()) ;
        assertSame(dg1, dg2) ;
    }
    
    @Test public void factory2()
    {
        DatasetGraphMakerTDB f = TDBMaker.getImplFactory() ;

        TDBMaker.clearDatasetCache() ;
        DatasetGraphTDB dg0 = TDBFactory.createDatasetGraph(Location.mem()) ;

        // Uncached.
        TDBMaker.setImplFactory(TDBMaker.uncachedFactory) ;
        DatasetGraphTDB dg1 = TDBFactory.createDatasetGraph(Location.mem()) ;
        DatasetGraphTDB dg2 = TDBFactory.createDatasetGraph(Location.mem()) ;
        assertNotSame(dg1, dg2) ;
        
        // Switch back to cached.
        TDBMaker.setImplFactory(f) ;
        DatasetGraphTDB dg3 = TDBFactory.createDatasetGraph(Location.mem()) ;
        assertNotSame(dg3, dg1) ;
        assertNotSame(dg3, dg2) ;
        assertSame(dg3, dg0) ;
    }
    
    @Test public void factoryTxn1()
    {
        DatasetGraphTransaction dg1 = TDBFactoryTxn.createDatasetGraph(Location.mem()) ;
        DatasetGraphTransaction dg2 = TDBFactoryTxn.createDatasetGraph(Location.mem()) ;
        assertSame(dg1.get(), dg2.get()) ;
    }
    
    private static Triple triple1 = SSE.parseTriple("(<s> <p> <o>)") ;  
    
    @Test public void factoryTxn2()
    {
        Dataset ds1 = TDBFactoryTxn.createDataset(DIR) ;
        Dataset ds2 = TDBFactoryTxn.createDataset(DIR) ;
        
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
