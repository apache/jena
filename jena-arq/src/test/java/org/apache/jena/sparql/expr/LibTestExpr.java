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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.FunctionEnvBase;
import org.apache.jena.sparql.function.library.leviathan.LeviathanConstants;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.NodeFactoryExtra;

public class LibTestExpr {

    private static PrefixMapping pmap = new PrefixMappingImpl();
    static {
        pmap.setNsPrefixes(ARQConstants.getGlobalPrefixMap());
        pmap.setNsPrefix("lfn", LeviathanConstants.LeviathanFunctionLibraryURI);
    }

    public static void testExpr(String exprExpected, String expectedResult) {
        NodeValue actual = eval(exprExpected);
        NodeValue expected = eval(expectedResult);
        assertEquals(expected, actual, ()->exprExpected);
    }

    public static Expr parse(String exprString) {
        return ExprUtils.parse(exprString, pmap);
    }

    /** Create an execution environment suitable for testing functions and expressions */
    public static FunctionEnv createTest()
    {
        Context cxt = ARQ.getContext().copy();
        cxt.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime());
        return new FunctionEnvBase(cxt);
    }

    public static NodeValue eval(String exprString) {
        Expr expr = parse(exprString);
        NodeValue result = expr.eval(null, new FunctionEnvBase());
        return result;
    }

    public static void test(String exprStr, String exprStrExpected) {
        Expr expr = parse(exprStrExpected);
        NodeValue rExpected = expr.eval(null, LibTestExpr.createTest());
        test(exprStr, rExpected);
    }

    /** SSE syntax */
    public static void testSSE(String functionExprStr, String exprStrExpected) {
        Expr expected = SSE.parseExpr(exprStrExpected);
        NodeValue vExpected = expected.eval(null, LibTestExpr.createTest());
        Expr actual = SSE.parseExpr(functionExprStr);
        NodeValue vActual = expected.eval(null, LibTestExpr.createTest());
        assertTrue(sameValueSameDatatype(vExpected, vActual), ()->"Expected = " + expected + " : Actual = " + actual);
    }

    public static void test(String exprString, Node result) {
        NodeValue expected = NodeValue.makeNode(result);
        test(exprString, expected);
    }

    public static void test(String exprStr, NodeValue expected) {
        Expr expr = parse(exprStr);
        NodeValue actual = expr.eval(null, LibTestExpr.createTest());
        assertTrue(sameValueSameDatatype(expected, actual), ()->"Expected = " + expected + " : Actual = " + actual);
    }

    public static void testIsNaN(String exprStr) {
        testSameObject(exprStr, NodeValue.nvNaN);
    }

    public static void testSameObject(String exprStr, NodeValue expected) {
        Expr expr = parse(exprStr);
        NodeValue actual = expr.eval(null, LibTestExpr.createTest());
        assertTrue(expected.equals(actual), ()->"Expected = " + expected + " : Actual = " + actual);
    }

    private static boolean sameValueSameDatatype(NodeValue nv1, NodeValue nv2) {
        if ( ! NodeValue.sameValueAs(nv1, nv2) )
            return false;
        Node n1 = nv1.asNode();
        Node n2 = nv2.asNode();
        if ( ! n1.isLiteral() || ! n2.isLiteral() )
            // URIs, bnodes etc because the sameAs test passed ...
            return true;
        return nv1.asNode().getLiteralDatatype().equals(nv2.asNode().getLiteralDatatype());
    }

    public static void test(String exprStr) {
        test(exprStr, NodeValue.TRUE);
    }

    public static void test(String exprStr, Predicate<NodeValue> test) {
        Expr expr = parse(exprStr);
        NodeValue r = expr.eval(null, LibTestExpr.createTest());
        assertTrue(test.test(r), ()->exprStr);
    }

    public static void testDouble(String exprString, String result, double delta) {
        Node r = NodeFactoryExtra.parseNode(result);
        testDouble(exprString, r, delta);
    }

    public static void testDouble(String exprString, Node result, double delta) {
        Expr expr = parse(exprString);
        NodeValue actual = expr.eval(null, new FunctionEnvBase());
        NodeValue expected = NodeValue.makeNode(result);
        // Note that we don't test lexical form because we can get mismatches
        // between how things like doubles are expressed
        if (NodeValue.sameValueAs(expected, actual))
            return;
        testDouble(exprString, expected.getDouble(), delta);;
    }

    public static void testDouble(String exprString, double expected, double delta) {
        Expr expr = ExprUtils.parse(exprString, pmap);
        NodeValue actual = expr.eval(null, new FunctionEnvBase());
        assertTrue(actual.isDouble(), ()->"Not a double: "+actual);
        double result = actual.getDouble();

        // Because Java floating point calculations are woefully imprecise we
        // are in many cases simply testing that the differences between the
        // values are within a given delta
        if ( Double.isInfinite(expected) ) {
            assertTrue(Double.isInfinite(result), ()->"Expected INF: Got "+result);
            return;
        }

        if ( Double.isNaN(expected) ) {
            assertTrue(Double.isNaN(result),()->"Expected NaN: Got "+result);
            return;
        }

        double difference = Math.abs(result - expected);
        assertTrue(difference <= delta,
                ()->"Values not within given delta " + delta + ": Expected = " + expected + " : Actual = " + actual);
    }


    public static void testError(String exprString) {
        Expr expr = ExprUtils.parse(exprString, pmap);
        expr.eval(null, new FunctionEnvBase());
    }
}
