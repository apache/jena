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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.reasoner.rulesys.Node_RuleVariable;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.util.NodeIsomorphismMap;
import org.apache.jena.vocabulary.RDF;
import org.junit.After;
import org.junit.Test;

public class ConvertersTest {

    @After
    public void cleanup() {
        TypeMapper.reset();
    }

    @Test
    public void checkVarTest() {
        Node n = Converters.checkVar(Node.ANY);
        assertFalse(n instanceof Var);
        n = Converters.checkVar(NodeFactory.createVariable("myVar"));
        assertTrue(n instanceof Var);
    }

    @Test
    public void makeLiteralObjectTest() throws MalformedURLException {
        Node n = Converters.makeLiteral(5);
        assertEquals("5", n.getLiteralLexicalForm());
        assertEquals(Integer.valueOf(5), n.getLiteralValue());
        Node n2 = NodeFactory.createLiteralDT("5", XSDDatatype.XSDint);
        assertEquals(n2, n);

        n = Converters.makeLiteral("Hello");
        assertEquals("Hello", n.getLiteralLexicalForm());
        assertEquals("Hello", n.getLiteralValue());
        assertEquals(XSDDatatype.XSDstring, n.getLiteralDatatype());

        URL url = new URL("http://example.com");
        n = Converters.makeLiteral(url);
        assertEquals("http://example.com", n.getLiteralLexicalForm());
        assertEquals(url, n.getLiteralValue());
        Node n3 = NodeFactory.createLiteralDT("http://example.com", XSDDatatype.XSDanyURI);
        assertEquals(n3, n);

        UUID uuid = UUID.randomUUID();
        try {
            n = Converters.makeLiteral(uuid);
            fail("Should throw exception");
        } catch (IllegalArgumentException expected) {
            // do nothing
        }

        TypeMapper.getInstance().registerDatatype(new UuidDataType());
        try {
            n = Converters.makeLiteral(uuid);
            assertEquals(uuid.toString(), n.getLiteralLexicalForm());
            assertEquals(uuid, n.getLiteralValue());
            String value = String.format("\"%s\"^^java:java.util.UUID", uuid);
            assertEquals(value, n.toString());
        } catch (IllegalArgumentException expected) {
            fail("Unexpected IllegalArgumentException");
        }

    }

    @Test
    public void makeLiteralStringStringTest() {
        Node n = Converters.makeLiteral("5", "http://www.w3.org/2001/XMLSchema#int");
        assertEquals("5", n.getLiteralLexicalForm());
        assertEquals(Integer.valueOf(5), n.getLiteralValue());
        assertEquals("\"5\"^^xsd:int", n.toString());

        n = Converters.makeLiteral("one", "some:stuff");
        assertEquals("one", n.getLiteralLexicalForm());
        try {
            n.getLiteralValue();
            fail("Should have thrown DatatypeFormatException");
        } catch (DatatypeFormatException expected) {
            // do nothing.
        }
        assertEquals("\"one\"^^some:stuff", n.toString());

        try {
            Converters.makeLiteral("NaN", "http://www.w3.org/2001/XMLSchema#int");
            fail("Should have thrown DatatypeFormatException");
        } catch (DatatypeFormatException expected) {
            // do nothing.
        }
    }

    @Test
    public void makeNodeTest() {
        PrefixMapping pMap = PrefixMapping.Factory.create();
        pMap.setNsPrefixes(PrefixMapping.Standard);

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
            Converters.makeNode(uuid, pMap);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // do nothing
        }

        TypeMapper.getInstance().registerDatatype(new UuidDataType());
        n = Converters.makeNode(uuid, pMap);
        Node nu = NodeFactory.createLiteralByValue(uuid);
        assertEquals(nu, n);

        n = Converters.makeNode(NodeFactory.createVariable("foo"), pMap);
        assertTrue(n.isVariable());
        assertEquals("foo", n.getName());
        assertTrue(n instanceof Var);

        n = Converters.makeNode("'text'@en", pMap);
        assertEquals("text", n.getLiteralLexicalForm());
        assertEquals("en", n.getLiteralLanguage());

