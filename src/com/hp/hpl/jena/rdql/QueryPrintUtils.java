/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;

import java.io.* ;
import com.hp.hpl.jena.rdql.parser.* ;

/**
 * @author   Andy Seaborne
 * @version  $Id: QueryPrintUtils.java,v 1.1.1.1 2002-12-19 19:18:49 bwm Exp $
 */

public class QueryPrintUtils
{
    // Helper operations
    static final String indentPrefix = "  " ;
    public static boolean multiLineExpr = false ;
    public static boolean printName = true ;

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

}

/*
 *  (c) Copyright Hewlett-Packard Company 2001
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
 *
 * This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/).
 *
 */
