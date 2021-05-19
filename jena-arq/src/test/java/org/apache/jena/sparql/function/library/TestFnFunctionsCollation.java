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

package org.apache.jena.sparql.function.library;

import static org.apache.jena.sparql.expr.LibTestExpr.test;
import static org.junit.Assert.assertArrayEquals;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.Test;

/**
 * Tests for {@link collation}.
 */
public class TestFnFunctionsCollation {
    @Test
    public void testFunctionCollationExec() {
        collation function = new collation();
        NodeValue collation = NodeValue.makeString("fi");

        final String[] unordered = {"tšekin kieli" , "tulun kieli", "töyhtöhyyppä",
                                    "tsahurin kieli", "tsahurin kieli", "tulun kieli"};
        String[] ordered = {"tsahurin kieli", "tsahurin kieli", "tšekin kieli",
                            "tulun kieli", "tulun kieli", "töyhtöhyyppä"};
        // tests collation sort order with Danish words, but New Zealand English
        // collation rules
        List<NodeValue> nodeValues = new LinkedList<>();
        for ( String string : unordered ) {
            nodeValues.add(function.exec(collation, NodeValue.makeString(string)));
        }
        nodeValues.sort((NodeValue o1, NodeValue o2) -> NodeValue.compare(o1, o2));
        List<String> result = new LinkedList<>();
        for ( NodeValue nv : nodeValues ) {
            String s = nv.getNode().getLiteralLexicalForm();
            result.add(s);
        }
        assertArrayEquals(ordered, result.toArray(new String[0]));
    }

    @Test
    public void collationKey_1() {
        test("fn:collation-key('foo', 'en') = 'Zm9vQGVu'^^xsd:base64Binary");
    }

    @Test(expected = ExprEvalException.class)
    public void collationKey_2() {
        test("fn:collation-key('foo', 22) = 'Zm9vQGVu'^^xsd:base64Binary");
    }

    @Test(expected = ExprEvalException.class)
    public void collationKey_3() {
        test("fn:collation-key(<x:bar>, 'en') = 'Zm9vQGVu'^^xsd:base64Binary");
    }

}
