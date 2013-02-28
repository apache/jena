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

package com.hp.hpl.jena.sparql.util;

import java.util.Locale ;
import java.util.regex.Pattern ;


import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Node_Literal ;
import com.hp.hpl.jena.graph.Triple ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.IRIRelativize ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.vocabulary.XSD ;

/** Presentation forms of various kinds of objects.
 *  Beware that bNodes are abbreviated to _:b0 etc.
 */

public class FmtUtils
{
    // OLD CODE - being replaces by riot.NodeFmtLib 
    
    // Consider withdrawing non-serialzation context forms of this.
    // Or a temporary SerialzationContext does not abbreviate bNodes.
    static final String indentPrefix = "  " ;
    public static boolean multiLineExpr = false ;
    public static boolean printOpName = true ;
    
    static NodeToLabelMap bNodeMap = new NodeToLabelMapBNode("b", false) ;
    
    public static SerializationContext sCxt()
    { 
        return sCxt(ARQConstants.getGlobalPrefixMap()) ;
    }
    
    public static SerializationContext sCxt(PrefixMapping pmap)
    { 
        return new SerializationContext(pmap) ; 
    }

    // Formatting various items
    public static String stringForTriple(Triple triple)
    {
        return
            stringForNode(triple.getSubject())+" "+
            stringForNode(triple.getPredicate())+" "+
            stringForNode(triple.getObject()) ;
    }
    
    
    public static String stringForTriple(Triple triple, PrefixMapping prefixMap)
    {
        return
            stringForNode(triple.getSubject(), prefixMap)+" "+
            stringForNode(triple.getPredicate(), prefixMap)+" "+
            stringForNode(triple.getObject(), prefixMap) ;
    }
    
    // Formatting various items
    public static String stringForQuad(Quad quad)
    {
        StringBuilder sb = new StringBuilder() ;
        
        if ( quad.getGraph() != null )
        {
            sb.append(stringForNode(quad.getGraph())) ;
            sb.append(" ") ;
        }
        
        sb.append(stringForNode(quad.getSubject())) ;
        sb.append(" ") ;
        sb.append(stringForNode(quad.getPredicate())) ;
        sb.append(" ") ;
        sb.append(stringForNode(quad.getObject())) ;
        return sb.toString() ;
    }
    
    
    public static String stringForQuad(Quad quad, PrefixMapping prefixMap)
    {
        StringBuilder sb = new StringBuilder() ;
        
        if ( quad.getGraph() != null )
        {
            sb.append(stringForNode(quad.getGraph(), prefixMap)) ;
            sb.append(" ") ;
        }
        
        sb.append(stringForNode(quad.getSubject(), prefixMap)) ;
        sb.append(" ") ;
        sb.append(stringForNode(quad.getPredicate(), prefixMap)) ;
        sb.append(" ") ;
        sb.append(stringForNode(quad.getObject(), prefixMap)) ;
        return sb.toString() ;
    }
    
    // To a Writer?
    private static void formatTriple(IndentedWriter out, Triple triple, SerializationContext sCxt)
    {
        out.print(stringForNode(triple.getSubject(), sCxt)) ;
        out.print(" ") ;
        out.print(stringForNode(triple.getPredicate(), sCxt)) ;
        out.print(" ") ;
        out.print(stringForNode(triple.getObject(), sCxt)) ;
        out.print(" .") ;
    }
    
    public static void formatPattern(IndentedWriter out, BasicPattern pattern, SerializationContext sCxt)
    {
        boolean first = true ;
        for (Triple t : pattern )
        {
            if ( ! first )
                out.println() ;
            formatTriple(out, t, sCxt) ;
            first = false ;
        }
    }
    
//    private static String stringForQuad(Quad quad)
//    {
//        return stringForQuad(quad, ARQConstants.getGlobalPrefixMap()) ;
//    }
//    
//    
//    private static String stringForQuad(Quad quad, PrefixMapping prefixMap)
//    {
//        StringBuilder buff = new StringBuilder() ;
//        
//        if ( ! quad.isDefaultGraph() )
//        {
//            if ( quad.isUnionGraph() )
//                buff.append("*") ;
//            else
//                buff.append(stringForNode(quad.getGraph(), prefixMap)) ;
//            buff.append(" ") ;
//        }
//        buff.append(stringForNode(quad.getSubject(), prefixMap)) ;
//        buff.append(" ") ;
//        buff.append(stringForNode(quad.getPredicate(), prefixMap)) ;
//        buff.append(" ") ;
//        buff.append(stringForNode(quad.getObject(), prefixMap)) ;
//        return buff.toString() ;
//    }
//    
//    public void formatQuad(IndentedWriter out, Quad quad, SerializationContext sCxt)
//    {
//        out.print(stringForNode(quad.getGraph(), sCxt)) ;
//        out.print(" ") ;
//        out.print(stringForNode(quad.getSubject(), sCxt)) ;
//        out.print(" ") ;
//        out.print(stringForNode(quad.getPredicate(), sCxt)) ;
//        out.print(" ") ;
//        out.print(stringForNode(quad.getObject(), sCxt)) ;
//        out.print(" .") ;
//    }
    
