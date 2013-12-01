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

package org.apache.jena.query.spatial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Reader;
import java.io.StringReader;
import java.util.Set;

import org.apache.jena.atlas.lib.StrUtils;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
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
public abstract class AbstractTestDatasetWithSpatialIndex {
	protected static final String RESOURCE_BASE = "http://example.org/data/resource/";
	protected static Dataset dataset;
	protected static final String QUERY_PROLOG = 
			StrUtils.strjoinNL(
				"PREFIX spatial: <http://jena.apache.org/spatial#>",
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
				"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>",
				"PREFIX : <"+ RESOURCE_BASE +">"
				);
	
	protected static final String TURTLE_PROLOG = 
				StrUtils.strjoinNL(
						"@prefix spatial: <http://jena.apache.org/spatial#> .",
						"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .",
						"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",
						"@prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> .",
						"@prefix wkt: <http://www.opengis.net/ont/geosparql#> .",
						"@prefix : <"+ RESOURCE_BASE +"> ."
						);
	
	
	protected void doTestSearchThrowException(String turtle, String queryString) {
		doTestSearch(turtle, queryString, null, -1, true);
	}
    
	protected void doTestSearch(String turtle, String queryString, Set<String> expectedEntityURIs) {
		doTestSearch(turtle, queryString, expectedEntityURIs, expectedEntityURIs.size(), false);
	}
	
	protected void doTestSearch(String turtle, String queryString, Set<String> expectedEntityURIs, int expectedNumResults) {
		doTestSearch(turtle, queryString, expectedEntityURIs, expectedNumResults, false);
	}
	
	private void doTestSearch(String turtle, String queryString, Set<String> expectedEntityURIs, int expectedNumResults, boolean throwException) {
		Model model = dataset.getDefaultModel();
		Reader reader = new StringReader(turtle);
		dataset.begin(ReadWrite.WRITE);
		model.read(reader, "", "TURTLE");
		dataset.commit();
		doTestQuery(dataset, queryString, expectedEntityURIs, expectedNumResults, throwException);
	}
	
	public static void doTestQuery(Dataset dataset, String queryString, Set<String> expectedEntityURIs, int expectedNumResults, boolean throwException) {
		Query query = QueryFactory.create(queryString) ;
		
		QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
		try {
			dataset.begin(ReadWrite.READ);
		    ResultSet results = qexec.execSelect() ;

		    boolean b = ( (expectedNumResults > 0) == results.hasNext() ) ;
		    if ( !b ) {
		        System.out.println(queryString) ;
		        System.out.println(expectedNumResults) ;
		        
		    }
		    if (throwException){
		    	fail("An exception is supposed to be thrown for spatial query!");
		    }
		    assertEquals( expectedNumResults > 0, results.hasNext());
		    int count;
		    for (count=0; results.hasNext(); count++) {
		    	String entityURI = results.next().getResource("s").getURI();
		        assertTrue(": unexpected result: " + entityURI, expectedEntityURIs.contains(entityURI));
		    }
		    assertEquals(expectedNumResults, count);
		} catch (QueryException e){
			if (!throwException){
				fail("The exception is not supposed to be thrown: "+ e.getMessage());
			}	
		} finally { qexec.close() ; dataset.end() ; }			
		
	
	}
	
}
