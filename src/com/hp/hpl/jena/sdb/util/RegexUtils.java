/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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
    {
        StringCharacterIterator iter = new StringCharacterIterator(string, fromIndex) ;
        
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
    
    public static boolean isStartsWith(String pattern)
    {
        return pattern.charAt(0) == '^' && noMetaChars(pattern, 1) ; 
    }
    
    public static boolean isEndsWith(String pattern)
    {
        return pattern.charAt(0) == '^' && noMetaChars(pattern, 1) ; 
    }

}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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