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

import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.jena.JenaRuntime;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIComponents;
import org.apache.jena.iri.Violation;
import org.apache.jena.irix.IRIProviderJenaIRI;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.SetupJenaIRI;
import org.apache.jena.irix.SystemIRIx;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.util.SplitIRI;

/**
 * Functions for checking nodes, triples and quads.
 * <p>
 * If the errorHandler is null, use the system wide handler.
 * <p>
 * If the errorHandler line/columns numbers are -1, -1, messages do not include them.
 * <p>
 * Operations "<tt>checkXXX(<i>item</i>)</tt>" are for boolean testing
 * and do not generate output.
 */

public class Checker {

    /** A node -- must be concrete node or a variable. */
    public static boolean check(Node node) {
        return check(node, nullErrorHandler, -1, -1);
    }

    /** A node -- must be a concrete node or a variable. */
    public static boolean check(Node node, ErrorHandler errorHandler, long line, long col) {
        if ( node.isURI() )
            return checkIRI(node, errorHandler, line, col);
        else if ( node.isBlank() )
            return checkBlankNode(node, errorHandler, line, col);
        else if ( node.isLiteral() )
            return checkLiteral(node, errorHandler, line, col);
        else if ( node.isVariable() )
            return checkVar(node, errorHandler, line, col);
        else if ( node.isNodeTriple() ) {
            Triple t = node.getTriple();
            return check(t.getSubject()) && check(t.getPredicate()) && check(t.getObject())
                    && checkTriple(t);
        }
        errorHandler(errorHandler).warning("Not a recognized node: ", line, col);
        return false;
    }

    // ==== IRIs

    public static boolean checkIRI(Node node) {
        return checkIRI(node, nullErrorHandler, -1, -1);
    }

    public static boolean checkIRI(Node node, ErrorHandler errorHandler, long line, long col) {
        if ( !node.isURI() ) {
            errorHandler(errorHandler).error("Not a URI: " + node, line, col);
            return false;
        }
        return checkIRI(node.getURI(), errorHandler, -1, -1);
    }

    public static boolean checkIRI(String iriStr) {
        return checkIRI(iriStr, nullErrorHandler, -1, -1);
    }

    /** See also {@link IRIs#reference} */
    public static boolean checkIRI(String iriStr, ErrorHandler errorHandler, long line, long col) {
        IRI iri = SetupJenaIRI.iriCheckerFactory().create(iriStr);
        boolean b = iriViolations(iri, errorHandler, line, col);
        return b;
    }

    /**
     * Process violations on an IRI Calls the {@link ErrorHandler} on all errors and
     * warnings (as warnings).
     */
    public static void iriViolations(IRI iri) {
        iriViolations(iri, null, false, true, -1L, -1L);
    }

    /**
     * Process violations on an IRI Calls the {@link ErrorHandler} on all errors and
     * warnings (as warnings).
     */
    public static boolean iriViolations(IRI iri, ErrorHandler errorHandler, long line, long col) {
        return iriViolations(iri, errorHandler, false, true, line, col);
    }

    /**
     * Process violations on an IRI Calls the errorHandler on all errors and warnings
     * (as warning). (If checking for relative IRIs, these are sent out as errors.)
     * Assumes error handler throws exceptions on errors if need be
     */
    public static boolean iriViolations(IRI iri, ErrorHandler errorHandler,
                                        boolean allowRelativeIRIs, boolean includeIRIwarnings,
                                        long line, long col) {

        if ( !allowRelativeIRIs && iri.isRelative() )
            // Relative IRIs.
            iriViolationMessage(iri.toString(), true, "Relative IRI: " + iri, line, col, errorHandler);

        boolean isOK = true;

        if ( iri.hasViolation(includeIRIwarnings) ) {
            Iterator<Violation> iter = iri.violations(includeIRIwarnings);

            for ( ; iter.hasNext() ; ) {
                Violation v = iter.next();
                int code = v.getViolationCode();
                boolean isError = v.isError();

                // --- Tune warnings.
                // IRIProviderJena filters ERRORs and throws an exception on error.
                // It can't add warnings or remove them at that point.
                // Do WARN filtering here.
                if ( code == Violation.LOWERCASE_PREFERRED && v.getComponent() != IRIComponents.SCHEME ) {
                    // Issue warning about the scheme part only. Not e.g. DNS names.
                    continue;
                }

                // Convert selected violations from ERROR to WARN for output/
                // There are cases where jena-iri always makes a violation an ERROR regardless of SetupJenaIRI
                // PROHIBITED_COMPONENT_PRESENT
//                if ( code == Violation.PROHIBITED_COMPONENT_PRESENT )
//                    isError = false;

                isOK = false;
                String msg = v.getShortMessage();
                String iriStr = iri.toString();
                iriViolationMessage(iriStr, isError, msg, line, col, errorHandler);
            }
        }
        return isOK;
    }

