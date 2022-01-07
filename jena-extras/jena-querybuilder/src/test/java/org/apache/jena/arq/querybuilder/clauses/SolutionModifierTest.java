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

import java.util.List;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_Random;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueInteger;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.junit.After;
import org.junit.Assert;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;
import org.xenei.junit.contract.IProducer;

@Contract(SolutionModifierClause.class)
public class SolutionModifierTest<T extends SolutionModifierClause<?>> extends AbstractClauseTest {

    // the producer we will user
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
    public final void cleanupDatasetClauseTest() {
        getProducer().cleanUp(); // clean up the producer for the next run
    }

    @ContractTest
    public void testAddOrderByString() {
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addOrderBy("foo");

        List<SortCondition> lst = builder.build().getOrderBy();
        Assert.assertEquals(1, lst.size());
        Assert.assertEquals(new SortCondition(new ExprVar(Var.alloc("foo")), Query.ORDER_DEFAULT), lst.get(0));

        builder = solutionModifier.addOrderBy("bar");
        lst = builder.build().getOrderBy();
        Assert.assertEquals(2, lst.size());
        Assert.assertEquals(new SortCondition(new ExprVar(Var.alloc("foo")), Query.ORDER_DEFAULT), lst.get(0));
        Assert.assertEquals(new SortCondition(new ExprVar(Var.alloc("bar")), Query.ORDER_DEFAULT), lst.get(1));

    }

    @ContractTest
    public void testAddOrderByStringAscending() {
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addOrderBy("foo", Order.ASCENDING);

        List<SortCondition> lst = builder.build().getOrderBy();
        Assert.assertEquals(1, lst.size());
        Assert.assertEquals(new SortCondition(new ExprVar(Var.alloc("foo")), Query.ORDER_ASCENDING), lst.get(0));

        builder = solutionModifier.addOrderBy("bar");
        lst = builder.build().getOrderBy();
        Assert.assertEquals(2, lst.size());
        Assert.assertEquals(new SortCondition(new ExprVar(Var.alloc("foo")), Query.ORDER_ASCENDING), lst.get(0));
        Assert.assertEquals(new SortCondition(new ExprVar(Var.alloc("bar")), Query.ORDER_DEFAULT), lst.get(1));

    }

    @ContractTest
    public void testAddOrderByStringDescending() {
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addOrderBy("foo", Order.DESCENDING);

        List<SortCondition> lst = builder.build().getOrderBy();
        Assert.assertEquals(1, lst.size());
        Assert.assertEquals(new SortCondition(new ExprVar(Var.alloc("foo")), Query.ORDER_DESCENDING), lst.get(0));

        builder = solutionModifier.addOrderBy("bar");
        lst = builder.build().getOrderBy();
        Assert.assertEquals(2, lst.size());
        Assert.assertEquals(new SortCondition(new ExprVar(Var.alloc("foo")), Query.ORDER_DESCENDING), lst.get(0));
        Assert.assertEquals(new SortCondition(new ExprVar(Var.alloc("bar")), Query.ORDER_DEFAULT), lst.get(1));
    }

    @ContractTest
    public void testAddOrderByExpr() {
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        Expr e = new E_Random();
        AbstractQueryBuilder<?> builder = solutionModifier.addOrderBy(e);
        
        Query query = builder.build();
        assertTrue( query.hasOrderBy() );
        List<SortCondition> orderBy = query.getOrderBy();
        assertEquals( 1, orderBy.size() );
        assertEquals( Query.ORDER_DEFAULT, orderBy.get(0).getDirection() );
        assertEquals( e, orderBy.get(0).getExpression());
        
        builder = solutionModifier.addOrderBy("bar");

        query = builder.build();
        assertTrue( query.hasOrderBy() );
        orderBy = query.getOrderBy();
        assertEquals( 2, orderBy.size() );
        assertEquals( Query.ORDER_DEFAULT, orderBy.get(0).getDirection() );
        assertEquals( e, orderBy.get(0).getExpression());
        assertEquals( Query.ORDER_DEFAULT, orderBy.get(1).getDirection() );
        assertEquals( new ExprVar("bar"), orderBy.get(1).getExpression());
    }

    @ContractTest
    public void testAddOrderByExprAscending() {
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        Expr e = new E_Random();
        AbstractQueryBuilder<?> builder = solutionModifier.addOrderBy(e, Order.ASCENDING);
        
        Query query = builder.build();
        assertTrue( query.hasOrderBy() );
        List<SortCondition> orderBy = query.getOrderBy();
        assertEquals( 1, orderBy.size() );
        assertEquals( Query.ORDER_ASCENDING, orderBy.get(0).getDirection() );
        assertEquals( e, orderBy.get(0).getExpression());

        builder = solutionModifier.addOrderBy("bar");

        query = builder.build();
        assertTrue( query.hasOrderBy() );
        orderBy = query.getOrderBy();
        assertEquals( 2, orderBy.size() );
        assertEquals( Query.ORDER_ASCENDING, orderBy.get(0).getDirection() );
        assertEquals( e, orderBy.get(0).getExpression());
        assertEquals( Query.ORDER_DEFAULT, orderBy.get(1).getDirection() );
        assertEquals( new ExprVar("bar"), orderBy.get(1).getExpression());
    }

