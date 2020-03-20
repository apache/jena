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

package org.apache.jena.sparql.algebra.optimize;

import org.apache.jena.atlas.lib.StrUtils;
import org.junit.Assert;
import org.junit.Test;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.sse.SSE;

/**
 * Tests for the {@link TransformEliminateAssignments}
 * 
 */
public class TestTransformEliminateAssignments {

    private void test(String input, String... output) {
        test(input, false, output);
    }

    private void test(String input, boolean aggressive, String... output) {
        Op original = SSE.parseOp(input);
        test(original, aggressive, output);
    }

    private void test(Op original, boolean aggressive, String... output) {
        // Transform
        Op actual = TransformEliminateAssignments.eliminate(original, aggressive);

        // Check results
        if (output == null) {
            // No transformation.
            Assert.assertEquals(original, actual);
        } else {
            // Transformation expected
            Op expected = SSE.parseOp(StrUtils.strjoinNL(output));
            Assert.assertEquals(expected, actual);
        }
    }

    private void testNoChange(String... input) {
        testNoChange(false, input);
    }

    private void testNoChangeAggressive(String... input) {
        testNoChange(true, input);
    }

    private void testNoChange(boolean aggressive, String... input) {
        test(StrUtils.strjoinNL(input), aggressive, (String[]) null);
    }