    public static String stringForObject(Object obj)
    {
        if ( obj == null )
            return "<<null>>" ;

        if ( obj instanceof RDFNode )
            return stringForRDFNode((RDFNode)obj) ;
        if ( obj instanceof Node )
            return stringForNode((Node)obj) ;
        return obj.toString() ;
    }
    
    
    public static String stringForRDFNode(RDFNode obj)
    {
        Model m = null ;
        if ( obj instanceof Resource )
            m = ((Resource)obj).getModel() ;
        return stringForRDFNode(obj, newSerializationContext(m)) ;
    }

    public static String stringForRDFNode(RDFNode obj, SerializationContext context)
    {
        return stringForNode(obj.asNode(), context) ;
    }
    
    public static String stringForLiteral(Node_Literal literal, SerializationContext context)
    {
        String datatype = literal.getLiteralDatatypeURI() ;
        String lang = literal.getLiteralLanguage() ;
        String s = literal.getLiteralLexicalForm() ;
        
        //For some literals we can use plain literal form unless the Serialization Context
        //explicitly says not to
        //For backwards compatibility if using a null context then we use plain literal
        //forms where possible as this was the existing behaviour prior to this addition to the API
        if ( datatype != null && (context == null || context.getUsePlainLiterals()))
        {
            // Special form we know how to handle?
            // Assume valid text
            if ( datatype.equals(XSD.integer.getURI()) )
            {
                try {
                    String s1 = s ;
                    // BigInteger does not allow leading +
                    // so chop it off before the format test
                    // BigDecimal does allow a leading +
                    if ( s.startsWith("+") )
                        s1 = s.substring(1) ;
                    new java.math.BigInteger(s1) ;
                    return s ;
                } catch (NumberFormatException nfe) {}
                // No luck.  Continue.
                // Continuing is always safe.
            }
            
            if ( datatype.equals(XSD.decimal.getURI()) )
            {
                if ( s.indexOf('.') > 0 )
                {
                    try {
                        // BigDecimal does allow a leading +
                        new java.math.BigDecimal(s) ;
                        return s ;
                    } catch (NumberFormatException nfe) {}
                    // No luck.  Continue.
                }
            }
            
            if ( datatype.equals(XSD.xdouble.getURI()) )
            {
                // Assumes SPARQL has decimals and doubles.
                // Must have 'e' or 'E' to be a double short form.

                if ( s.indexOf('e') >= 0 || s.indexOf('E') >= 0 )
                {
                    try {
                        Double.parseDouble(s) ;
                        return s ;  // returm the original lexical form. 
                    } catch (NumberFormatException nfe) {}
                    // No luck.  Continue.
                }
            }

            if ( datatype.equals(XSD.xboolean.getURI()) )
            {
                // Pragmatics: if the data wrote "1"^^xsd:boolean, keep that form.  
                // The lexical form must be lower case. 
//                if ( s.equals("true") || s.equals("1") ) return s ;
//                if ( s.equals("false") || s.equals("0")  ) return s ;
                if ( s.equals("true") ) return s ;
                if ( s.equals("false") ) return s ;
                
            }
            // Not a recognized form.
        }
        
        StringBuilder sbuff = new StringBuilder() ;
        sbuff.append("\"") ;
        stringEsc(sbuff, s, true) ;
        sbuff.append("\"") ;
        
        // Format the language tag 
        if ( lang != null && lang.length()>0)
        {
            sbuff.append("@") ;
            sbuff.append(lang) ;
        }

        if ( datatype != null )
        {
            sbuff.append("^^") ;
            sbuff.append(stringForURI(datatype, context)) ;
        }
        
        return sbuff.toString() ;
    }
    
