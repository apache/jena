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

package org.apache.jena.sparql.expr;

import static org.junit.Assert.fail;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

/** Testing sort ordering.
 * See also {@link TestComparison} and {@link TestOrdering}.
 */
public class TestSortOrdering {
    @Test public void ordering_01() {
        String s1 = "98800.0e0";
        String s2 = "206000.0e0";
        String s3 = "'49.2'^^<http://dbpedia.org/datatype/kilometre>";
        test(s1, s2, s3);
    }

    @Test public void ordering_02() {
        String s1 = "'9'";  // String
        String s2 = "+9.0"; // Decimal, sign first
        String s3 = "9.0";  // Decimal, digit first
        test(s1, s2, s3);
    }

    @Test public void ordering_03() {
        String s1 = "'9'";  // String
        String s2 = "09.0"; // Decimal, zero first
        String s3 = "9.0";  // Decimal, digit first
        test(s1, s2, s3);
    }

    // Bad
    @Test public void ordering_03a() {
        String s1 = "'9'";  // String
        String s2 = "09.0"; // Decimal, zero first
        String s3 = "9.0";  // Decimal, digit first
        // Same as 03 but reordered arguments.
        testNotOrdered(s1, s3, s2);
    }

    @Test public void ordering_04() {
        String s1 = "98800";
        String s2 = "98800.0";
        String s3 = "98800e0";
        // Same value, so lexical forms "Dot before e"
        test(s1, s2, s3);
    }

    @Test public void ordering_10() {
        String s1 = "<uri1>";
        String s2 = "<uri2>";
        String s3 = "<uri3>";
        test(s1, s2, s3);
    }

    @Test public void ordering_11() {
        String s1 = "_:b";
        String s2 = "<uri2>";
        String s3 = "'string'";
        test(s1, s2, s3);
    }

    // ValueSpaces:
    //  String > lang > numbers > boolean > date/time > duration > quoted triple
    @Test public void ordering_12() {
        String s1 = "'a'";
        String s2 = "'a'@en";
        String s3 = "'a'@fr";
        test(s1, s2, s3);
    }

    @Test public void ordering_13() {
        String s1 = "'a'@EN";
        String s2 = "'a'@En";
        String s3 = "'a'@en";
        test(s1, s2, s3);
    }

    @Test public void ordering_20() {
        String s1 = "<<:s1 :p :o>>";
        String s2 = "<<:s2 :p :o>>";
        String s3 = "<<:s3 :p :o>>";
        test(s1, s2, s3);
    }

    @Test public void ordering_21() {
        String s1 = "<<:s :p :o>>";
        String s2 = "<<:s :p :o1>>";
        String s3 = "<<:s :p1 :o1>>";
        test(s1, s2, s3);
    }

    @Test public void ordering_22() {
        String s1 = "<<:s :p 1>>";
        String s2 = "<<:s :p 2>>";
        String s3 = "<<:s :p 3>>";
        test(s1, s2, s3);
    }

    @Test public void ordering_23() {
        String s1 = "<<:s :p '1'>>";
        String s2 = "<<:s :p '1'@en>>";
        String s3 = "<<:s :p 1>>";
        test(s1, s2, s3);
    }

    @Test public void ordering_50() {
        String s1 = "'1'";
        String s2 = "'1'@fr";
        String s3 = "1";
        test(s1, s2, s3);
    }

    @Test public void ordering_51() {
        String s1 = "'1'";
        String s2 = "'1'^^xsd:boolean";
        String s3 = "true";
        test(s1, s2, s3);
    }

    @Test public void ordering_52() {
        String s1 = "'1'";
        String s2 = "'2022-12-28T10:11:12'^^xsd:dateTime";
        String s3 = "'PT0S'^^xsd:duration";
        test(s1, s2, s3);
    }

    @Test public void ordering_53() {
        String s1 = "'2022-12-28T10:11:12'^^xsd:dateTime";
        String s2 = "'3033-12-28T10:11:12'^^xsd:dateTime";
        String s3 = "<<:s :p :o>>";
        test(s1, s2, s3);
    }

    // Times and dates

    // Sort, unconditional, ordering.
    // This tries arg1 arg2 and arg3 in all 6 orders.
    // One must be A <= B, B <= C and A <= C
    // One must be A >= B, B >= C and A >= C
    // In addition the arguments must be in A <= B <= C order.

    private static void test(String string1, String string2, String string3) {
        test(string1, string2, string3, true);
    }

    private static void testNotOrdered(String string1, String string2, String string3) {
        test(string1, string2, string3, false);
    }

