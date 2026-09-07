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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.GraphTestLib;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.junit.NodeCreateUtils;
import org.apache.jena.rdf.model.helpers.ModelHelper;
import org.apache.jena.test.JenaTestLib;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestModel extends AbstractModelTestBase {

    /**
     * Test cases for RemoveSPO(); each entry is a triple (add, remove, result).
     * <ul>
     * <li>add - the triples to add to the graph to start with
     * <li>remove - the pattern to use in the removal
     * <li>result - the triples that should remain in the graph
     * </ul>
     */
    protected String[][] cases = {{"x R y", "x R y", ""}, {"x R y; a P b", "x R y", "a P b"}, {"x R y; a P b", "?? R y", "a P b"},
        {"x R y; a P b", "x R ??", "a P b"}, {"x R y; a P b", "x ?? y", "a P b"}, {"x R y; a P b", "?? ?? ??", ""},
        {"x R y; a P b; c P d", "?? P ??", "x R y"}, {"x R y; a P b; x S y", "x ?? ??", "a P b"},};

    protected Model copy(final Model m) {
        return createModel().add(m);
    }

    @Test
    public void testAsRDF() {
        testPresentAsRDFNode(GraphTestLib.node("a"), Resource.class);
        testPresentAsRDFNode(GraphTestLib.node("17"), Literal.class);
        testPresentAsRDFNode(GraphTestLib.node("_b"), Resource.class);
    }

    @Test
    public void testContainsResource() {
        ModelHelper.modelAdd(model, "x R y; _a P _b");
        assertTrue(model.containsResource(ModelHelper.resource(model, "x")));
        assertTrue(model.containsResource(ModelHelper.resource(model, "R")));
        assertTrue(model.containsResource(ModelHelper.resource(model, "y")));
        assertTrue(model.containsResource(ModelHelper.resource(model, "_a")));
        assertTrue(model.containsResource(ModelHelper.resource(model, "P")));
        assertTrue(model.containsResource(ModelHelper.resource(model, "_b")));
        assertFalse(model.containsResource(ModelHelper.resource(model, "i")));
        assertFalse(model.containsResource(ModelHelper.resource(model, "_j")));
    }

    @Test
    public void testCreateBlankFromNode() {
        final RDFNode S = model.getRDFNode(NodeCreateUtils.create("_Blank"));
        JenaTestLib.assertInstanceOf(Resource.class, S);
        assertEquals(new AnonId("_Blank"), ((Resource)S).getId());
    }

    @Test
    public void testCreateLiteralFromNode() {
        final RDFNode S = model.getRDFNode(NodeCreateUtils.create("42"));
        JenaTestLib.assertInstanceOf(Literal.class, S);
        assertEquals("42", ((Literal)S).getLexicalForm());
    }

    @Test
    public void testCreateListFromEmptyIterator() {
        RDFList list = model.createList(new ArrayList<RDFNode>().iterator());
        assertEquals(0, list.size());
    }

    @Test
    public void testCreateSingletonListFromIterator() {
        List<RDFNode> expected = new ArrayList<>();
        expected.add(model.createResource());
        RDFList list = model.createList(expected.iterator());
        assertEquals(expected, list.asJavaList());
    }

    @Test
    public void testCreateListFromIterator() {
        List<RDFNode> expected = new ArrayList<>();
        expected.add(model.createResource());
        expected.add(model.createResource());
        expected.add(model.createResource());
        RDFList list = model.createList(expected.iterator());
        assertEquals(expected, list.asJavaList());
    }

    @Test
    public void testCreateResourceFromNode() {
        final RDFNode S = model.getRDFNode(NodeCreateUtils.create("spoo:S"));
        JenaTestLib.assertInstanceOf(Resource.class, S);
        assertEquals("spoo:S", ((Resource)S).getURI());
    }

    /**
     * Test the new version of getProperty(), which delivers null for not-found
     * properties.
     */
    @Test
    public void testGetProperty() {
        ModelHelper.modelAdd(model, "x P a; x P b; x R c");
        final Resource x = ModelHelper.resource(model, "x");
        assertEquals(ModelHelper.resource(model, "c"), x.getProperty(ModelHelper.property(model, "R")).getObject());
        final RDFNode ob = x.getProperty(ModelHelper.property(model, "P")).getObject();
        assertTrue(ob.equals(ModelHelper.resource(model, "a")) || ob.equals(ModelHelper.resource(model, "b")));
        assertNull(x.getProperty(ModelHelper.property(model, "noSuchPropertyHere")));
    }

    @Test
    public void testIsClosedDelegatedToGraph() {
        assertFalse(model.isClosed());
        model.close();
        assertTrue(model.isClosed());
    }

    @Test
    public void testIsEmpty() {
        final Statement S1 = ModelHelper.statement(model, "model rdf:type nonEmpty");
        final Statement S2 = ModelHelper.statement(model, "pinky rdf:type Pig");
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

    @Test
    public void testLiteralNodeAsResourceFails() {
        try {
            model.wrapAsResource(GraphTestLib.node("17"));
            fail("should fail to convert literal to Resource");
        } catch (final UnsupportedOperationException e) {
            JenaTestLib.pass();
        }
    }

    private void testPresentAsRDFNode(final Node n, final Class<? extends RDFNode> nodeClass) {
        final RDFNode r = model.asRDFNode(n);
        assertSame(n, r.asNode());
        JenaTestLib.assertInstanceOf(nodeClass, r);
    }

    @Test
    public void testRemoveAll() {
        testRemoveAll("");
        testRemoveAll("a RR b");
        testRemoveAll("x P y; a Q b; c R 17; _d S 'e'");
        testRemoveAll("subject Predicate 'object'; http://nowhere/x scheme:cunning not:plan");
    }

    protected void testRemoveAll(final String statements) {
        ModelHelper.modelAdd(model, statements);
        assertSame(model, model.removeAll());
        assertEquals(0, model.size(), "model should have size 0 following removeAll(): ");
    }

    /**
     * Test that remove(s, p, o) works, in the presence of inferencing graphs that
     * mean emptyness isn't available. This is why we go round the houses and test
     * that expected ~= initialContent + addedStuff - removed - initialContent.
     */
    @Test
    public void testRemoveSPO() {
        final Model mc = createModel();
        for ( final String[] case1 : cases ) {
            for ( int j = 0 ; j < 3 ; j += 1 ) {
                final Model content = createModel();
                final Model baseContent = copy(content);
                ModelHelper.modelAdd(content, case1[0]);
                final Triple remove = GraphTestLib.triple(case1[1]);
                final Node s = remove.getSubject(), p = remove.getPredicate(), o = remove.getObject();
                final Resource S = (Resource)(s.equals(Node.ANY) ? null : mc.getRDFNode(s));
                final Property P = ((p.equals(Node.ANY) ? null : mc.getRDFNode(p).as(Property.class)));
                final RDFNode O = o.equals(Node.ANY) ? null : mc.getRDFNode(o);
                final Model expected = modelWithStatements(case1[2]);
                content.removeAll(S, P, O);
                final Model finalContent = copy(content).remove(baseContent);
                ModelHelper.assertIsoModels(case1[1], expected, finalContent);
            }
        }
    }

    @Test
    public void testToStatement() {
        final Triple t = GraphTestLib.triple("a P b");
        final Statement s = model.asStatement(t);
        assertEquals(GraphTestLib.node("a"), s.getSubject().asNode());
        assertEquals(GraphTestLib.node("P"), s.getPredicate().asNode());
        assertEquals(GraphTestLib.node("b"), s.getObject().asNode());
    }

    @Test
    public void testTransactions() {
        if ( model.supportsTransactions() )
            model.executeInTxn(() -> {});
    }

    @Test
    public void testURINodeAsResource() {
        final Node n = GraphTestLib.node("a");
        final Resource r = model.wrapAsResource(n);
        assertSame(n, r.asNode());
    }
}
