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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.vocabulary.DC_11;
import org.junit.Test;

public class UpdateBuilderTest {

    private Node g = NodeFactory.createURI("http://example.com/graph");
    private Node s = NodeFactory.createURI("http://example.com/subject");
    private Node p = NodeFactory.createURI("http://example.com/predicate");
    private Node o = NodeFactory.createURI("http://example.com/object");

    @Test
    public void testConstructorWithPrefixMappingAndWhereClause() {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("ex", "http://example.com/");
        UpdateBuilder builder = new UpdateBuilder(model);
        builder.addInsert(s, "ex:predicate", "?o");
        builder.addWhere( s, "ex:predicat2", "?o" );
        builder.build();
    }

    @Test
    public void testInsert_SPO() {
        UpdateBuilder builder = new UpdateBuilder();
        builder.addInsert(s, p, o);
        Update update = builder.build();
        assertTrue(update instanceof UpdateDataInsert);
        UpdateDataInsert udi = (UpdateDataInsert) update;
        List<Quad> quads = udi.getQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(Quad.defaultGraphNodeGenerated, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());
    }

    @Test
    public void testInsert_Triple() {
        Triple t = Triple.create(s, p, o);
        UpdateBuilder builder = new UpdateBuilder();
        builder.addInsert(t);
        Update update = builder.build();
        assertTrue(update instanceof UpdateDataInsert);
        UpdateDataInsert udi = (UpdateDataInsert) update;
        List<Quad> quads = udi.getQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(Quad.defaultGraphNodeGenerated, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());
    }

    @Test
    public void testInsert_NodeTriple() {
        Triple t = Triple.create(s, p, o);
        UpdateBuilder builder = new UpdateBuilder();
        builder.addInsert(g, t);
        Update update = builder.build();
        assertTrue(update instanceof UpdateDataInsert);
        UpdateDataInsert udi = (UpdateDataInsert) update;
        List<Quad> quads = udi.getQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(g, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());
    }

    @Test
    public void testInsert_GSPO() {

        UpdateBuilder builder = new UpdateBuilder();
        builder.addInsert(g, s, p, o);
        Update update = builder.build();
        assertTrue(update instanceof UpdateDataInsert);
        UpdateDataInsert udi = (UpdateDataInsert) update;
        List<Quad> quads = udi.getQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(g, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());
    }

    @Test
    public void testInsert_Quad() {
        UpdateBuilder builder = new UpdateBuilder();
        builder.addInsert(new Quad(g, s, p, o));
        Update update = builder.build();
        assertTrue(update instanceof UpdateDataInsert);
        UpdateDataInsert udi = (UpdateDataInsert) update;
        List<Quad> quads = udi.getQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(g, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());
    }

    @Test
    public void testInsert_QuadCollection() {
        UpdateBuilder builder = new UpdateBuilder();
        Collection<Quad> quads = new ArrayList<Quad>();

        quads.add(new Quad(g, s, p, o));

        Node g2 = NodeFactory.createURI("http://example.com/graph2");
        Node s2 = NodeFactory.createURI("http://example.com/subject2");
        Node p2 = NodeFactory.createURI("http://example.com/predicate2");
        Node o2 = NodeFactory.createURI("http://example.com/object2");

        quads.add(new Quad(g2, s2, p2, o2));

        builder.addInsertQuads(quads);
        Update update = builder.build();
        assertTrue(update instanceof UpdateDataInsert);
        UpdateDataInsert udi = (UpdateDataInsert) update;
        List<Quad> quads2 = udi.getQuads();
        assertEquals(2, quads2.size());
        Quad q = quads2.get(0);
        assertEquals(g, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());

        q = quads2.get(1);
        assertEquals(g2, q.getGraph());
        assertEquals(s2, q.getSubject());
        assertEquals(p2, q.getPredicate());
        assertEquals(o2, q.getObject());
    }

