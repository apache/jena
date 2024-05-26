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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.arq.querybuilder.handlers.HandlerBlock;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.RDF;
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

        public TestBuilder() {
            super();
            handlerBlock = new HandlerBlock(query);

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

        try {
            n = builder.makeNode(builder);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // do nothing
        }

        n = builder.makeNode(Integer.valueOf(5));
        assertTrue(n.isLiteral());
        assertEquals(XSDDatatype.XSDint, n.getLiteralDatatype());
        assertEquals("5", n.getLiteralLexicalForm());

        n = builder.makeNode(NodeFactory.createVariable("foo"));
        assertTrue(n.isVariable());
        assertEquals("foo", n.getName());
        assertTrue(n instanceof Var);

        n = builder.makeNode("'text'@en");
        assertTrue(n.isLiteral());
        assertEquals("text", n.getLiteralLexicalForm());
        assertEquals("en", n.getLiteralLanguage());
    }

    @Test
    public void testMakeValueNodes() {
        List<Object> list = new ArrayList<Object>();
        list.add(null);
        list.add(RDF.type);
        Node n2 = NodeFactory.createBlankNode();
        list.add(n2);
        builder.addPrefix("demo", "http://example.com/");
        list.add("demo:type");
        list.add("<one>");
        list.add(Integer.valueOf(5));

        Collection<Node> result = builder.makeValueNodes(list.iterator());

        assertEquals(6, result.size());
        assertTrue(result.contains(null));
        assertTrue(result.contains(RDF.type.asNode()));
        assertTrue(result.contains(n2));
        assertTrue(result.contains(NodeFactory.createURI("http://example.com/type")));
        assertTrue(result.contains(NodeFactory.createURI("one")));

        Node n = NodeFactory.createLiteral("5", XSDDatatype.XSDint);
        assertTrue(result.contains(n));

    }
    
    private void assertTripleMatch(Triple expected, Triple actual) {
        if (!expected.matches(actual)) {
            fail("expected: "+expected+" actual: "+actual);
        }
    }
    
    private void assertTripleMatch(Triple expected, TriplePath actual) {
        assertTripleMatch(expected, actual.asTriple());
    }
    
    @Test
    public void testMakeTriplePaths() {
        List<Object> list = new ArrayList<Object>();
        list.add(RDF.type);
        builder.addPrefix("demo", "http://example.com/");
        list.add("demo:type");
        list.add("<one>");
        list.add(Integer.valueOf(5));
        
        Triple[] expected = {
            Triple.create(Node.ANY, RDF.first.asNode(), RDF.type.asNode()),
            Triple.create(Node.ANY, RDF.rest.asNode(), Node.ANY),
            Triple.create(Node.ANY, RDF.first.asNode(), NodeFactory.createURI("http://example.com/type")),
            Triple.create(Node.ANY, RDF.rest.asNode(), Node.ANY),
            Triple.create(Node.ANY, RDF.first.asNode(), NodeFactory.createURI("one")), 
            Triple.create(Node.ANY, RDF.rest.asNode(), Node.ANY), 
            Triple.create(Node.ANY, RDF.first.asNode(), NodeFactory.createLiteral("5", XSDDatatype.XSDint)),
            Triple.create(Node.ANY, RDF.rest.asNode(), RDF.nil.asNode()),
            Triple.create(Var.alloc("s"), Var.alloc("p"), Node.ANY),
        };

        List<TriplePath> result = builder.makeTriplePaths("?s", "?p", list);

        assertEquals(expected.length, result.size());
        for (int i=0;i<expected.length;i++) {
            assertTripleMatch( expected[i], result.get(i));
        }
    }

}
