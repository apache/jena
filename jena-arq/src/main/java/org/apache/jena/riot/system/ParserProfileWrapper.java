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
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.sparql.core.Quad;

public class ParserProfileWrapper implements ParserProfile
{
    private final ParserProfile other;
    protected ParserProfile get() { return other; }
    
    public ParserProfileWrapper(ParserProfile other) {
        this.other = other;
    }
    
    @Override
    public FactoryRDF getFactorRDF() {
        return get().getFactorRDF();
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return get().getErrorHandler();
    }

    @Override
    public boolean isStrictMode() {
        return get().isStrictMode();
    }

    @Override
    public String resolveIRI(String uriStr, long line, long col) {
        return get().resolveIRI(uriStr, line, col);
    }

    @Override
    public void setBaseIRI(String baseIRI) { get().setBaseIRI(baseIRI); }

    @Override
    public Triple createTriple(Node subject, Node predicate, Node object, long line, long col) {
        return get().createTriple(subject, predicate, object, line, col);
    }

    @Override
    public Quad createQuad(Node graph, Node subject, Node predicate, Node object, long line, long col) {
        return get().createQuad(graph, subject, predicate, object, line, col);
    }

    @Override
    public Node createURI(String uriStr, long line, long col) {
        return get().createURI(uriStr, line, col);
    }

    @Override
    public Node createTypedLiteral(String lexical, RDFDatatype datatype, long line, long col) {
        return get().createTypedLiteral(lexical, datatype, line, col);
    }

    @Override
    public Node createLangLiteral(String lexical, String langTag, long line, long col) {
        return get().createLangLiteral(lexical, langTag, line, col);
    }

    @Override
    public Node createStringLiteral(String lexical, long line, long col) {
        return get().createStringLiteral(lexical, line, col);
    }

    @Override
    public Node createBlankNode(Node scope, String label, long line, long col) {
        return get().createBlankNode(scope, label, line, col);
    }

    @Override
    public Node createBlankNode(Node scope, long line, long col) {
        return get().createBlankNode(scope, line, col);
    }

    @Override
    public Node createNodeFromToken(Node scope, Token token, long line, long col) {
        return get().createNodeFromToken(scope, token, line, col);
    }

    @Override
    public Node create(Node currentGraph, Token token) {
        return get().create(currentGraph, token);
    }

    @Override
    public PrefixMap getPrefixMap() {
        return get().getPrefixMap();
    }
}