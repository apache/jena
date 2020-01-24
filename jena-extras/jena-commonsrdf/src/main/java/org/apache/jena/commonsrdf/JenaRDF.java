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

package org.apache.jena.commonsrdf;

import org.apache.commons.rdf.api.*;
import org.apache.jena.commonsrdf.impl.JCR_Factory;
import org.apache.jena.sys.JenaSystem;

/** RDFTermFactory with Jena-backed objects.
 * <p>
 * This is the class that implements {@link org.apache.commons.rdf.api.RDF}.
 *
 * @see RDFTermFactory
 */
public class JenaRDF implements RDF {

    static { JenaSystem.init(); }

    @Override
    public BlankNode createBlankNode() {
        return JCR_Factory.createBlankNode();
    }

    @Override
    public BlankNode createBlankNode(String name) {
        return JCR_Factory.createBlankNode(name);
    }

    @Override
    public Graph createGraph() {
        return JCR_Factory.createGraph();
    }

    @Override
    public Dataset createDataset() {
        return JCR_Factory.createDataset();
    }

    @Override
    public IRI createIRI(String iri) {
        validateIRI(iri);
        return JCR_Factory.createIRI(iri);
    }

    // Some simple validations - full IRI parsing is not cheap.
    private static void validateIRI(String iri) {
        if ( iri.contains(" ") ) throw new IllegalArgumentException();
        if ( iri.contains("<") ) throw new IllegalArgumentException();
        if ( iri.contains(">") ) throw new IllegalArgumentException();
    }

    @Override
    public Literal createLiteral(String lexicalForm) {
        return JCR_Factory.createLiteral(lexicalForm);
    }

    @Override
    public Literal createLiteral(String lexicalForm, IRI dataType) {
        return JCR_Factory.createLiteralDT(lexicalForm, dataType.getIRIString());
    }

    @Override
    public Literal createLiteral(String lexicalForm, String languageTag) {
        validateLang(languageTag);
        return JCR_Factory.createLiteralLang(lexicalForm, languageTag);
    }

    private static void validateLang(String languageTag) {
        if ( languageTag.contains(" ") ) throw new IllegalArgumentException();
    }

    @Override
    public Triple createTriple(BlankNodeOrIRI subject, IRI predicate, RDFTerm object) {
        return JCR_Factory.createTriple(subject, predicate, object);
    }

    @Override
    public Quad createQuad(BlankNodeOrIRI graphName, BlankNodeOrIRI subject, IRI predicate,
                           RDFTerm object) throws IllegalArgumentException {
        return JCR_Factory.createQuad(graphName, subject, predicate, object);
    }

}

