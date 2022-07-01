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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.arq.AbstractRegexpBasedTest;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.junit.Before;
import org.junit.Test;

public class SelectBuilderTest extends AbstractRegexpBasedTest {

    private SelectBuilder builder;

    @Before
    public void setup() {
        builder = new SelectBuilder();
    }

    @Test
    public void testSelectAsterisk() {
        builder.addVar("*").addWhere("?s", "?p", "?o");

        assertContainsRegex(SELECT + "\\*" + SPACE + WHERE + OPEN_CURLY + var("s") + SPACE + var("p") + SPACE + var("o")
                + OPT_SPACE + CLOSE_CURLY, builder.buildString());

        builder.setVar(Var.alloc("p"), RDF.type);

        assertContainsRegex(SELECT + "\\*" + SPACE + WHERE + OPEN_CURLY + var("s") + SPACE + regexRDFtype + SPACE
                + var("o") + OPT_SPACE + CLOSE_CURLY, builder.buildString());
    }

    @Test
    public void testAll() {
        builder.addVar("s").addPrefix("foaf", "http://xmlns.com/foaf/0.1/").addWhere("?s", RDF.type, "foaf:Person")
                .addOptional("?s", "foaf:name", "?name").addOrderBy("?s");

        String query = builder.buildString();
        /*
         * PREFIX foaf: <http://xmlns.com/foaf/0.1/>
         * 
         * SELECT ?s WHERE { ?s
         * <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> foaf:Person .
         * OPTIONAL { ?s foaf:name ?name .} } ORDER BY ?s
         */
        assertContainsRegex(PREFIX + "foaf:" + SPACE + uri("http://xmlns.com/foaf/0.1/"), query);
        assertContainsRegex(SELECT + var("s"), query);
        assertContainsRegex(WHERE + OPEN_CURLY + var("s") + SPACE + regexRDFtype + SPACE + "foaf:Person" + SPACE
                + OPTIONAL + OPEN_CURLY + var("s") + SPACE + "foaf:name" + SPACE + var("name") + OPT_SPACE + CLOSE_CURLY
                + CLOSE_CURLY, query);
        assertContainsRegex(ORDER_BY + var("s"), query);

        builder.setVar("name", "Smith");

        query = builder.buildString();
        assertContainsRegex(PREFIX + "foaf:" + SPACE + uri("http://xmlns.com/foaf/0.1/"), query);
        assertContainsRegex(SELECT + var("s"), query);
        assertContainsRegex(WHERE + OPEN_CURLY + var("s") + SPACE + regexRDFtype + SPACE + "foaf:Person" + SPACE
                + OPTIONAL + OPEN_CURLY + var("s") + SPACE + "foaf:name" + SPACE + quote("Smith") + presentStringType()
                + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY, query);
        assertContainsRegex(ORDER_BY + var("s"), query);
    }

    @Test
    public void testPredicateVar() {
        builder.addVar("*").addPrefix("", "http://example/").addWhere(":S", "?p", ":O");
        String query = builder.buildString();

        assertContainsRegex(WHERE + OPEN_CURLY + ":S" + SPACE + var("p") + SPACE + ":O" + OPT_SPACE + CLOSE_CURLY,
                query);
    }

    @Test
    public void testSubjectVar() {
        builder.addVar("*").addPrefix("", "http://example/").addWhere("?s", ":P", ":O");
        String query = builder.buildString();

        assertContainsRegex(WHERE + OPEN_CURLY + var("s") + SPACE + ":P" + SPACE + ":O" + OPT_SPACE + CLOSE_CURLY,
                query);
    }

    @Test
    public void testObjectVar() {
        builder.addVar("*").addPrefix("", "http://example/").addWhere(":S", ":P", "?o");
        String query = builder.buildString();

        assertContainsRegex(WHERE + OPEN_CURLY + ":S" + SPACE + ":P" + SPACE + var("o") + OPT_SPACE + CLOSE_CURLY,
                query);
    }

    @Test
    public void testNoVars() {
        builder.addWhere("?s", "?p", "?o");
        String query = builder.buildString();

        assertContainsRegex(SELECT + "\\*" + SPACE, query);
    }

    @Test
    public void testList() {

        builder.addVar("*").addWhere(builder.list("<one>", "?two", "'three'"), "<foo>", "<bar>");
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
        builder.addVar("*").addWhere("?two", "<foo>", "<bar>");
        SelectBuilder builder2 = builder.clone();
        builder2.addOrderBy("?two");

        String q1 = builder.buildString();
        String q2 = builder2.buildString();

        assertTrue(q2.contains("ORDER BY"));
        assertFalse(q1.contains("ORDER BY"));
    }

    @Test
    public void testAggregatorsInSelect() throws ParseException {
        builder.addVar("?x").addVar("count(*)", "?c").addWhere("?x", "?p", "?o").addGroupBy("?x");

        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource("urn:one");
        m.add(r, m.getProperty("urn:p:one"), m.createTypedLiteral(1));
        m.add(r, m.getProperty("urn:p:two"), m.createTypedLiteral(3));
        m.add(r, m.getProperty("urn:p:three"), m.createTypedLiteral(5));
        r = m.createResource("urn:two");
        m.add(r, m.getProperty("urn:p:one"), m.createTypedLiteral(1));
        m.add(r, m.getProperty("urn:p:two"), m.createTypedLiteral(3));
        m.add(r, m.getProperty("urn:p:three"), m.createTypedLiteral(5));

        QueryExecution qexec = QueryExecutionFactory.create(builder.build(), m);

        ResultSet results = qexec.execSelect();
        assertTrue(results.hasNext());
        for (; results.hasNext();) {
            QuerySolution soln = results.nextSolution();
            assertTrue(soln.contains("c"));
            assertTrue(soln.contains("x"));
            assertEquals(3, soln.get("c").asLiteral().getInt());
        }

        builder.addVar("min(?o)", "?min").addVar("max(?o)", "?max");

        qexec = QueryExecutionFactory.create(builder.build(), m);

        results = qexec.execSelect();
        assertTrue(results.hasNext());
        for (; results.hasNext();) {
            QuerySolution soln = results.nextSolution();
            assertTrue(soln.contains("c"));
            assertTrue(soln.contains("x"));
            assertTrue(soln.contains("?min"));
            assertEquals(3, soln.get("c").asLiteral().getInt());
            assertEquals(1, soln.get("min").asLiteral().getInt());
            assertEquals(5, soln.get("max").asLiteral().getInt());
        }

    }

    @Test
    public void testAggregatorsInSubQuery() throws ParseException {

        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource("urn:one");
        m.add(r, m.getProperty("urn:p:one"), m.createTypedLiteral(1));
        m.add(r, m.getProperty("urn:p:two"), m.createTypedLiteral(3));
        m.add(r, m.getProperty("urn:p:three"), m.createTypedLiteral(5));
        r = m.createResource("urn:two");
        m.add(r, m.getProperty("urn:p:one"), m.createTypedLiteral(2));
        m.add(r, m.getProperty("urn:p:two"), m.createTypedLiteral(4));
        m.add(r, m.getProperty("urn:p:three"), m.createTypedLiteral(6));

        SelectBuilder sb = new SelectBuilder().addVar("?x").addVar("max(?o)", "?max").addWhere("?x", "?p", "?o")
                .addGroupBy("?x");

        builder.addPrefix("xsd", XSD.getURI()).addVar("?x").addVar("min(?o2)", "?min").addWhere("?x", "?p2", "?o2")
                .addSubQuery(sb).addFilter("?max = '6'^^xsd:int").addGroupBy("?x");

        QueryExecution qexec = QueryExecutionFactory.create(builder.build(), m);

        ResultSet results = qexec.execSelect();
        assertTrue(results.hasNext());
        for (; results.hasNext();) {
            QuerySolution soln = results.nextSolution();
            assertTrue(soln.contains("x"));
            assertTrue(soln.contains("min"));
            assertEquals("urn:two", soln.get("?x").asResource().getURI());
            assertEquals(2, soln.get("?min").asLiteral().getInt());
        }
    }

    @Test
    public void testVarReplacementInSubQuery() throws ParseException {

        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource("urn:one");
        m.add(r, m.getProperty("urn:p:one"), m.createTypedLiteral(1));
        m.add(r, m.getProperty("urn:p:two"), m.createTypedLiteral(3));
        m.add(r, m.getProperty("urn:p:three"), m.createTypedLiteral(5));
        r = m.createResource("urn:two");
        m.add(r, m.getProperty("urn:p:one"), m.createTypedLiteral(2));
        m.add(r, m.getProperty("urn:p:two"), m.createTypedLiteral(4));
        m.add(r, m.getProperty("urn:p:three"), m.createTypedLiteral(6));

        SelectBuilder sb = new SelectBuilder().addVar("?x").addVar("?p").addWhere("?x", "?p", "?o")
                .addFilter("?o < ?limit");

        builder.addPrefix("xsd", XSD.getURI()).addVar("?x").addVar("count(?p)", "?c").addWhere("?x", "?p", "?o2")
                .addSubQuery(sb).addGroupBy("?x");

        builder.setVar("?limit", 4);

        QueryExecution qexec = QueryExecutionFactory.create(builder.build(), m);

        ResultSet results = qexec.execSelect();
        assertTrue(results.hasNext());
        for (; results.hasNext();) {
            QuerySolution soln = results.nextSolution();
            assertTrue(soln.contains("x"));
            assertTrue(soln.contains("c"));
            if ("urn:one".equals(soln.get("?x").asResource().getURI())) {
                assertEquals(2, soln.get("?c").asLiteral().getInt());
            } else {
                assertEquals(1, soln.get("?c").asLiteral().getInt());
            }
        }
    }

    @Test
    public void setDistinctTest() throws Exception {
        Query query = builder.query;
        assertFalse(query.isDistinct());
        assertFalse(query.isReduced());

        query = builder.setDistinct(true).query;
        assertTrue(query.isDistinct());
        assertFalse(query.isReduced());

        query = builder.setReduced(false).query;
        assertTrue(query.isDistinct());
        assertFalse(query.isReduced());

        query = builder.setReduced(true).query;
        assertFalse(query.isDistinct());
        assertTrue(query.isReduced());

        query = builder.setDistinct(true).query;
        assertTrue(query.isDistinct());
        assertFalse(query.isReduced());

        query = builder.setDistinct(false).query;
        assertFalse(query.isDistinct());
        assertFalse(query.isReduced());
    }

    @Test
    public void setReducedTest() throws Exception {
        Query query = builder.query;
        assertFalse(query.isDistinct());
        assertFalse(query.isReduced());

        query = builder.setReduced(true).query;
        assertFalse(query.isDistinct());
        assertTrue(query.isReduced());

        query = builder.setDistinct(false).query;
        assertFalse(query.isDistinct());
        assertTrue(query.isReduced());

        query = builder.setDistinct(true).query;
        assertTrue(query.isDistinct());
        assertFalse(query.isReduced());

        query = builder.setReduced(true).query;
        assertFalse(query.isDistinct());
        assertTrue(query.isReduced());

        query = builder.setReduced(false).query;
        assertFalse(query.isDistinct());
        assertFalse(query.isReduced());
    }

}
