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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.sparql.function.FunctionEnvBase;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.ExprUtils;

/** Break expression testing suite into parts
* @see TestExpressions
* @see TestExprLib
* @see TestNodeValue
*/
public class TestExpressions2
{
    private static boolean functionWarnFlag;
    private static boolean nodeValueWarnFlag;

    @BeforeAll
    public static void beforeAll() {
        functionWarnFlag = E_Function.WarnOnUnknownFunction;
        E_Function.WarnOnUnknownFunction = false;
        nodeValueWarnFlag = NodeValue.VerboseWarnings;
        NodeValue.VerboseWarnings = false;
    }

    @AfterAll
    public static void afterAll() {
        E_Function.WarnOnUnknownFunction = functionWarnFlag;
        NodeValue.VerboseWarnings = nodeValueWarnFlag;
    }

    @Test public void gregorian_eq_01()         { eval("'1999'^^xsd:gYear = '1999'^^xsd:gYear", true); }
    @Test public void gregorian_eq_02()         { eval("'1999'^^xsd:gYear != '1999'^^xsd:gYear", false); }
    @Test public void gregorian_eq_03()         { assertThrows(ExprEvalException.class, ()-> eval("'1999'^^xsd:gYear = '1999Z'^^xsd:gYear") ); }
    @Test public void gregorian_eq_04()         { eval("'1999'^^xsd:gYear = '2001Z'^^xsd:gYear", false); }

    // Different value spaces => different.
    @Test public void gregorian_eq_05()         { eval("'1999-01'^^xsd:gYearMonth != '2001Z'^^xsd:gYear", true); }
    @Test public void gregorian_eq_06()         { assertThrows(ExprEvalException.class, ()-> eval("'--01'^^xsd:gMonth != '--01-25'^^xsd:gMonthDay", true) ); }

    @Test public void gregorian_eq_07()         { eval("'---25'^^xsd:gDay = '---25'^^xsd:gDay", true); }
    @Test public void gregorian_eq_08()         { eval("'1999-01'^^xsd:gYearMonth != '2001Z'^^xsd:gYear", true); }
    @Test public void gregorian_eq_09()         { eval("'1999-01'^^xsd:gYearMonth != '2001Z'^^xsd:gYear", true); }

    @Test public void gregorian_cmp_01()        { eval("'1999'^^xsd:gYear < '2001'^^xsd:gYear", true); }
    @Test public void gregorian_cmp_02()        { eval("'1999'^^xsd:gYear > '2001'^^xsd:gYear", false); }
    @Test public void gregorian_cmp_03()        { eval("'1999'^^xsd:gYear < '2001+01:00'^^xsd:gYear", true); }
    @Test public void gregorian_cmp_04()        { assertThrows(ExprEvalException.class, ()-> eval("'1999'^^xsd:gYear < '1999+05:00'^^xsd:gYear") ); }

    @Test public void gregorian_cast_01()       { eval("xsd:gYear('2010-03-22'^^xsd:date) = '2010'^^xsd:gYear", true ); }

    @Test public void coalesce_01()             { assertThrows(ExprEvalException.class, ()-> eval("COALESCE()") ); }
    @Test public void coalesce_02()             { eval("COALESCE(1) = 1", true); }
    @Test public void coalesce_03()             { eval("COALESCE(?x,1) = 1", true); }
    @Test public void coalesce_04()             { eval("COALESCE(9,1) = 9", true); }