    /**
     * Common handling messages about IRIs during parsing whether a violation or an
     * IRIException. Prints a warning, with different messages for IRI error or warning.
     */
    public static void iriViolationMessage(String iriStr, boolean isError, String msg, long line, long col, ErrorHandler errorHandler) {
        try {
            if ( ! ( SystemIRIx.getProvider() instanceof IRIProviderJenaIRI ) )
                msg = "<" + iriStr + "> : " + msg;

            if ( isError ) {
                // ?? Treat as error, catch exceptions?
                errorHandler(errorHandler).warning("Bad IRI: " + msg, line, col);
            } else
                errorHandler(errorHandler).warning("Not advised IRI: " + msg, line, col);
        } catch (org.apache.jena.iri.IRIException | org.apache.jena.irix.IRIException ex) {}
    }

    // ==== Literals

    final static private Pattern langPattern = Pattern.compile("[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*");

    public static boolean checkLiteral(Node node) {
        return checkLiteral(node, nullErrorHandler, -1, -1);
    }

    public static boolean checkLiteral(Node node, ErrorHandler errorHandler, long line, long col) {
        if ( !node.isLiteral() ) {
            errorHandler(errorHandler).error("Not a literal: " + node, line, col);
            return false;
        }

        return checkLiteral(node.getLiteralLexicalForm(), node.getLiteralLanguage(), node.getLiteralDatatype(), errorHandler, line, col);
    }

    public static boolean checkLiteral(String lexicalForm, RDFDatatype datatype, ErrorHandler errorHandler, long line, long col) {
        return checkLiteral(lexicalForm, null, datatype, errorHandler, line, col);
    }

    public static boolean checkLiteral(String lexicalForm, String lang, ErrorHandler errorHandler, long line, long col) {
        return checkLiteral(lexicalForm, lang, null, errorHandler, line, col);
    }

    public static boolean checkLiteral(String lexicalForm, String lang, RDFDatatype datatype, ErrorHandler errorHandler, long line,
                                       long col) {
        boolean hasLang = lang != null && !lang.equals("");
        if ( !hasLang ) {
            // Datatype check (and RDF 1.0 simple literals are always well formed)
            if ( datatype != null )
                return validateByDatatype(lexicalForm, datatype, errorHandler, line, col);
            return true;
        }

        // Has a language.
        if ( JenaRuntime.isRDF11 ) {
            if ( datatype != null && !Objects.equals(datatype.getURI(), NodeConst.rdfLangString.getURI()) ) {
                errorHandler(errorHandler).error("Literal has language but wrong datatype", line, col);
                return false;
            }
        } else {
            if ( datatype != null ) {
                errorHandler(errorHandler).error("Literal has datatype and language", line, col);
                return false;
            }
        }

        // Test language tag format -- not a perfect test.
        if ( !lang.isEmpty() && !langPattern.matcher(lang).matches() ) {
            errorHandler(errorHandler).warning("Language not valid: " + lang, line, col);
            return false;
        }
        return true;
    }

    // Whitespace.
    // XSD allows whitespace before and after the lexical forms of a literal but not inside.
    // Jena handles this correctly.

    protected static boolean validateByDatatype(String lexicalForm, RDFDatatype datatype, ErrorHandler errorHandler, long line, long col) {
//        if ( SysRIOT.StrictXSDLexicialForms )
//            checkWhitespace(lexicalForm, datatype, errorHandler, line, col);
        return validateByDatatypeJena(lexicalForm, datatype, errorHandler, line, col);
    }

    protected static boolean validateByDatatypeJena(String lexicalForm, RDFDatatype datatype, ErrorHandler errorHandler, long line, long col) {
        if ( datatype.isValid(lexicalForm) )
            return true;
        errorHandler(errorHandler).warning("Lexical form '" + lexicalForm + "' not valid for datatype " + xsdDatatypeName(datatype), line, col);
        return false;
    }

    protected static boolean checkWhitespace(String lexicalForm, RDFDatatype datatype, ErrorHandler errorHandler, long line, long col) {
        if ( lexicalForm.contains(" ") ) {
            errorHandler(errorHandler).warning("Whitespace in " + xsdDatatypeName(datatype) + " literal: '" + lexicalForm + "'", line, col);
            return false;
        }
        if ( lexicalForm.contains("\n") ) {
            errorHandler(errorHandler).warning("Newline in " + xsdDatatypeName(datatype) + " literal: '" + lexicalForm + "'", line, col);
            return false;
        }
        if ( lexicalForm.contains("\r") ) {
            errorHandler(errorHandler).warning("Newline in " + xsdDatatypeName(datatype) + " literal: '" + lexicalForm + "'", line, col);
            return false;
        }
        return true;
    }

