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

import static org.junit.Assert.assertFalse;
import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.clauses.SolutionModifierClause;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Random;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.junit.After;
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
		assertContainsRegex(ORDER_BY + var("foo"), builder.buildString());

		builder = solutionModifier.addOrderBy("bar");
		assertContainsRegex(ORDER_BY + var("foo") + SPACE + var("bar"), builder.buildString());
	}

	@ContractTest
	public void testAddOrderByStringAscending() {
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = solutionModifier.addOrderBy("foo", Order.ASCENDING);
		assertContainsRegex(ORDER_BY + "ASC" + OPEN_PAREN + var("foo") + CLOSE_PAREN, builder.buildString());

		builder = solutionModifier.addOrderBy("bar");
		assertContainsRegex(ORDER_BY + "ASC" + OPEN_PAREN + var("foo") + CLOSE_PAREN + SPACE + var("bar"),
				builder.buildString());
	}

	@ContractTest
	public void testAddOrderByStringDescending() {
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = solutionModifier.addOrderBy("foo", Order.DESCENDING);
		assertContainsRegex(ORDER_BY + "DESC" + OPEN_PAREN + var("foo") + CLOSE_PAREN, builder.buildString());

		builder = solutionModifier.addOrderBy("bar");
		assertContainsRegex(ORDER_BY + "DESC" + OPEN_PAREN + var("foo") + CLOSE_PAREN + SPACE + var("bar"),
				builder.buildString());
	}

	@ContractTest
	public void testAddOrderByExpr() {
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		Expr e = new E_Random();
		AbstractQueryBuilder<?> builder = solutionModifier.addOrderBy(e);
		assertContainsRegex(ORDER_BY + "rand" + OPEN_PAREN + CLOSE_PAREN, builder.buildString());

		builder = solutionModifier.addOrderBy("bar");
		assertContainsRegex(ORDER_BY + "rand" + OPEN_PAREN + CLOSE_PAREN + SPACE + var("bar"), builder.buildString());
	}

	@ContractTest
	public void testAddOrderByExprAscending() {
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		Expr e = new E_Random();
		AbstractQueryBuilder<?> builder = solutionModifier.addOrderBy(e, Order.ASCENDING);
		assertContainsRegex(ORDER_BY + "ASC" + OPEN_PAREN + "rand" + OPEN_PAREN + CLOSE_PAREN + CLOSE_PAREN,
				builder.buildString());

		builder = solutionModifier.addOrderBy("bar");
		assertContainsRegex(
				ORDER_BY + "ASC" + OPEN_PAREN + "rand" + OPEN_PAREN + CLOSE_PAREN + CLOSE_PAREN + SPACE + var("bar"),
				builder.buildString());
	}

	@ContractTest
	public void testAddOrderByExprDescending() {
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		Expr e = new E_Random();
		AbstractQueryBuilder<?> builder = solutionModifier.addOrderBy(e, Order.DESCENDING);
		assertContainsRegex(ORDER_BY + "DESC" + OPEN_PAREN + "rand" + OPEN_PAREN + CLOSE_PAREN + CLOSE_PAREN,
				builder.buildString());

		builder = solutionModifier.addOrderBy("bar");
		assertContainsRegex(
				ORDER_BY + "DESC" + OPEN_PAREN + "rand" + OPEN_PAREN + CLOSE_PAREN + CLOSE_PAREN + SPACE + var("bar"),
				builder.buildString());
	}

	@ContractTest
	public void testAddGroupByString() {
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = solutionModifier.addGroupBy("foo");
		assertContainsRegex(GROUP_BY + var("foo"), builder.buildString());

		builder = solutionModifier.addGroupBy("bar");
		assertContainsRegex(GROUP_BY + var("foo") + SPACE + var("bar"), builder.buildString());
	}

	@ContractTest
	public void testAddGroupByExpr() {
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = solutionModifier.addGroupBy(new E_Random());
		assertContainsRegex(GROUP_BY + "rand" + OPEN_PAREN + CLOSE_PAREN, builder.buildString());
		builder = solutionModifier.addGroupBy("bar");
		assertContainsRegex(GROUP_BY + "rand" + OPEN_PAREN + CLOSE_PAREN + SPACE + var("bar"), builder.buildString());
	}

	@ContractTest
	public void testAddGroupByVar() {
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = solutionModifier.addGroupBy(Var.alloc("foo"));
		assertContainsRegex(GROUP_BY + var("foo"), builder.buildString());

		builder = solutionModifier.addGroupBy("bar");
		assertContainsRegex(GROUP_BY + var("foo") + SPACE + var("bar"), builder.buildString());
	}

	@ContractTest
	public void testAddGroupByVarAndExpr() {
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = solutionModifier.addGroupBy(Var.alloc("foo"), new E_Random());
		assertContainsRegex(GROUP_BY + OPEN_PAREN + "rand" + OPEN_PAREN + CLOSE_PAREN + SPACE + "AS" + SPACE
				+ var("foo") + CLOSE_PAREN, builder.buildString());

		builder = solutionModifier.addGroupBy("bar");
		assertContainsRegex(GROUP_BY + OPEN_PAREN + "rand" + OPEN_PAREN + CLOSE_PAREN + SPACE + "AS" + SPACE
				+ var("foo") + CLOSE_PAREN + SPACE + var("bar"), builder.buildString());
	}

	@ContractTest
	public void testAddHavingString() throws ParseException {
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = solutionModifier.addHaving("?foo<10");
		assertContainsRegex(HAVING + OPEN_PAREN + var("foo") + OPT_SPACE + LT + OPT_SPACE + "10" + CLOSE_PAREN,
				builder.buildString());

		builder = solutionModifier.addHaving("?bar < 10");
		assertContainsRegex(
				HAVING + OPEN_PAREN + var("foo") + OPT_SPACE + LT + OPT_SPACE + "10" + CLOSE_PAREN + OPT_SPACE
						+ OPEN_PAREN + var("bar") + OPT_SPACE + LT + OPT_SPACE + "10" + CLOSE_PAREN,
				builder.buildString());
	}

	@ContractTest
	public void testAddHavingObject() throws ParseException {
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = solutionModifier.addHaving(Var.alloc("foo"));
		assertContainsRegex(HAVING + var("foo"), builder.buildString());

		builder = solutionModifier.addHaving("?having2");
		assertContainsRegex(HAVING + var("foo") + SPACE + var("having2"), builder.buildString());
	}

	@ContractTest
	public void testAddHavingExpr() throws ParseException {
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = solutionModifier.addHaving(new E_Random());
		assertContainsRegex(HAVING + "rand" + OPEN_PAREN + CLOSE_PAREN, builder.buildString());

		solutionModifier.addHaving("?having2");
		assertContainsRegex(HAVING + "rand" + OPEN_PAREN + CLOSE_PAREN + SPACE + var("having2"), builder.buildString());
	}

	@ContractTest
	public void testSetLimit() {
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = solutionModifier.setLimit(500);
		assertContainsRegex("LIMIT\\s+500", builder.buildString());

		builder = solutionModifier.setLimit(200);
		String s = builder.buildString();
		assertContainsRegex(LIMIT + "200", s);
		assertNotContainsRegex(LIMIT + "500", s);

		builder = solutionModifier.setLimit(0);
		assertFalse("Should not contain LIMIT", builder.buildString().contains("LIMIT"));
	}

	@ContractTest
	public void testSetOffset() {
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = solutionModifier.setOffset(500);

		assertContainsRegex(OFFSET + "500", builder.buildString());

		builder = solutionModifier.setOffset(200);

		String s = builder.buildString();
		assertContainsRegex(OFFSET + "200", s);
		assertNotContainsRegex(OFFSET + "500", s);

		builder = solutionModifier.setOffset(0);

		assertFalse("Should not contain OFFSET", builder.buildString().contains("OFFSET"));
	}

	@ContractTest
	public void testSetVarsGroupBy() {
		Var v = Var.alloc("v");
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = solutionModifier.addGroupBy("?v");

		String[] s = byLine(builder);
		assertContainsRegex(GROUP_BY + var("v"), s);

		builder.setVar(v, Var.alloc("v2"));
		s = byLine(builder);
		assertContainsRegex(GROUP_BY + var("v2"), s);
	}

	@ContractTest
	public void testSetVarsHaving() throws ParseException {
		Var v = Var.alloc("v");
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = solutionModifier.addHaving("?v");

		String[] s = byLine(builder);
		assertContainsRegex(HAVING + var("v"), s);

		builder.setVar(v, Var.alloc("v2"));
		s = byLine(builder);
		assertContainsRegex(HAVING + var("v2"), s);
	}

	@ContractTest
	public void testSetVarsOrderBy() {
		Var v = Var.alloc("v");
		SolutionModifierClause<?> solutionModifier = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = solutionModifier.addOrderBy("?v");

		String[] s = byLine(builder);
		assertContainsRegex(ORDER_BY + var("v"), s);

		builder.setVar(v, Var.alloc("v2"));
		s = byLine(builder);
		assertContainsRegex(ORDER_BY + var("v2"), s);
	}

}
