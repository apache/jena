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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.FrontsNode ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.impl.LiteralLabel ;
import org.apache.jena.graph.impl.LiteralLabelFactory ;
import org.apache.jena.reasoner.rulesys.Node_RuleVariable ;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.ExprVar ;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.vocabulary.RDF ;
import org.junit.After;
import org.junit.Test;

public class ConvertersTest {
	
	@After
	public void cleanup() {
		TypeMapper.reset();
	}
	

	@Test 
	public void checkVarTest() {
		Node n = Converters.checkVar( Node.ANY );
		assertFalse( n instanceof Var );
		n = Converters.checkVar(NodeFactory.createVariable( "myVar"));
		assertTrue( n instanceof Var );
	}
	
	@Test
	public void makeLiteralObjectTest() throws MalformedURLException
	{
		Node n = Converters.makeLiteral( 5 );
		assertEquals( "5", n.getLiteralLexicalForm() );
		assertEquals( Integer.valueOf(5), n.getLiteralValue());
		assertEquals( "\"5\"^^http://www.w3.org/2001/XMLSchema#int", n.toString( null, true ));

		n = Converters.makeLiteral( "Hello" );
		assertEquals( "Hello", n.getLiteralLexicalForm() );
		assertEquals( "Hello", n.getLiteralValue());
		assertEquals( "\"Hello\"", n.toString( null, true ));
		
		URL url = new URL( "http://example.com");
		n = Converters.makeLiteral( url);
		assertEquals( "http://example.com", n.getLiteralLexicalForm() );
		assertEquals( url, n.getLiteralValue());
		assertEquals( "\"http://example.com\"^^http://www.w3.org/2001/XMLSchema#anyURI", n.toString(null, true));
		
		UUID uuid = UUID.randomUUID();
		try {
			n = Converters.makeLiteral( uuid );
			fail( "Should throw exception");
		}
		catch (IllegalArgumentException expected) {
			// do nothing
		}
		
		TypeMapper.getInstance().registerDatatype(new UuidDataType());
		try {
			n = Converters.makeLiteral( uuid );
			assertEquals( uuid.toString(), n.getLiteralLexicalForm() );
			assertEquals( uuid, n.getLiteralValue());
			String value = String.format( "\"%s\"^^java:java.util.UUID", uuid);
			assertEquals( value, n.toString(null, true));
		}
		catch (IllegalArgumentException expected) {
			fail( "Unexpected IllegalArgumentException");
		}
		
		
	}
	
	@Test
	public void makeLiteralStringStringTest()
	{
		Node n = Converters.makeLiteral( "5", "http://www.w3.org/2001/XMLSchema#int" );
		assertEquals( "5", n.getLiteralLexicalForm() );
		assertEquals( Integer.valueOf(5), n.getLiteralValue());
		assertEquals( "\"5\"^^http://www.w3.org/2001/XMLSchema#int", n.toString( null, true ));

		n = Converters.makeLiteral( "one", "some:stuff" );
		assertEquals( "one", n.getLiteralLexicalForm() );
		try {
			n.getLiteralValue();
			fail( "Should have thrown DatatypeFormatException");
		}
		catch (DatatypeFormatException expected) {
			// do nothing.
		}
		assertEquals( "\"one\"^^some:stuff", n.toString(null, true));

		try {
			Converters.makeLiteral( "NaN", "http://www.w3.org/2001/XMLSchema#int" );
			fail( "Should have thrown DatatypeFormatException");
		} catch (DatatypeFormatException expected) {
			// do nothing.
		}
	}
	
