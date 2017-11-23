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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.DCTypes;
import org.apache.jena.vocabulary.DC_11;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests based on https://www.w3.org/TR/sparql11-update examples
 * 
 * Tests 13-15 are not implemented as the builder does not interface with them.
 *
 */
public class UpdateBuilderExampleTests {

	private static final String NS_prefix = "http://example.org/ns#";

	private List<Triple> triples;
	private Graph g;
	private Model m;

	public UpdateBuilderExampleTests() {
		triples = new ArrayList<Triple>();
		g = new CollectionGraph(triples);
		m = ModelFactory.createModelForGraph(g);
	}

	@Before
	public void setup() {
		triples.clear();
		m.clearNsPrefixMap();
	}

	/**
	 * Example 1: Adding some triples to a graph
	 * 
	 * @see https://www.w3.org/TR/sparql11-update/#example_1
	 */
	@Test
	public void example1() {
		Resource r = ResourceFactory.createResource("http://example/book1");
		Property price = ResourceFactory.createProperty(NS_prefix + "price");
		Literal priceV = ResourceFactory.createPlainLiteral("42");
		m.add(r, price, priceV);
		m.setNsPrefix("dc", DC_11.NS);
		m.setNsPrefix("ns", NS_prefix);

		UpdateBuilder builder = new UpdateBuilder().addPrefix("dc", DC_11.NS).addInsert(r, DC_11.title, "A new book")
				.addInsert(r, DC_11.creator, "A.N.Other");

		UpdateAction.execute(builder.buildRequest(), m);

		assertTrue(m.contains(r, price, priceV));
		assertTrue(m.contains(r, DC_11.title, "A new book"));
		assertTrue(m.contains(r, DC_11.creator, "A.N.Other"));
		assertEquals(3, triples.size());
		assertEquals(2, m.getNsPrefixMap().size());
		assertEquals(NS_prefix, m.getNsPrefixMap().get("ns"));
		assertEquals(DC_11.NS, m.getNsPrefixMap().get("dc"));

	}

	/**
	 * Example 2: Adding some triples to a graph
	 * 
	 * @see https://www.w3.org/TR/sparql11-update/#example_2
	 */
	@Test
	public void example2() {
		Resource r = ResourceFactory.createResource("http://example/book1");
		Property price = ResourceFactory.createProperty(NS_prefix + "price");
		Literal priceV = ResourceFactory.createTypedLiteral(42);

		m.setNsPrefix("dc", DC_11.NS);
		m.add(r, DC_11.title, "Fundamentals of Compiler Design");

		UpdateBuilder builder = new UpdateBuilder().addPrefix("ns", NS_prefix).addInsert(r, "ns:price",
				priceV.asNode());

		UpdateAction.execute(builder.buildRequest(), m);

		assertTrue(m.contains(r, price, priceV));
		assertTrue(m.contains(r, DC_11.title, "Fundamentals of Compiler Design"));
		assertEquals(2, triples.size());
		// assertEquals( 2, m.getNsPrefixMap().size());
		// assertEquals( NS_prefix, m.getNsPrefixMap().get("ns"));
		assertEquals(DC_11.NS, m.getNsPrefixMap().get("dc"));
	}

	/**
	 * Example 3: Removing triples from a graph
	 * 
	 * @see https://www.w3.org/TR/sparql11-update/#example_3
	 */
	@Test
	public void example3() {
		Resource r = ResourceFactory.createResource("http://example/book2");
		Property price = ResourceFactory.createProperty(NS_prefix + "price");
		Literal priceV = ResourceFactory.createTypedLiteral(42);

		m.setNsPrefix("dc", DC_11.NS);
		m.setNsPrefix("ns", NS_prefix);
		m.add(r, price, priceV);
		m.add(r, DC_11.title, "David Copperfield");
		m.add(r, DC_11.creator, "Edmund Wells");

		UpdateBuilder builder = new UpdateBuilder().addPrefix("dc", DC_11.NS).addDelete(r, DC_11.title, "David Copperfield")
				.addDelete(r, DC_11.creator, "Edmund Wells");

		UpdateAction.execute(builder.buildRequest(), m);

		assertTrue(m.contains(r, price, priceV));
		assertEquals(1, triples.size());
		assertEquals(2, m.getNsPrefixMap().size());
		assertEquals(NS_prefix, m.getNsPrefixMap().get("ns"));
		assertEquals(DC_11.NS, m.getNsPrefixMap().get("dc"));
	}

