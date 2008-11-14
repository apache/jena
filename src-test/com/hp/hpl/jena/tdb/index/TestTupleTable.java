/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import iterator.Iter;

import java.util.Iterator;
import java.util.List;

import lib.Tuple;

import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.TupleIndex;
import com.hp.hpl.jena.tdb.index.TupleTable;
import com.hp.hpl.jena.tdb.store.NodeId;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

import org.junit.Test;
import test.BaseTest;


public class TestTupleTable extends BaseTest
{
    static RecordFactory factory = new RecordFactory(3*SystemTDB.SizeOfNodeId, 0) ;
    static NodeId n1 = new NodeId(1) ;
    static NodeId n2 = new NodeId(2) ;
    static NodeId n3 = new NodeId(3) ;
    static NodeId n4 = new NodeId(0x4040404040404040L) ;
    static NodeId n5 = new NodeId(0x5555555555555555L) ;
    static NodeId n6 = new NodeId(0x6666666666666666L) ; 
    
    static private TupleTable create()
    {
        TupleIndex idxSPO = TestTupleIndex.create("SPO") ;
        TupleIndex idxPOS = TestTupleIndex.create("POS") ;
        TupleIndex idxOSP = TestTupleIndex.create("OSP") ;
        TupleIndex x[] = { idxSPO, idxPOS, idxOSP } ;
        TupleTable table = new TupleTable(3, x, TestTupleIndex.factory, null) ;
        return table ;
    }
    
    static void add(TupleTable table, NodeId x1, NodeId x2, NodeId x3)
    {
        Tuple<NodeId> tuple = new Tuple<NodeId>(x1, x2, x3) ;
        table.add(tuple) ;
    }
    
    @Test public void create1() { create() ; } 
    
    @Test public void createFind1()
    { 
        TupleTable table = create() ;
        add(table, n1, n2, n3) ;

        Tuple<NodeId> pat = new Tuple<NodeId>(null, null, null) ;
        Iterator<Tuple<NodeId>> iter = table.find(pat) ;
        List<Tuple<NodeId>> x = Iter.toList(iter) ;
        int z = x.size() ;
        assertEquals(1, z) ;
        Tuple<NodeId> e1 = x.get(0) ;
        assertEquals(new Tuple<NodeId>(n1, n2, n3) , e1) ;
    }
    
    @Test public void createFind2()
    { 
        TupleTable table = create() ;
        add(table, n1, n2, n3) ;
        add(table, n1, n2, n4) ;

        Tuple<NodeId> pat = new Tuple<NodeId>(null, n2, null) ;
        Iterator<Tuple<NodeId>> iter = table.find(pat) ;
        assertNotNull(iter) ;
        List<Tuple<NodeId>> x = Iter.toList(iter) ;
        int z = x.size() ;
        assertEquals(2, z) ;
        
        Tuple<NodeId> e1 = x.get(0) ;
        Tuple<NodeId> e2 = x.get(1) ;
        assertEquals(new Tuple<NodeId>(n1, n2, n3) , e1) ;
        assertEquals(new Tuple<NodeId>(n1, n2, n4) , e2) ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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