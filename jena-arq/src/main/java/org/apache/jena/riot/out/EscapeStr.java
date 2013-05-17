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

package org.apache.jena.riot.out;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.AWriter ;

import com.hp.hpl.jena.sparql.lang.ParserBase ;

public class EscapeStr
{
    // Tests: TestOutput
    // See also OutputLangUtils.outputEsc.
    private final boolean ascii ;

    public EscapeStr(CharSpace charSpace) { this.ascii = ( charSpace == CharSpace.ASCII ) ; } 

    public void writeURI(AWriter w, String s)
    {
        if ( ascii )
            stringEsc(w, s, true, ascii) ;
        else
            // It's a URI - assume legal.
            w.print(s) ;
    }

    public void writeStr(AWriter w, String s) 
    {
        stringEsc(w, s, true, ascii) ;
    }

    public void writeStrMultiLine(AWriter w, String s) 
    {
        // N-Triples does not have """
        stringEsc(w, s, false, ascii) ;
    }

    // Utility
    /*
     * Escape characters in a string according to Turtle rules. 
     */
    public static String stringEsc(String s)
    { return stringEsc(s, true, false) ; }

    private static String stringEsc(String s, boolean singleLineString, boolean asciiOnly)
    {
        IndentedLineBuffer sb = new IndentedLineBuffer() ;
        stringEsc(sb, s, singleLineString, asciiOnly) ;
        return sb.toString() ;
    }

    public static void stringEsc(AWriter out, String s, boolean singleLineString, boolean asciiOnly)
    {
        int len = s.length() ;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);

            // \\ Escape always possible.
            if (c == '\\') 
            {
                out.print('\\') ;
                out.print(c) ;
                continue ;
            }
            if ( singleLineString )
            {
                if ( c == '"' )         { out.print("\\\""); continue ; }
                else if (c == '\n')     { out.print("\\n");  continue ; }
                else if (c == '\t')     { out.print("\\t");  continue ; }
                else if (c == '\r')     { out.print("\\r");  continue ; }
                else if (c == '\f')     { out.print("\\f");  continue ; }
            }
            // Not \-style esacpe. 
            if ( c >= 32 && c < 127 )
                out.print(c);
            else if ( !asciiOnly )
                out.print(c);
            else
            {
                // Outside the charset range.
                // Does not cover beyond 16 bits codepoints directly
                // (i.e. \U escapes) but Java keeps these as surrogate
                // pairs and will print as characters
                out.print( "\\u") ;
                OutputUtils.printHex(out, c, 4) ;
            }
        }
    }

    // Utilities to remove escapes

    public static String unescapeStr(String s)
    { return unescape(s, '\\') ; }
    
    // Worker function
    public static String unescape(String s, char escape)
    {
        return ParserBase.unescape(s, escape, false,  -1, -1) ;
        
    }
}