	/**
	 * Example 4:
	 * 
	 * @see https://www.w3.org/TR/sparql11-update/#example_4
	 */
	@Test
	public void example4() {
		Resource r = ResourceFactory.createResource("http://example/book1");
		Node graphName = NodeFactory.createURI("http://example/bookStore");
		Dataset ds = DatasetFactory.create();
		ds.addNamedModel(graphName.getURI(), m);
		m.setNsPrefix("dc", DC_11.NS);
		m.add(r, DC_11.title, "Fundamentals of Compiler Desing");

		SelectBuilder sb = new SelectBuilder().addWhere(r, DC_11.title, "Fundamentals of Compiler Desing");
		sb = new SelectBuilder().addPrefix("dc", DC_11.NS).addGraph(graphName, sb);

		UpdateBuilder builder = new UpdateBuilder().addPrefix("dc", DC_11.NS).addDelete(sb);
		UpdateRequest req = builder.buildRequest();

		sb = new SelectBuilder().addWhere(r, DC_11.title, "Fundamentals of Compiler Design");
		sb = new SelectBuilder().addPrefix("dc", DC_11.NS).addGraph(graphName, sb);

		builder = new UpdateBuilder().addPrefix("dc", DC_11.NS).addInsert(sb);

		builder.appendTo(req);

		UpdateAction.execute(req, ds);

		Model m2 = ds.getNamedModel(graphName.getURI());

		assertTrue(m2.contains(r, DC_11.title, "Fundamentals of Compiler Design"));

		assertEquals(1, m2.listStatements().toSet().size());
		// assertEquals( 1, m2.getNsPrefixMap().size());
		// assertEquals( DC.NS, m2.getNsPrefixMap().get("dc"));
	}

	/**
	 * Example 5:
	 * 
	 * @see https://www.w3.org/TR/sparql11-update/#example_5
	 */
	@Test
	public void example5() {
		Resource p25 = ResourceFactory.createResource("http://example/president25");
		Resource p27 = ResourceFactory.createResource("http://example/president27");
		Resource p42 = ResourceFactory.createResource("http://example/president42");
		m.setNsPrefix("foaf", FOAF.NS);
		m.add(p25, FOAF.givenName, "Bill");
		m.add(p25, FOAF.familyName, "McKinley");
		m.add(p27, FOAF.givenName, "Bill");
		m.add(p27, FOAF.familyName, "Taft");
		m.add(p42, FOAF.givenName, "Bill");
		m.add(p42, FOAF.familyName, "Clinton");

		Node graphName = NodeFactory.createURI("http://example/addresses");
		Dataset ds = DatasetFactory.create();
		ds.addNamedModel(graphName.getURI(), m);

		UpdateBuilder builder = new UpdateBuilder().addPrefix("foaf", FOAF.NS).with(graphName)
				.addDelete("?person", FOAF.givenName, "Bill").addInsert("?person", FOAF.givenName, "William")
				.addWhere("?person", FOAF.givenName, "Bill");

		UpdateRequest req = builder.buildRequest();

		UpdateAction.execute(req, ds);

		Model m2 = ds.getNamedModel(graphName.getURI());
		List<RDFNode> nodes = m2.listObjectsOfProperty(FOAF.givenName).toList();
		assertEquals(1, nodes.size());
		assertEquals("William", nodes.get(0).asLiteral().toString());
		List<Resource> subjects = m2.listSubjectsWithProperty(FOAF.givenName).toList();
		assertEquals(3, subjects.size());
	}

