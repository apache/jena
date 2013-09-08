/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.graph;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.Command;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.testing_framework.AbstractGraphProducerUser;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.FileUtils;

import static com.hp.hpl.jena.testing_framework.GraphTestUtils.*;

/**
 * AbstractTestGraph provides a bunch of basic tests for something that purports
 * to be a Graph. The abstract method getGraph must be overridden in subclasses
 * to deliver a Graph of interest.
 */

public abstract class AbstractTransactionHandlerTest extends
		AbstractGraphProducerUser {
	protected Graph graphWithTxn(String s) {
		Graph g = getGraphProducer().newGraph();
		txnBegin(g);
		try {
			graphAdd(g, s);
			txnCommit(g);
		} catch (Exception e) {
			txnRollback(g);
			fail(e.getMessage());
		}
		return g;
	}

	/**
	 * Test that Graphs have transaction support methods, and that if they fail
	 * on some g they fail because they do not support the operation.
	 */
	@Test
	public void testTransactionsExistAsPerTransactionSupported() {
		Command cmd = new Command() {
			@Override
			public Object execute() {
				return null;
			}
		};

		Graph g = getGraphProducer().newGraph();

		TransactionHandler th = g.getTransactionHandler();

		if (th.transactionsSupported()) {
			th.begin();
			th.abort();
			th.begin();
			th.commit();
			th.executeInTransaction(cmd);
		} else {
			try {
				th.begin();
				fail("Should have thrown UnsupportedOperationException");
			} catch (UnsupportedOperationException x) {
			}

			try {
				th.abort();
				fail("Should have thrown UnsupportedOperationException");
			} catch (UnsupportedOperationException x) {
			}
			try {
				th.commit();
				fail("Should have thrown UnsupportedOperationException");
			} catch (UnsupportedOperationException x) {
			}
			/* */
			try {
				th.executeInTransaction(cmd);
				fail("Should have thrown UnsupportedOperationException");
			} catch (UnsupportedOperationException x) {
			}
		}
	}

	@Test
	public void testExecuteInTransactionCatchesThrowable() {
		Graph g = getGraphProducer().newGraph();
		TransactionHandler th = g.getTransactionHandler();
		if (th.transactionsSupported()) {
			Command cmd = new Command() {
				@Override
				public Object execute() throws Error {
					throw new Error();
				}
			};
			try {
				th.executeInTransaction(cmd);
				fail("Should have thrown JenaException");
			} catch (JenaException x) {
			}
		}
	}

	static final Triple[] tripleArray = tripleArray("S P O; A R B; X Q Y");

	static final List<Triple> tripleList = Arrays
			.asList(tripleArray("i lt j; p equals q"));

	static final Triple[] setTriples = tripleArray("scissors cut paper; paper wraps stone; stone breaks scissors");

	static final Set<Triple> tripleSet = CollectionFactory
			.createHashedSet(Arrays.asList(setTriples));

	@Test
	public void testTransactionCommit() {
		Graph g = getGraphProducer().newGraph();
		if (g.getTransactionHandler().transactionsSupported()) {
			Graph initial = graphWithTxn("initial hasValue 42; also hasURI hello");
			Graph extra = graphWithTxn("extra hasValue 17; also hasURI world");

			GraphUtil.addInto(g, initial);
			g.getTransactionHandler().begin();
			GraphUtil.addInto(g, extra);
			g.getTransactionHandler().commit();
			Graph union = graphWithTxn("");
			GraphUtil.addInto(union, initial);
			GraphUtil.addInto(union, extra);
			assertIsomorphic(union, g);
			// Model inFile = ModelFactory.createDefaultModel();
			// inFile.read( "file:///" + foo, "N-TRIPLES" );
			// assertIsomorphic( union, inFile.getGraph() );
		}
	}

	@Test
	public void testTransactionAbort() {
		Graph g = getGraphProducer().newGraph();
		if (g.getTransactionHandler().transactionsSupported()) {
			Graph initial = graphWithTxn("initial hasValue 42; also hasURI hello");
			Graph extra = graphWithTxn("extra hasValue 17; also hasURI world");
			File foo = FileUtils.tempFileName("fileGraph", ".n3");
			// Graph g = new FileGraph( foo, true, true );
			GraphUtil.addInto(g, initial);
			g.getTransactionHandler().begin();
			GraphUtil.addInto(g, extra);
			g.getTransactionHandler().abort();
			assertIsomorphic(initial, g);
		}
	}

	@Test
	public void testTransactionCommitThenAbort() {
		Graph g = getGraphProducer().newGraph();
		if (g.getTransactionHandler().transactionsSupported()) {
			Graph initial = graphWithTxn("A pings B; B pings C");
			Graph extra = graphWithTxn("C pingedBy B; fileGraph rdf:type Graph");
			// Graph g = getGraphProducer().newGraph();
			// File foo = FileUtils.tempFileName( "fileGraph", ".nt" );
			// Graph g = new FileGraph( foo, true, true );
			g.getTransactionHandler().begin();
			GraphUtil.addInto(g, initial);
			g.getTransactionHandler().commit();
			g.getTransactionHandler().begin();
			GraphUtil.addInto(g, extra);
			g.getTransactionHandler().abort();
			assertIsomorphic(initial, g);
			// Model inFile = ModelFactory.createDefaultModel();
			// inFile.read( "file:///" + foo, "N-TRIPLES" );
			// assertIsomorphic( initial, inFile.getGraph() );
		}
	}

}
