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

package org.apache.jena.sparql.expr.nodevalue ;

import java.util.Iterator ;
import java.util.UUID ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.Violation ;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.ExprTypeException ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.graph.NodeConst ;
import org.apache.jena.sparql.util.FmtUtils ;
import org.apache.jena.sparql.util.NodeUtils ;
import org.apache.jena.vocabulary.XSD ;

/**
 * Implementation of node-centric functions.
 */
public class NodeFunctions {
    private static final NodeValue xsdString = NodeValue.makeNode(XSD.xstring.asNode()) ;

    // Helper functions
    /**
     * check and get a string (may be a simple literal, literal with language
     * tag or an XSD string).
     */
    public static Node checkAndGetStringLiteral(String label, NodeValue nv) {
        Node n = nv.asNode() ;
        if ( !n.isLiteral() )
            throw new ExprEvalException(label + ": Not a literal: " + nv) ;
        String lang = n.getLiteralLanguage() ;
        
        if ( NodeUtils.isLangString(n) )
            // Language tag.  Legal.  
            return n ;
        
        // No language tag : either no datatype or a datatype of xsd:string 
        // Includes the case of rdf:langString and no language ==> Illegal as a compatible string.

        if ( nv.isString() )
                return n ;
        throw new ExprEvalException(label + ": Not a string literal: " + nv) ;
    }

    /**
     * Check for string operations with primary first arg and second arg
     * (e.g. CONTAINS).  The arguments are not used in the same way and the check
     * operation is not symmetric. 
     * <li> "abc"@en is compatible with "abc"
     * <li> "abc" is NOT compatible with "abc"@en
     */
    public static void checkTwoArgumentStringLiterals(String label, NodeValue arg1, NodeValue arg2) {
        
        /* Quote the spec:
         * Compatibility of two arguments is defined as:
         *    The arguments are simple literals or literals typed as xsd:string
         *    The arguments are plain literals with identical language tags
         *    The first argument is a plain literal with language tag and the second argument is a simple literal or literal typed as xsd:string
         */
        
        Node n1 = checkAndGetStringLiteral(label, arg1) ;
        Node n2 = checkAndGetStringLiteral(label, arg2) ;
        String lang1 = n1.getLiteralLanguage() ;
        String lang2 = n2.getLiteralLanguage() ;
        if ( lang1 == null )
            lang1 = "" ;
        if ( lang2 == null )
            lang2 = "" ;

        // Case 1
        if ( lang1.equals("") ) { 
            if ( lang2.equals("") )
                return ;
            throw new ExprEvalException(label + ": Incompatible: " + arg1 + " and " + arg2) ;
        }

        // Case 2
        if ( lang1.equalsIgnoreCase(lang2) )
            return ;
        
        // Case 3
        if ( lang2.equals("") )
            return ;

        throw new ExprEvalException(label + ": Incompatible: " + arg1 + " and " + arg2) ;
        
        // ----------
                
//                if ( lang1.equals("") && !lang2.equals("") )
//            throw new ExprEvalException(label + ": Incompatible: " + arg1 + " and " + arg2) ;
//        
//        
//        
//        if ( n1.getLiteralDatatype() != null ) {
//            // n1 is an xsd string by checkAndGetString
//            if ( XSDDatatype.XSDstring.equals(n2.getLiteralDatatypeURI()) )
//                return ;
//            if ( n2.getLiteralLanguage().equals("") )
//                return ;
//            throw new ExprEvalException(label + ": Incompatible: " + arg1 + " and " + arg2) ;
//        }
//
//        // Incompatible?
//        // arg1 simple or xsd:string, arg2 has a lang.
//        // arg1 with lang, arg2 has a different lang.
//        if ( !lang1.equals("") && (!lang2.equals("") && !lang1.equals(lang2)) )
//            throw new ExprEvalException(label + ": Incompatible: " + arg1 + " and " + arg2) ;
    }

    // -------- sameTerm

    public static NodeValue sameTerm(NodeValue nv1, NodeValue nv2) {
        return NodeValue.booleanReturn(sameTerm(nv1.asNode(), nv2.asNode())) ;
    }

