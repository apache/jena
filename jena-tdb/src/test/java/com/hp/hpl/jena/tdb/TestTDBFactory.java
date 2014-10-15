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

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.FileOps ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.sys.TDBMaker ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;

public class TestTDBFactory extends BaseTest
{
    String DIR = ConfigTest.getCleanDir() ;
    
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
        TDBMaker.reset() ;
        DatasetGraph dg1 = TDBMaker.createDatasetGraphTransaction(DIR) ;
        DatasetGraph dg2 = TDBMaker.createDatasetGraphTransaction(DIR) ;
        
        DatasetGraph dgBase1 = ((DatasetGraphTransaction)dg1).getBaseDatasetGraph() ;
        DatasetGraph dgBase2 = ((DatasetGraphTransaction)dg2).getBaseDatasetGraph() ;
        
        assertSame(dgBase1, dgBase2) ;
    }
    
    @Test public void testTDBMakerTxn2()
    {
        // Named memory locations
        TDBMaker.reset() ;
        DatasetGraph dg1 = TDBMaker.createDatasetGraphTransaction(Location.mem("FOO")) ;
        DatasetGraph dg2 = TDBMaker.createDatasetGraphTransaction(Location.mem("FOO")) ;
        
        DatasetGraph dgBase1 = ((DatasetGraphTransaction)dg1).getBaseDatasetGraph() ;
        DatasetGraph dgBase2 = ((DatasetGraphTransaction)dg2).getBaseDatasetGraph() ;
        
        assertSame(dgBase1, dgBase2) ;
    }
    
    @Test public void testTDBMakerTxn3()
    {
        // Un-named memory locations
        TDBMaker.reset() ;
        DatasetGraph dg1 = TDBMaker.createDatasetGraphTransaction(Location.mem()) ;
        DatasetGraph dg2 = TDBMaker.createDatasetGraphTransaction(Location.mem()) ;
        
        DatasetGraph dgBase1 = ((DatasetGraphTransaction)dg1).getBaseDatasetGraph() ;
        DatasetGraph dgBase2 = ((DatasetGraphTransaction)dg2).getBaseDatasetGraph() ;
        
        assertNotSame(dgBase1, dgBase2) ;
    }

}
