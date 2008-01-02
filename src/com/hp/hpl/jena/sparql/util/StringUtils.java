/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.expr.Expr;


public class StringUtils
{
    /** Join an array of strings */
    public static String join(String sep, String[]a)
    {
        if ( a.length == 0 )
            return "" ;
        
        if ( a.length == 1)
            return a[0] ;

        StringBuffer sbuff = new StringBuffer() ;
        sbuff.append(a[0]) ;
        
        for ( int i = 1 ; i < a.length ; i++ )
        {
            if ( sep != null )
                sbuff.append(sep) ;
            sbuff.append(a[i]) ;
        }
        return sbuff.toString() ;
    }
    
    /** Join a list of strings */
    public static String join(String sep, List a)
    {
        return join(sep, (String[])a.toArray(new String[0])) ;
    }
    
    public static int strCompare(String s1, String s2)
    {
        // Value is the difference of the first differing chars
        int x = s1.compareTo(s2) ;
        if ( x < 0 ) return Expr.CMP_LESS ;
        if ( x > 0 ) return Expr.CMP_GREATER ;
        if ( x == 0 ) return Expr.CMP_EQUAL ;
        throw new ARQInternalErrorException("String comparison failure") ;
    }
    
    public static int strCompareIgnoreCase(String s1, String s2)
    {
        // Value is the difference of the first differing chars
        int x = s1.compareToIgnoreCase(s2) ;
        if ( x < 0 ) return Expr.CMP_LESS ;
        if ( x > 0 ) return Expr.CMP_GREATER ;
        if ( x == 0 ) return Expr.CMP_EQUAL ;
        throw new ARQInternalErrorException("String comparison failure") ;
    }

    public static byte[] asUTF8bytes(String s)
    {
        try { return s.getBytes("UTF-8") ; }
        catch (UnsupportedEncodingException ex)
        { throw new ARQInternalErrorException("UTF-8 not supported!") ; } 
    }
    
    // See FmtUtils.toString()
//    public static String toString(Printable f)
//    { 
//        IndentedLineBuffer buff = new IndentedLineBuffer() ;
//        IndentedWriter out = buff.getIndentedWriter() ;
//        f.output(out) ;
//        out.flush();
//        return buff.toString() ;
//    }
    
    public static String str(Object x)
    {
        if ( x == null ) return "<null>" ;
        return x.toString() ;
    }

    /** Does one string contain another string?
     * @param str1
     * @param str2
     * @return true if str1 contains str2
     */
    public static boolean contains(String str1, String str2)
    {
        // java 1.5 String.contains
        return str1.indexOf(str2) >= 0 ;
    }
    
//    public static String replace(String string, String target, String replacement)
//    {
//        // Java 1.5: str.replace(target, replacment)
//        return 
//    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
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