    public static String stringForString(String str)
    {
        StringBuilder sbuff = new StringBuilder() ;
        sbuff.append("\"") ;
        stringEsc(sbuff, str, true) ;
        sbuff.append("\"") ;
        return sbuff.toString() ; 
    }
    
    public static String stringForResource(Resource r)
    {
        return stringForResource(r, newSerializationContext(r.getModel())) ;
    }
   
    public static String stringForResource(Resource r, SerializationContext context)
    {
        return stringForNode(r.asNode(), context) ;
    }

    public static String stringForNode(Node n)
    { return stringForNode(n, ARQConstants.getGlobalPrefixMap()) ; }

    public static String stringForNode(Node n, PrefixMapping prefixMap)
    { return stringForNode(n, newSerializationContext(prefixMap)) ; }
    
    public static String stringForNode(Node n, Prologue prologue)
    { return stringForNode(n, newSerializationContext(prologue)) ; }

    public static String stringForNode(Node n, SerializationContext context)
    {
//        if ( context == null )
//            context = new SerializationContext() ;
        
        if ( n == null )
            return "<<null>>" ;
        
        // mappable?
        if ( context != null && context.getBNodeMap() != null )
        {
            String str = context.getBNodeMap().asString(n)  ;
            if ( str != null )
                return str ;
        }
        
        if ( n.isBlank() )
            return "_:"+n.getBlankNodeLabel() ;
        
        if ( n.isLiteral() )
            return stringForLiteral((Node_Literal)n, context) ;

        if ( n.isURI() )
        {
            String uri = n.getURI() ;
            return stringForURI(uri, context) ;
        }
        
        if ( n.isVariable() )
            return "?"+n.getName() ;
        
        if ( n.equals(Node.ANY) )
            return "ANY" ;

        Log.warn(FmtUtils.class, "Failed to turn a node into a string: "+n) ;
        return n.toString() ;
    }

    static public String stringForURI(String uri)
    { return "<"+stringEsc(uri)+">" ; }

    static public String stringForURI(String uri, Prologue prologue)
    {
        return stringForURI(uri, prologue.getBaseURI(), prologue.getPrefixMapping()) ;
    }

    static public String stringForURI(String uri, String baseIRI)
    {
        return stringForURI(uri, baseIRI, null) ;
    }
    
    static public String stringForURI(String uri, SerializationContext context)
    {
        if ( context == null )
            return stringForURI(uri, null, null) ;
//        if ( ! context.getPrologue().explicitlySetBaseURI() )
//            return stringForURI(uri, null, context.getPrefixMapping()) ;
        
        return stringForURI(uri, context.getBaseIRI(), context.getPrefixMapping()) ;
    }

    static public String stringForURI(String uri, PrefixMapping mapping)
    { return stringForURI(uri, null, mapping) ; }
    
    static public String stringForURI(String uri, String base, PrefixMapping mapping)
    {
        if ( mapping != null )
        {
            String pname = prefixFor(uri, mapping) ;
            if ( pname != null )
                return pname ;
        }
        if ( base != null )
        {
            String x = abbrevByBase(uri, base) ;
            if ( x != null ) 
                return "<"+x+">" ;
        }
        return stringForURI(uri) ; 
    }
    
    
    static private int relFlags = IRIRelativize.SAMEDOCUMENT | IRIRelativize.CHILD ;
    
    static public String abbrevByBase(String uri, String base)
    {
        if ( hasScheme(uri) )
            return uri ;
        IRI baseIRI = IRIFactory.jenaImplementation().construct(base) ;
        IRI rel = baseIRI.relativize(uri, relFlags) ;
        String r = rel.toString() ;
        return r ;
    }
    
    static private Pattern schemePattern = Pattern.compile("[A-Za-z]+:") ;
    static private boolean hasScheme(String uriStr)
    {
        return schemePattern.matcher(uriStr).matches() ;
    }
    
