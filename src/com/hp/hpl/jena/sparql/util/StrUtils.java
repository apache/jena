/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class StrUtils
{
    // Merge with StringUtils.
    private StrUtils() {}
    
    /** strjoin with a newline as the separator */
    public static String strjoinNL(String... args)
    {
        return join("\n", args) ;
    }
    
    /** strjoin with a newline as the separator */
    public static String strjoinNL(List<String> args)
    {
        return join("\n", args) ;
    }
    
    /** Concatentate string, using a separator */
    public static String strjoin(String sep, String... args)
    {
        return join(sep, args) ;
    }
    
    /** Concatentate string, using a separator */
    public static String strjoin(String sep, List<String> args)
    {
        return join(sep, args) ;
    }
    
    
    
    /** Join an array of strings */
    private static String join(String sep, String... a)
    {
        if ( a.length == 0 )
            return "" ;
        
        if ( a.length == 1)
            return a[0] ;

        StringBuilder sbuff = new StringBuilder() ;
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
    private static String join(String sep, List<String> a)
    {
        return join(sep, a.toArray(new String[0])) ;
    }
    
    public static final int CMP_GREATER  = +1 ;
    public static final int CMP_EQUAL    =  0 ;
    public static final int CMP_LESS     = -1 ;
    
    public static final int CMP_UNEQUAL  = -9 ;
    public static final int CMP_INDETERMINATE  = 2 ;
    
    public static int strCompare(String s1, String s2)
    {
        // Value is the difference of the first differing chars
        int x = s1.compareTo(s2) ;
        if ( x < 0 ) return CMP_LESS ;
        if ( x > 0 ) return CMP_GREATER ;
        if ( x == 0 ) return CMP_EQUAL ;
        throw new InternalError("String comparison failure") ;
    }
    
    public static int strCompareIgnoreCase(String s1, String s2)
    {
        // Value is the difference of the first differing chars
        int x = s1.compareToIgnoreCase(s2) ;
        if ( x < 0 ) return CMP_LESS ;
        if ( x > 0 ) return CMP_GREATER ;
        if ( x == 0 ) return CMP_EQUAL ;
        throw new InternalError("String comparison failure") ;
    }

    public static byte[] asUTF8bytes(String s)
    {
        try { return s.getBytes("UTF-8") ; }
        catch (UnsupportedEncodingException ex)
        { throw new InternalError("UTF-8 not supported!") ; } 
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
    public final static boolean contains(String str1, String str2)
    {
        return str1.contains(str2) ;
    }
    
    public final static String replace(String string, String target, String replacement)
    {
        return string.replace(target, replacement) ;
    }
    
    public static String substitute(String str, Map<String, String>subs)
    {
        for ( Map.Entry<String, String> e : subs.entrySet() )
        {
            String param = e.getKey() ;
            if ( str.contains(param) ) 
                str = str.replace(param, e.getValue()) ;
        }
        return str ;
    }
    
    public static String strform(Map<String, String>subs, String... args)
    {
        return substitute(strjoinNL(args),subs) ;
    }

    public static String chop(String x)
    {
        if ( x.length() == 0 )
            return x ;
        return x.substring(0, x.length()-1) ;
    }

    public static String noNewlineEnding(String x)
    {
        while ( x.endsWith("\n") || x.endsWith("\r") )
            x = StrUtils.chop(x) ;
        return x ;
    }
    
    public static List<Character> toCharList(String str)
    {
        List<Character> characters = new ArrayList<Character>(str.length()) ;
        for ( Character ch : str.toCharArray() )
            characters.add(ch) ;
        return characters ;
    }
    
    // ==== Encoding and decoding strings based on a marker character (e.g. %)
    // and then the hexadecimal representation of the character.  
    // Only characters 0-255 can be encoded.
    
    final private static char[] hexDigits = {
            '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' ,
            '9' , 'A' , 'B' , 'C' , 'D' , 'E' , 'F' 
    //         , 'g' , 'h' ,
    //        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
    //        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
    //        'u' , 'v' , 'w' , 'x' , 'y' , 'z'
            };

    /** Encode a string using hex values e.g. %20
     * 
     * @param str       String to encode
     * @param marker    Marker character
     * @param escapees  Characters to encode (must include the marker)
     * @return          Encoded string (returns input object if no change)
     */
    public static String encode(String str, char marker, char[] escapees)
    {
        // We make a first pass to see if there is anything to do.
        // This is assuming
        // (1) the string is shortish (e.g. fits in L1)
        // (2) necessary escaping is not common
        
        int N = str.length();
        int idx = 0 ;
        // Scan stage.
        outer: 
            for ( ; idx < N ; idx++ )
        {
            char ch = str.charAt(idx) ;
            for ( int j = 0 ; j < escapees.length ; j++ )
                if ( ch == escapees[j] )
                    // and idx is the first char needed to be worked on.
                    break outer ;
        }
        if ( idx == N )
            return str ;

        // At least one char to convert
        StringBuilder buff = new StringBuilder() ;
        buff.append(str, 0, idx) ;  // Insert first part.
        for ( ; idx < N ; idx++ )
        {
            char ch = str.charAt(idx) ;
            int j = 0 ; 
            for ( ; j < escapees.length ; j++ )
            {
                if ( ch == escapees[j] )
                {
                    buff.append(marker) ;
                    int lo = ch & 0xF ;
                    int hi = ch >> 4 ;
                    buff.append(hexDigits[hi]) ;                
                    buff.append(hexDigits[lo]) ; 
                    break ; // Out of escapees loop.
                }
            }
            if ( j >= escapees.length )
                buff.append(ch) ;
        }
        return buff.toString();
    }

    // Encoding is table-driven but for decode, we use code.
    static private int hexDecode(char ch) {
        if (ch >= '0' && ch <= '9' )
            return ch - '0' ;
        if ( ch >= 'A' && ch <= 'F' )
            return ch - 'A' + 10 ;
        if ( ch >= 'a' && ch <= 'f' )
            return ch - 'a' + 10 ;
        return -1 ;
    }
    
    /** Decode a string using marked hex values e.g. %20
     * 
     * @param str       String to decode
     * @param marker    The marker charcater
     * @return          Decoded string (returns input object on no change)
     */
    public static String decode(String str, char marker)
    {
        int idx = str.indexOf(marker) ;
        if ( idx == -1 )
            return str ;
        StringBuilder buff = new StringBuilder() ;
        
        buff.append(str, 0, idx) ;
        int N = str.length() ;
        
        for ( ; idx < N ; idx++ ) 
        {
            char ch = str.charAt(idx) ;
            // First time through this is true, always.
            if ( ch != marker )
                buff.append(ch) ;
            else
            {
                char hi = str.charAt(idx+1) ; 
                char lo = str.charAt(idx+2) ;   // exceptions.
                char ch2 = (char)(hexDecode(hi)<<4 | hexDecode(lo)) ;
                buff.append(ch2) ;
                idx += 2 ;
            }
        }
        return buff.toString() ; 
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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