        Node tripleNode = NodeFactory.createTripleNode(NodeFactory.createURI("a"),
                NodeFactory.createURI("b"), NodeFactory.createURI("c"));
        n = Converters.makeNode(tripleNode, pMap);
        assertEquals(tripleNode, n);

    }

    @Test
    public void makeNodeOrPathTest() {
        PrefixMapping pMap = PrefixMapping.Factory.create();
        pMap.setNsPrefixes(PrefixMapping.Standard);

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
            Converters.makeNodeOrPath(uuid, pMap);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // do nothing
        }

        TypeMapper.getInstance().registerDatatype(new UuidDataType());
        n = Converters.makeNodeOrPath(uuid, pMap);
        Node nu = NodeFactory.createLiteralByValue(uuid);
        assertEquals(nu, n);

        n = Converters.makeNodeOrPath(NodeFactory.createVariable("foo"), pMap);
        assertTrue(n instanceof Var);
        Node node = (Node) n;
        assertTrue(node.isVariable());
        assertEquals("foo", node.getName());

        n = Converters.makeNodeOrPath("'text'@en", pMap);
        assertTrue(n instanceof Node);
        node = (Node) n;
        assertEquals("text", node.getLiteralLexicalForm());
        assertEquals("en", node.getLiteralLanguage());

        n = Converters.makeNodeOrPath("<one>/<two>", pMap);
        assertTrue(n instanceof Path);
        Path pth = (Path) n;
        assertEquals("<one>/<two>", pth.toString());

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
    public void makeValueNodesTest() {
        PrefixMapping pMap = PrefixMapping.Factory.create();
        pMap.setNsPrefixes(PrefixMapping.Standard);

        List<Object> list = new ArrayList<Object>();
        list.add(null);
        list.add(RDF.type);
        Node n2 = NodeFactory.createBlankNode();
        list.add(n2);
        pMap.setNsPrefix("demo", "http://example.com/");
        list.add("demo:type");
        list.add("<one>");
        list.add(Integer.MAX_VALUE);

        Collection<Node> result = Converters.makeValueNodes(list.iterator(), pMap);

        assertTrue(result.contains(null));
        assertTrue(result.contains(RDF.type.asNode()));
        assertTrue(result.contains(n2));
        assertTrue(result.contains(NodeFactory.createURI("http://example.com/type")));
        assertTrue(result.contains(NodeFactory.createURI("one")));

    }

    @Test
    public void quotedTest() {
        assertEquals("'one'", Converters.quoted("one"));
        assertEquals("\"'one'\"", Converters.quoted("'one'"));
        assertEquals("'\"one\"'", Converters.quoted("\"one\""));
        assertEquals("'\"I am the 'one'\"'", Converters.quoted("\"I am the 'one'\""));
        assertEquals("\"'I am the \"one\"'\"", Converters.quoted("'I am the \"one\"'"));
    }

    private Node getSubject(Object o) {
        if (o instanceof Triple) {
            return ((Triple) o).getSubject();
        }
        if (o instanceof TriplePath) {
            return ((TriplePath) o).getSubject();
        }
        throw new IllegalArgumentException("o must be Triple or TriplePath");
    }

    private static List<Object> theTestList = List.of("<one>", "(<two>)", "('an' <embedded> \"array\")", Node.ANY);

    private void assertExpectedTripleList(List<?> lst) {

        assertTriplePath(Triple.create(Node.ANY, RDF.first.asNode(), NodeFactory.createURI("one")), lst.get(0));
        assertTriplePath(Triple.create(getSubject(lst.get(0)), RDF.rest.asNode(), getSubject(lst.get(4))), lst.get(1));
        // sublist 1
        assertTriplePath(Triple.create(Node.ANY, RDF.first.asNode(), NodeFactory.createURI("two")), lst.get(2));
        assertTriplePath(Triple.create(getSubject(lst.get(2)), RDF.rest.asNode(), RDF.nil.asNode()), lst.get(3));
        // end of sublist 1
        assertTriplePath(Triple.create(Node.ANY, RDF.first.asNode(), getSubject(lst.get(2))), lst.get(4));
        assertTriplePath(Triple.create(getSubject(lst.get(4)), RDF.rest.asNode(), getSubject(lst.get(12))), lst.get(5));
        // sublist 2
        assertTriplePath(Triple.create(Node.ANY, RDF.first.asNode(), NodeFactory.createLiteralString("an")), lst.get(6));
        assertTriplePath(Triple.create(getSubject(lst.get(6)), RDF.rest.asNode(), getSubject(lst.get(8))), lst.get(7));
        assertTriplePath(Triple.create(Node.ANY, RDF.first.asNode(), NodeFactory.createURI("embedded")), lst.get(8));
        assertTriplePath(Triple.create(getSubject(lst.get(8)), RDF.rest.asNode(), getSubject(lst.get(10))), lst.get(9));
        assertTriplePath(Triple.create(Node.ANY, RDF.first.asNode(), NodeFactory.createLiteralString("array")), lst.get(10));
        assertTriplePath(Triple.create(getSubject(lst.get(10)), RDF.rest.asNode(), RDF.nil.asNode()), lst.get(11));
        // end of sublist 2
        assertTriplePath(Triple.create(Node.ANY, RDF.first.asNode(), getSubject(lst.get(6))), lst.get(12));
        assertTriplePath(Triple.create(getSubject(lst.get(12)), RDF.rest.asNode(), getSubject(lst.get(14))),
                lst.get(13));
        assertTriplePath(Triple.create(Node.ANY, RDF.first.asNode(), Node.ANY), lst.get(14));
        assertTriplePath(Triple.create(getSubject(lst.get(14)), RDF.rest.asNode(), RDF.nil.asNode()), lst.get(15));
    }

    @Test
    public void makeCollectionTriplesTest() {
        PrefixMapping pMap = PrefixMapping.Factory.create();
        pMap.setNsPrefixes(PrefixMapping.Standard);

        List<Triple> lst = Converters.makeCollectionTriples(theTestList, pMap);
        assertEquals(16, lst.size());
        assertExpectedTripleList(lst);
    }

    private Triple asTriple(Object o) {
        if (o instanceof Triple) {
            return (Triple) o;
        }
        if (o instanceof TriplePath) {
            return ((TriplePath) o).asTriple();
        }
        throw new IllegalArgumentException("o must be Triple or TriplePath");
    }

    private TriplePath asTriplePath(Object o) {
        if (o instanceof Triple) {
            return new TriplePath((Triple) o);
        }
        if (o instanceof TriplePath) {
            return (TriplePath) o;
        }
        throw new IllegalArgumentException("o must be Triple or TriplePath");
    }

    private void assertTriplePath(TriplePath expected, Object actual) {
        String errMsg = String.format("Expected '%s', actual '%s'", expected, actual);
        if (expected.isTriple()) {
            assertTrue(errMsg, expected.asTriple().matches(asTriple(actual)));
        } else {
            assertTrue(errMsg, expected.getSubject().matches(asTriplePath(actual).getSubject()));
            assertTrue(errMsg, expected.getObject().matches(asTriplePath(actual).getObject()));
            assertTrue(errMsg, expected.getPath().equalTo(asTriplePath(actual).getPath(), new NodeIsomorphismMap()));
        }
    }

    private void assertTriplePath(Triple expected, Object actual) {
        String errMsg = String.format("Expected '%s', actual '%s'", expected, actual);
        assertTrue(errMsg, expected.matches(asTriple(actual)));
    }

    @Test
    public void makeCollectionTriplePathsTest() {
        PrefixMapping pMap = PrefixMapping.Factory.create();
        pMap.setNsPrefixes(PrefixMapping.Standard);
        List<TriplePath> lst = Converters.makeCollectionTriplePaths(theTestList, pMap);
        assertEquals(16, lst.size());
        assertExpectedTripleList(lst);
    }

    @Test
    public void makeTriplePathsTest() {
        PrefixMapping pMap = PrefixMapping.Factory.create();
        Path p = new P_Link(NodeFactory.createURI("foo"));

        List<TriplePath> result = Converters.makeTriplePaths("<s>", p, theTestList, pMap);
        assertExpectedTripleList(result);
        assertEquals(17, result.size());
        assertTriplePath(new TriplePath(NodeFactory.createURI("s"), p, result.get(0).getSubject()), result.get(16));
    }

    @Test
    public void makeTriplesTest() {
        PrefixMapping pMap = PrefixMapping.Factory.create();

        List<Triple> result = Converters.makeTriples("<s>", "<p>", theTestList, pMap);
        assertExpectedTripleList(result);
        assertEquals(17, result.size());
        assertTriplePath(
                Triple.create(NodeFactory.createURI("s"), NodeFactory.createURI("p"), result.get(0).getSubject()),
                result.get(16));
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
            super("java:java.util.UUID");
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
