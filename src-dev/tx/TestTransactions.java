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

package tx;

import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.FileOps ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.ReadWrite ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.base.file.Location ;

public class TestTransactions extends BaseTest
{
    static Node s = NodeFactory.parseNode("<s>") ;
    static Node p = NodeFactory.parseNode("<p>") ;
    static Node o = NodeFactory.parseNode("<o>") ;
    static Node g = NodeFactory.parseNode("<g>") ;
    static Triple t = new Triple(s,p,o) ;
    static Quad q = new Quad(g,s,p,o) ;
    
    static final String DIR = ConfigTest.getTestingDirDB() ;
    static final Location LOC = new Location(DIR) ;
    
    @Before public void setup()
    {
        FileOps.clearDirectory(DIR) ;
        StoreConnection.reset() ;
        StoreConnection sConn = StoreConnection.make(LOC) ;
    }
    
    @After public void teardown() {} 
    
    @Test public void trans_01()
    {
        StoreConnection sConn = StoreConnection.make(LOC) ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.READ) ;
        dsg.close() ;
    }
    
    @Test public void trans_02()
    {
        StoreConnection sConn = StoreConnection.make(LOC) ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
        try {
            dsg.add(q) ;
            assertTrue(dsg.contains(q)) ;
            dsg.commit() ;
        } finally { dsg.close() ; }
    }
    
    @Test public void trans_03()
    {
        StoreConnection sConn = StoreConnection.make(LOC) ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
        
        dsg.add(q) ;
        assertTrue(dsg.contains(q)) ;
        dsg.commit() ;
        dsg.close() ;
        
        DatasetGraphTxn dsg2 = sConn.begin(ReadWrite.READ) ;
        assertTrue(dsg2.contains(q)) ;
        dsg2.close() ;
    }

}