    // IF
    @Test public void if_01()                   { eval("IF(1+2=3, 'yes', 'no') = 'yes'", true); }
    @Test public void if_02()                   { eval("IF(1+2=4, 'yes', 'no') = 'no'", true); }
    @Test public void if_03()                   { eval("IF(true, 'then', 1/0) = 'then'", true); }
    @Test public void if_04()                   { eval("IF(false, 1/0, 'else') = 'else'", true); }
    @Test public void if_05()                   { assertThrows(ExprEvalException.class, ()-> eval("IF(true, 1/0, 'no') = 'no'") ); }
    @Test public void if_06()                   { assertThrows(ExprEvalException.class, ()-> eval("IF(false, 'yes', 1/0) = 'yes'") ); }
    // EBV in the condition
    @Test public void if_10()                   { eval("IF( 1, 'yes', 'no') = 'yes'"); }
    @Test public void if_11()                   { eval("IF( 1.1, 'yes', 'no') = 'yes'"); }
    @Test public void if_12()                   { eval("IF( 1e2, 'yes', 'no') = 'yes'"); }
    @Test public void if_13()                   { eval("IF( 0, 'yes', 'no') = 'no'"); }
    @Test public void if_14()                   { eval("IF( 0.0, 'yes', 'no') = 'no'"); }
    @Test public void if_15()                   { eval("IF( -0e1, 'yes', 'no') = 'no'"); }
    @Test public void if_16()                   { eval("IF('NaN'^^xsd:double, 'yes', 'no') = 'no'"); }

    @Test public void if_20()                   { assertThrows(ExprEvalException.class, ()-> eval("IF(?unbound, 'yes', 'no') = 'no'")); }
    @Test public void if_21()                   { assertThrows(ExprEvalException.class, ()-> eval("IF(! (?unbound), 'yes', 'no') = 'no'")); }
    @Test public void if_22()                   { assertThrows(ExprEvalException.class, ()-> eval("IF(1/0, 'yes', 'no') = 'no'")); }

    // NOT IN, IN
    @Test public void in_01()                   { eval("1 IN(1,2,3)", true); }
    @Test public void in_02()                   { eval("1 IN(<x>,2,1)", true); }
    @Test public void in_03()                   { eval("1 IN()", false); }
    @Test public void in_04()                   { eval("1 IN(7)", false); }

    @Test public void not_in_01()               { eval("1 NOT IN(1,2,3)", false); }
    @Test public void not_in_02()               { eval("1 NOT IN(<x>,2,1)", false); }
    @Test public void not_in_03()               { eval("1 NOT IN()", true); }
    @Test public void not_in_04()               { eval("1 NOT IN(7)", true); }

    @Test public void ebv_01()                  { eval("EBV(false)", false); }
    @Test public void ebv_02()                  { eval("EBV(true)", true); }
    @Test public void ebv_03()                  { eval("EBV(0)", false); }
    @Test public void ebv_04()                  { eval("EBV(1)", true); }
    @Test public void ebv_05()                  { eval("EBV(0.0)", false); }
    @Test public void ebv_06()                  { eval("EBV(1.0)", true); }
    @Test public void ebv_07()                  { eval("EBV('')", false); }
    @Test public void ebv_08()                  { eval("EBV('str')", true); }
    @Test public void ebv_09()                  { eval("EBV('NaN'^^xsd:double)", false); }
    @Test public void ebv_10()                  { eval("EBV('false')", true); }     // The string "false" is not special in anyway.

    // EBV errors.
    // Not a numeric, boolean or string.
    @Test public void ebv_20()                  { assertThrows(ExprEvalException.class, ()-> eval("EBV('2025-08-18'^^xsd:date)", false)); }
    @Test public void ebv_21()                  { assertThrows(ExprEvalException.class, ()-> eval("EBV(<http://example/>)", false)); }
    @Test public void ebv_22()                  { assertThrows(ExprEvalException.class, ()-> eval("EBV(1/0)", false)); }
    @Test public void ebv_23()                  { assertThrows(ExprEvalException.class, ()-> eval("EBV(?unbound)", false)); }
    @Test public void ebv_24()                  { assertThrows(ExprEvalException.class, ()-> eval("EBV('notValid'^^xsd:integer)", false)); }

    // Term constructors
    @Test public void term_constructor_iri_01() { eval("IRI('http://example/') = <http://example/>", true); }
    @Test public void term_constructor_iri_02() { assertThrows(ExprEvalException.class, ()-> eval("IRI(123)") ); }
    @Test public void term_constructor_iri_03() { eval("IRI(<http://example/>) = <http://example/>", true); }
    @Test public void term_constructor_iri_04() { eval("isIRI(IRI(BNODE()))", true); }                  // SPARQL extension
    @Test public void term_constructor_iri_05() { eval("regex(str(IRI(BNODE())), '^_:' )", true); }     // SPARQL extension

