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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.impl.Util;
import org.junit.Test;

/** Testing making string-like RDF terms */
public class TestNodeCreateStrings {

    // -- xsd:string

    @Test
    public void testIsSimpleString1() {
        Node n = NodeFactory.createLiteralString("abc");
        assertTrue(Util.isSimpleString(n));
        assertFalse(Util.isLangString(n));
        assertFalse(Util.isDirLangString(n));
        assertFalse(Util.hasLang(n));
        assertFalse(Util.hasDirection(n));
    }

    @Test
    public void testIsSimpleString2() {
        Node n = NodeFactory.createLiteralLang("abc", "");
        assertTrue(Util.isSimpleString(n));
        assertFalse(Util.isLangString(n));
        assertFalse(Util.isDirLangString(n));
        assertFalse(Util.hasLang(n));
        assertFalse(Util.hasDirection(n));
    }

    @Test
    public void testIsSimpleString3() {
        Node n = NodeFactory.createLiteralDirLang("abc", "", (String)null);
        assertTrue(Util.isSimpleString(n));
        assertFalse(Util.isLangString(n));
        assertFalse(Util.isDirLangString(n));
        assertFalse(Util.hasLang(n));
        assertFalse(Util.hasDirection(n));
    }

    // -- rdf:langString

    @Test
    public void testIsLangString1() {
        Node n = NodeFactory.createLiteralLang("abc", "en-GB");
        assertFalse(Util.isSimpleString(n));
        assertTrue(Util.isLangString(n));
        assertFalse(Util.isDirLangString(n));
        assertTrue(Util.hasLang(n));
        assertFalse(Util.hasDirection(n));
    }

    @Test
    public void testIsLangString2() {
        Node n = NodeFactory.createLiteralDirLang("abc", "en-GB", (String)null);
        assertFalse(Util.isSimpleString(n));
        assertTrue(Util.isLangString(n));
        assertFalse(Util.isDirLangString(n));
        assertTrue(Util.hasLang(n));
        assertFalse(Util.hasDirection(n));
    }

    // -- rdf:dirLangString (only one way to make it)

    @Test
    public void testIsDirLangString1() {
        Node n = NodeFactory.createLiteralDirLang("abc", "en-GB", "ltr");
        assertFalse(Util.isSimpleString(n));
        assertFalse(Util.isLangString(n));
        assertTrue(Util.isDirLangString(n));
        assertTrue(Util.hasLang(n));
        assertTrue(Util.hasDirection(n));
    }
}