    @Test
    public void testInsertValueReplacement() {
        Var v = Var.alloc("v");
        UpdateBuilder builder = new UpdateBuilder();
        builder.addInsert(v, p, o);
        builder.setVar(v, s);
        Update update = builder.build();
        assertTrue(update instanceof UpdateDataInsert);
        UpdateDataInsert udi = (UpdateDataInsert) update;
        List<Quad> quads = udi.getQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(Quad.defaultGraphNodeGenerated, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());
    }

    @Test
    public void testDelete_SPO() {
        UpdateBuilder builder = new UpdateBuilder();
        builder.addDelete(s, p, o);
        Update update = builder.build();
        assertTrue(update instanceof UpdateDataDelete);
        UpdateDataDelete udd = (UpdateDataDelete) update;
        List<Quad> quads = udd.getQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(Quad.defaultGraphNodeGenerated, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());
    }

    @Test
    public void testDelete_Triple() {
        Triple t = Triple.create(s, p, o);
        UpdateBuilder builder = new UpdateBuilder();
        builder.addDelete(t);
        Update update = builder.build();
        assertTrue(update instanceof UpdateDataDelete);
        UpdateDataDelete udd = (UpdateDataDelete) update;
        List<Quad> quads = udd.getQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(Quad.defaultGraphNodeGenerated, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());
    }

    @Test
    public void testDelete_NodeTriple() {
        Triple t = Triple.create(s, p, o);
        UpdateBuilder builder = new UpdateBuilder();
        builder.addDelete(g, t);
        Update update = builder.build();
        assertTrue(update instanceof UpdateDataDelete);
        UpdateDataDelete udd = (UpdateDataDelete) update;
        List<Quad> quads = udd.getQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(g, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());
    }

    @Test
    public void testDelete_GSPO() {

        UpdateBuilder builder = new UpdateBuilder();
        builder.addDelete(g, s, p, o);
        Update update = builder.build();
        assertTrue(update instanceof UpdateDataDelete);
        UpdateDataDelete udd = (UpdateDataDelete) update;
        List<Quad> quads = udd.getQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(g, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());
    }

    @Test
    public void testDelete_Quad() {

        UpdateBuilder builder = new UpdateBuilder();
        builder.addDelete(new Quad(g, s, p, o));
        Update update = builder.build();
        assertTrue(update instanceof UpdateDataDelete);
        UpdateDataDelete udd = (UpdateDataDelete) update;
        List<Quad> quads = udd.getQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(g, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());
    }

    @Test
    public void testDelete_QuadCollection() {
        UpdateBuilder builder = new UpdateBuilder();
        Collection<Quad> quads = new ArrayList<Quad>();

        quads.add(new Quad(g, s, p, o));

        Node g2 = NodeFactory.createURI("http://example.com/graph2");
        Node s2 = NodeFactory.createURI("http://example.com/subject2");
        Node p2 = NodeFactory.createURI("http://example.com/predicate2");
        Node o2 = NodeFactory.createURI("http://example.com/object2");

        quads.add(new Quad(g2, s2, p2, o2));

        builder.addDeleteQuads(quads);
        Update update = builder.build();
        assertTrue(update instanceof UpdateDataDelete);
        UpdateDataDelete udd = (UpdateDataDelete) update;
        List<Quad> quads2 = udd.getQuads();
        assertEquals(2, quads2.size());
        Quad q = quads2.get(0);
        assertEquals(g, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());

        q = quads2.get(1);
        assertEquals(g2, q.getGraph());
        assertEquals(s2, q.getSubject());
        assertEquals(p2, q.getPredicate());
        assertEquals(o2, q.getObject());
    }

    @Test
    public void testDeleteValueReplacement() {
        Var v = Var.alloc("v");
        UpdateBuilder builder = new UpdateBuilder();
        builder.addDelete(v, p, o);
        builder.setVar(v, s);
        Update update = builder.build();
        assertTrue(update instanceof UpdateDataDelete);
        UpdateDataDelete udd = (UpdateDataDelete) update;
        List<Quad> quads = udd.getQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(Quad.defaultGraphNodeGenerated, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());
    }

