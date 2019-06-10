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
import static org.junit.Assert.* ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ARQ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.FunctionEnvBase ;
import org.apache.jena.sparql.function.library.leviathan.LeviathanConstants ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ExprUtils ;
import org.apache.jena.sparql.util.NodeFactoryExtra ;

public class LibTestExpr {


    private static PrefixMapping pmap = new PrefixMappingImpl();
    static {
        pmap.setNsPrefixes(ARQConstants.getGlobalPrefixMap());
        pmap.setNsPrefix("lfn", LeviathanConstants.LeviathanFunctionLibraryURI);
    }
    
    static void testExpr(String exprExpected, String expectedResult) {
        NodeValue actual = eval(exprExpected) ;
        NodeValue expected = eval(expectedResult) ;
        assertEquals(exprExpected, expected, actual) ; 
    }

    static Expr parse(String exprString) {
        return ExprUtils.parse(exprString, pmap);
    }
    
    /** Create an execution environment suitable for testing functions and expressions */ 
    public static FunctionEnv createTest()
    {
        Context cxt = ARQ.getContext().copy();
        cxt.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime()) ;
        return new FunctionEnvBase(cxt) ;
    }
        
    static NodeValue eval(String exprString) {
        Expr expr = ExprUtils.parse(exprString, pmap);
        NodeValue result = expr.eval(null, new FunctionEnvBase());
        return result ;
    }
        
    
    static void test(String exprString, String result) {
        Node r = NodeFactoryExtra.parseNode(result);
        test(exprString, r);
    }
    
    static void test(String exprString, Node result) {
        Expr expr = ExprUtils.parse(exprString, pmap);
        NodeValue actual = expr.eval(null, new FunctionEnvBase());
        NodeValue expected = NodeValue.makeNode(result);
        assertTrue("Expected = " + expected + " : Actual = " + actual, NodeValue.sameAs(expected, actual)) ;
    }

    static void testDouble(String exprString, String result, double delta) {
        Node r = NodeFactoryExtra.parseNode(result);
        testDouble(exprString, r, delta);
    }

    static void testDouble(String exprString, Node result, double delta) {
        Expr expr = ExprUtils.parse(exprString, pmap);
        NodeValue actual = expr.eval(null, new FunctionEnvBase());
        NodeValue expected = NodeValue.makeNode(result);
        // Note that we don't test lexical form because we can get mismatches
        // between how things like doubles are expressed
        if (NodeValue.sameAs(expected, actual))
            return;

        testDouble(exprString, expected.getDouble(), delta); ;
    }
    
    static void testDouble(String exprString, double expected, double delta) {
        Expr expr = ExprUtils.parse(exprString, pmap);
        NodeValue actual = expr.eval(null, new FunctionEnvBase());
        assertTrue("Not a double: "+actual, actual.isDouble() ) ; 
        double result = actual.getDouble() ;
        
        // Because Java floating point calculations are woefully imprecise we
        // are in many cases simply testing that the differences between the
        // values are within a given delta
        if ( Double.isInfinite(expected) ) {
            assertTrue("Expected INF: Got "+result, Double.isInfinite(result)) ;
            return ;
        }
        
        if ( Double.isNaN(expected) ) {
            assertTrue("Expected NaN: Got "+result, Double.isNaN(result)) ;
            return ;
        }

        double difference = Math.abs(result - expected);
        assertTrue("Values not within given delta " + delta + ": Expected = " + expected + " : Actual = " + actual,
                difference <= delta);
    }

    
    static void testError(String exprString) {
        Expr expr = ExprUtils.parse(exprString, pmap);
        expr.eval(null, new FunctionEnvBase());
    }
}
