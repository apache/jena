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

package org.apache.jena.riot.system ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.checker.CheckerIRI ;
import org.apache.jena.riot.checker.CheckerLiterals ;
import org.apache.jena.riot.lang.LabelToNode ;

import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

/**
 * ParserProfile that performs checks:
 * <ul>
 * <li>IRI violations</li>
 * <li>Valid lexical forms for literals</li>
 * <li>Valid triples (e.g. literals as subjects)</li>
 * <li>Valid quads</li>
 * </ul>
 */

public class ParserProfileChecker extends ParserProfileBase // implements
                                                            // ParserProfile
{
    private boolean checkLiterals = true ;

    public ParserProfileChecker(Prologue prologue, ErrorHandler errorHandler) {
        super(prologue, errorHandler) ;
    }

    public ParserProfileChecker(Prologue prologue, ErrorHandler errorHandler, LabelToNode labelMapping) {
        super(prologue, errorHandler, labelMapping) ;
    }

    @Override
    public String resolveIRI(String uriStr, long line, long col) {
        // Go via code that checks.
        return makeIRI(uriStr, line, col).toString() ;
    }

    @Override
    public IRI makeIRI(String uriStr, long line, long col) {
        // resolves, but we handle the errors and warnings.
        IRI iri = prologue.getResolver().resolveSilent(uriStr) ;
        CheckerIRI.iriViolations(iri, errorHandler, line, col) ;
        return iri ;
    }

    @Override
    public Triple createTriple(Node subject, Node predicate, Node object, long line, long col) {
        checkTriple(subject, predicate, object, line, col) ;
        return super.createTriple(subject, predicate, object, line, col) ;
    }

    private void checkTriple(Node subject, Node predicate, Node object, long line, long col) {
        if ( subject == null || (!subject.isURI() && !subject.isBlank()) ) {
            errorHandler.error("Subject is not a URI or blank node", line, col) ;
            throw new RiotException("Bad subject: " + subject) ;
        }
        if ( predicate == null || (!predicate.isURI()) ) {
            errorHandler.error("Predicate not a URI", line, col) ;
            throw new RiotException("Bad predicate: " + predicate) ;
        }
        if ( object == null || (!object.isURI() && !object.isBlank() && !object.isLiteral()) ) {
            errorHandler.error("Object is not a URI, blank node or literal", line, col) ;
            throw new RiotException("Bad object: " + object) ;
        }
    }

    @Override
    public Quad createQuad(Node graph, Node subject, Node predicate, Node object, long line, long col) {
        checkQuad(graph, subject, predicate, object, line, col) ;
        return super.createQuad(graph, subject, predicate, object, line, col) ;
    }

    private void checkQuad(Node graph, Node subject, Node predicate, Node object, long line, long col) {
        // Allow blank nodes - syntax may restrict more.
        if ( graph != null && !graph.isURI() && !graph.isBlank() ) {
            errorHandler.error("Graph name is not a URI or blank node: " + FmtUtils.stringForNode(graph), line, col) ;
            throw new RiotException("Bad graph name: " + graph) ;
        }
        checkTriple(subject, predicate, object, line, col) ;
    }

    @Override
    public Node createURI(String x, long line, long col) {
        try {
            if ( RiotLib.isBNodeIRI(x) )
                return RiotLib.createIRIorBNode(x) ;
            else {
                String resolvedIRI = resolveIRI(x, line, col) ;
                return NodeFactory.createURI(resolvedIRI) ;
            }
        }
        catch (RiotException ex) {
            // Error was handled.
            // errorHandler.error(ex.getMessage(), line, col) ;
            throw ex ;
        }
    }

    @Override
    public Node createTypedLiteral(String lexical, RDFDatatype datatype, long line, long col) {
        Node n = NodeFactory.createLiteral(lexical, null, datatype) ;
        CheckerLiterals.checkLiteral(lexical, datatype, errorHandler, line, col) ;
        return n ;
    }

    @Override
    public Node createLangLiteral(String lexical, String langTag, long line, long col) {
        Node n = NodeFactory.createLiteral(lexical, langTag, null) ;
        CheckerLiterals.checkLiteral(lexical, langTag, errorHandler, line, col) ;
        return n ;
    }

    @Override
    public Node createStringLiteral(String lexical, long line, long col) {
        return NodeFactory.createLiteral(lexical) ;
    }

    @Override
    public Node createBlankNode(Node scope, String label, long line, long col) {
        return labelMapping.get(scope, label) ;
    }

}
