/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.sparql.test;

import java.util.* ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.Log ;
import org.junit.runner.Description ;
import org.junit.runner.Runner ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.junit.* ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifest ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestUpdate_11 ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestX ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifest_11 ;
import com.hp.hpl.jena.vocabulary.RDF ;

/** In support of testing */
public class LibTestSPARQL
{
    public static EarlReport report = null ;
    public static int counter = 0 ;

    private final static String prefixes = 
        StrUtils.strjoinNL(
             "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
             "PREFIX : <http://www.w3.org/2001/sw/DataAccess/tests/data-r2/distinct/manifest#>",
             "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
             "PREFIX mf:     <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>",
             "PREFIX qt:     <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>",
             "PREFIX dawgt:   <http://www.w3.org/2001/sw/DataAccess/tests/test-dawg#>",
             "PREFIX apf:     <http://jena.hpl.hp.com/ARQ/property#>",
             "PREFIX afn:     <http://jena.hpl.hp.com/ARQ/function#>",
             "PREFIX list:    <http://jena.hpl.hp.com/ARQ/list#>") ;

    // qt:data and qt:graphData - multiple.
    private final static String testsQueryString =
        StrUtils.strjoinNL(prefixes,
            //"SELECT ?manifestName ?name ?test ?action ?query ?result ?testType  {",
            "SELECT *  {",
            "  ?z mf:entries [ list:member ?test ] . ",
            "",
            "  OPTIONAL { ?test mf:name ?name . }",
            "  OPTIONAL { ?test rdf:type ?testType . }",
            "  OPTIONAL { ?test mf:action ?action",
            "        OPTIONAL { ?action qt:query ?q }",
            "        BIND( COALESCE(?q,?action) AS ?query)",
            "   }",
            "  OPTIONAL { ?test mf:result ?result }", 
            "",
            "  BIND( REPLACE(str(?query), '.*/', '')  AS ?_query )",
            "  BIND( REPLACE(str(?result), '.*/', '') AS ?_result )", "} ") ;
    
    
    private final static String includedQueryString =
        StrUtils.strjoinNL(prefixes,
            "SELECT * {",
            "  ?z  mf:include ?x .",
            //"  OPTIONAL{?z  rdfs:label|rdfs:comment ?manifestName }",
            "  ?x list:member ?included .",
            "}") ;
    
    private final static String manifestNameQueryString = 
        StrUtils.strjoinNL(prefixes,
                           "SELECT * {",
                           "  ?z rdf:type mf:Manifest ",
                           "  OPTIONAL{?z  rdfs:label|rdfs:comment ?manifestName }",
                           "}") ;
    
    public static List<String> getNames(Model model) {
        return findStrings(model, manifestNameQueryString, "manifestName") ;
    }
        
    public static List<String> getIncludes(Model model) {
        return findResourceURIs(model, includedQueryString, "included") ;
    }

    private static List<String> findStrings(Model model, String queryString, String varName) {
        try(QueryExecution qExec = execute(queryString, model)) {
            List<String> strings = new ArrayList<>() ;
            ResultSetRewindable rs = ResultSetFactory.copyResults(qExec.execSelect()) ;
            while (rs.hasNext()) {
                QuerySolution row = rs.nextSolution() ;
                String included = row.getLiteral(varName).getLexicalForm() ;
                strings.add(included) ;
            }
            return strings ;
        }
    }
    
    private static List<String> findResourceURIs(Model model, String queryString, String varName) {
        try(QueryExecution qExec = execute(queryString, model)) {
            List<String> strings = new ArrayList<>() ;
            ResultSetRewindable rs = ResultSetFactory.copyResults(qExec.execSelect()) ;
            while (rs.hasNext()) {
                QuerySolution row = rs.nextSolution() ;
                Resource included = row.getResource(varName) ;
                strings.add(included.getURI()) ;
            }
            return strings ;
        }
    }

    // We use JUnit3 TestCase so as to work with the existing QueryTest etc
    // If that is retired, then create a "TestSPARQL".

    public static List<EarlTestCase> generateTests(Model model) {
        try(QueryExecution qExec = execute(testsQueryString, model)) {
            List<EarlTestCase> testData = new ArrayList<>() ;

            ResultSetRewindable rs = ResultSetFactory.copyResults(qExec.execSelect()) ;
            // Checks.
            Map<RDFNode, QuerySolution> tests = new LinkedHashMap<>() ;

            while (rs.hasNext()) {
                QuerySolution row = rs.nextSolution() ;
                Resource entry = row.getResource("test") ;
                //System.out.println(row) ;
                if ( tests.containsKey(entry) ) {
                    QuerySolution row0 = tests.get(entry) ;
                    System.out.println(row0) ;
                    System.out.println(row) ;
                    System.err.println("Test already seen: " + entry) ;
                }
                String name = row.getLiteral("name").getString() ;
                Resource testType = row.getResource("testType") ;
                Resource query = row.getResource("query") ;
                Resource action = row.getResource("action") ;
                Resource result = row.getResource("result") ;

                String fileName ;
                if ( query.isURIResource() )
                    fileName = query.getURI().replaceAll("^.*/", "") ;
                else
                    fileName = "b"+(++counter) ;// query.getId().getLabelString() ;

                TestItem item = TestItem.create(entry, null) ;

                EarlTestCase test = makeTest(item, entry, name, action, result) ;
                tests.put(entry, row) ;
                testData.add(test) ;
            }
            return testData ;
        }
    }

    public static EarlTestCase makeTest(TestItem item, Resource entry, String testName, Resource action, Resource result) {
        if ( entry == null ) {
            System.err.println("Null entry") ;
            return null ;
        }

        Resource testType = null ;
        if ( entry.hasProperty(RDF.type) )
            testType = entry.getProperty(RDF.type).getResource() ;

        EarlTestCase test = null ;

        if ( testType != null ) {
            // == Good syntax
            if ( testType.equals(TestManifest.PositiveSyntaxTest) )
                return new SyntaxTest(testName, report, item) ;
            if ( testType.equals(TestManifest_11.PositiveSyntaxTest11) )
                return new SyntaxTest(testName, report, item) ;
            if ( testType.equals(TestManifestX.PositiveSyntaxTestARQ) )
                return new SyntaxTest(testName, report, item) ;

            // == Bad
            if ( testType.equals(TestManifest.NegativeSyntaxTest) )
                return new SyntaxTest(testName, report, item, false) ;
            if ( testType.equals(TestManifest_11.NegativeSyntaxTest11) )
                return new SyntaxTest(testName, report, item, false) ;
            if ( testType.equals(TestManifestX.NegativeSyntaxTestARQ) )
                return new SyntaxTest(testName, report, item, false) ;

            // ---- Update tests
            if ( testType.equals(TestManifest_11.PositiveUpdateSyntaxTest11) )
                return new SyntaxUpdateTest(testName, report, item, true) ;
            if ( testType.equals(TestManifest_11.NegativeUpdateSyntaxTest11) )
                return new SyntaxUpdateTest(testName, report, item, false) ;

            // Two names for same thing.
            // Note item is not passed down.
            if ( testType.equals(TestManifestUpdate_11.UpdateEvaluationTest) )
                return UpdateTest.create(testName, report, entry, action, result) ;
            if ( testType.equals(TestManifest_11.UpdateEvaluationTest) )
                return UpdateTest.create(testName, report, entry, action, result) ;

            // ----

            if ( testType.equals(TestManifestX.TestSerialization) )
                return new TestSerialization(testName, report, item) ;

            if ( testType.equals(TestManifest.QueryEvaluationTest) || testType.equals(TestManifestX.TestQuery) )
                return new QueryTest(testName, report, item) ;

            // Reduced is funny.
            if ( testType.equals(TestManifest.ReducedCardinalityTest) )
                return new QueryTest(testName, report, item) ;

            if ( testType.equals(TestManifestX.TestSurpressed) )
                return new SurpressedTest(testName, report, item) ;

            if ( testType.equals(TestManifest_11.CSVResultFormatTest) ) {
                Log.warn("Tests", "Skip CSV test: " + testName) ;
                return null ;
            }

            System.err.println("Test type '" + testType + "' not recognized") ;
        }
        // Default
        test = new QueryTest(testName, report, item) ;
        return test ;
    }

    public static void setUpManifests(Description description, List<Runner> runners, List<String> manifests) {
        for ( String x : manifests ) {
            Runner r = new RunnerOneManifest(x) ;
            description.addChild(r.getDescription());
            runners.add(r) ;
        }
    }
    
    private static QueryExecution execute(String queryString, Model model) {
        Dataset ds = DatasetFactory.create(model) ;
        ds.getContext().setFalse(ARQ.strictSPARQL);  // Else property functions are turned off.
        QueryExecution qExec = QueryExecutionFactory.create(queryString, ds) ;
        //qExec.getContext().setFalse(ARQ.strictGraph);
        return qExec ;
    }
    
    // Keep Eclipse happy.
    public static String fixupName(String string) {
        string = string.replace('(', '[') ;
        string = string.replace(')', ']') ;
        return string ;
    }
}