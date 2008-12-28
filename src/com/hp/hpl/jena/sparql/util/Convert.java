/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */
 
package com.hp.hpl.jena.sparql.util;

import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException ;

/**
 * @author      Andy Seaborne
 */
public class Convert
{
    // UTF-8 is required in Java implementations
 
    // Choose the characters to escape.

    // Chars from RDF 2396 to escape: 
    // 2.2. Reserved Characters
    // 2.4.3 Excluded US-ASCII Characters
    //
    // reserved    = ";" | "/" | "?" | ":" | "@" | "&" | "=" | "+" |
    //               "$" | ","
    //
    // control     = <US-ASCII coded characters 00-1F and 7F hexadecimal>
    // space       = <US-ASCII coded character 20 hexadecimal>
    //
    // delims      = "<" | ">" | "#" | "%" | <">
    // unwise      = "{" | "}" | "|" | "\" | "^" | "[" | "]" | "`"
    //
    // These should be OK:
    //
    // unreserved  = alphanum | mark
    // mark        = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
   
    // URLEncoder is savage:
    
    // + The alphanumeric characters "a" through "z",
    //   "A" through "Z" and "0" through "9" remain the same.
    // + The special characters ".", "-", "*", and "_" remain the same. 
    // + The space character " " is converted into a plus sign "+".
    // + All other characters are unsafe and are first converted into
    //   one or more bytes using some encoding scheme. 
    
    public static String encWWWForm(String s)
    { 
        try {
            return URLEncoder.encode(s, "UTF-8") ;
            // Can't fail - UTF-8 is required
        } catch (UnsupportedEncodingException ex) { return null ;}
    }

    public static String encWWWForm(StringBuffer sbuff)
    {
        return encWWWForm(sbuff.toString()) ;
    }
    
    
    public static String decWWWForm(String s)
    {
        try {
            return URLDecoder.decode(s, "UTF-8") ;
            // Can't fail - UTF-8 is required
        } catch (UnsupportedEncodingException ex) { return null ;}
    }

    public static String decWWWForm(StringBuffer sbuff)
    {
        return decWWWForm(sbuff.toString()) ;
    }
    
}

/*
 *  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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