    public static boolean sameTerm(Node n1, Node n2) {
        if ( n1.equals(n2) )
            return true ;
        if ( n1.isLiteral() && n2.isLiteral() ) {
            // But language tags are case insensitive.
            String lang1 = n1.getLiteralLanguage() ;
            String lang2 = n2.getLiteralLanguage() ;

            if ( !lang1.equals("") && lang1.equalsIgnoreCase(lang2) ) {
                // Two language tags, equal by case insensitivity.
                boolean b = n1.getLiteralLexicalForm().equals(n2.getLiteralLexicalForm()) ;
                if ( b )
                    return true ;
            }
        }
        return false ;
    }

    // -------- RDFterm-equals

    public static NodeValue rdfTermEquals(NodeValue nv1, NodeValue nv2) {
        return NodeValue.booleanReturn(rdfTermEquals(nv1.asNode(), nv2.asNode())) ;
    }

    // Exact as defined by SPARQL spec.
    public static boolean rdfTermEquals(Node n1, Node n2) {
        if ( n1.equals(n2) )
            return true ;

        if ( n1.isLiteral() && n2.isLiteral() ) {
            // Two literals, may be sameTerm by language tag case insensitivity.
            String lang1 = n1.getLiteralLanguage() ;
            String lang2 = n2.getLiteralLanguage() ;

            if ( !lang1.equals("") && lang1.equalsIgnoreCase(lang2) ) {
                // Two language tags, equal by case insensitivity.
                boolean b = n1.getLiteralLexicalForm().equals(n2.getLiteralLexicalForm()) ;
                if ( b )
                    return true ;
            }
            // Two literals, different terms, different language tags.
            NodeValue.raise(new ExprEvalException("Mismatch in RDFterm-equals: " + n1 + ", " + n2)) ;
        }
        // One or both not a literal.
        return false ;
    }

    // -------- str
    public static NodeValue str(NodeValue nv) {
        return NodeValue.makeString(str(nv.asNode())) ;
    }

    public static String str(Node node) {
        if ( node.isLiteral() )
            return node.getLiteral().getLexicalForm() ;
        if ( node.isURI() )
            return node.getURI() ;
        // if ( node.isBlank() ) return node.getBlankNodeId().getLabelString() ;
        // if ( node.isBlank() ) return "" ;
        if ( node.isBlank() )
            NodeValue.raise(new ExprTypeException("Blank node: " + node)) ;

        NodeValue.raise(new ExprEvalException("Not a string: " + node)) ;
        return "[undef]" ;
    }

    // -------- sort key (collation)

    public static NodeValue sortKey(NodeValue nv, String collation) {
        return NodeValue.makeSortKey(str(nv.asNode()), collation) ;
    }

    // -------- datatype
    public static NodeValue datatype(NodeValue nv) {
        return NodeValue.makeNode(datatype(nv.asNode())) ;
    }

    public static Node datatype(Node node) {
        if ( !node.isLiteral() ) {
            NodeValue.raise(new ExprTypeException("datatype: Not a literal: " + node)) ;
            return null ;
        }

        String s = node.getLiteralDatatypeURI() ;
        boolean plainLiteral = (s == null || s.equals("")) ;

        if ( plainLiteral ) {
            boolean simpleLiteral = (node.getLiteralLanguage() == null || node.getLiteralLanguage().equals("")) ;
            if ( !simpleLiteral )
                return NodeConst.rdfLangString ;
            return XSD.xstring.asNode() ;
        }
        return NodeFactory.createURI(s) ;
    }

    // -------- lang

    public static NodeValue lang(NodeValue nv) {
        if ( nv.isLangString() )
            return NodeValue.makeString(nv.getLang()) ;
        if ( nv.isLiteral() )
            return NodeValue.nvEmptyString ;
        NodeValue.raise(new ExprTypeException("lang: Not a literal: " + nv.asQuotedString())) ;
        return null ;
    }

    public static String lang(Node node) {
        if ( !node.isLiteral() )
            NodeValue.raise(new ExprTypeException("lang: Not a literal: " + FmtUtils.stringForNode(node))) ;

        String s = node.getLiteralLanguage() ;
        if ( s == null )
            s = "" ;
        return s ;
    }

