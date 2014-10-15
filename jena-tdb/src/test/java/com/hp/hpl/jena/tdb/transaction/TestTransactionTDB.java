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

package com.hp.hpl.jena.tdb.transaction;

import static com.hp.hpl.jena.query.ReadWrite.READ ;
import static com.hp.hpl.jena.query.ReadWrite.WRITE ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.junit.* ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.transaction.AbstractTestTransaction ;
import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class TestTransactionTDB extends AbstractTestTransaction
{
    static boolean nonDeleteableMMapFiles = SystemTDB.isWindows ;
    String DIR = null ; 
    
    @BeforeClass public static void beforeClassLoggingOff() { LogCtl.disable(SystemTDB.errlog.getName()) ; } 
    @AfterClass public static void afterClassLoggingOn()    { LogCtl.setInfo(SystemTDB.errlog.getName()) ; }
    
    @Before public void before()
    {
        DIR = ConfigTest.getCleanDir() ;
        StoreConnection.release(new Location(DIR)) ;
    }
    
    @After public void after()
    {
        FileOps.clearDirectory(DIR) ; 
    }
    
    @Override
    protected Dataset create()
    { 
        return TDBFactory.createDataset(DIR) ;
    }
    
    private static Triple triple1 = SSE.parseTriple("(<s> <p> <o>)") ;  

    
    @Test public void transaction_50()
    {
        // This assumes you have two datasets on the same location.
        // That's not necessarily true for uncached memory datasets, 
        // where you get two separate datasets so changes to one are
        // not seen by the other at all.
        
        Dataset ds1 = create() ;
        Dataset ds2 = create() ;
        
        ds1.begin(WRITE) ;
        ds1.getDefaultModel().getGraph().add(triple1) ; 
        
        ds2.begin(READ) ;
        assertTrue(ds2.getDefaultModel().isEmpty()) ;
        ds2.commit() ;
        
        ds1.commit() ;

        ds2.begin(READ) ;
        assertFalse(ds2.getDefaultModel().isEmpty()) ;
        assertEquals(1, ds2.getDefaultModel().size()) ;
        ds2.commit() ;
    }
}

