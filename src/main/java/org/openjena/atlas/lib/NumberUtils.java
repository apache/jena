/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

public class NumberUtils
{
    // Maximum length of a length 1,2,3,4...
    private final static int [] maxTable = { 
        9, 99, 999, 9999, 99999, 999999, 9999999,
        99999999, 999999999, Integer.MAX_VALUE };

    /** Fast, but basic, integer to StringBuilder */
    public static void formatInt(StringBuilder sb, int value) 
    { 
        int len = length(value) ;
        formatInt(sb, value, len, false) ;
    }
    
    /** Fast, but basic, integer to StringBuilder : always signed */
    public static void formatSignedInt(StringBuilder sb, int value) 
    { 
        int len = length(value) ;
        if ( value >= 0 )
            len++ ;
        formatInt(sb, value, len, true) ;
    }

    static int length(int x)
    {
        if ( x < 0 )
            return length(-x)+1 ;
        
        for (int i=0; ; i++)
            if (x <= maxTable[i])
                return i+1;
    }

    /** Place a fixed width representation of a non-negative int into the string buffer */ 
    public static void formatInt(StringBuilder sb, int value, int width)
    { 
        formatInt(sb, value, width, false) ;
    }
    
    /** Place a fixed width representation into the string buffer : always signed. */ 
    public static void formatSignedInt(StringBuilder sb, int value, int width) 
    { 
        formatInt(sb, value, width, true) ;
    }

    public static void formatInt(StringBuilder sb, int value, int width, boolean signAlways)
    {
        boolean negative = (value < 0 ) ;
        
        if ( negative )
        {
            value = -value ;
            width -- ;
            sb.append('-') ;
        }
        else if ( signAlways )
        {
            width -- ;
            sb.append('+') ;
        }
            
        char chars[] = new char[width] ;

        formatUnsignedInt(chars, value, width) ;
        
        // Append - the buffer was filled backwards. 
        for ( int i = 0 ; i < width ; i++ )
            // Un-backwards.
            sb.append(chars[width-1-i]) ;
    }

    // No checking.
    private static int formatUnsignedInt(char[] b, int x, int width)
    {
        // x >= 0 
        // Inserts chars backwards
        int idx = 0 ;
        while ( width > 0 )
        {
            int i = x%10 ;
            char ch = Chars.digits10[i] ;
            b[idx] = ch ;
            width-- ;
            idx++ ;

            x = x / 10 ;
            if ( x == 0 )
                break ;
        }
        
        if ( x != 0 )
            throw new AtlasException("formatInt: overflow") ;
        
        while ( width > 0 )
        {
            b[idx] = '0' ;
            idx++ ;
            width-- ;
        }
        return width ;
    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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