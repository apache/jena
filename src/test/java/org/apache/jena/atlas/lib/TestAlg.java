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

import java.nio.ByteBuffer ;
import java.nio.ByteOrder ;
import java.nio.IntBuffer ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Alg ;
import org.junit.Test ;

public class TestAlg extends BaseTest
{
    // Linear search is really there to test binary search.
    @Test public void linear1() 
    {
        int[] data = {1, 2, 3} ; 
        IntBuffer b = make(data) ;
        int idx = 0 ; 
        
        idx = Alg.linearSearch(b, 1) ;
        assertEquals(0, idx) ;
        
        idx = Alg.linearSearch(b, 2) ;
        assertEquals(1, idx) ;

        idx = Alg.linearSearch(b, 3) ;
        assertEquals(2, idx) ;
        
    }

    @Test public void linear2() 
    {
        int[] data = {2, 4, 6} ; 
        IntBuffer b = make(data) ;
        int idx = 0 ; 
        
        idx = Alg.linearSearch(b, 1) ;
        assertEquals(-1, idx) ;
        
        idx = Alg.linearSearch(b, 3) ;
        assertEquals(-2, idx) ;

        idx = Alg.linearSearch(b, 5) ;
        assertEquals(-3, idx) ;
        
        idx = Alg.linearSearch(b, 7) ;
        assertEquals(-4, idx) ;
    }

    @Test public void linear3() 
    {
        int[] data = {} ; 
        IntBuffer b = make(data) ;
        int idx = Alg.linearSearch(b, 1) ;
        assertEquals(-1, idx) ;
    }
    
    @Test public void linear4() 
    {
        int[] data = {9} ; 
        IntBuffer b = make(data) ;
        int idx ;
        idx = Alg.linearSearch(b, 1) ;
        assertEquals(-1, idx) ;
        idx = Alg.linearSearch(b, 9) ;
        assertEquals(0, idx) ;
        idx = Alg.linearSearch(b, 100) ;
        assertEquals(-2, idx) ;
    }
    
    @Test public void linear5()
    {
        int[] data = {2, 4, 6, 8, 10} ; 
        IntBuffer b = make(data) ;
        int idx = 0 ;
        idx = Alg.linearSearch(b, 1, 4, 6) ;
        assertEquals(2, idx) ;
        
        idx = Alg.linearSearch(b, 1, 4, 5) ;
        assertEquals(-3, idx) ;
        
        idx = Alg.linearSearch(b, 1, 4, 2) ;
        assertEquals(-2, idx) ;
        
        idx = Alg.linearSearch(b, 1, 4, 10) ;
        assertEquals(-5, idx) ;
    }
    
    @Test public void linear6()
    {
        int[] data = {2, 4, 6, 8, 10} ; 
        IntBuffer b = make(data) ;
        int idx = 0 ;
        idx = Alg.linearSearch(b, 3, 3, 6) ;
        assertEquals(-4, idx) ;
        idx = Alg.linearSearch(b, 3, 3, 5) ;
        assertEquals(-4, idx) ;
        idx = Alg.linearSearch(b, 3, 3, 50) ;
        assertEquals(-4, idx) ;
    }
    
    @Test public void linear7()
    {
        int[] data = {2, 4, 4, 8, 8} ; 
        IntBuffer b = make(data) ;
        int idx = 0 ;
        idx = Alg.linearSearch(b, 4) ;
        assertEquals(1, idx) ;
        idx = Alg.linearSearch(b, 8) ;
        assertEquals(3, idx) ;
    }
    
    @Test public void binary1() 
    {
        int[] data = {1, 2, 3} ; 
        IntBuffer b = make(data) ;
        search(b, 1) ; 
        search(b, 2) ;
        search(b, 3) ;
    }
    
    @Test public void binary2() 
    {
        int[] data = {2, 4, 6} ; 
        IntBuffer b = make(data) ;
        search(b, 1) ;
        search(b, 3) ;
        search(b, 5) ;
        search(b, 7) ;
    }
    
    @Test public void binary3() 
    {
        int[] data = {} ; 
        IntBuffer b = make(data) ;
        search(b, 1) ;
    }
    
    @Test public void binary4() 
    {
        int[] data = {9} ; 
        IntBuffer b = make(data) ;
        search(b, 1) ;
        search(b, 9) ;
        search(b, 100) ;
    }
    
    @Test public void binary5()
    {
        int[] data = {2, 4, 6, 8, 10} ; 
        IntBuffer b = make(data) ;
        search(b, 6, 1, 4) ;
        search(b, 5, 1, 4) ;
        search(b, 2, 1, 4) ;
        search(b, 10, 1, 4) ;
    }

    @Test public void binary6()
    {
        int[] data = {2, 4, 6, 8, 10} ; 
        IntBuffer b = make(data) ;
        search(b, 6, 3, 3) ;
        search(b, 5, 3, 3) ;
        search(b, 50, 3, 3) ;
    }

    // Binary serach does not state which index is returned for sequenences of same value
    @Test public void binary7()
    {
        int[] data = {2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 8, 8} ; 
        IntBuffer b = make(data) ;
        int idx = 0 ;
        idx = Alg.binarySearch(b, 4) ;
        assertEquals(4, data[idx]) ;
        idx = Alg.linearSearch(b, 8) ;
        assertEquals(8, data[idx]) ;
        
        search(b, 3) ;
        search(b, 5) ;
        search(b, 9) ;
    }
    
    private static IntBuffer make(int[] data) 
    {
        //IntBuffer x = IntBuffer.allocate(data.length) ;
        ByteBuffer z = ByteBuffer.allocate(4*data.length) ;
        z.order(ByteOrder.BIG_ENDIAN) ;
        IntBuffer x = z.asIntBuffer() ;
        
        for ( int i = 0 ; i < data.length ; i++ )
            x.put(i,data[i]) ;
        return x ;
    }

    private static void search(IntBuffer b, int k)
    {
        int idx1 = Alg.linearSearch(b, k) ;
        int idx2 = Alg.binarySearch(b, k) ;
        assertEquals(idx1, idx2) ;
    }
    
    private static void search(IntBuffer b, int k, int low, int high)
    {
        int idx1 = Alg.linearSearch(b, low, high, k) ;
        int idx2 = Alg.binarySearch(b, low, high, k) ;
        assertEquals(idx1, idx2) ;
    }
}
