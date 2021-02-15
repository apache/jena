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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.arq.querybuilder.WhereValidator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.syntax.ElementData;
import org.junit.Before;
import org.junit.Test;

public class ValuesHandlerTest extends AbstractHandlerTest {

	private Query query;
	private ValuesHandler handler;

	@Before
	public void setup() {
		query = new Query();
		handler = new ValuesHandler(query);
	}

	@Test
	public void noChangeTest()
	{
		handler.build();
		assertNull( query.getValuesVariables());
		assertNull( query.getValuesData() );
	}

	@Test
	public void oneVarNullData() {
		handler.addValueVar( Var.alloc( "x" ), null);
		handler.build();
		assertNull( query.getValuesVariables());
		assertNull( query.getValuesData() );
	}

	@Test
	public void oneVarEmptyData() {
		handler.addValueVar( Var.alloc( "x" ), Collections.emptyList());
		handler.build();
		assertNull( query.getValuesVariables());
		assertNull( query.getValuesData() );
	}

	@Test
	public void oneVarOneData() {
		Node n =NodeFactory.createLiteral( "hello");
		Var v = Var.alloc( "x" );
		handler.addValueVar( v, Arrays.asList(n) );
		handler.build();

		List<Var> vars = query.getValuesVariables();
		assertEquals( 1, vars.size());
		assertEquals( v, vars.get(0));


		assertNotNull( query.getValuesData() );
		List<Binding> lb = query.getValuesData();
		assertEquals( 1, lb.size());
		Binding b = lb.get(0);

		assertTrue( b.contains(v));
		assertEquals( n, b.get(v) );
	}

	@Test
	public void oneVarTwoData() {
		Node n =NodeFactory.createLiteral( "hello");
		Node n2 = NodeFactory.createLiteral( "there");

		Var v = Var.alloc( "x" );
		handler.addValueVar( v, Arrays.asList( n, n2 ));
		handler.build();

		List<Var> vars = query.getValuesVariables();
		assertEquals( 1, vars.size());
		assertEquals( v, vars.get(0));

		assertNotNull( query.getValuesData() );
		List<Binding> lb = query.getValuesData();
		assertEquals( 2, lb.size());

		List<Node> ln = new ArrayList<Node>();
		ln.add(n);
		ln.add(n2);
		for (Binding b : lb ) {
			assertTrue( b.contains(v));

			assertTrue( ln.contains( b.get(v) ) );
			ln.remove( b.get(v));
		}
	}

	@Test
	public void twoVarOneData() {
		Node n =NodeFactory.createLiteral( "hello");
		Var v = Var.alloc( "x" );
		Var v2 = Var.alloc( "y" );
		handler.addValueVar( v, Arrays.asList( n) );
		handler.addValueVar( v2, null );
		try {
			handler.build();
			fail( "Shoud have thrown QueryBuildException");
		}
		catch (QueryBuildException expected)
		{
			// do nothing.
		}
	}



	@Test
	public void twoVarTwoBlocks() {
		Node n =NodeFactory.createLiteral( "hello");
		Node nn = NodeFactory.createLiteral( "hola");
		Node n2 = NodeFactory.createLiteral( "there");
		Node nn2 = NodeFactory.createLiteral( "aqui");

		Var v = Var.alloc( "x" );
		Var v2 = Var.alloc( "y");

		handler.addValueVar( v, null );
		handler.addValueVar(v2, null);
		handler.addValueRow( Arrays.asList( n, n2));
		handler.addValueRow( Arrays.asList( nn, nn2));

		handler.build();

		List<Var> vars = query.getValuesVariables();
		assertEquals( 2, vars.size());
		assertTrue( vars.contains(v));
		assertTrue( vars.contains(v2));

		assertNotNull( query.getValuesData() );
		List<Binding> lb = query.getValuesData();
		assertEquals( 2, lb.size());

		List<Node> ln = new ArrayList<Node>();
		ln.add(n);
		ln.add(nn);

		List<Node> ln2 = new ArrayList<Node>();
		ln2.add(n2);
		ln2.add(nn2);


		for (Binding b : lb ) {
			assertTrue( b.contains(v));
			assertTrue( ln.contains( b.get(v) ) );
			ln.remove( b.get(v));
			assertTrue( b.contains(v2));
			assertTrue( ln2.contains( b.get(v2) ) );
			ln2.remove( b.get(v2));
		}
	}

