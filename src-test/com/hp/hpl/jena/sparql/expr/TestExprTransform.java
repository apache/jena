/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.sse.SSE ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

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

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */