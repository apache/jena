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

package com.hp.hpl.jena.sparql.junit ;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat ;
import com.hp.hpl.jena.sparql.resultset.SPARQLResult ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifest ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestX ;
import com.hp.hpl.jena.sparql.vocabulary.VocabTestQuery ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.util.iterator.ClosableIterator ;
import com.hp.hpl.jena.util.junit.TestException ;
import com.hp.hpl.jena.util.junit.TestUtils ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;

/**
 * Wrapper class for individual test items. Assumes it is a query test item,
 * using both the manifest vocabulary and the test query vocabulary.
 */
public class TestItem
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
    private Syntax       queryFileSyntax ;

    public static TestItem create(Resource entry, Resource defaultTestType) {
        return new TestItem(entry, defaultTestType) ;
    }

    public static TestItem create(String _name, String _queryFile, String _dataFile, String _resultFile) {
        return new TestItem(_name, _queryFile, _dataFile, _resultFile) ;
    }

    private TestItem(Resource entry, Resource defaultTestType) {
        testResource = entry ;

        if ( !entry.hasProperty(TestManifest.name) )
            throw new QueryTestException("TestItem with no name (" + entry + ")") ;
        name = _getName() ;

        if ( !entry.hasProperty(TestManifest.action) )
            throw new QueryTestException("TestItem '" + name + "' with no action") ;

        // Assumes one type per test only.
        testType = TestUtils.getResource(entry, RDF.type) ;
        if ( testType == null )
            testType = defaultTestType ;

        resultFile = _getResultFile() ;
        comment = _getComment() ;

        defaultGraphURIs = _getDefaultGraphURIs() ;
        namedGraphURIs = _getNamedGraphsURIs() ;

        queryFile = _getQueryFile() ;
        queryFileSyntax = _getQuerySyntax(entry.getModel(), queryFile, Syntax.syntaxARQ) ;
        buildLuceneIndex = _getTextIndex() ;
    }

    private TestItem(String _name, String _queryFile, String _dataFile, String _resultFile) {
        name = _name ;
        queryFile = _queryFile ;
        defaultGraphURIs = new ArrayList<>() ;
        defaultGraphURIs.add(_dataFile) ;
        namedGraphURIs = new ArrayList<>() ;
        resultFile = _resultFile ;
        comment = "" ;
        queryFileSyntax = Syntax.guessFileSyntax(_queryFile) ;
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

    public Syntax getFileSyntax() {
        return queryFileSyntax ;
    }

    public void setFileSyntax(Syntax syntax) {
        queryFileSyntax = syntax ;
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
            Model m = FileManager.get().loadModel(resultFile) ;
            return new SPARQLResult(m) ;
        }

        // Attempt to handle as a resulset or boolean result.s
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
        if ( s == null )
            return "<<unset>>" ;
        return s.getString() ;
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
            return TestUtils.getLiteralOrURI(testResource, TestManifest.result) ;
        } catch (TestException ex) { return null ; }
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
            return TestUtils.getLiteralOrURI(r, VocabTestQuery.query) ;

        // No query property - must be this action node

        if ( _getAction().isAnon() )
            return "[]" ;
        return _getAction().getURI() ;
    }

    private Syntax _getQuerySyntax(Model m, String uri, Syntax def) {
        Resource r = m.createResource(uri) ;
        if ( r.hasProperty(TestManifestX.querySyntax) ) {
            Syntax x = Syntax.make(r.getProperty(TestManifestX.querySyntax).getResource().getURI()) ;
            // System.err.println("Query syntax: "+x) ;
            return x ;
        }

        if ( uri != null ) {
            Syntax synFileName = Syntax.guessFileSyntax(uri) ;
            if ( synFileName != null )
                return synFileName ;
        }
        return def ;
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
