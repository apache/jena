/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;

import java.io.* ;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdql.parser.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author   Andy Seaborne
 * @version  $Id: QueryPrintUtils.java,v 1.7 2005-02-21 12:15:24 andy_seaborne Exp $
 */

public class QueryPrintUtils
{
    // Helper operations
    static final String indentPrefix = "  " ;
    public static boolean multiLineExpr = false ;
    public static boolean printName = true ;
    static int bNodeCounter = 0 ;
    static Map bNodeStrings = new HashMap() ; 
    


    public static void print(PrintWriter pw, Expr expr)
    {
        //expr.print(pw, 0) ;
        pw.println(expr.asPrefixString());
    }

    public static String asInfixString1(Expr expr, String opName, String symbol)
    {
        StringBuffer sb = new StringBuffer() ;
        sb.append("(") ;
        sb.append(symbol) ;
        sb.append(" ") ;
        sb.append(expr.asInfixString()) ;
        sb.append(")");
        return sb.toString() ;
    }

    public static String asInfixString2(Expr left, Expr right, String opName, String symbol)
    {
        StringBuffer sb = new StringBuffer() ;
        sb.append("( ") ;
        sb.append(left.asInfixString()) ;
        sb.append(" ") ;
        sb.append( symbol ) ;
        sb.append(" ") ;
        sb.append(right.asInfixString()) ;
        sb.append(" )");
        return sb.toString() ;
    }

    public static String asPrefixString(Expr left, Expr right, String opName, String symbol)
    {
        StringBuffer sb = new StringBuffer() ;

        sb.append("(") ;
        sb.append( printName?opName:symbol ) ;
        sb.append(" ") ;
        sb.append(left.asPrefixString()) ;
        if ( right != null )
        {
            sb.append(" ") ;
            sb.append(right.asPrefixString()) ;
        }
        sb.append(")");
        return sb.toString() ;
    }

    // Prints prefix notation, multiline.
    public static void print(PrintWriter pw, Expr left, Expr right, String opName, String symbol, int level)
    {
        indent(pw, level) ;
        pw.println("("+(printName?opName:symbol)) ;
        left.print(pw, level+1);
        if ( right != null )
            right.print(pw, level+1);
        indent(pw, level) ;
        pw.println(")") ;
    }

    public static void indent(PrintWriter pw, int level)
    {
        for ( int i = 0 ; i < level ; i++ )
          pw.print(indentPrefix);
    }
    // Formatting various items
    
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
        if ( obj == null )
            return "<<null>>" ;
        
        if ( obj instanceof Literal )
            return stringForLiteral((Literal)obj)  ;
        
        if ( obj instanceof Resource )
            return stringForResource((Resource)obj) ;
        
        return obj.toString() ;
    }
    
    public static String stringForLiteral(Literal literal)
    {
        return stringForLiteralLabel(literal.asNode().getLiteral()) ;
    }
        
    public static String stringForLiteralLabel(LiteralLabel literal)
    {
        String datatype = literal.getDatatypeURI() ;
        String lang = literal.language() ;
        String s = literal.getLexicalForm() ;
        
        if ( datatype != null )
        {
            // Special form we know how to handle?
            // Assume valid text
            if ( datatype.equals(XSD.integer.getURI()) )
            {
                try {
                    new java.math.BigInteger(s) ;
                    return s ;
                } catch (NumberFormatException nfe) {}
                // No luck.  Continue.
                // Continuing is always safe.
            }
            
            if ( datatype.equals(XSD.xdouble.getURI()) )
            {
                // Must have an '.' or 'e' or 'E'
                if ( s.indexOf('.') >= 0 || 
                s.indexOf('e') >= 0 ||
                s.indexOf('E') >= 0 )
                {
                    try {
                        Double.parseDouble(s) ;
                        return s ;
                    } catch (NumberFormatException nfe) {}
                    // No luck.  Continue.
                }
            }
        }
        
        // Format the text - no escaping.
        StringBuffer sbuff = new StringBuffer() ;
        sbuff.append("\"") ;
        sbuff.append(s) ;
        sbuff.append("\"") ;
        
        // Format the language tag 
        if ( lang != null && lang.length()>0)
        {
            sbuff.append("@") ;
            sbuff.append(lang) ;
        }
        
        return sbuff.toString() ;
    }
    
    
    public static String stringForResource(Resource r)
    {
        return stringForNode(r.getNode(), r.getModel()) ;
//        if ( r.isAnon() )
//        {
//            AnonId a = r.getId() ;
//            if ( ! bNodeStrings.containsKey(a) )
//                bNodeStrings.put(a, "_:b"+(bNodeCounter++)) ;
//            //return "anon:"+r.getId() ;
//            return (String)bNodeStrings.get(a) ;
//        }
//        else
//        {
//            String u = r.getURI() ;
//            String tmp = r.getModel().shortForm(u) ;
//            if ( u.equals(tmp) )
//                return "<"+u+">" ;
//            return tmp ;
//        }
//        
    }
    
    public static String stringForNode(Node n) { return stringForNode(n, null) ; }
    
    public static String stringForNode(Node n, PrefixMapping mapping)
    {
        if ( n == null )
            return "<<null>>" ;
        
        if ( n.isBlank() )
        {
            AnonId a = n.getBlankNodeId() ;
            if ( ! bNodeStrings.containsKey(a) )
                bNodeStrings.put(a, "_:b"+(bNodeCounter++)) ;
            //return "anon:"+r.getId() ;
            return (String)bNodeStrings.get(a) ;
        }
        if ( n.isLiteral() )
        {
            LiteralLabel ll = n.getLiteral() ;
            return stringForLiteralLabel(ll) ;
        }
        if ( n.isURI() )
        {
            String uri = n.getURI() ;
            if ( mapping != null )
            {
                String tmp = mapping.shortForm(n.getURI()) ;
                if ( tmp != null && !tmp.equals(uri) )
                    return tmp ;
            }
            
            return "<"+uri+">" ; 
        }
        if ( n.isVariable() )
            return "?"+n.getName() ;

        return n.toString() ;
    }

}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
