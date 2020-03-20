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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Random;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.junit.Before;
import org.junit.Test;

public class SolutionModifierHandlerTest extends AbstractHandlerTest {

	private Query query;
	private SolutionModifierHandler solutionModifier;

	@Before
	public void setup() {
		query = new Query();
		solutionModifier = new SolutionModifierHandler(query);
	}

	@Test
	public void testAddAll() throws ParseException {
		SolutionModifierHandler solutionModifier2 = new SolutionModifierHandler(new Query());
		solutionModifier2.addOrderBy(Var.alloc("orderBy"));
		solutionModifier2.addGroupBy(Var.alloc("groupBy"));
		solutionModifier2.addHaving("?having<10");
		solutionModifier2.setLimit(500);
		solutionModifier2.setOffset(200);

		solutionModifier.addAll(solutionModifier2);

		String[] s = byLine(query.toString());
		assertContainsRegex(GROUP_BY + var("groupBy"), s);
		assertContainsRegex(HAVING + OPEN_PAREN + var("having") + OPT_SPACE + LT + OPT_SPACE + "10" + CLOSE_PAREN, s);
		assertContainsRegex(ORDER_BY + var("orderBy"), s);
		assertContainsRegex(LIMIT + "500", s);
		assertContainsRegex(OFFSET + "200", s);
	}

	@Test
	public void testAll() throws ParseException {
		solutionModifier.addOrderBy(Var.alloc("orderBy"));
		solutionModifier.addGroupBy(Var.alloc("groupBy"));
		solutionModifier.addHaving("SUM(?lprice) > 10");
		solutionModifier.setLimit(500);
		solutionModifier.setOffset(200);

		String[] s = byLine(query.toString());
		assertContainsRegex(GROUP_BY + var("groupBy"), s);
		assertContainsRegex(HAVING + OPEN_PAREN + "SUM" + OPEN_PAREN + var("lprice") + CLOSE_PAREN + OPT_SPACE + GT
				+ "10" + CLOSE_PAREN, s);
		assertContainsRegex(ORDER_BY + var("orderBy"), s);
		assertContainsRegex(LIMIT + "500", s);
		assertContainsRegex(OFFSET + "200", s);

	}

	@Test
	public void testAddOrderBy() {
		solutionModifier.addOrderBy(Var.alloc("orderBy"));
		List<SortCondition> sc = query.getOrderBy();
		assertEquals("Wrong number of conditions", 1, sc.size());
		assertEquals("Wrong value", sc.get(0).expression.asVar(), Var.alloc("orderBy"));

		solutionModifier.addOrderBy(Var.alloc("orderBy2"));
		sc = query.getOrderBy();
		assertEquals("Wrong number of conditions", 2, sc.size());
		assertEquals("Wrong value", sc.get(0).expression.asVar(), Var.alloc("orderBy"));
		assertEquals("Wrong value", sc.get(1).expression.asVar(), Var.alloc("orderBy2"));
	}

	@Test
	public void testAddGroupByVar() {
		solutionModifier.addGroupBy(Var.alloc("groupBy"));
		String[] s = byLine(query.toString());
		assertContainsRegex(GROUP_BY + var("groupBy"), s);

		solutionModifier.addGroupBy(Var.alloc("groupBy2"));
		s = byLine(query.toString());
		assertContainsRegex(GROUP_BY + var("groupBy") + SPACE + var("groupBy2"), s);
	}

	@Test
	public void testAddGroupByExpr() {
		solutionModifier.addGroupBy(new E_Random());
		String[] s = byLine(query.toString());
		assertContainsRegex(GROUP_BY + "rand" + OPEN_PAREN + CLOSE_PAREN, s);

		solutionModifier.addGroupBy(Var.alloc("groupBy2"));
		s = byLine(query.toString());
		assertContainsRegex(GROUP_BY + "rand" + OPEN_PAREN + CLOSE_PAREN + SPACE + var("groupBy2"), s);
	}

