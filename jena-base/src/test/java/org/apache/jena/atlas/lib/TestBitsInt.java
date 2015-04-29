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
import org.apache.jena.atlas.lib.BitsInt ;
import org.junit.Test ;


public class TestBitsInt extends BaseTest
{
    @Test public void testMask1()
    {
        int v = BitsInt.mask(0,1) ;
        check(0x1, v) ;
    }
    
    @Test public void testMask2()
    {
        int v = BitsInt.mask(0,2) ;
        check(0x3, v) ;
    }
    
    @Test public void testMask3()
    {
        int v = BitsInt.mask(1,2) ;
        check(0x2, v) ;
    }
    
    @Test public void testMask4()
    {
        int v = BitsInt.mask(0,32) ;
        check(-1, v) ;
    }
    
    @Test public void testMask5()
    {
        int v = BitsInt.mask(16,24) ;
        check(0x00FF0000, v) ;
    }

    @Test public void testMask6()
    {
        int v = BitsInt.mask(16,32) ;
        check(0xFFFF0000, v) ;
    }

    @Test public void testMask7()
    {
        int v = BitsInt.mask(0, 0) ;
        check(0, v) ;
    }
    
    @Test public void testMaskZero1()
    {
        int v = BitsInt.maskZero(0,1) ;
        check(~0x1, v) ;
    }
    
    @Test public void testMaskZero2()
    {
        int v = BitsInt.maskZero(0,2) ;
        check(~0x3, v) ;
    }
    
    @Test public void testMaskZero3()
    {
        int v = BitsInt.maskZero(1,2) ;
        check(0xFFFFFFFD, v) ;
    }
    
    @Test public void testMaskZero4()
    {
        int v = BitsInt.maskZero(0,32) ;
        check(0, v) ;
    }
    
    @Test public void testMaskZero5()
    {
        int v = BitsInt.maskZero(16,24) ;
        check(0xFF00FFFF, v) ;
    }

    @Test public void testMaskZero6()
    {
        int v = BitsInt.maskZero(16,32) ;
        check(0xFFFF, v) ;
    }

    @Test public void testMaskZero7()
    {
        int v = BitsInt.maskZero(0, 0) ;
        check(-1, v) ;
    }

    @Test public void testClear1()
    {
        int v = 0xF0F0 ;
        v = BitsInt.clear(v, 4, 8) ;
        String s = Integer.toHexString(v) ;
        check(0xF000, v ) ;
    }

    @Test public void testClear2()
    {
        int v = 0x80000000;
        v = BitsInt.clear(v, 31, 32) ;
        String s = Integer.toHexString(v) ;
        check(0x0, v ) ;
    }

    @Test public void testClear3()
    {
        int v = 0xC0000000;
        v = BitsInt.clear(v, 31, 32) ;
        String s = Integer.toHexString(v) ;
        check(0x40000000, v ) ;
    }

    @Test public void testClear4()
    {
        int v = -1 ;
        v = BitsInt.clear(v, 31, 32) ;
        String s = Integer.toHexString(v) ;
        check(0x7FFFFFFF, v ) ;
    }
    
    @Test public void testClear5()
    {
        int v = -1 ;
        v = BitsInt.clear(v, 16, 32) ;
        String s = Integer.toHexString(v) ;
        check(0x0000FFFF, v ) ;
    }

    @Test public void testClear6()
    {
        int v = -1 ;
        v = BitsInt.clear(v, 0, 16) ;
        String s = Integer.toHexString(v) ;
        check(0xFFFF0000, v ) ;
    }

    @Test public void testClear7()
    {
        int v = -1 ;
        v = BitsInt.clear(v, 0, 0) ;
        String s = Integer.toHexString(v) ;
        check(-1, v ) ;
    }

    @Test public void testSet1()
    {
        int v = 0x0 ;
        v = BitsInt.set(v, 0, 1) ;
        check(1, v) ;
    }
    
    @Test public void testSet2()
    {
        int v = 0x1 ;
        v = BitsInt.set(v, 0, 1) ;
        check(1, v) ;
    }
    
    @Test public void testSet3()
    {
        int v = 0xF0 ;
        v = BitsInt.set(v, 0, 1) ;
        check(0xF1, v) ;
    }
    
    @Test public void testSet4()
    {
        int v = 0xF0F0F0F0 ;
        v = BitsInt.set(v, 0, 8) ;
        check(0xF0F0F0FF, v) ;
    }

    @Test public void testSet5()
    {
        int v = 0 ;
        v = BitsInt.set(v, 16, 24) ;
        check(0x00FF0000, v) ;
    }
    
    @Test public void testSet6()
    {
        int v = 0 ;
        v = BitsInt.set(v, 31, 32) ;
        check(0x80000000, v) ;
    }
    
    @Test public void testSet7()
    {
        int v = 0 ;
        v = BitsInt.set(v, 30, 32) ;
        check(0xC0000000, v) ;
    }
    
    @Test public void testSet8()
    {
        int v = 0 ;
        v = BitsInt.set(v, 0, 32) ;
        check(-1, v) ;
    }
    
    @Test public void testSet9()
    {
        int v = 0 ;
        v = BitsInt.set(v, 10, 10) ;
        check(0, v) ;
    }
    
    @Test public void testSetBit1()
    {
        int v = 0 ;
        v = BitsInt.set(v, 0) ;
        check(1, v) ;
    }
    
    @Test public void testSetBit2()
    {
        int v = 0 ;
        v = BitsInt.set(v, 1) ;
        check(2, v) ;
    }
    
    @Test public void testSetBit3()
    {
        int v = 1 ;
        v = BitsInt.set(v, 0) ;
        check(1, v) ;
    }

