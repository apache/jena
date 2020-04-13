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

package org.apache.jena.riot.system;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.iri.IRI;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.sparql.core.Quad;

/**
 * {@code ParserProfile} is specific to parsing, providing the operations needed by a parser to
 * create IRIs/Nodes/Triples/Quads at the point in the parsing process when the line and
 * column are available to put in error messages.
 * <p>
 * {@code ParserProfile} uses a {@link FactoryRDF} to create items in the parsing
 * process. A {@code ParserProfile} adds handling the position in the parsing stream,
 * and URI processing (prefix mapping and base URI).
 *
 * @see FactoryRDF
 */
public interface ParserProfile {
    /** Resolve a URI, returning a string */
    public String resolveIRI(String uriStr, long line, long col);

    /**
     * Resolve a URI, returning an IRI.
     * @deprecated Use {@link #resolveIRI(String, long, long)}. This method will be removed.
     */
    @Deprecated
    public default IRI makeIRI(String uriStr, long line, long col) {
        String resolved = resolveIRI(uriStr, line, col);
        return IRIResolver.parseIRI(resolved);
    }

    /* Reset the base for IRI resolution. */
    public void setBaseIRI(String baseIRI);

    /** Create a triple */
    public Triple createTriple(Node subject, Node predicate, Node object, long line, long col);

    /** Create a quad */
    public Quad createQuad(Node graph, Node subject, Node predicate, Node object, long line, long col);

    /** Create a URI Node */
    public Node createURI(String uriStr, long line, long col);

    /** Create a literal for a string+datatype */
    public Node createTypedLiteral(String lexical, RDFDatatype datatype, long line, long col);

    /** Create a literal for a string+language */
    public Node createLangLiteral(String lexical, String langTag, long line, long col);

    /** Create a literal for a string */
    public Node createStringLiteral(String lexical, long line, long col);

    /** Create a fresh blank node based on scope and label */
    public Node createBlankNode(Node scope, String label, long line, long col);

    /** Create a fresh blank node */
    public Node createBlankNode(Node scope, long line, long col);

    /** Create a triple node (RDF*) */
    public Node createTripleNode(Node subject, Node predicate, Node object, long line, long col);

    /** Create a triple node (RDF*) */
    public Node createTripleNode(Triple triple, long line, long col);

    /** Create a graph node. This is an N3-formula and not named graphs */
    public Node createGraphNode(Graph graph, long line, long col);

    /**
     * Make a node from a token - called after all else has been tried to handle
     * special cases Return null for "no special node recognized"
     */
    public Node createNodeFromToken(Node scope, Token token, long line, long col);

    /** Make any node from a token as appropriate */
    public Node create(Node currentGraph, Token token);

    /** Is this in strict mode? */
    public boolean isStrictMode();

    /* Return the prefix map, if any, used for mapping tokens into Nodes. */
    public PrefixMap getPrefixMap();

    /** Get the {@link ErrorHandler error handler} used by this {@code ParserProfile} */
    public ErrorHandler getErrorHandler();

    /** Get the {@link FactoryRDF factory for RDF terms, triples and quads} */
    public FactoryRDF getFactorRDF();
}
