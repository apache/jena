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

package org.apache.jena.riot.system;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.ErrorHandlerTestLib;
import org.apache.jena.riot.ErrorHandlerTestLib.ExWarning;
import org.apache.jena.shared.impl.JenaParameters;
import org.apache.jena.sparql.util.NodeFactoryExtra;

/** Tests for node Checker */
public class TestChecker
{
    private boolean value_enableWhitespaceCheckingOfTypedLiterals;

    @BeforeEach
    public void before() {
        value_enableWhitespaceCheckingOfTypedLiterals = JenaParameters.enableWhitespaceCheckingOfTypedLiterals;
        // The default is false which allows whitespace around integers.
        // Jena seems to allow white space around dateTimes either way.
        // JenaParameters.enableWhitespaceCheckingOfTypedLiterals = true;
    }

    @AfterEach
    public void after() {
        JenaParameters.enableWhitespaceCheckingOfTypedLiterals = value_enableWhitespaceCheckingOfTypedLiterals;
    }

    @Test
    public void checker_uri_01()    { check("<http://example/x>"); }

    @Test
    public void checker_uri_02()    { checkWarning("<x>"); }

    @Test
    public void checker_uri_03()    { checkWarning("<urn:abc>"); }

    @Test
    public void checker_uri_04()    { checkWarning("<urn:x:bc>"); }

    @Test
    public void checker_uri_05()    { check("<urn:abc:y>"); }

    @Test
    public void checker_uri_06()    { checkWarning("<URN:abc:y>"); }

    @Test
    public void checker_uri_07()    { checkWarning("<http://example:80/>"); }

    @Test
    public void checker01()         { check("''"); }

    @Test
    public void checker02()         { check("''@en"); }

    @Test
    public void checker10()         { checkWarning("''^^xsd:dateTime"); }

    // Whitespace facet processing.
    // Strictly illegal RDF but Jena accepts them.
    // See JenaParameters.enableWhitespaceCheckingOfTypedLiterals

    @Test
    public void checker11()         { check("'  2010-05-19T01:01:01.01+01:00'^^xsd:dateTime"); }

    @Test
    public void checker12()         { check("'\\n2010-05-19T01:01:01.01+01:00\\t\\r  '^^xsd:dateTime"); }

    @Test
    public void checker13()         { check("' 123'^^xsd:integer"); }

    // Internal white space - illegal
    @Test
    public void checker14()     { checkWarning("'12 3'^^xsd:integer"); }

    @Test
    public void checker15()     { check("'\\n123'^^xsd:integer"); }

    // Test all the data type hierarchies that whitespace foo affects.
    @Test
    public void checker16()     { check("'123.0  '^^xsd:float"); }

    @Test
    public void checker17()     { check("'123.0\\n'^^xsd:double"); }

    @Test
    public void checker18()     { checkWarning("'\\b123.0\\n'^^xsd:double"); }

    // Other bad lexical forms.
    @Test
    public void checker20()     { checkWarning("'XYZ'^^xsd:integer"); }

    // Lang tag
    @Test
    public void checker21()     { checkWarning("'XYZ'@abcdefghijklmn"); }


    @Test
    public void checker30()     { checkWarning("<http://base/[]iri>"); }

    // XML Literals.

    @Test
    public void checker40()     { check("\"<x></x>\"^^rdf:XMLLiteral"); }

    @Test
    // Unmatched tag
    public void checker41()     { checkWarning("\"<x>\"^^rdf:XMLLiteral"); }

    @Test
    // Unmatched tag
    public void checker42()     { checkWarning("\"</x>\"^^rdf:XMLLiteral"); }

    @Test
    // Bad tagging.
    public void checker43()     { checkWarning("\"<x><y></x></y>\"^^rdf:XMLLiteral"); }

    // Not exclusive canonicalization - bad in RDF 1.0
    @Test // Valid RDF 1.1
    public void checker44()     { check("\"<x/>\"^^rdf:XMLLiteral"); }

    @Test
    public void checker45()     { check("'''<x xmlns=\"http://example/ns#\" attr=\"foo\"></x>'''^^rdf:XMLLiteral" ); }

    // Exclusive canonicalization requires namespace declaration before attributes - bad in RDF 1.0
    @Test // Valid RDF 1.1
    public void checker46()     { check("'''<x attr=\"foo\" xmlns=\"http://example/ns#\"></x>'''^^rdf:XMLLiteral"); }

    private static ErrorHandler errorHandler = new ErrorHandlerTestLib.ErrorHandlerEx();

    private static void checkWarning(String string) {
        assertThrows(ExWarning.class, ()->check(string));
    }

    private static void check(String string) {
        Node n = NodeFactoryExtra.parseNode(string);
        Checker.check(n, errorHandler, -1, -1);
    }
}