    @Test public void testSetBit4()
    {
        int v = -1 ;
        v = BitsInt.set(v, 0) ;
        check(-1, v) ;
    }

    @Test public void testSetBit5()
    {
        int v = 0 ;
        v = BitsInt.set(v, 30) ;
        check(0x40000000, v) ;
    }

    @Test public void testSetBit6()
    {
        int v = 0 ;
        v = BitsInt.set(v, 31) ;
        check(0x80000000, v) ;
    }
    
    @Test public void testBitTest1()
    {
        int v = 0 ;
        assertTrue(BitsInt.test(v, false, 0)) ;
    }
    
    @Test public void testBitTest2()
    {
        int v = 1 ;
        assertTrue(BitsInt.test(v, true, 0)) ;
    }
    
    @Test public void testBitTest3()
    {
        int v = -1 ;
        assertTrue(BitsInt.test(v, true, 31)) ;
    }

    @Test public void testBitTest4()
    {
        int v = 0x7FFFFFFF ;
        assertTrue(BitsInt.test(v, false, 31)) ;
    }

    @Test public void testBitsTest1()
    {
        int v = 0x76543210 ;
        assertTrue(BitsInt.test(v, 0x0, 0, 4)) ;
    }
    
    @Test public void testBitsTest2()
    {
        int v = 0x76543210 ;
        assertTrue(BitsInt.test(v, 0x10, 0, 8)) ;
    }
    
    @Test public void testBitsTest3()
    {
        int v = 0x76543210 ;
        assertTrue(BitsInt.test(v, v, 0, 32)) ;
    }
    
    @Test public void testBitsTest4()
    {
        int v = 0x76543210 ;
        assertFalse(BitsInt.test(v, 0, 0, 32)) ;
    }
    
    @Test public void testBitsTest5()
    {
        int v = 0x76543210 ;
        assertTrue(BitsInt.test(v, 0x00543200, 8, 24)) ;
    }
    
    @Test public void testIsSet1()
    {
        int v = 0x00000010 ;
        BitsInt.isSet(v, 4) ;
        assertTrue(BitsInt.isSet(v, 4)) ;
        assertFalse(BitsInt.isSet(v, 3)) ;
        assertFalse(BitsInt.isSet(v, 5)) ;
    }
    
    @Test public void testAccess1()
    {
        int v = -1 ;
        v = BitsInt.access(v, 4, 8) ;
        check(0xF0, v ) ;
    }
    
    @Test public void testAccess2()
    {
        int v = 0x76543210 ;
        v = BitsInt.access(v, 0, 8) ;
        check(0x10, v ) ;
    }

    @Test public void testAccess3()
    {
        int v = 0x76543210 ;
        v = BitsInt.access(v, 0, 32) ;
        check(0x76543210, v ) ;
    }

    @Test public void testAccess4()
    {
        int v = 0xFEF43210 ;
        v = BitsInt.access(v, 30, 32) ;
        check(0xC0000000, v ) ;
    }

    @Test public void testAccess5()
    {
        int v = 0x76543210 ;
        v = BitsInt.access(v, 0, 2) ;
        check(0, v ) ;
    }

    @Test public void testPack1()
    {
        int v = 0 ;
        v = BitsInt.pack(v, 0xF, 0, 4) ;
        check(0xF, v ) ;
    }
    
    @Test public void testPack2()
    {
        int v = 0xF0 ;
        v = BitsInt.pack(v, 0x2, 0, 4) ;
        check(0xF2, v ) ;
    }
    
    @Test public void testPack3()
    {
        int v = -1 ;
        v = BitsInt.pack(v, 0x2, 0, 8) ;
        check(0xFFFFFF02, v ) ;
    }

    @Test public void testPack4()
    {
        int v = 0xFFFF0000 ;
        v = BitsInt.pack(v, 0x2, 8, 16) ;
        check(0xFFFF0200, v ) ;
    }

    @Test public void testPack5()
    {
        int v = 0xFFFF0000 ;
        v = BitsInt.pack(v, 0xFF, 8, 16) ;
        check(0xFFFFFF00, v ) ;
    }

    @Test public void testUnpack1()
    {
        int v = 0xABCDABCD ;
        v = BitsInt.unpack(v, 0, 4) ;
        check(0xD, v ) ;
    }
    
    @Test public void testUnpack2()
    {
        int v = 0xABCDABCD ;
        v = BitsInt.unpack(v, 31, 32) ;
        check(1, v ) ;
    }

    @Test public void testUnpack3()
    {
        int v = 0xABCDABCD ;
        v = BitsInt.unpack(v, 24, 32) ;
        check(0xAB, v ) ;
    }

    @Test public void testUnpack4()
    {
        int v = 0xAB1234CD ;
        v = BitsInt.unpack(v, 8, 24) ;
        check(0x1234, v ) ;
    }

    @Test public void testUnpackStr1()
    {
        String s = "ABCD" ;
        int v = BitsInt.unpack(s, 0, 4) ;
        check(0xABCD, v ) ;
    }
    
    @Test public void testUnpackStr2()
    {
        String s = "ABCD" ;
        int v = BitsInt.unpack(s, 2, 4) ;
        check(0xCD, v ) ;
    }
    
    @Test public void testUnpackStr3()
    {
        String s = "ABCD" ;
        int v = BitsInt.unpack(s, 0, 2) ;
        check(0xAB, v ) ;
    }
    
    private void check(int expected, int actual)
    {
        check(null, expected, actual) ;
    }
    
    private void check(String msg, int expected, int actual)
    {
        if ( expected == actual ) return ;
        String s = "Expected: "+Integer.toHexString(expected)+" : Got: "+Integer.toHexString(actual) ;
        if ( msg != null )
            s = msg+": "+s ;
        assertFalse(s, true) ;
    }
}
