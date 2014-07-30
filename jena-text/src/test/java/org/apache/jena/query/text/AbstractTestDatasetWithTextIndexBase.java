/**
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

package org.apache.jena.query.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;
import java.util.Set;

import org.apache.jena.atlas.lib.StrUtils;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

/*
 * This abstract class defines a collection of test methods for testing
 * test searches.  Its subclasses create a dataset using the index to 
 * to be tested and then call the test methods in this class to run
 * the actual tests.
 */
public abstract class AbstractTestDatasetWithTextIndexBase {
	protected static final String RESOURCE_BASE = "http://example.org/data/resource/";
	protected static Dataset dataset;
	protected static final String QUERY_PROLOG = 
			StrUtils.strjoinNL(
				"PREFIX text: <http://jena.apache.org/text#>",
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				);
	
	protected static final String TURTLE_PROLOG = 
				StrUtils.strjoinNL(
						"@prefix text: <http://jena.apache.org/text#> .",
						"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ."
						);
	
	protected void doTestSearch(String turtle, String queryString, Set<String> expectedEntityURIs) {
		doTestSearch("", turtle, queryString, expectedEntityURIs);
	}
	
	protected void doTestSearch(String label, String turtle, String queryString, Set<String> expectedEntityURIs) {
		doTestSearch(label, turtle, queryString, expectedEntityURIs, expectedEntityURIs.size());
	}
	
	protected void doTestSearch(String label, String turtle, String queryString, Set<String> expectedEntityURIs, int expectedNumResults) {
		Model model = dataset.getDefaultModel();
		Reader reader = new StringReader(turtle);
		dataset.begin(ReadWrite.WRITE);
		model.read(reader, "", "TURTLE");
		dataset.commit();
		doTestQuery(dataset, label, queryString, expectedEntityURIs, expectedNumResults);
	}
	
	public static void doTestQuery(Dataset dataset, String label, String queryString, Set<String> expectedEntityURIs, int expectedNumResults) {
		Query query = QueryFactory.create(queryString) ;
		try(QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
			dataset.begin(ReadWrite.READ);
		    ResultSet results = qexec.execSelect() ;
		    
		    assertEquals(label, expectedNumResults > 0, results.hasNext());
		    int count;
		    for (count=0; results.hasNext(); count++) {
		    	String entityURI = results.next().getResource("s").getURI();
		        assertTrue(label + ": unexpected result: " + entityURI, expectedEntityURIs.contains(entityURI));
		    }
		    assertEquals(label, expectedNumResults, count);
		} finally { dataset.end() ; }		
	}
}
