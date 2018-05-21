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
import java.util.Collection;
import java.util.List;

import org.apache.jena.arq.querybuilder.handlers.HandlerBlock;
import org.apache.jena.graph.FrontsNode ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.impl.LiteralLabel ;
import org.apache.jena.graph.impl.LiteralLabelFactory ;
import org.apache.jena.reasoner.rulesys.Node_RuleVariable ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.ExprVar ;
import org.apache.jena.vocabulary.RDF ;
import org.junit.Before;
import org.junit.Test;

public class AbstractQueryBuilderTest {

	private AbstractQueryBuilder<?> builder;

	@Before
	public void setup() {
		builder = new TestBuilder();
	}

	private class TestBuilder extends AbstractQueryBuilder<TestBuilder> {
		private HandlerBlock handlerBlock;
		
		public TestBuilder()
		{
			super();
			handlerBlock = new HandlerBlock( query );
			
		}
		@Override
		public String toString() {
			return "TestBuilder";
		}

		@Override
		public HandlerBlock getHandlerBlock() {
			return handlerBlock;
		}
		
	}

	private class NodeFront implements FrontsNode {
		Node n;

		NodeFront(Node n) {
			this.n = n;
		}

		@Override
		public Node asNode() {
			return n;
		}
	}

	@Test
	public void testMakeNode() {
		Node n = builder.makeNode(null);
		assertEquals(Node.ANY, n);

		n = builder.makeNode(RDF.type);
		assertEquals(RDF.type.asNode(), n);

		Node n2 = NodeFactory.createBlankNode();
		n = builder.makeNode(n2);
		assertEquals(n2, n);

		builder.addPrefix("demo", "http://example.com/");
		n = builder.makeNode("demo:type");
		assertEquals(NodeFactory.createURI("http://example.com/type"), n);

		n = builder.makeNode("<one>");
		assertEquals(NodeFactory.createURI("one"), n);

		n = builder.makeNode(builder);
		LiteralLabel ll = LiteralLabelFactory.createTypedLiteral(builder);
		assertEquals(NodeFactory.createLiteral(ll), n);
		
		n = builder.makeNode( NodeFactory.createVariable("foo"));
		assertTrue( n.isVariable());
		assertEquals( "foo", n.getName());
		assertTrue( n instanceof Var );

	}

	@Test
	public void testMakeVar() {
		Var v = AbstractQueryBuilder.makeVar(null);
		assertEquals(Var.ANON, v);

		v = AbstractQueryBuilder.makeVar("a");
		assertEquals(Var.alloc("a"), v);

		v = AbstractQueryBuilder.makeVar("?a");
		assertEquals(Var.alloc("a"), v);

		Node n = NodeFactory.createVariable("foo");
		v = AbstractQueryBuilder.makeVar(n);
		assertEquals(Var.alloc("foo"), v);

		NodeFront nf = new NodeFront(n);
		v = AbstractQueryBuilder.makeVar(nf);
		assertEquals(Var.alloc("foo"), v);

		v = AbstractQueryBuilder.makeVar(Node_RuleVariable.WILD);
		assertNull(v);

		ExprVar ev = new ExprVar("bar");
		v = AbstractQueryBuilder.makeVar(ev);
		assertEquals(Var.alloc("bar"), v);

		ev = new ExprVar(n);
		v = AbstractQueryBuilder.makeVar(ev);
		assertEquals(Var.alloc("foo"), v);

		ev = new ExprVar(Var.ANON);
		v = AbstractQueryBuilder.makeVar(ev);
		assertEquals(Var.ANON, v);

	}

	@Test
	public void testMakeValueNodes()
	{
		List<Object> list = new ArrayList<Object>();
		list.add( null);
		list.add( RDF.type );
		Node n2 = NodeFactory.createBlankNode();
		list.add( n2 );
		builder.addPrefix("demo", "http://example.com/");
		list.add( "demo:type" );
		list.add( "<one>" );
		list.add( builder );
		
		Collection<Node> result = builder.makeValueNodes(list.iterator());
		
		assertTrue( result.contains( null ));
		assertTrue( result.contains( RDF.type.asNode()));
		assertTrue( result.contains( n2 ));
		assertTrue( result.contains(NodeFactory.createURI("http://example.com/type") ));
		assertTrue( result.contains(NodeFactory.createURI("one")));

	}
}
