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

package org.apache.jena.tdb.transaction ;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.tdb.ConfigTest ;
import org.apache.jena.tdb.StoreConnection ;
import org.apache.jena.tdb.base.block.FileMode ;
import org.apache.jena.tdb.base.file.FileFactory ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.base.objectfile.ObjectFile ;
import org.apache.jena.tdb.setup.DatasetBuilderStd;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.sys.Names ;
import org.apache.jena.tdb.sys.SystemTDB ;
import org.apache.jena.tdb.sys.TDBInternal;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

/** Test of re-attaching to a pre-existing database */  
public class TestTransRestart extends BaseTest {
    static { 
        // Only if run directly, not in test suite.
        if ( false )
            SystemTDB.setFileMode(FileMode.direct) ; 
    }
    
    private String path = null ; 
    private Location location = null ;
    
    private static boolean useTransactionsSetup = true ;
    private static Quad quad1 = SSE.parseQuad("(_ <foo:bar> rdfs:label 'foo')") ;
    private static Quad quad2 = SSE.parseQuad("(_ <foo:bar> rdfs:label 'bar')") ;
    
    @Before public void setup() {
        TDBInternal.reset();
        path = ConfigTest.getCleanDir() ; 
        location = Location.create (path) ;
        if ( useTransactionsSetup )
            setupTxn() ;
        else
            setupPlain() ;
    }
    
    @After public void teardown()
    {
        cleanup() ;
    }
    
    private static DatasetGraphTDB createPlain(Location location) { return DatasetBuilderStd.create(location) ; }
    
    private void setupPlain() {
        // Make without transactions.
        DatasetGraphTDB dsg = createPlain(location) ;
        dsg.add(quad1) ; 
        dsg.close() ;
        return ;
    }

    private void setupTxn() {
        StoreConnection.release(location) ;
        FileOps.clearDirectory(path);
        StoreConnection sc = StoreConnection.make(location) ;
        DatasetGraphTxn dsg = sc.begin(TxnType.WRITE);
        dsg.add(quad1) ; 
        dsg.commit() ;
        dsg.end() ;
        sc.flush(); 
        StoreConnection.release(location) ;
    }
        
    private void cleanup() {
        TDBInternal.reset();
        if ( FileOps.exists(path)) {
            FileOps.clearDirectory(path) ;
            FileOps.deleteSilent(path) ;
        }
    }
    
    @Test
    public void testTxn() {
        assertEquals (3, countRDFNodes()) ;
        StoreConnection sc = StoreConnection.make(location) ; 
        DatasetGraphTxn dsg = sc.begin(TxnType.WRITE) ;
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
        DatasetGraphTDB dsg = createPlain(location) ;
        assertTrue(dsg.contains(quad1)) ;
        dsg.add(quad2) ;
        assertTrue(dsg.contains(quad2)) ;
        dsg.close() ;
        StoreConnection.release(location) ;
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
