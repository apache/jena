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
package org.apache.jena.arq.querybuilder.updatebuilder;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.arq.querybuilder.handlers.WhereHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.P_Link;
import org.junit.Test;

public class WhereQuadHolderTest {
	
	private WhereQuadHolder holder;
	
	
	@Test
	public void anonymousTest()
	{
		holder = new WhereQuadHolder( new PrefixHandler() );
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createURI( "p" );
		Node o = NodeFactory.createURI( "o" );
		tLst.add( new Triple( s, p, o ) );
		holder.addWhere( new TriplePath( s, new P_Link(p), o ));
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s2, p2, o2 ) );
		holder.addWhere( new TriplePath( s2, new P_Link(p2), o2 ));

		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(1)), lst.get(1));
	}

	@Test
	public void anonymousTest_Var()
	{
		holder = new WhereQuadHolder( new PrefixHandler() );
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createVariable( "p" );
		Node o = NodeFactory.createURI( "o" );
		holder.addWhere( new TriplePath( s, new P_Link(p), o ));
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s, p2, o ) );
		tLst.add( new Triple( s2, p2, o2 ) );
		holder.addWhere( new TriplePath( s2, new P_Link(p), o2 ));
		
		Map<Var,Node> map = new HashMap<>();
		map.put( Var.alloc(p), p2);
		holder.setValues( map );
		
		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(1)), lst.get(1));
	}
	
	@Test
	public void namedTest()
	{
		holder = new WhereQuadHolder( new PrefixHandler() );
		
		Node g = NodeFactory.createURI( "g" );
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createURI( "p" );
		Node o = NodeFactory.createURI( "o" );
		tLst.add( new Triple( s, p, o ) );
		WhereHandler whereHandler = new WhereHandler();
		whereHandler.addWhere( new TriplePath(s, new P_Link(p), o )); 
		holder.addGraph( g, whereHandler );
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s2, p2, o2 ) );
		whereHandler = new WhereHandler();
		whereHandler.addWhere( new TriplePath(s2, new P_Link(p2), o2 )); 
		holder.addGraph( g, whereHandler );

		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( g, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( g, tLst.get(1)), lst.get(1));	
	}

	@Test
	public void namedTest_Var()
	{
		holder = new WhereQuadHolder( new PrefixHandler() );
		
		Node g = NodeFactory.createURI( "g" );
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createVariable( "p" );
		Node o = NodeFactory.createURI( "o" );
		WhereHandler whereHandler = new WhereHandler();
		whereHandler.addWhere( new TriplePath(s, new P_Link(p), o )); 
		holder.addGraph( g, whereHandler );
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		whereHandler = new WhereHandler();
		whereHandler.addWhere( new TriplePath(s2, new P_Link(p2), o2 )); 
		holder.addGraph( g, whereHandler );
		
		tLst.add( new Triple( s, p2, o ) );
		tLst.add( new Triple( s2, p2, o2 ) );
		
		Map<Var,Node> map = new HashMap<>();
		map.put( Var.alloc(p), p2);
		holder.setValues( map );
		
		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( g, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( g, tLst.get(1)), lst.get(1));	
	}

	@Test
	public void mixedTest()
	{
		holder = new WhereQuadHolder( new PrefixHandler() );
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createURI( "p" );
		Node o = NodeFactory.createURI( "o" );
		tLst.add( new Triple( s, p, o ) );
		holder.addWhere( new TriplePath(s, new P_Link(p), o ) );
		
		Node g = NodeFactory.createURI( "g" );
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s2, p2, o2 ) );
		WhereHandler whereHandler = new WhereHandler();
		whereHandler.addWhere( new TriplePath(s2, new P_Link(p2), o2 )); 
		holder.addGraph( g, whereHandler );

		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( g, tLst.get(1)), lst.get(1));	
	}
	
	@Test
	public void mixedTest_Var()
	{
		holder = new WhereQuadHolder( new PrefixHandler() );
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createVariable( "p" );
		Node o = NodeFactory.createURI( "o" );
		holder.addWhere( new TriplePath(s, new P_Link(p), o ) );
		
		Node g = NodeFactory.createURI( "g" );
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s, p2, o ) );
		tLst.add( new Triple( s2, p2, o2 ) );
		WhereHandler whereHandler = new WhereHandler();
		whereHandler.addWhere( new TriplePath(s2, new P_Link(p2), o2 )); 
		holder.addGraph( g, whereHandler );

		Map<Var,Node> map = new HashMap<>();
		map.put( Var.alloc(p), p2);
		holder.setValues( map );
		
		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( g, tLst.get(1)), lst.get(1));	
	}
}
