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

package com.hp.hpl.jena.sdb.util;

import java.text.StringCharacterIterator;

public class RegexUtils
{
    // ^$ - end markers
    // *?+ - modifiers
    // () [] enclosinging marks
    // \ escape

    static final char[] metaChars = {
        '^' , '$' , '.' ,
        '*' , '?' , '+' ,
        '(', ')', '[', ']',
        '\\'
    } ;
    
    public static boolean noMetaChars(String string, int fromIndex)
    { return noMetaChars(string, fromIndex, string.length()) ; }
    
    /** check for any regular expression metacharacters between the index point (inclusive,exclusive) */ 
    public static boolean noMetaChars(String string, int fromIndex, int endIndex)
    {
        StringCharacterIterator iter = new StringCharacterIterator(string, fromIndex, endIndex, fromIndex) ;
        
        char ch ;
        while ( ( ch = iter.next() ) != StringCharacterIterator.DONE )
        {
            for ( char mc : metaChars )
            {
                if ( mc == ch )
                    return false ; 
            }
        }
        return true ;
    }
    
    public static boolean isSimpleStartsWith(String pattern)
    {
        if ( pattern.length() < 1 )
            return false ;
        return pattern.charAt(0) == '^' && noMetaChars(pattern, 1) ;
    }

    public static boolean isSimpleAnchored(String pattern)
    {
        if ( pattern.length() < 2 )
            return false ;
        
        return pattern.charAt(0) == '^' && 
               pattern.charAt(pattern.length()-1) == '$' && 
               noMetaChars(pattern, 1, pattern.length()-1) ;
    }

    
    public static boolean isSimpleEndsWith(String pattern)
    {
        if ( pattern.length() < 1 )
            return false ;

        return pattern.charAt(pattern.length()-1) == '$' && noMetaChars(pattern, 0, pattern.length()-1) ; 
    }
    
    public static String regexToLike(String pattern)
    {
        // cases:
        // Not covered:
        // Regex "." is replaceable by "_" if no other metacharcaters.

        // Covered;
        // ^$, ^, $, none
        
        // ^...$
        if ( isSimpleAnchored(pattern) )
            return pattern.substring(1,pattern.length()-1) ;
        // ^...
        if ( isSimpleStartsWith(pattern) )
            return pattern.substring(1)+"%" ;
        // ...$
        if ( isSimpleEndsWith(pattern) )
            return "%"+pattern.substring(0,pattern.length()-1) ;
        
        if ( noMetaChars(pattern, 0) )
            return "%"+pattern+"%" ;
            
        return null ;
    }
}
