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

import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.FileOps ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.DatasetGraphMakerTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.sys.TDBMaker ;
import com.hp.hpl.jena.tdb.sys.TDBMakerTxn ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;

public class TestTDBFactory extends BaseTest
{
    final String DIR = SystemTDB.isWindows ? ConfigTest.getTestingDirUnique() : ConfigTest.getTestingDirDB() ;
    
    static Quad quad1 = SSE.parseQuad("(_ <s> <p> 1)") ;
    static Quad quad2 = SSE.parseQuad("(_ <s> <p> 1)") ;
    
    @Before public void before()
    {
        FileOps.clearDirectory(DIR) ; 
    }
    
    @After public void after()
    {
        FileOps.clearDirectory(DIR) ; 
    }
    
    @Test public void testTDBFactory1()
    {
        TDBFactory.reset() ;
        DatasetGraph dg1 = TDBFactory.createDatasetGraph(Location.mem("FOO")) ;
        DatasetGraph dg2 = TDBFactory.createDatasetGraph(Location.mem("FOO")) ;
        dg1.add(quad1) ;
        assertTrue(dg2.contains(quad1)) ;
    }
    
    @Test public void testTDBFactory2()
    {
        TDBFactory.reset() ;
        // The unnamed location is unique each time.
        DatasetGraph dg1 = TDBFactory.createDatasetGraph(Location.mem()) ;
        DatasetGraph dg2 = TDBFactory.createDatasetGraph(Location.mem()) ;
        dg1.add(quad1) ;
        assertFalse(dg2.contains(quad1)) ;
    }

    @Test public void testTDBMakerTxn1()
    {
        TDBMakerTxn.reset() ;
        DatasetGraph dg1 = TDBMakerTxn.createDatasetGraph(DIR) ;
        DatasetGraph dg2 = TDBMakerTxn.createDatasetGraph(DIR) ;
        
        DatasetGraph dgBase1 = ((DatasetGraphTransaction)dg1).getBaseDatasetGraph() ;
        DatasetGraph dgBase2 = ((DatasetGraphTransaction)dg2).getBaseDatasetGraph() ;
        
        assertSame(dgBase1, dgBase2) ;
    }
    
    @Test public void testTDBMakerTxn2()
    {
        TDBMakerTxn.reset() ;
        DatasetGraph dg1 = TDBMakerTxn.createDatasetGraph(Location.mem("FOO")) ;
        DatasetGraph dg2 = TDBMakerTxn.createDatasetGraph(Location.mem("FOO")) ;
        
        DatasetGraph dgBase1 = ((DatasetGraphTransaction)dg1).getBaseDatasetGraph() ;
        DatasetGraph dgBase2 = ((DatasetGraphTransaction)dg2).getBaseDatasetGraph() ;
        
        assertSame(dgBase1, dgBase2) ;
    }
    
    @Test public void testTDBMaker1()
    {
        TDBMaker.reset() ;
        DatasetGraph dg1 = TDBMaker._createDatasetGraph(Location.mem()) ;
        DatasetGraph dg2 = TDBMaker._createDatasetGraph(Location.mem()) ;
        assertSame(dg1, dg2) ;
    }
    
    @Test public void testTDBMaker2()
    {
        TDBMaker.reset() ;
        DatasetGraph dg1 = TDBMaker._createDatasetGraph(DIR) ;
        DatasetGraph dg2 = TDBMaker._createDatasetGraph(DIR) ;
        assertSame(dg1, dg2) ;
    }
    
    @Test public void testTDBMaker3()
    {
        TDBMaker.reset() ;
        
        DatasetGraphMakerTDB f = TDBMaker.getImplFactory() ;

        
        DatasetGraphTDB dg0 = TDBMaker._createDatasetGraph(Location.mem()) ;

        // Uncached.
        TDBMaker.setImplFactory(TDBMaker.uncachedFactory) ;
        DatasetGraphTDB dg1 = TDBMaker._createDatasetGraph(Location.mem()) ;
        DatasetGraphTDB dg2 = TDBMaker._createDatasetGraph(Location.mem()) ;
        assertNotSame(dg1, dg2) ;
        
        // Switch back to cached.
        TDBMaker.setImplFactory(f) ;
        DatasetGraphTDB dg3 = TDBMaker._createDatasetGraph(Location.mem()) ;
        assertNotSame(dg3, dg1) ;
        assertNotSame(dg3, dg2) ;
        assertSame(dg3, dg0) ;
    }
}