	@Test
	public void twoVarTwoBlocksWithVarReplacement() {
		Node n =NodeFactory.createLiteral( "hello");
		Node nn = NodeFactory.createLiteral( "hola");
		Node n2 = NodeFactory.createLiteral( "there");
		Node nn2 = NodeFactory.createLiteral( "aqui");

		Var v = Var.alloc( "x" );
		Var v2 = Var.alloc( "y");

		handler.addValueVar( v, null );
		handler.addValueVar(v2, null);
		handler.addValueRow( Arrays.asList( n, n2));
		handler.addValueRow( Arrays.asList( nn, nn2));

		Map<Var,Node> replaceVars = new HashMap<Var,Node>();
		replaceVars.put( v2, NodeFactory.createBlankNode());

		handler.setVars(replaceVars);
		handler.build();

		List<Var> vars = query.getValuesVariables();
		assertEquals( 1, vars.size());
		assertEquals( v, vars.get(0));

		assertNotNull( query.getValuesData() );
		List<Binding> lb = query.getValuesData();
		assertEquals( 2, lb.size());

		List<Node> ln = new ArrayList<Node>();
		ln.add(n);
		ln.add(nn);
		for (Binding b : lb ) {
			assertTrue( b.contains(v));
			assertFalse( b.contains(v2));
			assertTrue( ln.contains( b.get(v) ) );
			ln.remove( b.get(v));
		}
	}

	@Test
	public void twoVarTwoBlocksReplaceDataVar() {
		Node n =NodeFactory.createLiteral( "hello");
		Node nn = NodeFactory.createLiteral( "hola");
		Node n2 = NodeFactory.createLiteral( "there");
		Var nn2 = Var.alloc( "z");

		Var v = Var.alloc( "x" );
		Var v2 = Var.alloc( "y");

		handler.addValueVar( v, null );
		handler.addValueVar(v2, null);
		handler.addValueRow( Arrays.asList( n, n2));
		handler.addValueRow( Arrays.asList( nn, nn2));

		Node rep = NodeFactory.createLiteral( "aqui");
		Map<Var,Node> replaceVars = new HashMap<Var,Node>();
		replaceVars.put( nn2, rep);

		handler.setVars(replaceVars);
		handler.build();

		List<Var> vars = query.getValuesVariables();
		assertEquals( 2, vars.size());
		assertTrue( vars.contains(v));
		assertTrue( vars.contains(v2));

		assertNotNull( query.getValuesData() );
		List<Binding> lb = query.getValuesData();
		assertEquals( 2, lb.size());

		List<Node> ln = new ArrayList<Node>();
		ln.add(n);
		ln.add(nn);

		List<Node> ln2 = new ArrayList<Node>();
		ln2.add(n2);
		ln2.add(rep);

		for (Binding b : lb ) {
			assertTrue( b.contains(v));
			assertTrue( ln.contains( b.get(v) ) );
			ln.remove( b.get(v));
			assertTrue( b.contains(v2));
			assertTrue( ln2.contains( b.get(v2) ) );
			ln2.remove( b.get(v2));
		}
	}

	@Test
	public void oneVarTwoBlocksWithReplacement() {
		Node n =NodeFactory.createLiteral( "hello");
		Node n2 = NodeFactory.createLiteral( "there");

		Var v = Var.alloc( "x" );
		handler.addValueVar( v, Arrays.asList( n, n2) );

		Map<Var,Node> replaceVars = new HashMap<Var,Node>();
		replaceVars.put( v, NodeFactory.createBlankNode());

		handler.setVars(replaceVars);
		handler.build();

		assertNull( query.getValuesVariables());
		assertNull( query.getValuesData() );
	}

	@Test
	public void testAddSquare() {

		Node n =NodeFactory.createLiteral( "hello");
		Node nn = NodeFactory.createLiteral( "hola");
		Node n2 = NodeFactory.createLiteral( "there");
		Node nn2 = NodeFactory.createLiteral( "aqui");

		Var v = Var.alloc( "x" );
		Var v2 = Var.alloc( "y");

		handler.addValueVar( v, Arrays.asList( n, n2 ) );
		handler.addValueVar( v2, Arrays.asList( nn, nn2 ) );

		ValuesHandler handler2 = new ValuesHandler(new Query());
		Node n3 = NodeFactory.createLiteral( "why");
		Node nn3 = NodeFactory.createLiteral( "quando");
		handler2.addValueVar( v, Arrays.asList( n3 ) );
		handler2.addValueVar( v2, Arrays.asList( nn3 ) );

		handler.addAll( handler2 );
		handler.build();

		List<Var> vars = query.getValuesVariables();
		assertEquals( 2, vars.size());
		assertTrue( vars.contains(v));
		assertTrue( vars.contains(v2));

		assertNotNull( query.getValuesData() );
		List<Binding> lb = query.getValuesData();
		assertEquals( 3, lb.size());

		List<Node> ln = new ArrayList<Node>();
		ln.add(n);
		ln.add(n2);
		ln.add(n3);

		List<Node> ln2 = new ArrayList<Node>();
		ln2.add(nn);
		ln2.add(nn2);
		ln2.add(nn3);


		for (Binding b : lb ) {
			assertTrue( b.contains(v));
			assertTrue( ln.contains( b.get(v) ) );
			ln.remove( b.get(v));
			assertTrue( b.contains(v2));
			assertTrue( ln2.contains( b.get(v2) ) );
			ln2.remove( b.get(v2));
		}
	}

