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

package org.apache.jena.sparql.util;

import static org.apache.jena.sparql.util.NodeUtils.*;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Node compare operations - these compare operations are not value sensitive.
 * See {@link NodeValue#compare} and {@link NodeValue#compareAlways}.
 */
public class NodeCmp {

    /** Compare two Nodes, based on their RDF terms forms, not value */
    public static int compareRDFTerms(Node node1, Node node2) {
        if ( node1 == null ) {
            if ( node2 == null )
                return Expr.CMP_EQUAL;
            return Expr.CMP_LESS;
        }

        if ( node2 == null )
            return Expr.CMP_GREATER;

        // No nulls.
        if ( node1.isLiteral() && node2.isLiteral() ) {
            if ( "old".equalsIgnoreCase(System.getProperty("org.apache.jena.compare")) )
                return compareLiteralsBySyntaxOLD(node1, node2);
            else
                return compareLiteralsBySyntax(node1, node2);
        }

        // One or both not literals
        // Variables < Blank nodes < URIs < Literals < Triple Terms

        //-- Variables
        if ( node1.isVariable() ) {
            if ( node2.isVariable() ) {
                return StrUtils.strCompare(node1.getName(), node2.getName());
            }
            // Variables before anything else
            return Expr.CMP_LESS;
        }

        if ( node2.isVariable() ) {
            // node1 not variable
            return Expr.CMP_GREATER;
        }

        //-- Blank nodes
        if ( node1.isBlank() ) {
            if ( node2.isBlank() ) {
                String s1 = node1.getBlankNodeId().getLabelString();
                String s2 = node2.getBlankNodeId().getLabelString();
                return StrUtils.strCompare(s1, s2);
            }
            // bNodes before anything but variables
            return Expr.CMP_LESS;
        }

        if ( node2.isBlank() )
            // node1 not blank.
            return Expr.CMP_GREATER;

        // Not blanks. 2 URI or one URI and one literal

        //-- URIs
        if ( node1.isURI() ) {
            if ( node2.isURI() ) {
                String s1 = node1.getURI();
                String s2 = node2.getURI();
                return StrUtils.strCompare(s1, s2);
            }
            return Expr.CMP_LESS;
        }

        if ( node2.isURI() )
            return Expr.CMP_GREATER;

        // -- Two literals already done just leaving ...
        if ( node2.isLiteral() )
            return Expr.CMP_GREATER;

        // Because triple terms are after literals ...
        if ( node1.isLiteral() )
            return Expr.CMP_LESS;

        // -- Triple nodes.
        if ( node1.isNodeTriple() ) {
            if ( node2.isNodeTriple() ) {
                Triple t1 = node1.getTriple();
                Triple t2 = node2.getTriple();
                int x1 = compareRDFTerms(t1.getSubject(), t2.getSubject());
                if ( x1 != Expr.CMP_EQUAL )
                    return x1;
                int x2 = compareRDFTerms(t1.getPredicate(), t2.getPredicate());
                if ( x2 != Expr.CMP_EQUAL )
                    return x2;
                int x3 = compareRDFTerms(t1.getObject(), t2.getObject());
                if ( x3 != Expr.CMP_EQUAL )
                    return x3;
                return Expr.CMP_EQUAL;
            }
        }

        if ( node2.isNodeTriple() )
            return Expr.CMP_GREATER;

        // No URIs, no blanks, no literals, no triple terms nodes by this point

        // Should not happen.
        throw new ARQInternalErrorException("Compare: " + node1 + "  " + node2);
    }

    /** Compare literals by kind - not by value.
     *  Gives a deterministic, stable, arbitrary ordering between unrelated literals.
     *
     * Ordering:
     *  <ol>
     *  <li>By lexical form</li>
     *  <li> For same lexical form:
     *       <ul>
     *       <li>  RDF 1.0 : simple literal < literal by lang < literal with type
     *       <li>  RDF 1.1 : xsd:string < rdf:langString < other dataypes.<br/>
     *             This is the closest to SPARQL 1.1: treat xsd:string as a simple literal</ul></li>
     *  <li> Lang by sorting on language tag (first case insensitive then case sensitive)
     *  <li> Datatypes by URI
     *  </ol>
     */

    private static int compareLiteralsBySyntax(Node node1, Node node2) {
        if ( node1 == null || !node1.isLiteral() || node2 == null || !node2.isLiteral() )
            throw new ARQInternalErrorException("compareLiteralsBySyntax called with non-literal: (" + node1 + "," + node2 + ")");

        if ( node1.equals(node2) )
            return Expr.CMP_EQUAL;

        // ** Rewrite as a classifier.??
        // simple < lang < datatype

        // Both simple? (xsd:string in RDF 1.1)
        if ( isSimpleString(node1) && isSimpleString(node2) ) {
            String lex1 = node1.getLiteralLexicalForm();
            String lex2 = node2.getLiteralLexicalForm();
            return StrUtils.strCompare(lex1, lex2);
        }

        // Any simple string?
        if ( isSimpleString(node1) )
            return Expr.CMP_LESS;
        if ( isSimpleString(node2) )
            return Expr.CMP_GREATER;

        // No simple string from here on.

        {
            int x = compareRDFLangTerms(node1, node2);
            if ( x != Expr.CMP_INDETERMINATE )
                return x;
        }

        // No simple strings, no lang strings.
        // Compare by datatype then lexical form.

        // Switch for old world (Jena 4.5.0 and before)
        // Affects ordering for datatypes (not simple or lang strings).
        // false - old world.
        final boolean byDatatype = false;
        if ( byDatatype ) {
            String dt1 = node1.getLiteralDatatypeURI();
            String dt2 = node2.getLiteralDatatypeURI();

            int x = StrUtils.strCompare(dt1, dt2);
            if ( x != Expr.CMP_EQUAL )
                return x;
            // Same datatype
            String lex1 = node1.getLiteralLexicalForm();
            String lex2 = node2.getLiteralLexicalForm();
            return StrUtils.strCompare(lex1, lex2);
        }

        // Old Jena 4.5.0 : lexical form before datatype.
        String lex1 = node1.getLiteralLexicalForm();
        String lex2 = node2.getLiteralLexicalForm();

        int x = StrUtils.strCompare(lex1, lex2);
        if ( x != Expr.CMP_EQUAL )
            return x;

        // Same by lexical form. Split on datatype.
        String dt1 = node1.getLiteralDatatypeURI();
        String dt2 = node2.getLiteralDatatypeURI();
        // Two datatypes.
        return StrUtils.strCompare(dt1, dt2);
    }

    /** May return {@link Expr#CMP_INDETERMINATE} */
    private static int compareRDFLangTerms(Node node1, Node node2) {
        if ( isLangString(node1) && isLangString(node2) ) {
            String lang1 = node1.getLiteralLanguage();
            String lang2 = node2.getLiteralLanguage();
            int x = StrUtils.strCompareIgnoreCase(lang1, lang2);
            if ( x != Expr.CMP_EQUAL )
                return x;
            // Two langs, same lang tag => lexical form.
            String lex1 = node1.getLiteralLexicalForm();
            String lex2 = node2.getLiteralLexicalForm();
            x = StrUtils.strCompare(lex1, lex2);
            if ( x != Expr.CMP_EQUAL )
                return x;

            // Same (case insensitive) lang, same lexical for - can be split them?
            x = StrUtils.strCompare(lang1, lang2);
            if ( x != Expr.CMP_EQUAL )
                return x;
            throw new ARQInternalErrorException("Same lang tag (inc case, same lexical form but not node.equals");
        }

        // Any lang string?
        if ( isLangString(node1) )
            // lang < datatype
            return Expr.CMP_LESS;

        if ( isLangString(node2) )
            return Expr.CMP_GREATER;

        return Expr.CMP_INDETERMINATE;
    }

//  **** Retained for reference only.
    /**
     * Version of Jena 4.5.0 and earlier.
     * Unstable. Unlike NodeValue.compare, it uses lexical form before lang/datatype.
     */
    public static int compareRDFTermsOLD(Node node1, Node node2) {
        if ( node1 == null ) {
            if ( node2 == null )
                return Expr.CMP_EQUAL;
            return Expr.CMP_LESS;
        }

        if ( node2 == null )
            return Expr.CMP_GREATER;

        // No nulls.
        if ( node1.isLiteral() && node2.isLiteral() )
            return compareLiteralsBySyntaxOLD(node1, node2);

        // One or both not literals
        // Variables < Blank nodes < URIs < Literals < Triple Terms

        //-- Variables
        if ( node1.isVariable() ) {
            if ( node2.isVariable() ) {
                return StrUtils.strCompare(node1.getName(), node2.getName());
            }
            // Variables before anything else
            return Expr.CMP_LESS;
        }

        if ( node2.isVariable() ) {
            // node1 not variable
            return Expr.CMP_GREATER;
        }

        //-- Blank nodes
        if ( node1.isBlank() ) {
            if ( node2.isBlank() ) {
                String s1 = node1.getBlankNodeId().getLabelString();
                String s2 = node2.getBlankNodeId().getLabelString();
                return StrUtils.strCompare(s1, s2);
            }
            // bNodes before anything but variables
            return Expr.CMP_LESS;
        }

        if ( node2.isBlank() )
            // node1 not blank.
            return Expr.CMP_GREATER;

        // Not blanks. 2 URI or one URI and one literal

        //-- URIs
        if ( node1.isURI() ) {
            if ( node2.isURI() ) {
                String s1 = node1.getURI();
                String s2 = node2.getURI();
                return StrUtils.strCompare(s1, s2);
            }
            return Expr.CMP_LESS;
        }

        if ( node2.isURI() )
            return Expr.CMP_GREATER;

        // -- Two literals already done just leaving ...
        if ( node2.isLiteral() )
            return Expr.CMP_GREATER;

        // Because triple terms are after literals ...
        if ( node1.isLiteral() )
            return Expr.CMP_LESS;

        // -- Triple nodes.
        if ( node1.isNodeTriple() ) {
            if ( node2.isNodeTriple() ) {
                Triple t1 = node1.getTriple();
                Triple t2 = node2.getTriple();
                int x1 = compareRDFTerms(t1.getSubject(), t2.getSubject());
                if ( x1 != Expr.CMP_EQUAL )
                    return x1;
                int x2 = compareRDFTerms(t1.getPredicate(), t2.getPredicate());
                if ( x2 != Expr.CMP_EQUAL )
                    return x2;
                int x3 = compareRDFTerms(t1.getObject(), t2.getObject());
                if ( x3 != Expr.CMP_EQUAL )
                    return x3;
                return Expr.CMP_EQUAL;
            }
        }

        if ( node2.isNodeTriple() )
            return Expr.CMP_GREATER;

        // No URIs, no blanks, no literals, no triple terms nodes by this point

        // Should not happen.
        throw new ARQInternalErrorException("Compare: " + node1 + "  " + node2);
    }

    /** Compare literals by kind - not by value.
     *  Gives a deterministic, stable, arbitrary ordering between unrelated literals.
     *
     * Ordering:
     *  <ol>
     *  <li>By lexical form</li>
     *  <li> For same lexical form:
     *       <ul>
     *       <li>  RDF 1.0 : simple literal < literal by lang < literal with type
     *       <li>  RDF 1.1 : xsd:string < rdf:langString < other dataypes.<br/>
     *             This is the closest to SPARQL 1.1: treat xsd:string as a simple literal</ul></li>
     *  <li> Lang by sorting on language tag (first case insensitive then case sensitive)
     *  <li> Datatypes by URI
     *  </ol>
     */

    private static int compareLiteralsBySyntaxOLD(Node node1, Node node2) {
        if ( node1 == null || !node1.isLiteral() || node2 == null || !node2.isLiteral() )
            throw new ARQInternalErrorException("compareLiteralsBySyntax called with non-literal: (" + node1 + "," + node2 + ")");

        if ( node1.equals(node2) )
            return Expr.CMP_EQUAL;

        String lex1 = node1.getLiteralLexicalForm();
        String lex2 = node2.getLiteralLexicalForm();

        int x = StrUtils.strCompare(lex1, lex2);
        if ( x != Expr.CMP_EQUAL )
            return x;

        // Same lexical form. Not .equals()
        if ( isSimpleString(node1) ) // node2 not a simple string because they
                                     // would be .equals
            return Expr.CMP_LESS;
        if ( isSimpleString(node2) )
            return Expr.CMP_GREATER;
        // Neither simple string / xsd:string(RDF 1.1)

        // Both language strings?
        if ( isLangString(node1) && isLangString(node2) ) {
            String lang1 = node1.getLiteralLanguage();
            String lang2 = node2.getLiteralLanguage();
            x = StrUtils.strCompareIgnoreCase(lang1, lang2);
            if ( x != Expr.CMP_EQUAL )
                return x;
            x = StrUtils.strCompare(lang1, lang2);
            if ( x != Expr.CMP_EQUAL )
                return x;
            throw new ARQInternalErrorException("compareLiteralsBySyntax: lexical form and languages tags identical on non.equals literals");
        }

        // One a language string?
        if ( isLangString(node1) )
            return Expr.CMP_LESS;
        if ( isLangString(node2) )
            return Expr.CMP_GREATER;

        // Both have other datatypes. Neither simple nor language tagged.
        String dt1 = node1.getLiteralDatatypeURI();
        String dt2 = node2.getLiteralDatatypeURI();
        // Two datatypes.
        return StrUtils.strCompare(dt1, dt2);
    }


}
