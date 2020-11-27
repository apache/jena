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

package org.apache.jena.sparql.lang;

import java.math.BigInteger ;
import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.lib.EscapeStr ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.TypeMapper ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.query.QueryParseException ;
import org.apache.jena.riot.checker.CheckerIRI ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.core.Prologue ;
import org.apache.jena.sparql.core.TriplePath ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.E_Exists ;
import org.apache.jena.sparql.expr.E_NotExists ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.graph.NodeConst ;
import org.apache.jena.sparql.modify.request.QuadAccSink ;
import org.apache.jena.sparql.path.Path ;
import org.apache.jena.sparql.syntax.* ;
import org.apache.jena.sparql.util.ExprUtils ;
import org.apache.jena.sparql.util.LabelToNodeMap ;
import org.apache.jena.ttl.JenaURIException;
import org.apache.jena.vocabulary.RDF ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Base class parsers, mainly SPARQL related */
public class ParserBase
{
    // NodeConst
    protected final Node XSD_TRUE       = NodeConst.nodeTrue ;
    protected final Node XSD_FALSE      = NodeConst.nodeFalse ;

    protected final Node nRDFtype       = NodeConst.nodeRDFType ;

    protected final Node nRDFnil        = NodeConst.nodeNil ;
    protected final Node nRDFfirst      = NodeConst.nodeFirst ;
    protected final Node nRDFrest       = NodeConst.nodeRest ;

    protected final Node nRDFsubject    = RDF.Nodes.subject ;
    protected final Node nRDFpredicate  = RDF.Nodes.predicate ;
    protected final Node nRDFobject     = RDF.Nodes.object ;

    // ----
    // Graph patterns, true; in templates, false.
    private boolean bNodesAreVariables = true ;
    // In DELETE, false.
    private boolean bNodesAreAllowed = true ;

    // label => bNode for construct templates patterns
    @SuppressWarnings("deprecation")
    final LabelToNodeMap bNodeLabels = LabelToNodeMap.createBNodeMap() ;

    // label => bNode (as variable) for graph patterns
    final LabelToNodeMap anonVarLabels = LabelToNodeMap.createVarMap() ;

    // This is the map used allocate blank node labels during parsing.
    // 1/ It is different between CONSTRUCT and the query pattern
    // 2/ Each BasicGraphPattern is a scope for blank node labels so eachf
    //    BGP causes the map to be cleared at the start of the BGP

    LabelToNodeMap activeLabelMap = anonVarLabels ;
    Set<String> previousLabels = new HashSet<>() ;

    // Aggregates are only allowed in places where grouping can happen.
    // e.g. SELECT clause but not a FILTER.
    private boolean allowAggregatesInExpressions = false ;
    private int     aggregateDepth               = 0 ;

    //LabelToNodeMap listLabelMap = new LabelToNodeMap(true, new VarAlloc("L")) ;
    // ----

    public ParserBase() {}

    protected Prologue prologue ;
    public void setPrologue(Prologue prologue) { this.prologue = prologue ; }
    public Prologue getPrologue() { return prologue ; }

    protected void setBase(String iriStr, int line, int column) {
        if ( isBNodeIRI(iriStr) )
            throwParseException("Blank node URI syntax used for BASE", line, column);
        iriStr = resolveIRI(iriStr, line, column);
        getPrologue().setBaseURI(iriStr);
    }

    protected void setPrefix(String prefix, String uriStr, int line, int column) {
        // Should have happen in the parser because this step is "token to prefix".
        //prefix = fixupPrefix(prefix, line, column);
        getPrologue().setPrefix(prefix, uriStr);
    }

    protected void setInConstructTemplate(boolean b) {
        setBNodesAreVariables(!b) ;
    }

    protected boolean getBNodesAreVariables()   { return bNodesAreVariables ; }

    protected void setBNodesAreVariables(boolean bNodesAreVariables) {
        this.bNodesAreVariables = bNodesAreVariables ;
        if ( bNodesAreVariables )
            activeLabelMap = anonVarLabels ;
        else
            activeLabelMap = bNodeLabels ;
    }

    protected boolean getBNodesAreAllowed()   { return bNodesAreAllowed ; }

    protected void setBNodesAreAllowed(boolean bNodesAreAllowed) {
        this.bNodesAreAllowed = bNodesAreAllowed ;
    }

    protected boolean getAllowAggregatesInExpressions() {
        return allowAggregatesInExpressions ;
    }

    protected void setAllowAggregatesInExpressions(boolean allowAggregatesInExpressions) {
        this.allowAggregatesInExpressions = allowAggregatesInExpressions;
    }

