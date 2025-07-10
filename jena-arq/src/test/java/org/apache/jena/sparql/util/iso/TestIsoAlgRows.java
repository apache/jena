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

package org.apache.jena.sparql.util.iso;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetMem;
import org.apache.jena.sparql.exec.RowSetRewindable;
import org.apache.jena.sparql.resultset.ResultsCompare;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.EqualityTest;
import org.apache.jena.sparql.util.NodeUtils;

public class TestIsoAlgRows {

    @Test public void testIsoTerm_01() { testIsoTerm(":s", ":s", true); }
    @Test public void testIsoTerm_02() { testIsoTerm(":s", "'s'", false); }
    @Test public void testIsoTerm_03() { testIsoTerm("_:b0", "_:b1", true); }
    @Test public void testIsoTerm_04() { testIsoTerm("<<(_:b0 :p :o )>>", "<<(_:b1 :p :o )>>", true); }

    @Test
    public void testIsoTerm_05() {
        testIsoTerm("<<( _:b0 :p <<( _:b0 _:b0 _:b0 )>> )>>", "<<( _:b1 :p <<( _:b1 _:b1 _:b1 )>> )>>", true);
    }

    @Test
    public void testIsoTerm_06() {
        testIsoTerm("<<( _:b0 :p <<( _:b0 _:b0 _:b0 )>> )>>", "<<( _:b1 :p <<( _:b1 _:b1 _:b2 )>> )>>", false);
    }

    @Test
    public void testIsoTerm_07() {
        testIsoTerm("<<( _:b1 :p <<( _:b0 _:b0 _:b1 )>> )>>", "<<( _:b2 :p <<( _:b3 _:b3 _:b2 )>> )>>", true);
    }

    @Test public void testIsoRowSet_01() { testIsoRowSet("(rowset (?x))", "(rowset (?x))", true); }
    @Test public void testIsoRowSet_02() { testIsoRowSet("(rowset (?x))", "(rowset (?y))", false); }
    @Test public void testIsoRowSet_03() { testIsoRowSet("(rowset (?x))", "(rowset (?x ?y))", false); }
    @Test public void testIsoRowSet_04() { testIsoRowSet("(rowset (?x ?y))", "(rowset (?y ?x))", true); }

    @Test public void testIsoRowSet_10() { testIsoRowSet("(rowset (?x) (row (?x 'A')) )", "(rowset (?x) (row (?x 'A')) )", true); }
    @Test public void testIsoRowSet_11() { testIsoRowSet("(rowset (?x) (row (?x 'A')) )", "(rowset (?y) (row (?y 'A')) )", false); }
    @Test public void testIsoRowSet_12() { testIsoRowSet("(rowset (?x) (row (?x 'A')) )", "(rowset (?x) (row (?x 'A')) )", true); }
    @Test public void testIsoRowSet_13() { testIsoRowSet("(rowset (?x) (row (?x 'A')) )", "(rowset (?x) (row (?x 'A')) )", true); }

    @Test public void testIsoRowSet_14() { testIsoRowSet("(rowset (?x ?y))", "(rowset (?y ?x) (row) )", false); }
    @Test public void testIsoRowSet_15() { testIsoRowSet("(rowset (?x) (row (?x 'A')) )", "(rowset (?x) (row (?x 'A')) (row) )", false); }

    @Test public void testIsoRowSet_16() {
        testIsoRowSet("(rowset (?x ?y) (row (?x 'A') (?y 'B')) )",
                      "(rowset (?x ?y) (row (?y 'B') (?x 'A')) )",
                      true);
    }

    // Blank nodes, one row

    @Test public void testIsoRowSet_21() {
        testIsoRowSet("(rowset (?x ?y ?z) (row (?x _:b0) (?y _:b1) (?z _:b2)) )",
                      "(rowset (?x ?y ?z) (row (?x _:c0) (?y _:c1) (?z _:c2)) )",
                      true); }

    @Test public void testIsoRowSet_22() {
        testIsoRowSet("(rowset (?x ?y ?z) (row (?x _:b0) (?y _:b1) (?z _:b0)) )",
                      "(rowset (?x ?y ?z) (row (?x _:c0) (?y _:c1) (?z _:c0)) )",
                      true); }

    @Test public void testIsoRowSet_23() {
        testIsoRowSet("(rowset (?x ?y ?z) (row (?x _:b0) (?y _:b1) (?z _:b0)) )",
                      "(rowset (?x ?y ?z) (row (?x _:c0) (?y _:c1) (?z _:c2)) )",
                      false); }

    // Multiple rows
    @Test public void testIsoRowSet_30() {
        testIsoRowSet("(rowset (?x ?y) (row (?x 'A')) (row (?y 'B')) )",
                      "(rowset (?x ?y) (row (?y 'B')) (row (?x 'A')) )",
                      true);
    }

    @Test public void testIsoRowSet_31() {
        testIsoRowSet("(rowset (?x ?y) (row (?x _:b0)) (row (?y _:b0)) )",
                      "(rowset (?x ?y) (row (?x _:c0)) (row (?y _:c1)) )",
                      false);
    }