    @ContractTest
    public void testAddOrderByExprDescending() {
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        Expr e = new E_Random();
        AbstractQueryBuilder<?> builder = solutionModifier.addOrderBy(e, Order.DESCENDING);
        
        
        Query query = builder.build();
        assertTrue( query.hasOrderBy() );
        List<SortCondition> orderBy = query.getOrderBy();
        assertEquals( 1, orderBy.size() );
        assertEquals( Query.ORDER_DESCENDING, orderBy.get(0).getDirection() );
        assertEquals( e, orderBy.get(0).getExpression());

        builder = solutionModifier.addOrderBy("bar");
        
        query = builder.build();
        assertTrue( query.hasOrderBy() );
        orderBy = query.getOrderBy();
        assertEquals( 2, orderBy.size() );
        assertEquals( Query.ORDER_DESCENDING, orderBy.get(0).getDirection() );
        assertEquals( e, orderBy.get(0).getExpression());
        assertEquals( Query.ORDER_DEFAULT, orderBy.get(1).getDirection() );
        assertEquals( new ExprVar("bar"), orderBy.get(1).getExpression());
    }

    @ContractTest
    public void testAddGroupByString() {
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addGroupBy("foo");
        
        Query query = builder.build();
        assertTrue( query.hasGroupBy() );
        VarExprList groupBy = query.getGroupBy();
        assertEquals( 1, groupBy.size() );
        List<Var> vars = groupBy.getVars();
        assertEquals( 1, vars.size() );
        assertEquals( Var.alloc("foo" ), vars.get(0));
        assertNull( groupBy.getExpr( vars.get(0) ));

        builder = solutionModifier.addGroupBy("bar");

        query = builder.build();
        assertTrue( query.hasGroupBy() );
        groupBy = query.getGroupBy();
        assertEquals( 2, groupBy.size() );
        vars = groupBy.getVars();
        assertEquals( 2, vars.size() );
        assertEquals( Var.alloc("foo" ), vars.get(0));
        assertNull( groupBy.getExpr( vars.get(0) ));
        assertEquals( Var.alloc("bar" ), vars.get(1));
        assertNull( groupBy.getExpr( vars.get(1) ));
    }

    @ContractTest
    public void testAddGroupByExpr() {
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addGroupBy(new E_Random());
        
        Query query = builder.build();
        assertTrue( query.hasGroupBy() );
        VarExprList groupBy = query.getGroupBy();
        assertEquals( 1, groupBy.size() );
        List<Var> vars = groupBy.getVars();
        assertEquals( 1, vars.size() );
        assertEquals( new E_Random(), groupBy.getExpr( vars.get(0) ));

        builder = solutionModifier.addGroupBy("bar");
        
        query = builder.build();
        assertTrue( query.hasGroupBy() );
        groupBy = query.getGroupBy();
        assertEquals( 2, groupBy.size() );
        vars = groupBy.getVars();
        assertEquals( 2, vars.size() );
        assertEquals( new E_Random(), groupBy.getExpr( vars.get(0) ));
        assertEquals( Var.alloc("bar" ), vars.get(1));
        assertNull( groupBy.getExpr( vars.get(1) ));
    }

    @ContractTest
    public void testAddGroupByVar() {
        Var foo = Var.alloc("foo");
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addGroupBy(foo);

        VarExprList groupBy = builder.build().getGroupBy();
        Assert.assertEquals(1, groupBy.size());
        Assert.assertEquals(foo, groupBy.getVars().get(0));
        Assert.assertNull(groupBy.getExpr(foo));

        builder = solutionModifier.addGroupBy("bar");
        groupBy = builder.build().getGroupBy();
        Assert.assertEquals(2, groupBy.size());
        Assert.assertEquals(foo, groupBy.getVars().get(0));
        Assert.assertNull(groupBy.getExpr(foo));

        Assert.assertEquals(Var.alloc("bar"), groupBy.getVars().get(1));
        Assert.assertNull(groupBy.getExpr(Var.alloc("bar")));

    }

