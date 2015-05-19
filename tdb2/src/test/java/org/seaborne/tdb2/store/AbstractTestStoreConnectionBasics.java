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

package org.seaborne.tdb2.store;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.tdb2.lib.TDBTxn ;
import org.seaborne.tdb2.sys.StoreConnection ;

/** StoreConnection and transactions - basic wiring.
 *  These tests are slow on rotational disk.
 *  Complet cleaning of file areas is done.
 */ 
public abstract class AbstractTestStoreConnectionBasics extends BaseTest
{
    // Subclass to give direct and mapped versions.
    
    // Per-test unique-ish.
    static int count = 0 ;
    long x = System.currentTimeMillis()+(count++) ;
    
    Quad q  = SSE.parseQuad("(<g> <s> <p> '000-"+x+"') ") ;
    Quad q1 = SSE.parseQuad("(<g> <s> <p> '111-"+x+"')") ;
    Quad q2 = SSE.parseQuad("(<g> <s> <p> '222-"+x+"')") ;
    Quad q3 = SSE.parseQuad("(<g> <s> <p> '333-"+x+"')") ;
    Quad q4 = SSE.parseQuad("(<g> <s> <p> '444-"+x+"')") ;
    
    Location location = null ;

    protected abstract Location getLocation() ;
    
    @Before public void before()
    {
        StoreConnection.reset() ;
        location = getLocation() ;
    }

    @After public void after() {} 

    @Test
    public void store_01()
    {
        StoreConnection sConn = StoreConnection.make(location) ;
        assertNotNull(sConn);
        StoreConnection.release(location) ;
        StoreConnection sConn2 = StoreConnection.getExisting(location) ;
        assertNull(sConn2);
        StoreConnection sConn3 = StoreConnection.make(location) ;
        assertNotNull(sConn3);
    }

    @Test
    public void store_02()
    {
        StoreConnection sConn = StoreConnection.make(location) ;
        
        { // Isolate to stop mix ups on variables.
            DatasetGraphTxn dsg = sConn.getDatasetGraph() ;
            TDBTxn.executeWrite(dsg, ()->{
                dsg.add(q1) ;
            }) ;

            TDBTxn.executeWrite(dsg, ()->{
                assertTrue(dsg.contains(q1)) ;
            }) ;

            TDBTxn.executeRead(dsg, ()->{
                assertTrue(dsg.contains(q1)) ;
            }) ;
        }
        
        {
            StoreConnection sConn2 = StoreConnection.make(location) ;
            DatasetGraphTxn dsg2 = sConn2.getDatasetGraph() ;
            TDBTxn.executeRead(dsg2, ()->{
                assertTrue(dsg2.contains(q1)) ;
            }) ;
        }
        
        StoreConnection.release(sConn.getLocation()) ;
        {
            if ( ! location.isMem() ) {
                StoreConnection sConn2 = StoreConnection.make(location) ;
                DatasetGraphTxn dsg2 = sConn2.getDatasetGraph() ;
                TDBTxn.executeRead(dsg2, ()->{
                    assertTrue(dsg2.contains(q1)) ;
                }) ;
            }
        }
    }
    
    @Test
    public void store_03()
    {
        StoreConnection sConn = StoreConnection.make(location) ;
        
        DatasetGraphTxn dsg = sConn.getDatasetGraph() ;
        TDBTxn.executeWrite(dsg, ()->{
            dsg.add(q1) ;
        }) ;
        
        TDBTxn.executeWrite(dsg, ()->{
            assertTrue(dsg.contains(q1)) ;
        }) ;
        
        try { 
            TDBTxn.executeWrite(dsg, ()->{
                dsg.add(q2) ;
                throw new RuntimeException() ; 
            }) ;
            fail("Should not get to here!") ;
        } catch (RuntimeException ex) {}

        TDBTxn.executeRead(dsg, ()->{
            assertTrue(dsg.contains(q1)) ;
            assertFalse(dsg.contains(q2)) ;
        }) ;
    }
    
    @Test
    public void store_04()
    {
        StoreConnection sConn = StoreConnection.make(location) ;
        
        DatasetGraphTxn dsg = sConn.getDatasetGraph() ;
        TDBTxn.executeWrite(dsg, ()->{
            dsg.add(q1) ;
        }) ;
        
        TDBTxn.executeWrite(dsg, ()->{
            assertTrue(dsg.contains(q1)) ;
        }) ;

        dsg.begin(ReadWrite.WRITE);
        dsg.add(q2) ;
        dsg.abort() ;
        dsg.end() ;
                
        TDBTxn.executeRead(dsg, ()->{
            assertTrue(dsg.contains(q1)) ;
            assertFalse(dsg.contains(q2)) ;
        }) ;
    }
    
    @Test
    public void store_05()
    {
        StoreConnection sConn = StoreConnection.make(location) ;
        
        DatasetGraphTxn dsg = sConn.getDatasetGraph() ;
        TDBTxn.executeWrite(dsg, ()->{
            dsg.add(q3) ;
        }) ;
        
        TDBTxn.executeWrite(dsg, ()->{
            assertTrue(dsg.contains(q3)) ;
        }) ;
    }

}