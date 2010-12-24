/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.pipeline;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.pipeline.normalize.CanonicalizeLiteral ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;

public class TestNormalization extends BaseTest
{
    // TODO lang tags
    
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
    @Test public void normalize_int_20()        { normalize("'-000'^^xsd:int", "0") ; }
    @Test public void normalize_int_21()        { normalize("'0'^^xsd:int", "0") ; }
    @Test public void normalize_int_22()        { normalize("'1'^^xsd:long", "1") ; }
    @Test public void normalize_int_23()        { normalize("'100'^^xsd:unsignedInt", "100") ; }
    @Test public void normalize_int_24()        { normalize("'-100'^^xsd:nonPositiveInteger", "-100") ; }
    @Test public void normalize_int_25()        { normalize("'+100'^^xsd:positiveInteger", "100") ; }
    
    @Test public void normalize_decimal_01()    { normalize("0.0", "0.0") ; }
    @Test public void normalize_decimal_02()    { normalize("'0'^^xsd:decimal", "0.0") ; }
    @Test public void normalize_decimal_03()    { normalize("1.0", "1.0") ; }
    @Test public void normalize_decimal_04()    { normalize("1.1", "1.1") ; }
    @Test public void normalize_decimal_05()    { normalize("0001.10", "1.1") ; }

    @Test public void normalize_decimal_06()    { normalize("-0.0", "0.0") ; }
    @Test public void normalize_decimal_07()    { normalize("+0.0", "0.0") ; }
    @Test public void normalize_decimal_08()    { normalize("+00560.0", "560.0") ; }
    @Test public void normalize_decimal_09()    { normalize("-1.0", "-1.0") ; }

    @Test public void normalize_decimal_10()    { normalize("-1.0", "-1.0") ; }
    @Test public void normalize_decimal_11()    { normalize("+1.0", "1.0") ; }
    @Test public void normalize_decimal_12()    { normalize("+1.0001", "1.0001") ; }
    @Test public void normalize_decimal_13()    { normalize("-1.000100", "-1.0001") ; }
    @Test public void normalize_decimal_14()    { normalize("'-1'^^xsd:decimal", "-1.0") ; }
    @Test public void normalize_decimal_15()    { normalize("'0'^^xsd:decimal", "0.0") ; }
    
    // Check - what about exponent normalization?
    @Test public void normalize_double_01()     { normalize("1e0", "1.0E0") ; }
    @Test public void normalize_double_02()     { normalize("0e0", "0.0E0") ; }
    @Test public void normalize_double_03()     { normalize("00e0", "0.0E0") ; }
    @Test public void normalize_double_04()     { normalize("0e00", "0.0E0") ; }
    @Test public void normalize_double_05()     { normalize("10e0", "1.0E1") ; }

    @Test public void normalize_double_10()     { normalize("'-1e+0'^^xsd:double", "-1.0E0") ; }
    @Test public void normalize_double_11()     { normalize("'+0e01'^^xsd:double", "0.0E0") ; }
    @Test public void normalize_double_12()     { normalize("'1000'^^xsd:double", "1.0E3") ; }
    @Test public void normalize_double_13()     { normalize("+1.e4", "1.0E4") ; }
    @Test public void normalize_double_14()     { normalize("+12345.6789e+9", "1.23456789E13") ; }
    @Test public void normalize_double_15()     { normalize("-12345.6789e+9", "-1.23456789E13") ; }
    @Test public void normalize_double_16()     { normalize("+12345.6789e-9", "1.23456789E-5") ; }
    @Test public void normalize_double_17()     { normalize("-12345.6789e-9", "-1.23456789E-5") ; }
    
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
    @Test public void normalize_lang_04()       { normalizeLang("'abc'@EN-UK", "'abc'@en-UK") ; }
    @Test public void normalize_lang_05()       { normalizeLang("'abc'@EN", "'abc'@EN", false) ; }
    @Test public void normalize_lang_06()       { normalizeLang("'abc'@EN-UK", "'abc'@en-uk", false) ; }

    private static void normalize(String input, String expected)
    {
        Node n1 = NodeFactory.parseNode(input) ;
        assertTrue("Invalid lexical form", n1.getLiteralDatatype().isValid(n1.getLiteralLexicalForm()));
        
        Node n2 = CanonicalizeLiteral.get().convert(n1) ;
        Node n3 = NodeFactory.parseNode(expected) ;
        assertEquals("Invalid canonicalization (lex)", n3.getLiteralLexicalForm(), n2.getLiteralLexicalForm()) ;
        assertEquals("Invalid canonicalization (node)", n3, n2) ;
    }

    private static void normalizeLang(String input, String expected)
    { normalizeLang(input, expected, true) ; }
    
    private static void normalizeLang(String input, String expected, boolean correct)
    {
        Node n1 = NodeFactory.parseNode(input) ;
        Node n2 = CanonicalizeLiteral.get().convert(n1) ;
        Node n3 = NodeFactory.parseNode(expected) ;
        if ( correct )
        {
            assertEquals("Invalid canonicalization (lang)", n3.getLiteralLanguage(), n2.getLiteralLanguage()) ;
            assertEquals("Invalid canonicalization (node)", n3, n2) ;
        }
        else
        {
            assertNotEquals("Invalid canonicalization (lang)", n3.getLiteralLanguage(), n2.getLiteralLanguage()) ;
            assertNotEquals("Invalid canonicalization (node)", n3, n2) ;
        }
    }
    

    private static void normalizeDT(String input, String expected)
    {
        normalize("'"+input+"'^^xsd:dateTime",
                  "'"+expected+"'^^xsd:dateTime") ;
    }

}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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