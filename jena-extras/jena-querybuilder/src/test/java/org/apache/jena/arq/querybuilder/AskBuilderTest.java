/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.arq.querybuilder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.vocabulary.RDF;

public class AskBuilderTest extends AbstractRegexpBasedTest {

    private AskBuilder builder;

    @BeforeEach
    public void setup() {
        builder = new AskBuilder();
    }

    @Test
    public void testAll() {
        builder.addPrefix("foaf", "http://xmlns.com/foaf/0.1/").addWhere("?s", RDF.type, "foaf:Person")
                .addOptional("?s", "foaf:name", "?name").addOrderBy("?s");

        String query = builder.buildString();
        /*
         * PREFIX foaf: <http://xmlns.com/foaf/0.1/>
         *
         * ASK WHERE { ?s
         * <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> foaf:Person .
         * OPTIONAL { ?s foaf:name ?name .} } ORDER BY ?s
         */
        assertContainsRegex(PREFIX + "foaf:" + SPACE + uri("http://xmlns.com/foaf/0.1/"), query);
        assertContainsRegex(ASK, query);
        assertContainsRegex(WHERE + OPEN_CURLY + var("s") + SPACE + regexRDFtype + SPACE + "foaf:Person" + SPACE
                + OPTIONAL + OPEN_CURLY + var("s") + SPACE + "foaf:name" + SPACE + var("name") + OPT_SPACE + CLOSE_CURLY
                + CLOSE_CURLY, query);
        assertContainsRegex(ORDER_BY + var("s"), query);

        builder.setVar("name", "Smith");

        query = builder.buildString();
        assertContainsRegex(PREFIX + "foaf:" + SPACE + uri("http://xmlns.com/foaf/0.1/"), query);
        assertContainsRegex(ASK + WHERE + OPEN_CURLY + var("s") + SPACE + regexRDFtype + SPACE + "foaf:Person" + SPACE
                + OPTIONAL + OPEN_CURLY + var("s") + SPACE + "foaf:name" + SPACE + quote("Smith") + presentStringType()
                + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY, query);
        assertContainsRegex(ORDER_BY + var("s"), query);
    }

    @Test
    public void testPredicateVar() {
        builder.addPrefix("", "http://example/").addWhere(":S", "?p", ":O");
        String query = builder.buildString();

        assertContainsRegex(WHERE + OPEN_CURLY + ":S" + SPACE + var("p") + SPACE + ":O" + OPT_SPACE + CLOSE_CURLY,
                query);
    }

    @Test
    public void testSubjectVar() {
        builder.addPrefix("", "http://example/").addWhere("?s", ":P", ":O");
        String query = builder.buildString();

        assertContainsRegex(WHERE + OPEN_CURLY + var("s") + SPACE + ":P" + SPACE + ":O" + OPT_SPACE + CLOSE_CURLY,
                query);
    }

    @Test
    public void testObjectVar() {
        builder.addPrefix("", "http://example/").addWhere(":S", ":P", "?o");
        String query = builder.buildString();

        assertContainsRegex(WHERE + OPEN_CURLY + ":S" + SPACE + ":P" + SPACE + var("o") + OPT_SPACE + CLOSE_CURLY,
                query);
    }

    @Test
    public void testClone() {
        builder.addWhere("?two", "<foo>", "<bar>");
        AskBuilder builder2 = builder.clone();
        builder2.addOrderBy("?two");

        String q1 = builder.buildString();
        String q2 = builder2.buildString();

        assertTrue(q2.contains("ORDER BY"));
        assertFalse(q1.contains("ORDER BY"));
    }
}
