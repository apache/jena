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

package com.hp.hpl.jena.sparql.lang;

import java.math.BigInteger ;
import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.riot.checker.CheckerIRI ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.RiotLib ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.n3.JenaURIException ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.E_Exists ;
import com.hp.hpl.jena.sparql.expr.E_NotExists ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.sparql.modify.request.QuadAccSink ;
import com.hp.hpl.jena.sparql.path.Path ;
import com.hp.hpl.jena.sparql.syntax.* ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.LabelToNodeMap ;
import com.hp.hpl.jena.vocabulary.RDF ;

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
    final LabelToNodeMap bNodeLabels = LabelToNodeMap.createBNodeMap() ;
    
    // label => bNode (as variable) for graph patterns
    final LabelToNodeMap anonVarLabels = LabelToNodeMap.createVarMap() ;
    
    // This is the map used allocate blank node labels during parsing.
    // 1/ It is different between CONSTRUCT and the query pattern
    // 2/ Each BasicGraphPattern is a scope for blank node labels so each
    //    BGP causes the map to be cleared at the start of the BGP
    
    LabelToNodeMap activeLabelMap = anonVarLabels ;
    Set<String> previousLabels = new HashSet<>() ;
    
    //LabelToNodeMap listLabelMap = new LabelToNodeMap(true, new VarAlloc("L")) ;
    // ----
    
    public ParserBase() {}
    
    protected Prologue prologue ;
    public void setPrologue(Prologue prologue) { this.prologue = prologue ; }
    public Prologue getPrologue() { return prologue ; }
    
    protected void setInConstructTemplate(boolean b)
    {
        setBNodesAreVariables(!b) ;
    }
    
    protected boolean getBNodesAreVariables()   { return bNodesAreVariables ; }
    protected void setBNodesAreVariables(boolean bNodesAreVariables)
    {
        this.bNodesAreVariables = bNodesAreVariables ;
        if ( bNodesAreVariables )
            activeLabelMap = anonVarLabels ;
        else 
            activeLabelMap = bNodeLabels  ;
    }
    
    protected boolean getBNodesAreAllowed()   { return bNodesAreAllowed ; }
    protected void setBNodesAreAllowed(boolean bNodesAreAllowed)
    {
        this.bNodesAreAllowed = bNodesAreAllowed ;
    }

    protected Element compressGroupOfOneGroup(ElementGroup elg)
    {
        // remove group of one group.
        if ( elg.getElements().size() == 1 )
        {
            Element e1 = elg.getElements().get(0) ;
            if ( e1 instanceof ElementGroup )
                return e1 ;
        }
        return elg ;
    }
    
    protected Node createLiteralInteger(String lexicalForm)
    {
        return NodeFactory.createLiteral(lexicalForm, null, XSDDatatype.XSDinteger) ;
    }
    
    protected Node createLiteralDouble(String lexicalForm)
    {
        return NodeFactory.createLiteral(lexicalForm, null, XSDDatatype.XSDdouble) ;
    }
    
    protected Node createLiteralDecimal(String lexicalForm)
    {
        return NodeFactory.createLiteral(lexicalForm, null, XSDDatatype.XSDdecimal) ;
    }

    protected Node stripSign(Node node)
    {
        if ( ! node.isLiteral() ) return node ;
        String lex = node.getLiteralLexicalForm() ;
        String lang = node.getLiteralLanguage() ;
        RDFDatatype dt = node.getLiteralDatatype() ;
        
        if ( ! lex.startsWith("-") && ! lex.startsWith("+") )
            throw new ARQInternalErrorException("Literal does not start with a sign: "+lex) ;
        
        lex = lex.substring(1) ;
        return NodeFactory.createLiteral(lex, lang, dt) ;
    }
    
    protected Node createLiteral(String lexicalForm, String langTag, String datatypeURI)
    {
        Node n = null ;
        // Can't have type and lang tag.
        if ( datatypeURI != null)
        {
            RDFDatatype dType = TypeMapper.getInstance().getSafeTypeByName(datatypeURI) ;
            n = NodeFactory.createLiteral(lexicalForm, null, dType) ;
        }
        else
            n = NodeFactory.createLiteral(lexicalForm, langTag, null) ;
        return n ;
    }
    
    protected long integerValue(String s)
    {
        try {
            if ( s.startsWith("+") )
                s = s.substring(1) ;
            if ( s.startsWith("0x") )
            {
                // Hex
                s = s.substring(2) ;
                return Long.parseLong(s, 16) ;
            }
            return Long.parseLong(s) ;
        } catch (NumberFormatException ex)
        {
            try {
                // Possible too large for a long.
                BigInteger integer = new BigInteger(s) ;
                throwParseException("Number '"+s+"' is a valid number but can't not be stored in a long") ;
            } catch (NumberFormatException ex2) {}
            throw new QueryParseException(ex, -1, -1) ;
        }
    }
    
    protected double doubleValue(String s)
    {
        if ( s.startsWith("+") )
            s = s.substring(1) ;
        double valDouble = Double.parseDouble(s) ;
        return valDouble ; 
    }
    
    /** Remove first and last characters (e.g. ' or "") from a string */
    protected static String stripQuotes(String s)
    {
        return s.substring(1,s.length()-1)  ;
    }
    
    /** Remove first 3 and last 3 characters (e.g. ''' or """) from a string */ 
    protected static String stripQuotes3(String s)
    {
        return s.substring(3,s.length()-3)  ;
    }

    /** remove the first n charcacters from the string*/ 
    public static String stripChars(String s, int n)
    {
        return s.substring(n, s.length())  ;
    }
        
    protected Var createVariable(String s, int line, int column)
    {
        s = s.substring(1) ; // Drop the marker
        
        // This is done by the parser input stream nowadays.
        //s = unescapeCodePoint(s, line, column) ;
        // Check \ u did not put in any illegals. 
        return Var.alloc(s) ;
    }

    // ---- IRIs and Nodes
    // See RiotLib re bNode IRIs.
    // Merge sometime.
    
    protected String resolveQuotedIRI(String iriStr, int line, int column)
    {
        iriStr = stripQuotes(iriStr) ;
        return resolveIRI(iriStr, line, column) ;
    }

    public static final String ParserLoggerName = "SPARQL" ;
    private static Logger parserLog = LoggerFactory.getLogger(ParserLoggerName) ;
    private static ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(parserLog) ;

    protected String resolveIRI(String iriStr, int line, int column)
    {
        if ( isBNodeIRI(iriStr) )
            return iriStr ;
        
        if ( getPrologue() != null )
        {
            if ( getPrologue().getResolver() != null )
                try {
                    // Used to be errors (pre Jena 2.12.0)
                    // .resolve(iriStr)
                    IRI iri = getPrologue().getResolver().resolveSilent(iriStr) ;
                    if ( true ) 
                        CheckerIRI.iriViolations(iri, errorHandler, line, column) ;
                    iriStr = iri.toString() ;
                } catch (JenaURIException ex)
                { throwParseException(ex.getMessage(), line, column) ; }
        }
        return iriStr ;
    }
    
    protected String resolvePName(String qname, int line, int column)
    {
        // It's legal.
        int idx = qname.indexOf(':') ;
        
        // -- Escapes in local name
        String prefix = qname.substring(0, idx) ;
        String local = qname.substring(idx+1) ;
        local = unescapePName(local, line, column) ;
        qname = prefix+":"+local ;
        // --
        
        String s = getPrologue().expandPrefixedName(qname) ;
        if ( s == null )
            throwParseException("Unresolved prefixed name: "+qname, line, column) ;
        return s ;
    }
    
    boolean skolomizedBNodes = ARQ.isTrue(ARQ.constantBNodeLabels) ;
    protected Node createNode(String iri)
    {
        if ( skolomizedBNodes )
            return RiotLib.createIRIorBNode(iri) ;
        else
            return NodeFactory.createURI(iri) ;
    }
    
    protected boolean isBNodeIRI(String iri)
    {
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
    protected void startGroup(ElementGroup elg)
    { 
        endBasicGraphPattern() ;
        startBasicGraphPattern() ;
    }
    
    protected void endGroup(ElementGroup elg)
    { 
        endBasicGraphPattern() ;
    }
    
    // --------
    
    // BNode from a list
//    protected Node createListNode()
//    { return listLabelMap.allocNode() ; }
    
    protected Node createListNode(int line, int column) { return createBNode(line, column) ; }

    // Unlabelled bNode.
    protected Node createBNode(int line, int column)
    {
        if ( ! bNodesAreAllowed )
            throwParseException("Blank nodes not allowed in DELETE templates", line, column) ;
        return activeLabelMap.allocNode() ;
    }
    
    // Labelled bNode.
    protected Node createBNode(String label, int line, int column)
    { 
        if ( ! bNodesAreAllowed )
            throwParseException("Blank nodes not allowed in DELETE templates: "+label, line, column) ;
        if ( previousLabels.contains(label) )
            throwParseException("Blank node label reuse not allowed at this point: "+label,
                                line, column) ;
        
        //label = unescapeCodePoint(label, line, column) ;
        return activeLabelMap.asNode(label) ;
    }
    
    protected Expr createExprExists(Element element)
    {
        return new E_Exists(element) ;
    }
    
    protected Expr createExprNotExists(Element element)
    {
        // Could negate here.
        return new E_NotExists(element) ;
    }
    
    protected String fixupPrefix(String prefix, int line, int column)
    {
        // \ u processing!
        if ( prefix.endsWith(":") )
            prefix = prefix.substring(0, prefix.length()-1) ;
        return prefix ; 
    }
    
    protected void setAccGraph(QuadAccSink acc, Node gn)
    {
        acc.setGraph(gn) ;
    }
    
    protected void insert(TripleCollector acc, Node s, Node p, Node o)
    {
        acc.addTriple(new Triple(s, p, o)) ;
    }
    
    protected void insert(TripleCollectorMark acc, int index, Node s, Node p, Node o)
    {
        acc.addTriple(index, new Triple(s, p, o)) ;
    }
    
    protected void insert(TripleCollector acc, Node s, Node p, Path path, Node o)
    {
        if ( p == null )
            acc.addTriplePath(new TriplePath(s, path, o)) ;
        else
            acc.addTriple(new Triple(s, p, o)) ;
    }
    
    protected void insert(TripleCollectorMark acc, int index, Node s, Node p, Path path, Node o)
    {
        if ( p == null )
            acc.addTriplePath(index, new TriplePath(s, path, o)) ;
        else
            acc.addTriple(index, new Triple(s, p, o)) ;
    }
    
    protected void insert(TripleCollector target, ElementPathBlock source)
    {
        for (TriplePath path : source.getPattern())
        {
            if (path.isTriple())
            {
                target.addTriple(path.asTriple());
            }
            else
            {
                target.addTriplePath(path);
            }
        }
    }

    protected Expr asExpr(Node n)
    {
        return ExprUtils.nodeToExpr(n) ;
    }

    protected Expr asExprNoSign(Node n)
    {
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

    
    public static String unescapeStr(String s, int line, int column)
    { return unescape(s, '\\', false, line, column) ; }
    
    // Worker function
    public static String unescape(String s, char escape, boolean pointCodeOnly, int line, int column)
    {
        int i = s.indexOf(escape) ;
        
        if ( i == -1 )
            return s ;
        
        // Dump the initial part straight into the string buffer
        StringBuilder sb = new StringBuilder(s.substring(0,i)) ;
        
        for ( ; i < s.length() ; i++ )
        {
            char ch = s.charAt(i) ;
            // Keep line and column numbers.
            switch (ch)
            {
                case '\n': 
                case '\r':
                    line++ ;
                    column = 1 ;
                    break ;
                default:
                    column++ ;
                    break ;
            }

            if ( ch != escape )
            {
                sb.append(ch) ;
                continue ;
            }
                
            // Escape
            if ( i >= s.length()-1 )
                throwParseException("Illegal escape at end of string", line, column) ;
            char ch2 = s.charAt(i+1) ;
            column = column+1 ;
            i = i + 1 ;
            
            // \\u and \\U
            if ( ch2 == 'u' )
            {
                // i points to the \ so i+6 is next character
                if ( i+4 >= s.length() )
                    throwParseException("\\u escape too short", line, column) ;
                int x = hex(s, i+1, 4, line, column) ;
                sb.append((char)x) ;
                // Jump 1 2 3 4 -- already skipped \ and u
                i = i+4 ;
                column = column+4 ;
                continue ;
            }
            if ( ch2 == 'U' )
            {
                // i points to the \ so i+6 is next character
                if ( i+8 >= s.length() )
                    throwParseException("\\U escape too short", line, column) ;
                int x = hex(s, i+1, 8, line, column) ;
                // Convert to UTF-16 codepoint pair.
                sb.append((char)x) ;
                // Jump 1 2 3 4 5 6 7 8 -- already skipped \ and u
                i = i+8 ;
                column = column+8 ;
                continue ;
            }
            
            // Are we doing just point code escapes?
            // If so, \X-anything else is legal as a literal "\" and "X" 
            
            if ( pointCodeOnly )
            {
                sb.append('\\') ;
                sb.append(ch2) ;
                i = i + 1 ;
                continue ;
            }
            
            // Not just codepoints.  Must be a legal escape.
            char ch3 = 0 ;
            switch (ch2)
            {
                case 'n': ch3 = '\n' ;  break ; 
                case 't': ch3 = '\t' ;  break ;
                case 'r': ch3 = '\r' ;  break ;
                case 'b': ch3 = '\b' ;  break ;
                case 'f': ch3 = '\f' ;  break ;
                case '\'': ch3 = '\'' ; break ;
                case '\"': ch3 = '\"' ; break ;
                case '\\': ch3 = '\\' ; break ;
                default:
                    throwParseException("Unknown escape: \\"+ch2, line, column) ;
            }
            sb.append(ch3) ;
        }
        return sb.toString() ;
    }

    // Line and column that started the escape
    public static int hex(String s, int i, int len, int line, int column)
    {
//        if ( i+len >= s.length() )
//        {
//            
//        }
        int x = 0 ;
        for ( int j = i ; j < i+len ; j++ )
        {
           char ch = s.charAt(j) ;
           column++ ;
           int k = 0  ;
           switch (ch)
           {
               case '0': k = 0 ; break ; 
               case '1': k = 1 ; break ;
               case '2': k = 2 ; break ;
               case '3': k = 3 ; break ;
               case '4': k = 4 ; break ;
               case '5': k = 5 ; break ;
               case '6': k = 6 ; break ;
               case '7': k = 7 ; break ;
               case '8': k = 8 ; break ;
               case '9': k = 9 ; break ;
               case 'A': case 'a': k = 10 ; break ;
               case 'B': case 'b': k = 11 ; break ;
               case 'C': case 'c': k = 12 ; break ;
               case 'D': case 'd': k = 13 ; break ;
               case 'E': case 'e': k = 14 ; break ;
               case 'F': case 'f': k = 15 ; break ;
               default:
                   throwParseException("Illegal hex escape: "+ch, line, column) ;
           }
           x = (x<<4)+k ;
        }
        return x ;
    }
    
    public static String  unescapePName(String s, int line, int column)
    {
        char escape = '\\' ;
        int idx = s.indexOf(escape) ;
        
        if ( idx == -1 )
            return s ;
        
        int len = s.length() ;
        StringBuilder sb = new StringBuilder() ;
        
        for ( int i = 0 ; i < len ; i++ )
        {
            // Copied form unescape abobve - share!
            char ch = s.charAt(i) ;
            // Keep line and column numbers.
            switch (ch)
            {
                case '\n': 
                case '\r':
                    line++ ;
                    column = 1 ;
                    break ;
                default:
                    column++ ;
                    break ;
            }

            if ( ch != escape )
            {
                sb.append(ch) ;
                continue ;
            }

            // Escape
            if ( i >= s.length()-1 )
                throwParseException("Illegal escape at end of string", line, column) ;
            char ch2 = s.charAt(i+1) ;
            column = column+1 ;
            i = i + 1 ;

           switch (ch2)
           {
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
               default:
                   throwParseException("Illegal prefix name escape: "+ch2, line, column) ;
           }
        }
        return sb.toString() ;
    }
    
    
    protected void warnDeprecation(String msg)
    {
        Log.warn(this, msg) ;
    }
    
    public static void throwParseException(String msg, int line, int column)
    {
        throw new QueryParseException("Line " + line + ", column " + column + ": " + msg,
                                      line, column) ;
    }
    
    public static void throwParseException(String msg)
    {
        throw new QueryParseException(msg, -1, -1) ;
    }

}
