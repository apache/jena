/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.util.Iterator ;

import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;

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
    
    static Node n1 = NodeFactory.parseNode("<http://example/n1>") ;
    static Node n2 = NodeFactory.parseNode("<http://example/n2>") ;
    static Node n3 = NodeFactory.parseNode("<http://example/n3>") ;
    static Node n4 = NodeFactory.parseNode("<http://example/n4>") ;
    static Node n5 = NodeFactory.parseNode("<http://example/n5>") ;
    static Node n6 = NodeFactory.parseNode("<http://example/n6>") ;
    
    
    
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
        DatasetGraphTDB ds = SetupTDB.buildDataset(Location.mem()) ; 
        return ds.getTripleTable() ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */