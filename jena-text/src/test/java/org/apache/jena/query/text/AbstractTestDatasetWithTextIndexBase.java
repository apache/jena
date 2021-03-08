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
import static org.junit.Assert.assertFalse;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

/*
 * This abstract class defines a collection of test methods for testing
 * test searches.  Its subclasses create a dataset using the index to 
 * to be tested and then call the test methods in this class to run
 * the actual tests.
 */
public abstract class AbstractTestDatasetWithTextIndexBase {
    protected static final String RESOURCE_BASE = "http://example.org/data/resource/";
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
    
    protected Dataset dataset;
    
    protected void doTestSearch(String turtle, String queryString, Set<String> expectedEntityURIs) {
        doTestSearch("", turtle, queryString, expectedEntityURIs);
    }
    
    protected void doTestSearch(String label, String turtle, String queryString, Set<String> expectedEntityURIs) {
        doTestSearch(label, turtle, queryString, expectedEntityURIs, expectedEntityURIs.size());
    }
    
    protected void doTestSearch(String label, String turtle, String queryString, Set<String> expectedEntityURIs, int expectedNumResults) {
        loadData(turtle);
        doTestQuery(dataset, label, queryString, expectedEntityURIs, expectedNumResults);
    }

    protected Map<String,Float> doTestSearchWithScores(String turtle, String queryString, Set<String> expectedEntityURIs) {
        loadData(turtle);
        return doTestQueryWithScores(queryString, expectedEntityURIs);
    }

    protected void doTestSearchNoResult(String turtle, String queryString) {
        doTestSearchNoResult("", turtle, queryString);
    }

    protected void doTestSearchNoResult(String label, String turtle, String queryString) {
        loadData(turtle);
        doTestNoResult(dataset, label, queryString);
    }

    protected void loadData(String turtle) {
        Model model = dataset.getDefaultModel();
        Reader reader = new StringReader(turtle);
        dataset.begin(ReadWrite.WRITE);
        model.read(reader, "", "TURTLE");
        dataset.commit();
    }

    public static void doTestQuery(Dataset dataset, String label, String queryString, Set<String> expectedEntityURIs, int expectedNumResults) {
        Query query = QueryFactory.create(queryString) ;
        dataset.begin(ReadWrite.READ);
        try(QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
            ResultSet rs = qexec.execSelect() ;
            ResultSetRewindable results = rs.rewindable();
//            ResultSetFormatter.out(results); 
//            results.reset(); 
            assertEquals(label, expectedNumResults > 0, results.hasNext());
            int count;
            for (count=0; results.hasNext(); count++) {
                String entityURI = results.next().getResource("s").getURI();
                assertTrue(label + ": unexpected result: " + entityURI, expectedEntityURIs.contains(entityURI));
            }
            assertEquals(label, expectedNumResults, count);
        }
        finally {
            dataset.end() ;
        }        
    }
    
    protected Map<String,Float> doTestQueryWithScores(String queryString, Set<String> expectedEntityURIs) {
        Map<String,Float> scores = new HashMap<>();

        Query query = QueryFactory.create(queryString) ;
        dataset.begin(ReadWrite.READ);
        try(QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
            ResultSet results = qexec.execSelect() ;
            
            assertEquals(expectedEntityURIs.size() > 0, results.hasNext());
            int count;
            for (count=0; results.hasNext(); count++) {
                QuerySolution soln = results.nextSolution();
                String entityUri = soln.getResource("s").getURI();
                assertTrue(expectedEntityURIs.contains(entityUri));
                float score = soln.getLiteral("score").getFloat();
                scores.put(entityUri, score);
            }
            assertEquals(expectedEntityURIs.size(), count);
        }
        finally {
            dataset.end() ;
        }
        return scores;
    }

    public static void doTestNoResult(Dataset dataset, String label, String queryString) {
        Query query = QueryFactory.create(queryString) ;
        dataset.begin(ReadWrite.READ);
        try(QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
            ResultSet results = qexec.execSelect() ;
            assertFalse(label, results.hasNext());
        }
        finally {
            dataset.end() ;
        }
    }

    protected void doUpdate(String updateString) {
        dataset.begin(ReadWrite.WRITE);
        UpdateRequest request = UpdateFactory.create(updateString);
        UpdateProcessor proc = UpdateExecutionFactory.create(request, dataset);
        proc.execute();
        dataset.commit();
    }
}
