/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.graph;

import static org.apache.jena.graph.TextDirection.RTL;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.shared.JenaException;
import org.apache.jena.vocabulary.RDF;

/**
 * Tests for create RDF Terms (@link Node Nodes}) involving strings (xsd:string,
 * rdf:langString, rdf:dirLangString). Base direction introduced in RDF 1.2.
 */
public class TestRDFStringLiterals {

    private static TextDirection noTextDirection = Node.noTextDirection;
    private static String noLangTag = Node.noLangTag;

    // ---- xsd:string

    @Test
    public void string01() {
        Node n = NodeFactory.createLiteralString("abc");
        test(n, "abc", "", noTextDirection, XSDDatatype.XSDstring, "abc");
    }

    @Test
    public void string02() {
        Node n = NodeFactory.createLiteralLang("abc", null);
        test(n, "abc", "", null, XSDDatatype.XSDstring, "abc");
    }

    @Test
    public void string03() {
        Node n = NodeFactory.createLiteralDirLang("abc", null, (String)null);
        test(n, "abc", "", null, XSDDatatype.XSDstring, "abc");
    }

    @Test
    public void string04() {
        Node n = NodeFactory.createLiteralDirLang("abc", "", "");
        test(n, "abc", "", null, XSDDatatype.XSDstring, "abc");
    }

    // ---- rdf:langString

    @Test
    public void strLang01() {
        Node n = NodeFactory.createLiteralLang("abc", "en");
        test(n, "abc", "en", null, RDF.dtLangString, "abc@en");
    }

    @Test
    public void strLang02() {
        Node n = NodeFactory.createLiteralLang("abc", "EN");
        test(n, "abc", "en", null, RDF.dtLangString, "abc@en");
    }

    @Test
    public void strLang03() {
        Node n = NodeFactory.createLiteralLang("abc", "");
        test(n, "abc", "", null, XSDDatatype.XSDstring, "abc");
    }

    // Make with explicit no base direction.

    @Test
    public void strLang04() {
        Node n = NodeFactory.createLiteralDirLang("abc", null, (String)null);
        test(n, "abc", "", null, XSDDatatype.XSDstring, "abc");
    }

    @Test
    public void strLang05() {
        // "" is a convenience of no direction.
        Node n = NodeFactory.createLiteralDirLang("abc", "", "");
        test(n, "abc", "", null, XSDDatatype.XSDstring, "abc");
    }

    // language tags
    @Test
    public void strLangTag01() {
        Node n = NodeFactory.createLiteralLang("abc", "en");
        test(n, "abc", "en", null, RDF.dtLangString, "abc@en");
    }

    @Test
    public void strLangTag02() {
        Node n = NodeFactory.createLiteralLang("abc", "EN-GB");
        test(n, "abc", "en-GB", null, RDF.dtLangString, "abc@en-GB");
    }

    @Test
    public void strLangTag03() {
        // "" is a convenience of no direction.
        Node n = NodeFactory.createLiteralLang("abc", "EN-LATN-GB");
        test(n, "abc", "en-Latn-GB", null, RDF.dtLangString, "abc@en-Latn-GB");
    }

    // ---- rdf:dirLangString

    @Test
    public void dirLangString01() {
        Node n = NodeFactory.createLiteralDirLang("abc", "en", "rtl");
        test(n, "abc", "en", RTL, RDF.dtDirLangString, "abc@en");
    }

    @Test
    public void dirLangString02() {
        assertThrows(JenaException.class, ()->{
            Node n = NodeFactory.createLiteralDirLang("abc", "en", "LTR");
        });
    }

    @Test
    public void dirLangString03() {
        assertThrows(JenaException.class, ()->{
            Node n = NodeFactory.createLiteralDirLang("abc", "en", "unk");
        });
    }

    @Test
    public void dirLangString04() {
        Node n = NodeFactory.createLiteralDirLang("abc", "en", "");
        test(n, "abc", "en", null, RDF.dtLangString, "abc@en");
    }

    @Test
    public void dirLangString05() {
        assertThrows(JenaException.class, ()->{
            Node n = NodeFactory.createLiteralDirLang("abc", "en", "x");
        });
    }

    // -- Via createLiteralLang splitting lang tags on "--"
    @Test
    public void dirLangString10() {
        Node n = NodeFactory.createLiteralLang("abc", "en--rtl");
        test(n, "abc", "en", RTL, RDF.dtDirLangString, "abc@en");
    }

    @Test
    public void dirLangString_equality() {
        // Equality of rdf:dirLangString
        Node nDirLangString1 = NodeFactory.createLiteralLang("abc", "en--ltr");
        Node nDirLangString2 = NodeFactory.createLiteralLang("abc", "en--ltr");
        assertEquals(nDirLangString1, nDirLangString2);

        Node nDirLangString3 = NodeFactory.createLiteralLang("abc", "en--rtl");
        assertNotEquals(nDirLangString1, nDirLangString3);

        Node nDirLangString4 = NodeFactory.createLiteralLang("abc", "en");
        assertNotEquals(nDirLangString1, nDirLangString4);
    }

    @Test
    public void dirLangString11() {
        assertThrows(JenaException.class, ()->{
            Node n = NodeFactory.createLiteralLang("abc", "en--LTR");
        });
    }

    @Test
    public void dirLangString12() {
        assertThrows(JenaException.class, ()->{
            Node n = NodeFactory.createLiteralLang("abc", "en--");
        });
    }

    // Errors

    @Test
    public void rdfStringBad01() {
        assertThrows(JenaException.class, ()->{
            // No lang but with a direction
            Node n = NodeFactory.createLiteralDirLang("abc", null, TextDirection.LTR);
        });
    }

    @Test
    public void rdfStringBad02() {
        assertThrows(JenaException.class, ()->{
            // No lang but with a direction
            Node n = NodeFactory.createLiteralDirLang("abc", "", TextDirection.LTR);
        });
    }

    @Test
    public void rdfStringBad03() {
        assertThrows(NullPointerException.class, ()->{
            Node n = NodeFactory.createLiteralString((String)null);
        });
    }

    @Test
    public void rdfStringBad04() {
        assertThrows(NullPointerException.class, ()->{
            Node n = NodeFactory.createLiteralLang((String)null, "en");
        });
    }

    @Test
    public void rdfStringBad05() {
        assertThrows(NullPointerException.class, ()->{
            Node n = NodeFactory.createLiteralDirLang((String)null, "en", TextDirection.LTR);
        });
    }

    // ----

    private static void test(Node node, String lexicalForm, String lang, TextDirection textDir, RDFDatatype datatype,
                             String indexingValue) {
        assertEquals(lexicalForm, node.getLiteralLexicalForm(), "Lexical form:");
        assertEquals(lang, node.getLiteralLanguage(), "Language:");
        assertEquals(textDir, node.getLiteralBaseDirection(), "Text Direction:");
        assertEquals(datatype, node.getLiteralDatatype(), "Datatype:");
        assertEquals(indexingValue, node.getIndexingValue(), "Indexing:");
    }
}
