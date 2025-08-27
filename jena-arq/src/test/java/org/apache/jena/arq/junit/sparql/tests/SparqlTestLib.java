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

import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.arq.junit.manifest.TestSetupException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.sparql.vocabulary.TestManifestX;
import org.apache.jena.sparql.vocabulary.VocabTestQuery;
import org.apache.jena.system.G;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/** Misc code to make the tests clearer */
class SparqlTestLib {

    // Data Parser - no warnings.
    static RDFParserBuilder parser(String sourceURI) {
        return RDFParser.create().source(sourceURI).errorHandler(ErrorHandlerFactory.errorHandlerNoWarnings);
    }

    static void setupFailure(String msg) {
        throw new RuntimeException(msg);
    }

    static void testFailure(String msg) {
        throw new AssertionError(msg);
    }

//    /**
//     * Get the query file: either it is the action (data in query) or it is
//     * specified within the bNode as a query/data pair.
//     *
//     * @return
//     */
//    private String _getQueryFile() {
//        Node queryFile = G.getZeroOrOneSP(graph, testResource, VocabTestQuery.query.asNode());
//        if ( queryFile != null )
//            return getStringOrURI(queryFile, "query file");
//
//        // No query property - must be this action node
//
//        if ( actionResource.isBlank() )
//            throw new TestSetupException("Can't determine the query from the action");
//        return actionResource.getURI();
//    }

    static String getStringOrURI(Node node, String context) {
        if ( node.isLiteral() )
            return node.getLiteralLexicalForm();
        if ( node.isURI() )
            return node.getURI();
        if ( context == null )
            throw new TestSetupException("Not a string or URI: "+node);
        throw new TestSetupException("Not a string or URI for "+context+": "+node);
    }

    static String queryFile(ManifestEntry entry) {
        Graph graph = entry.getGraph();
        Node testResource = entry.getEntry();
        Node queryFile = G.getZeroOrOneSP(graph, testResource, VocabTestQuery.query.asNode());
        if ( queryFile != null )
            return getStringOrURI(queryFile, "query file");

        // No query property - must be this action node

        if ( entry.getAction().isBlank() ) {
            // action -> :query
            Node x = G.getZeroOrOneSP(graph, entry.getAction(), VocabTestQuery.query.asNode());
            if ( x == null )
                throw new TestSetupException("Can't determine the query from the action");
            return x.getURI();
        }
        return entry.getAction().getURI();
    }

    static Query queryFromEntry(ManifestEntry entry) {
        return queryFromEntry(entry, null);
    }

    /** read a query, forcing syntax */
    static Query queryFromEntry(ManifestEntry entry, Syntax syntax) {
        if ( queryFile(entry) == null ) {
            SparqlTestLib.setupFailure("Query test file is null");
            return null;
        }

        Syntax syn = syntax;
        if ( syn == null )
            syn = querySyntax(entry, null);

        Query query = QueryFactory.read(queryFile(entry), null, syn);
        return query;
    }

    private static Syntax querySyntax(ManifestEntry entry, Syntax def) {
        Graph graph = entry.getGraph();
        Node r = entry.getAction();
        if ( G.hasProperty(graph, r, TestManifestX.querySyntax.asNode()) ) {
            Node n = G.getOneSP(graph, r, TestManifestX.querySyntax.asNode());
            Syntax x = Syntax.make(n.getURI());
            return x;
        }

        Node q = G.getZeroOrOneSP(graph, r, VocabTestQuery.query.asNode());
        if ( q == null )
            q = entry.getAction();

        if ( q == null ) {
            //throw new TestSetupException("No query");
            // Default on manifest?
            return Syntax.syntaxSPARQL_11;
        }

        String uri = q.getURI();
        if ( uri != null ) {
            Syntax synFileName = guessFileSyntax(uri);
            if ( synFileName != null )
                return synFileName;
        }
        return def;
    }

    // Allow *.rq is strictly SPARQL 1.1 tests.
    // but RDF-star test may fail.
    protected static Syntax guessFileSyntax(String filename) {
//        if ( filename.endsWith(".rq") )
//            return Syntax.syntaxSPARQL_11;
//        if ( filename.endsWith(".ru") )
//            return Syntax.syntaxSPARQL_11;

        return Syntax.guessFileSyntax(filename);
    }
//
//    static UpdateRequest updateFromString(String str) {
//        return UpdateFactory.create(str);
//    }
//
    static UpdateRequest updateFromEntry(ManifestEntry entry) {
        return updateFromEntry(entry, null);
    }

    static UpdateRequest updateFromEntry(ManifestEntry entry, Syntax syntax) {

        if ( queryFile(entry) == null ) {
            SparqlTestLib.setupFailure("Query test file is null");
            return null;
        }
        String fn = queryFile(entry);
        Syntax syn = (syntax!=null) ? syntax : guessFileSyntax(fn);

        UpdateRequest request = UpdateFactory.read(fn, syn);
        return request;
    }
}