    @ContractTest
    public void testAddGroupByVarAndExpr() {
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addGroupBy(Var.alloc("foo"), new E_Random());
        
        Query query = builder.build();
        assertTrue( query.hasGroupBy() );
        VarExprList groupBy = query.getGroupBy();
        assertEquals( 1, groupBy.size() );
        List<Var> vars = groupBy.getVars();
        assertEquals( 1, vars.size() );
        assertEquals( Var.alloc("foo" ), vars.get(0));
        assertEquals( new E_Random(), groupBy.getExpr( vars.get(0) ));

        builder = solutionModifier.addGroupBy("bar");
        
        query = builder.build();
        assertTrue( query.hasGroupBy() );
        groupBy = query.getGroupBy();
        assertEquals( 2, groupBy.size() );
        vars = groupBy.getVars();
        assertEquals( 2, vars.size() );
        assertEquals( Var.alloc("foo" ), vars.get(0));
        assertEquals( new E_Random(), groupBy.getExpr( vars.get(0) ));
        assertEquals( Var.alloc("bar" ), vars.get(1));
        assertNull( groupBy.getExpr( vars.get(1) ));

    }

    @ContractTest
    public void testAddHavingString() throws ParseException {
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addHaving("?foo<10");

        Query query = builder.build();
        assertTrue( query.hasHaving() );
        List<Expr> having = query.getHavingExprs();
        assertEquals( 1, having.size() );
        Expr exp = having.get(0);
        assertTrue( exp.isFunction() );
        assertTrue( exp.getFunction() instanceof E_LessThan  );
        List<Expr> args = exp.getFunction().getArgs();
        assertEquals( new ExprVar( "foo"), args.get(0));
        assertEquals( new NodeValueInteger(10), args.get(1));

        builder = solutionModifier.addHaving("?bar < 10");
        
        query = builder.build();
        assertTrue( query.hasHaving() );
        having = query.getHavingExprs();
        assertEquals( 2, having.size() );
        exp = having.get(0);
        assertTrue( exp.isFunction() );
        assertTrue( exp.getFunction() instanceof E_LessThan  );
        args = exp.getFunction().getArgs();
        assertEquals( new ExprVar( "foo"), args.get(0));
        assertEquals( new NodeValueInteger(10), args.get(1));
        
        exp = having.get(1);
        assertTrue( exp.isFunction() );
        assertTrue( exp.getFunction() instanceof E_LessThan  );
        args = exp.getFunction().getArgs();
        assertEquals( new ExprVar( "bar"), args.get(0));
        assertEquals( new NodeValueInteger(10), args.get(1));
    }

    @ContractTest
    public void testAddHavingObject() throws ParseException {
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addHaving(Var.alloc("foo"));
        
        Query query = builder.build();
        assertTrue( query.hasHaving() );
        List<Expr> having = query.getHavingExprs();
        assertEquals( 1, having.size() );
        Expr exp = having.get(0);
        assertTrue( exp.isVariable() );
        assertEquals( new ExprVar( "foo"), exp.getExprVar());

        builder = solutionModifier.addHaving("?having2");
        
        query = builder.build();
        assertTrue( query.hasHaving() );
        having = query.getHavingExprs();
        assertEquals( 2, having.size() );
        exp = having.get(0);
        assertTrue( exp.isVariable() );
        assertEquals( new ExprVar( "foo"), exp.getExprVar());
        exp = having.get(1);
        assertTrue( exp.isVariable() );
        assertEquals( new ExprVar( "having2"), exp.getExprVar());
    }

    @ContractTest
    public void testAddHavingExpr() throws ParseException {
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addHaving(new E_Random());
        
        Query query = builder.build();
        assertTrue( query.hasHaving() );
        List<Expr> having = query.getHavingExprs();
        assertEquals( 1, having.size() );
        Expr exp = having.get(0);
        assertTrue( exp.isFunction() );
        assertTrue( exp.getFunction() instanceof E_Random  );
        
        solutionModifier.addHaving("?having2");
        
        query = builder.build();
        assertTrue( query.hasHaving() );
        having = query.getHavingExprs();
        assertEquals( 2, having.size() );
        exp = having.get(0);
        assertTrue( exp.isFunction() );
        assertTrue( exp.getFunction() instanceof E_Random  );
        exp = having.get(1);
        assertTrue( exp.isVariable() );
        assertEquals( new ExprVar( "having2"), exp.getExprVar());
    }

    @ContractTest
    public void testSetLimit() {
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.setLimit(500);
        
        Query query = builder.build();
        assertTrue( query.hasLimit());
        assertEquals( 500L, query.getLimit() );

        builder = solutionModifier.setLimit(200);
        
        query = builder.build();
        assertTrue( query.hasLimit());
        assertEquals( 200L, query.getLimit() );

        builder = solutionModifier.setLimit(0);
        query = builder.build();
        assertFalse( "Should not contain LIMIT", query.hasLimit());
    }

