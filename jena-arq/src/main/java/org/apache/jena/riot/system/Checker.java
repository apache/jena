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


import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.checker.* ;
import org.apache.jena.sparql.core.Quad;

/** A checker that drives the process of validating RDF terms, triples and quads. */
public final class Checker
{
    private boolean allowRelativeIRIs = false ;
    private boolean warningsAreErrors = false ;
    private ErrorHandler handler ;

    private NodeChecker checkLiterals ;
    private NodeChecker checkURIs ;
    private NodeChecker checkBlankNodes ;
    private NodeChecker checkVars ;

    public Checker() {
        this(null);
    }

    public Checker(ErrorHandler handler) {
        if ( handler == null )
            handler = ErrorHandlerFactory.getDefaultErrorHandler();
        this.handler = handler;

        checkLiterals = new CheckerLiterals(handler);

        checkURIs = new CheckerIRI(handler);
        checkBlankNodes = new CheckerBlankNodes(handler);
        checkVars = new CheckerVar(handler);
    }

    public boolean check(Node node, long line, long col) {
        // NodeVisitor?
        if      ( node.isURI() )        return checkIRI(node, line, col) ;
        else if ( node.isBlank() )      return checkBlank(node, line, col) ;
        else if ( node.isLiteral() )    return checkLiteral(node, line, col) ;
        else if ( node.isVariable() )   return checkVar(node, line, col) ;
        handler.warning("Not a recognized node: ", line, col) ;
        return false ;
    }

    /** Check a triple - assumes individual nodes are legal */
    public boolean check(Triple triple, long line, long col) {
        return checkTriple(triple.getSubject(), triple.getPredicate(), triple.getObject(), line, col);
    }

    /** Check a triple against the RDF rules for a triple : subject is a IRI or bnode, predicate is a IRI and object is an bnode, literal or IRI */
    public boolean checkTriple(Node subject, Node predicate, Node object, long line, long col) {
        boolean rc = true;

        if ( subject == null || (!subject.isURI() && !subject.isBlank()) ) {
            handler.error("Subject is not a URI or blank node", line, col);
            rc = false;
        }
        if ( predicate == null || (!predicate.isURI()) ) {
            handler.error("Predicate not a URI", line, col);
            rc = false;
        }
        if ( object == null || (!object.isURI() && !object.isBlank() && !object.isLiteral()) ) {
            handler.error("Object is not a URI, blank node or literal", line, col);
            rc = false;
        }
        return rc;
    }

    /** Check a quad - assumes individual nodes are legal */
    public boolean checkQuad(Quad quad, long line, long col) {
        return checkQuad(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject(), line, col);
    }

    /** Check a quad against the RDF rules for a quad : subject is a IRI or bnode, predicate is a IRI and object is an bnode, literal or IRI */
    public boolean checkQuad(Node graph, Node subject, Node predicate, Node object, long line, long col) {
        boolean rc = true;

        if ( graph == null || (!graph.isURI() && !graph.isBlank()) ) {
            handler.error("Graph name is not a URI or blank node", line, col);
            rc = false;
        }

        if ( subject == null || (!subject.isURI() && !subject.isBlank() && !subject.isNodeTriple() ) ) {
            handler.error("Subject is not a URI, blank node or RDF-star triple term", line, col);
            rc = false;
        }
        if ( predicate == null || (!predicate.isURI()) ) {
            handler.error("Predicate not a URI", line, col);
            rc = false;
        }
        if ( object == null || (!object.isURI() && !object.isBlank() && !object.isLiteral() && !subject.isNodeTriple() ) ) {
            handler.error("Object is not a URI, blank node, literal or RDF-star triple term", line, col);
            rc = false;
        }
        return rc;
    }

    public boolean checkVar(Node node, long line, long col) {
        return checkVars.check(node, line, col);
    }

    public boolean checkLiteral(Node node, long line, long col) {
        return checkLiterals.check(node, line, col);
    }

    public boolean checkBlank(Node node, long line, long col) {
        return checkBlankNodes.check(node, line, col);
    }

    public boolean checkIRI(Node node, long line, long col) {
        return checkURIs.check(node, line, col);
    }

    public ErrorHandler getHandler() {
        return handler;
    }

    public Checker setHandler(ErrorHandler handler) {
        this.handler = handler;
        return this;
    }

    public NodeChecker getCheckLiterals() {
        return checkLiterals;
    }

    public Checker setCheckLiterals(NodeChecker checkLiterals) {
        this.checkLiterals = checkLiterals;
        return this;
    }

    public NodeChecker getCheckURIs() {
        return checkURIs;
    }

    public Checker setCheckURIs(NodeChecker checkURIs) {
        this.checkURIs = checkURIs;
        return this;
    }

    public NodeChecker getCheckBlankNodes() {
        return checkBlankNodes;
    }

    public Checker setCheckBlankNodes(NodeChecker checkBlankNodes) {
        this.checkBlankNodes = checkBlankNodes;
        return this;
    }

    public NodeChecker getCheckVars() {
        return checkVars;
    }

    public Checker setCheckVars(NodeChecker checkVars) {
        this.checkVars = checkVars;
        return this;
    }
}