	/**
	 * Example 6:
	 * 
	 * @see https://www.w3.org/TR/sparql11-update/#example_6
	 */
	@Test
	public void example6() {

		Resource book1 = ResourceFactory.createResource("http://example/book1");
		Resource book2 = ResourceFactory.createResource("http://example/book2");
		Resource book3 = ResourceFactory.createResource("http://example/book3");

		Literal d1977 = ResourceFactory.createTypedLiteral("1977-01-01T00:00:00-02:00", XSDDatatype.XSDdateTime);
		Literal d1970 = ResourceFactory.createTypedLiteral("1970-01-01T00:00:00-02:00", XSDDatatype.XSDdateTime);
		Literal d1948 = ResourceFactory.createTypedLiteral("1948-01-01T00:00:00-02:00", XSDDatatype.XSDdateTime);

		Property price = ResourceFactory.createProperty(NS_prefix + "price");
		Literal priceV = ResourceFactory.createPlainLiteral("42");

		m.setNsPrefix("dc", DC_11.NS);
		m.setNsPrefix("ns", NS_prefix);

		m.add(book1, DC_11.title, "Principles of Compiler Design");
		m.add(book1, DC_11.date, d1977);

		m.add(book2, price, priceV);
		m.add(book2, DC_11.title, "David Copperfield");
		m.add(book2, DC_11.creator, "Edmund Wells");
		m.add(book2, DC_11.date, d1948);

		m.add(book3, DC_11.title, "SPARQL 1.1 Tutorial");

		UpdateBuilder builder = new UpdateBuilder().addPrefix("dc", DC_11.NS)
				.addPrefix("xsd", "http://www.w3.org/2001/XMLSchema#").addDelete("?book", "?p", "?v")
				.addWhere("?book", "dc:date", "?date");

		ExprFactory exprFact = builder.getExprFactory();
		builder.addFilter(exprFact.gt(exprFact.asExpr("?date"), d1970)).addWhere("?book", "?p", "?v");

		UpdateRequest req = builder.buildRequest();

		UpdateAction.execute(req, m);

		assertTrue(m.contains(book2, price, priceV));
		assertTrue(m.contains(book2, DC_11.title, "David Copperfield"));
		assertTrue(m.contains(book2, DC_11.creator, "Edmund Wells"));
		assertTrue(m.contains(book2, DC_11.date, d1948));

		assertTrue(m.contains(book3, DC_11.title, "SPARQL 1.1 Tutorial"));

		assertEquals(5, triples.size());
	}

	/**
	 * Example 7:
	 * 
	 * @see https://www.w3.org/TR/sparql11-update/#example_7
	 */
	@Test
	public void example7() {
		Resource will = ResourceFactory.createResource("http://example/william");
		Resource willMail = ResourceFactory.createResource("mailto:bill@example");
		Resource fred = ResourceFactory.createResource("http://example/fred");
		Resource fredMail = ResourceFactory.createResource("mailto:fred@example");

		Node graphName = NodeFactory.createURI("http://example/addresses");

		m.add(will, RDF.type, FOAF.Person);
		m.add(will, FOAF.givenName, "William");
		m.add(will, FOAF.mbox, willMail);

		m.add(fred, RDF.type, FOAF.Person);
		m.add(fred, FOAF.givenName, "Fred");
		m.add(fred, FOAF.mbox, fredMail);

		Dataset ds = DatasetFactory.create();
		ds.addNamedModel(graphName.getURI(), m);

		UpdateBuilder builder = new UpdateBuilder().addPrefix("foaf", FOAF.NS).with(graphName)
				.addDelete("?person", "?property", "?value").addWhere("?person", "?property", "?value")
				.addWhere("?person", FOAF.givenName, "'Fred'");

		UpdateAction.execute(builder.build(), ds);

		Model m2 = ds.getNamedModel(graphName.getURI());

		assertTrue(m2.contains(will, RDF.type, FOAF.Person));
		assertTrue(m2.contains(will, FOAF.givenName, "William"));
		assertTrue(m2.contains(will, FOAF.mbox, willMail));
		assertEquals(3, m2.listStatements().toList().size());
	}

