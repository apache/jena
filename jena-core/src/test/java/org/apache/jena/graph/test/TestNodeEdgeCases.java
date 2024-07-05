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

package org.apache.jena.graph.test;

import static org.junit.Assert.assertNotNull;

import org.apache.jena.datatypes.xsd.impl.RDFDirLangString;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

public class TestNodeEdgeCases {

    // Corner cases of NodeFactory
    @Test public void term_create_01() {
        // "abc"^^rdf:langString (no language)
        Node node1 = NodeFactory.createLiteral("abc", null, (String)null, RDFLangString.rdfLangString);
        assertNotNull(node1);
        Node node2 = NodeFactory.createLiteralDT("abc", RDFLangString.rdfLangString);
        assertNotNull(node2);
    }

    @Test public void term_create_02() {
        // "abc"^rdf:dirLangString (no language, no initial text direction)
        Node node1 = NodeFactory.createLiteral("abc", null, (String)null, RDFDirLangString.rdfDirLangString);
        assertNotNull(node1);
        Node node2 = NodeFactory.createLiteralDT("abc", RDFDirLangString.rdfDirLangString);
        assertNotNull(node2);
    }
}
