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

import static org.junit.Assert.assertEquals;

import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp ;
import org.apache.jena.sparql.sse.Item ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.sse.builders.BuilderBinding ;
import org.apache.jena.sparql.util.ExprUtils ;
import org.junit.Test ;

/** Expression evaluation involving bindings.
* @see TestExpressions
* @see TestExpressions2
* @see TestExpressions3
* @see TestExprLib
* @see TestNodeValue
*/
public class TestExpressions3
{
    @Test public void bound_01()       { eval("BOUND(?x)", "(?x 1)", true) ; }
    @Test public void bound_02()       { eval("BOUND(?x)", "(?y 1)", false) ; }
    @Test public void bound_03()       { evalExpr("(bound 1)", "(?y 1)", true) ; }
    @Test public void bound_04()       { evalExpr("(bound 1)", "()", true) ; }
    @Test public void bound_05()       { evalExpr("(bound ?x)", "(?y 1)", false) ; }
    @Test public void bound_06()       { evalExpr("(bound ?x)", "(?x 1)", true) ; }
    @Test public void bound_07()       { evalExpr("(bound (+ ?x 1))", "(?y 1)", false) ; }
    @Test public void bound_08()       { evalExpr("(bound (+ ?y 1))", "(?y 1)", true) ; }

    // From SPARQL syntax
    private static void eval(String string, String bindingStr, boolean expected) {
        Binding binding = binding(bindingStr) ;
        Expr expr = ExprUtils.parse(string) ;
        NodeValue nv = expr.eval(binding, LibTestExpr.createTest()) ;
        boolean b = XSDFuncOp.booleanEffectiveValue(nv) ;
        assertEquals(string, expected, b) ;
    }

    // From algebra/SSE
    private static void evalExpr(String exprString, String bindingStr, boolean expected) {
        Binding binding = binding(bindingStr) ;
        Expr expr = SSE.parseExpr(exprString) ;
        NodeValue nv = expr.eval(binding, LibTestExpr.createTest()) ;
        boolean b = XSDFuncOp.booleanEffectiveValue(nv) ;
        assertEquals(exprString, expected, b) ;
    }

    private static Binding binding(String bindingStr) {
        if ( bindingStr == null || bindingStr.matches("\\s*\\(\\s*\\)\\s*") )
            return null ;
        Item item = SSE.parse("(binding "+bindingStr+")") ;
        Binding binding = BuilderBinding.build(item) ;
        return binding ;
    }
}
