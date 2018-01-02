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

package org.apache.jena.tdb2.store;


import static org.junit.Assert.*;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.system.Txn;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.sys.StoreConnection;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

/** StoreConnection and transactions - basic wiring.
 *  These tests are slow on rotational disk.
 *  Complet cleaning of file areas is done.
 */ 
public abstract class AbstractTestStoreConnectionBasics
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
        TDBInternal.reset() ;
        location = getLocation() ;
    }

    @After public void after() { TDBInternal.reset() ; } 

    @Test
    public void store_01()
    {
        StoreConnection sConn = StoreConnection.connectCreate(location) ;
        assertNotNull(sConn);
        StoreConnection.release(location) ;
        StoreConnection sConn2 = StoreConnection.connectExisting(location) ;
        assertNull(sConn2);
        StoreConnection sConn3 = StoreConnection.connectCreate(location) ;
        assertNotNull(sConn3);
    }

    @Test
    public void store_02()
    {
        StoreConnection sConn = StoreConnection.connectCreate(location) ;

        { // Isolate to stop mix ups on variables.
            DatasetGraphTDB dsg = sConn.getDatasetGraphTDB() ;
            Txn.executeWrite(dsg, ()->{
                dsg.add(q1) ;
            }) ;

            Txn.executeWrite(dsg, ()->{
                assertTrue(dsg.contains(q1)) ;
            }) ;

            Txn.executeRead(dsg, ()->{
                assertTrue(dsg.contains(q1)) ;
            }) ;
        }

        {
            StoreConnection sConn2 = StoreConnection.connectCreate(location) ;
            DatasetGraphTDB dsg2 = sConn.getDatasetGraphTDB() ;
            Txn.executeRead(dsg2, ()->{
                assertTrue(dsg2.contains(q1)) ;
            }) ;
        }
        
        StoreConnection.release(sConn.getLocation()) ;
        sConn = null ;
        
        {
            if ( ! location.isMem() ) {
                StoreConnection sConn2 = StoreConnection.connectCreate(location) ;
                DatasetGraphTDB dsg3 = sConn2.getDatasetGraphTDB() ;
                Txn.executeRead(dsg3, ()->{
                    assertTrue(dsg3.contains(q1)) ;
                }) ;
            }
        }
    }
    
    @Test
    public void store_03()
    {
        StoreConnection sConn = StoreConnection.connectCreate(location) ;
        
        DatasetGraphTDB dsg = sConn.getDatasetGraphTDB() ;
        Txn.executeWrite(dsg, ()->{
            dsg.add(q1) ;
        }) ;
        
        Txn.executeWrite(dsg, ()->{
            assertTrue(dsg.contains(q1)) ;
        }) ;
        
        try { 
            Txn.executeWrite(dsg, ()->{
                dsg.add(q2) ;
                throw new RuntimeException() ; 
            }) ;
            fail("Should not get to here!") ;
        } catch (RuntimeException ex) {}

        Txn.executeRead(dsg, ()->{
            assertTrue(dsg.contains(q1)) ;
            assertFalse(dsg.contains(q2)) ;
        }) ;
    }
    
    @Test
    public void store_04()
    {
        StoreConnection sConn = StoreConnection.connectCreate(location) ;
        
        DatasetGraphTDB dsg = sConn.getDatasetGraphTDB() ;
        Txn.executeWrite(dsg, ()->{
            dsg.add(q1) ;
        }) ;
        
        Txn.executeWrite(dsg, ()->{
            assertTrue(dsg.contains(q1)) ;
        }) ;

        dsg.begin(ReadWrite.WRITE);
        dsg.add(q2) ;
        dsg.abort() ;
        dsg.end() ;
                
        Txn.executeRead(dsg, ()->{
            assertTrue(dsg.contains(q1)) ;
            assertFalse(dsg.contains(q2)) ;
        }) ;
    }
    
    @Test
    public void store_05()
    {
        StoreConnection sConn = StoreConnection.connectCreate(location) ;
        
        DatasetGraphTDB dsg = sConn.getDatasetGraphTDB() ;
        Txn.executeWrite(dsg, ()->{
            dsg.add(q3) ;
        }) ;
        
        Txn.executeWrite(dsg, ()->{
            assertTrue(dsg.contains(q3)) ;
        }) ;
    }

}