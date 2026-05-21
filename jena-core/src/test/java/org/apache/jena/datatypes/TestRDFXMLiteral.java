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

package org.apache.jena.datatypes;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Test;

public class TestRDFXMLiteral {

    @Test
    public void rdfxmlLiteral_1() {
        test("<x></x>", "<x/>", false, false, true);
    }

    @Test
    public void rdfxmlLiteral_2() {
        test("<x b='8' a='123'></x>", "<x     a='123' b='8'/>", false, false, true);
    }

    @Test
    public void rdfxmlLiteral_3() {
        test("<x a:b='8' xmlns:a='http://ex/'></x>", "<x a:b='8' xmlns:a='http://ex/'></x>", true, true, true);
    }

    @Test
    public void rdfxmlLiteral_4() {
        test("<x b='8' xmlns:a='http://ex/'></x>", "<x   b='8'  xmlns:a='http://ex/'    ></x>", false, false, true);
    }

    @Test
    public void rdfxmlLiteral_illgeal_1() {
        test("<x>", "<x>", true, true, true);
    }

    @Test
    public void rdfxmlLiteral_illgeal_2() {
        test("<x>", "<y>", false, false, false);
    }

    private static void test(String lex1, String lex2, boolean javaEquals, boolean sameTerm, boolean sameValue) {
        Node n1 = NodeFactory.createLiteralDT(lex1, RDF.dtXMLLiteral);
        Node n2 = NodeFactory.createLiteralDT(lex2, RDF.dtXMLLiteral);
        assertEquals(javaEquals, n1.equals(n2));
        assertEquals(sameTerm, n1.sameTermAs(n2));
        assertEquals(sameValue, n1.sameValueAs(n2));
    }
}
