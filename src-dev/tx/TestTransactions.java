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

import java.util.ArrayList ;
import java.util.List ;

import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.FileOps ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;
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
    
    static Node o1 = NodeFactory.parseNode("1") ;
    static Node o2 = NodeFactory.parseNode("2") ;
    static Node o3 = NodeFactory.parseNode("3") ;
    static Node o4 = NodeFactory.parseNode("4") ;
    
    static Node g = NodeFactory.parseNode("<g>") ;
    
    static Triple t = new Triple(s,p,o) ;
    static Triple t1 = new Triple(s,p,o1) ;
    static Triple t2 = new Triple(s,p,o2) ;
    static Triple t3 = new Triple(s,p,o3) ;
    static Triple t4 = new Triple(s,p,o4) ;
    
    static Quad q = new Quad(g,s,p,o) ;
    static Quad q1 = new Quad(g,s,p,o1) ;
    static Quad q2 = new Quad(g,s,p,o2) ;
    static Quad q3 = new Quad(g,s,p,o3) ;
    static Quad q4 = new Quad(g,s,p,o4) ;
    
    // Later : AbstractTransactionBasics
    static boolean MEMORY = true ;
    
    static final String DIR = ConfigTest.getTestingDirDB() ;
    static final Location LOC = MEMORY ? Location.mem() : new Location(DIR) ;
    
    private StoreConnection sConn ;
    
    @Before public void setup()
    {
        if ( ! MEMORY )
            FileOps.clearDirectory(DIR) ;
        StoreConnection.reset() ;
    }
    
    @After public void teardown() {} 
    
    // Basics.
    
    @Test public void trans_01()
    {
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.READ) ;
        dsg.close() ;
    }
    
    private StoreConnection getStoreConnection()
    {
        if ( MEMORY )
            return StoreConnection.make(Location.mem()) ;
        else
            return StoreConnection.make(LOC) ;
    }

    @Test public void trans_02()
    {
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
        try {
            dsg.add(q) ;
            assertTrue(dsg.contains(q)) ;
            dsg.commit() ;
        } finally { dsg.close() ; }
    }
    
    
    
    @Test public void trans_03()
    {
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
        
        dsg.add(q) ;
        assertTrue(dsg.contains(q)) ;
        dsg.commit() ;
        dsg.close() ;
        
        DatasetGraphTxn dsg2 = sConn.begin(ReadWrite.READ) ;
        assertTrue(dsg2.contains(q)) ;
        dsg2.close() ;
    }
    
    @Test public void trans_04()
    {
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
        
        dsg.add(q) ;
        assertTrue(dsg.contains(q)) ;
        dsg.abort() ;
        dsg.close() ;
        
        DatasetGraphTxn dsg2 = sConn.begin(ReadWrite.READ) ;
        assertFalse(dsg2.contains(q)) ;
        dsg2.close() ;
    }

    static int count(String queryStr, DatasetGraph dsg)
    {
        int counter = 0 ;
        Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.create(dsg)) ;
        try {
            ResultSet rs = qExec.execSelect() ;
            for (; rs.hasNext() ; )
            {
                rs.nextBinding() ;
                counter++ ;
            }
            return counter ;
        } finally { qExec.close() ; }
    }
    
    // To QueryExecUtils?
    public static List<Node> query(String queryStr, String var, DatasetGraphTxn dsg)
    {
        Var v = Var.alloc(var) ;
        Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.create(dsg)) ;
        List<Node> nodes = new ArrayList<Node>() ;
        try {
            ResultSet rs = qExec.execSelect() ;
            for (; rs.hasNext() ; )
            {
                Node n = rs.nextBinding().get(v) ;
                nodes.add(n) ;
            }
            return nodes ;
        } finally { qExec.close() ; }
    }
    
    // Patterns.
    // RS-RE
    // RS-WS-WE-RS 
    // RS-WS-WE-WS-WE-RS
    
    // Errors to catch:
    // WS-WS
    
    
    
}