    // BNODE -> IRI (<_:....>) => string => IRI
    @Test public void term_constructor_iri_06() { eval("isIRI(IRI(str(IRI(BNODE()))))", true); }

    @Test public void iri_base_01() {
        parseEqualsTest("http://example/",  "IRI('x')",   "http://base/",     "IRI('x')",   false);
    }
    @Test public void iri_base_02() {
        parseEqualsTest("http://example/",  "IRI('x')",   "http://example/",  "IRI('x')",   true);
    }
    @Test public void iri_base_03() {
        parseEqualsTest("http://example/",  "IRI('x1')",  "http://example/",  "IRI('x2')",  false);
    }
    @Test public void iri_base_04() {
        parseEqualsTest("http://example/",  "IRI('x')",   "http://example/",  "URI('x')",   false);
    }
    @Test public void iri_base_05() {
        parseEqualsTest("http://example/",  "IRI(<base>, 'x')",   "http://base/",     "IRI(<base>, 'x')",   false);
    }
    @Test public void iri_base_06() {
        parseEqualsTest("http://example/",  "IRI(<base>, 'x')",   "http://example/",  "IRI(<base>, 'x')",   true);
    }
    @Test public void iri_base_07() {
        parseEqualsTest("http://example/",  "IRI(<base>, 'x1')",  "http://example/",  "IRI(<base>, 'x2')",  false);
    }
    @Test public void iri_base_08() {
        parseEqualsTest("http://example/",  "IRI(<base>, 'x')",   "http://example/",  "URI(<base>, 'x')",   false);
    }
    @Test public void iri_base_09() {
        parseEqualsTest("http://example/",  "IRI(<base1>, 'x')",  "http://example/",  "IRI(<base2>, 'x')",  false);
    }
    @Test public void iri_base_10() {
        parseEqualsTest("http://example/",  "IRI(<a/b/c>, 'x')",   "http://example/",  "IRI(<a/b/c>, <x>)",   false);
    }
    // One arg form.
    @Test public void iri_base_20() {
        evalTest("http://example/", "IRI('x')", "<http://example/x>");
    }
    @Test public void iri_base_21() {
        evalTest("http://example/", "IRI(<x>)", "<http://example/x>");
    }
    @Test public void iri_base_22() {
        evalTest("http://example/", "IRI(<http://host/x>)", "<http://host/x>");
    }
    // 2 arg foirm.
    @Test public void iri_base_30() {
        evalTest("http://example/", "IRI(<base>, 'x')",         "<http://example/x>");
    }
    @Test public void iri_base_31() {
        evalTest("http://example/", "IRI(<http://base/>, 'x')", "<http://base/x>");
    }
    @Test public void iri_base_32() {
        evalTest("http://example/", "IRI(<a/b/c>, 'x')",        "<http://example/a/b/x>");
    }

    private static void evalTest(String parserBase, String exprStr, String result) {
        Node expected = SSE.parseNode(result);
        Expr expr = expr(parserBase, exprStr);
        NodeValue nv = expr.eval(BindingFactory.binding(), new FunctionEnvBase());
        Node actual = nv.asNode();
        assertEquals(expected, actual);
    }

    private static void parseEqualsTest(String parserBase1, String exprStr1,
                                        String parserBase2, String exprStr2,
                                        boolean expected) {
        Expr expr1 = expr(parserBase1, exprStr1);
        Expr expr2 = expr(parserBase2, exprStr2);
        boolean b = expr1.equals(expr2);
        assertEquals(expected, b, ()->exprStr1+" equals "+exprStr2);
    }

    private static Expr expr(String parserBase, String exprStr) {
        return ExprUtils.parse(exprStr, (PrefixMapping)null, parserBase);
    }
    // end IRI and base

    @Test public void term_constructor_bnode_01()   { eval("isBlank(BNODE())", true); }
    @Test public void term_constructor_bnode_02()   { eval("isBlank(BNODE('abc'))", true); }
    @Test public void term_constructor_bnode_03()   { eval("isBlank(BNODE('abc'))", true); }
    @Test public void term_constructor_bnode_04()   { eval("BNODE('abc') = BNODE('abc')", true); }
    @Test public void term_constructor_bnode_05()   { eval("BNODE('abc') = BNODE('def')", false); }

