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

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertTrue ;

import java.io.Reader ;
import java.io.StringReader ;
import java.util.Arrays ;
import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;

/*
 * This abstract class defines a collection of test methods for testing
 * test searches.  Its subclasses create a dataset using the index to 
 * to be tested and then call the test methods in this class to run
 * the actual tests.
 */
public abstract class AbstractTestDatasetWithTextIndex {
	private static final String RESOURCE_BASE = "http://example.org/data/resource/";
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
		Set<String> expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((new String[] {"http://example.org/data/resource/testOneSimpleResult"})));
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
		Set<String> expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((new String[]
				{
			    "http://example.org/data/resource/" + label + "1",
			    "http://example.org/data/resource/" + label + "2"
				}
		)));
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
		Set<String> expectedURIsLabel = (new HashSet<String>());
		expectedURIsLabel.addAll( Arrays.asList((new String[]
				{
			    "http://example.org/data/resource/" + label + "1",
				}
		)));
		Set<String> expectedURIsComment = (new HashSet<String>());
		expectedURIsComment.addAll( Arrays.asList((new String[]
				{
			    "http://example.org/data/resource/" + label + "2",
				}
		)));
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
		Set<String> expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((new String[]
				{
			    "http://example.org/data/resource/" + label + "1",
				}
		)));
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
		Set<String> expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    "http://example.org/data/resource/" + label + "1",
					    "http://example.org/data/resource/" + label + "2",
					    "http://example.org/data/resource/" + label + "3",
					    "http://example.org/data/resource/" + label + "4",
				}
		)));
		doTestSearch("default field:", turtle, queryString, expectedURIs, 3 );
	}
	
	protected void doTestSearch(String turtle, String queryString, Set<String> expectedEntityURIs) {
		doTestSearch("", turtle, queryString, expectedEntityURIs);
	}
	
	private void doTestSearch(String label, String turtle, String queryString, Set<String> expectedEntityURIs) {
		doTestSearch(label, turtle, queryString, expectedEntityURIs, expectedEntityURIs.size());
	}
	
	private void doTestSearch(String label, String turtle, String queryString, Set<String> expectedEntityURIs, int expectedNumResults) {
		Model model = dataset.getDefaultModel();
		Reader reader = new StringReader(turtle);
		dataset.begin(ReadWrite.WRITE);
		model.read(reader, "", "TURTLE");
		dataset.commit();
		doTestQuery(dataset, label, queryString, expectedEntityURIs, expectedNumResults);
	}
	
	public static void doTestQuery(Dataset dataset, String label, String queryString, Set<String> expectedEntityURIs, int expectedNumResults) {
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
		    
		    
		    assertEquals(label, expectedNumResults > 0, results.hasNext());
		    int count;
		    for (count=0; results.hasNext(); count++) {
		    	String entityURI = results.next().getResource("s").getURI();
		        assertTrue(label + ": unexpected result: " + entityURI, expectedEntityURIs.contains(entityURI));
		    }
		    assertEquals(label, expectedNumResults, count);
		} finally { qexec.close() ; dataset.end() ; }		
	}
	
//	private void dumpSolrTextIndex(TextIndexSolr index) {
//		System.out.println("Index:");
//		SolrServer solrServer = index.getServer();
//        SolrQuery sq = new SolrQuery("*:*");
//        try {
//            QueryResponse rsp = solrServer.query( sq ) ;
//            SolrDocumentList docs = rsp.getResults();
//            Iterator<SolrDocument> iter = docs.iterator();
//            while (iter.hasNext()) {
//            	SolrDocument doc = iter.next();
//            	Iterator<String> iterFieldNames = doc.getFieldNames().iterator();
//            	while (iterFieldNames.hasNext()) {
//            		System.out.println("  --");
//            		String fieldName = iterFieldNames.next();
//            		Object fieldValue = doc.getFieldValue(fieldName);
//            		System.out.println("    " + fieldName + " : " + fieldValue);
//            		System.out.println("  --");
//            	}
//            	
//            }
//        } catch (SolrServerException e) { 
//        	e.printStackTrace();
//        }
//        System.out.println("**");		
//	}
}