    // Tracking for nested aggregates.
    protected void startAggregate()   { aggregateDepth++; }
    protected int getAggregateDepth() { return aggregateDepth; }
    protected void finishAggregate()  { aggregateDepth--; }

    protected Element compressGroupOfOneGroup(ElementGroup elg) {
        // remove group of one group.
        if ( elg.size() == 1 ) {
            Element e1 = elg.get(0) ;
            if ( e1 instanceof ElementGroup )
                return e1 ;
        }
        return elg ;
    }

    protected Node createLiteralInteger(String lexicalForm) {
        return NodeFactory.createLiteral(lexicalForm, XSDDatatype.XSDinteger) ;
    }

    protected Node createLiteralDouble(String lexicalForm) {
        return NodeFactory.createLiteral(lexicalForm, XSDDatatype.XSDdouble) ;
    }

    protected Node createLiteralDecimal(String lexicalForm) {
        return NodeFactory.createLiteral(lexicalForm, XSDDatatype.XSDdecimal) ;
    }

    protected Node stripSign(Node node) {
        if ( !node.isLiteral() )
            return node ;
        String lex = node.getLiteralLexicalForm() ;
        String lang = node.getLiteralLanguage() ;
        RDFDatatype dt = node.getLiteralDatatype() ;

        if ( !lex.startsWith("-") && !lex.startsWith("+") )
            throw new ARQInternalErrorException("Literal does not start with a sign: " + lex) ;

        lex = lex.substring(1) ;
        return NodeFactory.createLiteral(lex, lang, dt) ;
    }

    protected void checkString(String string, int line, int column) {
        for ( int i = 0 ; i < string.length() ; i++ ) {
            // Not "codePointAt" which does surrogate processing.
            char ch = string.charAt(i);
            // Check surrogate pairs are pairs.
            if ( Character.isHighSurrogate(ch) ) {
                i++;
                if ( i == string.length() )
                    throw new QueryParseException("Bad surrogate pair (end of string)", line, column);
                char ch1 = string.charAt(i);
                if ( ! Character.isLowSurrogate(ch1) ) {
                    throw new QueryParseException("Bad surrogate pair (high surrogate not followed by low surrogate)", line, column);
                }
            } else if ( Character.isLowSurrogate(ch) ) {
                throw new QueryParseException("Bad surrogate pair (low surrogate without high surrogate)", line, column);
            }
        }
    }

    protected Node createLiteral(String lexicalForm, String langTag, String datatypeURI) {
        Node n = null ;
        // Can't have type and lang tag in parsing.
        if ( datatypeURI != null ) {
            RDFDatatype dType = TypeMapper.getInstance().getSafeTypeByName(datatypeURI) ;
            n = NodeFactory.createLiteral(lexicalForm, dType) ;
        } else if ( langTag != null && !langTag.isEmpty() )
            n = NodeFactory.createLiteral(lexicalForm, langTag) ;
        else
            n = NodeFactory.createLiteral(lexicalForm) ;
        return n ;
    }

    protected long integerValue(String s) {
        try {
            if ( s.startsWith("+") )
                s = s.substring(1) ;
            if ( s.startsWith("0x") ) {
                // Hex
                s = s.substring(2) ;
                return Long.parseLong(s, 16) ;
            }
            return Long.parseLong(s) ;
        }
        catch (NumberFormatException ex) {
            try {
                // Possible too large for a long.
                BigInteger integer = new BigInteger(s) ;
                throwParseException("Number '" + s + "' is a valid number but can't not be stored in a long") ;
            }
            catch (NumberFormatException ex2) {}
            throw new QueryParseException(ex, -1, -1) ;
        }
    }

    protected double doubleValue(String s) {
        if ( s.startsWith("+") )
            s = s.substring(1) ;
        double valDouble = Double.parseDouble(s) ;
        return valDouble ;
    }

    /** Remove first and last characters (e.g. ' or "") from a string */
    protected static String stripQuotes(String s) {
        return s.substring(1, s.length() - 1) ;
    }

    /** Remove first 3 and last 3 characters (e.g. ''' or """) from a string */
    protected static String stripQuotes3(String s) {
        return s.substring(3, s.length() - 3) ;
    }

    /** remove the first n characters from the string */
    public static String stripChars(String s, int n) {
        return s.substring(n, s.length()) ;
    }

    protected Var createVariable(String s, int line, int column) {
        s = s.substring(1) ; // Drop the marker

        // This is done by the parser input stream nowadays.
        // s = unescapeCodePoint(s, line, column) ;
        // Check \ u did not put in any illegals.
        return Var.alloc(s) ;
    }

    // ---- IRIs and Nodes

