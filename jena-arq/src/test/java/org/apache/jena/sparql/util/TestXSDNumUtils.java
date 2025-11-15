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

package org.apache.jena.sparql.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

public class TestXSDNumUtils {

    @Test public void canonicalDecimal_01() {
        test(0, 0, "0.0", 0, 1);
    }

    @Test public void canonicalDecimal_02() {
        test(0, 1, "0.0", 0, 1);
    }

    // Positive scale = digits after decimal point of the value.
    // Negative scale = digits extra before decimal point

    @Test public void canonicalDecimal_03() {
        test(101, 0, "101.0", 1010, 1);
    }

    @Test public void canonicalDecimal_04() {
        test(101, 1, "10.1", 101, 1);
    }

    @Test public void canonicalDecimal_05() {
        test(101, 2, "1.01", 101, 2);
    }

    @Test public void canonicalDecimal_06() {
        test(-101, -2, "-10100.0", -101000, 1);
    }

    private static void test(long inputValue, int inputScale, String expected, long expectedValue, int expectedScale) {
        BigDecimal bd1 = new BigDecimal(BigInteger.valueOf(inputValue), inputScale);
        BigDecimal bdActual = XSDNumUtils.canonicalDecimal(bd1);
        String actual = XSDNumUtils.stringForm(bdActual);
        assertEquals(expected, actual, "String");
        assertEquals(expectedValue, bdActual.unscaledValue().longValue(), "value");
        assertEquals(expectedScale, bdActual.scale(), "scale");
    }
}
