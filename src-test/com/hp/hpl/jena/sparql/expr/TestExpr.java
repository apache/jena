/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import org.junit.Assert ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;

/** New (ARQ 2.8.3) expression testing test suite
* @see TestExpressions
* @see TestExprLib
* @see TestNodeValue
*/
public class TestExpr extends Assert
{
    
    @Test public void gregorian_eq_01()         { eval("'1999'^^xsd:gYear = '1999'^^xsd:gYear", true) ; }
    @Test public void gregorian_eq_02()         { eval("'1999'^^xsd:gYear != '1999'^^xsd:gYear", false) ; }
    
    @Test (expected=ExprEvalException.class)
    public void gregorian_eq_03()               { eval("'1999'^^xsd:gYear = '1999Z'^^xsd:gYear", false) ; }

    @Test  public void gregorian_eq_04()        { eval("'1999'^^xsd:gYear = '2001Z'^^xsd:gYear", false) ; }
    
    // Different value spaces => different.
    @Test public void gregorian_eq_05()         { eval("'1999-01'^^xsd:gYearMonth != '2001Z'^^xsd:gYear", true) ; }

    @Test public void gregorian_eq_06()         { eval("'--01'^^xsd:gMonth != '--01-25'^^xsd:gMonthDay", true) ; }
    @Test public void gregorian_eq_07()         { eval("'---25'^^xsd:gDay = '---25'^^xsd:gDay", true) ; }
    @Test public void gregorian_eq_08()         { eval("'1999-01'^^xsd:gYearMonth != '2001Z'^^xsd:gYear", true) ; }
    @Test public void gregorian_eq_09()         { eval("'1999-01'^^xsd:gYearMonth != '2001Z'^^xsd:gYear", true) ; }
    
    @Test public void gregorian_cmp_01()        { eval("'1999'^^xsd:gYear < '2000'^^xsd:gYear", true) ; }
    @Test public void gregorian_cmp_02()        { eval("'1999'^^xsd:gYear > '2000'^^xsd:gYear", false) ; }
    @Test public void gregorian_cmp_03()        { eval("'1999'^^xsd:gYear < '2000+01:00'^^xsd:gYear", true) ; }

    @Test (expected=ExprEvalException.class)
    public void gregorian_cmp_04()              { eval("'1999'^^xsd:gYear < '1999+05:00'^^xsd:gYear", false) ; }
    
    
    
    // It's easier to write tests that simple are expected to return true/false 
    private static void eval(String string, boolean result)
    {
        Expr expr = ExprUtils.parse(string) ;
        NodeValue nv = expr.eval(null, null) ;
        boolean b = XSDFuncOp.booleanEffectiveValue(nv) ;
        assertEquals(string, result, b) ;
    }
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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