/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import org.junit.Assert ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;

/** Break expression testing suite into parts
* @see TestExpressions
* @see TestExprLib
* @see TestNodeValue
*/
public class TestExpressions2 extends Assert
{
    
    @Test public void gregorian_eq_01()         { eval("'1999'^^xsd:gYear = '1999'^^xsd:gYear", true) ; }
    @Test public void gregorian_eq_02()         { eval("'1999'^^xsd:gYear != '1999'^^xsd:gYear", false) ; }
    
    @Test (expected=ExprEvalException.class)
    public void gregorian_eq_03()               { evalErr("'1999'^^xsd:gYear = '1999Z'^^xsd:gYear") ; }

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
    public void gregorian_cmp_04()              { evalErr("'1999'^^xsd:gYear < '1999+05:00'^^xsd:gYear") ; }
    
    public void gregorian_cast_01()             { eval("xsd:gYear('2010-03-22'^^xsd:date) = '2010'^^xsd:gYear", true ) ; }

    @Test (expected=ExprEvalException.class)
    public void coalesce_01()                   { evalErr("COALESCE()") ; } 
    @Test public void coalesce_02()             { eval("COALESCE(1) = 1", true) ; } 
    @Test public void coalesce_03()             { eval("COALESCE(?x,1) = 1", true) ; } 
    @Test public void coalesce_04()             { eval("COALESCE(9,1) = 9", true) ; } 
    
    // IF
    @Test public void if_01()                   { eval("IF(1+2=3, 'yes', 'no') = 'yes'", true) ; }
    @Test public void if_02()                   { eval("IF(1+2=4, 'yes', 'no') = 'no'", true) ; }
    @Test public void if_03()                   { eval("IF(true, 'yes', 1/0) = 'yes'", true) ; }
    @Test (expected=ExprEvalException.class)
    public void if_04()                         { evalErr("IF(true, 1/0, 'no') = 'no'") ; }
    
    // NOT IN, IN
    @Test public void in_01()                   { eval("1 IN(1,2,3)", true) ; }
    @Test public void in_02()                   { eval("1 IN(<x>,2,1)", true) ; }
    @Test public void in_03()                   { eval("1 IN()", false) ; }
    @Test public void in_04()                   { eval("1 IN(7)", false) ; }

    @Test public void not_in_01()                   { eval("1 NOT IN(1,2,3)", false) ; }
    @Test public void not_in_02()                   { eval("1 NOT IN(<x>,2,1)", false) ; }
    @Test public void not_in_03()                   { eval("1 NOT IN()", true) ; }
    @Test public void not_in_04()                   { eval("1 NOT IN(7)", true) ; }
    
    // Term constructors
    @Test public void term_constructor_iri_01()     { eval("IRI('http://example/') = <http://example/>", true) ; }
    
    @Test (expected=ExprEvalException.class)
    public void term_constructor_iri_02()           { evalErr("IRI(123)") ; } 
    @Test public void term_constructor_iri_03()     { eval("IRI(<http://example/>) = <http://example/>", true) ; }
    @Test public void term_constructor_iri_04()     { eval("isIRI(IRI(BNODE()))", true) ; }            // SPARQL extension
    @Test public void term_constructor_iri_05()     { eval("regex(str(IRI(BNODE())), '^_:' )", true) ; } // SPARQL extension

    @Test  public void term_constructor_bnode_01()  { eval("isBlank(BNODE())", true) ; }
    @Test public void term_constructor_bnode_02()   { eval("isBlank(BNODE('abc'))", true) ; }
    @Test public void term_constructor_bnode_03()   { eval("isBlank(BNODE('abc'))", true) ; }
    @Test public void term_constructor_bnode_04()   { eval("BNODE('abc') = BNODE('abc')", true) ; }
    @Test public void term_constructor_bnode_05()   { eval("BNODE('abc') = BNODE('def')", false) ; }

    @Test public void term_constructor_strdt_01()   { eval("STRDT('123',xsd:integer) = 123", true) ; }
    @Test public void term_constructor_strdt_02()   { eval("STRDT('123',<http://example/DT>) = '123'^^<http://example/DT>", true) ; }
    @Test (expected=ExprEvalException.class)
    public void term_constructor_strdt_03()         { evalErr("STRDT('123','abc') = '123'") ; }
    @Test (expected=ExprEvalException.class)
    public void term_constructor_strdt_04()         { evalErr("STRDT('123'^^xsd:integer,<http://example/DT>) = '123'^^<http://example/DT>") ; }
    
    @Test public void term_constructor_strlang_01() { eval("STRLANG('abc', 'en') = 'abc'@en", true) ; }
    @Test (expected=ExprEvalException.class)
    public void term_constructor_strlang_02()       { evalErr("STRLANG(<http://example/>, 'en') = 'abc'@en") ; }

    @Test (expected=ExprEvalException.class)
    public void term_constructor_strlang_03()       { evalErr("STRLANG('abc'@en, 'en') = 'abc'@en") ; }
    
    // ---- Workers
    
    private static void evalErr(String string)
    { 
        eval(string, true) ;
        throw new RuntimeException() ; 
    }
    
    // It's easier to write tests that simple are expected to return true/false 
    private static void eval(String string, boolean result)
    {
        Expr expr = ExprUtils.parse(string) ;
        NodeValue nv = expr.eval(null, FunctionEnvBase.createTest()) ;
        boolean b = XSDFuncOp.booleanEffectiveValue(nv) ;
        assertEquals(string, result, b) ;
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