	/**
	 * Example 8:
	 * 
	 * @see https://www.w3.org/TR/sparql11-update/#example_8
	 */
	@Test
	public void example8() {

		Resource book1 = ResourceFactory.createResource("http://example/book1");
		Resource book2 = ResourceFactory.createResource("http://example/book2");
		Resource book3 = ResourceFactory.createResource("http://example/book3");
		Resource book4 = ResourceFactory.createResource("http://example/book4");

		Literal d1977 = ResourceFactory.createTypedLiteral("1977-01-01T00:00:00-02:00", XSDDatatype.XSDdateTime);
		Literal d1970 = ResourceFactory.createTypedLiteral("1970-01-01T00:00:00-02:00", XSDDatatype.XSDdateTime);
		Literal d1948 = ResourceFactory.createTypedLiteral("1948-01-01T00:00:00-02:00", XSDDatatype.XSDdateTime);

		Property price = ResourceFactory.createProperty(NS_prefix + "price");
		Literal priceV = ResourceFactory.createPlainLiteral("42");

		Node graphName1 = NodeFactory.createURI("http://example/bookStore");
		Node graphName2 = NodeFactory.createURI("http://example/bookStore2");

		Model m1 = ModelFactory.createDefaultModel();
		m1.add(book1, DC_11.title, "Fundamentals of Compiler Design");
		m1.add(book1, DC_11.date, d1977);

		m1.add(book2, price, priceV);
		m1.add(book2, DC_11.title, "David Copperfield");
		m1.add(book2, DC_11.creator, "Edmund Wells");
		m1.add(book2, DC_11.date, d1948);

		m1.add(book3, DC_11.title, "SPARQL 1.1 Tutorial");

		Model m2 = ModelFactory.createDefaultModel();
		m2.add(book4, DC_11.title, "SPARQL 1.1 Tutorial");

		Dataset ds = DatasetFactory.create();
		ds.addNamedModel(graphName1.getURI(), m1);
		ds.addNamedModel(graphName2.getURI(), m2);

		ExprFactory factory = new ExprFactory();

		SelectBuilder ins = new SelectBuilder().addGraph(graphName2, new SelectBuilder().addWhere("?book", "?p", "?v"));

		SelectBuilder whr = new SelectBuilder().addGraph(graphName1,
				new SelectBuilder().addWhere("?book", DC_11.date, "?date").addFilter(factory.gt("?date", d1970))
						.addWhere("?book", "?p", "?v"));
		UpdateBuilder builder = new UpdateBuilder().addPrefix("dc", DC_11.NS).addPrefix("xsd", XSD.NS).addInsert(ins)
				.addWhere(whr);

		UpdateAction.execute(builder.build(), ds);

		m1 = ds.getNamedModel(graphName1.getURI());
		assertEquals(7, m1.listStatements().toList().size());

		assertEquals(2, m1.listStatements(book1, null, (RDFNode) null).toList().size());
		assertTrue(m1.contains(book1, DC_11.title, "Fundamentals of Compiler Design"));
		assertTrue(m1.contains(book1, DC_11.date, d1977));

		assertEquals(4, m1.listStatements(book2, null, (RDFNode) null).toList().size());
		assertTrue(m1.contains(book2, price, priceV));
		assertTrue(m1.contains(book2, DC_11.title, "David Copperfield"));
		assertTrue(m1.contains(book2, DC_11.creator, "Edmund Wells"));
		assertTrue(m1.contains(book2, DC_11.date, d1948));

		assertEquals(1, m1.listStatements(book3, null, (RDFNode) null).toList().size());
		assertTrue(m1.contains(book3, DC_11.title, "SPARQL 1.1 Tutorial"));

		m2 = ds.getNamedModel(graphName2.getURI());
		assertEquals(3, m2.listStatements().toList().size());

		assertEquals(2, m2.listStatements(book1, null, (RDFNode) null).toList().size());
		assertTrue(m2.contains(book1, DC_11.title, "Fundamentals of Compiler Design"));
		assertTrue(m2.contains(book1, DC_11.date, d1977));

		assertEquals(1, m2.listStatements(book4, null, (RDFNode) null).toList().size());
		assertTrue(m2.contains(book4, DC_11.title, "SPARQL 1.1 Tutorial"));

	}