    @Test
    public void unused_01() {
        // Assignments never used can be eliminated
        // However we must be inside a projection as otherwise the assigned
        // variable would be visible and we couldn't eliminate the assignment
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?y)",
                                "  (extend (?x true)",
                                "    (table unit)))"),
             "(project (?y)",
             "  (table unit))");
        //@formatter:on
    }

    @Test
    public void unused_02() {
        // Assignments never used can be eliminated
        // However we must be inside a projection as otherwise the assigned
        // variable would be visible and we couldn't eliminate the assignment
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?y)",
                                "  (extend ((?x true) (?y false))",
                                "    (table unit)))"),
             "(project (?y)",
             "  (extend (?y false)",
             "    (table unit)))");
        //@formatter:on
    }

    @Test
    public void filter_01() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // However we must be inside a projection as otherwise the assigned
        // variable would be visible and we couldn't eliminate the assignment
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?y)",
                                "  (filter (exprlist ?x)",
                                "    (extend (?x true)",
                                "      (table unit))))"),
             "(project (?y)",
             "  (filter (exprlist true)",
             "    (table unit)))");
        //@formatter:on
    }

    @Test
    public void filter_02() {
        // Assignment for ?y can be removed because it is never used
        // Assignment for ?x can be in-lined
        // However we must be inside a projection as otherwise the assigned
        // variable would be visible and we couldn't eliminate the assignment
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?z)",
                                "  (filter (exprlist ?x)",
                                "    (extend ((?x true) (?y false))",
                                "      (table unit))))"),
             "(project (?z)",
             "  (filter (exprlist true)",
             "    (table unit)))");
        //@formatter:on
    }

    @Test
    public void extend_01() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // However we must be inside a projection as otherwise the assigned
        // variable would be visible and we couldn't eliminate the assignment
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?y)",
                                "  (extend ((?x true) (?y ?x))",
                                "    (table unit)))"),
             "(project (?y)",
             "  (extend (?y true)",
             "    (table unit)))");
        //@formatter:on
    }

    @Test
    public void extend_02() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // However we must be inside a projection as otherwise the assigned
        // variable would be visible and we couldn't eliminate the assignment
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?z)",
                                "  (extend ((?x true) (?y ?x) (?z ?y))",
                                "    (table unit)))"),
             "(project (?z)",
             "  (extend (?z true)",
             "    (table unit)))");
        //@formatter:on
    }

    @Test
    public void extend_03() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // However we must be inside a projection as otherwise the assigned
        // variable would be visible and we couldn't eliminate the assignment
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?z)",
                                "  (extend ((?a true) (?b ?a) (?c false) (?d ?c) (?z (|| ?b ?d)))",
                                "    (table unit)))"),
             "(project (?z)",
             "  (extend (?z (|| true false))",
             "    (table unit)))");
        //@formatter:on
    }

    @Test
    public void orderby_01() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // However we must be inside a projection as otherwise the assigned
        // variable would be visible and we couldn't eliminate the assignment
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?y)",
                                "  (order (?x)",
                                "    (extend (?x true)",
                                "      (table unit))))"),
             "(project (?y)",
             "  (order (true)",
             "    (table unit)))");
        //@formatter:on
    }

    @Test
    public void orderby_02() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // BUT we won't do this by default for complex expressions where they
        // are used in a place where they could be evaluated multiple times
        //@formatter:off
        testNoChange(StrUtils.strjoinNL("(project (?y)",
                                        "  (order (?x)",
                                        "    (extend (?x (contains 'foo' 'bar'))",
                                        "      (table unit))))"));
        //@formatter:on
    }

    @Test
    public void orderby_03() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // BUT we won't do this by default for complex expressions where they
        // are used in a place where they could be evaluated multiple times
        // EXCEPT if we are doing aggressive in-lining
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?y)",
                                "  (order (?x)",
                                "    (extend (?x (contains 'foo' 'bar'))",
                                "      (table unit))))"),
             true,
             "(project (?y)",
             "  (order ((contains 'foo' 'bar'))",
             "    (table unit)))");
        //@formatter:on
    }

    @Test
    public void filter_unstable_01() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // EXCEPT if the expression is unstable in which case we leave it alone
        //@formatter:off
        testNoChange(StrUtils.strjoinNL("(project (?y)",
                                        "  (filter (exprlist ?x)",
                                        "    (extend (?x (rand))",
                                        "      (table unit))))"));
        //@formatter:on
    }

    @Test
    public void filter_unstable_02() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // EXCEPT if the expression is unstable in which case we leave it alone
        //@formatter:off
        testNoChange(StrUtils.strjoinNL("(project (?y)",
                                        "  (filter (exprlist ?x)",
                                        "    (extend (?x (uuid))",
                                        "      (table unit))))"));
        //@formatter:on
    }

    @Test
    public void filter_unstable_03() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // EXCEPT if the expression is unstable in which case we leave it alone
        //@formatter:off
        testNoChange(StrUtils.strjoinNL("(project (?y)",
                                        "  (filter (exprlist ?x)",
                                        "    (extend (?x (struuid))",
                                        "      (table unit))))"));
        //@formatter:on
    }

    @Test
    public void filter_unstable_04() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // EXCEPT if the expression is unstable in which case we leave it alone
        //@formatter:off
        testNoChange(StrUtils.strjoinNL("(project (?y)",
                                        "  (filter (exprlist ?x)",
                                        "    (extend (?x (bnode))",
                                        "      (table unit))))"));
        //@formatter:on
    }

    @Test
    public void orderby_unstable_01() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // EXCEPT if the expression is unstable in which case we leave it alone
        //@formatter:off
        testNoChangeAggressive(StrUtils.strjoinNL("(project (?y)",
                                                  "  (order (?x)",
                                                  "    (extend (?x (rand))",
                                                  "      (table unit))))"));
        //@formatter:on
    }

    @Test
    public void orderby_unstable_02() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // EXCEPT if the expression is unstable in which case we leave it alone
        //@formatter:off
        testNoChangeAggressive(StrUtils.strjoinNL("(project (?y)",
                                                  "  (order (?x)",
                                                  "    (extend (?x (uuid))",
                                            "      (table unit))))"));
        //@formatter:on
    }

    @Test
    public void orderby_unstable_03() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // EXCEPT if the expression is unstable in which case we leave it alone
        //@formatter:off
        testNoChangeAggressive(StrUtils.strjoinNL("(project (?y)",
                                                  "  (order (?x)",
                                                  "    (extend (?x (struuid))",
                                                  "      (table unit))))"));
        //@formatter:on
    }

    @Test
    public void orderby_unstable_04() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // EXCEPT if the expression is unstable in which case we leave it alone
        //@formatter:off
        testNoChangeAggressive(StrUtils.strjoinNL("(project (?y)",
                                                  "  (order (?x)",
                                                  "    (extend (?x (bnode))",
                                                  "      (table unit))))"));
        //@formatter:on
    }

    @Test
    public void ineligible_01() {
        // Cannot eliminate as there is no projection so the assigned variable
        // is visible even though in the algebra given it is used only once
        //@formatter:off
        testNoChange("(filter (exprlist ?x)",
                     "  (extend (?x true)",
                     "    (table unit)))");
        //@formatter:on
    }

    @Test
    public void ineligible_02() {
        // Cannot eliminate as there is no projection so the assigned variable
        // is visible even though in the algebra given it is used only once
        //@formatter:off
        testNoChange("(filter (exprlist ?x)",
                     "  (extend ((?x true) (?y false))",
                     "    (table unit)))");
        //@formatter:on
    }

    @Test
    public void ineligible_03() {
        // As the assigned variable is used multiple times we leave the
        // assignment alone
        //@formatter:off
        testNoChange("(project (?y)",
                     "  (filter (> (* ?x ?x) 16)",
                     "    (extend (?x 3)",
                     "      (table unit))))");
        //@formatter:on
    }

    @Test
    public void ineligible_04() {
        // Because the value of the assignment is used in multiple places we
        // leave the assignment alone
        //@formatter:off
        testNoChange("(project (?y)",
                     "  (filter (exprlist ?x)",
                     "    (join",
                     "      (extend (?x true)",
                     "        (table unit))",
                     "      (bgp (triple ?x ?y ?z)))))");
        //@formatter:on
    }

    @Test
    public void ineligible_05() {
        // Don't inline if we'd move it from within a n-ary operator as doing so
        // may change the queries semantics
        //@formatter:off
        testNoChange("(project (?s)",
                    "  (filter (exprlist ?x)",
                    "    (union",
                    "      (bgp (triple ?s ?p ?o))",
                    "      (extend (?x true)",
                    "        (table unit)))))");
        //@formatter:on
    }

    @Test
    public void ineligible_06() {
        // Don't inline if we'd move it from within a n-ary operator as doing so
        // may change the queries semantics
        //@formatter:off
        testNoChange("(project (?s)",
                    "  (filter (exprlist ?x)",
                    "    (leftjoin",
                    "      (bgp (triple ?s ?p ?o))",
                    "      (extend (?x true)",
                    "        (table unit)))))");
        //@formatter:on
    }

    @Test
    public void ineligible_07() {
        // Don't inline if we'd move it from within a n-ary operator as doing so
        // may change the queries semantics
        //@formatter:off
        testNoChange("(project (?s)",
                    "  (filter (exprlist ?x)",
                    "    (minus",
                    "      (bgp (triple ?s ?p ?o))",
                    "      (extend (?x true)",
                    "        (table unit)))))");
        //@formatter:on
    }

    @Test
    public void ineligible_08() {
        // Don't inline if we'd move it from within a n-ary operator as doing so
        // may change the queries semantics
        // Technically a trivial case like this is probably safe to inline but
        // the reality is that the assignment expression may depend on the
        // behaviour of one branch and so it is safer to not inline
        //@formatter:off
        testNoChange("(project (?s)",
                    "  (filter (exprlist ?x)",
                    "    (join",
                    "      (bgp (triple ?s ?p ?o))",
                    "      (extend (?x true)",
                    "        (table unit)))))");
        //@formatter:on
    }

    @Test
    public void ineligible_09() {
        // We can't inline if the assignment will be projected out
        //@formatter:off
        testNoChange("(project (?x ?y)",
                     "  (filter (exprlist ?x)",
                     "    (extend (?x true)",
                     "      (bgp (triple ?y <urn:pred> <urn:obj>)))))");
        //@formatter:on
    }

    // Test not correct.
    // The "(extend (?x ...))" is not used - it does not cause ?x to come out of (exists).
    // This would be true regardless of the (project) but the current code is only
    // triggered by (project)

    // OLD - wrong (new walker correctly waks into the exist). 
