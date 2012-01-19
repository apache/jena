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
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.FileOps ;

import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.DatasetGraphMakerTDB ;
import com.hp.hpl.jena.tdb.sys.TDBMaker ;

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
        DatasetGraphTDB dg1 = TDBMaker._createDatasetGraph(Location.mem()) ;
        DatasetGraphTDB dg2 = TDBMaker._createDatasetGraph(Location.mem()) ;
        assertSame(dg1, dg2) ;
    }
    
    @Test public void factory2()
    {
        DatasetGraphMakerTDB f = TDBMaker.getImplFactory() ;

        TDBMaker.clearDatasetCache() ;
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
