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

package org.apache.jena.propertytable.graph;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.lang.csv.CSV2RDF;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.sparql.engine.main.StageBuilder ;
import org.apache.jena.sparql.engine.main.StageGenerator ;
import org.junit.Assert ;
import org.junit.BeforeClass ;
import org.junit.Test ;

/**
 * Tests related to GraphCSV with some real world data.
 */
public class GraphCSVTest extends Assert {
	
	@BeforeClass
	public static void init(){
		CSV2RDF.init() ;
	}
	
	@Test
	public void testGraphCSV() {
		//String file = "src/test/resources/HEFCE_organogram_senior_data_31032011.csv";test.csv
		String file = "src/test/resources/test.csv";
		
		Model csv = ModelFactory.createModelForGraph(new GraphCSV(file));
		assertEquals(12, csv.size());

		Query query = QueryFactory
				.create("PREFIX : <src/test/resources/test.csv#> SELECT ?townName ?pop {?x :Town ?townName ; :Population ?pop ; :Predicate%20With%20Space 'PredicateWithSpace2' . FILTER(?pop > 500000)}");
		
		QueryExecution qexec = QueryExecutionFactory.create(query, csv);
		ResultSet results = qexec.execSelect();
		
		assertTrue(results.hasNext());
		QuerySolution soln = results.nextSolution();
		assertEquals( "Northville", soln.getLiteral("townName").getString());
		assertTrue( 654000 == soln.getLiteral("pop").getInt());
		
		assertFalse(results.hasNext());
	}
	
	@Test 
	public void stageGeneratorTest(){
		wireIntoExecution();
		testGraphCSV();
	}
	
    private static void wireIntoExecution() {
        StageGenerator orig = (StageGenerator)ARQ.getContext().get(ARQ.stageGenerator) ;
        StageGenerator stageGenerator = new StageGeneratorPropertyTable(orig) ;
        StageBuilder.setGenerator(ARQ.getContext(), stageGenerator) ;
    }
	
	//http://www.w3.org/TR/csvw-ucr/#UC-OrganogramData
	//2.4 Use Case #4 - Publication of public sector roles and salaries
	@Test
	public void testUseCase4(){
		String file = "src/test/resources/HEFCE_organogram_senior_data_31032011.csv";
		
		Model csv = ModelFactory.createModelForGraph(new GraphCSV(file));
		assertEquals(72, csv.size());

		String x = StrUtils.strjoinNL
		    ("PREFIX : <src/test/resources/HEFCE_organogram_senior_data_31032011.csv#>"
		    ,"SELECT ?name ?unit"
		    ,"{ ?x :Name ?name ;"
		    ,"     :Unit ?unit ;"
		    ,"     :Actual%20Pay%20Floor%20%28%A3%29 ?floor ;"
		    ,"     :Actual%20Pay%20Ceiling%20%28%A3%29 ?ceiling ."
		    ,"FILTER(?floor > 100000 && ?ceiling <120000 )"
		    ,"}");
		
		Query query = QueryFactory.create(x) ;
		
		QueryExecution qexec = QueryExecutionFactory.create(query, csv);
		ResultSet results = qexec.execSelect();
		
		assertTrue(results.hasNext());
		QuerySolution soln = results.nextSolution();
		assertEquals( "David Sweeney", soln.getLiteral("name").getString());
		assertEquals( "Research, Innovation and Skills", soln.getLiteral("unit").getString());
		
		assertFalse(results.hasNext());
	}
	
	
	//http://www.w3.org/TR/csvw-ucr/#UC-JournalArticleSearch
	//2.6 Use Case #6 - Journal Article Solr Search Results
	@Test
	public void testUseCase6(){
		String file = "src/test/resources/PLOSone-search-results.csv";
		
		Model csv = ModelFactory.createModelForGraph(new GraphCSV(file));
		assertEquals(30, csv.size());

		Query query = QueryFactory
				.create("PREFIX : <src/test/resources/PLOSone-search-results.csv#> SELECT ?author {?x :author ?author ; :doi '10.1371/journal.pone.0095156' }");
		
		QueryExecution qexec = QueryExecutionFactory.create(query, csv);
		ResultSet results = qexec.execSelect();
		
		assertTrue(results.hasNext());
		QuerySolution soln = results.nextSolution();
		assertEquals( "Oshrat Raz,Dorit L Lev,Alexander Battler,Eli I Lev", soln.getLiteral("author").getString());
		
		assertFalse(results.hasNext());
	}
	
	//http://www.w3.org/TR/csvw-ucr/#UC-PaloAltoTreeData
	//2.11 Use Case #11 - City of Palo Alto Tree Data
	@Test
	public void testUseCase11(){
		String file = "src/test/resources/Palo_Alto_Trees.csv";
		
		Model csv = ModelFactory.createModelForGraph(new GraphCSV(file));
		assertEquals(199, csv.size());

		Query query = QueryFactory
				.create("PREFIX : <src/test/resources/Palo_Alto_Trees.csv#> SELECT ?longitude ?latitude {?x :Longitude ?longitude ; :Latitude ?latitude ; :Distance%20from%20Property ?distance . FILTER(?distance > 50 )}");
		
		QueryExecution qexec = QueryExecutionFactory.create(query, csv);
		ResultSet results = qexec.execSelect();
		
		assertTrue(results.hasNext());
		QuerySolution soln = results.nextSolution();
		assertEquals( -122.1566921, soln.getLiteral("longitude").getDouble(), 0);
		assertEquals( 37.4408948, soln.getLiteral("latitude").getDouble(), 0);
		
		assertFalse(results.hasNext());
	}

}
