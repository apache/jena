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

package org.apache.jena.arq.junit.sparql.tests ;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.arq.junit.LibTestSetup;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.junit.QueryTestException;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.sparql.vocabulary.TestManifestX;
import org.apache.jena.sparql.vocabulary.VocabTestQuery;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.TestManifest;

/**
 * Parse out details for a query evaluation test.
 * There are a lot of details so move out all
 * the parse/extract details in this class.
 */
public class QueryTestItem
{
    static int counter = 0 ;

    public static String fakeURI() {
        return "test:" + (++counter) ;
    }

    private Resource     testResource     = null ;
    private Resource     actionResource   = null ;

    private String       name ;

    private boolean      buildLuceneIndex = false ;
    private String       resultFile ;
    private String       comment ;
    private List<String> defaultGraphURIs ;
    private List<String> namedGraphURIs ;
    private Resource     testType         = null ;
    private String       queryFile ;

    public static QueryTestItem create(Resource entry, Resource defaultTestType) {
        return new QueryTestItem(entry, defaultTestType) ;
    }

    public static QueryTestItem create(String _name, String _queryFile, String _dataFile, String _resultFile) {
        return new QueryTestItem(_name, _queryFile, _dataFile, _resultFile) ;
    }

    private QueryTestItem(Resource entry, Resource defaultTestType) {
        testResource = entry ;

        if ( !entry.hasProperty(TestManifest.name) )
            throw new QueryTestException("TestItem with no name (" + entry + ")") ;
        name = _getName() ;

        if ( !entry.hasProperty(TestManifest.action) )
            throw new QueryTestException("TestItem '" + name + "' with no action") ;

        // Assumes one type per test only.
        testType = LibTestSetup.getResource(entry, RDF.type) ;
        if ( testType == null )
            testType = defaultTestType ;

        resultFile = _getResultFile() ;
        comment = _getComment() ;

        defaultGraphURIs = _getDefaultGraphURIs() ;
        namedGraphURIs = _getNamedGraphsURIs() ;

        queryFile = _getQueryFile() ;
        buildLuceneIndex = _getTextIndex() ;
    }

    private QueryTestItem(String _name, String _queryFile, String _dataFile, String _resultFile) {
        name = _name ;
        queryFile = _queryFile ;
        defaultGraphURIs = new ArrayList<>() ;
        defaultGraphURIs.add(_dataFile) ;
        namedGraphURIs = new ArrayList<>() ;
        resultFile = _resultFile ;
        comment = "" ;
    }

    public Resource getResource() {
        return testResource ;
    }

    public Resource getAction() {
        return _getAction() ;
    }

    /** @return Returns the testType. */
    public Resource getTestType() {
        return testType ;
    }

    public String getQueryFile() {
        return queryFile ;
    }

    public String getResultFile() {
        return resultFile ;
    }

    /**
     * Load results as a SPARQLResult. If the results are a model, no conversion
     * to a result set is attempted here.
     */
    public SPARQLResult getResults() {
        if ( resultFile == null )
            return null ;
        ResultsFormat format = ResultsFormat.guessSyntax(resultFile) ;

        if ( ResultsFormat.isRDFGraphSyntax(format) ) {
            // Load plain.
            Graph g = GraphFactory.createPlainGraph();
            SparqlTestLib.parser(resultFile).parse(g);
            Model m = ModelFactory.createModelForGraph(g);
            return new SPARQLResult(m) ;
        }

        if ( ResultsFormat.isDatasetSyntax(format) ) {
            Dataset d = RDFDataMgr.loadDataset(resultFile) ;
            return new SPARQLResult(d) ;
        }

        // Attempt to handle as a resultset or boolean result.s
        SPARQLResult x = ResultSetFactory.result(resultFile) ;
        return x ;
    }

    public String getName() {
        return name ;
    }

    public String getURI() {
        if ( testResource != null && testResource.isURIResource() )
            return testResource.getURI() ;
        return fakeURI() ;
    }

    public String getComment() {
        return comment ;
    }

    public List<String> getDefaultGraphURIs() {
        return defaultGraphURIs ;
    }

    public List<String> getNamedGraphURIs() {
        return namedGraphURIs ;
    }

    public boolean requiresTextIndex() {
        return buildLuceneIndex ;
    }

    private String _getName() {

        Statement s = testResource.getProperty(TestManifest.name) ;
        String ln = s.getSubject().getLocalName();
        if ( s == null )
            return ln+" <<unset>>" ;
        return "("+ln+") "+s.getString() ;
    }

    private Resource _getAction() {
        if ( actionResource == null )
            actionResource = testResource.getProperty(TestManifest.action).getResource() ;
        return actionResource ;
    }

    private String _getResultFile() {
        try {
            // It's bnode in some update tests.
            // The Update test code managed building the result.
            return LibTestSetup.getLiteralOrURI(testResource, TestManifest.result) ;
        } catch (RuntimeException ex) { return null ; }
    }

    private String _getComment() {
        Statement s = testResource.getProperty(RDFS.comment) ;
        if ( s == null )
            return null ;
        return s.getString() ;
    }

    // ----------------------------------------------------
    // ---- Query specific properties

    /**
     * Get the data file (default graph): maybe unknown if part for the query
     * (FROM)
     *
     * @return List
     */

    private List<String> _getDefaultGraphURIs() {
        if ( !_getAction().isAnon() )
            // Action is a URI - data had better be in the query itself.
            return null ;

        List<String> l = new ArrayList<>() ;
        ClosableIterator<Statement> cIter = _getAction().listProperties(VocabTestQuery.data) ;
        for (; cIter.hasNext();) {
            Statement stmt = cIter.next() ;
            String df = stmt.getResource().getURI() ;
            l.add(df) ;
        }
        cIter.close() ;

        return l ;
    }

    /**
     * Get the named graphs : maybe unknown if part for the query (FROM NAMED)
     *
     * @return List
     */

    private List<String> _getNamedGraphsURIs() {
        if ( !_getAction().isAnon() )
            // Action is a URI - data had better be in the query itself.
            return null ;

        List<String> l = new ArrayList<>() ;
        ClosableIterator<Statement> cIter = _getAction().listProperties(VocabTestQuery.graphData) ;
        for (; cIter.hasNext();) {
            Statement obj = cIter.next() ;
            String df = obj.getResource().getURI() ;
            l.add(df) ;
        }
        cIter.close() ;

        return l ;
    }

    /**
     * Get the query file: either it is the action (data in query) or it is
     * specified within the bNode as a query/data pair.
     *
     * @return
     */

    private String _getQueryFile() {
        Resource r = _getAction() ;

        if ( r.hasProperty(VocabTestQuery.query) )
            return LibTestSetup.getLiteralOrURI(r, VocabTestQuery.query) ;

        // No query property - must be this action node

        if ( _getAction().isAnon() )
            return "[]" ;
        return _getAction().getURI() ;
    }

    private boolean _getTextIndex() {
        Statement s = testResource.getProperty(TestManifestX.textIndex) ;
        if ( s == null )
            return false ;
        return s.getString().equalsIgnoreCase("true") ;
    }

    // ----------------------------------------------------
    // Misc

    @Override
    public String toString() {
        StringBuilder sbuff = new StringBuilder() ;
        String name = getName() ;
        // String actionStr = FmtUtils.stringForRDFNode(_getAction()) ;

        sbuff.append("Name: " + name) ;

        if ( getComment() != null )
            sbuff.append("    Comment: " + getComment()) ;
        return sbuff.toString() ;
    }
}
