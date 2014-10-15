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

package org.apache.jena.query.spatial.pfunction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.spatial.AbstractTestDatasetWithSpatialIndex;
import org.junit.Test;

public abstract class AbstractTestNorthPF extends AbstractTestDatasetWithSpatialIndex {

	@Test
	public void testOneSimpleResult() {
		final String turtle = StrUtils.strjoinNL(TURTLE_PROLOG, "<"
				+ RESOURCE_BASE + "testOneSimpleResult>",
				"   geo:lat '51.3827'^^xsd:float ;",
				"   geo:long '-2.71909'^^xsd:float ", ".");
		String queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:north (51.3000 0.0000 -1) .", "}");
		Set<String> expectedURIs = (new HashSet<String>());
		expectedURIs
				.addAll(Arrays
						.asList((new String[] { "http://example.org/data/resource/testOneSimpleResult" })));
		doTestSearch(turtle, queryString, expectedURIs);
	}

	@Test
	public void testMultipleResults() {
		String label = "testMultipleResults";
		final String turtle = StrUtils.strjoinNL(TURTLE_PROLOG, "<"
				+ RESOURCE_BASE + label + "1>",
				"   geo:lat '51.3827'^^xsd:float ;",
				"   geo:long '-2.71909'^^xsd:float ;" , ".",
				"<"	+ RESOURCE_BASE + label + "2>",
				"   geo:lat '51.3967'^^xsd:float ;",
				"   geo:long '-3.34333'^^xsd:float ;", ".");

		String queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:north (51.3000 0.0000 -1) .", "}");
		Set<String> expectedURIs = (new HashSet<String>());
		expectedURIs.addAll(Arrays.asList((new String[] {
				"http://example.org/data/resource/" + label + "1",
				"http://example.org/data/resource/" + label + "2" })));
		doTestSearch(turtle, queryString, expectedURIs);
	}
	
	@Test
	public void testArgumentListSize() {
		final String turtle = StrUtils.strjoinNL(TURTLE_PROLOG, "<"
				+ RESOURCE_BASE + "testArgumentListSize>",
				"   geo:lat '51.3827'^^xsd:float ;",
				"   geo:long '-2.71909'^^xsd:float ", ".");
		String queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:north (51.3000) .", "}");
		doTestSearchThrowException(turtle, queryString);
		
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:north (51.3000 0.0000 -1 'another argument') .", "}");
		doTestSearchThrowException(turtle, queryString);
	}
	
  @Test
	public void testSearchLimitsResults() {
		String label = "testSearchLimitsResults";
		final String turtle = StrUtils.strjoinNL(TURTLE_PROLOG, "<"
				+ RESOURCE_BASE + label + "1>",
				"   geo:lat '51.3827'^^xsd:float ;",
				"   geo:long '-2.71909'^^xsd:float ;" , ".",
				"<"	+ RESOURCE_BASE + label + "2>",
				"   geo:lat '51.3967'^^xsd:float ;",
				"   geo:long '-3.34333'^^xsd:float ;", ".");
		String queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:north (51.3000 0.0000 1) .", "}");
		Set<String> expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    "http://example.org/data/resource/" + label + "1",
					    "http://example.org/data/resource/" + label + "2"
				}
		)));
		doTestSearch(turtle, queryString, expectedURIs, 1);
		
		
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:north (51.3000 0.0000 -1) .", "}");
		expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    "http://example.org/data/resource/" + label + "1",
					    "http://example.org/data/resource/" + label + "2"
				}
		)));
		doTestSearch(turtle, queryString, expectedURIs);
		
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:north (51.3000 0.0000 3) .", "}");
		expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    "http://example.org/data/resource/" + label + "1",
					    "http://example.org/data/resource/" + label + "2"
				}
		)));
		doTestSearch(turtle, queryString, expectedURIs);
		
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:north (51.3000 0.0000 'something not an integer') .", "}");
		expectedURIs = (new HashSet<String>());
		doTestSearch(turtle, queryString, expectedURIs);
	}
  
}
