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

package org.apache.jena.datatypes;

import static org.junit.Assert.*;

import org.apache.jena.datatypes.xsd.impl.XMLLiteralType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

public class TestRDFXMLiteral {
    // Different RDF terms with the same value.

    @Test public void rdfxmlLiteral_1() {
        // Normalization -> different terms, same value
        test("<x></x>", "<x/>", false, false, true);
    }

    @Test public void rdfxmlLiteral_2() {
        // Normalization -> different terms, same value
        test("<x b='8' a='123'></x>", "<x     a='123' b='8'/>", false, false, true);
    }

    @Test public void rdfxmlLiteral_3() {
        // Same term.
        test("<x a:b='8' xmlns:a='http://ex/'></x>", "<x a:b='8' xmlns:a='http://ex/'></x>", true, true, true);
    }

    @Test public void rdfxmlLiteral_4() {
        // Different term by trivial white space (removed by XML Node normalization)
        test("<x b='8' xmlns:a='http://ex/'></x>","<x   b='8'  xmlns:a='http://ex/'    ></x>", false, false, true);
    }

    // Lexical forms do not conform to the lexical space of legal XML fragments.
    @Test public void rdfxmlLiteral_illgeal_1() {
        test("<x>", "<x>", true, true, true);
    }

    @Test public void rdfxmlLiteral_illgeal_2() {
        test("<x>", "<y>", false, false, false);
    }

    // ----

    private static void test(String lex1, String lex2, boolean javaEquals, boolean sameTerm, boolean sameValue) {
        Node n1 = NodeFactory.createLiteralDT(lex1, XMLLiteralType.rdfXMLLiteral);
        Node n2 = NodeFactory.createLiteralDT(lex2, XMLLiteralType.rdfXMLLiteral);
        assertEquals(javaEquals, n1.equals(n2));
        assertEquals(sameTerm, n1.sameTermAs(n2));
        assertEquals(sameValue, n1.sameValueAs(n2));
    }

}
