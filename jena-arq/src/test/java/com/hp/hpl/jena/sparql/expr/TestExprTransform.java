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

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.sse.SSE ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

public class TestExprTransform extends BaseTest
{
    ExprTransform et1 = new ExprTransformCopy() 
    {   @Override
        public Expr transform(ExprVar exprVar)  
        { return new ExprVar(exprVar.getVarName().toUpperCase()) ; } 
    } ;
    
    @Test public void exprTransform_01()    { test("?v", "?V", et1 ) ; }
    @Test public void exprTransform_02()    { test("(+ ?v 1)", "(+ ?V 1)", et1 ) ; }
    @Test public void exprTransform_03()    { test("(str (+ ?v 1))", "(str (+ ?V 1))", et1 ) ; }
    @Test public void exprTransform_04()    { test("(if (+ ?v 1) ?a ?b)", "(if (+ ?V 1) ?A ?B)", et1 ) ; }
    
    // 2 or 3 ?
    @Test public void exprTransform_05()    { test("(regex ?a ?b ?c)", "(regex ?A ?B ?C)", et1) ; }  
    @Test public void exprTransform_06()    { test("(regex ?a ?b)", "(regex ?A ?B)", et1) ; }

    
    private void test(String string, String string2, ExprTransform et)
    {
        Expr e1 = SSE.parseExpr(string) ;
        Expr e2 = SSE.parseExpr(string2) ;
        
        Expr e3 = ExprTransformer.transform(et, e1) ;
        assertEquals(e2, e3) ;
        
    }
}
