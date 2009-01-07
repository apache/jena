/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang.rdql;

import java.io.PrintWriter;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/**
 * @author   Andy Seaborne
 */

public class RDQLQueryPrintUtils
{
    static final String indentPrefix = "  " ;
    public static boolean multiLineExpr = false ;
    public static boolean printOpName = true ;
    
    static int bNodeCounter = 0 ;
    
    static PrefixMapping prefixes = PrefixMapping.Factory.create()
                                    .setNsPrefixes( PrefixMapping.Standard )
                                    .setNsPrefix( "xsd" , XSDDatatype.XSD+"#" ) ;
    
    // Formatting various items
    
    public static void print(PrintWriter pw, ExprRDQL expr)
    {
        //expr.print(pw, 0) ;
        pw.println(expr.asPrefixString());
    }
    
    public static String asInfixString1(ExprRDQL expr, String opName, String symbol)
    {
        StringBuffer sb = new StringBuffer() ;
        sb.append("(") ;
        sb.append(symbol) ;
        sb.append(" ") ;
        sb.append(expr.asInfixString()) ;
        sb.append(")");
        return sb.toString() ;
    }
    
    public static String asInfixString2(ExprRDQL left, ExprRDQL right, String opName, String symbol)
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
    
    public static String asPrefixString(ExprRDQL left, ExprRDQL right, String opName, String symbol)
    {
        StringBuffer sb = new StringBuffer() ;
        
        sb.append("(") ;
        sb.append( printOpName?opName:symbol ) ;
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
    
    static final int indent = 2 ;

    // Support function for formatted output (OLD parser)
    public static void format(IndentedWriter w, ExprRDQL left, ExprRDQL right, String opName, String symbol)
    {
        w.print("( ") ;
        if ( right == null )
        {
            // unary
            w.print(symbol) ;
            left.format(w) ;
        }
        else
        {
            // Binary
            left.format(w) ;
            w.print(" ") ;
            w.print(symbol) ;
            w.print(" ") ;
            right.format(w) ;
        }
        w.print(" )") ;
    }
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
