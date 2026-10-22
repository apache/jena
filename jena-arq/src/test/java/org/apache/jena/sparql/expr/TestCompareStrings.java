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

package org.apache.jena.sparql.expr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;

/** Comparisons of RDF strings in Unicode code point order. */
public class TestCompareStrings {
    private static final String BMP = Character.toString(0xFFE0);
    private static final String SUPPLEMENTARY = Character.toString(0x1D11E);
    private static final String LATER_SUPPLEMENTARY = Character.toString(0x1F600);

    @Test public void bmpBeforeSupplementary() {
        // UTF-16 code unit order gives the opposite result for this pair.
        compare(BMP, SUPPLEMENTARY, Expr.CMP_LESS);
        compare(SUPPLEMENTARY, BMP, Expr.CMP_GREATER);
        compare("a" + BMP, "a" + SUPPLEMENTARY, Expr.CMP_LESS);
    }

    @Test public void supplementaryCodePoints() {
        compare(SUPPLEMENTARY, LATER_SUPPLEMENTARY, Expr.CMP_LESS);
        compare("a" + SUPPLEMENTARY + "a", "a" + SUPPLEMENTARY + "b", Expr.CMP_LESS);
    }

    @Test public void equalStrings() {
        compare("", "", Expr.CMP_EQUAL);
        compare(SUPPLEMENTARY + BMP, SUPPLEMENTARY + BMP, Expr.CMP_EQUAL);
    }

    @Test public void prefixesAndEmptyStrings() {
        compare("", SUPPLEMENTARY, Expr.CMP_LESS);
        compare(SUPPLEMENTARY, SUPPLEMENTARY + BMP, Expr.CMP_LESS);
        compare("a" + SUPPLEMENTARY, "a" + SUPPLEMENTARY + "b", Expr.CMP_LESS);
    }

    @Test public void longCommonPrefix() {
        String prefix = "http://example.org/resource/with/a/long/shared/prefix/";
        compare(prefix + "a", prefix + "b", Expr.CMP_LESS);
        compare(prefix + BMP, prefix + SUPPLEMENTARY, Expr.CMP_LESS);
    }

    @Test public void stringValueEquality() {
        String prefix = "http://example.org/resource/with/a/long/shared/prefix/";
        NodeValue bmp = NodeValue.makeString(prefix + BMP);
        NodeValue supplementary = NodeValue.makeString(prefix + SUPPLEMENTARY);
        assertFalse(NodeValue.sameValueAs(bmp, supplementary));
        assertTrue(NodeValue.sameValueAs(bmp, NodeValue.makeNode(
                NodeFactory.createLiteralDT(prefix + BMP, XSDDatatype.XSDstring))));
    }

    @Test public void plainAndXsdStringValues() {
        NodeValue plain = NodeValue.makeString(BMP);
        NodeValue typed = NodeValue.makeNode(NodeFactory.createLiteralDT(SUPPLEMENTARY, XSDDatatype.XSDstring));
        assertEquals(Expr.CMP_LESS, NodeValue.compare(plain, typed));
        assertEquals(Expr.CMP_GREATER, NodeValue.compare(typed, plain));
        assertEquals(Expr.CMP_EQUAL, NodeValue.compare(plain,
                NodeValue.makeNode(NodeFactory.createLiteralDT(BMP, XSDDatatype.XSDstring))));
    }

    @Test public void sameLanguageStrings() {
        NodeValue bmp = NodeValue.makeLangString(BMP, "en");
        NodeValue supplementary = NodeValue.makeLangString(SUPPLEMENTARY, "en");
        assertEquals(Expr.CMP_LESS, NodeValue.compare(bmp, supplementary));
        assertEquals(Expr.CMP_GREATER, NodeValue.compare(supplementary, bmp));
        assertEquals(Expr.CMP_EQUAL, NodeValue.compare(bmp, NodeValue.makeLangString(BMP, "en")));
    }

    @Test public void differentLanguagesAreNotValueComparable() {
        assertThrows(ExprNotComparableException.class, () -> NodeValue.compare(
                NodeValue.makeLangString(BMP, "en"), NodeValue.makeLangString(SUPPLEMENTARY, "fr")));
    }

    private static void compare(String left, String right, int expected) {
        NodeValue nvLeft = NodeValue.makeString(left);
        NodeValue nvRight = NodeValue.makeString(right);
        assertEquals(expected, XSDFuncOp.compareString(nvLeft, nvRight));
        assertEquals(-expected, XSDFuncOp.compareString(nvRight, nvLeft));
        assertEquals(expected, NodeValue.compare(nvLeft, nvRight));
    }
}
