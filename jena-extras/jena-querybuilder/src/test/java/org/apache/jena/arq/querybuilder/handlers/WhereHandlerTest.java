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

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.handlers.WhereHandler;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.lang.sparql_11.ParseException;
import com.hp.hpl.jena.vocabulary.RDF;

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

		assertContainsRegex(WHERE + OPEN_CURLY + node("one") + SPACE
				+ node("two") + SPACE + quote("three") + OPT_SPACE + DOT
				+ CLOSE_CURLY, query.toString());
	}

	@Test
	public void addWhereTriple() {
		handler.addWhere(new Triple(NodeFactory.createURI("one"), NodeFactory
				.createURI("two"), NodeFactory.createURI("three")));
		assertContainsRegex(WHERE + OPEN_CURLY + node("one") + SPACE
				+ node("two") + SPACE + node("three") + OPT_SPACE + DOT
				+ CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddWhereObjects() {
		handler.addWhere(new Triple(NodeFactory.createURI("one"),
				ResourceFactory.createResource("two").asNode(), ResourceFactory
						.createLangLiteral("three", "en-US").asNode()));
		assertContainsRegex(WHERE + OPEN_CURLY + node("one") + SPACE
				+ node("two") + SPACE + quote("three") + "@en-US" + OPT_SPACE
				+ DOT + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddWhereAnonymous() {
		handler.addWhere(new Triple(Node.ANY, RDF.first.asNode(), Node.ANY));
		assertContainsRegex(WHERE + OPEN_CURLY + "ANY" + SPACE
				+ node("http://www\\.w3\\.org/1999/02/22-rdf-syntax-ns#first")
				+ SPACE + "ANY" + OPT_SPACE + DOT + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddOptionalStrings() {
		handler.addOptional(new Triple(NodeFactory.createURI("one"),
				NodeFactory.createURI("two"), NodeFactory.createURI("three")));
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + node("one") + SPACE + node("two") + SPACE
				+ node("three") + OPT_SPACE + DOT + CLOSE_CURLY + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddOptionalAnonymous() {
		handler.addOptional(new Triple(Node.ANY, RDF.first.asNode(), Node.ANY));
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + "ANY" + SPACE
				+ node("http://www\\.w3\\.org/1999/02/22-rdf-syntax-ns#first")
				+ SPACE + "ANY" + OPT_SPACE + DOT + CLOSE_CURLY + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddOptionalObjects() {
		handler.addOptional(new Triple(NodeFactory.createURI("one"),
				ResourceFactory.createResource("two").asNode(), ResourceFactory
						.createLangLiteral("three", "en-US").asNode()));
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + node("one") + SPACE + node("two") + SPACE
				+ quote("three") + "@en-US" + OPT_SPACE + DOT + CLOSE_CURLY
				+ CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddWhereStrings() {
		handler.addWhere(new Triple(NodeFactory.createURI("one"), NodeFactory
				.createURI("two"), NodeFactory.createURI("three")));
		assertContainsRegex(WHERE + OPEN_CURLY + node("one") + SPACE
				+ node("two") + SPACE + node("three") + OPT_SPACE + DOT
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
				+ OPEN_CURLY + node("one") + ".+" + node("two") + ".+"
				+ quote("three") + ".+" + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddSubQueryWithoutVars() {
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", "three");
		handler.addSubQuery(sb);
		assertContainsRegex(WHERE + OPEN_CURLY + node("one") + ".+"
				+ node("two") + ".+" + quote("three") + ".+" + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddUnion() {
		SelectBuilder sb = new SelectBuilder();
		sb.addWhere(new Triple(NodeFactory.createURI("one"), NodeFactory
				.createURI("two"), NodeFactory.createURI("three")));

		handler.addUnion(sb);
		assertContainsRegex(WHERE + OPEN_CURLY + UNION + OPEN_CURLY
				+ node("one") + SPACE + node("two") + SPACE + node("three")
				+ OPT_SPACE + DOT + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddUnionWithVar() {
		SelectBuilder sb = new SelectBuilder().addVar("x").addWhere(
				new Triple(NodeFactory.createURI("one"), NodeFactory
						.createURI("two"), NodeFactory.createURI("three")));

		handler.addUnion(sb);
		assertContainsRegex(WHERE + OPEN_CURLY + UNION + OPEN_CURLY + SELECT
				+ var("x") + SPACE + WHERE + OPEN_CURLY + node("one") + SPACE
				+ node("two") + SPACE + node("three") + OPT_SPACE + DOT
				+ CLOSE_CURLY, query.toString());
	}

	@Test
	public void addGraph() {

		WhereHandler handler2 = new WhereHandler(new Query());
		handler2.addWhere(new Triple(NodeFactory.createURI("one"), NodeFactory
				.createURI("two"), NodeFactory.createURI("three")));

		handler.addGraph(NodeFactory.createURI("graph"), handler2);
		assertContainsRegex(WHERE + OPEN_CURLY + "GRAPH" + SPACE
				+ node("graph") + SPACE + OPEN_CURLY + node("one") + SPACE
				+ node("two") + SPACE + node("three") + OPT_SPACE + DOT
				+ CLOSE_CURLY + CLOSE_CURLY, query.toString());

	}

	@Test
	public void testSetVarsInTriple() {
		Var v = Var.alloc("v");
		handler.addWhere(new Triple(NodeFactory.createURI("one"), NodeFactory
				.createURI("two"), v));
		assertContainsRegex(WHERE + OPEN_CURLY + node("one") + SPACE
				+ node("two") + SPACE + var("v") + OPT_SPACE + DOT
				+ CLOSE_CURLY, query.toString());
		Map<Var, Node> values = new HashMap<Var, Node>();
		values.put(v, NodeFactory.createURI("three"));
		handler.setVars(values);
		assertContainsRegex(WHERE + OPEN_CURLY + node("one") + SPACE
				+ node("two") + SPACE + node("three") + OPT_SPACE + DOT
				+ CLOSE_CURLY, query.toString());
	}

	@Test
	public void testSetVarsInFilter() throws ParseException {
		handler.addFilter("?one < ?v");
		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE
				+ OPEN_PAREN + var("one") + OPT_SPACE + LT + OPT_SPACE
				+ var("v") + CLOSE_PAREN + CLOSE_CURLY, query.toString());
		Map<Var, Node> values = new HashMap<Var, Node>();

		values.put(Var.alloc("v"),
				NodeFactory.createLiteral(LiteralLabelFactory.create(10)));
		handler.setVars(values);
		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE
				+ OPEN_PAREN + var("one") + OPT_SPACE + LT + OPT_SPACE
				+ quote("10") + "\\^\\^"
				+ node("http://www.w3.org/2001/XMLSchema#int") + CLOSE_PAREN
				+ CLOSE_CURLY, query.toString());

	}

	@Test
	public void testSetVarsInOptional() {
		Var v = Var.alloc("v");
		handler.addOptional(new Triple(NodeFactory.createURI("one"),
				NodeFactory.createURI("two"), v));
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + node("one") + SPACE + node("two") + SPACE
				+ var("v") + OPT_SPACE + DOT + CLOSE_CURLY + CLOSE_CURLY,
				query.toString());
		Map<Var, Node> values = new HashMap<Var, Node>();
		values.put(v, NodeFactory.createURI("three"));
		handler.setVars(values);
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + node("one") + SPACE + node("two") + SPACE
				+ node("three") + OPT_SPACE + DOT + CLOSE_CURLY + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testSetVarsInSubQuery() {
		Var v = Var.alloc("v");
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
		handler.addSubQuery(sb);
		assertContainsRegex(WHERE + OPEN_CURLY + node("one") + ".+"
				+ node("two") + ".+" + var("v") + ".+" + CLOSE_CURLY,
				query.toString());
		Map<Var, Node> values = new HashMap<Var, Node>();
		values.put(v, NodeFactory.createURI("three"));
		handler.setVars(values);
		assertContainsRegex(WHERE + OPEN_CURLY + node("one") + ".+"
				+ node("two") + ".+" + node("three") + ".+" + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testSetVarsInUnion() {
		Var v = Var.alloc("v");
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
		handler.addUnion(sb);
		assertContainsRegex(WHERE + OPEN_CURLY + UNION + OPEN_CURLY
				+ node("one") + ".+" + node("two") + ".+" + var("v") + ".+"
				+ CLOSE_CURLY, query.toString());
		Map<Var, Node> values = new HashMap<Var, Node>();
		values.put(v, NodeFactory.createURI("three"));
		handler.setVars(values);
		assertContainsRegex(WHERE + OPEN_CURLY + UNION + OPEN_CURLY
				+ node("one") + ".+" + node("two") + ".+" + node("three")
				+ ".+" + CLOSE_CURLY, query.toString());
	}

}
