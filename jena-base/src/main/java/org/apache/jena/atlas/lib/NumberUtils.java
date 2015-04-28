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

package org.apache.jena.atlas.lib;

import org.apache.jena.atlas.AtlasException ;

public class NumberUtils
{
    // Maximum length of a length 1,2,3,4...
    private final static int [] maxTable = { 
        9, 99, 999, 9999, 99999, 999999, 9999999,
        99999999, 999999999, Integer.MAX_VALUE };

    /** Fast, but basic, integer to StringBuilder */
    public static void formatInt(StringBuilder sb, int value) 
    { 
        // SeeAlso Integer.toString.
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

    /** Format an integer, which may be signed */
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

        formatUnsignedInt(sb, value, width) ;
    }

    /** Place a fixed width representation into the string buffer : never signed. */ 
    public static void formatUnsignedInt(StringBuilder sb, int value, int width) 
    { 
        char chars[] = new char[width] ;
        formatUnsignedInt$(chars, value, width) ;
        
        // Append - the buffer was filled backwards. 
        for ( int i = 0 ; i < width ; i++ )
            // Un-backwards.
            sb.append(chars[width-1-i]) ;
        
    }

    // No checking.  char[] filled backwards
    private static int formatUnsignedInt$(char[] b, int x, int width)
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
            throw new AtlasException("formatInt: overflow[x="+x+", width="+width+"]") ;
        
        while ( width > 0 )
        {
            b[idx] = '0' ;
            idx++ ;
            width-- ;
        }
        return width ;
    }

}
