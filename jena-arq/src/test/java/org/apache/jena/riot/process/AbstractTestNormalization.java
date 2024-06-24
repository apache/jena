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

package org.apache.jena.riot.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.util.NodeFactoryExtra ;
import org.junit.Test ;

public abstract class AbstractTestNormalization
{
    @Test public void normalize_int_01()        { normalize("23", "23") ; }
    @Test public void normalize_int_02()        { normalize("023", "23") ; }
    @Test public void normalize_int_03()        { normalize("+23", "23") ; }
    @Test public void normalize_int_04()        { normalize("+023", "23") ; }
    @Test public void normalize_int_05()        { normalize("-23", "-23") ; }
    @Test public void normalize_int_06()        { normalize("-0230", "-230") ; }
    @Test public void normalize_int_07()        { normalize("0", "0") ; }
    @Test public void normalize_int_08()        { normalize("00", "0") ; }
    @Test public void normalize_int_09()        { normalize("+00", "0") ; }
    @Test public void normalize_int_10()        { normalize("-0", "0") ; }
    @Test public void normalize_int_11()        { normalize("-000", "0") ; }

    // Subtypes of integer
    @Test public void normalize_int_20()        { normalize("'-000'^^xsd:int", "'0'^^xsd:int") ; }
    @Test public void normalize_int_21()        { normalize("'0'^^xsd:int", "'0'^^xsd:int") ; }
    @Test public void normalize_int_22()        { normalize("'1'^^xsd:long", "'1'^^xsd:long") ; }
    @Test public void normalize_int_23()        { normalize("'0100'^^xsd:unsignedInt", "'100'^^xsd:unsignedInt") ; }
    @Test public void normalize_int_24()        { normalize("'-100'^^xsd:nonPositiveInteger", "'-100'^^xsd:nonPositiveInteger") ; }
    @Test public void normalize_int_25()        { normalize("'+100'^^xsd:positiveInteger", "'100'^^xsd:positiveInteger") ; }

    @Test public void normalize_decimal_01()    { normalize("0.0", "0.0") ; }
    @Test public void normalize_decimal_02()    { normalize("'0'^^xsd:decimal", "0.0") ; }
    @Test public void normalize_decimal_03()    { normalize("1.0", "1.0") ; }
    @Test public void normalize_decimal_04()    { normalize("1.1", "1.1") ; }
    @Test public void normalize_decimal_05()    { normalize("0001.10", "1.1") ; }

    @Test public void normalize_decimal_06()    { normalize("-0.0", "0.0") ; }
    @Test public void normalize_decimal_07()    { normalize("+0.0", "0.0") ; }
    @Test public void normalize_decimal_08()    { normalize("+00560.0", "560.0") ; }

    @Test public void normalize_decimal_10()    { normalize("-1.0", "-1.0") ; }
    @Test public void normalize_decimal_11()    { normalize("+1.0", "1.0") ; }
    @Test public void normalize_decimal_12()    { normalize("+1.0001", "1.0001") ; }
    @Test public void normalize_decimal_13()    { normalize("-1.000100", "-1.0001") ; }
    @Test public void normalize_decimal_14()    { normalize("'-1'^^xsd:decimal", "-1.0") ; }
    @Test public void normalize_decimal_15()    { normalize("'0'^^xsd:decimal", "0.0") ; }

    @Test public void normalize_double_01()     { normalize("'1e0'^^xsd:double", "1.0e0") ; }
    @Test public void normalize_double_02()     { normalize("'0e0'^^xsd:double", "0.0e0") ; }
    @Test public void normalize_double_03()     { normalize("'00e0'^^xsd:double", "0.0e0") ; }
    @Test public void normalize_double_04()     { normalize("'0e00'^^xsd:double", "0.0e0") ; }
    @Test public void normalize_double_05()     { normalize("'10e0'^^xsd:double", "10.0e0") ; }
    @Test public void normalize_double_06()     { normalize("'1e1'^^xsd:double", "10.0e0") ; }

