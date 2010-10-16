/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * 
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