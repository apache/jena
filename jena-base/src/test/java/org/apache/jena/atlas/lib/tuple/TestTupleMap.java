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

package org.apache.jena.atlas.lib.tuple;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays ;
import java.util.List ;

import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.TupleFactory ;
import org.apache.jena.atlas.lib.tuple.TupleMap ;
import org.junit.Test;

public class TestTupleMap {
    // Check coverage
    
    @Test
    public void map_tuple_01() {
        TupleMap tmap = TupleMap.create("SPO", "POS");
        Tuple<String> tuple = TupleFactory.tuple("S", "P", "O");
        Tuple<String> tuple2 = tmap.map(tuple);
        assertEquals(tuple2.get(0), "P");
        assertEquals(tuple2.get(1), "O");
        assertEquals(tuple2.get(2), "S");

        Tuple<String> tuple3 = tmap.unmap(tuple2);
        assertEquals(tuple, tuple3);
    }


    @Test
    public void map_tuple_02() {
        TupleMap x = TupleMap.create("SPO", "POS");
        Tuple<String> tuple = TupleFactory.tuple("S", "P", "O");
        Tuple<String> mapped = x.map(tuple);
        Tuple<String> expected = TupleFactory.tuple("P", "O", "S");
        assertEquals(expected, mapped);
    }

    @Test
    public void map_tuple_03() {
        TupleMap tmap = TupleMap.create("GSPO", "OSPG");
        Tuple<String> tuple = TupleFactory.tuple("G", "S", "P", "O");
        Tuple<String> mapped = tmap.map(tuple);
        Tuple<String> expected = TupleFactory.tuple("O", "S", "P", "G");
        assertEquals(expected, mapped);
        Tuple<String> unmapped = tmap.unmap(mapped);
        assertEquals(TupleFactory.tuple("G", "S", "P", "O"), unmapped);
    }

    @Test
    public void map_tuple_04() {
        String[] x = {"G", "S", "P", "O"};
        String[] y = {"O", "S", "P", "G"};

        TupleMap tmap = TupleMap.create("Test", x, y);
        Tuple<String> tuple = TupleFactory.tuple(x);
        Tuple<String> mapped = tmap.map(tuple);

        Tuple<String> expected = TupleFactory.tuple(y);
        assertEquals(expected, mapped);
        Tuple<String> unmapped = tmap.unmap(mapped);
        assertEquals(TupleFactory.tuple(x), unmapped);
    }

    @Test
    public void compile1() {
        TupleMap map = TupleMap.create("SPO", "POS");
        // SPO -> POS 
        // col 0 goes to col 2
        // col 1 goes to col 0
        // col 2 goes to col 1
        Integer[] expectedPut = {2, 0, 1};
        assertEquals(Arrays.asList(expectedPut), map.transformPut());
        Integer[] expectedGet = {1, 2, 0};
        assertEquals(Arrays.asList(expectedGet), map.transformGet());
    }

    @Test
    public void compile2() {
        TupleMap map = TupleMap.create("SPOG", "GOPS");
        Integer[] expected = {3, 2, 1, 0};
        assertEquals(Arrays.asList(expected), map.transformPut());
    }

    @Test
    public void map_slot_01() {
        TupleMap tmap = TupleMap.create("SPO", "POS");
        Tuple<String> tuple = TupleFactory.tuple("S", "P", "O");
        assertEquals("P", tmap.mapSlot(0, tuple));
        assertEquals("O", tmap.mapSlot(1, tuple));
        assertEquals("S", tmap.mapSlot(2, tuple));

        Tuple<String> tuple1 = tmap.map(tuple);
        assertEquals("S", tmap.unmapSlot(0, tuple1));
        assertEquals("P", tmap.unmapSlot(1, tuple1));
        assertEquals("O", tmap.unmapSlot(2, tuple1));
    }

    @Test
    public void map_slot_02() {
        TupleMap tmap = TupleMap.create("SPO", "POS");
        Tuple<String> tuple = TupleFactory.tuple("S", "P", "O");
        Tuple<String> tuple1 = TupleFactory.tuple
            (tuple.get(tmap.mapIdx(0))
            ,tuple.get(tmap.mapIdx(1))
            ,tuple.get(tmap.mapIdx(2)) ) ;
        Tuple<String> tuple2 = tmap.map(tuple);
        assertEquals(tuple2, tuple1) ;
    }        

    @Test
    public void map_slot_03() {
        TupleMap tmap = TupleMap.create("POS", "SPO");
        Tuple<String> tuple = TupleFactory.tuple("P", "O", "S");
        Tuple<String> tuple1 = TupleFactory.tuple
            (tuple.get(tmap.unmapIdx(0))
            ,tuple.get(tmap.unmapIdx(1))
            ,tuple.get(tmap.unmapIdx(2)) ) ;
        Tuple<String> tuple2 = tmap.unmap(tuple);
        assertEquals(tuple2, tuple1) ;
    }        

    @Test
    public void map_transforms() {
        TupleMap x = TupleMap.create("SPO","POS"); 
        List<Integer> listGet = x.transformPut() ;
        List<Integer> listGetExpected = Arrays.asList(2, 0, 1) ;
        assertEquals(listGetExpected, listGet) ;

        List<Integer> listPut = x.transformGet() ;
        List<Integer> listPutExpected = Arrays.asList(1, 2, 0) ;
        assertEquals(listGetExpected, listGet) ;
        
    }
    
    @Test
    public void map_array_01() {
        TupleMap x = TupleMap.create("SPO","POS"); 
        Tuple<Integer> t = TupleFactory.tuple(2, 0, 1);
        Tuple<Integer> t1 = x.map(t);

        String[] array = {"X", "Y", "Z"};
        
        assertEquals("Y", x.mapSlot(0, array)); // The 0th item after mapping is the "1"
        assertEquals("Z", x.mapSlot(1, array));
        assertEquals("X", x.mapSlot(2, array));
        
        String[] array2 = new String[array.length] ;
        x.map(array, array2) ;
        assertArrayEquals(new String[] {"Y", "Z", "X"}, array2) ;
        String[] array3 = new String[array.length] ;
        x.unmap(array2, array3) ;
        
        assertArrayEquals(array, array3) ;
    }
    
    @Test
    public void map_array_02() {
        // (0,1,2) -> (2,0,1) S->2 etc
        // so (0,1,2) <- (1,2,0)
        TupleMap x = TupleMap.create("SPO","POS");
        String[] array = {"Y", "Z", "X"};
        assertEquals("X", x.unmapSlot(0, array)); // The index 0 comes from position 3.
        assertEquals("Y", x.unmapSlot(1, array));
        assertEquals("Z", x.unmapSlot(2, array));
    }
}
