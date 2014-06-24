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

import java.io.Reader ;
import java.io.StringReader ;
import java.util.Arrays ;
import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.junit.Test ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.rdf.model.Model ;

/**
 * This abstract class defines tests of the graph-specific indexing.
 */
public class AbstractTestDatasetWithGraphTextIndex extends AbstractTestDatasetWithTextIndex {

    private void putTurtleInModel(String turtle, String modelName) {
        Model model = modelName != null ? dataset.getNamedModel(modelName) : dataset.getDefaultModel() ;
        Reader reader = new StringReader(turtle) ;
        dataset.begin(ReadWrite.WRITE) ;
        model.read(reader, "", "TURTLE") ;
        dataset.commit() ;
    }

	@Test
	public void testOneSimpleResultInGraph() {
		final String turtle = StrUtils.strjoinNL(
				TURTLE_PROLOG,
				"<" + RESOURCE_BASE + "testOneSimpleResult>",
				"  rdfs:label 'bar testOneSimpleResult barfoo foo'",
				"."
				);
                putTurtleInModel(turtle, "http://example.org/modelA") ;
		String queryString = StrUtils.strjoinNL(
				QUERY_PROLOG,
				"SELECT ?s",
				"WHERE {",
				"  GRAPH ?g { ?s text:query ( rdfs:label 'testOneSimpleResult' 10 ) . }",
				"}"
				);
		Set<String> expectedURIs = new HashSet<>() ;
		expectedURIs.addAll( Arrays.asList(RESOURCE_BASE + "testOneSimpleResult")) ;
		doTestQuery(dataset, "", queryString, expectedURIs, expectedURIs.size()) ;
	}

	@Test
	public void testOneResultTwoGraphs() {
		final String turtleA = StrUtils.strjoinNL(
				TURTLE_PROLOG,
				"<" + RESOURCE_BASE + "testResultInModelA>",
				"  rdfs:label 'bar testOneResult barfoo foo'",
				"."
				);
                putTurtleInModel(turtleA, "http://example.org/modelA") ;
		final String turtleB = StrUtils.strjoinNL(
				TURTLE_PROLOG,
				"<" + RESOURCE_BASE + "testResultInModelB>",
				"  rdfs:label 'bar testOneResult barfoo foo'",
				"."
				);
                putTurtleInModel(turtleB, "http://example.org/modelB") ;
		String queryString = StrUtils.strjoinNL(
				QUERY_PROLOG,
				"SELECT ?s",
				"WHERE {",
				"  GRAPH <http://example.org/modelA> { ?s text:query ( rdfs:label 'testOneResult' 10 ) . }",
				"}"
				);
		Set<String> expectedURIs = new HashSet<>() ;
		expectedURIs.addAll( Arrays.asList(RESOURCE_BASE + "testResultInModelA")) ;
		doTestQuery(dataset, "", queryString, expectedURIs, expectedURIs.size()) ;
	}

	@Test
	public void testORFromGraphs() {
		final String turtleA = StrUtils.strjoinNL(
				TURTLE_PROLOG,
				"<" + RESOURCE_BASE + "testResultOneInModelA>",
				"  rdfs:label 'bar testResultOne barfoo foo'",
				".",
				"<" + RESOURCE_BASE + "testResultTwoInModelA>",
				"  rdfs:label 'bar testResultTwo barfoo foo'",
				".",
				"<" + RESOURCE_BASE + "testResultThreeInModelA>",
				"  rdfs:label 'bar testResultThree barfoo foo'",
				"."
				);
                putTurtleInModel(turtleA, "http://example.org/modelA") ;
		final String turtleB = StrUtils.strjoinNL(
				TURTLE_PROLOG,
				"<" + RESOURCE_BASE + "testResultOneInModelB>",
				"  rdfs:label 'bar testResultOne barfoo foo'",
				"."
				);
                putTurtleInModel(turtleB, "http://example.org/modelB") ;
		String queryString = StrUtils.strjoinNL(
				QUERY_PROLOG,
				"SELECT ?s",
				"WHERE {",
				"  GRAPH <http://example.org/modelA> { ?s text:query ( rdfs:label 'testResultOne OR testResultTwo' 10 ) . }",
				"}"
				);
		Set<String> expectedURIs = new HashSet<>() ;
		expectedURIs.addAll( Arrays.asList(RESOURCE_BASE + "testResultOneInModelA", RESOURCE_BASE + "testResultTwoInModelA")) ;
		doTestQuery(dataset, "", queryString, expectedURIs, expectedURIs.size()) ;
	}

	@Test
	public void testQueryFromDefaultGraph() {
		final String turtleA = StrUtils.strjoinNL(
				TURTLE_PROLOG,
				"<" + RESOURCE_BASE + "testResultInModelA>",
				"  rdfs:label 'bar testOneResult barfoo foo'",
				"."
				);
                putTurtleInModel(turtleA, null) ; // put in default graph
		final String turtleB = StrUtils.strjoinNL(
				TURTLE_PROLOG,
				"<" + RESOURCE_BASE + "testResultInModelB>",
				"  rdfs:label 'bar testOneResult barfoo foo'",
				"."
				);
                putTurtleInModel(turtleB, "http://example.org/modelB") ;
		String queryString = StrUtils.strjoinNL(
				QUERY_PROLOG,
				"SELECT ?s",
				"WHERE {",
				"    ?s text:query ( rdfs:label 'testOneResult' 10 ) .",
				"}"
				);
		Set<String> expectedURIs = new HashSet<>() ;
		expectedURIs.addAll( Arrays.asList(RESOURCE_BASE + "testResultInModelA")) ;
		doTestQuery(dataset, "", queryString, expectedURIs, expectedURIs.size()) ;
	}

	@Test
	public void testBnodeIdentifiedGraph() {
		final String trig = StrUtils.strjoinNL(
				TURTLE_PROLOG,
				"_:graphA {",
				"  <" + RESOURCE_BASE + "testResultInGraphA>",
				"    rdfs:label 'bar testResult barfoo foo' .",
				"}"
				);
                StringReader reader = new StringReader(trig);
                dataset.begin(ReadWrite.WRITE) ;
                RDFDataMgr.read(dataset.asDatasetGraph(), reader, "", Lang.TRIG);
                dataset.commit();

		String queryString = StrUtils.strjoinNL(
				QUERY_PROLOG,
				"SELECT ?s",
				"WHERE {",
				"  GRAPH ?g { ?s text:query ( rdfs:label 'testResult' 10 ) . }",
				"}"
				);
		Set<String> expectedURIs = new HashSet<>() ;
		expectedURIs.addAll( Arrays.asList(RESOURCE_BASE + "testResultInGraphA")) ;
		doTestQuery(dataset, "", queryString, expectedURIs, expectedURIs.size()) ;
	}
}