	@Test
	public void testAddGroupByVarAndExpr() {
		solutionModifier.addGroupBy(Var.alloc("groupBy"), new E_Random());
		String[] s = byLine(query.toString());
		assertContainsRegex(GROUP_BY + OPEN_PAREN + "rand" + OPEN_PAREN + CLOSE_PAREN + SPACE + "AS" + SPACE
				+ var("groupBy") + CLOSE_PAREN, s);

		solutionModifier.addGroupBy(Var.alloc("groupBy2"));
		s = byLine(query.toString());
		assertContainsRegex(GROUP_BY + OPEN_PAREN + "rand" + OPEN_PAREN + CLOSE_PAREN + SPACE + "AS" + SPACE
				+ var("groupBy") + CLOSE_PAREN + SPACE + var("groupBy2"), s);
	}

	@Test
	public void testAddHavingString() throws ParseException {
		solutionModifier.addHaving("?having<10");
		assertContainsRegex(HAVING + OPEN_PAREN + var("having") + OPT_SPACE + LT + 10 + CLOSE_PAREN, query.toString());

		solutionModifier.addHaving("?having2");
		assertContainsRegex(
				HAVING + OPEN_PAREN + var("having") + OPT_SPACE + LT + 10 + CLOSE_PAREN + OPT_SPACE + var("having2"),
				query.toString());
	}

	@Test
	public void testAddHavingVar() throws ParseException {
		solutionModifier.addHaving(Var.alloc("foo"));
		assertContainsRegex(HAVING + var("foo"), query.toString());

		solutionModifier.addHaving("?having2");
		assertContainsRegex(HAVING + var("foo") + SPACE + var("having2"), query.toString());
	}

	@Test
	public void testAddHavingExpr() throws ParseException {
		solutionModifier.addHaving(new E_Random());
		assertContainsRegex(HAVING + "rand" + OPEN_PAREN + CLOSE_PAREN, query.toString());

		solutionModifier.addHaving("?having2");
		assertContainsRegex(HAVING + "rand" + OPEN_PAREN + CLOSE_PAREN + SPACE + var("having2"), query.toString());
	}

	@Test
	public void testSetLimit() {
		solutionModifier.setLimit(500);
		String[] s = byLine(query.toString());
		assertContainsRegex("LIMIT\\s+500", s);

		solutionModifier.setLimit(200);
		s = byLine(query.toString());
		assertContainsRegex("LIMIT\\s+200", s);

		solutionModifier.setLimit(-1);
		s = byLine(query.toString());
		assertNotContainsRegex("LIMIT.*", s);

	}

	@Test
	public void testSetOffset() {
		solutionModifier.setOffset(500);
		String[] s = byLine(query.toString());
		assertContainsRegex("OFFSET\\s+500", s);

		solutionModifier.setOffset(200);
		s = byLine(query.toString());
		assertContainsRegex("OFFSET\\s+200", s);

		solutionModifier.setOffset(-1);
		s = byLine(query.toString());
		assertNotContainsRegex("OFFSET.*", s);
	}

	@Test
	public void testSetVarsGroupBy() {
		Var v = Var.alloc("v");
		solutionModifier.addGroupBy(v);

		String[] s = byLine(query.toString());
		assertContainsRegex(GROUP_BY + var("v"), s);

		Map<Var, Node> values = new HashMap<>();
		values.put(v, Var.alloc("v2"));
		solutionModifier.setVars(values);
		s = byLine(query.toString());
		assertContainsRegex(GROUP_BY + var("v2"), s);
	}

	@Test
	public void testSetVarsHaving() {
		Var v = Var.alloc("v");
		solutionModifier.addHaving(v);

		String[] s = byLine(query.toString());
		assertContainsRegex(HAVING + var("v"), s);

		Map<Var, Node> values = new HashMap<>();
		values.put(v, Var.alloc("v2"));
		solutionModifier.setVars(values);
		s = byLine(query.toString());
		assertContainsRegex(HAVING + var("v2"), s);
	}

	@Test
	public void testSetVarsOrderBy() {
		Var v = Var.alloc("v");
		solutionModifier.addOrderBy(v);

		String[] s = byLine(query.toString());
		assertContainsRegex(ORDER_BY + var("v"), s);

		Map<Var, Node> values = new HashMap<>();
		values.put(v, Var.alloc("v2"));
		solutionModifier.setVars(values);
		s = byLine(query.toString());
		assertContainsRegex(ORDER_BY + var("v2"), s);
	}

}
