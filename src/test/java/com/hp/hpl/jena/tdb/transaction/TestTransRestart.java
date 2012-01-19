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

package com.hp.hpl.jena.tdb.transaction ;

import java.io.File ;
import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.lib.Pair ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.base.block.FileMode ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.sys.TDBMaker ;

/** Test of re-attaching to a pre-existing database */  
public class TestTransRestart extends BaseTest {
    static { 
        // Only if run directly, not in test suite.
        if ( false )
            SystemTDB.setFileMode(FileMode.direct) ; 
    }
    
//    private final String path = ( SystemTDB.isWindows ? ConfigTest.getTestingDirUnique() : ConfigTest.getTestingDirDB() ) ; 
    private final String path = ( SystemTDB.isWindows ? ConfigTest.getTestingDirUnique() : "/tmp/TDB" ) ; 
    private Location location = new Location (path) ;

    private static boolean useTransactionsSetup = false ;
    private static Quad quad1 = SSE.parseQuad("(_ <foo:bar> rdfs:label 'foo')") ;
    private static Quad quad2 = SSE.parseQuad("(_ <foo:bar> rdfs:label 'bar')") ;
    
    @Before public void setup() {
        cleanup() ;
        FileOps.ensureDir(path) ;
        if (  useTransactionsSetup )
            setupTxn() ;
        else
            setupPlain() ;
    }
    
    @After public void teardown()
    {
        cleanup() ;
    }
    
    private void setupPlain() {
        // Make without transactions.
        DatasetGraphTDB dsg = TDBMaker._createDatasetGraph(location) ;
        dsg.add(quad1) ; 
        dsg.close() ;
        // Normally done via close() but be explicit. 
        TDBMaker.releaseDataset(dsg) ;
        return ;
    }

    private void setupTxn() {
        StoreConnection sc = StoreConnection.make(location) ; 
        DatasetGraphTxn dsg = sc.begin(ReadWrite.WRITE) ; 
        dsg.add(quad1) ; 
        dsg.commit() ; 
        TDB.sync(dsg) ; 
        dsg.end() ; 
        StoreConnection.release(location) ; 
    }
        
    private void cleanup() {
        File dir = new File(path) ;
        if ( dir.exists() ) {
            FileOps.clearDirectory(path) ;
            FileOps.deleteSilent(path) ;
        }
        if ( ! SystemTDB.isWindows )
            // Windows, any mode, does not remove directories, at least not instantly. 
            assertFalse ( dir.exists() ) ;
    }
    
    @Test
    public void testTxn() {
        assertEquals (3, countRDFNodes()) ;

        StoreConnection sc = StoreConnection.make(location) ; 
        DatasetGraphTxn dsg = sc.begin(ReadWrite.WRITE) ;
        assertTrue(dsg.contains(quad1)) ;
        dsg.add(quad2) ; 
        dsg.commit() ; 
        dsg.end() ; 
        StoreConnection.release(location) ;
        assertEquals (4, countRDFNodes()) ;
    }
    
    @Test
    public void testPlain() {
        assertEquals (3, countRDFNodes()) ;
        DatasetGraphTDB dsg = TDBMaker._createDatasetGraph(location) ;
        assertTrue(dsg.contains(quad1)) ;
        dsg.add(quad2) ;
        assertTrue(dsg.contains(quad2)) ;
        dsg.close() ; 
        TDBMaker.releaseDataset(dsg) ;
        assertEquals (4, countRDFNodes()) ;
    }
    
    // Only call when the dataset is not in TDBMaker or in StoreConnection  
    private int countRDFNodes() {
        ObjectFile objects = FileFactory.createObjectFileDisk( location.getPath(Names.indexId2Node, Names.extNodeData) ) ;
        int count = 0 ;
        Iterator<Pair<Long,ByteBuffer>> iter = objects.all() ; 
        while ( iter.hasNext() ) { 
            iter.next() ;
            count++ ;
        }
        objects.close() ;
        return count ;
    }
    
}