//    @Test
//    public void ineligible_10() {
//        // We can't inline out of an EXISTS since the assignment isn't projected
//        // out anyway
//        //@formatter:off
//        testNoChange("(project (?y)",
//                     "  (filter (exprlist ?x (exists",
//                     "                         (extend (?x true)",
//                     "                           (table unit))))",
//                     "    (table unit)))");
//        //@formatter:on
//    }
    
    // Checks the right output though for the wrong reason. 
//    @Test
//    public void eligible_10() {
//        // The "(extend (?x ...)" is not used - it does not com eout of the (exists).
//        // This would be true reagrdless of the (project) but the current code is only
//        // triggered by (project)
//        //@formatter:off
//        test(StrUtils.strjoinNL("(project (?y)",
//                                "  (filter (exprlist ?x (exists",
//                                "                         (extend (?x true)",
//                                "                           (table unit))))",
//                                "    (table unit)))"),
//             "(project (?y)",
//             "  (filter (exprlist ?x (exists",
//             "                         (extend (?x true)",
//             "                           (table unit))))",
//             "    (table unit)))");
//             ;
//        //@formatter:on
//    }
//
//    @Test
//    public void ineligible_11() {
//        // We can't inline out of an EXISTS since the assignment isn't projected
//        // out anyway
//        //@formatter:off
//        testNoChange("(project (?y)",
//                     "  (filter (exprlist ?x)",
//                     "    (filter (exprlist (exists",
//                     "                         (extend (?x true)",
//                     "                           (table unit))))",
//                     "      (table unit))))");
//        //@formatter:on
//    }

    @Test
    public void ineligible_12() {
        // Can't inline through a distinct
        //@formatter:off
        testNoChange("(project (?y)",
                     "  (filter (exprlist ?x)",
                     "    (distinct",
                     "      (extend (?x true)",
                     "        (bgp (triple ?s ?p ?o))))))");
        //@formatter:on
    }

    @Test
    public void ineligible_13() {
        // Can't inline through a reduced
        //@formatter:off
        testNoChange("(project (?y)",
                     "  (filter (exprlist ?x)",
                     "    (reduced",
                     "      (extend (?x true)",
                     "        (bgp (triple ?s ?p ?o))))))");
        //@formatter:on
    }

    @Test
    public void exists_01() {
        // (extend) used in the filter-exists
        //@formatter:off
        testNoChange("(project (?y)",
                     "  (filter (exists",
                     "            (filter  ?x (table unit)))",
                     "    (extend (?x true)",
                     "      (table unit))))") ; 
        //@formatter:on
    }

    @Test
    public void exists_02() {
        // We can't inline into an EXISTS since the assignment isn't projected
        // out anyway and its an n-ary operator so would change semantics
        // However this makes the assignment unused so can still remove it
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?y)",
                               "  (filter (exists",
                               "            (filter ?z",
                               "              (table unit)))",
                               "    (extend (?x true)",
                               "      (table unit))))"),
            "(project (?y)",
            "  (filter (exprlist (exists",
            "                      (filter (exprlist ?z)",
            "                        (table unit))))",
            "    (table unit)))");
        //@formatter:on
    }


    @Test
    public void exists_03() {
        // "?x" not used elsewhere.
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?y)",
                                "  (filter (exists ",
                                "            (filter ?x",
                                "              (extend (?x true)",
                                "                 (table unit))))",
                                "    (table unit)))"),
            "(project (?y)",
            "  (filter (exists (filter true (table unit)))",
            "     (table unit)",
            "))") ;
            
        //@formatter:on
    }

    @Test
    public void exists_04() {
        // Can inline within an exists provided it meets normal conditions of
        // being inside a projection
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?y)",
                               "  (filter (exprlist (exists",
                               "                      (project (?y)",
                               "                        (filter (exprlist ?x)",
                               "                          (extend (?x true)",
                               "                            (table unit))))))",
                               "    (table unit)))"),
            "(project (?y)",
            "  (filter (exprlist (exists",
            "                      (project (?y)",
            "                        (filter (exprlist true)",
            "                          (table unit)))))",
            "    (table unit)))");
        //@formatter:on
    }

    @Test
    public void through_project_01() {
        // We can inline out through a project by also eliminating the variable
        // from the project
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?y)",
                                "  (filter (exprlist ?x)",
                                "    (project (?x)",
                                "      (extend (?x true)",
                                "        (table unit)))))"),
            "(project (?y)",
            "  (filter true",
            "    (project ()",
            "      (table unit))))");
        //@formatter:on
    }

    @Test
    public void through_project_02() {
        // We can inline out through a project by also eliminating the variable
        // from the project
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?y)",
                                "  (filter (exprlist ?x)",
                                "    (project (?x ?y)",
                                "      (extend (?x true)",
                                "        (bgp (triple ?y <urn:pred> <urn:obj>))))))"),
            "(project (?y)",
            "  (filter true",
            "    (project (?y)",
            "      (bgp (triple ?y <urn:pred> <urn:obj>)))))");
        //@formatter:on
    }

    @Test
    public void through_project_03() {
        // We can't inline out through a project if the assignment is not
        // projected
        // However we can still eliminate it from the inner projection if that
        // would render it unused
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?y)",
                                "  (filter (exprlist ?x)",
                                "    (project (?y)",
                                "      (extend (?x true)",
                                "        (table unit)))))"),
             "(project (?y)",
             "  (filter (exprlist ?x)",
             "    (project (?y)",
             "      (table unit))))");
        //@formatter:on
    }

    @Test
    public void through_project_04() {
        // Can't inline because the value is projected out
        //@formatter:off
        testNoChange("(project (?y)",
                     "  (project (?x ?y)",
                     "    (filter (exprlist ?x)",
                     "      (extend (?x true)",
                     "        (bgp (triple ?y <urn:pred> <urn:obj>))))))");
        //@formatter:on
    }

    @Test
    public void no_merge_01() {
        // We should not merge extends
        //@formatter:off
        testNoChange("(project (?x ?y)",
                     "  (filter (exprlist ?x)",
                     "    (extend (?x true)",
                     "      (extend (?y false)",
                     "        (table unit)))))");
        //@formatter:on
    }

    @Test
    public void scope_01() {
        // If the assignment is out of scope by the time it is used in the outer
        // scope then we can't substitute it out there
        // In this test the outer ?x is technically different from the inner ?x
        // anyway because of the projection
        //@formatter:off
        testNoChange(StrUtils.strjoinNL("(filter (exprlist ?x)",
                                        "  (project (?x ?y)",
                                        "    (extend (?x true)",
                                        "      (table unit))))"));
        //@formatter:on
    }

    @Test
    public void scope_02() {
        // If the assignment is out of scope by the time it is used in the outer
        // scope then we can't substitute it out there
        // However if the scoping means the value is never used we can instead
        // eliminate it entirely
        //@formatter:off
        test(StrUtils.strjoinNL("(filter (exprlist ?x)",
                                "  (project (?y)",
                                "    (extend (?x true)",
                                "      (table unit))))"),
            "(filter (exprlist ?x)",
            "  (project (?y)",
            "    (table unit)))");
        //@formatter:on
    }

    @Test
    public void scope_03() {
        // If the assignment is out of scope by the time it is used in the outer
        // scope then we can't substitute it out there
        // However in this case we can substitute it in the inner scope
        //@formatter:off
        test(StrUtils.strjoinNL("(filter (exprlist ?x)",
                                "  (project (?y)",
                                "    (filter (exprlist ?x)",
                                "      (extend (?x true)",
                                "        (table unit)))))"),
            "(filter (exprlist ?x)",
            "  (project (?y)",
            "    (filter (exprlist true)",
            "      (table unit))))");
        //@formatter:on
    }

    @Test
    public void scope_04() {
        // Only the topmost assignment should be inlined since any deeper
        // assignments are technically different variables
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?y)",
                                "  (filter (exprlist ?x)",
                                "    (extend (?x true)",
                                "      (project (?z)",
                                "         (filter (exprlist (|| ?x (! ?x)))",
                                "           (extend (?x false)",
                                "             (table unit)))))))"),
             "(project (?y)",
             "  (filter (exprlist true)",
             "    (project (?z)",
             "      (filter (exprlist (|| ?x (! ?x)))",
             "        (extend (?x false)",
             "          (table unit))))))");
       //@formatter:on
    }

    @Test
    public void scope_05() {
        // Technically invalid query but validates that the inner projection of
        // ?x blocks later in-lining
        //@formatter:off
        testNoChange("(project (?y)",
                     "  (filter (exprlist ?x)",
                     "    (extend (?x true)",
                     "      (project (?x)",
                     "         (filter (exprlist (|| ?x (! ?x)))",
                     "           (extend (?x false)",
                     "             (table unit)))))))");
       //@formatter:off
    }
    
    @Test
    public void scope_06() {
        // In-lining in the outer scope should not change the inner scope
        //@formatter:off
        test(StrUtils.strjoinNL("(project (?y)",
                                "  (filter (exprlist ?x)",
                                "    (extend (?x true)",
                                "      (project (?z)",
                                "         (project (?x)",
                                "           (filter (exprlist (|| ?x (! ?x)))",
                                "             (extend (?x false)",
                                "               (table unit))))))))"),
            "(project (?y)",
            "  (filter (exprlist true)",
            "    (project (?z)",
            "       (project (?x)",
            "         (filter (exprlist (|| ?x (! ?x)))",
            "           (extend (?x false)",
            "             (table unit)))))))");
       //@formatter:off
    }
}