    @Test public void term_constructor_strdt_01()   { eval("STRDT('123',xsd:integer) = 123", true); }
    @Test public void term_constructor_strdt_02()   { eval("STRDT('123',<http://example/DT>) = '123'^^<http://example/DT>", true); }
    @Test public void term_constructor_strdt_03()   { assertThrows(ExprEvalException.class, ()-> eval("STRDT('123','abc') = '123'") ); }
    @Test public void term_constructor_strdt_04()   { assertThrows(ExprEvalException.class, ()-> eval("STRDT('123'^^xsd:integer,<http://example/DT>) = '123'^^<http://example/DT>") ); }

    @Test public void term_constructor_strlang_01() { eval("STRLANG('abc', 'en') = 'abc'@en", true); }
    @Test public void term_constructor_strlang_02() { assertThrows(ExprEvalException.class, ()-> eval("STRLANG(<http://example/>, 'en') = 'abc'@en") ); }
    @Test public void term_constructor_strlang_03() { assertThrows(ExprEvalException.class, ()-> eval("STRLANG('abc'@en, 'en') = 'abc'@en") ); }

    @Test public void triple_term_cmp_01()
    { eval("<<(<ex:s> <ex:p> <ex:p>)>> = <<(<ex:s> <ex:p> <ex:p>)>>"); }

    @Test public void triple_term_cmp_02()
    { eval("<<(<ex:s> <ex:p> <ex:o1>)>> != <<(<ex:s> <ex:p> <ex:o2>)>>"); }

    @Test public void triple_term_cmp_03()
    { eval("<<(<ex:s> <ex:p> 1)>> < <<(<ex:s> <ex:p> 2)>>"); }

    @Test
    public void triple_term_cmp_04()
    { assertThrows(ExprEvalException.class, ()-> eval("<<(<ex:s> <ex:p1> 2)>> < <<(<ex:s> <ex:p2> 2)>>") ); }

    // XSD casts

    @Test public void xsd_cast_01()             { eval("xsd:integer('1') = 1", true); }
    @Test public void xsd_cast_02()             { eval("xsd:boolean('1') = true", true); }
    @Test public void xsd_cast_03()             { eval("sameTerm(xsd:double('1.0e0'),1.0e0)", true); }
    @Test public void xsd_cast_04()             { eval("xsd:double('1') = 1", true); }

    @Test public void xsd_cast_10()             { assertThrows(ExprEvalException.class, ()-> eval("xsd:integer(' 1')") ); }
    @Test public void xsd_cast_11()             { assertThrows(ExprEvalException.class, ()-> eval("xsd:boolean(' 1')") ); }
    @Test public void xsd_cast_12()             { assertThrows(ExprEvalException.class, ()-> eval("xsd:double(' 1.0e0')") ); }
    @Test public void xsd_cast_13()             { assertThrows(ExprEvalException.class, ()-> eval("xsd:double(' 1.0e0')") ); }

    // Dynamic Function Calls
    @Test public void dynamic_call_01()         { assertThrows(QueryParseException.class, ()-> eval("CALL()", false) ); }
    @Test public void dynamic_call_02()         { eval("CALL(xsd:double, '1') = 1"); }
    @Test public void dynamic_call_03()         { eval("CALL(fn:concat, 'A', 2+3 ) = 'A5'"); }

    @Test public void dynamic_call_10()         { assertThrows(ExprEvalException.class, ()-> eval("CALL(xsd:double)") ); }
    @Test public void dynamic_call_11()         { assertThrows(ExprEvalException.class, ()-> eval("CALL(xsd:noSuchFunc)") ); }

    // ---- Workers

    /*package*/ static void eval(String string) {
        eval(string, true);
    }

    // It's easier to write tests that simply are expected to return true/false
    /*package*/ static void eval(String string, boolean result) {
        Expr expr = ExprUtils.parse(string);
        NodeValue nv = expr.eval(null, LibTestExpr.createTest());
        boolean b = XSDFuncOp.effectiveBooleanValue(nv);
        assertEquals(result, b, ()->"Input="+string);
    }
}