	@Test
	public void testAddNotSquare() {

		Node n =NodeFactory.createLiteral( "hello");
		Node nn = NodeFactory.createLiteral( "hola");
		Node n2 = NodeFactory.createLiteral( "there");
		Node nn2 = NodeFactory.createLiteral( "aqui");

		Var v = Var.alloc( "x" );
		Var v2 = Var.alloc( "y");
		Var v3 = Var.alloc( "z");

		handler.addValueVar( v, Arrays.asList( n, n2 ) );
		handler.addValueVar( v2, Arrays.asList( nn, nn2 ) );

		ValuesHandler handler2 = new ValuesHandler(new Query());
		Node n3 = NodeFactory.createLiteral( "why");
		Node nn3 = NodeFactory.createLiteral( "quando");
		handler2.addValueVar( v2, Arrays.asList( n3 ) );
		handler2.addValueVar( v3, Arrays.asList( nn3 ) );

		handler.addAll( handler2 );
		handler.build();

		List<Var> vars = query.getValuesVariables();
		assertEquals( 3, vars.size());
		assertTrue( vars.contains(v));
		assertTrue( vars.contains(v2));
		assertTrue( vars.contains(v3));

		assertNotNull( query.getValuesData() );
		List<Binding> lb = query.getValuesData();
		assertEquals( 3, lb.size());


		for (Binding b : lb ) {
			assertTrue( b.contains(v2));
			Node node = b.get(v2);
			if (node.equals(nn))
			{
				assertEquals( n, b.get(v));
				assertFalse( b.contains( v3 ));
			}
			else if (node.equals(nn2)) {
				assertEquals( n2, b.get(v));
				assertFalse( b.contains( v3 ));
			} else if (node.equals(n3)) {
				assertFalse( b.contains( v ));
				assertEquals( nn3, b.get(v3));
			} else {
				fail( "Wrong data in table");
			}
		}
	}

	@Test
	public void testAsElement() {
		final Var v = Var.alloc("v");
		final Var x = Var.alloc("x");
		final Node one = NodeFactory.createURI( "one");
		final Node two = NodeFactory.createURI( "two");
		final Node three = NodeFactory.createLiteral( "three");
		final Node four = NodeFactory.createLiteral( "four");

		handler.addValueVar( v, Arrays.asList( one, two ));
		handler.addValueVar( x, Arrays.asList( three, four ));

		ElementData edat = new ElementData();
		edat.add( v );
		edat.add( x );

		Binding binding1 = BindingFactory.binding(v, NodeFactory.createURI("one"), x, NodeFactory.createLiteral("three"));
		edat.add(binding1);
		Binding binding2 = BindingFactory.binding(v, NodeFactory.createURI("two"), x, NodeFactory.createLiteral("four"));
		edat.add(binding2);

		WhereValidator visitor = new WhereValidator( edat );
		handler.asElement().visit( visitor );
		assertTrue( visitor.matching );
	}

	@Test
	public void testisEmpty() {
		assertTrue( handler.isEmpty());
	}

	@Test
	public void testisEmpty_NoNodes() {
		handler.addValueVar( Var.alloc("v"), Collections.emptyList());
		assertFalse( handler.isEmpty());
	}

	@Test
	public void testisEmpty_NodeValues() {
		handler.addValueVar( Var.alloc("v"), Arrays.asList( Node.ANY));
		assertFalse( handler.isEmpty());
	}

	@Test
	public void testDataQuery() {
		// test that the getVars getMap and clear methods work.
		Var x = Var.alloc("x");
		Var y = Var.alloc("y");
		Node foo = NodeFactory.createURI( "foo" );
		Node bar = NodeFactory.createLiteral( "bar" );

		assertTrue(handler.getValuesVars().isEmpty());

		handler.addValueVar(x, Arrays.asList(foo));
		handler.addValueVar(y, Arrays.asList(bar));

		assertFalse(handler.getValuesVars().isEmpty());


		List<Var> lst = handler.getValuesVars();
		assertEquals(2, lst.size());
		assertEquals(x, lst.get(0));
		assertEquals(y, lst.get(1));

		Map<Var, List<Node>> map = handler.getValuesMap();
		assertEquals(2, map.keySet().size());
		List<Node> nodes = map.get(x);
		assertEquals(1, nodes.size());
		assertEquals(foo, nodes.get(0));

		nodes = map.get(y);
		assertEquals(1, nodes.size());
		assertEquals(bar, nodes.get(0));

		handler.clear();

		assertTrue(handler.getValuesVars().isEmpty());
		assertTrue(handler.getValuesMap().isEmpty());
		assertTrue(handler.isEmpty());

	}

}