	@Test
	public void makeNodeTest() {
		PrefixMapping pMap = PrefixMapping.Factory.create();
		pMap.setNsPrefixes(PrefixMapping.Standard );
		
		Node n = Converters.makeNode(null, pMap);
		assertEquals(Node.ANY, n);

		n = Converters.makeNode(RDF.type, pMap);
		assertEquals(RDF.type.asNode(), n);

		Node n2 = NodeFactory.createBlankNode();
		n = Converters.makeNode(n2, pMap);
		assertEquals(n2, n);

		pMap.setNsPrefix("demo", "http://example.com/");
		n = Converters.makeNode("demo:type", pMap);
		assertEquals(NodeFactory.createURI("http://example.com/type"), n);

		n = Converters.makeNode("<one>", pMap);
		assertEquals(NodeFactory.createURI("one"), n);

		UUID uuid = UUID.randomUUID();
		try {
			Converters.makeNode( uuid, pMap);
			fail( "Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException expected) {
			// do nothing
		}
		
		TypeMapper.getInstance().registerDatatype(new UuidDataType());		
		n = Converters.makeNode( uuid, pMap );
		LiteralLabel ll = LiteralLabelFactory.createTypedLiteral(uuid);
		assertEquals(NodeFactory.createLiteral(ll), n);
		
		
		n = Converters.makeNode( NodeFactory.createVariable("foo"), pMap);
		assertTrue( n.isVariable());
		assertEquals( "foo", n.getName());
		assertTrue( n instanceof Var );

		n = Converters.makeNode( "'text'@en", pMap);
		assertEquals( "text", n.getLiteralLexicalForm());
		assertEquals( "en", n.getLiteralLanguage());
	}

	@Test
	public void makeNodeOrPathTest() {
		PrefixMapping pMap = PrefixMapping.Factory.create();
		pMap.setNsPrefixes(PrefixMapping.Standard );
		
		Object n = Converters.makeNodeOrPath(null, pMap);
		assertEquals(Node.ANY, n);

		n = Converters.makeNodeOrPath(RDF.type, pMap);
		assertEquals(RDF.type.asNode(), n);

		Node n2 = NodeFactory.createBlankNode();
		n = Converters.makeNodeOrPath(n2, pMap);
		assertEquals(n2, n);

		pMap.setNsPrefix("demo", "http://example.com/");
		n = Converters.makeNodeOrPath("demo:type", pMap);
		assertEquals(NodeFactory.createURI("http://example.com/type"), n);

		n = Converters.makeNodeOrPath("<one>", pMap);
		assertEquals(NodeFactory.createURI("one"), n);

		UUID uuid = UUID.randomUUID();
		try {
			Converters.makeNodeOrPath( uuid, pMap);
			fail( "Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException expected) {
			// do nothing
		}

		TypeMapper.getInstance().registerDatatype(new UuidDataType());		
		n = Converters.makeNodeOrPath( uuid, pMap);
		LiteralLabel ll = LiteralLabelFactory.createTypedLiteral(uuid);
		assertEquals(NodeFactory.createLiteral(ll), n);
		
		n = Converters.makeNodeOrPath( NodeFactory.createVariable("foo"), pMap);
		assertTrue( n instanceof Var );
		Node node = (Node)n;
		assertTrue( node.isVariable());
		assertEquals( "foo", node.getName());

		n = Converters.makeNodeOrPath( "'text'@en", pMap);
		assertTrue( n instanceof Node );
		node = (Node)n;
		assertEquals( "text", node.getLiteralLexicalForm());
		assertEquals( "en", node.getLiteralLanguage());
		
		n = Converters.makeNodeOrPath( "<one>/<two>", pMap);
		assertTrue( n instanceof Path );
		Path pth = (Path)n;
		assertEquals( "<one>/<two>", pth.toString() );
		
	}
	
	@Test
	public void makeVarTest() {
		Var v = Converters.makeVar(null);
		assertEquals(Var.ANON, v);

		v = Converters.makeVar("a");
		assertEquals(Var.alloc("a"), v);

		v = Converters.makeVar("?a");
		assertEquals(Var.alloc("a"), v);

		Node n = NodeFactory.createVariable("foo");
		v = Converters.makeVar(n);
		assertEquals(Var.alloc("foo"), v);

		NodeFront nf = new NodeFront(n);
		v = Converters.makeVar(nf);
		assertEquals(Var.alloc("foo"), v);

		v = Converters.makeVar(Node_RuleVariable.WILD);
		assertNull(v);

		ExprVar ev = new ExprVar("bar");
		v = Converters.makeVar(ev);
		assertEquals(Var.alloc("bar"), v);

		ev = new ExprVar(n);
		v = Converters.makeVar(ev);
		assertEquals(Var.alloc("foo"), v);

		ev = new ExprVar(Var.ANON);
		v = Converters.makeVar(ev);
		assertEquals(Var.ANON, v);

	}

	@Test
	public void makeValueNodesTest()
	{
		PrefixMapping pMap = PrefixMapping.Factory.create();
		pMap.setNsPrefixes(PrefixMapping.Standard );

		List<Object> list = new ArrayList<Object>();
		list.add( null);
		list.add( RDF.type );
		Node n2 = NodeFactory.createBlankNode();
		list.add( n2 );
		pMap.setNsPrefix("demo", "http://example.com/");
		list.add( "demo:type" );
		list.add( "<one>" );
		list.add( Integer.MAX_VALUE );
		
		Collection<Node> result = Converters.makeValueNodes(list.iterator(), pMap);
		
		assertTrue( result.contains( null ));
		assertTrue( result.contains( RDF.type.asNode()));
		assertTrue( result.contains( n2 ));
		assertTrue( result.contains(NodeFactory.createURI("http://example.com/type") ));
		assertTrue( result.contains(NodeFactory.createURI("one")));

	}
	
	@Test
	public void quotedTest() {
		assertEquals( "'one'", Converters.quoted( "one" ));
		assertEquals( "\"'one'\"", Converters.quoted( "'one'" ));
		assertEquals( "'\"one\"'", Converters.quoted( "\"one\"" ));
		assertEquals( "'\"I am the 'one'\"'", Converters.quoted( "\"I am the 'one'\"" ));
		assertEquals( "\"'I am the \"one\"'\"", Converters.quoted( "'I am the \"one\"'" ));
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
	
	private class UuidDataType extends BaseDatatype {
		
		public UuidDataType() {
			super( "java:java.util.UUID");
		}
	
		@Override
        public Class<?> getJavaClass() {
            return UUID.class;
        }

        @Override
        public Object parse(String lexicalForm) throws DatatypeFormatException {
            try {
                return UUID.fromString(lexicalForm);
            } catch (Throwable th) {
                throw new DatatypeFormatException();
            }
        }
    };
	
}
