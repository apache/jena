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

package com.hp.hpl.jena.tdb.store;

import java.io.File ;

import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.FileOps ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.transaction.TDBTransactionException ;

public class TestStoreConnections  extends BaseTest
{
    static boolean nonDeleteableMMapFiles = SystemTDB.isWindows ;
    
    static Quad q  = SSE.parseQuad("(<g> <s> <p> 000) ") ;
    static Quad q1 = SSE.parseQuad("(<g> <s> <p> 111)") ;
    static Quad q2 = SSE.parseQuad("(<g> <s> <p> 222)") ;
    static Quad q3 = SSE.parseQuad("(<g> <s> <p> 333)") ;
    static Quad q4 = SSE.parseQuad("(<g> <s> <p> 444)") ;
    
    String DIR = null ;

    @Before public void before()
    {
        StoreConnection.reset() ;
        DIR = nonDeleteableMMapFiles ? ConfigTest.getTestingDirUnique() : ConfigTest.getTestingDir() ;
        FileOps.ensureDir(DIR) ;
        FileOps.clearDirectory(DIR) ;
        
        File d = new File(DIR) ;
        if ( d.list().length > 2 )  // . and ..
            throw new RuntimeException("not empty") ;
    }

    @After public void after() {} 

    protected StoreConnection getStoreConnection()
    {
        return StoreConnection.make(DIR) ;
    }
    
    @Test public void store_0()
    {
        // Expel.
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.commit() ;
        dsgW1.end() ;
        StoreConnection.release(sConn.getLocation()) ;
        StoreConnection sConn2 = getStoreConnection() ;
    }
    
    @Test
    public void store_1()
    {
        // Expel.
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.add(q1) ;
        dsgW1.commit() ;
        dsgW1.end() ;
        dsgR1.end();
        
        StoreConnection.release(sConn.getLocation()) ;
        sConn = null ;
        
        StoreConnection sConn2 = getStoreConnection() ;
    }
    
    @Test(expected=TDBTransactionException.class)
    public void store_2()
    {
        // Expel.
        // Only applies to non-memory.
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        StoreConnection.release(sConn.getLocation()) ;
    }

    @Test(expected=TDBTransactionException.class)
    public void store_3()
    {
        // Expel.
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.WRITE) ;
        StoreConnection.release(sConn.getLocation()) ;
    }
    
    
    //@Test
    public void store_4()
    {
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.add(q1) ;
        dsgW1.commit() ;
        dsgW1.end() ;
        dsgR1.end();
        
        StoreConnection.release(sConn.getLocation()) ;
        sConn = null ;
        
        StoreConnection sConn2 = getStoreConnection() ;
        DatasetGraphTxn dsgW2 = sConn2.begin(ReadWrite.WRITE) ;
        dsgW2.add(q2) ;
        dsgW2.commit() ;
        dsgW2.end() ;
        
        DatasetGraphTxn dsgR2 = sConn2.begin(ReadWrite.READ) ;
        long x = Iter.count(dsgR2.find()) ;
        assertEquals(2, x) ;
    }


}

