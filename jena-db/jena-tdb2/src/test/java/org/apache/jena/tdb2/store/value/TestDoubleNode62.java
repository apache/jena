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

package org.apache.jena.tdb2.store.value;

import org.apache.jena.atlas.lib.BitsLong;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestDoubleNode62 {
    // See also TestNodeIdInline.nodeId_double_*
    @Test public void double_01() { testRoundTripDouble(1d); }
    @Test public void double_02() { testRoundTripDouble(-1d); }
    @Test public void double_03() { testRoundTripDouble(-1111111111e50d); }
    @Test public void double_04() { testRoundTripDouble(1111111111e50d); }

    @Test public void double_05() { testNoEncoding(1e300); }
    @Test public void double_06() { testNoEncoding(1e100); }
    @Test public void double_07() { testNoEncoding(1e78); }
    @Test public void double_08() { testNoEncoding(2e77); }
    @Test public void double_09() { testRoundTripDouble(1e77); }

    @Test public void double_10() { testNoEncoding(3e-300); }
    @Test public void double_11() { testNoEncoding(3e-100); }
    @Test public void double_12() { testNoEncoding(3e-77); }
    @Test public void double_13() { testRoundTripDouble(3.5e-77); }
    @Test public void double_14() { testRoundTripDouble(4e-77); }
    @Test public void double_15() { testRoundTripDouble(1e-76); }
    @Test public void double_16() { testRoundTripDouble(1e-75); }

    @Test public void double_20() { testRoundTripDouble(Double.POSITIVE_INFINITY); }
    @Test public void double_21() { testRoundTripDouble(Double.NEGATIVE_INFINITY); }
    @Test public void double_22() { testRoundTripDouble(Double.NaN); }
    @Test public void double_23() { testNoEncoding(Double.MAX_VALUE); }
    @Test public void double_24() { testNoEncoding(Double.MIN_NORMAL); }
    // Despite being out of the normal range of DoubleNode62,
    // this does encode and round trip even though out of range.
    // Its encoding is long value 1.
    @Test public void double_25() { testRoundTripDouble(Double.MIN_VALUE); }

    @Test public void double_30() { testRoundTripDouble(DoubleNode62.POSITIVE_INFINITY); }
    @Test public void double_31() { testRoundTripDouble(DoubleNode62.NEGATIVE_INFINITY); }
    @Test public void double_32() { testRoundTripDouble(DoubleNode62.NaN); }

    @Test public void double_33() { testRoundTripDouble(DoubleNode62.MAX_VALUE); }
    @Test public void double_34() { testRoundTripDouble(DoubleNode62.MIN_NORMAL); }
    @Test public void double_35() { testRoundTripDouble(DoubleNode62.MIN_VALUE); }

    @Test public void double_40() { sameValue(DoubleNode62.POSITIVE_INFINITY, Double.POSITIVE_INFINITY); }
    @Test public void double_41() { sameValue(DoubleNode62.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY); }
    @Test public void double_42() { sameValue(DoubleNode62.NaN, Double.NaN); }

    @Test public void double_50() { testConst(DoubleNode62.POSITIVE_INFINITY_BITS, 0x1ff0000000000000L); }
    @Test public void double_51() { testConst(DoubleNode62.NEGATIVE_INFINITY_BITS, 0x3ff0000000000000L); }
    @Test public void double_52() { testConst(DoubleNode62.NaN_BITS, 0x1ff8000000000000L); }
    @Test public void double_53() { testConst(DoubleNode62.MAX_VALUE_BITS,  0x3fefffffffffffffL); }
    @Test public void double_54() { testConst(DoubleNode62.MIN_NORMAL_BITS, 0x0010000000000000L); }
    @Test public void double_55() { testConst(DoubleNode62.MIN_VALUE_BITS,  0x01L); }

    private void sameValue(double d1, double d2) {
        // Not d1 == d2 because NaN != NaN
        assertEquals(Double.valueOf(d1), Double.valueOf(d2));
    }

    private static void testConst(long x, long expected) {
        assertEquals(expected, x);
        double d = DoubleNode62.unpack(x);
        long z = DoubleNode62.pack(d);
        assertEquals(expected, z);
    }

    private void testNoEncoding(double d) {
        long x = DoubleNode62.pack(d);
        assertEquals("Expected no encoding", x, DoubleNode62.NO_ENCODING);
    }

    private static void testRoundTripDouble(double d) {
        long x = DoubleNode62.pack(d);
        assertNotEquals("Expected encoding", x, DoubleNode62.NO_ENCODING);
        double d2 = DoubleNode62.unpack(x);
        assertEquals(d, d2, 0);
    }

    private static void print(long x) {
        long z[] = new long[4];
        for ( int i = 0; i < 4 ; i++ ) {
             z[3-i] = BitsLong.unpack(x, i*16, (i+1)*16);
        }
        System.out.printf("0x%04X %04X %04X %04X\n", z[0], z[1], z[2], z[3]);
    }
}