    @Test public void normalize_double_10()     { normalize("'-1e+0'^^xsd:double", "-1.0e0") ; }
    @Test public void normalize_double_11()     { normalize("'+0e01'^^xsd:double", "0.0e0") ; }
    @Test public void normalize_double_12()     { normalize("'1000'^^xsd:double", "1000.0e0") ; }
    @Test public void normalize_double_13()     { normalize("'+1.e4'^^xsd:double", "10000.0e0"); }
    @Test public void normalize_double_14()     { normalize("'+12345.6789e+9'^^xsd:double", "1.23456789E13") ; }
    @Test public void normalize_double_15()     { normalize("'-12345.6789e+9'^^xsd:double", "-1.23456789E13") ; }
    @Test public void normalize_double_16()     { normalize("'+12345.6789e-9'^^xsd:double", "1.23456789E-5") ; }
    @Test public void normalize_double_17()     { normalize("'-12345.6789e-9'^^xsd:double", "-1.23456789E-5") ; }
    // Double.toString switches to scientific notation at 1e7 and 1e-4
    @Test public void normalize_double_18()     { normalize("'1e7'^^xsd:double", "1.0E7") ; }
    @Test public void normalize_double_19()     { normalize("'-1e7'^^xsd:double", "-1.0E7") ; }
    @Test public void normalize_double_20()     { normalize("'1e-3'^^xsd:double", "0.001e0") ; }
    @Test public void normalize_double_21()     { normalize("'1e-4'^^xsd:double", "1.0E-4") ; }

    // Excessive precision
    @Test public void normalize_double_25()     { normalize("'-1.23456789012345678901234'^^xsd:double", "-1.2345678901234567e0") ; }

    @Test public void normalize_double_30()     { normalize("'NaN'^^xsd:double",    "'NaN'^^xsd:double") ; }
    @Test public void normalize_double_31()     { normalize("'INF'^^xsd:double",    "'INF'^^xsd:double"); }
    @Test public void normalize_double_32()     { normalize("'+INF'^^xsd:double",   "'INF'^^xsd:double"); }
    @Test public void normalize_double_33()     { normalize("'-INF'^^xsd:double",   "'-INF'^^xsd:double"); }
    @Test public void normalize_double_34()     { normalize("'-0'^^xsd:double",     "'-0.0e0'^^xsd:double"); }
    @Test public void normalize_double_35()     { normalize("'+0'^^xsd:double",     "'0.0e0'^^xsd:double"); }

    @Test public void normalize_float_01()     { normalize("'1e0'^^xsd:float",   "'1.0'^^xsd:float"); }
    @Test public void normalize_float_02()     { normalize("'0e0'^^xsd:float",   "'0.0'^^xsd:float"); }
    @Test public void normalize_float_03()     { normalize("'00e0'^^xsd:float",  "'0.0'^^xsd:float"); }
    @Test public void normalize_float_04()     { normalize("'0e00'^^xsd:float",  "'0.0'^^xsd:float"); }
    @Test public void normalize_float_05()     { normalize("'10e0'^^xsd:float",  "'10.0'^^xsd:float"); }
    @Test public void normalize_float_06()     { normalize("'1e01'^^xsd:float",  "'10.0'^^xsd:float"); }

    // Float.toString switches to scientific notation at 1e7 and 1e-4
    @Test public void normalize_float_18()     { normalize("'1e7'^^xsd:float",   "'1.0E7'^^xsd:float") ; }
    @Test public void normalize_float_19()     { normalize("'-1e7'^^xsd:float",  "'-1.0E7'^^xsd:float") ; }
    @Test public void normalize_float_20()     { normalize("'1e-3'^^xsd:float",  "'0.001'^^xsd:float") ; }
    @Test public void normalize_float_21()     { normalize("'1e-4'^^xsd:float",  "'1.0E-4'^^xsd:float") ; }

    // Excessive precision
    @Test public void normalize_float_25()     { normalize("'1.234567890'^^xsd:float", "'1.2345679'^^xsd:float"); }

