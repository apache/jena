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

package org.apache.jena.atlas.lib;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.ColumnMap ;
import org.apache.jena.atlas.lib.Tuple ;
import static org.apache.jena.atlas.lib.Tuple.* ;
import org.junit.Test ;

public class TestColumnMap extends BaseTest
{
    @Test public void remap1() 
    {
        ColumnMap x = new ColumnMap("SPO->POS", 2,0,1) ;   // S->2 etc
        
        Integer[] array = {0,1,2 } ;
        assertEquals(Integer.valueOf(2), x.mapSlot(0, array) ) ;   
        assertEquals(Integer.valueOf(0), x.mapSlot(1, array) ) ; 
        assertEquals(Integer.valueOf(1), x.mapSlot(2, array) ) ; 
    }
    
    @Test public void remap2() 
    {
        ColumnMap x = new ColumnMap("SPO->POS", 2,0,1) ;
        Integer[] array = { 0,1,2 } ;
        assertEquals(Integer.valueOf(1), x.fetchSlot(0, array)) ;   // The index 1 comes from position 0.
        assertEquals(Integer.valueOf(2), x.fetchSlot(1, array)) ;
        assertEquals(Integer.valueOf(0), x.fetchSlot(2, array)) ;
    }

    @Test public void remap3() 
    {
        ColumnMap x = new ColumnMap("POS", 2,0,1) ;
        Tuple<String> tuple = createTuple("S", "P", "O") ;
        Tuple<String> mapped = x.map(tuple) ;
        Tuple<String> expected = createTuple("P", "O", "S") ;
        assertEquals(expected, mapped) ;
    }
    
    @Test public void remap4() 
    {
        ColumnMap x = new ColumnMap("POS", 2,0,1) ;
        Tuple<String> tuple = createTuple("S", "P", "O") ;
        Tuple<String> tuple2 = x.map(tuple) ;
        tuple2 = x.unmap(tuple2) ;
        assertEquals(tuple, tuple2) ;
    }
    
    @Test public void compile1()
    {
        int[] x = ColumnMap.compileMapping("SPO", "POS") ;
        // SPO -> POS so col 0 goes to col 2, col 1 goes to col 0 and col 2 goes to col 1
        int[] expected = { 2,0,1 } ;
        assertArrayEquals(expected, x) ;
    }

    @Test public void compile2()
    {
        int[] x = ColumnMap.compileMapping("SPOG", "GOPS") ;
        int[] expected = { 3, 2, 1, 0 } ;
        assertArrayEquals(expected, x) ;
    }

    @Test public void map1()
    {
        ColumnMap cmap = new ColumnMap("GSPO", "OSPG") ;
        Tuple<String> tuple = createTuple("G", "S", "P", "O") ;
        Tuple<String> mapped = cmap.map(tuple) ;
        Tuple<String> expected = createTuple("O", "S", "P", "G") ;
        assertEquals(expected, mapped) ;
        Tuple<String> unmapped = cmap.unmap(mapped) ;
        assertEquals(createTuple("G", "S", "P", "O"), unmapped) ;
    }

    @Test public void map2()
    {
        String[] x = { "G", "S", "P", "O" } ;
        String[] y = { "O", "S", "P", "G" } ;
        
        ColumnMap cmap = new ColumnMap("Test", x, y) ;
        Tuple<String> tuple = Tuple.create(x) ;
        Tuple<String> mapped = cmap.map(tuple) ;
        
        Tuple<String> expected = Tuple.create(y) ;
        assertEquals(expected, mapped) ;
        Tuple<String> unmapped = cmap.unmap(mapped) ;
        assertEquals(Tuple.create(x), unmapped) ;
    }
    
}