    private static String xsdDatatypeName(RDFDatatype datatype) {
        return "XSD " + SplitIRI.localname(datatype.getURI());
    }

    // ==== Blank nodes

    public static boolean checkBlankNode(Node node) {
        return checkBlankNode(node, nullErrorHandler, -1, -1);
    }

    public static boolean checkBlankNode(Node node, ErrorHandler errorHandler, long line, long col) {
        if ( !node.isBlank() ) {
            errorHandler(errorHandler).error("Not a blank node: " + node, line, col);
            return false;
        }
        return checkBlankNode(node.getBlankNodeLabel(), errorHandler, line, col);
    }

    public static boolean checkBlankNode(String label) {
        return checkBlankNode(label, null, -1, -1);
    }

    public static boolean checkBlankNode(String label, ErrorHandler errorHandler, long line, long col) {
        if ( label.indexOf(' ') >= 0 ) {
            errorHandler(errorHandler).error("Illegal blank node label (contains a space): " + label, line, col);
            return false;
        }
        return true;
    }

    // ==== Var

    public static boolean checkVar(Node node) {
        return checkVar(node, nullErrorHandler, -1, -1);
    }

    public static boolean checkVar(Node node, ErrorHandler errorHandler, long line, long col) {
        if ( node.isVariable() ) {
            errorHandler(errorHandler).error("Not a variable: " + node, line, col);
            return false;
        }
        return true;
    }

    // ==== Triples

    public static boolean checkTriple(Triple triple) {
        return checkTriple(triple, nullErrorHandler, -1, -1);
    }

    /** Check a triple - assumes individual nodes are legal */
    public static boolean checkTriple(Triple triple, ErrorHandler errorHandler, long line, long col) {
        return checkTriple(triple.getSubject(), triple.getPredicate(), triple.getObject(), errorHandler, line, col);
    }

    /**
     * Check a triple against the RDF rules for a triple : subject is a IRI or bnode,
     * predicate is a IRI and object is an bnode, literal or IRI
     */
    public static boolean checkTriple(Node subject, Node predicate, Node object, ErrorHandler errorHandler, long line, long col) {
        boolean rc = true;

        if ( subject == null || (!subject.isURI() && !subject.isBlank()) ) {
            errorHandler(errorHandler).error("Subject is not a URI or blank node", line, col);
            rc = false;
        }
        if ( predicate == null || (!predicate.isURI()) ) {
            errorHandler(errorHandler).error("Predicate not a URI", line, col);
            rc = false;
        }
        if ( object == null || (!object.isURI() && !object.isBlank() && !object.isLiteral()) ) {
            errorHandler(errorHandler).error("Object is not a URI, blank node or literal", line, col);
            rc = false;
        }
        return rc;
    }

    // ==== Quads

    public static boolean checkQuad(Quad quad) {
        return checkQuad(quad, nullErrorHandler, -1, -1);
    }

    /** Check a quad - assumes individual nodes are legal */
    public static boolean checkQuad(Quad quad, ErrorHandler errorHandler, long line, long col) {
        return checkQuad(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject(), errorHandler, line, col);
    }

    /**
     * Check a quad against the RDF rules for a quad : subject is a IRI or bnode,
     * predicate is a IRI and object is an bnode, literal or IRI
     */
    public static boolean checkQuad(Node graph, Node subject, Node predicate, Node object, ErrorHandler errorHandler, long line, long col) {
        boolean rc = true;

        if ( graph == null || (!graph.isURI() && !graph.isBlank()) ) {
            errorHandler(errorHandler).error("Graph name is not a URI or blank node", line, col);
            rc = false;
        }

        if ( subject == null || (!subject.isURI() && !subject.isBlank() && !subject.isNodeTriple()) ) {
            errorHandler(errorHandler).error("Subject is not a URI, blank node or RDF-star triple term", line, col);
            rc = false;
        }
        if ( predicate == null || (!predicate.isURI()) ) {
            errorHandler(errorHandler).error("Predicate not a URI", line, col);
            rc = false;
        }
        if ( object == null || (!object.isURI() && !object.isBlank() && !object.isLiteral() && !subject.isNodeTriple()) ) {
            errorHandler(errorHandler).error("Object is not a URI, blank node, literal or RDF-star triple term", line, col);
            rc = false;
        }
        return rc;
    }

    private static ErrorHandler errorHandler(ErrorHandler handler) {
        return handler != null ? handler : ErrorHandlerFactory.errorHandlerStd;
    }

    // Does nothing. Used in "check(node)" operations where the boolean result is key.
    private static ErrorHandler nullErrorHandler  = new ErrorHandler() {
        @Override
        public void warning(String message, long line, long col) {}

        @Override
        public void error(String message, long line, long col) {}

        @Override
        public void fatal(String message, long line, long col) {}
    };

}
