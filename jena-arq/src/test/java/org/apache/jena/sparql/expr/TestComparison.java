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

import static org.apache.jena.sparql.expr.Expr.CMP_EQUAL;
import static org.apache.jena.sparql.expr.Expr.CMP_INDETERMINATE;
import static org.apache.jena.sparql.expr.Expr.CMP_LESS;
import static org.apache.jena.sparql.expr.Expr.CMP_UNEQUAL;
import static org.junit.Assert.assertEquals;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

/**
 * Comparison tests - not sorting
 * See also {@link TestSortOrdering} and {@link TestOrdering}.
 */
public class TestComparison {

    @Test public void compare_01() { compare("<x:p1>", "<x:p1>", CMP_EQUAL ); }

    @Test public void compare_02() { compare("<x:p1>", "<x:p2>", CMP_UNEQUAL); }

    @Test(expected=ExprNotComparableException.class)
    public void compare_03() { compare("'abc'@en", "<x:p1>", CMP_INDETERMINATE); }

    @Test(expected=ExprNotComparableException.class)
    public void compare_04() { compare("'abc'@en", "'abc'", CMP_INDETERMINATE); }

    @Test public void compare_dt_01() { compare("'2022'^^xsd:gYear", "'2023'^^xsd:gYear", CMP_LESS); }

    @Test(expected=ExprNotComparableException.class)
    public void compare_dt_02() { compare("'2005-10-14Z'^^xsd:date", "'2005-10-14T14:09:43Z'^^xsd:dateTime", CMP_LESS); }

    @Test public void compare_duration_01() { compare("'PT0S'^^xsd:duration", "'PT1S'^^xsd:duration", CMP_LESS); }
    @Test public void compare_duration_02() { compare("'PT0S'^^xsd:duration", "'P0Y'^^xsd:duration", CMP_INDETERMINATE); }
    @Test public void compare_duration_03() { compare("'PT0S'^^xsd:duration", "'P0MT0S'^^xsd:duration", CMP_EQUAL); }

    private static void compare(String string1, String string2, int expected) {
        compare(nodeValue(string1), nodeValue(string2), expected);
    }

    private static void compare(NodeValue nv1, NodeValue nv2, int expected) {
        try {
            int cmp = NodeValueCmp.compareByValue(nv1, nv2);
            assertEquals(expected, cmp);
        } catch (ExprNotComparableException ex) {
            throw ex;
        }
    }

//    // Development support.
//    private static void compare(String string1, String string2) {
//        compare(nodeValue(string1), nodeValue(string2));
//    }
//
//    private static void compare(NodeValue nv1, NodeValue nv2) {
//        System.out.println("== Always");
//        try {
//            if ( true ) throw new RuntimeException("Switch to sorting branch");
//            int cmp = -99;
//            //int cmp = NodeValueCmp.compareAlways(nv1, nv2);
//            System.out.printf("%s   %s :: %s\n", compStr(cmp), nvStr(nv1), nvStr(nv2));
//        } catch (ExprNotComparableException ex) {
//            System.out.printf("Not   %s :: %s\n", nvStr(nv1), nvStr(nv2));
//            System.out.println("ExprNotComparableException: "+ex.getMessage());
//        } catch (ExprEvalException ex) {
//            System.out.printf("Err   %s :: %s\n", nvStr(nv1), nvStr(nv2));
//        }
//
//        System.out.println("== Value");
//        try {
//            if ( true ) throw new RuntimeException("Switch to sorting branch");
//            //int cmp2 = NodeValueCmp.compareByValue(nv1, nv2);
//            int cmp2 = -99;
//            System.out.printf("%s   %s :: %s\n", compStr(cmp2), nvStr(nv1), nvStr(nv2));
//        } catch (ExprNotComparableException ex) {
//            System.out.printf("Not   %s :: %s\n", nvStr(nv1), nvStr(nv2));
//            System.out.println("ExprNotComparableException: "+ex.getMessage());
//        } catch (ExprEvalException ex) {
//            System.out.printf("Err   %s :: %s\n", nvStr(nv1), nvStr(nv2));
//        }
//        System.out.println();
//    }

    private static PrefixMap prefixMap = PrefixMapFactory.create(PrefixMapping.Standard);
    private static SerializationContext sCxt = new SerializationContext(PrefixMapping.Standard);

    public static String nvStr(NodeValue nv) {
        return nv.asQuotedString(sCxt);
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

    public static NodeValue nodeValue(String str) {
        Node n = SSE.parseNode(str);
        NodeValue nv = NodeValue.makeNode(n);
        return nv;
    }
}
