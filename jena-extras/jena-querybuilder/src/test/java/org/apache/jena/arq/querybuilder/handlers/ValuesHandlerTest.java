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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
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
	public void oneVarNoData() {
		handler.addValueVar( Var.alloc( "x" ));
		handler.build();
		assertNull( query.getValuesVariables());
		assertNull( query.getValuesData() );
	}
	
	@Test
	public void noVarOneData() {
		handler.addDataBlock( Arrays.asList( NodeFactory.createLiteral( "hello")));
		handler.build();
		assertNull( query.getValuesVariables());
		assertNull( query.getValuesData() );
	}
	
	@Test
	public void oneVarOneData() {
		Node n =NodeFactory.createLiteral( "hello");
		handler.addDataBlock( Arrays.asList( n));
		Var v = Var.alloc( "x" );
		handler.addValueVar( v );
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
	public void oneVarTwoBlocks() {
		Node n =NodeFactory.createLiteral( "hello");
		Node n2 = NodeFactory.createLiteral( "there");
		handler.addDataBlock( Arrays.asList( n ));
		handler.addDataBlock( Arrays.asList( n2 ));
		
		Var v = Var.alloc( "x" );
		handler.addValueVar( v );
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
		handler.addDataBlock( Arrays.asList( n));
		Var v = Var.alloc( "x" );
		Var v2 = Var.alloc( "x" );
		handler.addValueVar( v );
		handler.addValueVar( v2 );
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
	public void oneVarTwoData() {
		Node n =NodeFactory.createLiteral( "hello");
		Node n2 = NodeFactory.createLiteral( "there");
		handler.addDataBlock( Arrays.asList( n, n2 ));		
		Var v = Var.alloc( "x" );
		handler.addValueVar( v );
		
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
		handler.addDataBlock( Arrays.asList( n, nn ));
		handler.addDataBlock( Arrays.asList( n2, nn2 ));
		
		Var v = Var.alloc( "x" );
		Var v2 = Var.alloc( "y");
		
		handler.addValueVar( v );
		handler.addValueVar(v2);
	

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
		ln.add(n2);
		
		List<Node> ln2 = new ArrayList<Node>();
		ln2.add(nn);
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
		handler.addDataBlock( Arrays.asList( n, nn ));
		handler.addDataBlock( Arrays.asList( n2, nn2 ));
		
		Var v = Var.alloc( "x" );
		Var v2 = Var.alloc( "y");
		
		handler.addValueVar( v );
		handler.addValueVar(v2);
		
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
		ln.add(n2);
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
		handler.addDataBlock( Arrays.asList( n, nn ));
		handler.addDataBlock( Arrays.asList( n2, nn2 ));
		
		Var v = Var.alloc( "x" );
		Var v2 = Var.alloc( "y");
		
		handler.addValueVar( v );
		handler.addValueVar(v2);
	

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
		ln.add(n2);
		
		List<Node> ln2 = new ArrayList<Node>();
		ln2.add(nn);
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
		handler.addDataBlock( Arrays.asList( n ));
		handler.addDataBlock( Arrays.asList( n2 ));
		
		Var v = Var.alloc( "x" );
		handler.addValueVar( v );
		
		Map<Var,Node> replaceVars = new HashMap<Var,Node>();
		replaceVars.put( v, NodeFactory.createAnon());
		
		handler.setVars(replaceVars);
		handler.build();
		
		assertNull( query.getValuesVariables());
		assertNull( query.getValuesData() );
	}
}