	/**
	 * Example 9:
	 * 
	 * @see https://www.w3.org/TR/sparql11-update/#example_9
	 */
	@Test
	public void example9() {
		Node graphName1 = NodeFactory.createURI("http://example/addresses");
		Node graphName2 = NodeFactory.createURI("http://example/people");

		Resource alice = ResourceFactory.createResource();
		Resource aliceMail = ResourceFactory.createResource("mailto:alice@example.com");

		Resource bob = ResourceFactory.createResource();

		Model m1 = ModelFactory.createDefaultModel();
		m1.add(alice, RDF.type, FOAF.Person);
		m1.add(alice, FOAF.name, "Alice");
		m1.add(alice, FOAF.mbox, aliceMail);

		m1.add(bob, RDF.type, FOAF.Person);
		m1.add(bob, FOAF.name, "Bob");

		Model m2 = ModelFactory.createDefaultModel();

		Dataset ds = DatasetFactory.create();
		ds.addNamedModel(graphName1.getURI(), m1);
		ds.addNamedModel(graphName2.getURI(), m2);

		SelectBuilder ins = new SelectBuilder().addGraph(graphName2,
				new SelectBuilder().addWhere("?person", FOAF.name, "?name").addWhere("?person", FOAF.mbox, "?email"));

		SelectBuilder whr = new SelectBuilder().addGraph(graphName1, new SelectBuilder()
				.addWhere("?person", FOAF.name, "?name").addOptional("?person", FOAF.mbox, "?email"));
		UpdateBuilder builder = new UpdateBuilder().addInsert(ins).addWhere(whr);

		UpdateAction.execute(builder.build(), ds);

		m1 = ds.getNamedModel(graphName1.getURI());
		assertEquals(5, m1.listStatements().toList().size());

		assertEquals(3, m1.listStatements(alice, null, (RDFNode) null).toList().size());
		assertTrue(m1.contains(alice, RDF.type, FOAF.Person));
		assertTrue(m1.contains(alice, FOAF.name, "Alice"));
		assertTrue(m1.contains(alice, FOAF.mbox, aliceMail));

		assertEquals(2, m1.listStatements(bob, null, (RDFNode) null).toList().size());
		assertTrue(m1.contains(bob, RDF.type, FOAF.Person));
		assertTrue(m1.contains(bob, FOAF.name, "Bob"));

		m2 = ds.getNamedModel(graphName2.getURI());
		assertEquals(3, m2.listStatements().toList().size());

		assertEquals(2, m2.listStatements(alice, null, (RDFNode) null).toList().size());
		assertTrue(m2.contains(alice, FOAF.name, "Alice"));
		assertTrue(m2.contains(alice, FOAF.mbox, aliceMail));

		assertEquals(1, m2.listStatements(bob, null, (RDFNode) null).toList().size());
		assertTrue(m2.contains(bob, FOAF.name, "Bob"));

	}

	/**
	 * Example 10:
	 * 
	 * @see https://www.w3.org/TR/sparql11-update/#example_10
	 */
	@Test
	public void example10() {

		Resource book1 = ResourceFactory.createResource("http://example/book1");
		Resource book3 = ResourceFactory.createResource("http://example/book3");
		Resource book4 = ResourceFactory.createResource("http://example/book4");

		Literal d1996 = ResourceFactory.createTypedLiteral("1996-01-01T00:00:00-02:00", XSDDatatype.XSDdateTime);
		Literal d2000 = ResourceFactory.createTypedLiteral("2000-01-01T00:00:00-02:00", XSDDatatype.XSDdateTime);

		Node graphName1 = NodeFactory.createURI("http://example/bookStore");
		Node graphName2 = NodeFactory.createURI("http://example/bookStore2");

		Model m1 = ModelFactory.createDefaultModel();
		m1.add(book1, DC_11.title, "Fundamentals of Compiler Design");
		m1.add(book1, DC_11.date, d1996);
		m1.add(book1, RDF.type, DCTypes.PhysicalObject);

		m1.add(book3, DC_11.title, "SPARQL 1.1 Tutorial");

		Model m2 = ModelFactory.createDefaultModel();
		m2.add(book4, DC_11.title, "SPARQL 1.1 Tutorial");

		Dataset ds = DatasetFactory.create();
		ds.addNamedModel(graphName1.getURI(), m1);
		ds.addNamedModel(graphName2.getURI(), m2);

		ExprFactory factory = new ExprFactory();

		SelectBuilder ins = new SelectBuilder().addGraph(graphName2, new SelectBuilder().addWhere("?book", "?p", "?v"));

		SelectBuilder whr = new SelectBuilder().addGraph(graphName1,
				new SelectBuilder().addWhere("?book", DC_11.date, "?date").addFilter(factory.lt("?date", d2000))
						.addWhere("?book", "?p", "?v"));

		UpdateBuilder builder = new UpdateBuilder().addPrefix("dc", DC_11.NS).addPrefix("xsd", XSD.NS).addInsert(ins)
				.addWhere(whr);

		UpdateRequest req = builder.buildRequest();

		builder = new UpdateBuilder().with(graphName1).addDelete("?book", "?p", "?v")
				.addWhere("?book", DC_11.date, "?date").addWhere("?book", RDF.type, DCTypes.PhysicalObject)
				.addFilter(factory.lt("?date", d2000)).addWhere("?book", "?p", "?v");

		builder.appendTo(req);

		UpdateAction.execute(req, ds);

		m1 = ds.getNamedModel(graphName1.getURI());
		assertEquals(1, m1.listStatements().toList().size());

		assertTrue(m1.contains(book3, DC_11.title, "SPARQL 1.1 Tutorial"));

		m2 = ds.getNamedModel(graphName2.getURI());
		assertEquals(4, m2.listStatements().toList().size());

		assertEquals(3, m2.listStatements(book1, null, (RDFNode) null).toList().size());
		assertTrue(m2.contains(book1, DC_11.title, "Fundamentals of Compiler Design"));
		assertTrue(m2.contains(book1, DC_11.date, d1996));
		assertTrue(m2.contains(book1, RDF.type, DCTypes.PhysicalObject));

		assertEquals(1, m2.listStatements(book4, null, (RDFNode) null).toList().size());
		assertTrue(m2.contains(book4, DC_11.title, "SPARQL 1.1 Tutorial"));

	}