    @Test public void testIsoRowSet_32() {
        testIsoRowSet("(rowset (?x ?y) (row (?x _:b0)) (row (?y _:b0)) )",
                      "(rowset (?x ?y) (row (?y _:c1)) (row (?x _:c0)) )",
                      false);
    }

    @Test public void testIsoRowSet_33() {
        testIsoRowSet("(rowset (?x) (row (?x _:b0)) (row (?x _:b0)) )",
                      "(rowset (?x) (row (?x _:c0)) (row (?x _:c1)) )",
                      false);
    }

    @Test public void testIsoRowSet_34() {
        testIsoRowSet("(rowset (?x) (row (?x _:b0)) (row (?x _:b0)) )",
                      "(rowset (?x) (row (?x _:c0)) (row (?x _:c0)) )",
                      true);
    }

    //ResultsCompare.equalsExact

    @Test public void testIsoExactRowSet_01() {
        String x = "(rowset (?x) (row (?x _:b0)) )";
        // Re-parsed - fresh bnodes.s
        testIsoExactRowSet(x, x, false);
    }

    @Test public void testIsoExactRowSet_02() {
        String x = """
                (rowset (?x ?y)
                    (row (?x _:b0) (?y _:b1))
                    (row (?x _:b2) (?y _:b3))
                    (row (?x _:b1) (?y _:b0))
                )
           """;
        testIsoExactRowSet(x, x, false);
    }

    @Test public void testIsoExactRowSet_10() {

        String x = """
                (rowset (?x ?y)
                    (row (?x _:b0) (?y _:b1))
                    (row (?x _:b2) (?y _:b3))
                    (row (?x _:b1) (?y _:b0))
                )
           """;
        RowSetRewindable rowSet1 = SSE.parseRowSet(x).rewindable();
        RowSetRewindable rowSet2 =RowSetMem.create(rowSet1);
        rowSet1.reset();
        rowSet2.reset();
        testIsoExactRowSet(rowSet1, x, rowSet2, x, true);
    }

    @Test public void testIsoRowSet_35() {
        String x = """
                (rowset (?x ?y)
                    (row (?x _:b0) (?y _:b1))
                    (row (?x _:b2) (?y _:b3))
                    (row (?x _:b1) (?y _:b0))
                )
           """;
        testIsoRowSet(x, x, true);
    }

    // Term testing

    @Test public void testIsoRowSet_40() {
        testIsoRowSet("(rowset (?x) (row (?x <<(:s :p _:b)>>)) )",
                      "(rowset (?x) (row (?x <<(:s :p _:b)>>)) )",
                      true);
    }

    @Test public void testIsoRowSet_41() {
        testIsoRowSet("(rowset (?x ?y) (row (?x <<(:s :p _:b)>>) (?y <<(:s :p _:b)>>)) )",
                      "(rowset (?x ?y) (row (?x <<(:s :p _:b)>>) (?y <<(:s :p _:b)>>)) )",
                      true);
    }

    @Test public void testIsoRowSet_42() {
        testIsoRowSet("(rowset (?x ?y) (row (?x <<(:s :p _:b)>>) (?y <<(:s :p _:b)>>)) )",
                      "(rowset (?x ?y) (row (?x <<(:s :p _:b1)>>) (?y <<(:s :p _:b2)>>)) )",
                      false);
    }

    @Test public void testIsoRowSet_50() {
        testIsoRowSet("(rowset (?x) (row (?x <<(_:b :p <<(_:b :p _:b)>> )>>)) )",
                      "(rowset (?x) (row (?x <<(_:b :p <<(_:b :p _:b)>> )>>)) )",
                      true);
    }

    @Test public void testIsoRowSet_51() {
        testIsoRowSet("(rowset (?x) (row (?x <<(_:b :p <<(_:b :p _:b)>> )>>)) )",
                      "(rowset (?x) (row (?x <<(_:b :p <<(_:b1 :p _:b)>> )>>)) )",
                      false);
    }

    // ---- Lists of rows.
    @FunctionalInterface
    interface ListTest {
        boolean test(List<Binding> list1, List<Binding> list2);
    }

    @Test public void testIsoList_01() {
        testIsoListRows("(row (?x :x0) (?y :x1)) (row (?x :x2) (?y :x3)) ",
                        "(row (?x :x0) (?y :x1)) (row (?x :x2) (?y :x3)) ",
                        ResultsCompare::equalsExact,
                        true);
    }

    @Test public void testIsoList_02() {
        String arg1 = "(row (?x :x0) (?y :x1)) (row (?x :x2) (?y :x3))";
        String arg2 = "(row (?x :x2) (?y :x3)) (row (?x :x0) (?y :x1))";

        testIsoListRows(arg1, arg2, ResultsCompare::equalsExact, true);
        testIsoListRows(arg1, arg2, ResultsCompare::equalsExactAndOrder, false);
    }

    // ---- Machinery

