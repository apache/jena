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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.arq.AbstractRegexpBasedTest;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;

public class ConstructBuilderTest extends AbstractRegexpBasedTest {

    private ConstructBuilder builder;

    @Before
    public void setup() {
        builder = new ConstructBuilder();
    }

    @Test
    public void testAll() {
        builder.addPrefix("foaf", "http://xmlns.com/foaf/0.1/").addConstruct(Triple.ANY)
                .addWhere("?s", RDF.type, "foaf:Person").addOptional("?s", "foaf:name", "?name").addOrderBy("?s");

        String query = builder.buildString();

        assertContainsRegex(PREFIX + "foaf:" + SPACE + uri("http://xmlns.com/foaf/0.1/"), query);
        assertContainsRegex(CONSTRUCT + OPEN_CURLY + "ANY" + SPACE + "ANY" + SPACE + "ANY" + DOT + CLOSE_CURLY, query);
        assertContainsRegex(WHERE + OPEN_CURLY + var("s") + SPACE + regexRDFtype + SPACE + "foaf:Person" + SPACE
                + OPTIONAL + OPEN_CURLY + var("s") + SPACE + "foaf:name" + SPACE + var("name") + OPT_SPACE + CLOSE_CURLY
                + CLOSE_CURLY, query);
        assertContainsRegex(ORDER_BY + var("s"), query);

        builder.setVar("name", "Smith");

        query = builder.buildString();
        assertContainsRegex(PREFIX + "foaf:" + SPACE + uri("http://xmlns.com/foaf/0.1/"), query);
        assertContainsRegex(CONSTRUCT + OPEN_CURLY + "ANY" + SPACE + "ANY" + SPACE + "ANY" + DOT + CLOSE_CURLY, query);
        assertContainsRegex(WHERE + OPEN_CURLY + var("s") + SPACE + regexRDFtype + SPACE + "foaf:Person" + SPACE
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
    public void testNoVars() {
        builder.addConstruct("?s", "?p", "?o");
        String query = builder.buildString();
        assertContainsRegex(CONSTRUCT + OPEN_CURLY + var("s") + SPACE + var("p") + SPACE + var("o") + DOT + CLOSE_CURLY,
                query);
    }

    @Test
    public void testList() {
        builder.addWhere(builder.list("<one>", "?two", "'three'"), "<foo>", "<bar>");
        Query query = builder.build();

        Node one = NodeFactory.createURI("one");
        Node two = Var.alloc("two").asNode();
        Node three = NodeFactory.createLiteral("three");
        Node foo = NodeFactory.createURI("foo");
        Node bar = NodeFactory.createURI("bar");

        ElementPathBlock epb = new ElementPathBlock();
        Node firstObject = NodeFactory.createBlankNode();
        Node secondObject = NodeFactory.createBlankNode();
        Node thirdObject = NodeFactory.createBlankNode();

        epb.addTriplePath(new TriplePath(new Triple(firstObject, RDF.first.asNode(), one)));
        epb.addTriplePath(new TriplePath(new Triple(firstObject, RDF.rest.asNode(), secondObject)));
        epb.addTriplePath(new TriplePath(new Triple(secondObject, RDF.first.asNode(), two)));
        epb.addTriplePath(new TriplePath(new Triple(secondObject, RDF.rest.asNode(), thirdObject)));
        epb.addTriplePath(new TriplePath(new Triple(thirdObject, RDF.first.asNode(), three)));
        epb.addTriplePath(new TriplePath(new Triple(thirdObject, RDF.rest.asNode(), RDF.nil.asNode())));
        epb.addTriplePath(new TriplePath(new Triple(firstObject, foo, bar)));

        WhereValidator visitor = new WhereValidator(epb);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testClone() {
        builder.addWhere("?two", "<foo>", "<bar>");
        ConstructBuilder builder2 = builder.clone();
        builder2.addOrderBy("?two");

        String q1 = builder.buildString();
        String q2 = builder2.buildString();

        assertTrue(q2.contains("ORDER BY"));
        assertFalse(q1.contains("ORDER BY"));
    }
}