    private static void test(String string1, String string2, String string3, boolean isGoodOrder) {
        NodeValue nv1 = nodeValue(string1);
        NodeValue nv2 = nodeValue(string2);
        NodeValue nv3 = nodeValue(string3);
        test(nv1, nv2, nv3);

        // And this should be <= order
        int x12 = NodeValueCmp.compareWithOrdering(nv1, nv2);
        int x23 = NodeValueCmp.compareWithOrdering(nv2, nv3);
        int x13 = NodeValueCmp.compareWithOrdering(nv1, nv3);

        boolean isTransitiveLE = isLE(x12) && isLE(x23) && isLE(x13);

        if ( isGoodOrder && ! isTransitiveLE ) {
            String str = String.format("Failed (not transitive)   %s :: %s :: %s\n", nvStr(nv1), nvStr(nv2), nvStr(nv3));
            fail(str);
        }
    }

    /**
     * Check the sorting comparison for {@link NodeValue NodeValues}.
     * For any three values, check that comparison transitive relationship requirement:
     * <br/>
     * {@code If A <= B and B <= C then A <= C}
     * <br/>
     * {@code If A >= B and B >= C then A >= C}
     * <p>
     * See {@link Comparable}.
     */
    static void test(NodeValue nv1, NodeValue nv2, NodeValue nv3) {
        int x = 0;
        boolean b = false;

        b = testWorker(nv1, nv2, nv3);
        if ( b ) x++;

        b = testWorker(nv1, nv3, nv2);
        if ( b ) x++;

        b = testWorker(nv2, nv3, nv1);
        if ( b ) x++;

        b = testWorker(nv2, nv1, nv3);
        if ( b ) x++;

        b = testWorker(nv3, nv1, nv2);
        if ( b ) x++;

        b = testWorker(nv3, nv2, nv1);
        if ( b ) x++;

        // Equality means may be more than 2
        if ( x != 2 )
            fail(String.format("BAD  ** %s :: %s :: %s\n", nvStr(nv1), nvStr(nv2), nvStr(nv3)));
    }

    /*
     * Test, return true if a transitive order of arguments holds (either "less than or equals" or "greater than or equal").
     */
    static boolean testWorker(NodeValue nv1, NodeValue nv2, NodeValue nv3) {
        int x12 = NodeValueCmp.compareWithOrdering(nv1, nv2);
        int x23 = NodeValueCmp.compareWithOrdering(nv2, nv3);
        int x13 = NodeValueCmp.compareWithOrdering(nv1, nv3);

        if ( isLE(x12) ) {
            if ( isLE(x23) ) {
                if ( ! isLE(x13) )
                    fail(String.format("** not LE ** (%s %s %s) :: 1-2 %s\n2-3 %s\n1-3 %s\n", nvStr(nv1), nvStr(nv2), nvStr(nv3), compStr(x12), compStr(x23), compStr(x13)));
            }
        }
        else if ( isGE(x12) ) {
            if ( isGE(x23) ) {
                if ( ! isGE(x13) )
                    fail(String.format("** not GE ** (%s %s %s) :: 1-2 %s\n2-3 %s\n1-3 %s\n", nvStr(nv1), nvStr(nv2), nvStr(nv3), compStr(x12), compStr(x23), compStr(x13)));
            }
        }

        return ( isLE(x12) && isLE(x23) && isLE(x13) ) ||
               ( isGE(x12) && isGE(x23) && isGE(x13) ) ;
    }

    private static boolean isLE(NodeValue nv1, NodeValue nv2) {
        return isLE(NodeValueCmp.compareWithOrdering(nv1, nv2));
    }

    private static boolean isGE(NodeValue nv1, NodeValue nv2) {
        return isGE(NodeValueCmp.compareWithOrdering(nv1, nv2));
    }

    private static boolean isLE(int x) {
        return ( Expr.CMP_EQUAL == x ) || ( Expr.CMP_LESS == x ) ;
    }

    private static boolean isGE(int x) {
        return ( Expr.CMP_EQUAL == x )|| ( Expr.CMP_GREATER == x ) ;
    }

    public static NodeValue nodeValue(String str) {
        Node n = SSE.parseNode(str);
        NodeValue nv = NodeValue.makeNode(n);
        return nv;
    }

    public static String compStr(int x) {
        String s = "("+String.format("%+d",x)+") ";

        if ( x == Expr.CMP_GREATER ) return s+">";
        if ( x == Expr.CMP_EQUAL) return s+"=";
        if ( x == Expr.CMP_LESS ) return s+"<";
        if ( x == Expr.CMP_UNEQUAL ) return s+"!=";
        if ( x == Expr.CMP_INDETERMINATE ) return s+"?";
        return "*";
    }

    private static PrefixMap prefixMap = PrefixMapFactory.create(PrefixMapping.Standard);
    private static SerializationContext sCxt = new SerializationContext(PrefixMapping.Standard);
    private static String nvStr(NodeValue nv) {
        return nv.asQuotedString(sCxt);
    }
}
