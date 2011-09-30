/**
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

package org.openjena.riot.system;

public class RiotChars
{
    // ---- Character classes 
    
    public static boolean isAlpha(int codepoint)
    {
        return Character.isLetter(codepoint) ;
    }
    
    public static boolean isAlphaNumeric(int codepoint)
    {
        return Character.isLetterOrDigit(codepoint) ;
    }
    
    /** ASCII A-Z */
    public static boolean isA2Z(int ch)
    {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z') ;
    }

    /** ASCII A-Z or 0-9 */
    public static boolean isA2ZN(int ch)
    {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z') || range(ch, '0', '9') ;
    }

    /** ASCII 0-9 */
    public static boolean isDigit(int ch)
    {
        return range(ch, '0', '9') ;
    }
    
    public static boolean isWhitespace(int ch)
    {
        // ch = ch | 0xFF ;
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n' || ch == '\f' ;    
    }
    
    public static boolean isNewlineChar(int ch)
    {
        return ch == '\r' || ch == '\n' ;
    }

    public static int valHexChar(int ch)
    {
        if ( range(ch, '0', '9') )
            return ch-'0' ;
        if ( range(ch, 'a', 'f') )
            return ch-'a'+10 ;
        if ( range(ch, 'A', 'F') )
            return ch-'A'+10 ;
        return -1 ;
    }

    
    public static boolean range(int ch, char a, char b)
    {
        return ( ch >= a && ch <= b ) ;
    }

    public static boolean charInArray(int ch, char[] chars)
    {
        for ( int xch : chars )
        {
            if ( ch == xch )  return true ;
        }
        return false ;
    }
}
