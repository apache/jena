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
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.*;

import org.apache.jena.arq.querybuilder.*;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_Random;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.syntax.*;
import org.apache.jena.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;

public class WhereHandlerTest extends AbstractHandlerTest {

    private Query query;
    private WhereHandler handler;

    @Before
    public void setup() {
        query = new Query();
        handler = new WhereHandler(query);
    }

    @Test
    public void testAddAllOnEmpty() {
        Query query2 = new Query();
        WhereHandler handler2 = new WhereHandler(query2);
        handler2.addWhere(new TriplePath(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createLiteral("three"))));
        handler.addAll(handler2);
        handler.build();

        Triple t1 = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createLiteral("three"));
        TriplePath tp = new TriplePath(t1);
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);

        WhereValidator wv = new WhereValidator(epb);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);

    }

    @Test
    public void testAddAllPopulatedEmpty() {
        handler.addWhere(new TriplePath(Triple.ANY));
        Query query2 = new Query();
        WhereHandler handler2 = new WhereHandler(query2);
        handler2.addWhere(new TriplePath(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createLiteral("three"))));
        handler.addAll(handler2);
        handler.build();

        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(Triple.ANY);
        ElementGroup eg = new ElementGroup();
        eg.addElement(epb);
        epb = new ElementPathBlock();
        Triple t1 = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createLiteral("three"));
        epb.addTriple(t1);
        eg.addElement(epb);

        WhereValidator wv = new WhereValidator(eg);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);

    }

    @Test
    public void addWhereTriple() {
        handler.addWhere(new TriplePath(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createURI("three"))));
        handler.build();

        Triple t1 = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createURI("three"));
        TriplePath tp = new TriplePath(t1);
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);

        WhereValidator wv = new WhereValidator(epb);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);

    }

    @Test
    public void testAddWhereObjects() {
        handler.addWhere(
                new TriplePath(Triple.create(NodeFactory.createURI("one"), ResourceFactory.createResource("two").asNode(),
                        ResourceFactory.createLangLiteral("three", "en-US").asNode())));
        handler.build();

        Triple t1 = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                ResourceFactory.createLangLiteral("three", "en-US").asNode());
        TriplePath tp = new TriplePath(t1);
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        WhereValidator wv = new WhereValidator(epb);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);
    }

    @Test
    public void testAddWhereObjectsWithPath() {
        PrefixMapping pmap = new PrefixMappingImpl();
        pmap.setNsPrefix("ts", "urn:test:");
        Path path = PathParser.parse("ts:two/ts:dos", pmap);
        handler.addWhere(new TriplePath(NodeFactory.createURI("one"), path,
                ResourceFactory.createLangLiteral("three", "en-US").asNode()));
        handler.build();

        TriplePath tp = new TriplePath(NodeFactory.createURI("one"), path,
                ResourceFactory.createLangLiteral("three", "en-US").asNode());
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        WhereValidator wv = new WhereValidator(epb);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);

    }

    @Test
    public void testAddWhereAnonymous() {
        handler.addWhere(new TriplePath(Triple.create(Node.ANY, RDF.first.asNode(), Node.ANY)));
        handler.build();

        TriplePath tp = new TriplePath(Triple.create(Node.ANY, RDF.first.asNode(), Node.ANY));
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        WhereValidator wv = new WhereValidator(epb);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);

    }

    @Test
    public void testAddOptionalStrings() {
        handler.addOptional(new TriplePath(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createURI("three"))));
        handler.build();

        TriplePath tp = new TriplePath(
                Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three")));
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        ElementOptional opt = new ElementOptional(epb);
        WhereValidator wv = new WhereValidator(opt);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);

    }

    @Test
    public void testAddOptionalAnonymous() {
        handler.addOptional(new TriplePath(Triple.create(Node.ANY, RDF.first.asNode(), Node.ANY)));
        handler.build();

        TriplePath tp = new TriplePath(Triple.create(Node.ANY, RDF.first.asNode(), Node.ANY));
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        ElementOptional opt = new ElementOptional(epb);
        WhereValidator wv = new WhereValidator(opt);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);
    }

    @Test
    public void testAddOptionalWhereHandler() {

        WhereHandler pattern = new WhereHandler(new Query());
        Var s = Var.alloc("s");
        Node q = NodeFactory.createURI("urn:q");
        Node v = NodeFactory.createURI("urn:v");
        Var x = Var.alloc("x");
        Node n123 = NodeFactory.createLiteralByValue(123);

        pattern.addWhere(new TriplePath(Triple.create(s, q, n123)));
        pattern.addWhere(new TriplePath(Triple.create(s, v, x)));

        handler.addOptional(pattern);
        handler.build();

        ElementPathBlock epb = new ElementPathBlock();
        ElementOptional optional = new ElementOptional(epb);
        TriplePath tp = new TriplePath(Triple.create(s, q, n123));
        epb.addTriplePath(tp);
        tp = new TriplePath(Triple.create(s, v, x));
        epb.addTriplePath(tp);

        WhereValidator visitor = new WhereValidator(optional);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @Test
    public void testAddOptionalObjects() {
        handler.addOptional(
                new TriplePath(Triple.create(NodeFactory.createURI("one"), ResourceFactory.createResource("two").asNode(),
                        ResourceFactory.createLangLiteral("three", "en-US").asNode())));
        handler.build();

        ElementPathBlock epb = new ElementPathBlock();
        ElementOptional optional = new ElementOptional(epb);
        TriplePath tp = new TriplePath(
                Triple.create(NodeFactory.createURI("one"), ResourceFactory.createResource("two").asNode(),
                        ResourceFactory.createLangLiteral("three", "en-US").asNode()));
        epb.addTriplePath(tp);

        WhereValidator visitor = new WhereValidator(optional);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testAddOptionalObjectsWithPath() {
        PrefixMapping pmap = new PrefixMappingImpl();
        pmap.setNsPrefix("ts", "urn:test:");
        Path path = PathParser.parse("ts:two/ts:dos", pmap);

        handler.addOptional(new TriplePath(NodeFactory.createURI("one"), path,
                ResourceFactory.createLangLiteral("three", "en-US").asNode()));
        handler.build();

        ElementPathBlock epb = new ElementPathBlock();
        ElementOptional optional = new ElementOptional(epb);
        TriplePath tp = new TriplePath(NodeFactory.createURI("one"), path,
                ResourceFactory.createLangLiteral("three", "en-US").asNode());
        epb.addTriplePath(tp);

        WhereValidator visitor = new WhereValidator(optional);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @Test
    public void testAddWhereStrings() {
        handler.addWhere(new TriplePath(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createURI("three"))));
        handler.build();

        ElementPathBlock epb = new ElementPathBlock();
        TriplePath tp = new TriplePath(
                Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three")));
        epb.addTriplePath(tp);

        WhereValidator visitor = new WhereValidator(epb);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testAddFilter() throws ParseException {
        handler.addFilter("?one < 10");
        handler.build();

        BigInteger bi = new BigInteger(Integer.toString(10));
        E_LessThan expr = new ExprFactory().lt(NodeFactory.createVariable("one"), bi);
        WhereValidator visitor = new WhereValidator(new ElementFilter(expr));
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testAddFilterWithNamespace() throws ParseException {
        query.setPrefix("afn", "http://jena.apache.org/ARQ/function#");
        handler.addFilter("afn:namespace(?one) = 'foo'");
        handler.build();

        ExprFactory fact = new ExprFactory();
        E_Function func = new E_Function("http://jena.apache.org/ARQ/function#namespace", fact.asList("?one"));
        E_Equals expr = fact.eq(func, "foo");
        WhereValidator visitor = new WhereValidator(new ElementFilter(expr));
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @Test
    public void testAddFilterVarOnly() throws ParseException {
        handler.addFilter("?one");
        handler.build();

        ExprFactory fact = new ExprFactory();
        WhereValidator visitor = new WhereValidator(new ElementFilter(fact.asExpr("?one")));
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @Test
    public void testAddSubQueryWithVars() {
        SelectBuilder sb = new SelectBuilder();
        sb.addPrefix("pfx", "uri").addVar("?x").addWhere("<one>", "<two>", "three");
        handler.addSubQuery(sb);
        handler.build();

        Triple t1 = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createLiteral("three"));
        TriplePath tp = new TriplePath(t1);
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        Query q = new Query();

        q.addResultVar("x");
        q.setQuerySelectType();
        q.setQueryPattern(epb);
        ElementSubQuery esq = new ElementSubQuery(q);
        WhereValidator wv = new WhereValidator(esq);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);
    }

    @Test
    public void testAddSubQueryWithVarExpressions() throws ParseException {
        SelectBuilder sb = new SelectBuilder();
        sb.addPrefix("pfx", "uri").addVar("count(*)", "?x").addWhere("<one>", "<two>", "three");
        handler.addSubQuery(sb);
        handler.build();

        Triple t1 = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createLiteral("three"));
        TriplePath tp = new TriplePath(t1);
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        Query q = new Query();

        q.addResultVar("x", q.allocAggregate(new AggCount()));
        q.setQuerySelectType();
        q.setQueryPattern(epb);
        ElementSubQuery esq = new ElementSubQuery(q);
        WhereValidator wv = new WhereValidator(esq);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);
    }

    @Test
    public void testAddSubQueryWithoutVars() {
        SelectBuilder sb = new SelectBuilder();
        sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", "three");
        handler.addSubQuery(sb);
        handler.build();

        Triple t1 = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createLiteral("three"));
        TriplePath tp = new TriplePath(t1);
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        Query q = new Query();
        q.setQuerySelectType();
        q.setQueryResultStar(true);
        q.setQueryPattern(epb);
        ElementSubQuery esq = new ElementSubQuery(q);
        WhereValidator wv = new WhereValidator(esq);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);
    }

    @Test
    public void testAddUnion() {
        Triple t1 = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createURI("three"));
        Triple t2 = Triple.create(NodeFactory.createURI("uno"), NodeFactory.createURI("dos"),
                NodeFactory.createURI("tres"));

        SelectBuilder sb1 = new SelectBuilder().addWhere(t1);

        SelectBuilder sb2 = new SelectBuilder().addWhere(t2);

        handler.addUnion(sb1);
        handler.addUnion(sb2);
        handler.build();

        ElementUnion union = new ElementUnion();
        ElementPathBlock epb1 = new ElementPathBlock();
        epb1.addTriple(t1);
        union.addElement(epb1);

        ElementPathBlock epb2 = new ElementPathBlock();
        epb2.addTriple(t2);
        union.addElement(epb2);

        WhereValidator visitor = new WhereValidator(union);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @Test
    public void testAddUnionOfOne() {
        Triple t1 = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createURI("three"));
        SelectBuilder sb = new SelectBuilder().addWhere(t1);
        handler.addUnion(sb);
        handler.build();

        ElementPathBlock epb1 = new ElementPathBlock();
        epb1.addTriple(t1);

        WhereValidator visitor = new WhereValidator(epb1);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testAddUnionToExisting() {
        handler.addWhere(new TriplePath(
                Triple.create(NodeFactory.createURI("s"), NodeFactory.createURI("p"), NodeFactory.createURI("o"))));
        SelectBuilder sb = new SelectBuilder();
        sb.addWhere(
                Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three")));
        handler.addUnion(sb);
        handler.build();

        TriplePath tp1 = new TriplePath(
                Triple.create(NodeFactory.createURI("s"), NodeFactory.createURI("p"), NodeFactory.createURI("o")));
        ElementPathBlock epb1 = new ElementPathBlock();
        epb1.addTriple(tp1);

        TriplePath tp2 = new TriplePath(
                Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three")));
        ElementPathBlock epb2 = new ElementPathBlock();
        epb2.addTriple(tp2);

        ElementUnion union = new ElementUnion();
        union.addElement(epb1);
        union.addElement(epb2);

        WhereValidator visitor = new WhereValidator(union);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @Test
    public void testAddUnionWithVar() {
        Triple t1 = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createURI("three"));
        Triple t2 = Triple.create(NodeFactory.createURI("uno"), NodeFactory.createURI("dos"),
                NodeFactory.createURI("tres"));

        SelectBuilder sb = new SelectBuilder().addVar("x").addWhere(t1);
        handler.addUnion(sb);

        SelectBuilder sb2 = new SelectBuilder().addWhere(t2);
        handler.addUnion(sb2);
        handler.build();

        ElementUnion union = new ElementUnion();
        Query q = new Query();
        q.setQuerySelectType();
        ElementPathBlock epb1 = new ElementPathBlock();
        epb1.addTriple(t1);
        q.setQueryPattern(epb1);
        q.addProjectVars(Arrays.asList(Var.alloc("x")));
        ElementSubQuery sq = new ElementSubQuery(q);
        union.addElement(sq);
        ElementPathBlock epb2 = new ElementPathBlock();
        epb2.addTriple(t2);
        union.addElement(epb2);

        WhereValidator visitor = new WhereValidator(union);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @Test
    public void testAddUnionToExistingWithVar() {
        handler.addWhere(new TriplePath(
                Triple.create(NodeFactory.createURI("s"), NodeFactory.createURI("p"), NodeFactory.createURI("o"))));

        SelectBuilder sb = new SelectBuilder().addVar("x").addWhere(
                Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three")));

        handler.addUnion(sb);
        handler.build();

        Query q = new Query();
        q.setQuerySelectType();
        ElementPathBlock epb1 = new ElementPathBlock();
        epb1.addTriple(
                Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three")));
        q.setQueryPattern(epb1);
        q.addProjectVars(Arrays.asList(Var.alloc("x")));
        ElementSubQuery sq = new ElementSubQuery(q);
        ElementPathBlock epb2 = new ElementPathBlock();
        epb2.addTriple(Triple.create(NodeFactory.createURI("s"), NodeFactory.createURI("p"), NodeFactory.createURI("o")));

        ElementUnion union = new ElementUnion();
        union.addElement(epb2);
        union.addElement(sq);

        WhereValidator visitor = new WhereValidator(union);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void addGraph() {

        WhereHandler handler2 = new WhereHandler(new Query());
        handler2.addWhere(new TriplePath(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createURI("three"))));

        handler.addGraph(NodeFactory.createURI("graph"), handler2);
        handler.build();

        TriplePath tp = new TriplePath(
                Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three")));
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        ElementNamedGraph eng = new ElementNamedGraph(NodeFactory.createURI("graph"), epb);

        WhereValidator visitor = new WhereValidator(eng);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testSetVarsInTriple() {
        Var v = Var.alloc("v");
        handler.addWhere(new TriplePath(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), v)));
        handler.build();

        Triple t = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), v);
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(t);
        WhereValidator visitor = new WhereValidator(epb);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        Map<Var, Node> values = new HashMap<>();
        values.put(v, NodeFactory.createURI("three"));
        handler.setVars(values);
        handler.build();

        t = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three"));
        epb = new ElementPathBlock();
        epb.addTriple(t);
        visitor = new WhereValidator(epb);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @Test
    public void testSetVarsInFilter() throws ParseException {
        handler.addFilter("?one < ?v");
        handler.build();

        E_LessThan expr = new ExprFactory().lt(NodeFactory.createVariable("one"), NodeFactory.createVariable("v"));
        WhereValidator visitor = new WhereValidator(new ElementFilter(expr));
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        Map<Var, Node> values = new HashMap<>();

        values.put(Var.alloc("v"), NodeFactory.createLiteralByValue(10));
        handler.setVars(values);
        handler.build();

        expr = new ExprFactory().lt(NodeFactory.createVariable("one"),
                                    NodeFactory.createLiteralByValue(10));
        visitor = new WhereValidator(new ElementFilter(expr));
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testSetVarsInOptional() {
        Var v = Var.alloc("v");
        handler.addOptional(new TriplePath(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), v)));
        handler.build();

        TriplePath tp = new TriplePath(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), v));
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        ElementOptional opt = new ElementOptional(epb);
        WhereValidator wv = new WhereValidator(opt);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);

        Map<Var, Node> values = new HashMap<>();
        values.put(v, NodeFactory.createURI("three"));
        handler.setVars(values);
        handler.build();

        tp = new TriplePath(
                Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three")));
        epb = new ElementPathBlock();
        epb.addTriple(tp);
        opt = new ElementOptional(epb);
        wv = new WhereValidator(opt);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);
    }

    @Test
    public void testSetVarsInSubQuery() {
        Var v = Var.alloc("v");
        SelectBuilder sb = new SelectBuilder();
        sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
        handler.addSubQuery(sb);
        handler.build();

        TriplePath tp = new TriplePath(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), v));
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        WhereValidator wv = new WhereValidator(epb);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);

        Map<Var, Node> values = new HashMap<>();
        values.put(v, NodeFactory.createURI("three"));
        handler.setVars(values);
        handler.build();

        tp = new TriplePath(
                Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three")));
        epb = new ElementPathBlock();
        epb.addTriple(tp);
        wv = new WhereValidator(epb);
        query.getQueryPattern().visit(wv);
        assertTrue(wv.matching);
    }

    @Test
    public void testSetVarsInUnion() {
        Var v = Var.alloc("v");
        SelectBuilder sb = new SelectBuilder();
        sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
        handler.addUnion(sb);
        SelectBuilder sb2 = new SelectBuilder().addWhere("<uno>", "<dos>", "<tres>");
        handler.addUnion(sb2);
        handler.build();

        Node one = NodeFactory.createURI("one");
        Node two = NodeFactory.createURI("two");
        Node three = NodeFactory.createURI("three");
        Node uno = NodeFactory.createURI("uno");
        Node dos = NodeFactory.createURI("dos");
        Node tres = NodeFactory.createURI("tres");

        ElementUnion union = new ElementUnion();
        ElementPathBlock epb = new ElementPathBlock();
        Triple t = Triple.create(one, two, v);
        epb.addTriple(t);
        union.addElement(epb);
        ElementPathBlock epb2 = new ElementPathBlock();
        t = Triple.create(uno, dos, tres);
        epb2.addTriple(t);
        union.addElement(epb2);
        WhereValidator visitor = new WhereValidator(union);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        Map<Var, Node> values = new HashMap<>();
        values.put(v, three);
        handler.setVars(values);
        handler.build();

        union = new ElementUnion();
        epb = new ElementPathBlock();
        t = Triple.create(one, two, three);
        epb.addTriple(t);
        union.addElement(epb);
        epb2 = new ElementPathBlock();
        t = Triple.create(uno, dos, tres);
        epb2.addTriple(t);
        union.addElement(epb2);
        visitor = new WhereValidator(union);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @Test
    public void testBindStringVar() throws ParseException {
        Var v = Var.alloc("foo");
        handler.addBind("rand()", v);
        handler.build();

        ElementBind bind = new ElementBind(Var.alloc("foo"), new E_Random());
        WhereValidator visitor = new WhereValidator(bind);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @Test
    public void testBindExprVar() {
        Var v = Var.alloc("foo");
        handler.addBind(new E_Random(), v);
        handler.build();

        ElementBind bind = new ElementBind(Var.alloc("foo"), new E_Random());
        WhereValidator visitor = new WhereValidator(bind);
        handler.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testList() {
        Node n = handler.list("<one>", "?var", "'three'");

        Node one = NodeFactory.createURI("one");
        Node two = Var.alloc("var");
        Node three = NodeFactory.createLiteral("three");

        ElementPathBlock epb = new ElementPathBlock();
        Node firstObject = NodeFactory.createBlankNode();
        Node secondObject = NodeFactory.createBlankNode();
        Node thirdObject = NodeFactory.createBlankNode();

        epb.addTriplePath(new TriplePath(Triple.create(firstObject, RDF.first.asNode(), one)));
        epb.addTriplePath(new TriplePath(Triple.create(firstObject, RDF.rest.asNode(), secondObject)));
        epb.addTriplePath(new TriplePath(Triple.create(secondObject, RDF.first.asNode(), two)));
        epb.addTriplePath(new TriplePath(Triple.create(secondObject, RDF.rest.asNode(), thirdObject)));
        epb.addTriplePath(new TriplePath(Triple.create(thirdObject, RDF.first.asNode(), three)));
        epb.addTriplePath(new TriplePath(Triple.create(thirdObject, RDF.rest.asNode(), RDF.nil.asNode())));

        WhereValidator visitor = new WhereValidator(epb);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        assertTrue(n.isBlank());
    }

    @Test
    public void testListInTriple() {
        handler.addWhere(new TriplePath(Triple.create(handler.list("<one>", "?var", "'three'"),
                ResourceFactory.createResource("foo").asNode(), ResourceFactory.createResource("bar").asNode())));
        handler.build();

        P_Link first = new P_Link(RDF.first.asNode());
        P_Link rest = new P_Link(RDF.rest.asNode());
        Node n = NodeFactory.createURI("foo");
        P_Link foo = new P_Link(n);
        // ElementPathBlock epb = (ElementPathBlock) query.getQueryPattern();
        ElementPathBlock epb = new ElementPathBlock();
        Node list = NodeFactory.createBlankNode();
        Node s = list;
        epb.addTriple(new TriplePath(s, first, NodeFactory.createURI("one")));
        Node o = NodeFactory.createBlankNode();
        epb.addTriple(new TriplePath(s, rest, o));
        s = o;
        epb.addTriple(new TriplePath(s, first, Var.alloc("var")));
        o = NodeFactory.createBlankNode();
        epb.addTriple(new TriplePath(s, rest, o));
        s = o;
        epb.addTriple(new TriplePath(s, first, NodeFactory.createLiteral("three")));
        epb.addTriple(new TriplePath(s, rest, RDF.nil.asNode()));
        epb.addTriple(new TriplePath(list, foo, NodeFactory.createURI("bar")));

        WhereValidator visitor = new WhereValidator(epb);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testAddMinus() {
        SelectBuilder sb = new SelectBuilder();
        sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", "three");
        handler.addMinus(sb);
        handler.build();

        ElementPathBlock epb = new ElementPathBlock();
        ElementMinus minus = new ElementMinus(epb);
        epb.addTriplePath(new TriplePath(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createLiteral("three"))));
        WhereValidator visitor = new WhereValidator(minus);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testAddValueVar_pfx_obj() {
        handler.addValueVar(query.getPrefixMapping(), "?v");
        handler.build();

        Var v = Var.alloc("v");
        ElementData edat = new ElementData();
        edat.add(v);

        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testAddValueVar_pfx_obj_array() {

        Node two = NodeFactory.createURI("two");
        handler.addValueVar(query.getPrefixMapping(), "?v", "<one>", two);
        handler.build();

        Var v = Var.alloc("v");
        ElementData edat = new ElementData();
        edat.add(v);
        Binding binding1 = BindingFactory.binding(v, NodeFactory.createURI("one"));
        edat.add(binding1);
        Binding binding2 = BindingFactory.binding(v, two);
        edat.add(binding2);

        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testAddValueVars() {
        final Var v = Var.alloc("v");
        Map<Object, List<?>> map = new LinkedHashMap<Object, List<?>>();

        map.put(Var.alloc("v"), Arrays.asList("<one>", "<two>"));
        map.put("?x", Arrays.asList("three", "four"));

        handler.addValueVars(query.getPrefixMapping(), map);
        handler.build();

        Var x = Var.alloc("x");
        ElementData edat = new ElementData();

        edat.add(v);
        edat.add(x);
        setupBindings(edat, x, v);

        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testAddValueRow_pfx_array() {
        final Var v = Var.alloc("v");
        final Var x = Var.alloc("x");

        handler.addValueVar(query.getPrefixMapping(), v);
        handler.addValueVar(query.getPrefixMapping(), x);
        handler.addValueRow(query.getPrefixMapping(), "<one>", "three");
        handler.addValueRow(query.getPrefixMapping(), "<two>", "four");
        handler.build();

        ElementData edat = new ElementData();
        edat.add(v);
        edat.add(x);
        setupBindings(edat, x, v);

        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testAddValueRow_pfx_collection() {
        final Var v = Var.alloc("v");
        final Var x = Var.alloc("x");

        handler.addValueVar(query.getPrefixMapping(), v);
        handler.addValueVar(query.getPrefixMapping(), x);
        handler.addValueRow(query.getPrefixMapping(), Arrays.asList("<one>", "three"));
        handler.addValueRow(query.getPrefixMapping(), Arrays.asList("<two>", "four"));
        handler.build();

        ElementData edat = new ElementData();
        edat.add(v);
        edat.add(x);
        setupBindings(edat, x, v);

        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    private static void setupBindings(ElementData edat, Var x, Var v) {
        Binding binding1 = BindingFactory.binding(v, NodeFactory.createURI("one"), x,
                NodeFactory.createLiteral("three"));
        edat.add(binding1);
        Binding binding2 = BindingFactory.binding(v, NodeFactory.createURI("two"), x,
                NodeFactory.createLiteral("four"));
        edat.add(binding2);
    }

    @Test
    public void testGetValuesVars() {
        final Var v = Var.alloc("v");
        final Var x = Var.alloc("x");

        handler.addValueVar(query.getPrefixMapping(), v);
        handler.addValueVar(query.getPrefixMapping(), "?x");
        List<Var> lst = handler.getValuesVars();
        assertEquals(2, lst.size());
        assertTrue(lst.contains(v));
        assertTrue(lst.contains(x));
    }

    @Test
    public void testGetValuesMap() {
        final Var v = Var.alloc("v");
        final Var x = Var.alloc("x");

        handler.addValueVar(query.getPrefixMapping(), v);
        handler.addValueVar(query.getPrefixMapping(), x);
        handler.addValueRow(query.getPrefixMapping(), Arrays.asList("<one>", "three"));
        handler.addValueRow(query.getPrefixMapping(), Arrays.asList("<two>", "four"));

        Map<Var, List<Node>> map = handler.getValuesMap();
        assertEquals(2, map.keySet().size());
        assertTrue(map.keySet().contains(v));
        assertTrue(map.keySet().contains(x));
        List<Node> lst = map.get(v);
        assertEquals(2, lst.size());
        assertTrue(lst.contains(NodeFactory.createURI("one")));
        assertTrue(lst.contains(NodeFactory.createURI("two")));
        lst = map.get(x);
        assertEquals(2, lst.size());
        assertTrue(lst.contains(NodeFactory.createLiteral("three")));
        assertTrue(lst.contains(NodeFactory.createLiteral("four")));
    }

    @Test
    public void testClearValues() {
        final Var v = Var.alloc("v");
        final Var x = Var.alloc("x");

        handler.addValueVar(query.getPrefixMapping(), v);
        handler.addValueVar(query.getPrefixMapping(), x);
        handler.addValueRow(query.getPrefixMapping(), Arrays.asList("<one>", "three"));
        handler.addValueRow(query.getPrefixMapping(), Arrays.asList("<two>", "four"));
        handler.clearValues();
        Map<Var, List<Node>> map = handler.getValuesMap();
        assertTrue(map.isEmpty());
    }

    @Test
    public void testSetVarsInWhereValues() {
        Var v = Var.alloc("v");
        Node value = NodeFactory.createLiteralByValue(10);
        Map<Var, Node> values = new HashMap<>();
        values.put(v, value);

        handler.addValueVar(query.getPrefixMapping(), "?x", "<one>", "?v");
        handler.setVars(values);
        handler.build();

        ElementData edat = new ElementData();
        Var x = Var.alloc("x");
        edat.add(x);
        Binding binding1 = BindingFactory.binding(x, NodeFactory.createURI("one"));
        edat.add(binding1);
        Binding binding2 = BindingFactory.binding(x, value);
        edat.add(binding2);

        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @Test
    public void testWhereDataQuery() {
        // test that the getVars getMap and clear methods work.
        Var x = Var.alloc("x");
        Var y = Var.alloc("y");
        Node foo = NodeFactory.createURI("foo");
        Node bar = NodeFactory.createLiteral("bar");

        assertTrue(handler.getValuesVars().isEmpty());

        handler.addValueVar(query.getPrefixMapping(), x, foo);
        handler.addValueVar(query.getPrefixMapping(), y, bar);

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

        handler.clearValues();

        assertTrue(handler.getValuesVars().isEmpty());
        assertTrue(handler.getValuesMap().isEmpty());

    }

    @Test
    public void testMakeSubQueryFromSelectWithVar() {
        SelectBuilder sb = new SelectBuilder().addVar("?x").addWhere("?x", RDF.type, RDF.Alt);

        ElementSubQuery esq = handler.makeSubQuery(sb);

        Triple t1 = Triple.create(NodeFactory.createVariable("x"), RDF.type.asNode(), RDF.Alt.asNode());
        TriplePath tp = new TriplePath(t1);
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        Query q = new Query();
        q.setQuerySelectType();
        q.addResultVar(NodeFactory.createVariable("x"));
        q.setQueryPattern(epb);
        ElementSubQuery esq2 = new ElementSubQuery(q);
        WhereValidator wv = new WhereValidator(esq2);
        esq.visit(wv);
        assertTrue(wv.matching);
    }

    @Test
    public void testMakeSubQueryFromSelectWithOutVar() {
        SelectBuilder sb = new SelectBuilder().addWhere("?x", RDF.type, RDF.Alt);

        ElementSubQuery esq = handler.makeSubQuery(sb);

        Triple t1 = Triple.create(NodeFactory.createVariable("x"), RDF.type.asNode(), RDF.Alt.asNode());
        TriplePath tp = new TriplePath(t1);
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        Query q = new Query();
        q.setQuerySelectType();
        q.setQueryResultStar(true);
        q.setQueryPattern(epb);
        ElementSubQuery esq2 = new ElementSubQuery(q);
        WhereValidator wv = new WhereValidator(esq2);
        esq.visit(wv);
        assertTrue(wv.matching);
    }

    @Test
    public void testMakeSubQueryFromConstruct() {
        AbstractQueryBuilder<?> sb = new ConstructBuilder().addConstruct("?x", RDF.type, NodeFactory.createURI("foo"))
                .addWhere("?x", RDF.type, RDF.Alt);

        ElementSubQuery esq = handler.makeSubQuery(sb);

        Triple t1 = Triple.create(NodeFactory.createVariable("x"), RDF.type.asNode(), RDF.Alt.asNode());
        TriplePath tp = new TriplePath(t1);
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        Query q = new Query();
        q.setQuerySelectType();
        q.setQueryResultStar(true);
        q.setQueryPattern(epb);
        ElementSubQuery esq2 = new ElementSubQuery(q);
        WhereValidator wv = new WhereValidator(esq2);
        esq.visit(wv);
        assertTrue(wv.matching);
    }

    @Test
    public void testMakeSubQueryFromDescribe() {
        AbstractQueryBuilder<?> sb = new DescribeBuilder().addWhere("?x", RDF.type, RDF.Alt);

        ElementSubQuery esq = handler.makeSubQuery(sb);

        Triple t1 = Triple.create(NodeFactory.createVariable("x"), RDF.type.asNode(), RDF.Alt.asNode());
        TriplePath tp = new TriplePath(t1);
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        Query q = new Query();
        q.setQuerySelectType();
        q.setQueryResultStar(true);
        q.setQueryPattern(epb);
        ElementSubQuery esq2 = new ElementSubQuery(q);
        WhereValidator wv = new WhereValidator(esq2);
        esq.visit(wv);
        assertTrue(wv.matching);
    }

    @Test
    public void testMakeSubQueryFromAsk() {
        AbstractQueryBuilder<?> sb = new AskBuilder().addWhere("?x", RDF.type, RDF.Alt);

        ElementSubQuery esq = handler.makeSubQuery(sb);

        Triple t1 = Triple.create(NodeFactory.createVariable("x"), RDF.type.asNode(), RDF.Alt.asNode());
        TriplePath tp = new TriplePath(t1);
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        Query q = new Query();
        q.setQuerySelectType();
        q.setQueryResultStar(true);
        q.setQueryPattern(epb);
        ElementSubQuery esq2 = new ElementSubQuery(q);
        WhereValidator wv = new WhereValidator(esq2);
        esq.visit(wv);
        assertTrue(wv.matching);
    }

    @Test
    public void testMakeSubQueryFromWhere() {
        AbstractQueryBuilder<?> sb = new WhereBuilder().addWhere("?x", RDF.type, RDF.Alt);

        ElementSubQuery esq = handler.makeSubQuery(sb);

        Triple t1 = Triple.create(NodeFactory.createVariable("x"), RDF.type.asNode(), RDF.Alt.asNode());
        TriplePath tp = new TriplePath(t1);
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        Query q = new Query();
        q.setQuerySelectType();
        q.setQueryResultStar(true);
        q.setQueryPattern(epb);
        ElementSubQuery esq2 = new ElementSubQuery(q);
        WhereValidator wv = new WhereValidator(esq2);
        esq.visit(wv);
        assertTrue(wv.matching);
    }
}