    @Test public void normalize_float_30()     { normalize("'NaN'^^xsd:float",    "'NaN'^^xsd:float") ; }
    @Test public void normalize_float_31()     { normalize("'INF'^^xsd:float",    "'INF'^^xsd:float") ; }
    @Test public void normalize_float_32()     { normalize("'+INF'^^xsd:float",   "'INF'^^xsd:float") ; }
    @Test public void normalize_float_33()     { normalize("'-INF'^^xsd:float",   "'-INF'^^xsd:float") ; }
    @Test public void normalize_float_34()     { normalize("'-0'^^xsd:float",     "'-0.0'^^xsd:float"); }
    @Test public void normalize_float_35()     { normalize("'+0'^^xsd:float",     "'0.0'^^xsd:float"); }

    @Test public void normalize_datetime_01()   { normalizeDT("1984-01-01T07:07:07",    "1984-01-01T07:07:07") ; }
    @Test public void normalize_datetime_02()   { normalizeDT("1984-01-01T07:07:07.0",  "1984-01-01T07:07:07") ; }
    @Test public void normalize_datetime_03()   { normalizeDT("1984-01-01T07:07:07.00", "1984-01-01T07:07:07") ; }
    @Test public void normalize_datetime_04()   { normalizeDT("1984-01-01T07:07:07.01", "1984-01-01T07:07:07.01") ; }
    @Test public void normalize_datetime_05()   { normalizeDT("1984-01-01T07:07:07.010","1984-01-01T07:07:07.01") ; }

    @Test public void normalize_boolean_01()    { normalize("'true'^^xsd:boolean",  "'true'^^xsd:boolean") ; }
    @Test public void normalize_boolean_02()    { normalize("'false'^^xsd:boolean", "'false'^^xsd:boolean") ; }
    @Test public void normalize_boolean_03()    { normalize("'1'^^xsd:boolean",     "'true'^^xsd:boolean") ; }
    @Test public void normalize_boolean_04()    { normalize("'0'^^xsd:boolean",     "'false'^^xsd:boolean") ; }

    @Test public void normalize_lang_01()       { normalizeLang("''", "''") ; }
    @Test public void normalize_lang_02()       { normalizeLang("'abc'", "'abc'") ; }
    @Test public void normalize_lang_03()       { normalizeLang("'abc'@EN", "'abc'@en") ; }
    @Test public void normalize_lang_04()       { normalizeLang("'abc'@EN-GB", "'abc'@en-GB") ; }
    @Test public void normalize_lang_05()       { normalizeLang("'abc'@EN-LATN-GB", "'abc'@en-Latn-GB") ; }

    protected abstract Node normalize(Node node);

    protected void normalize(String input, String expected) {
        Node n1 = NodeFactoryExtra.parseNode(input);

        String lex1 = n1.getLiteralLexicalForm();
        RDFDatatype dt1 =  n1.getLiteralDatatype();
            assertTrue("Invalid input lexical form", dt1.isValid(lex1));

        Node n2 = normalize(n1);
        Node n3 = NodeFactoryExtra.parseNode(expected);
        assertEquals("Different datatype", n3.getLiteralDatatype(), n2.getLiteralDatatype());
        assertEquals("Invalid canonicalization (lex)", n3.getLiteralLexicalForm(), n2.getLiteralLexicalForm());
        assertEquals("Invalid canonicalization (node)", n3, n2);
    }

    protected void normalizeLang(String input, String expected) {
        Node n1 = NodeFactoryExtra.parseNode(input);
        Node n2 = normalize(n1);
        Node n3 = NodeFactoryExtra.parseNode(expected);
        assertEquals("Invalid canonicalization (lang)", n3.getLiteralLanguage(), n2.getLiteralLanguage());
        assertEquals("Invalid canonicalization (node)", n3, n2);
    }

    protected void normalizeDT(String input, String expected) {
        normalize("'" + input + "'^^xsd:dateTime", "'" + expected + "'^^xsd:dateTime");
    }
}