    protected String resolveQuotedIRI(String iriStr, int line, int column) {
        iriStr = stripQuotes(iriStr) ;
        iriStr = unescapeUnicode(iriStr, line, column);
        return resolveIRI(iriStr, line, column) ;
    }

    public static final String ParserLoggerName = "SPARQL" ;
    private static Logger parserLog = LoggerFactory.getLogger(ParserLoggerName) ;
    private static ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(parserLog) ;

    protected String resolveIRI(String iriStr, int line, int column) {
        if ( isBNodeIRI(iriStr) )
            return iriStr ;

        if ( getPrologue() != null ) {
            if ( getPrologue().getResolver() != null )
                try {
                    // Used to be errors (pre Jena 2.12.0)
                    // .resolve(iriStr)
                    IRI iri = getPrologue().getResolver().resolveSilent(iriStr) ;
                    if ( true )
                        CheckerIRI.iriViolations(iri, errorHandler, line, column) ;
                    iriStr = iri.toString() ;
                }
                catch (JenaURIException ex) {
                    throwParseException(ex.getMessage(), line, column) ;
                }
        }
        return iriStr ;
    }

    protected String resolvePName(String prefixedName, int line, int column) {
        // It's legal.
        int idx = prefixedName.indexOf(':') ;

        // -- Escapes in local name
        String prefix = prefixedName.substring(0, idx) ;
        String local = prefixedName.substring(idx + 1) ;
        local = unescapePName(local, line, column) ;
        prefixedName = prefix + ":" + local ;
        // --

        String s = getPrologue().expandPrefixedName(prefixedName) ;
        if ( s == null ) {
            if ( ARQ.isTrue(ARQ.fixupUndefinedPrefixes) )
                return RiotLib.fixupPrefixes.apply(prefixedName) ;
            throwParseException("Unresolved prefixed name: " + prefixedName, line, column) ;
        }
        return s ;
    }

    private boolean skolomizedBNodes = ARQ.isTrue(ARQ.constantBNodeLabels) ;

    protected Node createNode(String iri) {
        if ( skolomizedBNodes )
            return RiotLib.createIRIorBNode(iri) ;
        else
            return NodeFactory.createURI(iri) ;
    }

    protected boolean isBNodeIRI(String iri) {
        return skolomizedBNodes && RiotLib.isBNodeIRI(iri) ;
    }

    // -------- Basic Graph Patterns and Blank Node label scopes

    // A BasicGraphPattern is any sequence of TripleBlocks, separated by filters,
    // but not by other graph patterns.

    protected void startBasicGraphPattern()
    { activeLabelMap.clear() ; }

    protected void endBasicGraphPattern()
    { previousLabels.addAll(activeLabelMap.getLabels()) ; }

    protected void startTriplesBlock()
    { }

    protected void endTriplesBlock()
    { }

    // On entry to a new group, the current BGP is ended.
    protected void startGroup(ElementGroup elg) {
        endBasicGraphPattern() ;
        startBasicGraphPattern() ;
    }

    protected void endGroup(ElementGroup elg) {
        endBasicGraphPattern() ;
    }

    // --------

    protected void checkConcrete(Node n, int line, int column) {
        if ( ! n.isConcrete() )
            throwParseException("Term is not concrete: "+n, line, column) ;
    }

    // BNode from a list
//    protected Node createListNode()
//    { return listLabelMap.allocNode() ; }

    protected Node createListNode(int line, int column) { return createBNode(line, column) ; }

    // Unlabelled bNode.
    protected Node createBNode(int line, int column) {
        if ( !bNodesAreAllowed )
            throwParseException("Blank nodes not allowed in DELETE templates", line, column) ;
        return activeLabelMap.allocNode() ;
    }

    // Labelled bNode.
    protected Node createBNode(String label, int line, int column) {
        if ( !bNodesAreAllowed )
            throwParseException("Blank nodes not allowed in DELETE templates: " + label, line, column) ;
        if ( previousLabels.contains(label) )
            throwParseException("Blank node label reuse not allowed at this point: " + label, line, column) ;

        // label = unescapeCodePoint(label, line, column) ;
        return activeLabelMap.asNode(label) ;
    }

    protected Node createTripleTerm(Node s, Node p, Node o, int line, int column) {
        return NodeFactory.createTripleNode(s, p, o);
    }

    protected Expr createExprExists(Element element) {
        return new E_Exists(element) ;
    }

    protected Expr createExprNotExists(Element element) {
        // Could negate here.
        return new E_NotExists(element) ;
    }

    // Convert a parser token, which includes the final ":", to a prefix name.
    protected String fixupPrefix(String prefix, int line, int column) {
        // \ u processing!
        if ( prefix.endsWith(":") )
            prefix = prefix.substring(0, prefix.length() - 1) ;
        return prefix ;
    }

