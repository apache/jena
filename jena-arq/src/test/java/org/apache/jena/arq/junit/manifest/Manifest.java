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

package org.apache.jena.arq.junit.manifest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.vocabulary.TestManifestX;
import org.apache.jena.system.G;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.TestManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A "Manifest" is the includes and entries for a single file.
 */
public class Manifest
{
    private static Logger log = LoggerFactory.getLogger(Manifest.class);
    private final Graph manifestGraph;
    private final Node manifest;
    private String manifestName;
    // Assumed base URI for the tests - this makes downloaded manifest tests assume their original location work.
    private String manifestTestBase;
    private String filenameOrURI;
    private List<String> includedFiles = new ArrayList<>();
    private List<ManifestEntry> entries = new ArrayList<>();

    public static Manifest parse(String filenameOrURI) {
        Graph manifestRDF;
        // Exceptions from @TestFactories are swallowed by JUnit5.
        try {
            manifestRDF = RDFParser.source(filenameOrURI).toGraph();
        } catch (RiotException ex) {
            // Exit on error.
            log.error("Error reading manifest: "+filenameOrURI);
            System.exit(1);
            throw ex;
        } catch (RuntimeException ex) {
            // Exit on error.
            log.error("Error reading manifest: "+filenameOrURI);
            System.exit(1);
            throw ex;
        }
        Manifest manifest = new Manifest(manifestRDF);
        return manifest;
    }

    private Manifest(Graph manifestRDF) {
        manifestGraph = manifestRDF;
        manifest = getManifestNode(manifestGraph, filenameOrURI);
        parseManifest();
        parseIncludes();
        parseEntries();
    }

    private static Node getManifestNode(Graph manifestGraph, String filename) {
        List<Node> manifests = G.nodesOfTypeAsList(manifestGraph, TestManifest.Manifest.asNode());
        if ( manifests.size() > 1 ) {
            log.warn("Multiple manifests in the manifest file: " + filename);
            return null;
        }
        return manifests.get(0);
    }

    public String getName()     { return manifestName; }
    public String getFileName() { return filenameOrURI; }
    public String getTestBase() { return manifestTestBase; }
    public Graph  getGraph()    { return manifestGraph; }

    public Iterator<String> includedManifests() { return includedFiles.iterator(); }

    public List<ManifestEntry> entries() { return entries; }

    private void parseManifest() {
        manifestName = getLiteral(manifest, RDFS.Nodes.label);
        if ( manifestName == null )
            manifestName = getLiteral(manifest, RDFS.Nodes.comment);
        if ( manifestName == null )
            manifestName = getLiteral(manifest, TestManifest.name.asNode());
        manifestTestBase = getLiteralOrURI(manifest, TestManifest.assumedTestBase.asNode());
    }

    private void parseEntries() {
        Node entriesNode = G.getZeroOrOneSP(manifestGraph, manifest, TestManifest.entries.asNode());
        if ( entriesNode == null )
            return;
        List<Node> items = G.rdfList(manifestGraph, entriesNode);
        items.forEach(entry->{
            String testName = getLiteral(entry, TestManifest.name.asNode());
            Node testType = G.getZeroOrOneSP(manifestGraph, entry, RDF.Nodes.type);
            Node action = G.getZeroOrOneSP(manifestGraph, entry,  TestManifest.action.asNode());
            Node result = G.getZeroOrOneSP(manifestGraph, entry,  TestManifest.result.asNode());
            ManifestEntry manifestEntry = new ManifestEntry(this, entry, testName, testType, action, result);
            entries.add(manifestEntry);
        });
    }

    private void parseIncludes() {
        parseIncludes(TestManifest.include.asNode());
        parseIncludes(TestManifestX.include.asNode());
    }

    private void parseIncludes(Node property) {
        List<Node> includes = G.listSP(manifestGraph, null, property);
        includes.forEach(include->{
            if ( include.isBlank() || include.isURI() ) {
                parseOneIncludesList(include);
            } else
                log.warn("Include: not a URI or blank node: "+include);
        });
    }

    private void parseOneIncludesList(Node r)
    {
        if ( r == null )
            return;
        if ( r.equals(RDF.Nodes.nil) )
            return;

        if ( r.isURI() ) {
            String uri = r.getURI();
            if ( includedFiles.contains(uri) )
                return;
            includedFiles.add(r.getURI());
            return;
        }
        if ( ! r.isBlank() )
            return;
        // Blank node - assumed to be a list.
        List<Node> includes = G.rdfList(manifestGraph, r);
        for ( Node inc : includes ) {
            if ( inc.isBlank() || inc.isURI() ) {
                parseOneIncludesList(inc);
                continue;
            }
            log.warn("Include: not a URI or blank node: "+inc);
        }
    }

    private String getLiteral(Node r, Node p) {
        if ( r == null )
            return null;
        Node n = G.getZeroOrOneSP(manifestGraph, r, p);
        if ( n == null )
            return null;

        if ( n.isLiteral() )
            return n.getLiteralLexicalForm();

        throw new TestSetupException("Manifest problem (not a Literal): " + r + " => " + p);
    }

    private String getLiteralOrURI(Node r, Node p)
    {
        if ( r == null )
            return null;
        Node n = G.getZeroOrOneSP(manifestGraph, r, p);
        if ( n == null )
            return null;
        if ( n.isLiteral() )
            return n.getLiteralLexicalForm();
        if ( n.isURI() )
            return n.getURI();
        throw new TestSetupException("Manifest problem: "+r+" => "+p);
    }

    @Override
    public String toString() {
        return "manifest["+filenameOrURI+"]";
    }
}