    @ContractTest
    public void testSetOffset() {
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.setOffset(500);

        Query query = builder.build();
        assertTrue( query.hasOffset());
        assertEquals( 500, query.getOffset());

        builder = solutionModifier.setOffset(200);

        query = builder.build();
        assertTrue( query.hasOffset());
        assertEquals( 200, query.getOffset());

        
        builder = solutionModifier.setOffset(0);
        query = builder.build();
        assertFalse( "Should not contain OFFSET", query.hasOffset());        
    }

    @ContractTest
    public void testSetVarsGroupBy() {
        Var v = Var.alloc("v");
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addGroupBy("?v");

        VarExprList groupBy = builder.build().getGroupBy();
        Assert.assertEquals(1, groupBy.size());
        Assert.assertEquals(Var.alloc("v"), groupBy.getVars().get(0));
        Assert.assertNull(groupBy.getExpr(Var.alloc("v")));

        builder.setVar(v, Var.alloc("v2"));
        groupBy = builder.build().getGroupBy();
        Assert.assertEquals(1, groupBy.size());
        Assert.assertEquals(Var.alloc("v2"), groupBy.getVars().get(0));
        Assert.assertNull(groupBy.getExpr(Var.alloc("v2")));
        builder.setVar(v, Var.alloc("v2"));

    }

    @ContractTest
    public void testSetVarsGroupBy_Node_Variable() {
        Node v = NodeFactory.createVariable("v");
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addGroupBy(v);

        VarExprList groupBy = builder.build().getGroupBy();
        Assert.assertEquals(1, groupBy.size());
        Assert.assertEquals(Var.alloc("v"), groupBy.getVars().get(0));
        Assert.assertNull(groupBy.getExpr(Var.alloc("v")));

        builder.setVar(v, NodeFactory.createVariable("v2"));
        groupBy = builder.build().getGroupBy();
        Assert.assertEquals(1, groupBy.size());
        Assert.assertEquals(Var.alloc("v2"), groupBy.getVars().get(0));
        Assert.assertNull(groupBy.getExpr(Var.alloc("v2")));

    }

    @ContractTest
    public void testSetVarsHaving() throws ParseException {
        Var v = Var.alloc("v");
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addHaving("?v");

        List<Expr> exprs = builder.build().getHavingExprs();
        Assert.assertEquals(1, exprs.size());
        Assert.assertEquals(new ExprVar(Var.alloc(v)), exprs.get(0));

        builder.setVar(v, Var.alloc("v2"));
        exprs = builder.build().getHavingExprs();
        Assert.assertEquals(1, exprs.size());
        Assert.assertEquals(new ExprVar(Var.alloc("v2")), exprs.get(0));
    }

    @ContractTest
    public void testSetVarsHaving_Node_Variable() throws ParseException {
        Node v = NodeFactory.createVariable("v");
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addHaving(v);

        List<Expr> exprs = builder.build().getHavingExprs();
        Assert.assertEquals(1, exprs.size());
        Assert.assertEquals(new ExprVar(Var.alloc(v)), exprs.get(0));

        builder.setVar(v, Var.alloc("v2"));
        exprs = builder.build().getHavingExprs();
        Assert.assertEquals(1, exprs.size());
        Assert.assertEquals(new ExprVar(Var.alloc("v2")), exprs.get(0));
    }

    @ContractTest
    public void testSetVarsOrderBy() {
        Var v = Var.alloc("v");
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addOrderBy("?v");

        List<SortCondition> lst = builder.build().getOrderBy();
        Assert.assertEquals(1, lst.size());
        Assert.assertEquals(new SortCondition(new ExprVar(Var.alloc(v)), Query.ORDER_DEFAULT), lst.get(0));

        builder.setVar(v, Var.alloc("v2"));
        lst = builder.build().getOrderBy();
        Assert.assertEquals(1, lst.size());
        Assert.assertEquals(new SortCondition(new ExprVar(Var.alloc("v2")), Query.ORDER_DEFAULT), lst.get(0));

    }

    @ContractTest
    public void testSetVarsOrderBy_NodeVariable() {
        Node v = NodeFactory.createVariable("v");
        SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
        AbstractQueryBuilder<?> builder = solutionModifier.addOrderBy(v);

        List<SortCondition> lst = builder.build().getOrderBy();
        Assert.assertEquals(1, lst.size());
        Assert.assertEquals(new SortCondition(new ExprVar(Var.alloc(v)), Query.ORDER_DEFAULT), lst.get(0));

        builder.setVar(v, Var.alloc("v2"));
        lst = builder.build().getOrderBy();
        Assert.assertEquals(1, lst.size());
        Assert.assertEquals(new SortCondition(new ExprVar(Var.alloc("v2")), Query.ORDER_DEFAULT), lst.get(0));
    }
}
