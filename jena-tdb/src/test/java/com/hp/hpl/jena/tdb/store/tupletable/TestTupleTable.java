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

package com.hp.hpl.jena.tdb.store.tupletable;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.store.NodeId;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleTable ;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Tuple ;
import static org.apache.jena.atlas.lib.Tuple.* ;
import org.junit.Test;


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
        TupleIndex idxSPO = TestTupleIndexRecordDirect.create("SPO") ;
        TupleIndex idxPOS = TestTupleIndexRecordDirect.create("POS") ;
        TupleIndex idxOSP = TestTupleIndexRecordDirect.create("OSP") ;
        TupleIndex x[] = { idxSPO, idxPOS, idxOSP } ;
        TupleTable table = new TupleTable(3, x) ;
        return table ;
    }
    
    static private TupleTable create2()
    {
        TupleIndex idxSPO = TestTupleIndexRecordDirect.create("SPO") ;
        TupleIndex x[] = { idxSPO } ;
        TupleTable table = new TupleTable(3, x) ;
        return table ;
    }
    
    static void add(TupleTable table, NodeId x1, NodeId x2, NodeId x3)
    {
        Tuple<NodeId> tuple = createTuple(x1, x2, x3) ;
        table.add(tuple) ;
    }
    
    @Test public void create1() { create() ; } 
    
    @Test public void createFind1()
    { 
        TupleTable table = create() ;
        add(table, n1, n2, n3) ;
        // Cast removes compile lint warning.
        Tuple<NodeId> pat = createTuple((NodeId)null, null, null) ;
        Iterator<Tuple<NodeId>> iter = table.find(pat) ;
        List<Tuple<NodeId>> x = Iter.toList(iter) ;
        int z = x.size() ;
        assertEquals(1, z) ;
        Tuple<NodeId> e1 = x.get(0) ;
        assertEquals(createTuple(n1, n2, n3) , e1) ;
    }
    
    @Test public void createFind2()
    { 
        TupleTable table = create() ;
        add(table, n1, n2, n3) ;
        add(table, n1, n2, n4) ;

        Tuple<NodeId> pat = createTuple(null, n2, null) ;
        Iterator<Tuple<NodeId>> iter = table.find(pat) ;
        assertNotNull(iter) ;
        List<Tuple<NodeId>> x = Iter.toList(iter) ;
        int z = x.size() ;
        assertEquals(2, z) ;
        
        Tuple<NodeId> e1 = x.get(0) ;
        Tuple<NodeId> e2 = x.get(1) ;
        assertEquals(createTuple(n1, n2, n3) , e1) ;
        assertEquals(createTuple(n1, n2, n4) , e2) ;
    }
    
    @Test public void createFind3()
    { 
        // test scan
        TupleTable table = create2() ;
        add(table, n1, n2, n3) ;
        add(table, n1, n2, n4) ;

        Tuple<NodeId> pat = createTuple(n1, null, n3) ;
        Iterator<Tuple<NodeId>> iter = table.find(pat) ;
        assertNotNull(iter) ;
        List<Tuple<NodeId>> x = Iter.toList(iter) ;
        int z = x.size() ;
        assertEquals(1, z) ;
        
        Tuple<NodeId> e1 = x.get(0) ;
        assertEquals(createTuple(n1, n2, n3) , e1) ;
    }
    
    @Test public void createFind4()
    { 
        // test scan
        TupleTable table = create2() ;
        add(table, n1, n2, n3) ;
        add(table, n1, n2, n4) ;

        Tuple<NodeId> pat = createTuple(null, null, n3) ;
        Iterator<Tuple<NodeId>> iter = table.find(pat) ;
        assertNotNull(iter) ;
        List<Tuple<NodeId>> x = Iter.toList(iter) ;
        int z = x.size() ;
        assertEquals(1, z) ;
        
        Tuple<NodeId> e1 = x.get(0) ;
        assertEquals(createTuple(n1, n2, n3) , e1) ;
    }
    
    @Test public void createFind5()
    { 
        // test scan
        TupleTable table = create2() ;
        add(table, n1, n2, n3) ;
        add(table, n1, n2, n4) ;

        Tuple<NodeId> pat = createTuple(null, NodeId.NodeIdAny, n3) ;
        Iterator<Tuple<NodeId>> iter = table.find(pat) ;
        assertNotNull(iter) ;
        List<Tuple<NodeId>> x = Iter.toList(iter) ;
        int z = x.size() ;
        assertEquals(1, z) ;
        
        Tuple<NodeId> e1 = x.get(0) ;
        assertEquals(createTuple(n1, n2, n3) , e1) ;
    }

}
