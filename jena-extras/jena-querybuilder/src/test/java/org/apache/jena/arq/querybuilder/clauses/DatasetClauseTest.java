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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.handlers.DatasetHandler;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.sparql.core.Var ;
import org.junit.After;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;
import org.xenei.junit.contract.IProducer;

@Contract(DatasetClause.class)
public class DatasetClauseTest<T extends DatasetClause<?>> extends
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
	public final void cleanupDatasetClauseTest() {
		getProducer().cleanUp(); // clean up the producer for the next run
	}

	@ContractTest
	public void testFromNamed() {
		DatasetClause<?> datasetClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = datasetClause.fromNamed("name");
		String[] s = byLine(builder);
		assertContains("FROM NAMED <name>", s);
		builder = datasetClause.fromNamed("name2");
		s = byLine(builder);
		assertContains("FROM NAMED <name>", s);
		assertContains("FROM NAMED <name2>", s);
	}

	@ContractTest
	public void testFromNamedCollection() {
		String[] names = { "name", "name2" };
		DatasetClause<?> datasetClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = datasetClause.fromNamed(Arrays
				.asList(names));
		String[] s = byLine(builder);
		assertContains("FROM NAMED <name>", s);
		assertContains("FROM NAMED <name2>", s);
	}

	@ContractTest
	public void testFrom() {
		DatasetClause<?> datasetClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = datasetClause.from("name");
		String[] s = byLine(builder);
		assertContains("FROM <name>", s);
		builder = datasetClause.from("name2");
		s = byLine(builder);
		assertContains("FROM <name2>", s);
	}

	@ContractTest
	public void testGetDatasetHandler() {
		DatasetClause<?> datasetClause = getProducer().newInstance();
		DatasetHandler dsHandler = datasetClause.getDatasetHandler();
		assertNotNull(dsHandler);
	}

	@ContractTest
	public void testAll() {
		DatasetClause<?> datasetClause = getProducer().newInstance();
		datasetClause.fromNamed("name");
		datasetClause.fromNamed("name2");
		AbstractQueryBuilder<?> builder = datasetClause.from("name3");
		String[] s = byLine(builder);
		assertContains("FROM NAMED <name>", s);
		assertContains("FROM NAMED <name2>", s);
		assertContains("FROM <name3>", s);
	}

	@ContractTest
	public void setVarsFromNamed() {
		DatasetClause<?> datasetClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = datasetClause.fromNamed("?foo");
		datasetClause.from("?bar");
		builder.setVar(Var.alloc("foo"),
				NodeFactory.createURI("http://example.com/foo"));

		String s = builder.buildString();
		assertTrue(s.contains("FROM NAMED <http://example.com/foo>"));
		assertTrue(s.contains("FROM <?bar>"));
	}

	@ContractTest
	public void setVarsFrom() {
		DatasetClause<?> datasetClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = datasetClause.fromNamed("?foo");
		datasetClause.from("?bar");
		builder.setVar(Var.alloc("bar"),
				NodeFactory.createURI("http://example.com/bar"));

		String s = builder.buildString();
		assertTrue(s.contains("FROM NAMED <?foo>"));
		assertTrue(s.contains("FROM <http://example.com/bar>"));
	}

	@ContractTest
	public void setVarsBoth() {
		DatasetClause<?> datasetClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = datasetClause.fromNamed("?foo");
		datasetClause.from("?bar");
		builder.setVar(Var.alloc("bar"),
				NodeFactory.createURI("http://example.com/bar"));
		builder.setVar(Var.alloc("foo"),
				NodeFactory.createURI("http://example.com/foo"));
		String s = builder.buildString();
		assertTrue(s.contains("FROM NAMED <http://example.com/foo>"));
		assertTrue(s.contains("FROM <http://example.com/bar>"));
	}

}
