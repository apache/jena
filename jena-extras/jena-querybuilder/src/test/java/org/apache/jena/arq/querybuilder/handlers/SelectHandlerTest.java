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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.query.Query ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.E_Random;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.expr.aggregate.AggSum;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.junit.Before;
import org.junit.Test;

public class SelectHandlerTest extends AbstractHandlerTest {

	private SelectHandler handler;
	private Query query;

	@Before
	public void setup() {
		query = new Query();
		AggregationHandler aggHandler = new AggregationHandler(query);
		handler = new SelectHandler(aggHandler);
	}

	@Test
	public void testAddVar() {
		Var v = Var.alloc("one");
		handler.addVar(v);
		VarExprList expr = query.getProject();
		assertEquals(1, expr.size());
		assertTrue(expr.contains(v));
	}

	@Test
	public void testAddVarAsterisk() {
		handler.addVar(null);
		VarExprList expr = query.getProject();
		assertEquals(0, expr.size());
		assertTrue(query.isQueryResultStar());
	}

	@Test
	public void testAddStringVar() {
		Var v = Var.alloc("foo");
		handler.addVar("rand()", v);
		VarExprList expr = query.getProject();
		assertEquals(1, expr.size());
		Expr e = expr.getExpr( Var.alloc( "foo" ));
		assertNotNull( "expression should not be null", e );
		assertTrue( "Should be an E_Random", e instanceof E_Random);
	}

	@Test
	public void testAddStringWithPrefixVar() {
		query.setPrefix( "xsd","http://www.w3.org/2001/XMLSchema#" );
		Var v = Var.alloc("foo");
		handler.addVar("sum(xsd:integer(?V3))", v);
		VarExprList expr = query.getProject();
		assertEquals(1, expr.size());
		Expr e = expr.getExpr( Var.alloc( "foo" ));
		assertNotNull( "expression should not be null", e );
		assertTrue( "Should be an ExprAggregator", e instanceof ExprAggregator);
		assertTrue( "Should contain an AggSum", ((ExprAggregator)e).getAggregator() instanceof AggSum);
	}

	@Test
	public void testAddAggregateStringVar() {
		Var v = Var.alloc("foo");
		handler.addVar("count(*)", v);
		VarExprList expr = query.getProject();
		assertEquals(1, expr.size());
		Expr e = expr.getExpr( Var.alloc( "foo" ));
		assertNotNull( "expression should not be null", e );
		assertTrue( "Should be an ExprAggregator", e instanceof ExprAggregator);
		assertTrue( "Should be AggCount", ((ExprAggregator)e).getAggregator() instanceof AggCount);
	}

	@Test
	public void testAddExprVar() {
		Var v = Var.alloc("foo");
		handler.addVar(new E_Random(), v);
		VarExprList expr = query.getProject();
		assertEquals(1, expr.size());
		Expr e = expr.getExpr( Var.alloc( "foo" ));
		assertNotNull( "expression should not be null", e );
		assertTrue( "Should be an E_Random", e instanceof E_Random);
	}

	@Test
	public void testAddVarAfterAsterisk() {
		handler.addVar(null);
		handler.addVar(Var.alloc("x"));
		VarExprList expr = query.getProject();
		assertEquals(1, expr.size());
		assertFalse(query.isQueryResultStar());
		assertTrue(expr.contains(Var.alloc("x")));
	}

	@Test
	public void testAddVarVar() {
		Var v = Var.alloc("one");
		handler.addVar(v);
		VarExprList expr = query.getProject();
		assertEquals(1, expr.size());
		assertTrue(expr.contains(v));
	}

	@Test
	public void testSetDistinct() {
		assertFalse(query.isDistinct());
		assertFalse(query.isReduced());

		handler.setDistinct(true);
		assertTrue(query.isDistinct());
		assertFalse(query.isReduced());

		handler.setReduced(false);
		assertTrue(query.isDistinct());
		assertFalse(query.isReduced());

		handler.setReduced(true);
		assertFalse(query.isDistinct());
		assertTrue(query.isReduced());

		handler.setDistinct(true);
		assertTrue(query.isDistinct());
		assertFalse(query.isReduced());

		handler.setDistinct(false);
		assertFalse(query.isDistinct());
		assertFalse(query.isReduced());
	}

	@Test
	public void testSetReduced() {
		assertFalse(query.isDistinct());
		assertFalse(query.isReduced());

		handler.setReduced(true);
		assertFalse(query.isDistinct());
		assertTrue(query.isReduced());

		handler.setDistinct(false);
		assertFalse(query.isDistinct());
		assertTrue(query.isReduced());

		handler.setDistinct(true);
		assertTrue(query.isDistinct());
		assertFalse(query.isReduced());

		handler.setReduced(true);
		assertFalse(query.isDistinct());
		assertTrue(query.isReduced());

		handler.setReduced(false);
		assertFalse(query.isDistinct());
		assertFalse(query.isReduced());

	}

	@Test
	public void testAddAllResultStartReduced() {
		AggregationHandler aggHandler = new AggregationHandler(new Query());
		SelectHandler sh = new SelectHandler(aggHandler);
		sh.addVar(null);
		sh.setReduced(true);

		handler.addAll(sh);
		assertTrue(query.isReduced());
		assertTrue(query.isQueryResultStar());
	}

	@Test
	public void testAddAllVarsDistinct() {
		AggregationHandler aggHandler = new AggregationHandler(new Query());
		SelectHandler sh = new SelectHandler(aggHandler);
		sh.addVar(Var.alloc("foo"));
		sh.setDistinct(true);

		handler.addAll(sh);
		// make sure warning does not fire.
		query.setQueryPattern( new ElementGroup() );
		assertTrue(query.isDistinct());
		assertFalse(query.isQueryResultStar());
		assertEquals(1, query.getResultVars().size());
	}

}
