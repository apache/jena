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

package org.seaborne.tdb2;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.tdb2.lib.TDBTxn ;
import org.seaborne.tdb2.sys.StoreConnection ;

public class TestTDBFactory extends BaseTest
{
    String DIRx = ConfigTest.getCleanDir() ;
    Location DIR = Location.create(DIRx);
    
    static Quad quad1 = SSE.parseQuad("(_ <s> <p> 1)") ;
    static Quad quad2 = SSE.parseQuad("(_ <s> <p> 1)") ;
    
    @Before public void before()
    {
        FileOps.clearDirectory(DIRx) ; 
    }
    
    @After public void after()
    {
        FileOps.clearDirectory(DIRx) ; 
    }
    
    @Test public void testTDBFactory1()
    {
        StoreConnection.reset() ;
        DatasetGraph dg1 = TDBFactory.createDatasetGraph(Location.mem("FOO")) ;
        DatasetGraph dg2 = TDBFactory.createDatasetGraph(Location.mem("FOO")) ;
        TDBTxn.executeWrite(dg1, ()->{
            dg1.add(quad1) ;    
        }) ;
        TDBTxn.executeRead(dg2, ()->{
            assertTrue(dg2.contains(quad1)) ;
        }) ;
    }
    
    @Test public void testTDBFactory2()
    {
        StoreConnection.reset() ;
        // The unnamed location is unique each time.
        DatasetGraph dg1 = TDBFactory.createDatasetGraph(Location.mem()) ;
        DatasetGraph dg2 = TDBFactory.createDatasetGraph(Location.mem()) ;
        TDBTxn.executeWrite(dg1, ()->{
            dg1.add(quad1) ;    
        }) ;
        TDBTxn.executeRead(dg2, ()->{
            assertFalse(dg2.contains(quad1)) ;
        }) ;
    }

    @Test public void testStoreConnectionTxn1()
    {
        StoreConnection.reset() ;
        DatasetGraph dg1 = StoreConnection.make(DIR).getDatasetGraph() ;
        DatasetGraph dg2 = StoreConnection.make(DIR).getDatasetGraph() ;
        assertSame(dg1, dg2) ;
    }
    
    @Test public void testStoreConnectionTxn2()
    {
        // Named memory locations
        StoreConnection.reset() ;
        DatasetGraph dg1 = StoreConnection.make(Location.mem("FOO")).getDatasetGraph() ;
        DatasetGraph dg2 = StoreConnection.make(Location.mem("FOO")).getDatasetGraph() ;
        
        assertSame(dg1, dg2) ;
    }
    
    @Test public void testStoreConnectionTxn3()
    {
        // Un-named memory locations
        StoreConnection.reset() ;
        DatasetGraph dg1 = StoreConnection.make(Location.mem()).getDatasetGraph() ;
        DatasetGraph dg2 = StoreConnection.make(Location.mem()).getDatasetGraph() ;
        
        assertNotSame(dg1, dg2) ;
    }

}
