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
package org.apache.jena.arq.querybuilder.handlers;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.handlers.WhereHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Random;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WhereHandlerTest extends AbstractHandlerTest {

	private Query query;
	private WhereHandler handler;

	@Before
	public void setup() {
		query = new Query();
		handler = new WhereHandler(query);
	}

	@Test
	public void testAddAll() {
		Query query2 = new Query();
		WhereHandler handler2 = new WhereHandler(query2);
		handler2.addWhere(new Triple(NodeFactory.createURI("one"), NodeFactory
				.createURI("two"), NodeFactory.createLiteral("three")));
		handler.addAll(handler2);

		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + quote("three") + OPT_SPACE
				+ CLOSE_CURLY, query.toString());
	}

	@Test
	public void addWhereTriple() {
		handler.addWhere(new Triple(NodeFactory.createURI("one"), NodeFactory
				.createURI("two"), NodeFactory.createURI("three")));
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + uri("three") + OPT_SPACE
				+ CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddWhereObjects() {
		handler.addWhere(new Triple(NodeFactory.createURI("one"),
				ResourceFactory.createResource("two").asNode(), ResourceFactory
						.createLangLiteral("three", "en-US").asNode()));
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + quote("three") + "@en-US" + OPT_SPACE
				+ CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddWhereAnonymous() {
		handler.addWhere(new Triple(Node.ANY, RDF.first.asNode(), Node.ANY));
		assertContainsRegex(WHERE + OPEN_CURLY + "ANY" + SPACE
				+ uri("http://www\\.w3\\.org/1999/02/22-rdf-syntax-ns#first")
				+ SPACE + "ANY" + OPT_SPACE + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddOptionalStrings() {
		handler.addOptional(new Triple(NodeFactory.createURI("one"),
				NodeFactory.createURI("two"), NodeFactory.createURI("three")));
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ uri("three") + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddOptionalAnonymous() {
		handler.addOptional(new Triple(Node.ANY, RDF.first.asNode(), Node.ANY));
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + "ANY" + SPACE
				+ uri("http://www\\.w3\\.org/1999/02/22-rdf-syntax-ns#first")
				+ SPACE + "ANY" + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY,
				query.toString());
	}

	
	@Test
	public void testAddOptionalWhereHandler() throws ParseException {
		
		WhereHandler pattern = new WhereHandler(new Query());
		Var s = Var.alloc("s" );
		Node q = NodeFactory.createURI( "urn:q" );
		Node v = NodeFactory.createURI( "urn:v" );
		Var x = Var.alloc("x");
		Node n123 = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(123));	
		
		pattern.addWhere( new Triple( s, q,  n123 ) );
		pattern.addWhere( new Triple( s, v, x));
		pattern.addFilter( "?x>56");
		
		handler.addOptional( pattern );
		
		Query expected = QueryFactory.create( "SELECT * WHERE { OPTIONAL { ?s <urn:q> '123'^^<http://www.w3.org/2001/XMLSchema#int> . ?s <urn:v> ?x . FILTER(?x>56) }}");
		
		Assert.assertEquals( expected.getQueryPattern(), query.getQueryPattern());

	}
	
	@Test
	public void testAddOptionalObjects() {
		handler.addOptional(new Triple(NodeFactory.createURI("one"),
				ResourceFactory.createResource("two").asNode(), ResourceFactory
						.createLangLiteral("three", "en-US").asNode()));
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ quote("three") + "@en-US" + OPT_SPACE + CLOSE_CURLY
				+ CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddWhereStrings() {
		handler.addWhere(new Triple(NodeFactory.createURI("one"), NodeFactory
				.createURI("two"), NodeFactory.createURI("three")));
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + uri("three") + OPT_SPACE
				+ CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddFilter() throws ParseException {
		handler.addFilter("?one < 10");

		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE
				+ OPEN_PAREN + var("one") + OPT_SPACE + LT + OPT_SPACE + "10"
				+ CLOSE_PAREN + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddFilterWithNamespace() throws ParseException {
		query.setPrefix("afn", "http://jena.apache.org/ARQ/function#");
		handler.addFilter("afn:namespace(?one) = 'foo'");

		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE
				+ OPEN_PAREN + "afn:namespace" + OPEN_PAREN + var("one")
				+ CLOSE_PAREN + OPT_SPACE + EQ + OPT_SPACE + QUOTE + "foo"
				+ QUOTE + CLOSE_PAREN + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddFilterVarOnly() throws ParseException {
		handler.addFilter("?one");

		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE
				+ OPEN_PAREN + var("one") + CLOSE_PAREN + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddSubQueryWithVars() {
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addVar("?x")
				.addWhere("<one>", "<two>", "three");
		handler.addSubQuery(sb);
		assertContainsRegex("SELECT" + SPACE + var("x") + SPACE + WHERE
				+ OPEN_CURLY + uri("one") + ".+" + uri("two") + ".+"
				+ quote("three") + ".+" + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddSubQueryWithoutVars() {
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", "three");
		handler.addSubQuery(sb);
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + ".+"
				+ uri("two") + ".+" + quote("three") + ".+" + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddUnion() {
		SelectBuilder sb = new SelectBuilder();
		sb.addWhere(new Triple(NodeFactory.createURI("one"), NodeFactory
				.createURI("two"), NodeFactory.createURI("three")));

		handler.addUnion(sb);
		assertContainsRegex(WHERE + OPEN_CURLY + UNION + OPEN_CURLY
				+ uri("one") + SPACE + uri("two") + SPACE + uri("three")
				+ OPT_SPACE + CLOSE_CURLY, query.toString());
	}
	
	@Test
	public void testAddUnionToExisting() {
		handler.addWhere( new Triple( NodeFactory.createURI( "s"), NodeFactory.createURI("p"),
				NodeFactory.createURI("o")));
		SelectBuilder sb = new SelectBuilder();
		sb.addWhere(new Triple(NodeFactory.createURI("one"), NodeFactory
				.createURI("two"), NodeFactory.createURI("three")));

		handler.addUnion(sb);
		assertContainsRegex(WHERE + OPEN_CURLY + OPEN_CURLY 
				+ uri("s") + SPACE+ uri("p") + SPACE + uri("o")+CLOSE_CURLY
				+OPT_SPACE + UNION + OPEN_CURLY
				+ uri("one") + SPACE + uri("two") + SPACE + uri("three")
				+ OPT_SPACE + CLOSE_CURLY+ CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddUnionWithVar() {
		SelectBuilder sb = new SelectBuilder().addVar("x").addWhere(
				new Triple(NodeFactory.createURI("one"), NodeFactory
						.createURI("two"), NodeFactory.createURI("three")));

		handler.addUnion(sb);
		assertContainsRegex(WHERE + OPEN_CURLY + UNION + OPEN_CURLY + SELECT
				+ var("x") + SPACE + WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + uri("three") + OPT_SPACE
				+ CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddUnionToExistingWithVar() {
		handler.addWhere( new Triple( NodeFactory.createURI( "s"), NodeFactory.createURI("p"),
				NodeFactory.createURI("o")));
		SelectBuilder sb = new SelectBuilder().addVar("x").addWhere(
				new Triple(NodeFactory.createURI("one"), NodeFactory
						.createURI("two"), NodeFactory.createURI("three")));

		handler.addUnion(sb);
		assertContainsRegex(WHERE + OPEN_CURLY + OPEN_CURLY 
				+ uri("s") + SPACE+ uri("p") + SPACE + uri("o")+CLOSE_CURLY
				+OPT_SPACE 				
				+ UNION + OPEN_CURLY + SELECT
				+ var("x") + SPACE + WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + uri("three") + OPT_SPACE
				+ CLOSE_CURLY, query.toString());
	}
	@Test
	public void addGraph() {

		WhereHandler handler2 = new WhereHandler(new Query());
		handler2.addWhere(new Triple(NodeFactory.createURI("one"), NodeFactory
				.createURI("two"), NodeFactory.createURI("three")));

		handler.addGraph(NodeFactory.createURI("graph"), handler2);
		assertContainsRegex(WHERE + OPEN_CURLY + "GRAPH" + SPACE
				+ uri("graph") + SPACE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + uri("three") + OPT_SPACE
				+ CLOSE_CURLY + CLOSE_CURLY, query.toString());

	}

	@Test
	public void testSetVarsInTriple() {
		Var v = Var.alloc("v");
		handler.addWhere(new Triple(NodeFactory.createURI("one"), NodeFactory
				.createURI("two"), v));
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + var("v") + OPT_SPACE
				+ CLOSE_CURLY, query.toString());
		Map<Var, Node> values = new HashMap<Var, Node>();
		values.put(v, NodeFactory.createURI("three"));
		handler.setVars(values);
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + uri("three") + OPT_SPACE
				+ CLOSE_CURLY, query.toString());
	}

	@Test
	public void testSetVarsInFilter() throws ParseException {
		handler.addFilter("?one < ?v");
		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE
				+ OPEN_PAREN + var("one") + OPT_SPACE + LT + OPT_SPACE
				+ var("v") + CLOSE_PAREN + CLOSE_CURLY, query.toString());
		Map<Var, Node> values = new HashMap<Var, Node>();

		values.put(Var.alloc("v"), NodeFactory
				.createLiteral(LiteralLabelFactory.createTypedLiteral(10)));
		handler.setVars(values);
		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE
				+ OPEN_PAREN + var("one") + OPT_SPACE + LT + OPT_SPACE
				+ quote("10") + "\\^\\^"
				+ uri("http://www.w3.org/2001/XMLSchema#int") + CLOSE_PAREN
				+ CLOSE_CURLY, query.toString());

	}

	@Test
	public void testSetVarsInOptional() {
		Var v = Var.alloc("v");
		handler.addOptional(new Triple(NodeFactory.createURI("one"),
				NodeFactory.createURI("two"), v));
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ var("v") + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY,
				query.toString());
		Map<Var, Node> values = new HashMap<Var, Node>();
		values.put(v, NodeFactory.createURI("three"));
		handler.setVars(values);
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ uri("three") + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testSetVarsInSubQuery() {
		Var v = Var.alloc("v");
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
		handler.addSubQuery(sb);
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + ".+"
				+ uri("two") + ".+" + var("v") + ".+" + CLOSE_CURLY,
				query.toString());
		Map<Var, Node> values = new HashMap<Var, Node>();
		values.put(v, NodeFactory.createURI("three"));
		handler.setVars(values);
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + ".+"
				+ uri("two") + ".+" + uri("three") + ".+" + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testSetVarsInUnion() {
		Var v = Var.alloc("v");
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
		handler.addUnion(sb);
		assertContainsRegex(WHERE + OPEN_CURLY + UNION + OPEN_CURLY
				+ uri("one") + ".+" + uri("two") + ".+" + var("v") + ".+"
				+ CLOSE_CURLY, query.toString());
		Map<Var, Node> values = new HashMap<Var, Node>();
		values.put(v, NodeFactory.createURI("three"));
		handler.setVars(values);
		assertContainsRegex(WHERE + OPEN_CURLY + UNION + OPEN_CURLY
				+ uri("one") + ".+" + uri("two") + ".+" + uri("three")
				+ ".+" + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testBindStringVar() throws ParseException {
		Var v = Var.alloc("foo");
		handler.addBind("rand()", v);

		assertContainsRegex(
				OPEN_CURLY + BIND + OPEN_PAREN + "rand\\(\\)" + SPACE + "AS"
						+ SPACE + var("foo") + CLOSE_PAREN + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testBindExprVar() {
		Var v = Var.alloc("foo");
		handler.addBind(new E_Random(), v);

		assertContainsRegex(
				OPEN_CURLY + BIND + OPEN_PAREN + "rand\\(\\)" + SPACE + "AS"
						+ SPACE + var("foo") + CLOSE_PAREN + CLOSE_CURLY,
				query.toString());
	}
	
	@Test
	public void testList() {
		Node n = handler.list( "<one>", "?var", "'three'" );

		assertContainsRegex(WHERE + OPEN_CURLY 
				+ "_:b0"+SPACE+ uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#first") + SPACE	+ uri("one") + SEMI 
				+ SPACE + uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest") + SPACE+"_:b1"+ DOT
				+ SPACE + "_:b1"+SPACE+ uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#first") + SPACE + var("var") + SEMI
				+ SPACE + uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest") + SPACE+"_:b2"+ DOT
				+ SPACE + "_:b2"+SPACE+ uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#first") + SPACE + quote("three") + SEMI
				+ SPACE + uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest") + SPACE +uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil") 
				+ CLOSE_CURLY, query.toString());
		
	 	assertTrue( n.isBlank() );
	}
	
	@Test
	public void testListInTriple() {
		handler.addWhere( new Triple(handler.list( "<one>", "?var", "'three'" ), ResourceFactory.createResource("foo").asNode(), 
				ResourceFactory.createResource("bar").asNode()));

		assertContainsRegex(
				 "_:b0"+SPACE+ uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#first") + SPACE	+ uri("one") + SEMI 
				+ SPACE + uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest") + SPACE+"_:b1"+ DOT
				+ SPACE + "_:b1"+SPACE+ uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#first") + SPACE + var("var") + SEMI
				+ SPACE + uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest") + SPACE+"_:b2"+ DOT
				+ SPACE + "_:b2"+SPACE+ uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#first") + SPACE + quote("three") + SEMI
				+ SPACE + uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest") + SPACE +uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil") 
				, query.toString());
		
		assertContainsRegex(
				 "_:b0"+SPACE+ uri("foo") + SPACE	+ uri("bar"), query.toString());

	 
	}
}
