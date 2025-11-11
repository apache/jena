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
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.RDFDirLangString;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.TextDirection;
import org.apache.jena.graph.Triple;
import org.apache.jena.irix.*;
import org.apache.jena.langtagx.LangTagX;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.vocabulary.RDF;

/**
 * Functions for checking nodes, triples and quads.
 * <p>
 * The "check..." functions have two basic signatures:<br>
 * 1. {@code check...(<i>object</i>)}<br>
 * 2. {@code check...(<i>object, errorHandler, line, col</i>)}
 * <p>
 * The first type are for boolean testing and do not generate output. They call the
 * second type with default values for the last 3 parameters: nullErrorHandler, -1L, -1L.
 * The second type are for boolean testing and optionally generate error handling
 * output.
 * <ul>
 * <li>Argument {@code errorHandler} - the {@link ErrorHandler} for output. If the errorHandler
 * is null, use the system wide handler.
 * <li>Argument {@code line} - code line number (a long integer) generating the check.
 * <li>Argument {@code col} - code column number (a long integer) generating the check.
 * </ul>
 * If the errorHandler is null, the line and column numbers not used.
 */

public class Checker {

    /** A node -- must be concrete node or a variable. */
    public static boolean check(Node node) {
        return check(node, nullErrorHandler, -1L, -1L);
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
        else if ( node.isTripleTerm() ) {
            Triple t = node.getTriple();
            return check(t.getSubject()) && check(t.getPredicate()) && check(t.getObject())
                    && checkTriple(t);
        }
        errorHandler(errorHandler).warning("Not a recognized node: ", line, col);
        return false;
    }

    // ==== IRIs

    public static boolean checkIRI(Node node) {
        return checkIRI(node, nullErrorHandler, -1L, -1L);
    }

    public static boolean checkIRI(Node node, ErrorHandler errorHandler, long line, long col) {
        if ( !node.isURI() ) {
            errorHandler(errorHandler).error("Not a URI: " + node, line, col);
            return false;
        }
        return checkIRI(node.getURI(), errorHandler, line, col);
    }

    /** See also {@link IRIs#reference} */
    public static boolean checkIRI(String iriStr, ErrorHandler errorHandler, long line, long col) {
        try {
            IRIx iri = IRIs.reference(iriStr);
            if ( ! iri.hasViolations() )
                return true;
            // IRI errors are errorHandler warnings when checking.
            iri.handleViolations((isError, message)->{
                    errorHandler.warning(message, line, col);
            });
            return false;
        } catch (IRIException ex) {
            errorHandler.warning(ex.getMessage(), line, col);
            return false;
        }
    }

    /**
     * Common handling messages about IRIs during parsing whether a violation or an
     * IRIException. Prints a warning, with different messages for IRI error or warning.
     */
    public static void iriViolationMessage(String iriStr, boolean isError, String msg, long line, long col, ErrorHandler errorHandler) {
        try {
            // The IRI is valid RFC3986 syntax, else it failed earlier.
            // This code is for scheme violations which do not stop parsing.
            if ( isError ) {
                errorHandler(errorHandler).warning("Bad IRI: " + msg, line, col);
            } else
                errorHandler(errorHandler).warning("Unwise IRI: " + msg, line, col);
        } catch (org.apache.jena.irix.IRIException ex) {}
    }

    public static boolean checkLiteral(Node node) {
        return checkLiteral(node, nullErrorHandler, -1L, -1L);
    }

    public static boolean checkLiteral(Node node, ErrorHandler errorHandler, long line, long col) {
        if ( !node.isLiteral() ) {
            errorHandler(errorHandler).error("Not a literal: " + node, line, col);
            return false;
        }
        return checkLiteral(node.getLiteralLexicalForm(), node.getLiteralLanguage(), node.getLiteralBaseDirection(), node.getLiteralDatatype(), errorHandler, line, col);
    }

    public static boolean checkLiteral(String lexicalForm, RDFDatatype datatype, ErrorHandler errorHandler, long line, long col) {
        return checkLiteral(lexicalForm, null, (TextDirection)null, datatype, errorHandler, line, col);
    }

