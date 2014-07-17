package com.hp.hpl.jena.propertytable.impl;
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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.engine.main.StageBuilder;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;
import com.hp.hpl.jena.util.PrintUtil;

public class GraphCSVTest extends Assert {
	
	@Test
	public void testGraphCSV() throws Exception {
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
	public void stageGeneratorTest() throws Exception{
		wireIntoExecution();
		testGraphCSV();
	}
	
    private static void wireIntoExecution() {
        StageGenerator orig = (StageGenerator)ARQ.getContext().get(ARQ.stageGenerator) ;
        StageGenerator stageGenerator = new StageGeneratorPropertyTable(orig) ;
        StageBuilder.setGenerator(ARQ.getContext(), stageGenerator) ;
    }
}
