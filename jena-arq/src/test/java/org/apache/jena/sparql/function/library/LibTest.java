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

package org.apache.jena.sparql.function.library;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertTrue ;

import java.util.function.Predicate;

import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.LibTestExpr;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.util.ExprUtils ;

public class LibTest {
    private static PrefixMapping pmap = ARQConstants.getGlobalPrefixMap() ;

    static void test(String string) {
        test(string, "true");
    }

    static void test(String exprStr, NodeValue result) {
        Expr expr = ExprUtils.parse(exprStr) ;
        NodeValue r = expr.eval(null, LibTestExpr.createTest()) ;
        assertEquals(result, r) ;
    }

    static void test(String exprStr, String exprStrExpected) {
        Expr expr = ExprUtils.parse(exprStrExpected) ;
        NodeValue rExpected = expr.eval(null, LibTestExpr.createTest()) ;
        test(exprStr, rExpected) ;
    }
    
    static void test(String exprStr, Predicate<NodeValue> test) {
        Expr expr = ExprUtils.parse(exprStr) ;
        NodeValue r = expr.eval(null, LibTestExpr.createTest()) ;
        assertTrue(exprStr, test.test(r));
    }
}
