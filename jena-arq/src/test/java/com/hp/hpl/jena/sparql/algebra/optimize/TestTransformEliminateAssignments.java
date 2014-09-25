package com.hp.hpl.jena.sparql.algebra.optimize;

import org.apache.jena.atlas.lib.StrUtils;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.sse.SSE;

/**
 * Tests for the {@link TransformEliminateAssignments}
 * 
 */
public class TestTransformEliminateAssignments {

    private void test(String input, String... output) {
        Op original = SSE.parseOp(input);
        test(original, output);
    }

    private void test(Op original, String... output) {
        // Transform
        Op actual = TransformEliminateAssignments.eliminate(original);

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

    private void testNoChange(String input) {
        test(input, (String[]) null);
    }

    private void testNoChange(String... input) {
        test(StrUtils.strjoinNL(input), (String[]) null);
    }

    @Test
    public void eliminate_single_use_extend_01() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        //@formatter:off
        test(StrUtils.strjoinNL("(filter (exprlist ?x)",
                                "  (extend (?x true)",
                                "    (table unit)))"),
             "(filter (exprlist true)",
             "  (table unit))");
        //@formatter:on
    }

    @Test
    public void eliminate_single_use_extend_02() {
        // Assigned variable used only once can substitute expression for the
        // later usage of the variable
        // The other assignment is removed because it's value is never used
        //@formatter:off
        test(StrUtils.strjoinNL("(filter (exprlist ?x)",
                                "  (extend ((?x true) (?y false))",
                                "    (table unit)))"),
             "(filter (exprlist true)",
             "    (table unit))");
        //@formatter:on
    }

    @Test
    public void multi_use_extend_unchanged_01() {
        // As the assigned variable is used multiple times we leave the
        // assignment alone
        //@formatter:off
        testNoChange("(filter (> (* ?x ?x) 16)",
                     "  (extend (?x 3)",
                     "    (table unit)))");
        //@formatter:on
    }

    @Test
    public void multi_use_extend_unchanged_02() {
        // Because the value of the assignment is used in multiple places we
        // leave the assignment alone
        //@formatter:off
        testNoChange("(filter (exprlist ?x)",
                     "  (join",
                     "    (extend (?x true)",
                     "      (table unit))",
                     "    (bgp (triple ?x ?y ?z))))");
        //@formatter:on
    }

    @Test
    public void scoped_use_extend_01() {
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
    public void scoped_use_extend_02() {
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
    public void eliminate_single_use_assign_01() {
        //@formatter:off
        test(StrUtils.strjoinNL("(filter (exprlist ?x)",
                                "  (assign (?x true)",
                                "    (table unit)))"),
             "(filter (exprlist true)",
             "  (table unit))");
        //@formatter:on
    }

    @Test
    public void multi_use_assign_unchanged_01() {
        //@formatter:off
        testNoChange("(filter (> (* ?x ?x) 16)",
                     "  (assign (?x 3)",
                     "    (table unit)))");
        //@formatter:on
    }

    @Test
    public void multi_use_assign_unchanged_02() {
        // Left alone because assigned to more than once
        //@formatter:off
        testNoChange("(filter (exprlist ?x)",
                     "  (assign (?x true)",
                     "    (assign (?x true)",
                     "      (table unit))))");
        //@formatter:on
    }

    @Test
    public void multi_use_assign_unchanged_03() {
        // Left alone because assigned to more than once
        //@formatter:off
        testNoChange("(filter (exprlist ?x)",
                     "  (assign (?x true)",
                     "    (assign (?x false)",
                     "      (table unit))))");
        //@formatter:on
    }
}