    private static void testIsoTerm(String str1, String str2, boolean expected) {
        Node n1 = SSE.parseNode(str1);
        Node n2 = SSE.parseNode(str2);

        testIsoTerm(n1, str1, n2, str2, expected);
        testIsoTerm(n2, str2, n1, str1, expected);

        boolean b = equalsIsoByTerm(n1, n2);
        if ( expected != b ) {
            String msg = expected ? "(expected match)" : "(expected no match)";
            System.out.println("== ** BAD "+msg);
            System.out.println("== Term1");
            System.out.println("  "+str1);
            System.out.println("== Term2");
            System.out.println("  "+str2);
            System.out.println();
        }
        assertEquals(expected, b);
    }

    private static void testIsoTerm(Node n2, String str1, Node n1, String str2, boolean expected) {
        boolean b = equalsIsoByTerm(n1, n2);
        if ( expected != b ) {
            String msg = expected ? "(expected match)" : "(expected no match)";
            System.out.println("== ** BAD "+msg);
            System.out.println("== Term1");
            System.out.println("  "+str1);
            System.out.println("== Term2");
            System.out.println("  "+str2);
            System.out.println();
        }
        assertEquals(expected, b);
    }

    private static boolean equalsIsoByTerm(Node n1, Node n2) {
        IsoMapping mapping = IsoAlgRows.matchTermsTest(n1, n2, NodeUtils.sameNode);
        return mapping != null;
    }

    // RowSet testing.

    private static void testIsoRowSet(String str1, String str2, boolean expected) {
        RowSetRewindable rowSet1 = SSE.parseRowSet(str1).rewindable();
        RowSetRewindable rowSet2 = SSE.parseRowSet(str2).rewindable();
        // Both ways round.
        testIsoRowSet(rowSet1, str1, rowSet2, str2, expected);
        rowSet1.reset();
        rowSet2.reset();
        testIsoRowSet(rowSet2, str2, rowSet1, str1, expected);
    }

    private static void testIsoRowSet(RowSet rowSet1, String str1, RowSet rowSet2, String str2, boolean expected) {
        RowSetRewindable rsw1 = rowSet1.rewindable();
        RowSetRewindable rsw2 = rowSet2.rewindable();
        boolean b = equalsIsoByTerm(rsw1, rsw2);
        if ( expected != b ) {
            System.out.println("== ** BAD");
            System.out.println("== rowSet1");
            System.out.println(str1);
            System.out.println("== rowSet2");
            System.out.println(str2);
        }
        assertEquals(expected, b);
    }

    private static boolean equalsIsoByTerm(RowSetRewindable rowSet1, RowSetRewindable rowSet2) {
        return ResultsCompare.equalsByTerm(rowSet1, rowSet2);
    }

    private void testIsoExactRowSet(String str1, String str2, boolean expected) {
        RowSetRewindable rowSet1 = SSE.parseRowSet(str1).rewindable();
        RowSetRewindable rowSet2 = SSE.parseRowSet(str2).rewindable();
        // Both ways round.
        testIsoExactRowSet(rowSet1, str1, rowSet2, str2, expected);
        rowSet1.reset();
        rowSet2.reset();
        testIsoExactRowSet(rowSet2, str2, rowSet1, str1, expected);
    }

    private void testIsoExactRowSet(RowSet rowSet1, String str1, RowSet rowSet2, String str2, boolean expected) {
        RowSetRewindable rsw1 = rowSet1.rewindable();
        RowSetRewindable rsw2 = rowSet2.rewindable();
        EqualityTest eqtest = NodeUtils.sameRdfTerm;
        boolean b = ResultsCompare.equalsExact(rowSet1, rowSet2);
        if ( expected != b ) {
            System.out.println("== ** BAD");
            System.out.println("== rowSet1");
            rsw1.reset();
            rsw1 = IsoLib.print(rsw1);
            System.out.println("== rowSet2");
            rsw2.reset();
            rsw2 = IsoLib.print(rsw2);
        }
        assertEquals(expected, b);
    }

    // Lists

    private static List<Binding> parseRows(String str) {
        RowSet rowSet = SSE.parseRowSet("(rowset () "+str+" )");
        Binding[] x = rowSet.stream().toArray(Binding[]::new);
        return List.of(x);  // Immutable, no nulls.
    }

    private static void testIsoListRows(String str1, String str2, ListTest predicate, boolean expected) {
        List<Binding> rows1 = parseRows(str1);
        List<Binding> rows2 = parseRows(str2);
        testIsoListRows(rows1, str1, rows2, str2, predicate, expected);
        testIsoListRows(rows2, str2, rows1, str1, predicate, expected);
    }

    private static void testIsoListRows(List<Binding> rows1, String str1, List<Binding> rows2, String str2, ListTest predicate, boolean expected) {
        boolean b = predicate.test(rows1, rows2);
        if ( expected != b ) {
            String msg = expected ? "(expected match)" : "(expected no match)";
            System.out.println("== ** BAD "+msg);
            System.out.println("== Row list 1");
            System.out.println("  "+str1);
            System.out.println("== Row list 12");
            System.out.println("  "+str2);
            System.out.println();
        }
        assertEquals(expected, b);
    }
}