    @Test
    public void testInsertAndDelete() {
        UpdateBuilder builder = new UpdateBuilder();
        builder.addInsert(new Quad(g, s, p, o));
        builder.addDelete(Triple.create(s, p, o));
        builder.addWhere(null, p, "foo");
        Update update = builder.build();
        assertTrue(update instanceof UpdateModify);
        UpdateModify um = (UpdateModify) update;
        List<Quad> quads = um.getInsertQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(g, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());

        quads = um.getDeleteQuads();
        assertEquals(1, quads.size());
        q = quads.get(0);
        assertEquals(Quad.defaultGraphNodeGenerated, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());

        Element e = um.getWherePattern();
        assertTrue(e instanceof ElementGroup);
        ElementGroup eg = (ElementGroup) e;
        assertEquals(1, eg.getElements().size());
        ElementPathBlock epb = (ElementPathBlock) eg.getElements().get(0);
        Triple t = epb.getPattern().get(0).asTriple();
        assertEquals(Node.ANY, t.getSubject());
        assertEquals(p, t.getPredicate());
        assertEquals(builder.makeNode("foo"), t.getObject());
    }

    @Test
    public void testInsertAndDeleteWithVarReplacement() {
        UpdateBuilder builder = new UpdateBuilder();
        Var v = Var.alloc("v");

        builder.addInsert(new Quad(g, s, v, o));
        builder.addDelete(Triple.create(s, v, o));
        builder.addWhere(null, v, "foo");
        builder.setVar(v, p);
        Update update = builder.build();
        assertTrue(update instanceof UpdateModify);
        UpdateModify um = (UpdateModify) update;
        List<Quad> quads = um.getInsertQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(g, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());

        quads = um.getDeleteQuads();
        assertEquals(1, quads.size());
        q = quads.get(0);
        assertEquals(Quad.defaultGraphNodeGenerated, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());

        Element e = um.getWherePattern();
        assertTrue(e instanceof ElementGroup);
        ElementGroup eg = (ElementGroup) e;
        assertEquals(1, eg.getElements().size());
        ElementPathBlock epb = (ElementPathBlock) eg.getElements().get(0);
        Triple t = epb.getPattern().get(0).asTriple();
        assertEquals(Node.ANY, t.getSubject());
        assertEquals(p, t.getPredicate());
        assertEquals(builder.makeNode("foo"), t.getObject());
    }

