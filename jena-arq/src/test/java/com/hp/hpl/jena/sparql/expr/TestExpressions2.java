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

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;

/** Break expression testing suite into parts
* @see TestExpressions
* @see TestExprLib
* @see TestNodeValue
*/
public class TestExpressions2 extends BaseTest
{
    
    @Test public void gregorian_eq_01()         { eval("'1999'^^xsd:gYear = '1999'^^xsd:gYear", true) ; }
    @Test public void gregorian_eq_02()         { eval("'1999'^^xsd:gYear != '1999'^^xsd:gYear", false) ; }
    
    @Test (expected=ExprEvalException.class)
    public void gregorian_eq_03()               { eval("'1999'^^xsd:gYear = '1999Z'^^xsd:gYear") ; }

    @Test  public void gregorian_eq_04()        { eval("'1999'^^xsd:gYear = '2001Z'^^xsd:gYear", false) ; }
    
    // Different value spaces => different.
    @Test public void gregorian_eq_05()         { eval("'1999-01'^^xsd:gYearMonth != '2001Z'^^xsd:gYear", true) ; }

    @Test public void gregorian_eq_06()         { eval("'--01'^^xsd:gMonth != '--01-25'^^xsd:gMonthDay", true) ; }
    @Test public void gregorian_eq_07()         { eval("'---25'^^xsd:gDay = '---25'^^xsd:gDay", true) ; }
    @Test public void gregorian_eq_08()         { eval("'1999-01'^^xsd:gYearMonth != '2001Z'^^xsd:gYear", true) ; }
    @Test public void gregorian_eq_09()         { eval("'1999-01'^^xsd:gYearMonth != '2001Z'^^xsd:gYear", true) ; }
    
    @Test public void gregorian_cmp_01()        { eval("'1999'^^xsd:gYear < '2001'^^xsd:gYear", true) ; }
    @Test public void gregorian_cmp_02()        { eval("'1999'^^xsd:gYear > '2001'^^xsd:gYear", false) ; }
    @Test public void gregorian_cmp_03()        { eval("'1999'^^xsd:gYear < '2001+01:00'^^xsd:gYear", true) ; }

    @Test (expected=ExprEvalException.class)
    public void gregorian_cmp_04()              { eval("'1999'^^xsd:gYear < '1999+05:00'^^xsd:gYear") ; }
    
    public void gregorian_cast_01()             { eval("xsd:gYear('2010-03-22'^^xsd:date) = '2010'^^xsd:gYear", true ) ; }

    @Test (expected=ExprEvalException.class)
    public void coalesce_01()                   { eval("COALESCE()") ; } 
    @Test public void coalesce_02()             { eval("COALESCE(1) = 1", true) ; } 
    @Test public void coalesce_03()             { eval("COALESCE(?x,1) = 1", true) ; } 
    @Test public void coalesce_04()             { eval("COALESCE(9,1) = 9", true) ; } 
    
    // IF
    @Test public void if_01()                   { eval("IF(1+2=3, 'yes', 'no') = 'yes'", true) ; }
    @Test public void if_02()                   { eval("IF(1+2=4, 'yes', 'no') = 'no'", true) ; }
    @Test public void if_03()                   { eval("IF(true, 'yes', 1/0) = 'yes'", true) ; }
    @Test (expected=ExprEvalException.class)
    public void if_04()                         { eval("IF(true, 1/0, 'no') = 'no'") ; }
    
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
    public void term_constructor_iri_02()           { eval("IRI(123)") ; } 
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
    public void term_constructor_strdt_03()         { eval("STRDT('123','abc') = '123'") ; }
    @Test (expected=ExprEvalException.class)
    public void term_constructor_strdt_04()         { eval("STRDT('123'^^xsd:integer,<http://example/DT>) = '123'^^<http://example/DT>") ; }
    
    @Test public void term_constructor_strlang_01() { eval("STRLANG('abc', 'en') = 'abc'@en", true) ; }
    @Test (expected=ExprEvalException.class)
    public void term_constructor_strlang_02()       { eval("STRLANG(<http://example/>, 'en') = 'abc'@en") ; }

    @Test (expected=ExprEvalException.class)
    public void term_constructor_strlang_03()       { eval("STRLANG('abc'@en, 'en') = 'abc'@en") ; }
    
    // XSD casts
    
    @Test public void xsd_cast_01()                 { eval("xsd:integer('1') = 1", true) ; }
    @Test public void xsd_cast_02()                 { eval("xsd:boolean('1') = true", true) ; }
    @Test public void xsd_cast_03()                 { eval("sameTerm(xsd:double('1.0e0'),1.0e0)", true) ; }
    @Test public void xsd_cast_04()                 { eval("xsd:double('1') = 1", true) ; }

    @Test (expected=ExprEvalException.class)
    public void xsd_cast_10()                       { eval("xsd:integer(' 1')") ; }
    @Test (expected=ExprEvalException.class)
    public void xsd_cast_11()                       { eval("xsd:boolean(' 1')") ; }
    @Test (expected=ExprEvalException.class)
    public void xsd_cast_12()                       { eval("xsd:double(' 1.0e0')") ; }
    @Test (expected=ExprEvalException.class)
    public void xsd_cast_13()                       { eval("xsd:double(' 1.0e0')") ; }
    
    // Dynamic Function Calls
    @Test (expected=QueryParseException.class)
    public void dynamic_call_01()                   { eval("CALL()", false); }
    
    @Test public void dynamic_call_02()             { eval("CALL(xsd:double, '1') = 1") ; }
    @Test public void dynamic_call_03()             { eval("CALL(fn:concat, 'A', 2+3 ) = 'A5'") ; }
    
    @Test (expected=ExprEvalException.class)
    public void dynamic_call_10()                   { eval("CALL(xsd:double)") ; }
    @Test (expected=ExprEvalException.class)
    public void dynamic_call_11()                   { eval("CALL(xsd:noSuchFunc)") ; }
    
    // ---- Workers
    
    private static void eval(String string)
    { 
        eval(string, true) ;
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
