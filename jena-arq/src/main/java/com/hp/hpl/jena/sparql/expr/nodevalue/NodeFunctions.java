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

package com.hp.hpl.jena.sparql.expr.nodevalue ;

import java.util.Iterator ;
import java.util.UUID ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.Violation ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.ExprTypeException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.vocabulary.XSD ;

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
        RDFDatatype dt = n.getLiteralDatatype() ;
        String lang = n.getLiteralLanguage() ;

        if ( dt != null && !dt.equals(XSDDatatype.XSDstring) )
            throw new ExprEvalException(label + ": Not a string literal: " + nv) ;
        return n ;
    }

    /**
     * Check for string operations with primary first arg and second second arg
     * (e.g. CONTAINS)
     */
    public static void checkTwoArgumentStringLiterals(String label, NodeValue arg1, NodeValue arg2) {
        Node n1 = checkAndGetStringLiteral(label, arg1) ;
        Node n2 = checkAndGetStringLiteral(label, arg2) ;
        String lang1 = n1.getLiteralLanguage() ;
        String lang2 = n2.getLiteralLanguage() ;
        if ( lang1 == null )
            lang1 = "" ;
        if ( lang2 == null )
            lang2 = "" ;

        if ( n1.getLiteralDatatype() != null ) {
            // n1 is an xsd string by checkAndGetString
            if ( XSDDatatype.XSDstring.equals(n2.getLiteralDatatypeURI()) )
                return ;
            if ( n2.getLiteralLanguage().equals("") )
                return ;
            throw new ExprEvalException(label + ": Incompatible: " + arg1 + " and " + arg2) ;
        }

        // Incompatible?
        // arg1 simple or xsd:string, arg2 has a lang.
        if ( lang1.equals("") && !lang2.equals("") )
            throw new ExprEvalException(label + ": Incompatible: " + arg1 + " and " + arg2) ;
        // arg1 with lang, arg2 has a different lang.
        if ( !lang1.equals("") && (!lang2.equals("") && !lang1.equals(lang2)) )
            throw new ExprEvalException(label + ": Incompatible: " + arg1 + " and " + arg2) ;
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
                return NodeConst.dtRDFlangString ;
            return XSD.xstring.asNode() ;
        }
        return NodeFactory.createURI(s) ;
    }

    // -------- lang

    public static NodeValue lang(NodeValue nv) {
        return NodeValue.makeString(lang(nv.asNode())) ;
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
    public static NodeValue langMatches(NodeValue nv, NodeValue nvPattern) {
        return langMatches(nv, nvPattern.getString()) ;
    }

    public static NodeValue langMatches(NodeValue nv, String langPattern) {
        Node node = nv.asNode() ;
        if ( !node.isLiteral() ) {
            NodeValue.raise(new ExprTypeException("langMatches: not a literal: " + node)) ;
            return null ;
        }

        String nodeLang = node.getLiteralLexicalForm() ;

        if ( langPattern.equals("*") ) {
            if ( nodeLang == null || nodeLang.equals("") )
                return NodeValue.FALSE ;
            return NodeValue.TRUE ;
        }

        // See RFC 3066 (it's "tag (-tag)*)"

        String[] langElts = nodeLang.split("-") ;
        String[] langRangeElts = langPattern.split("-") ;

        /*
         * Here is the logic to compare language code. There is a match if the
         * language matches the parts of the pattern - the language may be
         * longer than the pattern.
         */

        /*
         * RFC 4647 basic filtering.
         * 
         * To do extended: 1. Remove any -*- (but not *-) 2. Compare primary
         * tags. 3. Is the remaining range a subsequence of the remaining
         * language tag?
         */

        // // Step one: remove "-*-" (but not "*-")
        // int j = 1 ;
        // for ( int i = 1 ; i < langRangeElts.length ; i++ )
        // {
        // String range = langRangeElts[i] ;
        // if ( range.equals("*") )
        // continue ;
        // langRangeElts[j] = range ;
        // j++ ;
        // }
        //
        // // Null fill any free space.
        // for ( int i = j ; i < langRangeElts.length ; i++ )
        // langRangeElts[i] = null ;

        // This is basic specific.

        if ( langRangeElts.length > langElts.length )
            // Lang tag longer than pattern tag => can't match
            return NodeValue.FALSE ;
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
                return NodeValue.FALSE ;
        }
        return NodeValue.TRUE ;
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

    private static final IRIFactory iriFactory      = IRIFactory.iriImplementation() ;
    public static boolean           warningsForIRIs = false ;

    // -------- IRI
    public static NodeValue iri(NodeValue nv, String baseIRI) {
        if ( isIRI(nv.asNode()) )
            return nv ;
        Node n2 = iri(nv.asNode(), baseIRI) ;
        return NodeValue.makeNode(n2) ;
    }

    public static Node iri(Node nv, String baseIRI) {
        if ( nv.isURI() )
            return nv ;

        if ( nv.isBlank() ) {
            // Skolemization of blank nodes to IRIs : Don't ask, just don't ask.
            String x = nv.getBlankNodeLabel() ;
            return NodeFactory.createURI("_:" + x) ;
        }

        // Simple literal or xsd:string
        String str = simpleLiteralOrXSDString(nv) ;
        if ( str == null )
            throw new ExprEvalException("Can't make an IRI from " + nv) ;

        IRI iri = null ;
        String iriStr = nv.getLiteralLexicalForm() ;

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

    // The Jena version can vbe slow to inityailise (but is pure java)

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

        Node n = NodeFactory.createLiteral(lex, null, NodeFactory.getType(dt.getURI())) ;
        return NodeValue.makeNode(n) ;
    }

    public static NodeValue strLang(NodeValue v1, NodeValue v2) {
        if ( !v1.isString() )
            throw new ExprEvalException("Not a string (arg 1): " + v1) ;
        if ( !v2.isString() )
            throw new ExprEvalException("Not a string (arg 2): " + v2) ;

        String lex = v1.asString() ;
        String lang = v2.asString() ;
        // Check?

        Node n = NodeFactory.createLiteral(lex, lang, null) ;
        return NodeValue.makeNode(n) ;
    }

}
