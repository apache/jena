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

import java.io.UnsupportedEncodingException ;
import java.net.URLDecoder ;
import java.net.URLEncoder ;

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