    @Test
    public void testInsertAndDeleteWithVariableNodeReplacement() {
        UpdateBuilder builder = new UpdateBuilder();
        Node v = NodeFactory.createVariable("v");

        builder.addInsert(new Quad(g, s, v, o));
        builder.addDelete(Triple.create(s, v, o));
        builder.addWhere(null, v, "foo");
        builder.setVar(v, p);
        Update update = builder.build();
        assertTrue(update instanceof UpdateModify);
        UpdateModify um = (UpdateModify) update;
        List<Quad> quads = um.getInsertQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(g, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());

        quads = um.getDeleteQuads();
        assertEquals(1, quads.size());
        q = quads.get(0);
        assertEquals(Quad.defaultGraphNodeGenerated, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(p, q.getPredicate());
        assertEquals(o, q.getObject());

        Element e = um.getWherePattern();
        assertTrue(e instanceof ElementGroup);
        ElementGroup eg = (ElementGroup) e;
        assertEquals(1, eg.getElements().size());
        ElementPathBlock epb = (ElementPathBlock) eg.getElements().get(0);
        Triple t = epb.getPattern().get(0).asTriple();
        assertEquals(Node.ANY, t.getSubject());
        assertEquals(p, t.getPredicate());
        assertEquals(builder.makeNode("foo"), t.getObject());
    }

    @Test
    public void testInsertAndDeleteWithVariableNode() {
        UpdateBuilder builder = new UpdateBuilder();
        Node v = NodeFactory.createVariable("v");

        builder.addInsert(new Quad(g, s, v, o));
        builder.addDelete(Triple.create(s, v, o));
        builder.addWhere(null, v, "foo");

        Update update = builder.build();
        assertTrue(update instanceof UpdateModify);
        UpdateModify um = (UpdateModify) update;
        List<Quad> quads = um.getInsertQuads();
        assertEquals(1, quads.size());
        Quad q = quads.get(0);
        assertEquals(g, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(v, q.getPredicate());
        assertEquals(o, q.getObject());
        assertTrue(Var.isVar(q.getPredicate()));

        quads = um.getDeleteQuads();
        assertEquals(1, quads.size());
        q = quads.get(0);
        assertEquals(Quad.defaultGraphNodeGenerated, q.getGraph());
        assertEquals(s, q.getSubject());
        assertEquals(v, q.getPredicate());
        assertEquals(o, q.getObject());
        assertTrue(Var.isVar(q.getPredicate()));

        Element e = um.getWherePattern();
        assertTrue(e instanceof ElementGroup);
        ElementGroup eg = (ElementGroup) e;
        assertEquals(1, eg.getElements().size());
        ElementPathBlock epb = (ElementPathBlock) eg.getElements().get(0);
        Triple t = epb.getPattern().get(0).asTriple();
        assertEquals(Node.ANY, t.getSubject());
        assertEquals(v, t.getPredicate());
        assertEquals(builder.makeNode("foo"), t.getObject());
        assertTrue(Var.isVar(t.getPredicate()));
    }
    // testsbased on the examples

    /*
     * 	Example 1: Adding some triples to a graph

    This snippet describes two RDF triples to be inserted into the default graph of the Graph Store.

    PREFIX dc: <http://purl.org/dc/elements/1.1/>
    INSERT DATA
    {
      <http://example/book1> dc:title "A new book" ;
                             dc:creator "A.N.Other" .
    }

    Data before:

    # Default graph
    @prefix dc: <http://purl.org/dc/elements/1.1/> .
    @prefix ns: <http://example.org/ns#> .

    <http://example/book1> ns:price 42 .

    Data after:

    # Default graph
    @prefix dc: <http://purl.org/dc/elements/1.1/> .
    @prefix ns: <http://example.org/ns#> .

    <http://example/book1> ns:price 42 .
    <http://example/book1> dc:title "A new book" .
    <http://example/book1> dc:creator "A.N.Other" .

     */
    @Test
    public void example1() {
        Node n = NodeFactory.createURI("http://example/book1");
        Node priceN = NodeFactory.createURI("http://example.org/ns#price");
        Node priceV = NodeFactory.createLiteralString("42");
        UpdateBuilder builder = new UpdateBuilder().addPrefix("dc", DC_11.NS).addInsert(n, DC_11.title, "A new book")
                .addInsert(n, DC_11.creator, "A.N.Other");

        Model m = ModelFactory.createDefaultModel();
        m.getGraph().add(n, priceN, priceV);
        m.setNsPrefix("dc", DC_11.NS);
        m.setNsPrefix("ns", "http://example.org/ns#");

        UpdateAction.execute(builder.build(), m);

        Resource r = ResourceFactory.createResource(n.getURI());
        Property rPriceP = ResourceFactory.createProperty(priceN.getURI());
        Literal rPriceV = ResourceFactory.createPlainLiteral("42");
        assertTrue(m.contains(r, rPriceP, rPriceV));
        assertTrue(m.contains(r, DC_11.title, "A new book"));
        assertTrue(m.contains(r, DC_11.creator, "A.N.Other"));
        assertEquals(3, m.size());
    }

    @Test
    public void testPathInWhereClause() {
        Node p = NodeFactory.createURI("http://example.com/p");
        Path path = new P_Link(p);

        // JENA-1739 fails here
        new UpdateBuilder().addDelete("?s", "<x>", "?p").addWhere("?s", path, "?p").build();
    }

    @Test
    public void testPathInOptionalClause() {
        Node p = NodeFactory.createURI("http://example.com/p");
        Path path = new P_Link(p);

        // JENA-1739 fails here
        new UpdateBuilder().addDelete("?s", "<x>", "?p").addOptional("?s", path, "?p").build();
    }
}
