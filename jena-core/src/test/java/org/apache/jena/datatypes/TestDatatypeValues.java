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

package org.apache.jena.datatypes;

import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.datatypes.xsd.XSDDuration ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.junit.Assert ;
import org.junit.Test ;

/** Tests on values */
public class TestDatatypeValues {

    // Duration.

    @Test public void duration_01() {
        durationCompareTest(0, "PT1M", "PT1M") ;
    }

    @Test public void duration_02() {
        durationCompareTest(0, "PT1M", "P0YT1M0S") ;
    }

    @Test public void duration_03() {
        durationCompareTest(0, "P1Y", "P1Y0M") ;
    }

    @Test public void duration_04() {
        durationCompareTest(1, "P2Y", "P1Y") ;
    }

    @Test public void duration_05() {
        durationCompareTest(0, "-P2Y", "-P2Y") ;
    }

    @Test public void duration_06() {
        durationCompareTest(-1, "-P2Y", "-P1Y") ;
    }

    @Test public void duration_07() {
        durationCompareTest(-1, "P2Y", "P4Y") ;
    }

    @Test public void duration_10() {
        durationCompareTest(0, "P1Y", "P12M") ;
    }

    @Test public void duration_11() {
        durationCompareTest(0, "P2Y", "P24M") ;
    }

    @Test public void duration_12() {
        durationCompareTest(0, "P1D", "PT24H") ;
    }

    @Test public void duration_13() {
        durationCompareTest(0, "P1D", "PT1440M") ;
    }

    @Test public void duration_14() {
        durationCompareTest(0, "P1D", "PT86400S") ;
    }

    @Test public void duration_20() {
        durationCompareTest(0, "-P1D", "-PT86400S") ;
    }

    private static void durationCompareTest(int expected, String lex1, String lex2) {
        durationCompareTest(expected, lex1, XSDDatatype.XSDduration, lex2, XSDDatatype.XSDduration);
    }

    private static void durationCompareTest(int expected, String lex1, XSDDatatype dt1, String lex2, XSDDatatype dt2) {
        Node d1 = NodeFactory.createLiteral(lex1, null, XSDDatatype.XSDduration);
        Node d2 = NodeFactory.createLiteral(lex2, null, XSDDatatype.XSDduration);
        XSDDuration dur1 = (XSDDuration) d1.getLiteralValue();
        XSDDuration dur2 = (XSDDuration) d2.getLiteralValue();
        int cmp = dur1.compare(dur2) ;

        Assert.assertEquals("Compare: "+lex1+" and "+lex2, expected, cmp) ;
        if ( cmp == 0 ) {
            Assert.assertEquals("Not hash compatible: "+lex1+" and "+lex2, d1.getLiteral().getValueHashCode(), d2.getLiteral().getValueHashCode()) ;
            Assert.assertTrue("Not equal: "+lex1+" and "+lex2, d1.sameValueAs(d2)) ;
        } else {
            Assert.assertFalse("Equal: "+lex1+" and "+lex2, d1.sameValueAs(d2)) ;
        }
    }
}
