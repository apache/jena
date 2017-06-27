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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereValidator;
import org.apache.jena.arq.querybuilder.handlers.WhereHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Random;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.vocabulary.RDF;
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
	public void testAddAllOnEmpty() {
		Query query2 = new Query();
		WhereHandler handler2 = new WhereHandler(query2);
		handler2.addWhere(new TriplePath(new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
				NodeFactory.createLiteral("three"))));
		handler.addAll(handler2);
		handler.build();
		
		assertContainsRegex(
				WHERE + OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE + quote("three") + OPT_SPACE + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddAllPopulatedEmpty() {
		handler.addWhere(new TriplePath(Triple.ANY));
		Query query2 = new Query();
		WhereHandler handler2 = new WhereHandler(query2);
		handler2.addWhere(new TriplePath(new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
				NodeFactory.createLiteral("three"))));
		handler.addAll(handler2);
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + "ANY" + SPACE + "ANY" + SPACE + "ANY" + DOT + SPACE + uri("one")
				+ SPACE + uri("two") + SPACE + quote("three") + OPT_SPACE + CLOSE_CURLY, query.toString());
	}

	@Test
	public void addWhereTriple() {
		handler.addWhere(new TriplePath(new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
				NodeFactory.createURI("three"))));
		handler.build();
		
		assertContainsRegex(
				WHERE + OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE + uri("three") + OPT_SPACE + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddWhereObjects() {
		handler.addWhere(
				new TriplePath(new Triple(NodeFactory.createURI("one"), ResourceFactory.createResource("two").asNode(),
						ResourceFactory.createLangLiteral("three", "en-US").asNode())));
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE + quote("three") + "@en-US"
				+ OPT_SPACE + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddWhereObjectsWithPath() {
		PrefixMapping pmap = new PrefixMappingImpl();
		pmap.setNsPrefix("ts", "urn:test:");
		Path path = PathParser.parse("ts:two/ts:dos", pmap);
		handler.addWhere(new TriplePath(NodeFactory.createURI("one"), path,
				ResourceFactory.createLangLiteral("three", "en-US").asNode()));
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE + uri("urn:test:two") + "/" + uri("urn:test:dos")
				+ SPACE + quote("three") + "@en-US" + OPT_SPACE + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddWhereAnonymous() {
		handler.addWhere(new TriplePath(new Triple(Node.ANY, RDF.first.asNode(), Node.ANY)));
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + "ANY" + SPACE
				+ uri("http://www\\.w3\\.org/1999/02/22-rdf-syntax-ns#first") + SPACE + "ANY" + OPT_SPACE + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddOptionalStrings() {
		handler.addOptional(new TriplePath(new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
				NodeFactory.createURI("three"))));
		handler.build();
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE + OPEN_CURLY + uri("one") + SPACE + uri("two")
				+ SPACE + uri("three") + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddOptionalAnonymous() {
		handler.addOptional(new TriplePath(new Triple(Node.ANY, RDF.first.asNode(), Node.ANY)));
		handler.build();
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE + OPEN_CURLY + "ANY" + SPACE
				+ uri("http://www\\.w3\\.org/1999/02/22-rdf-syntax-ns#first") + SPACE + "ANY" + OPT_SPACE + CLOSE_CURLY
				+ CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddOptionalWhereHandler() throws ParseException {

		WhereHandler pattern = new WhereHandler(new Query());
		Var s = Var.alloc("s");
		Node q = NodeFactory.createURI("urn:q");
		Node v = NodeFactory.createURI("urn:v");
		Var x = Var.alloc("x");
		Node n123 = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(123));

		pattern.addWhere(new TriplePath(new Triple(s, q, n123)));
		pattern.addWhere(new TriplePath(new Triple(s, v, x)));

		handler.addOptional(pattern);
		handler.build();
		
		ElementPathBlock epb = new ElementPathBlock();
		ElementOptional optional = new ElementOptional(epb);
		TriplePath tp = new TriplePath( new Triple(s, q, n123));
		epb.addTriplePath( tp );
		 tp = new TriplePath( new Triple(s, v, x));
		epb.addTriplePath( tp );
		
		WhereValidator visitor = new WhereValidator( optional );
		handler.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );

	}

	@Test
	public void testAddOptionalObjects() {
		handler.addOptional(
				new TriplePath(new Triple(NodeFactory.createURI("one"), ResourceFactory.createResource("two").asNode(),
						ResourceFactory.createLangLiteral("three", "en-US").asNode())));
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE + OPEN_CURLY + uri("one") + SPACE + uri("two")
				+ SPACE + quote("three") + "@en-US" + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddOptionalObjectsWithPath() {
		PrefixMapping pmap = new PrefixMappingImpl();
		pmap.setNsPrefix("ts", "urn:test:");
		Path path = PathParser.parse("ts:two/ts:dos", pmap);

		handler.addOptional(new TriplePath(NodeFactory.createURI("one"), path,
				ResourceFactory.createLangLiteral("three", "en-US").asNode()));
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE + OPEN_CURLY + uri("one") + SPACE
				+ uri("urn:test:two") + "/" + uri("urn:test:dos") + SPACE + quote("three") + "@en-US" + OPT_SPACE
				+ CLOSE_CURLY + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddWhereStrings() {
		handler.addWhere(new TriplePath(new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
				NodeFactory.createURI("three"))));
		handler.build();
		
		assertContainsRegex(
				WHERE + OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE + uri("three") + OPT_SPACE + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddFilter() throws ParseException {
		handler.addFilter("?one < 10");
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE + OPEN_PAREN + var("one") + OPT_SPACE + LT
				+ OPT_SPACE + "10" + CLOSE_PAREN + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddFilterWithNamespace() throws ParseException {
		query.setPrefix("afn", "http://jena.apache.org/ARQ/function#");
		handler.addFilter("afn:namespace(?one) = 'foo'");
		handler.build();
		
		assertContainsRegex(
				WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE + OPEN_PAREN + "afn:namespace" + OPEN_PAREN + var("one")
						+ CLOSE_PAREN + OPT_SPACE + EQ + OPT_SPACE + QUOTE + "foo" + QUOTE + CLOSE_PAREN + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddFilterVarOnly() throws ParseException {
		handler.addFilter("?one");
		handler.build();
		
		assertContainsRegex(
				WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE + OPEN_PAREN + var("one") + CLOSE_PAREN + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddSubQueryWithVars() {
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addVar("?x").addWhere("<one>", "<two>", "three");
		handler.addSubQuery(sb);
		handler.build();
		
		assertContainsRegex(SELECT + var("x") + SPACE + WHERE + OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ quote("three") + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddSubQueryWithVarExpressions() throws ParseException {
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addVar("count(*)", "?x").addWhere("<one>", "<two>", "three");
		handler.addSubQuery(sb);
		handler.build();
		
		assertContainsRegex(SELECT + OPEN_PAREN + "count" + OPEN_PAREN + "\\*" + CLOSE_PAREN + SPACE + "AS" + SPACE
				+ var("x") + CLOSE_PAREN + SPACE + WHERE + OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ quote("three") + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddSubQueryWithoutVars() {
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", "three");
		handler.addSubQuery(sb);
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE + quote("three") + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testAddUnion() {
		Triple t1 = new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three"));
		Triple t2 = new Triple(NodeFactory.createURI("uno"), NodeFactory.createURI("dos"), NodeFactory.createURI("tres"));
				
		SelectBuilder sb1 = new SelectBuilder()
		.addWhere( t1 );
		
		SelectBuilder sb2 = new SelectBuilder()
				.addWhere( t2 );
				
		handler.addUnion(sb1);
		handler.addUnion(sb2);
		handler.build();
		

		ElementUnion union = new ElementUnion();
		ElementPathBlock epb1 = new ElementPathBlock();
		epb1.addTriple(t1);
		union.addElement( epb1 );
		
		ElementPathBlock epb2 = new ElementPathBlock();
		epb2.addTriple(t2);
		union.addElement( epb2 );
		
		WhereValidator visitor = new WhereValidator( union );
		handler.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
		
	}

	@Test
	public void testAddUnionOfOne() {
		Triple t1 = new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three"));
		SelectBuilder sb = new SelectBuilder().addWhere(t1);
		handler.addUnion(sb);
		handler.build();
		
		
		ElementPathBlock epb1 = new ElementPathBlock();
		epb1.addTriple(t1);
		
		WhereValidator visitor = new WhereValidator( epb1 );
		handler.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
	}
	
	@Test
	public void testAddUnionToExisting() {
		handler.addWhere(new TriplePath(
				new Triple(NodeFactory.createURI("s"), NodeFactory.createURI("p"), NodeFactory.createURI("o"))));
		SelectBuilder sb = new SelectBuilder();
		sb.addWhere(
				new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three")));
		handler.addUnion(sb);
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + OPEN_CURLY + uri("s") + SPACE + uri("p") + SPACE + uri("o")
				+ CLOSE_CURLY + OPT_SPACE + UNION + OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE + uri("three")
				+ OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testAddUnionWithVar() {
		Triple t1 = new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three"));
		Triple t2 = new Triple(NodeFactory.createURI("uno"), NodeFactory.createURI("dos"), NodeFactory.createURI("tres"));
		
		SelectBuilder sb = new SelectBuilder().addVar("x").addWhere( t1 );
		handler.addUnion(sb);
			
		SelectBuilder sb2 = new SelectBuilder().addWhere( t2 );
		handler.addUnion( sb2 );
		handler.build();
		
		ElementUnion union = new ElementUnion();
		Query q = new Query();
		q.setQuerySelectType();
		ElementPathBlock epb1 = new ElementPathBlock();
		epb1.addTriple(t1);
		q.setQueryPattern(epb1);
		q.addProjectVars( Arrays.asList(Var.alloc( "x" )));
		ElementSubQuery sq = new ElementSubQuery(q);
		union.addElement( sq );
		ElementPathBlock epb2 = new ElementPathBlock();
		epb2.addTriple(t2);
		union.addElement( epb2 );
		
		WhereValidator visitor = new WhereValidator( union );
		handler.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
				
	}

	@Test
	public void testAddUnionToExistingWithVar() {
		handler.addWhere(new TriplePath(
				new Triple(NodeFactory.createURI("s"), NodeFactory.createURI("p"), NodeFactory.createURI("o"))));
		handler.build();
		
		SelectBuilder sb = new SelectBuilder().addVar("x").addWhere(
				new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three")));

		handler.addUnion(sb);
		assertContainsRegex(WHERE + OPEN_CURLY + OPEN_CURLY + uri("s") + SPACE + uri("p") + SPACE + uri("o")
				+ CLOSE_CURLY + OPT_SPACE + UNION + OPEN_CURLY + SELECT + var("x") + SPACE + WHERE + OPEN_CURLY
				+ uri("one") + SPACE + uri("two") + SPACE + uri("three") + OPT_SPACE + CLOSE_CURLY, query.toString());
	}

	@Test
	public void addGraph() {

		WhereHandler handler2 = new WhereHandler(new Query());
		handler2.addWhere(new TriplePath(new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
				NodeFactory.createURI("three"))));

		handler.addGraph(NodeFactory.createURI("graph"), handler2);
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + "GRAPH" + SPACE + uri("graph") + SPACE + OPEN_CURLY + uri("one")
				+ SPACE + uri("two") + SPACE + uri("three") + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY, query.toString());

	}

	@Test
	public void testSetVarsInTriple() {
		Var v = Var.alloc("v");
		handler.addWhere(new TriplePath(new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"), v)));
		handler.build();
		
		assertContainsRegex(
				WHERE + OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE + var("v") + OPT_SPACE + CLOSE_CURLY,
				query.toString());
		Map<Var, Node> values = new HashMap<>();
		values.put(v, NodeFactory.createURI("three"));
		handler.setVars(values);
		assertContainsRegex(
				WHERE + OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE + uri("three") + OPT_SPACE + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testSetVarsInFilter() throws ParseException {
		handler.addFilter("?one < ?v");
		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE + OPEN_PAREN + var("one") + OPT_SPACE + LT
				+ OPT_SPACE + var("v") + CLOSE_PAREN + CLOSE_CURLY, query.toString());
		Map<Var, Node> values = new HashMap<>();

		values.put(Var.alloc("v"), NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(10)));
		handler.setVars(values);
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE + OPEN_PAREN + var("one") + OPT_SPACE + LT
				+ OPT_SPACE + quote("10") + "\\^\\^" + uri("http://www.w3.org/2001/XMLSchema#int") + CLOSE_PAREN
				+ CLOSE_CURLY, query.toString());

	}

	@Test
	public void testSetVarsInOptional() {
		Var v = Var.alloc("v");
		handler.addOptional(new TriplePath(new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"), v)));
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE + OPEN_CURLY + uri("one") + SPACE + uri("two")
				+ SPACE + var("v") + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY, query.toString());

		Map<Var, Node> values = new HashMap<>();
		values.put(v, NodeFactory.createURI("three"));
		handler.setVars(values);
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE + OPEN_CURLY + uri("one") + SPACE + uri("two")
				+ SPACE + uri("three") + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testSetVarsInSubQuery() {
		Var v = Var.alloc("v");
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
		handler.addSubQuery(sb);
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + ".+" + uri("two") + ".+" + var("v") + ".+" + CLOSE_CURLY,
				query.toString());
		
		Map<Var, Node> values = new HashMap<>();
		values.put(v, NodeFactory.createURI("three"));
		handler.setVars(values);
		handler.build();
		
		assertContainsRegex(
				WHERE + OPEN_CURLY + uri("one") + ".+" + uri("two") + ".+" + uri("three") + ".+" + CLOSE_CURLY,
				query.toString());
	}

	@Test
	public void testSetVarsInUnion() {
		Var v = Var.alloc("v");
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
		handler.addUnion(sb);
		SelectBuilder sb2 = new SelectBuilder().addWhere( "<uno>", "<dos>", "<tres>");
		handler.addUnion(sb2);
		handler.build();
		
		Node one = NodeFactory.createURI("one");
		Node two = NodeFactory.createURI("two");
		Node three = NodeFactory.createURI("three");
		Node uno = NodeFactory.createURI("uno");
		Node dos = NodeFactory.createURI("dos");
		Node tres = NodeFactory.createURI("tres");
		
		ElementUnion union = new ElementUnion();
		ElementPathBlock epb = new ElementPathBlock();
		Triple t = new Triple( one, two, v.asNode());
		epb.addTriple(t);
		union.addElement(epb);
		ElementPathBlock epb2 = new ElementPathBlock();
		t = new Triple( uno, dos, tres);
		epb2.addTriple(t);
		union.addElement(epb2);
		WhereValidator visitor = new WhereValidator( union );
		handler.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
		
		Map<Var, Node> values = new HashMap<>();
		values.put(v, three);
		handler.setVars(values);
		handler.build();
		
		 union = new ElementUnion();
		 epb = new ElementPathBlock();
		 t = new Triple( one, two, three);
		epb.addTriple(t);
		union.addElement(epb);
		 epb2 = new ElementPathBlock();
		t = new Triple( uno, dos, tres);
		epb2.addTriple(t);
		union.addElement(epb2);
		 visitor = new WhereValidator( union );
			handler.getQueryPattern().visit( visitor );
			assertTrue( visitor.matching );
			
	}

	@Test
	public void testBindStringVar() throws ParseException {
		Var v = Var.alloc("foo");
		handler.addBind("rand()", v);
		handler.build();
		
		assertContainsRegex(OPEN_CURLY + BIND + OPEN_PAREN + "rand\\(\\)" + SPACE + "AS" + SPACE + var("foo")
				+ CLOSE_PAREN + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testBindExprVar() {
		Var v = Var.alloc("foo");
		handler.addBind(new E_Random(), v);
		handler.build();
		
		assertContainsRegex(OPEN_CURLY + BIND + OPEN_PAREN + "rand\\(\\)" + SPACE + "AS" + SPACE + var("foo")
				+ CLOSE_PAREN + CLOSE_CURLY, query.toString());
	}

	@Test
	public void testList() {
		Node n = handler.list("<one>", "?var", "'three'");        
		
		Node one = NodeFactory.createURI("one");
		Node two = Var.alloc("var").asNode();
		Node three = NodeFactory.createLiteral( "three");
		
		ElementPathBlock epb = new ElementPathBlock();
		Node firstObject = NodeFactory.createBlankNode();		
		Node secondObject = NodeFactory.createBlankNode();
		Node thirdObject = NodeFactory.createBlankNode();
		
		epb.addTriplePath( new TriplePath( new Triple( firstObject, RDF.first.asNode(), one)));
		epb.addTriplePath( new TriplePath( new Triple( firstObject, RDF.rest.asNode(), secondObject)));
		epb.addTriplePath( new TriplePath( new Triple( secondObject, RDF.first.asNode(), two)));
		epb.addTriplePath( new TriplePath( new Triple( secondObject, RDF.rest.asNode(), thirdObject)));
		epb.addTriplePath( new TriplePath( new Triple( thirdObject, RDF.first.asNode(), three)));
		epb.addTriplePath( new TriplePath( new Triple( thirdObject, RDF.rest.asNode(), RDF.nil.asNode())));
		
		
		WhereValidator visitor = new WhereValidator( epb );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
		                    
		assertTrue(n.isBlank());
	}

	@Test
	public void testListInTriple() {
		handler.addWhere(new TriplePath(new Triple(handler.list("<one>", "?var", "'three'"),
				ResourceFactory.createResource("foo").asNode(), ResourceFactory.createResource("bar").asNode())));
		handler.build();
		
		assertContainsRegex(WHERE + OPEN_CURLY + PAREN_OPEN+SPACE+uri("one")+SPACE+var("var")+SPACE+quote("three")+SPACE+PAREN_CLOSE ,
		                    query.toString());

	}

	@Test
	public void testAddMinus() {
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", "three");
		handler.addMinus(sb);
		handler.build();
		
		assertContainsRegex(MINUS + OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE + quote("three") + CLOSE_CURLY,
				query.toString());		
	}
}
