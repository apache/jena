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

package com.hp.hpl.jena.sparql.graph;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.DynamicDatasets;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class TestDatasets {
		
	private static final String data = "INSERT DATA { <ex:default> <ex:default> <ex:default>.\n"
									   + "GRAPH <ex:from> { <ex:from> <ex:from> <ex:from> }\n"
									   + "GRAPH <ex:named> { <ex:named> <ex:named> <ex:named> }\n"
									   + "GRAPH <ex:other> { <ex:other> <ex:other> <ex:other> }\n"
									   + "}";

	private Dataset ds;
	private GraphStore gs;
	
	@Before
	public void setup() {
		this.ds = DatasetFactory.createMem();
		this.gs = GraphStoreFactory.create(this.ds);
		
		UpdateRequest up = UpdateFactory.create(TestDatasets.data);
		UpdateProcessor processor = UpdateExecutionFactory.create(up, this.gs);
		processor.execute();
	}
	
	private void test(String query, String[] expected, int expectedCount) {
		//Parse the query
		Query q = QueryFactory.create(query);
		
		//Render the dataset appropriately, ARQ doesn't do this for us automatically
		Dataset ds = DynamicDatasets.dynamicDataset(q.getDatasetDescription(), this.ds, false);
		
		//Then execute the query
		QueryExecution exec = QueryExecutionFactory.create(q, ds);
		
		ResultSet results = exec.execSelect();
		List<String> found = new ArrayList<>();
		int count = 0;
		while (results.hasNext()) {
			count++;
			QuerySolution sln = results.next();
			found.add(sln.get("s").toString());
		}
		
		boolean dumped = false;
		if (expectedCount != count) {
			//If incorrect dump output for debugging
			System.out.println(query);
			dump(expectedCount, count, expected, found);
			dumped = true;
		}
		Assert.assertEquals(expectedCount, count);
		for (String e : expected) {
			if (!found.contains(e)) {
				if (!dumped) {
					System.out.println(query);
					dump(expectedCount, count, expected, found);
					dumped = true;
				}
				Assert.fail("Did not find expected result " + e);
			}
		}
	}
	
	private void dump(int expectedCount, int actualCount, String[] expected, List<String> actual) {
		if (expectedCount != actualCount) {
			System.out.println("Got incorrect number of results, expected " + expectedCount + " but got " + actualCount);
		} else {
			System.out.println("Did not find an expected result");
		}
		System.out.print("Expected: ");
		for (String e : expected) {
			System.out.print(e + " ");
		}
		System.out.println();
		System.out.print("Actual: ");
		for (String a : actual) {
			System.out.print(a + " ");
		}
		System.out.println();	
	}
	
	/*
	 * This block of tests are for the case where we have all of FROM, FROM NAMED and GRAPH clause present
	 */
	
	@Test
	public void from_and_named_and_graph_uri_exists() {
		//FROM
		//FROM NAMED
		//GRAPH clause with URI of an existing graph which is in the FROM NAMED list
		
		//Yields triples from <ex:named>
		test("SELECT * FROM <ex:from> FROM NAMED <ex:named> { GRAPH <ex:named> { ?s ?p ?o } }", new String[] { "ex:named" }, 1);
	}
	
	@Test
	public void from_and_named_and_graph_uri_exists_not_in_list() {
		//FROM
		//FROM NAMED
		//GRAPH clause with URI of an existing graph which is NOT in the FROM NAMED list
		
		//Yields no triples, tries to access a named graph not in the named graph list
		test("SELECT * FROM <ex:from> FROM NAMED <ex:named> { GRAPH <ex:other> { ?s ?p ?o } }", new String[] { }, 0);
	}
	
	@Test
	public void from_and_named_and_graph_uri_missing() {
		//FROM
		//FROM NAMED
		//GRAPH clause with URI of an existing graph which is in the FROM NAMED list
		
		//Yields no triples
		test("SELECT * FROM <ex:from> FROM NAMED <ex:named> { GRAPH <ex:missing> { ?s ?p ?o } }", new String[] { }, 0);
	}
	
	@Test
	public void from_and_named_and_graph_var() {
		//FROM
		//FROM NAMED
		//GRAPH clause with variable
		
		//Yields triples from <ex:named>
		test("SELECT * FROM <ex:from> FROM NAMED <ex:named> { GRAPH ?g { ?s ?p ?o } }", new String[] { "ex:named" }, 1);
	}
	
	@Test
	public void from_and_named_and_graphs_var() {
		//FROM
		//FROM NAMED
		//GRAPH clause with variable
		
		//Yields triples from <ex:named>
		test("SELECT * FROM <ex:from> FROM NAMED <ex:named> FROM NAMED <ex:other> { GRAPH ?g { ?s ?p ?o } }", new String[] { "ex:named", "ex:other" }, 2);
	}
	
	/*
	 * This block of tests are for the case where we have FROM and a GRAPH clause present
	 */
	
	@Test
	public void from_and_graph_uri_exists() {
		//FROM
		//No FROM NAMED
		//GRAPH clause with URI of an existing graph
		
		//Yields no triples
		test("SELECT * FROM <ex:from> { GRAPH <ex:named> { ?s ?p ?o } }", new String[] { }, 0);
	}
	
	@Test
	public void from_and_graph_uri_missing() {
		//FROM
		//No FROM NAMED
		//GRAPH clause with URI of an existing graph
		
		//Yields no triples
		test("SELECT * FROM <ex:from> { GRAPH <ex:missing> { ?s ?p ?o } }", new String[] { }, 0);
	}
	
	@Test
	public void from_and_graph_var() {
		//FROM
		//No FROM NAMED
		//GRAPH clause with variable
		
		//Yields no triples
		test("SELECT * FROM <ex:from> { GRAPH ?g { ?s ?p ?o } }", new String[] { }, 0);
	}
	
	/**
	 * This block of tests are for the case where we have FROM NAMED and a GRAPH clause
	 */
	
	@Test
	public void named_graph_var() {
		//No FROM
		//FROM NAMED
		//GRAPH clause with variable
		
		//Yields triples in <ex:named>
		test("SELECT * FROM NAMED <ex:named> WHERE { GRAPH ?g { ?s ?p ?o } }", new String[] { "ex:named" }, 1);
	}
	
	@Test
	public void named_graphs_var() {
		//No FROM
		//FROM NAMED
		//GRAPH clause with variable
		
		//Yields triples in <ex:named> and <ex:other>
		test("SELECT * FROM NAMED <ex:named> FROM NAMED <ex:other> WHERE { GRAPH ?g { ?s ?p ?o } }", new String[] { "ex:named", "ex:other" }, 2);
	}
	
	@Test
	public void named_graph_uri_exists() {
		//No FROM
		//FROM NAMED
		//GRAPH clause with URI of existing graph
		
		//Yields triples in <ex:named>
		test("SELECT * FROM NAMED <ex:named> WHERE { GRAPH <ex:named> { ?s ?p ?o } }", new String[] { "ex:named" }, 1);
	}
	
	@Test
	public void named_graph_uri_missing() {
		//No FROM
		//FROM NAMED
		//GRAPH clause with URI of non-existent graph
		
		//Yields triples in <ex:named>
		test("SELECT * FROM NAMED <ex:named> WHERE { GRAPH <ex:missing> { ?s ?p ?o } }", new String[] { }, 0);
	}
	
	/**
	 * This block of tests are for the case where we have a FROM only
	 */
	
	@Test
	public void from() {
		//FROM
		//No FROM NAMED
		//No GRAPH clause
		
		//Yields triples from <ex:from>
		test("SELECT * FROM <ex:from> WHERE { ?s ?p ?o }", new String[] { "ex:from" }, 1);
	}
	
	/**
	 * This block of tests are for the cases where we only have a GRAPH clause
	 */
		
	@Test
	public void graph_var() {
		//No FROM
		//No FROM NAMED
		//GRAPH clause with variable
		
		//Yields triples in all named graphs
		test("SELECT * WHERE { GRAPH ?g { ?s ?p ?o } }", new String[] { "ex:from", "ex:named", "ex:other" }, 3);
	}
	
	@Test
	public void graph_uri_exists() {
		//No FROM
		//No FROM NAMED
		//GRAPH clause with URI of an existing graph
		
		//Yields triples in the specific named graph
		test("SELECT * WHERE { GRAPH <ex:named> { ?s ?p ?o } }", new String[] { "ex:named" }, 1);
	}
	
	@Test
	public void graph_uri_missing() {
		//No FROM
		//No FROM NAMED
		//GRAPH clause with URI of a non-existent graph
		
		//Yields no triples
		test("SELECT * WHERE { GRAPH <ex:missing> { ?s ?p ?o } }", new String[] { }, 0);
	}
	
	/**
	 * Tests where we have no explicit dataset definition of any kind
	 */
	
	@Test
	public void no_dataset() {
		//No FROM
		//No FROM NAMED
		//No GRAPH clause
		
		//Yields only triples in the default graph
		test("SELECT * WHERE { ?s ?p ?o }", new String[] { "ex:default" }, 1);
	}
}
