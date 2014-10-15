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

import static org.apache.jena.atlas.lib.Tuple.createTuple ;

import java.util.Iterator ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.ColumnMap ;
import org.apache.jena.atlas.lib.Tuple ;
import org.junit.Test ;

import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.IndexFactory ;
import com.hp.hpl.jena.tdb.index.IndexParams ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.setup.StoreParams ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class TestTupleIndexRecordDirect extends BaseTest
{
    static RecordFactory factory = new RecordFactory(3*SystemTDB.SizeOfNodeId, 0) ;
    static NodeId n1 = new NodeId(1) ;
    static NodeId n2 = new NodeId(2) ;
    static NodeId n3 = new NodeId(3) ;
    static NodeId n4 = new NodeId(0x4040404040404040L) ;
    static NodeId n5 = new NodeId(0x5555555555555555L) ;
    static NodeId n6 = new NodeId(0x6666666666666666L) ; 
    
    static TupleIndexRecord create(String description)
    {
        IndexParams indexParams = StoreParams.getDftStoreParams() ; 
        RangeIndex rIdx = IndexFactory.buildRangeIndex(FileSet.mem(), factory, indexParams) ;
        ColumnMap cmap = new ColumnMap("SPO", description) ;
        TupleIndexRecord index = new TupleIndexRecord(3, cmap, description, factory, rIdx) ;
        return index ;
    }
    
    static void add(TupleIndexRecord index, NodeId x1, NodeId x2, NodeId x3)
    {
        Tuple<NodeId> tuple = createTuple(x1, x2, x3) ;
        index.add(tuple) ;
    }
    
    @Test public void TupleIndexRecord_1()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
    }
    
    @Test public void TupleIndexRecordSPO_1()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        
        Tuple<NodeId> tuple2 = createTuple(n1, n2, n3) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        assertTrue(iter.hasNext()) ;
        iter.next();
        assertFalse(iter.hasNext()) ;
    }
 
    @Test public void TupleIndexRecordSPO_2()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        
        Tuple<NodeId> tuple2 = createTuple(n1, n2, null) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        assertTrue(iter.hasNext()) ;
        iter.next();
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void TupleIndexRecordSPO_3()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        
        Tuple<NodeId> tuple2 = createTuple(n1, null, n3) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        assertNull(iter) ;
        iter = index.findOrPartialScan(tuple2) ;
        assertTrue(iter.hasNext()) ;
        iter.next();
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void TupleIndexRecordSPO_4()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        
        Tuple<NodeId> tuple2 = createTuple(n1, NodeId.NodeIdAny, NodeId.NodeIdAny) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        assertTrue(iter.hasNext()) ;
        iter.next();
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void TupleIndexRecordSPO_5()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        add(index, n1, n2, n4) ;
        
        Tuple<NodeId> tuple2 = createTuple(n1, n2, n3) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        Set<Tuple<NodeId>> x = Iter.toSet(iter) ;
        assertEquals(1, x.size()) ;
        assertTrue(x.contains(createTuple(n1, n2, n3))) ;
        assertFalse(x.contains(createTuple(n1, n2, n4))) ;
    }

    @Test public void TupleIndexRecordSPO_6()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        add(index, n1, n2, n4) ;
        
        Tuple<NodeId> tuple2 = createTuple(n1, n2, NodeId.NodeIdAny) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        Set<Tuple<NodeId>> x = Iter.toSet(iter) ;
        assertEquals(2, x.size()) ;
        assertTrue(x.contains(createTuple(n1, n2, n3))) ;
        assertTrue(x.contains(createTuple(n1, n2, n4))) ;
    }

    @Test public void TupleIndexRecordSPO_7()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        add(index, n1, n2, n4) ;
        
        Tuple<NodeId> tuple2 = createTuple(n1, NodeId.NodeIdAny, NodeId.NodeIdAny) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        Set<Tuple<NodeId>> x = Iter.toSet(iter) ;
        assertEquals(2, x.size()) ;
        assertTrue(x.contains(createTuple(n1, n2, n3))) ;
        assertTrue(x.contains(createTuple(n1, n2, n4))) ;
    }

    @Test public void TupleIndexRecordSPO_8()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        add(index, n2, n3, n4) ;

        {
            Tuple<NodeId> tuple2 = createTuple(n1, NodeId.NodeIdAny, NodeId.NodeIdAny) ;
            Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
            Set<Tuple<NodeId>> x = Iter.toSet(iter) ;
            assertEquals(1, x.size()) ;
            assertTrue(x.contains(createTuple(n1, n2, n3))) ;
        }

        {
            Tuple<NodeId> tuple2 = createTuple(n2, NodeId.NodeIdAny, NodeId.NodeIdAny) ;
            Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
            Set<Tuple<NodeId>> x = Iter.toSet(iter) ;
            assertEquals(1, x.size()) ;
            assertTrue(x.contains(createTuple(n2, n3, n4))) ;
        }
    }

    @Test public void TupleIndexRecordPOS_1()
    {
        TupleIndexRecord index = create("POS") ;
        add(index, n1, n2, n3) ;
        Tuple<NodeId> tuple2 = createTuple(n1, n2, n3) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        assertTrue("Can't find tuple", iter.hasNext()) ;
        iter.next();
        assertFalse(iter.hasNext()) ;
    }
 
    @Test public void TupleIndexRecordPOS_2()
    {
        TupleIndexRecord index = create("POS") ;
        add(index, n1, n2, n3) ;
        
        Tuple<NodeId> tuple2 = createTuple(null, n2, null) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        assertTrue("Can't find tuple",iter.hasNext()) ;
        iter.next();
        assertFalse(iter.hasNext()) ;
    }
    

    @Test public void TupleIndexRecordPOS_3()
    {
        TupleIndexRecord index = create("POS") ;
        add(index, n1, n2, n3) ;
        
        Tuple<NodeId> tuple2 = createTuple(null, n2, n3) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        assertTrue("Can't find tuple", iter.hasNext()) ;
        iter.next();
        assertFalse(iter.hasNext()) ;
    }

    @Test public void TupleIndexRecordFindScan_1()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        Tuple<NodeId> tuple2 = createTuple(n1, null, n3) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        assertNull(iter) ;
        iter = index.findOrPartialScan(tuple2) ;
        assertTrue(iter.hasNext()) ;
        iter.next();
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void TupleIndexRecordFindScan_2()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        add(index, n1, n2, n4) ;
        
        
        Tuple<NodeId> tuple2 = createTuple(null, null, n3) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        assertNull(iter) ;
        
        iter = index.findOrPartialScan(tuple2) ;
        assertNull(iter) ;
        
        iter = index.findOrScan(tuple2) ;
        assertTrue(iter.hasNext()) ;
        iter.next();
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void TupleIndexRecordFindNot_1()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        
        Tuple<NodeId> tuple2 = createTuple(n4, n5, n6) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        assertNotNull(iter) ;
        assertFalse(iter.hasNext()) ;
   }
    
    @Test public void TupleIndexRecordFindNot_2()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        
        Tuple<NodeId> tuple2 = createTuple(n1, n5, n6) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        assertFalse(iter.hasNext()) ;
   }

    @Test public void TupleIndexRecordFindNot_3()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        
        Tuple<NodeId> tuple2 = createTuple(n1, null, n6) ;
        Iterator<Tuple<NodeId>> iter = index.findOrPartialScan(tuple2) ;
        assertFalse(iter.hasNext()) ;
   }

    @Test public void TupleIndexRecordFindNot_4()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        add(index, n1, n5, n6) ;
        
        Tuple<NodeId> tuple2 = createTuple(n4, n5, n6) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        assertFalse(iter.hasNext()) ;
   }
    
    @Test public void TupleIndexRecordFindNot_5()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        add(index, n1, n5, n6) ;
        
        Tuple<NodeId> tuple2 = createTuple(n2, n5, n6) ;
        Iterator<Tuple<NodeId>> iter = index.findByIndex(tuple2) ;
        assertFalse(iter.hasNext()) ;
   }

    @Test public void TupleIndexRecordFindNot_6()
    {
        TupleIndexRecord index = create("SPO") ;
        add(index, n1, n2, n3) ;
        add(index, n4, n5, n6) ;
        
        Tuple<NodeId> tuple2 = createTuple(n1, null, n6) ;
        Iterator<Tuple<NodeId>> iter = index.findOrPartialScan(tuple2) ;
        assertFalse(iter.hasNext()) ;
   }

    
}
