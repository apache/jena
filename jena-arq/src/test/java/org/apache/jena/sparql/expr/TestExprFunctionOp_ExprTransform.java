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
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.algebra.walker.Walker;
import org.apache.jena.sparql.expr.TestExprFunctionOp_NodeTransform.ExprFunctionOpValidator;
import org.apache.jena.sparql.util.ExprUtils;

public class TestExprFunctionOp_ExprTransform {
    private ExprTransform et = new ExprTransformCopy()
    {   @Override
        public Expr transform(ExprVar exprVar)
        { return new ExprVar(exprVar.getVarName().toUpperCase()); }
    };

    @Test public void et_exists_tp_01()        { test(    "exists { ?s ?p ?o }",    "exists { ?s ?p ?o }", et); }
    @Test public void et_notExists_tp_01()     { test("not exists { ?s ?p ?o }", "notexists { ?s ?p ?o }", et); }

    // Note: The empty graph pattern in "{} FILTER(...)" is used to establish
    //       syntactic equivalence with the transformation result.
    @Test public void et_exists_filter_01()    { test(    "exists { {} FILTER(?x = ?y) }",     "exists { {} FILTER(?X = ?Y) }", et); }
    @Test public void et_notExists_filter_01() { test("not exists { {} FILTER(?x = ?y) }", "not exists { {} FILTER(?X = ?Y) }", et); }

    @Test public void et_exists_bind_01()      { test(    "exists { BIND(?x AS ?y) }",     "exists { BIND(?X AS ?y) }", et); }
    @Test public void et_notExists_bind_01()   { test("not exists { BIND(?x AS ?y) }", "not exists { BIND(?X AS ?y) }", et); }

    @Test public void et_exists_nested_filter_01()     { test(    "exists { {} FILTER     exists { {} FILTER(?x = ?y) } }",     "exists { {} FILTER     exists { {} FILTER(?X = ?Y) } }", et); }
    @Test public void et_notExists_nested_filter_01()  { test("not exists { {} FILTER not exists { {} FILTER(?x = ?y) } }", "not exists { {} FILTER not exists { {} FILTER(?X = ?Y) } }", et); }

    private void test(String string, String string2, ExprTransform et)
    {
        Expr e1 = ExprUtils.parse(string);
        Expr e2 = ExprUtils.parse(string2);

        Expr e3 = ExprTransformer.transform(et, e1);

        // Check whether syntax and algebra are consistent
        ExprVisitor opVisitor = new ExprFunctionOpValidator();
        Walker.walk(e2, opVisitor);
        Walker.walk(e3, opVisitor);

        assertEquals(e2, e3);
        if (!e2.equalsBySyntax(e3)) {
            fail("Objects differ by syntax: " + e2 + " != " + e3);
        }
    }
}
