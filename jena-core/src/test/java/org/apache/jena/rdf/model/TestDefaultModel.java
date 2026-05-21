/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.rdf.model;

import junit.framework.TestSuite;
import org.apache.jena.graph.GraphTestLib;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.junit.NodeCreateUtils;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.test.JenaTestBase;
import org.apache.jena.test.JenaTestLib;

public class TestDefaultModel extends JenaTestBase {

    public TestDefaultModel(String name) {
        super(name);
    }

    static public TestSuite suite() {
        TestSuite ts = new TestSuite();
        ts.addTestSuite(TestDefaultModel.class);
        return ts;
    }

    public Model newModel() {
        return ModelFactory.createDefaultModel();
    }

    private Model model;

    @Override
    public void setUp() {
        model = newModel();
    }

    @Override
    public void tearDown() {
        model.close();
    }

    public void testTransactions() {
        if ( model.supportsTransactions() )
            model.executeInTxn(() -> {});
    }

    public void testCreateResourceFromNode() {
        RDFNode S = model.getRDFNode(NodeCreateUtils.create("spoo:S"));
        JenaTestLib.assertInstanceOf(Resource.class, S);
        assertEquals("spoo:S", ((Resource)S).getURI());
    }

    public void testCreateLiteralFromNode() {
        RDFNode S = model.getRDFNode(NodeCreateUtils.create("42"));
        JenaTestLib.assertInstanceOf(Literal.class, S);
        assertEquals("42", ((Literal)S).getLexicalForm());
    }

    public void testCreateBlankFromNode() {
        RDFNode S = model.getRDFNode(NodeCreateUtils.create("_Blank"));
        JenaTestLib.assertInstanceOf(Resource.class, S);
        assertEquals(new AnonId("_Blank"), ((Resource)S).getId());
    }

    public void testIsEmpty() {
        Statement S1 = ModelTestLib.statement(model, "model rdf:type nonEmpty");
        Statement S2 = ModelTestLib.statement(model, "pinky rdf:type Pig");
        assertTrue(model.isEmpty());
        model.add(S1);
        assertFalse(model.isEmpty());
        model.add(S2);
        assertFalse(model.isEmpty());
        model.remove(S1);
        assertFalse(model.isEmpty());
        model.remove(S2);
        assertTrue(model.isEmpty());
    }

    public void testContainsResource() {
        ModelTestLib.modelAdd(model, "x R y; _a P _b");
        assertTrue(model.containsResource(ModelTestLib.resource(model, "x")));
        assertTrue(model.containsResource(ModelTestLib.resource(model, "R")));
        assertTrue(model.containsResource(ModelTestLib.resource(model, "y")));
        assertTrue(model.containsResource(ModelTestLib.resource(model, "_a")));
        assertTrue(model.containsResource(ModelTestLib.resource(model, "P")));
        assertTrue(model.containsResource(ModelTestLib.resource(model, "_b")));
        assertFalse(model.containsResource(ModelTestLib.resource(model, "i")));
        assertFalse(model.containsResource(ModelTestLib.resource(model, "_j")));
    }

    /**
     * Test the new version of getProperty(), which delivers null for not-found
     * properties.
     */
    public void testGetProperty() {
        ModelTestLib.modelAdd(model, "x P a; x P b; x R c");
        Resource x = ModelTestLib.resource(model, "x");
        assertEquals(ModelTestLib.resource(model, "c"), x.getProperty(ModelTestLib.property(model, "R")).getObject());
        RDFNode ob = x.getProperty(ModelTestLib.property(model, "P")).getObject();
        assertTrue(ob.equals(ModelTestLib.resource(model, "a")) || ob.equals(ModelTestLib.resource(model, "b")));
        assertNull(x.getProperty(ModelTestLib.property(model, "noSuchPropertyHere")));
    }

    /**
     * Tests {@link Resource#getProperty(Property, String)} and
     * {@link Resource#getRequiredProperty(Property, String)}.
     */
    public void testGetPropertyWithLanguage() {
        model.add(ModelTestLib.resource(model, "x"), ModelTestLib.property(model, "P"), "a", "pt");
        model.add(ModelTestLib.resource(model, "x"), ModelTestLib.property(model, "P"), "b", "en");
        model.add(ModelTestLib.resource(model, "x"), ModelTestLib.property(model, "P"), "c", "es");
        model.add(ModelTestLib.resource(model, "x"), ModelTestLib.property(model, "R"), "d", "fr");
        model.add(ModelTestLib.resource(model, "x"), ModelTestLib.property(model, "R"), "e", "de");

        {// Tests {@link Resource#getProperty(Property, String)}
            final Resource x = ModelTestLib.resource(model, "x");
            assertEquals("a", x.getProperty(ModelTestLib.property(model, "P"), "pt").getString());
            assertEquals("b", x.getProperty(ModelTestLib.property(model, "P"), "en").getString());
            assertNull(x.getProperty(ModelTestLib.property(model, "P"), "ja"));
            final Literal l = x.getProperty(ModelTestLib.property(model, "R")).getLiteral();
            assertTrue("d".equals(l.getString()) || "e".equals(l.getString()));
        }

        {// Tests {@link Resource#getRequiredProperty(Property, String)}
            final Resource x = ModelTestLib.resource(model, "x");
            assertEquals("a", x.getRequiredProperty(ModelTestLib.property(model, "P"), "pt").getString());
            assertEquals("b", x.getRequiredProperty(ModelTestLib.property(model, "P"), "en").getString());
            try {
                x.getRequiredProperty(ModelTestLib.property(model, "P"), "ja");
                fail("Must thrown PropertyNotFoundException.");
            } catch (PropertyNotFoundException e) {}
            final Literal l = x.getRequiredProperty(ModelTestLib.property(model, "R")).getLiteral();
            assertTrue("d".equals(l.getString()) || "e".equals(l.getString()));
            assertTrue("de".equals(l.getLanguage()) || "fr".equals(l.getLanguage()));
        }
    }

