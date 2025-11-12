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

package org.apache.jena.arq.junit.sparql.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.arq.junit.manifest.TestSetupException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.sparql.vocabulary.TestManifestX;
import org.apache.jena.sparql.vocabulary.VocabTestQuery;
import org.apache.jena.system.G;
import org.apache.jena.util.SplitIRI;
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
    static int counter = 0;

    public static String fakeURI() {
        return "test:" + (++counter);
    }

    private final ManifestEntry manifestEntry;
    private final Graph       graph;
    private final Node        testResource;
    private final Node        actionResource;

    private final String       name;

    private boolean      buildLuceneIndex = false;
    private final String       resultFile;
    private final String       comment;
    private List<String> defaultGraphURIs;
    private List<String> namedGraphURIs;
    private final Node         testType;
    private final String       queryFile;

    public static QueryTestItem create(ManifestEntry entry, Node defaultTestType) {
        return new QueryTestItem(entry, defaultTestType);
    }

    private QueryTestItem(ManifestEntry entry, Node defaultTestType) {
        manifestEntry = entry;
        graph = entry.getGraph();
        Objects.requireNonNull(graph, "Manifest graph");

        testResource = entry.getEntry();
        actionResource = G.getOneSP(graph, testResource, TestManifest.action.asNode());

        if ( ! G.hasProperty(graph, testResource, TestManifest.name.asNode()) )
            throw new TestSetupException("TestItem with no name (" + entry + ")");
        name = _getName();

        if ( ! G.hasProperty(graph, testResource, TestManifest.action.asNode()) )
            throw new TestSetupException("TestItem '" + name + "' with no action");

        // Assumes one type per test only.
        Node _testType = G.getZeroOrOneSP(graph, testResource, RDF.Nodes.type);
        if ( _testType == null )
            _testType = defaultTestType;
        testType = _testType;

        resultFile = _getResultFile();
        comment = _getComment();

        defaultGraphURIs = _getDefaultGraphURIs();
        namedGraphURIs = _getNamedGraphsURIs();

        queryFile = SparqlTestLib.queryFile(entry);
        buildLuceneIndex = _getTextIndex();
    }

    public Node getResource() {
        return testResource;
    }

    public Node getAction() {
        return actionResource;
    }

    /** @return Returns the testType. */
    public Node getTestType() {
        return testType;
    }

    public String getQueryFile() {
        return queryFile;
    }

    public String getResultFile() {
        return resultFile;
    }

    /**
     * Load tests results as a SPARQLResult.
     * <p>
     * If the results are an RDF graph, no
     * conversion to a result set is attempted here.
     */
    public SPARQLResult getResults() {
        if ( resultFile == null )
            return null;
        Lang lang = RDFLanguages.pathnameToLang(resultFile);

        if ( ResultSetLang.isRegistered(lang) ) {
            // Attempt to handle as a resultset or boolean result.s
            SPARQLResult x = ResultSetFactory.result(resultFile);
            return x;
        }

        if ( RDFLanguages.isTriples(lang) ) {
            // Load plain. No warnings.
            Model m = SparqlTestLib.parser(resultFile).toModel();
            return new SPARQLResult(m);
        } else {
            Dataset d = SparqlTestLib.parser(resultFile).toDataset();
            return new SPARQLResult(d);
        }
    }

    public String getName() {
        return name;
    }

    public String getURI() {
        if ( testResource != null && testResource.isURI() )
            return testResource.getURI();
        return fakeURI();
    }

    public String getComment() {
        return comment;
    }

    public ManifestEntry getManifestEntry() {
        return manifestEntry;
    }

    public List<String> getDefaultGraphURIs() {
        return defaultGraphURIs;
    }

    public List<String> getNamedGraphURIs() {
        return namedGraphURIs;
    }

    public boolean requiresTextIndex() {
        return buildLuceneIndex;
    }

    private String _getName() {
        Node x = G.getOneSP(graph, testResource, TestManifest.name.asNode());
        if ( ! testResource.isURI() )
            return G.asString(x);
        String ln = SplitIRI.localname(testResource.getURI());
        return "("+ln+") "+G.asString(x);
    }

//    private Resource _getAction() {
//        if ( actionResource == null )
//            actionResource = testResource.getProperty(TestManifest.action).getResource();
//        return actionResource;
//    }
//
    private String _getResultFile() {
        Node x = G.getZeroOrOneSP(graph, testResource, TestManifest.result.asNode());
        if ( x == null )
            return null;
        return SparqlTestLib.getStringOrURI(x, "result file");
    }

    private String _getComment() {
        Node c = G.getZeroOrOneSP(graph, testResource, RDFS.Nodes.comment);
        if ( c == null )
            return null;
        return c.getLiteralLexicalForm();
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
        if ( ! actionResource.isBlank() )
            // Action is not a blank node - the data had better be in the query itself.
            return null;

        List<String> l = new ArrayList<>();
        G.listSP(graph, actionResource, VocabTestQuery.data.asNode()).forEach(x->{
            if ( ! x.isURI() )
                throw new TestSetupException("Deafult Graph URI is not a URI: "+x);
            l.add(x.getURI());
        });
        return l;
    }

    /**
     * Get the named graphs : maybe unknown if part for the query (FROM NAMED)
     *
     * @return List
     */

    private List<String> _getNamedGraphsURIs() {
        if ( ! actionResource.isBlank() )
            // Action is not a blank node - the data had better be in the query itself.
            return null;

        List<String> l = new ArrayList<>();
        G.listSP(graph, actionResource, VocabTestQuery.graphData.asNode()).forEach(x->{
            if ( ! x.isURI() )
                throw new TestSetupException("Deafult Graph URI is not a URI: "+x);
            l.add(x.getURI());
        });
        return l;
    }

    private boolean _getTextIndex() {
        Node x = G.getZeroOrOneSP(graph, testResource, TestManifestX.textIndex.asNode());
        if ( x == null )
            return false;
        return G.asString(x).equalsIgnoreCase("true");
    }

    // ----------------------------------------------------
    // Misc

    @Override
    public String toString() {
        StringBuilder sbuff = new StringBuilder();
        String name = getName();
        // String actionStr = FmtUtils.stringForRDFNode(_getAction());

        sbuff.append("Name: " + name);

        if ( getComment() != null )
            sbuff.append("    Comment: " + getComment());
        return sbuff.toString();
    }
}
