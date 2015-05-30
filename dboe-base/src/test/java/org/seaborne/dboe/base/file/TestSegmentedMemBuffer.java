/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
 
package org.seaborne.dboe.base.file;

import java.util.Arrays ;

import org.junit.Assert ;
import org.junit.Test ;

public class TestSegmentedMemBuffer extends Assert {
    private static byte[] data1 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 } ;  
    private static byte[] data2 = { 10,11,12 } ;
    
    @Test public void membuffer_01() {
        SegmentedMemBuffer space = new SegmentedMemBuffer() ;
        assertTrue(space.isOpen()) ;
        assertEquals(0, space.length()) ;
        space.close() ;
        assertFalse(space.isOpen()) ;
        space.close() ;
    }
    
    @Test public void membuffer_02() {
        SegmentedMemBuffer space = new SegmentedMemBuffer() ;
        writeread1(space) ;
        space.truncate(0); 
        writeread2(space) ;
    }

    @Test public void membuffer_03() {
        SegmentedMemBuffer space = new SegmentedMemBuffer() ;
        writeread2(space) ;
    }
    
    @Test public void membuffer_04() {
        SegmentedMemBuffer space = new SegmentedMemBuffer(2) ;
        writeread1(space) ;
        space.truncate(0); 
        writeread2(space) ;
    }

    @Test public void membuffer_05() {
        SegmentedMemBuffer space = new SegmentedMemBuffer(2) ;
        writeread2(space) ;
    }
    
    
    private void writeread1(SegmentedMemBuffer space) {
        long x = space.length() ;
        space.write(x, data1) ;
        assertEquals(x+data1.length, space.length()) ;
        byte[] bytes2 = new byte[data1.length+10] ;
        int y = space.read(x, bytes2) ;
        assertEquals(data1.length, y) ;
        byte[] bytes3 = Arrays.copyOf(bytes2, y) ;
        assertArrayEquals(data1, bytes3);
    }
    
    private void writeread2(SegmentedMemBuffer space) {
        // Offset.
        space.write(0, data2) ;
        long x = data2.length ;
        space.write(x, data1) ;
        assertEquals(x+data1.length, space.length()) ;
        byte[] bytes2 = new byte[data1.length+10] ;
        int y = space.read(x, bytes2) ;
        assertEquals(data1.length, y) ;
        byte[] bytes3 = Arrays.copyOf(bytes2, y) ;
        assertArrayEquals(data1, bytes3);
    }

}