    public static boolean checkLiteral(String lexicalForm, String lang, ErrorHandler errorHandler, long line, long col) {
        return checkLiteral(lexicalForm, lang, (TextDirection)null, null, errorHandler, line, col);
    }

    public static boolean checkLiteral(String lexicalForm, String lang, String direction, RDFDatatype datatype, ErrorHandler errorHandler, long line, long col) {
        TextDirection textDir = null;
        if ( direction != null ) {
            textDir = TextDirection.createOrNull(direction);
            if ( textDir == null )
                errorHandler(errorHandler).error("Language direction not valid: " + direction, line, col);
        }
        return checkLiteral(lexicalForm, lang, textDir, datatype, errorHandler, line, col);
    }

    public static boolean checkLiteral(String lexicalForm, String lang, TextDirection direction, RDFDatatype datatype, ErrorHandler errorHandler, long line, long col) {
        boolean hasLang = ( lang != null && !lang.isEmpty() );
        boolean hasTextDirection = direction != null;
        boolean hasDatatype = datatype != null;

        if ( !hasDatatype && !hasLang) {
            // This will become an xsd:string
            // No further checking needed.
            return true;
        }

        // If the Literal has a language...
        if ( hasLang ) {
            // Test language tag format
            if ( !LangTagX.checkLanguageTag(lang) ) {
                errorHandler(errorHandler).warning("Language not valid: " + lang, line, col);
                return false;
            }

            // No datatype is acceptable - NodeFactory deal with that case.
            if ( hasDatatype ) {
                // Jena is using the RDF 1.1 or later standard...
                if ( hasTextDirection && ! datatype.equals( RDFDirLangString.rdfDirLangString ) ) {
                    errorHandler(errorHandler).error("Literal has language and base direction but wrong datatype: "+datatype, line, col);
                    return false;
                }
                else if ( ! datatype.equals( RDFLangString.rdfLangString ) ) {
                    errorHandler(errorHandler).error("Literal has language but wrong datatype: "+datatype.getURI(), line, col);
                    return false;
                }
            }
            return true;
        }
        // No lang => no base direction
        if ( hasTextDirection )
            errorHandler(errorHandler).error("Language base direction without language", line, col);

        if ( datatype.equals(XSDDatatype.XSDstring) )
            // Simple literals are always well-formed...
            return true;

        // If the Literal has a datatype (but no language or base direction)...
        if ( datatype.equals(RDF.dtLangString) ) {
            errorHandler(errorHandler).warning("Literal has datatype "+datatype.getURI()+" but no language tag", line, col);
            return false;
        }
        if ( datatype.equals(RDF.dtDirLangString) ) {
            errorHandler(errorHandler).warning("Literal has datatype "+datatype.getURI()+" but no language tag and no base direction", line, col);
            return false;
        }
        return validateByDatatype(lexicalForm, datatype, errorHandler, line, col);
    }

    // NOTE: Whitespace
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
        return checkBlankNode(node, nullErrorHandler, -1L, -1L);
    }

    public static boolean checkBlankNode(Node node, ErrorHandler errorHandler, long line, long col) {
        if ( !node.isBlank() ) {
            errorHandler(errorHandler).error("Not a blank node: " + node, line, col);
            return false;
        }
        return checkBlankNode(node.getBlankNodeLabel(), errorHandler, line, col);
    }

    public static boolean checkBlankNode(String label) {
        return checkBlankNode(label, null, -1L, -1L);
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
        return checkVar(node, nullErrorHandler, -1L, -1L);
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
        return checkTriple(triple, nullErrorHandler, -1L, -1L);
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
        return checkQuad(quad, nullErrorHandler, -1L, -1L);
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

        if ( subject == null || (!subject.isURI() && !subject.isBlank() && !subject.isTripleTerm()) ) {
            errorHandler(errorHandler).error("Subject is not a URI, blank node or RDF-star triple term", line, col);
            rc = false;
        }
        if ( predicate == null || (!predicate.isURI()) ) {
            errorHandler(errorHandler).error("Predicate not a URI", line, col);
            rc = false;
        }
        if ( object == null || (!object.isURI() && !object.isBlank() && !object.isLiteral() && !subject.isTripleTerm()) ) {
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
