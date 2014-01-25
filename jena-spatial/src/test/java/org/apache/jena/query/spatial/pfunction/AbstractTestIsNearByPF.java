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

public abstract class AbstractTestIsNearByPF extends AbstractTestDatasetWithSpatialIndex {

	@Test
	public void testOneSimpleResult() {
		final String turtle = StrUtils.strjoinNL(TURTLE_PROLOG, "<"
				+ RESOURCE_BASE + "testOneSimpleResult>",
				"   geo:lat '51.3827'^^xsd:float ;",
				"   geo:long '-2.71909'^^xsd:float ", ".");
		String queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:nearby (51.3000 -2.71000 100.0 'miles' -1) .", "}");
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
				" ?s spatial:nearby (51.3000 -2.71000 100.0 'miles' -1) .", "}");
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
				" ?s spatial:nearby (51.3000 -2.71000) .", "}");
		doTestSearchThrowException(turtle, queryString);
		
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:nearby (51.3000 -2.71000 100 'miles' 1 'another argument') .", "}");
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
				" ?s spatial:nearby (51.3000 -2.71000 100.0 1) .", "}");
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
				" ?s spatial:nearby (51.3000 -2.71000 100.0 -1) .", "}");
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
				" ?s spatial:nearby (51.3000 -2.71000 100.0 3) .", "}");
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
				" ?s spatial:nearby (51.3000 -2.71000 100.0 'something not an integer') .", "}");
		expectedURIs = (new HashSet<String>());
		doTestSearch(turtle, queryString, expectedURIs);
	}
  
  @Test
	public void testDistanceUnits() {
		String label = "testDistanceUnits";
		final String turtle = StrUtils.strjoinNL(TURTLE_PROLOG, "<"
				+ RESOURCE_BASE + label + "1>",
				"   geo:lat '51.3827'^^xsd:float ;",
				"   geo:long '-2.71909'^^xsd:float ;" , ".",
				"<"	+ RESOURCE_BASE + label + "2>",
				"   geo:lat '51.3967'^^xsd:float ;",
				"   geo:long '-3.34333'^^xsd:float ;", ".", 
				"<"	+ RESOURCE_BASE + label + "3>",
				"   geo:lat '52.4539'^^xsd:float ;",
				"   geo:long '-1.74803'^^xsd:float ;", ".");
		String queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:nearby (51.3000 -2.71000 100.0 'miles') .", "}");
		Set<String> expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    "http://example.org/data/resource/" + label + "1",
					    "http://example.org/data/resource/" + label + "2",
					    "http://example.org/data/resource/" + label + "3"
				}
		)));
		doTestSearch(turtle, queryString, expectedURIs);
		
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:nearby (51.3000 -2.71000 100.0 'mi') .", "}");
		expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    "http://example.org/data/resource/" + label + "1",
					    "http://example.org/data/resource/" + label + "2",
					    "http://example.org/data/resource/" + label + "3"
				}
		)));
		doTestSearch(turtle, queryString, expectedURIs);
		
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:nearby (51.3000 -2.71000 100.0 'kilometres') .", "}");
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
				" ?s spatial:nearby (51.3000 -2.71000 100.0 'km') .", "}");
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
				" ?s spatial:nearby (51.3000 -2.71000 10000.0 'metres') .", "}");
		expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    "http://example.org/data/resource/" + label + "1"
				}
		)));
		doTestSearch(turtle, queryString, expectedURIs);
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:nearby (51.3000 -2.71000 10000.0 'm') .", "}");
		expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    "http://example.org/data/resource/" + label + "1"
				}
		)));
		doTestSearch(turtle, queryString, expectedURIs);
		
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:nearby (51.3000 -2.71000 1000000.0 'centimetres') .", "}");
		expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    "http://example.org/data/resource/" + label + "1"
				}
		)));
		doTestSearch(turtle, queryString, expectedURIs);
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:nearby (51.3000 -2.71000 1000000.0 'cm') .", "}");
		expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    "http://example.org/data/resource/" + label + "1"
				}
		)));
		doTestSearch(turtle, queryString, expectedURIs);
		
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:nearby (51.3000 -2.71000 10000000.0 'millimetres') .", "}");
		expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    "http://example.org/data/resource/" + label + "1"
				}
		)));
		doTestSearch(turtle, queryString, expectedURIs);
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:nearby (51.3000 -2.71000 10000000.0 'mm') .", "}");
		expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    "http://example.org/data/resource/" + label + "1"
				}
		)));
		doTestSearch(turtle, queryString, expectedURIs);
		
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:nearby (51.3000 -2.71000 0.09 'degrees') .", "}");
		expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    "http://example.org/data/resource/" + label + "1"
				}
		)));
		doTestSearch(turtle, queryString, expectedURIs);
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:nearby (51.3000 -2.71000 0.09 'de') .", "}");
		expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    "http://example.org/data/resource/" + label + "1"
				}
		)));
		doTestSearch(turtle, queryString, expectedURIs);
		
		
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:nearby (51.3000 -2.71000 100.0 1 'mi') .", "}");
		expectedURIs = (new HashSet<String>());
		doTestSearch(turtle, queryString, expectedURIs);
		
		queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" ?s spatial:nearby (51.3000 -2.71000 100.0 'other unsupported unit' -1) .", "}");
		expectedURIs = (new HashSet<String>());
		doTestSearch(turtle, queryString, expectedURIs);
	}
  
    @Test
	public void testLatLongBound() {
		final String turtle = StrUtils.strjoinNL(TURTLE_PROLOG, "<"
				+ RESOURCE_BASE + "testLatLongBound>",
				"   geo:lat '51.3827'^^xsd:float ;",
				"   geo:long '-2.71909'^^xsd:float ", ". ",
				"<" + RESOURCE_BASE + "center>",
				"   geo:lat '51.3000'^^xsd:float ;",
				"   geo:long '-2.71000'^^xsd:float ", ". ");
		String queryString = StrUtils.strjoinNL(QUERY_PROLOG, "SELECT ?s",
				"WHERE {",
				" :center geo:lat ?lat .",
				" :center geo:long ?long .",
				" ?s spatial:nearby (?lat ?long 100.0 'miles' -1) .", "}");
		Set<String> expectedURIs = (new HashSet<String>());
		expectedURIs
				.addAll(Arrays
						.asList((new String[] { "http://example.org/data/resource/testLatLongBound", "http://example.org/data/resource/center" })));
		doTestSearch(turtle, queryString, expectedURIs);
	}
}
