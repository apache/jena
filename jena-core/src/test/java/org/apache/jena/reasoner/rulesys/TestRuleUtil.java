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

package org.apache.jena.reasoner.rulesys;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

public class TestRuleUtil {
    @Test public void cmp1() {
        compare("1", XSDDatatype.XSDdecimal, "2", XSDDatatype.XSDdecimal, -1);
    }

    // JENA-1583
    @Test public void cmp2() {
        compare("1", XSDDatatype.XSDdecimal, "1.5", XSDDatatype.XSDdecimal, -1);
    }


    // JENA-1583
    @Test public void cmp3() {
        compare("10000000000000000000000000000000", XSDDatatype.XSDinteger, "10000000000000000000000000000000", XSDDatatype.XSDdecimal, 0);
    }

    @Test public void cmp4() {
        compare("10000000000000000000000000000000", XSDDatatype.XSDdecimal, "10000000000000000000000000000000.1", XSDDatatype.XSDdecimal, -1);
    }

    @Test public void cmp5() {
        compare("10000000000000000000000000000000.1", XSDDatatype.XSDdecimal, "10000000000000000000000000000000", XSDDatatype.XSDdecimal, +1);
    }
    
    private void compare(String lex1, XSDDatatype dt1, String lex2, XSDDatatype dt2, int outcome) {
        Node n1 = NodeFactory.createLiteral(lex1, dt1);
        Node n2 = NodeFactory.createLiteral(lex2, dt2);
        int z = Util.compareNumbers(n1, n2);
        assertEquals(outcome, z);
    }
    
    // Directly test the number comparision code.  
    
    private void compare(Number num1, Number num2, int outcome) {
        int z1 = Util.compareNumbers(num1, num2);
        assertEquals("compare(num1,num2)", outcome, z1);
        // reverse
        int z2 = Util.compareNumbers(num2, num1);
        assertEquals("compare(num2,num1)", outcome, -1 * z2);
        
    }

    @Test public void cmp_num1() {
        compare(new BigDecimal("1"), new BigDecimal("1.0"), 0);
    }

    @Test public void cmp_num2() {
        compare(new BigDecimal("1"), new BigDecimal("1.5"), -1);
    }


    @Test public void cmp_num3() {
        compare(new BigDecimal("10000000000000000000000000000000"), new BigDecimal("10000000000000000000000000000000.0"), 0);
    }

    @Test public void cmp_num4() {
        compare(new BigDecimal("10000000000000000000000000000000"), new BigDecimal("10000000000000000000000000000000.00000000000000000001"), -1);
    }

    @Test public void cmp_num5() {
        compare(new BigInteger("10000000000000000000000000000000"), new BigDecimal("0.00000000000000000001"), +1);
    }

    @Test public void cmp_num10() {
        compare(Long.valueOf("1"), new BigDecimal("1.5"), -1);
    }

    @Test public void cmp_num11() {
        compare(Long.valueOf(Long.MIN_VALUE+100), Double.parseDouble("1.5e0"), -1);
    }

    @Test public void cmp_num12() {
        compare(Byte.valueOf("1"), Double.parseDouble("1.5"), -1);
    }
    
    @Test public void cmp_num13() {
        compare(Double.parseDouble("-0.5"), Byte.valueOf("-1"), 1);
    }


}
