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
package org.apache.jena.permissions.query;

import org.apache.jena.permissions.Factory;
import org.apache.jena.permissions.MockSecurityEvaluator;
import org.apache.jena.permissions.model.SecuredModel;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataSetTest {
	private Dataset dataset;
	private Model baseModel;
	private MockSecurityEvaluator eval;
	private SecuredModel dftModel;

	@BeforeClass
	public static void setupFactory() {
		SecuredQueryEngineFactory.register();
	}

	@AfterClass
	public static void teardownFactory() {
		SecuredQueryEngineFactory.unregister();
	}

	public void setup() {

		DatasetGraph dsg = TDBFactory.createDatasetGraph();

		dsg.getContext().set(TDB.symUnionDefaultGraph, true);
		Dataset myDataset = DatasetFactory.wrap(dsg);

		baseModel = myDataset.getNamedModel("http://example.com/baseModel");
		baseModel = QueryEngineTest.populateModel(baseModel);

		dftModel = Factory.getInstance(eval, "http://example.com/securedModel", baseModel);

		dataset = DatasetFactory.create();
		dataset.setDefaultModel(dftModel);
	}

	@Test
	public void testOpenQueryType() {
		eval = new MockSecurityEvaluator(true, true, true, true, true, true);

		setup();

		try {
			final String query = "prefix fn: <http://www.w3.org/2005/xpath-functions#>  " + " SELECT ?foo ?bar WHERE "
					+ " { ?foo a <http://example.com/class> ; " + "?bar [] ." + "  } ";
			try( QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
				final ResultSet results = qexec.execSelect();
				int count = 0;
				for (; results.hasNext();) {
					count++;
					results.nextSolution();
				}
				Assert.assertEquals(8, count);
			}
		} finally {
			dataset.close();
		}
	}

	@Test
	public void testRestrictedQueryType() {
		eval = new MockSecurityEvaluator(true, true, true, true, true, true) {

			@Override
			public boolean evaluate(final Object principal, final Action action, final Node graphIRI,
					final Triple triple) {
				if (triple.getSubject().isURI()
						&& triple.getSubject().getURI().equals("http://example.com/resource/1")) {
					return false;
				}
				return super.evaluate(principal, action, graphIRI, triple);
			}

			@Override
			public boolean evaluateAny(Object principal, Set<Action> action, Node graphIRI, Triple triple) {
				if (triple.getSubject().isURI()
						&& triple.getSubject().getURI().equals("http://example.com/resource/1")) {
					return false;
				}
				return super.evaluateAny(principal, action, graphIRI, triple);
			}

		};

		setup();

		try {
			final String query = "prefix fn: <http://www.w3.org/2005/xpath-functions#>  " + " SELECT ?foo ?bar WHERE "
					+ " { ?foo a <http://example.com/class> ; " + "?bar [] ." + "  } ";
			try( QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ) {
				final ResultSet results = qexec.execSelect();
				int count = 0;
				for (; results.hasNext();) {
					count++;
					results.nextSolution();
				}
				Assert.assertEquals(4, count);
			}
		} finally {
			dataset.close();
		}
	}

	@Test
	public void testSelectAllType() {
		eval = new MockSecurityEvaluator(true, true, true, true, true, true) {

			@Override
			public boolean evaluate(final Object principal, final Action action, final Node graphIRI,
					final Triple triple) {
				if (triple.getSubject().isURI()
						&& triple.getSubject().getURI().equals("http://example.com/resource/1")) {
					return false;
				}
				return super.evaluate(principal, action, graphIRI, triple);
			}

			@Override
			public boolean evaluateAny(Object principal, Set<Action> action, Node graphIRI, Triple triple) {
				if (triple.getSubject().isURI()
						&& triple.getSubject().getURI().equals("http://example.com/resource/1")) {
					return false;
				}
				return super.evaluateAny(principal, action, graphIRI, triple);
			}

		};

		setup();

		try {
			String query = "SELECT ?s ?p ?o WHERE " + " { ?s ?p ?o } ";
			try ( QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ) {
				final ResultSet results = qexec.execSelect();
				int count = 0;
				for (; results.hasNext();) {
					count++;
					results.nextSolution();
				}
				// 2x 3 values + type triple
				Assert.assertEquals(8, count);
			}

			query = "SELECT ?g ?s ?p ?o WHERE " + " { GRAPH ?g {?s ?p ?o } }";
			try ( QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ) {
				final ResultSet results = qexec.execSelect();
				int count = 0;
				for (; results.hasNext();) {
					count++;
					results.nextSolution();
				}
				// 2x 3 values + type triple
				// all are in the base graph so no named graphs
				Assert.assertEquals(0, count);
			}
		} finally {
			dataset.close();
		}
	}
}
