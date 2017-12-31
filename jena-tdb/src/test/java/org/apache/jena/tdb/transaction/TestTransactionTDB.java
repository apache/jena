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

package org.apache.jena.tdb.transaction;

import static org.apache.jena.query.ReadWrite.READ ;
import static org.apache.jena.query.ReadWrite.WRITE ;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.transaction.AbstractTestTransactionLifecycle ;
import org.apache.jena.tdb.ConfigTest ;
import org.apache.jena.tdb.StoreConnection ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.sys.SystemTDB ;
import org.junit.* ;

public class TestTransactionTDB extends AbstractTestTransactionLifecycle
{
    private String DIR = null ; 
    
    @BeforeClass public static void beforeClassLoggingOff() { LogCtl.disable(SystemTDB.errlog.getName()) ; } 
    @AfterClass public static void afterClassLoggingOn()    { LogCtl.setInfo(SystemTDB.errlog.getName()) ; }
    
    @Before
    public void before() {
        DIR = ConfigTest.getCleanDir();
        StoreConnection.release(Location.create(DIR));
    }

    @After
    public void after() {
        FileOps.clearDirectory(DIR);
    }

    @Override
    protected Dataset create() {
        return TDBFactory.createDataset(DIR);
    }
    
    private static Triple triple1 = SSE.parseTriple("(<s> <p> <o>)") ;  

    @Test 
    public void transaction_50() {
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
        // See ds1 updates
        Graph g = ds2.getDefaultModel().getGraph() ;
        DatasetGraph dsg = ds2.asDatasetGraph() ; 
        g = dsg.getDefaultGraph() ;
        
        boolean b0 = g.isEmpty() ;
        boolean b1 = ds2.getDefaultModel().isEmpty() ;
        
        assertFalse(ds2.getDefaultModel().isEmpty()) ;
        assertEquals(1, ds2.getDefaultModel().size()) ;
        ds2.commit() ;
    }
}