    public void testToStatement() {
        Triple t = GraphTestLib.triple("a P b");
        Statement s = model.asStatement(t);
        assertEquals(GraphTestLib.node("a"), s.getSubject().asNode());
        assertEquals(GraphTestLib.node("P"), s.getPredicate().asNode());
        assertEquals(GraphTestLib.node("b"), s.getObject().asNode());
    }

    public void testAsRDF() {
        testPresentAsRDFNode(GraphTestLib.node("a"), Resource.class);
        testPresentAsRDFNode(GraphTestLib.node("17"), Literal.class);
        testPresentAsRDFNode(GraphTestLib.node("_b"), Resource.class);
    }

    private void testPresentAsRDFNode(Node n, Class<? extends RDFNode> nodeClass) {
        RDFNode r = model.asRDFNode(n);
        assertSame(n, r.asNode());
        JenaTestLib.assertInstanceOf(nodeClass, r);
    }

    public void testURINodeAsResource() {
        Node n = GraphTestLib.node("a");
        Resource r = model.wrapAsResource(n);
        assertSame(n, r.asNode());
    }

    public void testLiteralNodeAsResourceFails() {
        try {
            model.wrapAsResource(GraphTestLib.node("17"));
            fail("should fail to convert literal to Resource");
        } catch (UnsupportedOperationException e) {
            JenaTestLib.pass();
        }
    }

    public void testRemoveAll() {
        testRemoveAll("");
        testRemoveAll("a RR b");
        testRemoveAll("x P y; a Q b; c R 17; _d S 'e'");
        testRemoveAll("subject Predicate 'object'; http://nowhere/x scheme:cunning not:plan");
    }

    protected void testRemoveAll(String statements) {
        ModelTestLib.modelAdd(model, statements);
        assertSame(model, model.removeAll());
        assertEquals("model should have size 0 following removeAll(): ", 0, model.size());
    }

    /**
     * Test cases for RemoveSPO(); each entry is a triple (add, remove, result).
     * <ul>
     * <li>add - the triples to add to the graph to start with
     * <li>remove - the pattern to use in the removal
     * <li>result - the triples that should remain in the graph
     * </ul>
     */
    //@formatter:off
    protected String[][] cases = {
        { "x R y", "x R y", "" },
        { "x R y; a P b", "x R y", "a P b" },
        { "x R y; a P b", "?? R y", "a P b" },
        { "x R y; a P b", "x R ??", "a P b" },
        { "x R y; a P b", "x ?? y", "a P b" },
        { "x R y; a P b", "?? ?? ??", "" },
        { "x R y; a P b; c P d", "?? P ??", "x R y" },
        { "x R y; a P b; x S y", "x ?? ??", "a P b" },
    };
    //@formatter:on

    /**
     * Test that remove(s, p, o) works, in the presence of inferencing graphs that
     * mean emptiness isn't available. This is why we go round the houses and test
     * that expected ~= initialContent + addedStuff - removed - initialContent.
     */
    public void testRemoveSPO() {
        ModelCom mc = (ModelCom)ModelFactory.createDefaultModel();
        for ( String[] aCase : cases ) {
            for ( int j = 0; j < 3; j += 1 ) {
                Model content = newModel();
                Model baseContent = copy(content);
                ModelTestLib.modelAdd(content, aCase[0]);
                Triple remove = GraphTestLib.triple(aCase[1]);
                Node s = remove.getSubject(), p = remove.getPredicate(), o = remove.getObject();
                Resource S = (Resource)(s.equals(Node.ANY) ? null : mc.getRDFNode(s));
                Property P = ((p.equals(Node.ANY) ? null : mc.getRDFNode(p).as(Property.class)));
                RDFNode O = o.equals(Node.ANY) ? null : mc.getRDFNode(o);
                Model expected = ModelTestLib.modelWithStatements(aCase[2]);
                content.removeAll(S, P, O);
                Model finalContent = copy(content).remove(baseContent);
                ModelTestLib.assertIsoModels(aCase[1], expected, finalContent);
            }
        }
    }

    public void testIsClosedDelegatedToGraph() {
        Model m = newModel();
        assertFalse(m.isClosed());
        m.close();
        assertTrue(m.isClosed());
    }

    protected Model copy(Model m) {
        return ModelFactory.createDefaultModel().add(m);
    }
}
