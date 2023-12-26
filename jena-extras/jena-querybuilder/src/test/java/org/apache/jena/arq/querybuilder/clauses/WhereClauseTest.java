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
package org.apache.jena.arq.querybuilder.clauses;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.apache.jena.arq.TestAbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.arq.querybuilder.WhereValidator;
import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_Random;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueInteger;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.syntax.*;
import org.apache.jena.vocabulary.RDF;
import org.junit.After;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;
import org.xenei.junit.contract.IProducer;

@Contract(WhereClause.class)
public class WhereClauseTest<T extends WhereClause<?>> extends AbstractClauseTest {

    // the producer we will use
    private IProducer<T> producer;

    @Contract.Inject
    // define the method to set producer.
    public final void setProducer(IProducer<T> producer) {
        this.producer = producer;
    }

    protected final IProducer<T> getProducer() {
        return producer;
    }

    @After
    public final void cleanupWhereClauseTest() {
        getProducer().cleanUp(); // clean up the producer for the next run
    }

    @ContractTest
    public void testAddWhere3Objects() {
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addWhere("<one>", "<two>", "three");

        ElementPathBlock epb = new ElementPathBlock();
        Triple t = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createLiteralString("three"));
        epb.addTriple(t);

        WhereValidator visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }
    

    @ContractTest
    public void testAddWhereAbstractQueryBuilder() {
        WhereClause<?> whereClause = getProducer().newInstance();
        TriplePath tp = new TriplePath(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three")));

        TestAbstractQueryBuilder abstractQueryBuilder = new TestAbstractQueryBuilder();
        abstractQueryBuilder.getHandlerBlock().getWhereHandler().addWhere(tp);
        AbstractQueryBuilder<?> builder = whereClause.addWhere(abstractQueryBuilder);
        
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriplePath(tp);
        WhereValidator visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }
    
    @ContractTest
    public void testAddWhereTriplePath() {
        WhereClause<?> whereClause = getProducer().newInstance();
        PrefixMapping pmap = new PrefixMappingImpl();
        pmap.setNsPrefix("ts", "urn:test:");
        Path path = PathParser.parse("ts:two/ts:dos", pmap);
        TriplePath first = new TriplePath(NodeFactory.createURI("one"), path, NodeFactory.createURI("three"));
        
        AbstractQueryBuilder<?> builder = whereClause
                .addWhere(first);

        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriplePath(first);

        WhereValidator visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }
    

    @ContractTest
    public void testAddWhereTriple() {
        WhereClause<?> whereClause = getProducer().newInstance();
        Triple triple = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three"));

        AbstractQueryBuilder<?> builder = whereClause
                .addWhere(triple);

        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(triple);

        WhereValidator visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddWhereFrontsTriple() {
        WhereClause<?> whereClause = getProducer().newInstance();
        Triple triple = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three"));
        FrontsTriple front = new FrontsTriple() {

            @Override
            public Triple asTriple() {
                return triple;
            }};
        
        AbstractQueryBuilder<?> builder = whereClause
                .addWhere(front);

        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(triple);

        WhereValidator visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddWhereTriplePathCollection() {
        WhereClause<?> whereClause = getProducer().newInstance();
        PrefixMapping pmap = new PrefixMappingImpl();
        pmap.setNsPrefix("ts", "urn:test:");
        Path path = PathParser.parse("ts:two/ts:dos", pmap);
        TriplePath first = new TriplePath(NodeFactory.createURI("one"), path, NodeFactory.createURI("three"));
        TriplePath second = new TriplePath(NodeFactory.createURI("for"), path, NodeFactory.createURI("six"));

        AbstractQueryBuilder<?> builder = whereClause
                .addWhere(List.of(first, second));

        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriplePath(first);
        epb.addTriplePath(second);

        WhereValidator visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddWhereWhereClause() {
        WhereBuilder whereBuilder = new WhereBuilder().addWhere(new TriplePath(Triple.create(NodeFactory.createURI("one"),
                NodeFactory.createURI("two"), NodeFactory.createURI("three"))));

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addWhere(whereBuilder);

        ElementPathBlock epb = new ElementPathBlock();
        Triple t = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createURI("three"));
        epb.addTriple(t);

        WhereValidator visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddOptionalString() {
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addOptional("<one>", "<two>", "three");

        ElementPathBlock epb = new ElementPathBlock();
        ElementOptional optional = new ElementOptional(epb);
        Triple t = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createLiteralString("three"));
        epb.addTriple(t);

        WhereValidator visitor = new WhereValidator(optional);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testAddOptionalStringWithPath() {
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addOptional("<one>", "<two>/<dos>", "three");

        Path path = new P_Seq(new P_Link(NodeFactory.createURI("two")), new P_Link(NodeFactory.createURI("dos")));
        ElementPathBlock epb = new ElementPathBlock();
        ElementOptional optional = new ElementOptional(epb);
        TriplePath tp = new TriplePath(NodeFactory.createURI("one"), path, NodeFactory.createLiteralString("three"));
        epb.addTriplePath(tp);

        WhereValidator visitor = new WhereValidator(optional);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddOptionalObjects() {
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addOptional(NodeFactory.createURI("one"),
                NodeFactory.createURI("two"), NodeFactory.createURI("three"));

        ElementPathBlock epb = new ElementPathBlock();
        ElementOptional optional = new ElementOptional(epb);
        Triple t = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createURI("three"));
        epb.addTriple(t);

        WhereValidator visitor = new WhereValidator(optional);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddOptionalTriple() {
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addOptional(NodeFactory.createURI("one"),
                NodeFactory.createURI("two"), NodeFactory.createURI("three"));

        ElementPathBlock epb = new ElementPathBlock();
        ElementOptional optional = new ElementOptional(epb);
        Triple t = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createURI("three"));
        epb.addTriple(t);

        WhereValidator visitor = new WhereValidator(optional);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddOptionalTriplePath() {
        WhereClause<?> whereClause = getProducer().newInstance();
        PrefixMapping pmap = new PrefixMappingImpl();
        pmap.setNsPrefix("ts", "urn:test:");
        Path path = PathParser.parse("ts:two/ts:dos", pmap);
        AbstractQueryBuilder<?> builder = whereClause
                .addOptional(new TriplePath(NodeFactory.createURI("one"), path, NodeFactory.createURI("three")));

        ElementPathBlock epb = new ElementPathBlock();
        ElementOptional optional = new ElementOptional(epb);
        TriplePath tp = new TriplePath(NodeFactory.createURI("one"), path, NodeFactory.createURI("three"));
        epb.addTriplePath(tp);

        WhereValidator visitor = new WhereValidator(optional);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddOptionalTriplePathCollection() {
        WhereClause<?> whereClause = getProducer().newInstance();
        PrefixMapping pmap = new PrefixMappingImpl();
        pmap.setNsPrefix("ts", "urn:test:");
        Path path = PathParser.parse("ts:two/ts:dos", pmap);
        TriplePath first = new TriplePath(NodeFactory.createURI("one"), path, NodeFactory.createURI("three"));
        TriplePath second = new TriplePath(NodeFactory.createURI("for"), path, NodeFactory.createURI("six"));

        AbstractQueryBuilder<?> builder = whereClause
                .addOptional(List.of(first, second));

        ElementPathBlock epb = new ElementPathBlock();
        ElementOptional optional = new ElementOptional(epb);
        epb.addTriplePath(first);
        epb.addTriplePath(second);

        WhereValidator visitor = new WhereValidator(optional);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }
    
    @ContractTest
    public void testAddOptionalObjectsWithPath() {
        WhereClause<?> whereClause = getProducer().newInstance();
        PrefixMapping pmap = new PrefixMappingImpl();
        pmap.setNsPrefix("ts", "urn:test:");
        Path path = PathParser.parse("ts:two/ts:dos", pmap);
        AbstractQueryBuilder<?> builder = whereClause.addOptional(NodeFactory.createURI("one"), path,
                NodeFactory.createURI("three"));

        ElementPathBlock epb = new ElementPathBlock();
        ElementOptional optional = new ElementOptional(epb);
        TriplePath tp = new TriplePath(NodeFactory.createURI("one"), path, NodeFactory.createURI("three"));
        epb.addTriplePath(tp);

        WhereValidator visitor = new WhereValidator(optional);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testAddOptionalGroupPattern() {

        Var s = Var.alloc("s");
        Node q = NodeFactory.createURI("urn:q");
        Node v = NodeFactory.createURI("urn:v");
        Var x = Var.alloc("x");
        Node n123 = NodeFactory.createLiteralByValue(123);

        SelectBuilder pattern = new SelectBuilder();
        pattern.addWhere(Triple.create(s, q, n123));
        pattern.addWhere(Triple.create(s, v, x));

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addOptional(pattern);

        ElementPathBlock epb = new ElementPathBlock();
        ElementOptional optional = new ElementOptional(epb);
        TriplePath tp = new TriplePath(Triple.create(s, q, n123));
        epb.addTriplePath(tp);
        tp = new TriplePath(Triple.create(s, v, x));
        epb.addTriplePath(tp);

        WhereValidator visitor = new WhereValidator(optional);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testAddOptionalGroupPattern_VariableNode() {

        Node s = NodeFactory.createVariable("s");
        Node q = NodeFactory.createURI("urn:q");
        Node v = NodeFactory.createURI("urn:v");
        Var x = Var.alloc("x");
        Node n123 = NodeFactory.createLiteralByValue(123);

        SelectBuilder pattern = new SelectBuilder();
        pattern.addWhere(Triple.create(s, q, n123));
        pattern.addWhere(Triple.create(s, v, x));

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addOptional(pattern);

        ElementPathBlock epb = new ElementPathBlock();
        ElementOptional optional = new ElementOptional(epb);
        TriplePath tp = new TriplePath(Triple.create(Var.alloc(s), q, n123));
        epb.addTriplePath(tp);
        tp = new TriplePath(Triple.create(Var.alloc(s), v, x));
        epb.addTriplePath(tp);

        WhereValidator visitor = new WhereValidator(optional);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testAddFilter() {
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addFilter("?one<10");

        E_LessThan lt = new E_LessThan(new ExprVar(Var.alloc("one")), new NodeValueInteger(10));
        ElementFilter ef = new ElementFilter(lt);

        WhereValidator visitor = new WhereValidator(ef);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testAddSubQuery() {
        SelectBuilder sb = new SelectBuilder();
        sb.addPrefix("pfx", "urn:uri:").addVar("?x").addWhere("pfx:one", "pfx:two", "pfx:three");
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addSubQuery(sb);
        Query query = builder.build();

        Query q2 = new Query();
        q2.setQuerySelectType();
        q2.addProjectVars(Arrays.asList(Var.alloc("x")));
        ElementPathBlock epb = new ElementPathBlock();
        q2.setQueryPattern(epb);
        epb.addTriplePath(new TriplePath(Triple.create(NodeFactory.createURI("urn:uri:one"),
                NodeFactory.createURI("urn:uri:two"), NodeFactory.createURI("urn:uri:three"))));
        ElementSubQuery esq = new ElementSubQuery(q2);

        WhereValidator visitor = new WhereValidator(esq);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testAddUnion() {
        SelectBuilder sb = new SelectBuilder();
        sb.addPrefix("pfx", "uri").addVar("?x").addWhere("<one>", "<two>", "three");
        WhereClause<?> whereClause = getProducer().newInstance();
        whereClause.getWhereHandler().addWhere(new TriplePath(Triple.ANY));
        AbstractQueryBuilder<?> builder = whereClause.addUnion(sb);

        ElementUnion union = new ElementUnion();
        ElementPathBlock epb = new ElementPathBlock();
        union.addElement(epb);
        epb.addTriple(Triple.ANY);

        Query subQuery = new Query();
        ElementSubQuery esq = new ElementSubQuery(subQuery);
        union.addElement(esq);
        epb = new ElementPathBlock();
        subQuery.setQuerySelectType();
        subQuery.addProjectVars(Arrays.asList("x"));
        subQuery.setQueryPattern(epb);
        Triple t = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createLiteralString("three"));
        epb.addTriple(t);

        WhereValidator visitor = new WhereValidator(union);
        Query result = builder.build();
        result.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        assertEquals("uri", result.getPrefixMapping().getNsPrefixURI("pfx"));
    }

    @ContractTest
    public void testSetVarsInTriple() {
        Var v = Var.alloc("v");
        Node one = NodeFactory.createURI("one");
        Node two = NodeFactory.createURI("two");
        Node three = NodeFactory.createURI("three");
        Node four = NodeFactory.createURI("four");

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addWhere(Triple.create(one, two, v));

        TriplePath tp = new TriplePath(Triple.create(one, two, v));
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        WhereValidator visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        builder.setVar(v, three);

        tp = new TriplePath(Triple.create(one, two, three));
        epb = new ElementPathBlock();
        epb.addTriple(tp);
        visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        builder.setVar(v, four);

        tp = new TriplePath(Triple.create(one, two, four));
        epb = new ElementPathBlock();
        epb.addTriple(tp);
        visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        builder.setVar(v, null);

        tp = new TriplePath(Triple.create(one, two, v));
        epb = new ElementPathBlock();
        epb.addTriple(tp);
        visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testSetVarsInTriple_Node_Variable() {
        Node v = NodeFactory.createVariable("v");
        Node one = NodeFactory.createURI("one");
        Node two = NodeFactory.createURI("two");
        Node three = NodeFactory.createURI("three");
        Node four = NodeFactory.createURI("four");

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addWhere(Triple.create(one, two, v));

        TriplePath tp = new TriplePath(Triple.create(one, two, Var.alloc(v)));
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(tp);
        WhereValidator visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        builder.setVar(v, three);

        tp = new TriplePath(Triple.create(one, two, three));
        epb = new ElementPathBlock();
        epb.addTriple(tp);
        visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        builder.setVar(v, four);

        tp = new TriplePath(Triple.create(one, two, four));
        epb = new ElementPathBlock();
        epb.addTriple(tp);
        visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        builder.setVar(v, null);

        tp = new TriplePath(Triple.create(one, two, Var.alloc("v")));
        epb = new ElementPathBlock();
        epb.addTriple(tp);
        visitor = new WhereValidator(epb);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testSetVarsInFilter() {
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addFilter("?one < ?v");

        ExprVar one = new ExprVar(NodeFactory.createVariable("one"));
        ExprVar v = new ExprVar(NodeFactory.createVariable("v"));
        Expr expr = new E_LessThan(one, v);
        ElementFilter filter = new ElementFilter(expr);

        WhereValidator visitor = new WhereValidator(filter);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        Node literal = NodeFactory.createLiteralByValue(10);
        builder.setVar(Var.alloc("v"), literal);

        NodeValueInteger lit = new NodeValueInteger(10);
        expr = new E_LessThan(one, lit);
        filter = new ElementFilter(expr);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testSetVarsInOptional() {
        Var v = Var.alloc("v");
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause
                .addOptional(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), v));

        ElementPathBlock epb = new ElementPathBlock();
        ElementOptional optional = new ElementOptional(epb);
        TriplePath tp = new TriplePath(NodeFactory.createURI("one"), new P_Link(NodeFactory.createURI("two")), v);
        epb.addTriple(tp);

        WhereValidator visitor = new WhereValidator(optional);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        builder.setVar(v, NodeFactory.createURI("three"));

        epb = new ElementPathBlock();
        optional = new ElementOptional(epb);
        Triple t = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createURI("three"));
        epb.addTriple(t);

        visitor = new WhereValidator(optional);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testSetVarsInOptional_Node_Variable() {
        Node v = NodeFactory.createVariable("v");
        Node one = NodeFactory.createURI("one");
        Node two = NodeFactory.createURI("two");
        Node three = NodeFactory.createURI("three");

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addOptional(Triple.create(one, two, v));

        ElementPathBlock epb = new ElementPathBlock();
        ElementOptional optional = new ElementOptional(epb);
        TriplePath tp = new TriplePath(Triple.create(one, two, Var.alloc(v)));
        epb.addTriplePath(tp);

        WhereValidator visitor = new WhereValidator(optional);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        builder.setVar(v, three);
        epb = new ElementPathBlock();
        optional = new ElementOptional(epb);
        tp = new TriplePath(Triple.create(one, two, three));
        epb.addTriplePath(tp);

        visitor = new WhereValidator(optional);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testSetVarsInSubQuery() {
        Var v = Var.alloc("v");
        SelectBuilder sb = new SelectBuilder();
        sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addSubQuery(sb);

        Query subQuery = new Query();
        subQuery.setQuerySelectType();
        subQuery.setQueryResultStar(true);
        ElementSubQuery esq = new ElementSubQuery(subQuery);
        ElementPathBlock epb = new ElementPathBlock();
        subQuery.setQueryPattern(epb);
        Triple t = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createVariable("v"));
        epb.addTriple(t);

        WhereValidator visitor = new WhereValidator(esq);
        Query result = builder.build();
        result.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        builder.setVar(v, NodeFactory.createURI("three"));

        subQuery = new Query();
        subQuery.setQuerySelectType();
        subQuery.setQueryResultStar(true);
        esq = new ElementSubQuery(subQuery);
        epb = new ElementPathBlock();
        subQuery.setQueryPattern(epb);
        t = Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three"));
        epb.addTriple(t);

        visitor = new WhereValidator(esq);
        result = builder.build();
        result.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testSetVarsInSubQuery_Node_Variable() {
        Node v = NodeFactory.createVariable("v");
        SelectBuilder sb = new SelectBuilder();
        sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addSubQuery(sb);

        Query subQuery = new Query();
        subQuery.setQuerySelectType();
        subQuery.setQueryResultStar(true);
        ElementSubQuery esq = new ElementSubQuery(subQuery);
        ElementPathBlock epb = new ElementPathBlock();
        subQuery.setQueryPattern(epb);
        TriplePath tp = new TriplePath(NodeFactory.createURI("one"), new P_Link(NodeFactory.createURI("two")),
                NodeFactory.createVariable("v"));
        epb.addTriple(tp);

        WhereValidator visitor = new WhereValidator(esq);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        builder.setVar(v, NodeFactory.createURI("three"));

        subQuery = new Query();
        subQuery.setQuerySelectType();
        subQuery.setQueryResultStar(true);
        esq = new ElementSubQuery(subQuery);
        epb = new ElementPathBlock();
        subQuery.setQueryPattern(epb);
        tp = new TriplePath(NodeFactory.createURI("one"), new P_Link(NodeFactory.createURI("two")),
                NodeFactory.createURI("three"));
        epb.addTriple(tp);

        visitor = new WhereValidator(esq);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testSetVarsInUnion() {
        Var v = Var.alloc("v");
        SelectBuilder sb1 = new SelectBuilder().addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
        WhereClause<?> whereClause = getProducer().newInstance();
        whereClause.addUnion(sb1);
        SelectBuilder sb2 = new SelectBuilder().addWhere("<uno>", "<dos>", "<tres>");
        AbstractQueryBuilder<?> builder = whereClause.addUnion(sb2);
        Query query = builder.build();

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
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        builder.setVar(v, NodeFactory.createURI("three"));
        query = builder.build();

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
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testSetVarsInUnion_Node_Variable() {
        Node v = NodeFactory.createVariable("v");
        SelectBuilder sb1 = new SelectBuilder().addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
        WhereClause<?> whereClause = getProducer().newInstance();
        whereClause.addUnion(sb1);
        SelectBuilder sb2 = new SelectBuilder().addWhere("<uno>", "<dos>", "<tres>");
        AbstractQueryBuilder<?> builder = whereClause.addUnion(sb2);
        Query query = builder.build();

        Node one = NodeFactory.createURI("one");
        Node two = NodeFactory.createURI("two");
        Node three = NodeFactory.createURI("three");
        Node uno = NodeFactory.createURI("uno");
        Node dos = NodeFactory.createURI("dos");
        Node tres = NodeFactory.createURI("tres");

        ElementUnion union = new ElementUnion();
        ElementPathBlock epb = new ElementPathBlock();
        Triple t = Triple.create(one, two, Var.alloc(v));
        epb.addTriple(t);
        union.addElement(epb);
        ElementPathBlock epb2 = new ElementPathBlock();
        t = Triple.create(uno, dos, tres);
        epb2.addTriple(t);
        union.addElement(epb2);
        WhereValidator visitor = new WhereValidator(union);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        builder.setVar(v, NodeFactory.createURI("three"));
        query = builder.build();

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
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testBindStringVar() {
        Var v = Var.alloc("foo");
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addBind("rand()", v);
        Query query = builder.build();

        ElementBind bind = new ElementBind(v, new E_Random());
        WhereValidator visitor = new WhereValidator(bind);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        Node three = NodeFactory.createURI("three");
        builder.setVar(v, three);
        query = builder.build();

        visitor = new WhereValidator(new ElementTriplesBlock());
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testBindStringVar_Node_Variable() {
        Node v = NodeFactory.createVariable("foo");
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addBind("rand()", v);
        Query query = builder.build();

        ElementBind bind = new ElementBind(Var.alloc(v), new E_Random());
        WhereValidator visitor = new WhereValidator(bind);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        Node three = NodeFactory.createURI("three");
        builder.setVar(v, three);
        query = builder.build();

        visitor = new WhereValidator(new ElementTriplesBlock());
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testBindExprVar() {
        Var v = Var.alloc("foo");
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addBind(new E_Random(), v);
        Query query = builder.build();

        WhereValidator visitor = new WhereValidator(new ElementBind(Var.alloc("foo"), new E_Random()));
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        builder.setVar(v, NodeFactory.createURI("three"));
        query = builder.build();

        visitor = new WhereValidator(new ElementTriplesBlock());
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testBindExprVar_Node_Variable() {
        Node v = NodeFactory.createVariable("foo");
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addBind(new E_Random(), v);
        Query query = builder.build();

        WhereValidator visitor = new WhereValidator(new ElementBind(Var.alloc("foo"), new E_Random()));
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

        builder.setVar(v, NodeFactory.createURI("three"));
        query = builder.build();

        visitor = new WhereValidator(new ElementTriplesBlock());
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @SuppressWarnings("deprecation")
    @ContractTest
    public void testList() {
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addWhere(whereClause.list("<one>", "?two", "'three'"), "<foo>",
                "<bar>");

        Query query = builder.build();

        Node one = NodeFactory.createURI("one");
        Var two = Var.alloc("two");
        Node three = NodeFactory.createLiteralString("three");
        Node foo = NodeFactory.createURI("foo");
        Node bar = NodeFactory.createURI("bar");

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
        epb.addTriplePath(new TriplePath(Triple.create(firstObject, foo, bar)));

        WhereValidator visitor = new WhereValidator(epb);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);

    }

    @ContractTest
    public void testAddMinus() {
        SelectBuilder sb = new SelectBuilder();
        sb.addPrefix("pfx", "uri").addVar("?x").addWhere("<one>", "<two>", "three");
        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addMinus(sb);

        Query query = builder.build();

        ElementPathBlock epb = new ElementPathBlock();
        ElementMinus minus = new ElementMinus(epb);
        epb.addTriplePath(new TriplePath(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createLiteralString("three"))));
        WhereValidator visitor = new WhereValidator(minus);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddGraphAbstractQueryBuilder() {
        WhereClause<?> whereClause = getProducer().newInstance();
        TriplePath tp = new TriplePath(Triple.create(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three")));

        TestAbstractQueryBuilder abstractQueryBuilder = new TestAbstractQueryBuilder();
        abstractQueryBuilder.getHandlerBlock().getWhereHandler().addWhere(tp);
        AbstractQueryBuilder<?> builder = whereClause.addGraph( "<g>", abstractQueryBuilder);
        
        ElementPathBlock epb = new ElementPathBlock();
        ElementNamedGraph eng = new ElementNamedGraph(NodeFactory.createURI("g"), epb);
        
        epb.addTriplePath(tp);
        WhereValidator visitor = new WhereValidator(eng);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }
    
    @ContractTest
    public void testAddGraphFrontsTriple() {
        final Node s = NodeFactory.createURI("s");
        final Node p = NodeFactory.createURI("p");
        final Node o = NodeFactory.createURI("o");

        FrontsTriple ft = new FrontsTriple() {

            @Override
            public Triple asTriple() {
                return Triple.create(s, p, o);
            }
        };

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addGraph("<g>", ft);
        Query query = builder.build();

        ElementPathBlock epb = new ElementPathBlock();
        ElementNamedGraph eng = new ElementNamedGraph(NodeFactory.createURI("g"), epb);
        epb.addTriplePath(new TriplePath(Triple.create(s, p, o)));

        WhereValidator visitor = new WhereValidator(eng);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    public void addGraph_GSPO() {
        final Node s = NodeFactory.createURI("s");
        final Node p = NodeFactory.createURI("p");
        final Node o = NodeFactory.createURI("o");

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addGraph("<g>", s, p, o);
        Query query = builder.build();

        ElementPathBlock epb = new ElementPathBlock();
        ElementNamedGraph eng = new ElementNamedGraph(NodeFactory.createURI("g"), epb);
        epb.addTriplePath(new TriplePath(Triple.create(s, p, o)));

        WhereValidator visitor = new WhereValidator(eng);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddGraphTriple() {
        final Node s = NodeFactory.createURI("s");
        final Node p = NodeFactory.createURI("p");
        final Node o = NodeFactory.createURI("o");

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addGraph("<g>", Triple.create(s, p, o));
        Query query = builder.build();

        ElementPathBlock epb = new ElementPathBlock();
        ElementNamedGraph eng = new ElementNamedGraph(NodeFactory.createURI("g"), epb);
        epb.addTriplePath(new TriplePath(Triple.create(s, p, o)));

        WhereValidator visitor = new WhereValidator(eng);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddGraphTriplePath() {
        final Node s = NodeFactory.createURI("s");
        final Node p = NodeFactory.createURI("p");
        final Node o = NodeFactory.createURI("o");
        TriplePath tp = new TriplePath(Triple.create(s, p, o));

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addGraph("<g>", tp);
        Query query = builder.build();

        ElementPathBlock epb = new ElementPathBlock();
        ElementNamedGraph eng = new ElementNamedGraph(NodeFactory.createURI("g"), epb);
        epb.addTriplePath(new TriplePath(Triple.create(s, p, o)));

        WhereValidator visitor = new WhereValidator(eng);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }
    
    @ContractTest
    public void testAddGraphTriplePathCollection() {
        WhereClause<?> whereClause = getProducer().newInstance();
        PrefixMapping pmap = new PrefixMappingImpl();
        pmap.setNsPrefix("ts", "urn:test:");
        Path path = PathParser.parse("ts:two/ts:dos", pmap);
        TriplePath first = new TriplePath(NodeFactory.createURI("one"), path, NodeFactory.createURI("three"));
        TriplePath second = new TriplePath(NodeFactory.createURI("for"), path, NodeFactory.createURI("six"));

        AbstractQueryBuilder<?> builder = whereClause
                .addGraph("<g>", List.of(first, second));

        ElementPathBlock epb = new ElementPathBlock();
        ElementNamedGraph eng = new ElementNamedGraph(NodeFactory.createURI("g"), epb);
        epb.addTriplePath(first);
        epb.addTriplePath(second);

        WhereValidator visitor = new WhereValidator(eng);
        builder.build().getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddWhereValueVar_var() {

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addWhereValueVar("?v");

        Query query = builder.build();

        ElementData edat = new ElementData();
        edat.add(Var.alloc("v"));

        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddWhereValueVar_var_values() {

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addWhereValueVar("?v", "<one>");

        Query query = builder.build();

        final Var v = Var.alloc("v");
        Binding binding = BindingFactory.binding(v, NodeFactory.createURI("one"));
        ElementData edat = new ElementData();
        edat.add(v);
        edat.add(binding);
        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddWhereValueVars() {
        final Var v = Var.alloc("v");
        Map<Object, List<?>> map = new LinkedHashMap<Object, List<?>>();

        // Order matters for the test
        map.put(Var.alloc("x"), Arrays.asList("three", "four"));
        map.put(Var.alloc("v"), Arrays.asList("<one>", "<two>"));

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addWhereValueVars(map);

        Query query = builder.build();

        Var x = Var.alloc("x");
        ElementData edat = new ElementData();
        edat.add(x);
        edat.add(v);
        setupBindings(edat, x, v);

        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    private static void setupBindings(ElementData edat, Var x, Var v) {
        Binding binding1 = BindingFactory.binding(v, NodeFactory.createURI("one"), x,
                NodeFactory.createLiteralString("three"));
        edat.add(binding1);
        Binding binding2 = BindingFactory.binding(v, NodeFactory.createURI("two"), x,
                NodeFactory.createLiteralString("four"));
        edat.add(binding2);
    }

    @ContractTest
    public void testAddWhereValueVars_InSubQuery() {
        final Var v = Var.alloc("v");
        Map<Object, List<?>> map = new LinkedHashMap<Object, List<?>>();

        // Order matters for the test
        map.put("?x", Arrays.asList("three", "four"));
        map.put(Var.alloc("v"), Arrays.asList("<one>", "<two>"));

        WhereClause<?> whereClause = getProducer().newInstance();
        WhereClause<?> whereClause2 = getProducer().newInstance();

        AbstractQueryBuilder<?> builder = whereClause.addWhereValueVars(map);
        builder = whereClause2.addSubQuery(builder);

        Query query = builder.build();

        Var x = Var.alloc("x");
        ElementData edat = new ElementData();
        edat.add(x);
        edat.add(v);
        setupBindings(edat, x, v);

        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddWhereValueVars_Node_Variable() {

        Map<Object, List<?>> map = new LinkedHashMap<Object, List<?>>();

        // Order matters for the test
        map.put("?x", Arrays.asList("three", "four"));
        map.put(NodeFactory.createVariable("v"), Arrays.asList("<one>", "<two>"));

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addWhereValueVars(map);

        Query query = builder.build();

        Var x = Var.alloc("x");
        Var v = Var.alloc("v");
        ElementData edat = new ElementData();
        edat.add(x);
        edat.add(v);
        setupBindings(edat, x, v);

        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddWhereValueRow_array() {
        final Var v = Var.alloc("v");
        final Var x = Var.alloc("x");

        WhereClause<?> whereClause = getProducer().newInstance();
        whereClause = (WhereClause<?>) whereClause.addWhereValueVar(v);
        whereClause = (WhereClause<?>) whereClause.addWhereValueVar(x);
        whereClause = (WhereClause<?>) whereClause.addWhereValueRow("<one>", "three");
        AbstractQueryBuilder<?> builder = whereClause.addWhereValueRow("<two>", "four");

        Query query = builder.build();

        ElementData edat = new ElementData();
        edat.add(v);
        edat.add(x);
        setupBindings(edat, x, v);

        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddWhereValueRow_array_Node_Variable() {

        WhereClause<?> whereClause = getProducer().newInstance();
        whereClause = (WhereClause<?>) whereClause.addWhereValueVar(NodeFactory.createVariable("v"));
        whereClause = (WhereClause<?>) whereClause.addWhereValueVar(NodeFactory.createVariable("x"));
        whereClause = (WhereClause<?>) whereClause.addWhereValueRow("<one>", "three");
        AbstractQueryBuilder<?> builder = whereClause.addWhereValueRow("<two>", "four");

        Query query = builder.build();

        ElementData edat = new ElementData();
        final Var v = Var.alloc("v");
        final Var x = Var.alloc("x");
        edat.add(v);
        edat.add(x);
        setupBindings(edat, x, v);

        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddWhereValueRow_collection() {
        final Var v = Var.alloc("v");
        final Var x = Var.alloc("x");

        WhereClause<?> whereClause = getProducer().newInstance();
        whereClause = (WhereClause<?>) whereClause.addWhereValueVar(v);
        whereClause = (WhereClause<?>) whereClause.addWhereValueVar(x);
        whereClause = (WhereClause<?>) whereClause.addWhereValueRow(Arrays.asList("<one>", "three"));
        AbstractQueryBuilder<?> builder = whereClause.addWhereValueRow(Arrays.asList("<two>", "four"));

        Query query = builder.build();

        ElementData edat = new ElementData();
        edat.add(v);
        edat.add(x);
        setupBindings(edat, x, v);

        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testAddWhereValueRow_collection_Node_Variable() {
        WhereClause<?> whereClause = getProducer().newInstance();
        whereClause = (WhereClause<?>) whereClause.addWhereValueVar(NodeFactory.createVariable("v"));
        whereClause = (WhereClause<?>) whereClause.addWhereValueVar(NodeFactory.createVariable("x"));
        whereClause = (WhereClause<?>) whereClause.addWhereValueRow(Arrays.asList("<one>", "three"));
        AbstractQueryBuilder<?> builder = whereClause.addWhereValueRow(Arrays.asList("<two>", "four"));

        Query query = builder.build();

        ElementData edat = new ElementData();
        final Var v = Var.alloc("v");
        final Var x = Var.alloc("x");

        edat.add(v);
        edat.add(x);
        setupBindings(edat, x, v);

        WhereValidator visitor = new WhereValidator(edat);
        query.getQueryPattern().visit(visitor);
        assertTrue(visitor.matching);
    }

    @ContractTest
    public void testSetVarsInWhereValues() {
        Var v = Var.alloc("v");
        Node value = NodeFactory.createLiteralByValue(10);
        Map<Var, Node> values = new HashMap<>();
        values.put(v, value);

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addWhereValueVar("?x", "<one>", "?v");

        builder.setVar(v, value);

        Query query = builder.build();

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

    @ContractTest
    public void testSetVarsInWhereValues_NodeVariable() {
        Node v = NodeFactory.createVariable("v");
        Node value = NodeFactory.createLiteralByValue(10);

        WhereClause<?> whereClause = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = whereClause.addWhereValueVar("?x", "<one>", "?v");

        builder.setVar(v, value);

        Query query = builder.build();

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

    @ContractTest
    public void testDataQuery() {
        // test that the getVars getMap and clear methods work.
        Object o = "?x";

        WhereClause<?> whereClause = getProducer().newInstance();
        whereClause = (WhereClause<?>) whereClause.addWhereValueVar(o);
        whereClause = (WhereClause<?>) whereClause.addWhereValueVar("?y");
        whereClause = (WhereClause<?>) whereClause.addWhereValueRow("foo", "bar");
        whereClause = (WhereClause<?>) whereClause.addWhereValueRow("fu", null);

        assertFalse(whereClause.getWhereValuesVars().isEmpty());
        List<Var> lst = whereClause.getWhereValuesVars();
        assertEquals(2, lst.size());
        assertEquals(Var.alloc("x"), lst.get(0));
        assertEquals(Var.alloc("y"), lst.get(1));

        Map<Var, List<Node>> map = whereClause.getWhereValuesMap();
        assertEquals(2, map.keySet().size());
        List<Node> nodes = map.get(Var.alloc("x"));
        assertEquals(2, nodes.size());
        assertEquals(NodeFactory.createLiteralString("foo"), nodes.get(0));
        assertEquals(NodeFactory.createLiteralString("fu"), nodes.get(1));

        nodes = map.get(Var.alloc("y"));
        assertEquals(2, nodes.size());
        assertEquals(NodeFactory.createLiteralString("bar"), nodes.get(0));
        assertNull(nodes.get(1));

        whereClause.clearWhereValues();

        assertTrue(whereClause.getWhereValuesVars().isEmpty());
        assertTrue(whereClause.getWhereValuesMap().isEmpty());

    }

}
