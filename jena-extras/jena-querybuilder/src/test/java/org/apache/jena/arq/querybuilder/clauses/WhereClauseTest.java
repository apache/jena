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
package org.apache.jena.arq.querybuilder.clauses;

import java.util.List;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Random;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.junit.After;
import org.junit.Assert;

import static org.junit.Assert.*;

import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;
import org.xenei.junit.contract.IProducer;

@Contract(WhereClause.class)
public class WhereClauseTest<T extends WhereClause<?>> extends
		AbstractClauseTest {

	// the producer we will user
	private IProducer<T> producer;

	@Contract.Inject
	// define the method to set producer.
	public final void setProducer(IProducer<T> producer) {
		this.producer = producer;
	}

	protected final IProducer<T> getProducer() {
		return producer;
	}

	@After
	public final void cleanupWhereClauseTest() {
		getProducer().cleanUp(); // clean up the producer for the next run
	}

	@ContractTest
	public void testAddWhereStrings() {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addWhere("<one>",
				"<two>", "three");
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + quote("three") + presentStringType()
				+ OPT_SPACE + CLOSE_CURLY, builder.buildString());
	}

	@ContractTest
	public void testAddOptionalString() {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addOptional("<one>",
				"<two>", "three");
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ quote("three") + presentStringType() + OPT_SPACE
				+ CLOSE_CURLY + CLOSE_CURLY, builder.buildString());

	}

	@ContractTest
	public void testAddOptionalStringWithPath() {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addOptional("<one>",
				"<two>/<dos>", "three");
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two")+"/"+uri("dos") + SPACE
				+ quote("three") + presentStringType() + OPT_SPACE
				+ CLOSE_CURLY + CLOSE_CURLY, builder.buildString());

	}
	
	@ContractTest
	public void testAddOptionalObjects() {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addOptional(
				NodeFactory.createURI("one"), NodeFactory.createURI("two"),
				NodeFactory.createURI("three"));
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ uri("three") + OPT_SPACE + CLOSE_CURLY,
				builder.buildString());
	}

	@ContractTest
	public void testAddOptionalTriple() {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addOptional(
				NodeFactory.createURI("one"), NodeFactory.createURI("two"),
				NodeFactory.createURI("three"));

		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ uri("three") + OPT_SPACE + CLOSE_CURLY,
				builder.buildString());
	}
	
	@ContractTest
	public void testAddOptionalTriplePath() {
		WhereClause<?> whereClause = getProducer().newInstance();
		PrefixMapping pmap = new PrefixMappingImpl();
		pmap.setNsPrefix("ts", "urn:test:");
		Path path = PathParser.parse( "ts:two/ts:dos", pmap);
		AbstractQueryBuilder<?> builder = whereClause.addOptional(new TriplePath(
				NodeFactory.createURI("one"), path,
				NodeFactory.createURI("three")));

		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("urn:test:two")+"/"+uri("urn:test:dos") + SPACE
				+ uri("three") + OPT_SPACE + CLOSE_CURLY,
				builder.buildString());
	}
	
	@ContractTest
	public void testAddOptionalObjectsWithPath() {
		WhereClause<?> whereClause = getProducer().newInstance();
		PrefixMapping pmap = new PrefixMappingImpl();
		pmap.setNsPrefix("ts", "urn:test:");
		Path path = PathParser.parse( "ts:two/ts:dos", pmap);
		AbstractQueryBuilder<?> builder = whereClause.addOptional(
				NodeFactory.createURI("one"), path,
				NodeFactory.createURI("three"));

		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("urn:test:two")+"/"+uri("urn:test:dos") + SPACE
				+ uri("three") + OPT_SPACE + CLOSE_CURLY,
				builder.buildString());
	}

	@ContractTest
	public void testAddOptionalGroupPattern() throws ParseException {
		
		Var s = Var.alloc("s" );
		Node q = NodeFactory.createURI( "urn:q" );
		Node v = NodeFactory.createURI( "urn:v" );
		Var x = Var.alloc("x");
		Node n123 = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(123));	
		
		SelectBuilder pattern = new SelectBuilder();
		pattern.addWhere( new Triple( s, q,  n123 ) );
		pattern.addWhere( new Triple( s, v, x));
		pattern.addFilter( "?x>56");
		
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addOptional( pattern );
		
		Query expected = QueryFactory.create( "SELECT * WHERE { OPTIONAL { ?s <urn:q> '123'^^<http://www.w3.org/2001/XMLSchema#int> . ?s <urn:v> ?x . FILTER(?x>56) }}");
		
		Assert.assertEquals( expected.getQueryPattern(), builder.build().getQueryPattern());	
	}

	@ContractTest
	public void testAddFilter() throws ParseException {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addFilter("?one<10");

		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE
				+ OPEN_PAREN + var("one") + OPT_SPACE + LT + OPT_SPACE + "10"
				+ CLOSE_PAREN + CLOSE_CURLY, builder.buildString());
	}

	@ContractTest
	public void testAddSubQuery() {
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "urn:uri").addVar("?x")
				.addWhere("pfx:one", "pfx:two", "pfx:three");
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addSubQuery(sb);

		assertContainsRegex(PREFIX + "pfx:" + SPACE + uri("urn:uri") + SPACE
				+ ".*" + WHERE + OPEN_CURLY + OPEN_CURLY + SELECT + var("x")
				+ SPACE + WHERE + OPEN_CURLY + "pfx:one" + SPACE + "pfx:two"
				+ SPACE + "pfx:three" + OPT_SPACE + CLOSE_CURLY,
				builder.buildString());

	}

	@ContractTest
	public void testAddUnion() {
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addVar("?x")
				.addWhere("<one>", "<two>", "three");
		WhereClause<?> whereClause = getProducer().newInstance();
		whereClause.getWhereHandler().addWhere( new TriplePath(Triple.ANY));
		AbstractQueryBuilder<?> builder = whereClause.addUnion(sb);

		String str = builder.buildString();
		assertContainsRegex(PREFIX + "pfx:" + SPACE + uri("uri") + SPACE, str );
		assertContainsRegex( WHERE + OPEN_CURLY + OPEN_CURLY+"ANY"+SPACE+"ANY"+SPACE+"ANY"+CLOSE_CURLY
				+ SPACE+UNION + OPEN_CURLY + SELECT + var("x") + SPACE + WHERE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ quote("three") + presentStringType() + OPT_SPACE
				+ CLOSE_CURLY + CLOSE_CURLY, str);

	}

	@ContractTest
	public void testSetVarsInTriple() {
		Var v = Var.alloc("v");
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addWhere(new Triple(
				NodeFactory.createURI("one"), NodeFactory.createURI("two"), v));
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + var("v") + OPT_SPACE
				+ CLOSE_CURLY, builder.buildString());

		builder.setVar(v, NodeFactory.createURI("three"));

		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + uri("three") + OPT_SPACE
				+ CLOSE_CURLY, builder.buildString());

		builder.setVar(v, NodeFactory.createURI("four"));

		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + uri("four") + OPT_SPACE
				+ CLOSE_CURLY, builder.buildString());

		builder.setVar(v, null);

		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + var("v") + OPT_SPACE
				+ CLOSE_CURLY, builder.buildString());

	}

	@ContractTest
	public void testSetVarsInFilter() throws ParseException {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addFilter("?one < ?v");
		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE
				+ OPEN_PAREN + var("one") + OPT_SPACE + LT + OPT_SPACE
				+ var("v") + CLOSE_PAREN + CLOSE_CURLY, builder.buildString());

		builder.setVar(Var.alloc("v"), NodeFactory
				.createLiteral(LiteralLabelFactory.createTypedLiteral(10)));

		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE
				+ OPEN_PAREN + var("one") + OPT_SPACE + LT + OPT_SPACE
				+ quote("10") + "\\^\\^"
				+ uri("http://www.w3.org/2001/XMLSchema#int") + CLOSE_PAREN
				+ CLOSE_CURLY, builder.buildString());

	}

	@ContractTest
	public void testSetVarsInOptional() {
		Var v = Var.alloc("v");
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addOptional(new Triple(
				NodeFactory.createURI("one"), NodeFactory.createURI("two"), v));
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ var("v") + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY,
				builder.buildString());

		builder.setVar(v, NodeFactory.createURI("three"));
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ uri("three") + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY,
				builder.buildString());
	}

	@ContractTest
	public void testSetVarsInSubQuery() {
		Var v = Var.alloc("v");
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addSubQuery(sb);

		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + var("v") + CLOSE_CURLY,
				builder.buildString());

		builder.setVar(v, NodeFactory.createURI("three"));
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + uri("three") + CLOSE_CURLY,
				builder.buildString());
	}

	@ContractTest
	public void testSetVarsInUnion() {
		Var v = Var.alloc("v");
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addUnion(sb);
		assertContainsRegex(WHERE + OPEN_CURLY + UNION + OPEN_CURLY
				+ uri("one") + SPACE + uri("two") + SPACE + var("v") 
				+ CLOSE_CURLY, builder.buildString());

		builder.setVar(v, NodeFactory.createURI("three"));
		assertContainsRegex(WHERE + OPEN_CURLY + UNION + OPEN_CURLY
				+ uri("one") + SPACE + uri("two") + SPACE + uri("three")
				+ SPACE + CLOSE_CURLY, builder.buildString());
	}

	@ContractTest
	public void testBindStringVar() throws ParseException {
		Var v = Var.alloc("foo");
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addBind("rand()", v);

		assertContainsRegex(
				OPEN_CURLY + BIND + OPEN_PAREN + "rand\\(\\)" + SPACE + "AS"
						+ SPACE + var("foo") + CLOSE_PAREN + CLOSE_CURLY,
				builder.buildString());
		builder.setVar(v, NodeFactory.createURI("three"));
		Query q = builder.build();
		ElementGroup eg = (ElementGroup) q.getQueryPattern();
		List<Element> lst = eg.getElements();
		assertEquals( "Should only be one element",  1, lst.size());
		assertTrue( "Should have an ElementTriplesBlock", lst.get(0) instanceof ElementTriplesBlock );
		ElementTriplesBlock etb = (ElementTriplesBlock)lst.get(0);
		assertTrue( "ElementGroup should be empty", etb.isEmpty() );
	}

	@ContractTest
	public void testBindExprVar() {
		Var v = Var.alloc("foo");
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause
				.addBind(new E_Random(), v);

		assertContainsRegex(
				OPEN_CURLY + BIND + OPEN_PAREN + "rand\\(\\)" + SPACE + "AS"
						+ SPACE + var("foo") + CLOSE_PAREN + CLOSE_CURLY,
				builder.buildString());
		
		builder.setVar(v, NodeFactory.createURI("three"));
		Query q = builder.build();
		ElementGroup eg = (ElementGroup) q.getQueryPattern();
		List<Element> lst = eg.getElements();
		assertEquals( "Should only be one element",  1, lst.size());
		assertTrue( "Should have an ElementTriplesBlock", lst.get(0) instanceof ElementTriplesBlock );
		ElementTriplesBlock etb = (ElementTriplesBlock)lst.get(0);
		assertTrue( "ElementGroup should be empty", etb.isEmpty() );
	}
	
	@ContractTest
	public void testList() {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addWhere(whereClause.list( "<one>", "?two", "'three'"),
				"<foo>", "<bar>");
	
		assertContainsRegex(
				"_:b0"+SPACE+ uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#first") + SPACE	+ uri("one") + SEMI 
				+ SPACE + uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest") + SPACE+"_:b1"+ DOT
				+ SPACE + "_:b1"+SPACE+ uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#first") + SPACE + var("two") + SEMI
				+ SPACE + uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest") + SPACE+"_:b2"+ DOT
				+ SPACE + "_:b2"+SPACE+ uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#first") + SPACE + quote("three") + SEMI
				+ SPACE + uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest") + SPACE +uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil") 
				, builder.buildString());
		
		assertContainsRegex(
				 "_:b0"+SPACE+ uri("foo") + SPACE	+ uri("bar"), builder.buildString());
	}
}