    // -------- langMatches
    /** LANGMATCHES
     *  
     * @param nv The language string
     * @param nvPattern The pattern to match against 
     * @return Boolean nodeValue
     */
    public static NodeValue langMatches(NodeValue nv, NodeValue nvPattern) {
        return langMatches(nv, nvPattern.getString()) ;
    }

    /** LANGMATCHES
     *  
     * @param nv The language string
     * @param langPattern The pattern to match against 
     * @return Boolean nodeValue
     */
    public static NodeValue langMatches(NodeValue nv, String langPattern) {
        Node node = nv.asNode() ;
        if ( !node.isLiteral() ) {
            NodeValue.raise(new ExprTypeException("langMatches: not a literal: " + node)) ;
            return null ;
        }

        String langStr = node.getLiteralLexicalForm() ;
        return NodeValue.booleanReturn(langMatches(langStr, langPattern));
    }
    
    /** The algortihm for the SPARQ function "LANGMATCHES".
     *  
     * @param langStr The language string
     * @param langPattern The pattern to match against 
     * @return Whether there is a match. 
     */
    public static boolean langMatches(String langStr, String langPattern) {
        if ( langPattern.equals("*") ) {
            // Not a legal lang string.
            if ( langStr == null || langStr.equals("") )
                return false ;
            return true ;
        }

        // See RFC 3066 (it's "tag (-tag)*)"

        String[] langElts = langStr.split("-") ;
        String[] langRangeElts = langPattern.split("-") ;

        /*
         * Here is the logic to compare language code. There is a match if the
         * language matches the parts of the pattern - the language may be
         * longer than the pattern.
         */

        /*
         * RFC 4647 basic filtering.
         * 
         * Notes for extended:
         *  1. Remove any -*- (but not *-)
         *  2. Compare primary tags.
         *  3. Is the remaining range a subsequence of the remaining language tag?
         */
        if ( langRangeElts.length > langElts.length )
            // Lang tag longer than pattern tag => can't match
            return false ;
        for ( int i = 0 ; i < langRangeElts.length ; i++ ) {
            String range = langRangeElts[i] ;
            if ( range == null )
                break ;
            // Language longer than range
            if ( i >= langElts.length )
                break ;
            String lang = langElts[i] ;
            if ( range.equals("*") )
                continue ;
            if ( !range.equalsIgnoreCase(lang) )
                return false;
        }
        return true ;
    }

    // -------- isURI/isIRI

    public static NodeValue isIRI(NodeValue nv) {
        return NodeValue.booleanReturn(isIRI(nv.asNode())) ;
    }

    public static boolean isIRI(Node node) {
        if ( node.isURI() )
            return true ;
        return false ;
    }

    public static NodeValue isURI(NodeValue nv) {
        return NodeValue.booleanReturn(isIRI(nv.asNode())) ;
    }

    public static boolean isURI(Node node) {
        return isIRI(node) ;
    }

    // -------- isBlank
    public static NodeValue isBlank(NodeValue nv) {
        return NodeValue.booleanReturn(isBlank(nv.asNode())) ;
    }

    public static boolean isBlank(Node node) {
        return node.isBlank() ;
    }

    // -------- isLiteral
    public static NodeValue isLiteral(NodeValue nv) {
        return NodeValue.booleanReturn(isLiteral(nv.asNode())) ;
    }

    public static boolean isLiteral(Node node) {
        return node.isLiteral() ;
    }

    private static final IRIFactory iriFactory      = IRIResolver.iriFactory();
    public static boolean           warningsForIRIs = false ;

    // -------- IRI
    /** "Skolemize": BlankNode to IRI else return node unchanged. */ 
    public static Node blankNodeToIri(Node node) {
        if ( node.isBlank() ) {
            String x = node.getBlankNodeLabel() ;
            return NodeFactory.createURI("_:" + x) ;
        }
        return node;
    }

