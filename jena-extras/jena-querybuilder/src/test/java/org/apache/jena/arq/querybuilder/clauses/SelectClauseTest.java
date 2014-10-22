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

import static org.junit.Assert.*;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.handlers.SelectHandler;
import org.junit.After;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;
import org.xenei.junit.contract.IProducer;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;

@Contract(SelectClause.class)
public class SelectClauseTest<T extends SelectClause<?>> extends
		AbstractClauseTest {

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
	public final void cleanupSelectClauseTest() {
		getProducer().cleanUp(); // clean up the producer for the next run
	}

	@ContractTest
	public void getSelectHandlerTest() {
		SelectClause<?> selectClause = getProducer().newInstance();
		SelectHandler handler = selectClause.getSelectHandler();
		assertNotNull(handler);
	}

	@ContractTest
	public void setDistinctTest() throws Exception {
		SelectClause<?> selectClause = getProducer().newInstance();
		Query query = getQuery((AbstractQueryBuilder<?>) selectClause);
		assertFalse(query.isDistinct());
		assertFalse(query.isReduced());

		query = getQuery(selectClause.setDistinct(true));
		assertTrue(query.isDistinct());
		assertFalse(query.isReduced());

		query = getQuery(selectClause.setReduced(false));
		assertTrue(query.isDistinct());
		assertFalse(query.isReduced());

		query = getQuery(selectClause.setReduced(true));
		assertFalse(query.isDistinct());
		assertTrue(query.isReduced());

		query = getQuery(selectClause.setDistinct(true));
		assertTrue(query.isDistinct());
		assertFalse(query.isReduced());

		query = getQuery(selectClause.setDistinct(false));
		assertFalse(query.isDistinct());
		assertFalse(query.isReduced());
	}

	@ContractTest
	public void setReducedTest() throws Exception {
		SelectClause<?> selectClause = getProducer().newInstance();
		Query query = getQuery((AbstractQueryBuilder<?>) selectClause);
		assertFalse(query.isDistinct());
		assertFalse(query.isReduced());

		query = getQuery(selectClause.setReduced(true));
		assertFalse(query.isDistinct());
		assertTrue(query.isReduced());

		query = getQuery(selectClause.setDistinct(false));
		assertFalse(query.isDistinct());
		assertTrue(query.isReduced());

		query = getQuery(selectClause.setDistinct(true));
		assertTrue(query.isDistinct());
		assertFalse(query.isReduced());

		query = getQuery(selectClause.setReduced(true));
		assertFalse(query.isDistinct());
		assertTrue(query.isReduced());

		query = getQuery(selectClause.setReduced(false));
		assertFalse(query.isDistinct());
		assertFalse(query.isReduced());
	}

	@ContractTest
	public void testAddVarString() throws Exception {
		Var v = Var.alloc("one");
		SelectClause<?> selectClause = getProducer().newInstance();
		selectClause.addVar("one");
		Query query = getQuery(selectClause.addVar("one"));
		VarExprList expr = query.getProject();
		assertEquals(1, expr.size());
		assertTrue(expr.contains(v));
	}

	@ContractTest
	public void testAddVarNode() throws Exception {
		Var v = Var.alloc("one");
		SelectClause<?> selectClause = getProducer().newInstance();
		selectClause.addVar("one");
		Query query = getQuery(selectClause.addVar(NodeFactory
				.createVariable("one")));
		VarExprList expr = query.getProject();
		assertEquals(1, expr.size());
		assertTrue(expr.contains(v));
	}

	@ContractTest
	public void testAddVarVar() throws Exception {
		Var v = Var.alloc("one");
		SelectClause<?> selectClause = getProducer().newInstance();
		Query query = getQuery(selectClause.addVar(v));
		VarExprList expr = query.getProject();
		assertEquals(1, expr.size());
		assertTrue(expr.contains(v));
	}

	@ContractTest
	public void getVarsTest() {
		SelectClause<?> selectClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = selectClause.addVar(NodeFactory
				.createVariable("foo"));
		String[] s = byLine(builder);
	}

	@ContractTest
	public void testAddVarAsterisk() throws Exception {
		SelectClause<?> selectClause = getProducer().newInstance();
		selectClause.addVar("*");
		Query query = getQuery(selectClause.addVar("*"));
		VarExprList expr = query.getProject();
		assertEquals(0, expr.size());
		assertTrue(query.isQueryResultStar());
	}

}
