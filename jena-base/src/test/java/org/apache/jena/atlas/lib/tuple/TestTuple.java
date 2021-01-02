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

import static org.junit.Assert.assertArrayEquals ;
import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertNotEquals ;
import static org.junit.Assert.fail ;

import java.util.ArrayList ;
import java.util.List ;

import org.junit.Test ;

public class TestTuple {
    @Test public void tuple_0() {
        Tuple<Integer> tuple = TupleFactory.create0() ;
        assertEquals(0, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void tuple_1() {
        Tuple<Integer> tuple = TupleFactory.create1(9) ;
        assertEquals(1, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void tuple_2() {
        Tuple<Integer> tuple = TupleFactory.create2(9,8) ;
        assertEquals(2, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void tuple_3() {
        Tuple<Integer> tuple = TupleFactory.create3(9,8,7) ;
        assertEquals(3, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void tuple_4() {
        Tuple<Integer> tuple = TupleFactory.create4(9,8,7,6) ;
        assertEquals(4, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void tuple_5() {
        Tuple<Integer> tuple = TupleFactory.create5(9,8,7,6,5) ;
        assertEquals(5, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void tuple_6() {
        Tuple<Integer> tuple = TupleFactory.create6(9,8,7,6,5,4) ;
        assertEquals(6, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void tuple_7() {
        Tuple<Integer> tuple = TupleFactory.create7(9,8,7,6,5,4,3) ;
        assertEquals(7, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void tuple_8() {
        Tuple<Integer> tuple = TupleFactory.create8(9,8,7,6,5,4,3,2) ;
        assertEquals(8, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void tuple_N0() {
        Tuple<Integer> tuple = TupleFactory.tuple() ;
        assertEquals(0, tuple.len()) ;
        assertEquals(Tuple0.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void tuple_N1() {
        Tuple<Integer> tuple = TupleFactory.tuple(9) ;
        assertEquals(1, tuple.len()) ;
        assertEquals(Tuple1.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void tuple_N2() {
        Tuple<Integer> tuple = TupleFactory.tuple(9,8) ;
        assertEquals(2, tuple.len()) ;
        assertEquals(Tuple2.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void tuple_N3() {
        Tuple<Integer> tuple = TupleFactory.tuple(9,8,7) ;
        assertEquals(3, tuple.len()) ;
        assertEquals(Tuple3.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void tuple_N4() {
        Tuple<Integer> tuple = TupleFactory.tuple(9,8,7,6) ;
        assertEquals(4, tuple.len()) ;
        assertEquals(Tuple4.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void tuple_N5() {
        Tuple<Integer> tuple = TupleFactory.tuple(9,8,7,6,5) ;
        assertEquals(5, tuple.len()) ;
        assertEquals(Tuple5.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void tuple_N6() {
        Tuple<Integer> tuple = TupleFactory.tuple(9,8,7,6,5,4) ;
        assertEquals(6, tuple.len()) ;
        assertEquals(Tuple6.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void tuple_N7() {
        Tuple<Integer> tuple = TupleFactory.tuple(9,8,7,6,5,4,3) ;
        assertEquals(7, tuple.len()) ;
        assertEquals(Tuple7.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void tuple_N8() {
        Tuple<Integer> tuple = TupleFactory.tuple(9,8,7,6,5,4,3,2) ;
        assertEquals(8, tuple.len()) ;
        assertEquals(Tuple8.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void tuple_N() {
        Tuple<Integer> tuple = TupleFactory.tuple(9,8,7,6,5,4,3,2,1,0) ;
        assertEquals(10, tuple.len()) ;
        assertEquals(TupleN.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void tuple_map_0() {
        Tuple<Integer> tuple1 = TupleFactory.tuple() ;
        Tuple<Integer> tuple2 = tuple1.map(x->x+10);
        assertEquals(Tuple0.class, tuple2.getClass()) ;
    }

    @Test public void tuple_map_4() {
        Tuple<Integer> tuple1 = TupleFactory.tuple(3,2,1,0) ;
        Tuple<Integer> tuple2 = tuple1.map(x->x+10);
        assertEquals(Tuple4.class, tuple2.getClass()) ;
        assertEquals(10, tuple2.get(3).intValue());
        assertEquals(11, tuple2.get(2).intValue());
        assertEquals(12, tuple2.get(1).intValue());
        assertEquals(13, tuple2.get(0).intValue());
    }

    @Test public void tuple_map_N() {
        Tuple<Integer> tuple1 = TupleFactory.tuple(8,7,6,5,4,3,2,1,0) ;
        Tuple<Integer> tuple2 = tuple1.map(x->x+10);
        assertEquals(TupleN.class, tuple2.getClass()) ;
        assertEquals(10, tuple2.get(8).intValue());
        assertEquals(18, tuple2.get(0).intValue());
    }

    @Test public void tuple_equals_1() {
        Tuple<Integer> tuple1 = TupleFactory.tuple(9,8,7) ;
        Tuple<Integer> tuple2 = TupleN.create(9,8,7) ;
        assertEquals(tuple1.hashCode(), tuple2.hashCode()) ;
        assertEquals(tuple1, tuple2) ;
    }

    @Test public void tuple_not_equals_1() {
        Tuple<Integer> tuple1 = TupleFactory.tuple(9,8,7) ;
        Tuple<Integer> tuple2 = TupleFactory.tuple(7,8,9) ;
        assertNotEquals(tuple1.hashCode(), tuple2.hashCode()) ;
        assertNotEquals(tuple1, tuple2) ;
    }

    @Test public void tuple_not_equals_2() {
        Tuple<Integer> tuple1 = TupleFactory.tuple(9,8,7) ;
        Tuple<Integer> tuple2 = TupleFactory.tuple(9,8) ;
        assertNotEquals(tuple1.hashCode(), tuple2.hashCode()) ;
        assertNotEquals(tuple1, tuple2) ;
    }

    @Test public void tuple_array_1() {
        Tuple<Integer> tuple1 = TupleFactory.tuple(9,8,7) ;
        Integer[] array = tuple1.asArray(Integer.class) ;
        Tuple<Integer> tuple2 = TupleFactory.create(array) ;
        assertEquals(tuple1, tuple2) ;
    }

    @Test public void tuple_array_2() {
        Tuple<Integer> tuple1 = TupleFactory.tuple(9,8,7) ;
        Integer[] array = new Integer[2] ;
        tuple1.copyInto(array, 0, 2) ;
        Integer[] array1 = { 9, 8 } ;
        assertArrayEquals(array1, array) ;
    }

    @Test public void tuple_array_3() {
        Tuple<Integer> tuple1 = TupleFactory.tuple(9,8,7) ;
        Integer[] array = new Integer[3] ;
        tuple1.copyInto(array) ;
        Integer[] array1 = { 9, 8, 7 } ;
        assertArrayEquals(array1, array) ;
    }

    private static void check(Tuple<Integer> tuple) {
        int val = 9 ;
        for ( int i = 0 ; i < tuple.len() ; i++ ) {
            assertEquals(val-i, tuple.get(i).intValue()) ;
        }
        List<Integer> list = tuple.asList() ;
        for ( int i = 0 ; i < tuple.len() ; i++ ) {
            assertEquals(val-i, list.get(i).intValue()) ;
        }
        try { tuple.get(-1) ; fail("Index -1 did not throw an exception") ; }
        catch(IndexOutOfBoundsException ex) {}
        try { tuple.get(tuple.len()) ; fail("Index len() did not throw an exception") ; }
        catch(IndexOutOfBoundsException ex) {}

        // Other constructors
        List<Integer> list2 = new ArrayList<>(list) ;
        Tuple<Integer> tuple2 = TupleFactory.create(list2) ;
        assertEquals(tuple.hashCode(), tuple2.hashCode()) ;
        assertEquals(tuple, tuple2) ;

        // Other constructors
        List<Integer> list3 = new ArrayList<>(list) ;
        Tuple<Integer> tuple3 = TupleFactory.tuple(list3.toArray(new Integer[0])) ;
        assertEquals(tuple, tuple3) ;
    }
}
