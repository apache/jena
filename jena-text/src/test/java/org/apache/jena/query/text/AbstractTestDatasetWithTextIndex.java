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

import java.util.Arrays ;
import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

/*
 * This abstract class defines a collection of test methods for testing
 * test searches.  Its subclasses create a dataset using the index to 
 * to be tested and then call the test methods in this class to run
 * the actual tests.
 */
public abstract class AbstractTestDatasetWithTextIndex extends AbstractTestDatasetWithTextIndexBase {
	
	@Test
	public void testOneSimpleResult() {
		final String turtle = StrUtils.strjoinNL(
				TURTLE_PROLOG,
				"<" + RESOURCE_BASE + "testOneSimpleResult>",
				"  rdfs:label 'bar testOneSimpleResult barfoo foo'",
				"."
				);
		String queryString = StrUtils.strjoinNL(
				QUERY_PROLOG,
				"SELECT ?s",
				"WHERE {",
				"    ?s text:query ( rdfs:label 'testOneSimpleResult' 10 ) .",
				"}"
				);
		Set<String> expectedURIs = new HashSet<>() ;
		expectedURIs.addAll( Arrays.asList("http://example.org/data/resource/testOneSimpleResult")) ;
		doTestSearch(turtle, queryString, expectedURIs);
	}

	static String R_S1 = RESOURCE_BASE + "s1" ;
    static String R_S2 = RESOURCE_BASE + "s2" ;
	static String PF_DATA = StrUtils.strjoinNL(
	                                           TURTLE_PROLOG,
	                                           "<" + R_S1 + "> rdfs:label 'text' .",
	                                           "<" + R_S2 + "> rdfs:label 'fuzz' ."
	                                           );
	
    @Test
    public void propertyFunctionText_1() {
        final String turtle = PF_DATA ;
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query (rdfs:label  'text') .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>();
        expectedURIs.addAll( Arrays.asList( R_S1 ) ) ;
        doTestSearch(turtle, queryString, expectedURIs);
    }

    @Test
    public void propertyFunctionText_1_dft() {
        // As before but using default field.
        final String turtle = PF_DATA ;
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query ('text') .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>();
        expectedURIs.addAll( Arrays.asList( R_S1 ) ) ;
        doTestSearch(turtle, queryString, expectedURIs);
    }

    @Test
    public void propertyFunctionText_2() {
        final String turtle = PF_DATA ;
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query ( rdfs:label 'text') .",
                "    ?s rdfs:label 'text' .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>();
        expectedURIs.addAll( Arrays.asList( R_S1 ) ) ;
        doTestSearch(turtle, queryString, expectedURIs);
    }

    @Test
    public void propertyFunctionText_2_dft() {
        final String turtle = PF_DATA ;
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query ('text') .",
                "    ?s rdfs:label 'text' .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>();
        expectedURIs.addAll( Arrays.asList( R_S1 ) ) ;
        doTestSearch(turtle, queryString, expectedURIs);
    }

    @Test
    public void propertyFunctionText_3() {
        final String turtle = PF_DATA ;
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s rdfs:label 'text' .",
                "    ?s text:query ( rdfs:label 'text') .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>();
        expectedURIs.addAll( Arrays.asList( R_S1 ) ) ;
        doTestSearch(turtle, queryString, expectedURIs);
    }
    
    @Test
    public void propertyFunctionText_3_dft() {
        final String turtle = PF_DATA ;
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s rdfs:label 'text' .",
                "    ?s text:query ('text') .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>();
        expectedURIs.addAll( Arrays.asList( R_S1 ) ) ;
        doTestSearch(turtle, queryString, expectedURIs);
    }
    
    @Test
    public void propertyFunctionText_4() {
        final String turtle = PF_DATA ;
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s rdfs:label 'text' .",
                "    ?s text:query ( rdfs:label 'fuzz') .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>();
        doTestSearch(turtle, queryString, expectedURIs);
    }

    @Test
    public void propertyFunctionText_5() {
        final String turtle = PF_DATA ;
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    BIND('text' AS ?t)", 
                "    ?s text:query ( rdfs:label ?t) .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>();
        expectedURIs.addAll( Arrays.asList( R_S1 ) ) ;
        doTestSearch(turtle, queryString, expectedURIs);
    }

    @Test
    public void propertyFunctionText_6() {
        final String turtle = PF_DATA ;
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    BIND(rdfs:label AS ?P)", 
                "    ?s text:query ( ?P 'text') .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>();
        expectedURIs.addAll( Arrays.asList( R_S1 ) ) ;
        doTestSearch(turtle, queryString, expectedURIs);
    }


    @Test
    public void propertyFunctionText_7() {
        final String turtle = PF_DATA ;
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    BIND(1 AS ?C)", 
                "    ?s text:query ( rdfs:label 'text' ?C) .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>();
        expectedURIs.addAll( Arrays.asList( R_S1 ) ) ;
        doTestSearch(turtle, queryString, expectedURIs);
    }


    @Test
	public void testMultipleResults() {
		String label = "testMultipleResults";
		final String turtle = StrUtils.strjoinNL(
				TURTLE_PROLOG,
				"<" + RESOURCE_BASE + label +"1>",
				"  rdfs:label '" + label + "1'",
				".",
				"<" + RESOURCE_BASE + label + "2>",
				"  rdfs:label '" + label + "2'",
				"."
				);
		String queryString = StrUtils.strjoinNL(
				QUERY_PROLOG,
				"SELECT ?s",
				"WHERE {",
				"    ?s text:query ( rdfs:label '" + label + "?' 10 ) .",
				"}"
				);
		Set<String> expectedURIs = new HashSet<>() ;
		expectedURIs.addAll( Arrays.asList(
			    "http://example.org/data/resource/" + label + "1",
			    "http://example.org/data/resource/" + label + "2"
		    ));
		doTestSearch(turtle, queryString, expectedURIs);
	}

