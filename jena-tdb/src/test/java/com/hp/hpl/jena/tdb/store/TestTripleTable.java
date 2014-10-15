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

package com.hp.hpl.jena.tdb.store;

import java.util.Iterator ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.setup.DatasetBuilderStd ;

public class TestTripleTable extends BaseTest
{
    static {
        Logger.getLogger("com.hp.hpl.jena.tdb.info").setLevel(Level.WARN) ;
        Logger.getLogger("com.hp.hpl.jena.tdb.exec").setLevel(Level.WARN) ;
    }

    private static void add(TripleTable table, Node s, Node p, Node o)
    {
        table.add(new Triple(s,p,o)) ;
    }

    private static void notMatch(TripleTable table, Node s, Node p, Node o)
    {
        Iterator<Triple> iter = table.find(s, p, o) ;
        assertNotNull(iter) ;
        assertFalse(iter.hasNext()) ;
    }

    private static void match(TripleTable table, Node s, Node p, Node o)
    {
        Iterator<Triple> iter = table.find(s, p, o) ;
        assertNotNull(iter) ;
        assertTrue(iter.hasNext()) ;
    }
    
    
    private static void contains(TripleTable table, Node s, Node p, Node o)
    {
        Iterator<Triple> iter = table.find(s, p, o) ;
        assertNotNull(iter) ;
        assertTrue(iter.hasNext()) ;
        assertEquals(new Triple(s, p, o), iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }
    
    static Node n1 = NodeFactoryExtra.parseNode("<http://example/n1>") ;
    static Node n2 = NodeFactoryExtra.parseNode("<http://example/n2>") ;
    static Node n3 = NodeFactoryExtra.parseNode("<http://example/n3>") ;
    static Node n4 = NodeFactoryExtra.parseNode("<http://example/n4>") ;
    static Node n5 = NodeFactoryExtra.parseNode("<http://example/n5>") ;
    static Node n6 = NodeFactoryExtra.parseNode("<http://example/n6>") ;
    
    
    
    @Test public void createTripleTable1()
    { 
        TripleTable table = createTripleTableMem() ; 
        notMatch(table, n1, n2, n3) ;
    }
    
    @Test public void add1()
    { 
        TripleTable table = createTripleTableMem() ;
        table.add(new Triple(n1,n2,n3)) ;
    }
    
    @Test public void find1()
    { 
        TripleTable table = createTripleTableMem() ;
        add(table, n1, n2, n3) ;
        contains(table, n1, n2, n3) ;
        notMatch(table, n1, n2, n4) ;
    }

    @Test public void find2()
    { 
        TripleTable table = createTripleTableMem() ;
        add(table, n1, n2, n3) ;
        add(table, n1, n2, n4) ;
        contains(table, n1, n2, n3) ;
        contains(table, n1, n2, n4) ;
    }

    @Test public void find3()
    { 
        TripleTable table = createTripleTableMem() ;
        add(table, n1, n2, n3) ;
        add(table, n4, n5, n6) ;
        contains(table, n1, n2, n3) ;
        contains(table, n4, n5, n6) ;
        notMatch(table, n1, n2, n4) ;
    }

    @Test public void find4()
    { 
        TripleTable table = createTripleTableMem() ;
        add(table, n1, n2, n3) ;
        add(table, n4, n5, n6) ;
        match(table, Node.ANY, n2, n3) ;
        match(table, null, n2, n3) ;
        match(table, null, null, null) ;
    }
    
    private TripleTable createTripleTableMem()
    {
        DatasetGraphTDB ds = DatasetBuilderStd.create(Location.mem()) ; 
        return ds.getTripleTable() ;
    }
}