    /** NodeValue to NodeValue, skolemizing, and converting strings to URIs. */
    public static NodeValue iri(NodeValue nv, String baseIRI) {
        if ( isIRI(nv.asNode()) )
            return nv ;
        Node n2 = iri(nv.asNode(), baseIRI) ;
        return NodeValue.makeNode(n2) ;
    }
    
    /** Node to Node, skolemizing, and converting strings to URIs. */
    public static Node iri(Node n, String baseIRI) {
        Node node = blankNodeToIri(n);
        if ( node.isURI() )
            return node ;
        // Literals.
        // Simple literal or xsd:string
        String str = simpleLiteralOrXSDString(node) ;
        if ( str == null )
            throw new ExprEvalException("Can't make an IRI from " + node) ;

        IRI iri = null ;
        String iriStr = node.getLiteralLexicalForm() ;

        // Level of checking?
        if ( baseIRI != null ) {
            IRI base = iriFactory.create(baseIRI) ;
            iri = base.create(iriStr) ;
        } else
            iri = iriFactory.create(iriStr) ;

        if ( !iri.isAbsolute() )
            throw new ExprEvalException("Relative IRI string: " + iriStr) ;
        if ( warningsForIRIs && iri.hasViolation(false) ) {
            String msg = "unknown violation from IRI library" ;
            Iterator<Violation> iter = iri.violations(false) ;
            if ( iter.hasNext() ) {
                Violation viol = iter.next() ;
                msg = viol.getShortMessage() ;
            }
            Log.warn(NodeFunctions.class, "Bad IRI: " + msg + ": " + iri) ;
        }
        return NodeFactory.createURI(iri.toString()) ;
    }

    // The Jena version can be slow to inityailise (but is pure java)

    // private static UUIDFactory factory = new UUID_V4_Gen() ;
    // private static UUIDFactory factory = new UUID_V1_Gen() ;
    // public static NodeValue uuid()
    // {
    // JenaUUID uuid = factory.generate() ;
    // Node n = Node.createURI(uuid.asURN()) ;
    // return NodeValue.makeNode(n) ;
    // }
    //
    // public static NodeValue struuid()
    // {
    // JenaUUID uuid = factory.generate() ;
    // return NodeValue.makeString(uuid.asString()) ;
    // }

    public static NodeValue struuid() {
        return NodeValue.makeString(uuidString()) ;
    }

    public static NodeValue uuid() {
        String str = "urn:uuid:" + uuidString() ;
        Node n = NodeFactory.createURI(str) ;
        return NodeValue.makeNode(n) ;
    }

    private static String uuidString() {
        return UUID.randomUUID().toString() ;
    }

    private static String simpleLiteralOrXSDString(Node n) {
        if ( !n.isLiteral() )
            return null ;

        if ( n.getLiteralDatatype() == null ) {
            if ( n.getLiteralLanguage().equals("") )
                return n.getLiteralLexicalForm() ;
        } else if ( n.getLiteralDatatype().equals(XSDDatatype.XSDstring) )
            return n.getLiteralLexicalForm() ;
        return null ;
    }

    public static NodeValue strDatatype(NodeValue v1, NodeValue v2) {
        if ( !v1.isString() )
            throw new ExprEvalException("Not a string (arg 1): " + v1) ;
        if ( !v2.isIRI() )
            throw new ExprEvalException("Not an IRI (arg 2): " + v2) ;

        String lex = v1.asString() ;
        Node dt = v2.asNode() ;
        // Check?

        Node n = NodeFactory.createLiteral(lex, NodeFactory.getType(dt.getURI())) ;
        return NodeValue.makeNode(n) ;
    }

    public static NodeValue strLang(NodeValue v1, NodeValue v2) {
        if ( !v1.isString() )
            throw new ExprEvalException("Not a string (arg 1): " + v1) ;
        if ( !v2.isString() )
            throw new ExprEvalException("Not a string (arg 2): " + v2) ;

        String lex = v1.asString() ;
        String lang = v2.asString() ;
        if ( lang.isEmpty() )
            throw new ExprEvalException("Empty lang tag") ;
        return NodeValue.makeLangString(lex, lang) ;
    }

}