    private static String prefixFor(String uri, PrefixMapping mapping)
    {
        if ( mapping == null ) return null ;
        
        String pname = mapping.shortForm(uri) ;
        if ( pname != uri && checkValidPrefixName(pname) )
            return pname ;
        pname = mapping.qnameFor(uri) ;
        if ( pname != null && checkValidPrefixName(pname) )
            return pname ;
        return null ;
    }
    
    private static boolean checkValidPrefixName(String prefixedName)
    {
        // Split it to get the parts.
        int i = prefixedName.indexOf(':') ;
        if ( i < 0 )
            throw new ARQInternalErrorException("Broken short form -- "+prefixedName) ;
        String p = prefixedName.substring(0,i) ;
        String x = prefixedName.substring(i+1) ; 
        // Check legality
        if ( checkValidPrefix(p) && checkValidLocalname(x) )
            return true ;
        return false ;
    }
    
    private static boolean checkValidPrefix(String prefixStr)
    {
        if ( prefixStr.startsWith("_"))
            // Should .equals?? 
            return false ;
        return checkValidLocalname(prefixStr) ;
    }
    
    private static boolean checkValidLocalname(String localname)
    {
        if ( localname.length() == 0 )
            return true ;
        
        for ( int idx = 0 ; idx < localname.length() ; idx++ )
        {
            char ch = localname.charAt(idx) ;
            if ( ! validPNameChar(ch) )
                return false ;
        }
        
        // Test start and end - at least one character in the name.
        
        if ( localname.endsWith(".") )
            return false ;
        if ( localname.startsWith(".") )
            return false ;
        
        return true ;
    }
    
    private static boolean validPNameChar(char ch)
    {
        if ( Character.isLetterOrDigit(ch) ) return true ;
        if ( ch == '.' )    return true ;
        if ( ch == '-' )    return true ;
        if ( ch == '_' )    return true ;
        return false ;
    }
    
    static boolean applyUnicodeEscapes = false ;
    
    // Take unescape code from ParserBase.
    
    // take a string and make it safe for writing.
    public static String stringEsc(String s)
    { return stringEsc( s, true ) ; }
    
    public static String stringEsc(String s, boolean singleLineString)
    {
        StringBuilder sb = new StringBuilder() ;
        stringEsc(sb, s, singleLineString) ;
        return sb.toString() ;
    }
    
    public static void stringEsc(StringBuilder sbuff, String s)
    { stringEsc( sbuff,  s, true ) ; }

    public static void stringEsc(StringBuilder sbuff, String s, boolean singleLineString)
    {
        int len = s.length() ;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);

            // Escape escapes and quotes
            if (c == '\\' || c == '"' )
            {
                sbuff.append('\\') ;
                sbuff.append(c) ;
                continue ;
            }
            
            // Characters to literally output.
            // This would generate 7-bit safe files 
//            if (c >= 32 && c < 127)
//            {
//                sbuff.append(c) ;
//                continue;
//            }    

            // Whitespace
            if ( singleLineString && ( c == '\n' || c == '\r' || c == '\f' || c == '\t' ) )
            {
                if (c == '\n') sbuff.append("\\n");
                if (c == '\t') sbuff.append("\\t");
                if (c == '\r') sbuff.append("\\r");
                if (c == '\f') sbuff.append("\\f");
                continue ;
            }
            
            // Output as is (subject to UTF-8 encoding on output that is)
            
            if ( ! applyUnicodeEscapes )
                sbuff.append(c) ;
            else
            {
                // Unicode escapes
                // c < 32, c >= 127, not whitespace or other specials
                if ( c >= 32 && c < 127 )
                {
                    sbuff.append(c) ;
                }
                else
                {
                    String hexstr = Integer.toHexString(c).toUpperCase(Locale.ENGLISH);
                    int pad = 4 - hexstr.length();
                    sbuff.append("\\u");
                    for (; pad > 0; pad--)
                        sbuff.append("0");
                    sbuff.append(hexstr);
                }
            }
        }
    }
    
    static public void resetBNodeLabels() { bNodeMap = new NodeToLabelMapBNode("b", false) ; }
    
    private static SerializationContext newSerializationContext(PrefixMapping prefixMapping)
    {
        return new SerializationContext(prefixMapping, bNodeMap) ;
    }
    
    private static SerializationContext newSerializationContext(Prologue prologue)
    {
        return new SerializationContext(prologue, bNodeMap) ;
    }

}
