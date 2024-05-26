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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestFloatNode {
    // Floats can always be encoded.
    // See also TestNodeIdInline.nodeId_float_*
    @Test public void float_01() { testRoundTripFloat(1f); }
    @Test public void float_02() { testRoundTripFloat(-1f); }
    @Test public void float_03() { testRoundTripFloat(-1111111111e20f); }
    @Test public void float_04() { testRoundTripFloat(1111111111e20f); }

    @Test public void float_10() { testRoundTripFloat(Float.POSITIVE_INFINITY); }
    @Test public void float_11() { testRoundTripFloat(Float.NEGATIVE_INFINITY); }
    @Test public void float_12() { testRoundTripFloat(Float.NaN); }
    @Test public void float_13() { testRoundTripFloat(Float.MAX_VALUE); }
    @Test public void float_14() { testRoundTripFloat(Float.MIN_NORMAL); }
    @Test public void float_15() { testRoundTripFloat(Float.MIN_VALUE); }

    private static void testRoundTripFloat(float f) {
        long x0 = Float.floatToRawIntBits(f);
        long x = FloatNode.pack(f);
        // No high part.
        assertTrue( (x & 0xFFFFFFFF00000000L) == 0 );
        float f2 = FloatNode.unpack(x);
        assertEquals(f, f2, 0);
    }
}