    protected void setAccGraph(QuadAccSink acc, Node gn) {
        acc.setGraph(gn) ;
    }

    protected void insert(TripleCollector acc, Node s, Node p, Node o) {
        acc.addTriple(new Triple(s, p, o)) ;
    }

    protected void insert(TripleCollectorMark acc, int index, Node s, Node p, Node o) {
        acc.addTriple(index, new Triple(s, p, o)) ;
    }

    protected void insert(TripleCollector acc, Node s, Node p, Path path, Node o) {
        if ( p == null )
            acc.addTriplePath(new TriplePath(s, path, o)) ;
        else
            acc.addTriple(new Triple(s, p, o)) ;
    }

    protected void insert(TripleCollectorMark acc, int index, Node s, Node p, Path path, Node o) {
        if ( p == null )
            acc.addTriplePath(index, new TriplePath(s, path, o)) ;
        else
            acc.addTriple(index, new Triple(s, p, o)) ;
    }

    protected void insert(TripleCollector target, ElementPathBlock source) {
        for ( TriplePath path : source.getPattern() ) {
            if ( path.isTriple() ) {
                target.addTriple(path.asTriple()) ;
            } else {
                target.addTriplePath(path) ;
            }
        }
    }

    protected Expr asExpr(Node n) {
        return ExprUtils.nodeToExpr(n) ;
    }

    protected Expr asExprNoSign(Node n) {
        String lex = n.getLiteralLexicalForm() ;
        String lang = n.getLiteralLanguage() ;
        String dtURI = n.getLiteralDatatypeURI() ;
        n = createLiteral(lex, lang, dtURI) ;
        return ExprUtils.nodeToExpr(n) ;
    }

    // Utilities to remove escapes in strings.

    public static String unescapeStr(String s)
    { return unescape(s, '\\', false, 1, 1) ; }

//    public static String unescapeCodePoint(String s)
//    { return unescape(s, '\\', true, 1, 1) ; }
//
//    protected String unescapeCodePoint(String s, int line, int column)
//    { return unescape(s, '\\', true, line, column) ; }


    // Do we nee dthe line/column versions?
    // Why not catch exceptions and comvert to  QueryParseException

    public static String unescapeStr(String s, int line, int column)
    { return unescape(s, '\\', false, line, column) ; }

    // Worker function
    public static String unescape(String s, char escape, boolean pointCodeOnly, int line, int column) {
        try {
            return EscapeStr.unescape(s, escape, pointCodeOnly) ;
        } catch (AtlasException ex) {
            throwParseException(ex.getMessage(), line, column) ;
            return null ;
        }
    }

    public static String unescapeUnicode(String s, int line, int column) {
        return unescape(s, '\\', true, line, column);
    }

    public static String unescapePName(String s, int line, int column) {
        char escape = '\\' ;
        int idx = s.indexOf(escape) ;

        if ( idx == -1 )
            return s ;

        int len = s.length() ;
        StringBuilder sb = new StringBuilder() ;

        for ( int i = 0 ; i < len ; i++ ) {
            char ch = s.charAt(i) ;
            // Keep line and column numbers.
            switch (ch) {
                case '\n' :
                case '\r' :
                    line++ ;
                    column = 1 ;
                    break ;
                default :
                    column++ ;
                    break ;
            }

            if ( ch != escape ) {
                sb.append(ch) ;
                continue ;
            }

            // Escape
            if ( i >= s.length() - 1 )
                throwParseException("Illegal escape at end of string", line, column) ;
            char ch2 = s.charAt(i + 1) ;
            column = column + 1 ;
            i = i + 1 ;

            switch (ch2) {   // PN_LOCAL_ESC
                case '_' :
                case '~' :
                case '.' :
                case '-' :
                case '!' :
                case '$' :
                case '&' :
                case '\'' :
                case '(' :
                case ')' :
                case '*' :
                case '+' :
                case ',' :
                case ';' :
                case '=' :
                case ':' :
                case '/' :
                case '?' :
                case '#' :
                case '@' :
                case '%' :
                    sb.append(ch2) ;
                    break ;
                default :
                    throwParseException("Illegal prefix name escape: " + ch2, line, column) ;
            }
        }
        return sb.toString() ;
    }

    protected void warnDeprecation(String msg) {
        Log.warn(this, msg) ;
    }

    public static void throwParseException(String msg, int line, int column) {
        throw new QueryParseException("Line " + line + ", column " + column + ": " + msg, line, column) ;
    }

    public static void throwParseException(String msg) {
        throw new QueryParseException(msg, -1, -1) ;
    }
}