	/**
	 * Example 11:
	 * 
	 * @see https://www.w3.org/TR/sparql11-update/#example_11
	 */
	@Test
	public void example11() {

		Resource will = ResourceFactory.createResource("http://example/william");
		Resource willMail = ResourceFactory.createResource("mailto:bill@example");
		Resource fred = ResourceFactory.createResource("http://example/fred");
		Resource fredMail = ResourceFactory.createResource("mailto:fred@example");

		m.add(will, RDF.type, FOAF.Person);
		m.add(will, FOAF.givenName, "William");
		m.add(will, FOAF.mbox, willMail);

		m.add(fred, RDF.type, FOAF.Person);
		m.add(fred, FOAF.givenName, "Fred");
		m.add(fred, FOAF.mbox, fredMail);

		UpdateBuilder builder = new UpdateBuilder().addWhere("?person", FOAF.givenName, "Fred").addWhere("?person",
				"?property", "?value");

		UpdateAction.execute(builder.buildDeleteWhere(), m);

		assertEquals(3, m.listStatements().toList().size());
		assertTrue(m.contains(will, RDF.type, FOAF.Person));
		assertTrue(m.contains(will, FOAF.givenName, "William"));
		assertTrue(m.contains(will, FOAF.mbox, willMail));
	}

	/**
	 * Example 12:
	 * 
	 * @see https://www.w3.org/TR/sparql11-update/#example_12
	 */
	@Test
	public void example12() {

		Resource will = ResourceFactory.createResource("http://example/william");
		Resource willMail = ResourceFactory.createResource("mailto:bill@example");
		Resource fred = ResourceFactory.createResource("http://example/fred");
		Resource fredMail = ResourceFactory.createResource("mailto:fred@example");

		Node graphName1 = NodeFactory.createURI("http://example/names");
		Node graphName2 = NodeFactory.createURI("http://example/addresses");

		Model m1 = ModelFactory.createDefaultModel();
		m1.add(will, RDF.type, FOAF.Person);
		m1.add(will, FOAF.givenName, "William");
		m1.add(fred, RDF.type, FOAF.Person);
		m1.add(fred, FOAF.givenName, "Fred");

		Model m2 = ModelFactory.createDefaultModel();
		m2.add(will, FOAF.mbox, willMail);
		m2.add(fred, FOAF.mbox, fredMail);

		Dataset ds = DatasetFactory.create();
		ds.addNamedModel(graphName1.getURI(), m1);
		ds.addNamedModel(graphName2.getURI(), m2);

		UpdateBuilder builder = new UpdateBuilder()
				.addGraph(graphName1,
						new SelectBuilder().addWhere("?person", FOAF.givenName, "Fred").addWhere("?person", "?property",
								"?value1"))
				.addGraph(graphName2, new SelectBuilder().addWhere("?person", "?property2", "?value2"));

		UpdateAction.execute(builder.buildDeleteWhere(), ds);

		m1 = ds.getNamedModel(graphName1.getURI());
		assertEquals(2, m1.listStatements().toList().size());
		assertTrue(m1.contains(will, RDF.type, FOAF.Person));
		assertTrue(m1.contains(will, FOAF.givenName, "William"));

		m2 = ds.getNamedModel(graphName2.getURI());
		assertEquals(1, m2.listStatements().toList().size());
		assertTrue(m2.contains(will, FOAF.mbox, willMail));
	}
	
}