    @Test
    public void testMultipleResults_dft() {
        String label = "testMultipleResults";
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + label +"1>",
                "  rdfs:label '" + label + "1'",
                ".",
                "<" + RESOURCE_BASE + label + "2>",
                "  rdfs:label '" + label + "2'",
                "."
                );
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query ('" + label + "?' 10 ) .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList(
                "http://example.org/data/resource/" + label + "1",
                "http://example.org/data/resource/" + label + "2"
            ));
        doTestSearch(turtle, queryString, expectedURIs);
    }

    @Test
	public void testSearchCorrectField() {
		String label = "tscf";
		String label2 = "tscfo";
		final String turtle = StrUtils.strjoinNL(
				TURTLE_PROLOG,
				"<" + RESOURCE_BASE + label +"1>",
				"  rdfs:label '" + label + "a' ; ",
				"  rdfs:comment '" + label2 + "a' ;",
				".",
				"<" + RESOURCE_BASE + label + "2>",
				"  rdfs:label '" + label2 + "b' ; ",
				"  rdfs:comment '" + label + "b' ; ",
				"."
				);
		String queryStringLabel = StrUtils.strjoinNL(
				QUERY_PROLOG,
				"SELECT ?s",
				"WHERE {",
				"    ?s text:query ( rdfs:label '" + label + "?' 10 ) .",
				"}"
				);
		String queryStringComment = StrUtils.strjoinNL(
				QUERY_PROLOG,
				"SELECT ?s",
				"WHERE {",
				"    ?s text:query ( rdfs:comment '" + label + "?' 10 ) .",
				"}"
				);
		Set<String> expectedURIsLabel = new HashSet<>() ;
		expectedURIsLabel.addAll( Arrays.asList(
			    "http://example.org/data/resource/" + label + "1"
		    ));
		Set<String> expectedURIsComment = new HashSet<>() ;
		expectedURIsComment.addAll( Arrays.asList(
			    "http://example.org/data/resource/" + label + "2"
		    ));
		doTestSearch("label:", turtle, queryStringLabel, expectedURIsLabel);
		doTestSearch("comment:", turtle, queryStringComment, expectedURIsComment);
	}

    @Test
	public void testSearchDefaultField() {
		String label = "testSearchDefaultField";
		String label2 = "testSearchDefaultFieldOther";
		final String turtle = StrUtils.strjoinNL(
				TURTLE_PROLOG,
				"<" + RESOURCE_BASE + label +"1>",
				"  rdfs:label '" + label + "1' ; ",
				"  rdfs:comment '" + label2 + "1' ;",
				".",
				"<" + RESOURCE_BASE + label + "2>",
				"  rdfs:label '" + label2 + "2' ; ",
				"  rdfs:comment '" + label + "2' ; ",
				"."
				);
		String queryString = StrUtils.strjoinNL(
				QUERY_PROLOG,
				"SELECT ?s",
				"WHERE {",
				"    ?s text:query ( rdfs:label '" + label + "?' 10 ) .",
				"}"
				);
		Set<String> expectedURIs = new HashSet<>() ;
		expectedURIs.addAll( Arrays.asList(
			    "http://example.org/data/resource/" + label + "1"
		    ));
		doTestSearch("default field:", turtle, queryString, expectedURIs);
	}

    @Test
    public void testSearchDefaultField_dft() {
        String label = "testSearchDefaultField";
        String label2 = "testSearchDefaultFieldOther";
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + label +"1>",
                "  rdfs:label '" + label + "1' ; ",
                "  rdfs:comment '" + label2 + "1' ;",
                ".",
                "<" + RESOURCE_BASE + label + "2>",
                "  rdfs:label '" + label2 + "2' ; ",
                "  rdfs:comment '" + label + "2' ; ",
                "."
                );
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query ('" + label + "?' 10 ) .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList(
                "http://example.org/data/resource/" + label + "1"
            ));
        doTestSearch("default field:", turtle, queryString, expectedURIs);
    }

    @Test
	public void testSearchLimitsResults() {
		String label = "testSearchLimitsResults";
		final String turtle = StrUtils.strjoinNL(
				TURTLE_PROLOG,
				"<" + RESOURCE_BASE + label + "1>",
				"  rdfs:label '" + label + "' ;",
				".",
				"<" + RESOURCE_BASE + label + "2>",
				"  rdfs:label '" + label + "' ;",
				".",
				"<" + RESOURCE_BASE + label + "3>",
				"  rdfs:label '" + label + "' ;",
				".",
				"<" + RESOURCE_BASE + label + "4>",
				"  rdfs:label '" + label + "' ;",
				"."
				);
		String queryString = StrUtils.strjoinNL(
				QUERY_PROLOG,
				"SELECT ?s",
				"WHERE {",
				"    ?s text:query ( '" + label + "' 3 ) .",
				"}"
				);
		Set<String> expectedURIs = new HashSet<>() ;
		expectedURIs.addAll( Arrays.asList(
					    "http://example.org/data/resource/" + label + "1",
					    "http://example.org/data/resource/" + label + "2",
					    "http://example.org/data/resource/" + label + "3",
					    "http://example.org/data/resource/" + label + "4"
		    ));
		doTestSearch("default field:", turtle, queryString, expectedURIs, 3 );
	}
}