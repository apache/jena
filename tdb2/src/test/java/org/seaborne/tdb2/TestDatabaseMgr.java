/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.tdb2;

import static org.junit.Assert.*;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Test ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.jenax.Txn ;
import org.seaborne.tdb2.sys.TDBInternal ;

/** Test of DatabaseMgr - the DatasetGraph level API to TDB2 **/
public class TestDatabaseMgr
{
//    String DIRx = ConfigTest.getCleanDir() ;
//    Location DIR = Location.create(DIRx);
    
    static Quad quad1 = SSE.parseQuad("(_ _:a <p> 1)") ;
    static Quad quad2 = SSE.parseQuad("(_ <s> <p> 1)") ;
    
    @Test
    public void testDatabaseMgr1() {
        TDBInternal.reset() ; 
        DatasetGraph dg1 = DatabaseMgr.connectDatasetGraph(Location.mem("FOO")) ;
        DatasetGraph dg2 = DatabaseMgr.connectDatasetGraph(Location.mem("FOO")) ;
        Txn.executeWrite(dg1, ()->{
            dg1.add(quad1) ;
        }) ;
        Txn.executeRead(dg2, ()->{
            assertTrue(dg2.contains(quad1)) ;
        }) ;
    }
    
    @Test
    public void testDatabaseMgr2() {
        TDBInternal.reset() ;
        // The unnamed location is unique each time.
        DatasetGraph dg1 = DatabaseMgr.connectDatasetGraph(Location.mem()) ;
        DatasetGraph dg2 = DatabaseMgr.connectDatasetGraph(Location.mem()) ;
        Txn.executeWrite(dg1, ()->{
            dg1.add(quad1) ;    
        }) ;
        Txn.executeRead(dg2, ()->{
            assertFalse(dg2.contains(quad1)) ;
        }) ;
    }

    @Test
    public void testDatabaseMgrDisk() {
        TDBInternal.reset() ;
        // The named disk location
        String DIRx = ConfigTest.getCleanDir() ;
        Location LOC = Location.create(DIRx) ;
        FileOps.clearDirectory(DIRx) ;
        try {
            DatasetGraph dg1 = DatabaseMgr.connectDatasetGraph(LOC) ;
            DatasetGraph dg2 = DatabaseMgr.connectDatasetGraph(Location.create(LOC.getDirectoryPath())) ;
            assertSame(dg1, dg2) ;
            Txn.executeWrite(dg1, ()-> {
                dg1.add(quad1) ;
            }) ;
            Txn.executeRead(dg2, ()-> {
                assertTrue(dg2.contains(quad1)) ;
            }) ;
        }
        finally {
            FileOps.clearDirectory(DIRx) ;
        }

    }
}
