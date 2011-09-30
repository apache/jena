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

package org.openjena.riot.out;

import java.io.IOException ;
import java.io.StringWriter ;
import java.io.Writer ;

import com.hp.hpl.jena.sparql.lang.ParserBase ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.io.OutputUtils ;

public class EscapeStr
{
    // Tests: TestOutput
    // See also OutputLangUtils.outputEsc.
    private final boolean ascii ;

    public EscapeStr(boolean asciiOnly) { this.ascii = asciiOnly ; }
    
    public void writeURI(Writer w, String s)
    {
        try
        {
            if ( ascii )
                stringEsc(w, s, true, ascii) ;
            else
                // It's a URI - assume legal.
                w.write(s) ;
        } catch (IOException e) { IO.exception(e) ; }
    }
    
    public void writeStr(Writer w, String s) 
    {
        try
        {
            stringEsc(w, s, true, ascii) ;
        } catch (IOException e) { IO.exception(e) ; }
    }
    
    public void writeStrMultiLine(Writer w, String s) 
    {
        // N-Triples does not have """
        try
        {
            stringEsc(w, s, false, ascii) ;
        } catch (IOException e) { IO.exception(e) ; }
    }
    
    // Utility
    /*
     * Escape characters in a string according to Turtle rules. 
     */
    public static String stringEsc(String s)
    { return stringEsc(s, true, false) ; }
    
    private static String stringEsc(String s, boolean singleLineString, boolean asciiOnly)
    {
        try
        {
            Writer sb = new StringWriter() ;
            stringEsc(sb, s, singleLineString, asciiOnly) ;
            return sb.toString() ;
        } catch (IOException e) { IO.exception(e) ; return null ; }
    }
    
    public static void stringEsc(Writer out, String s, boolean singleLineString, boolean asciiOnly) throws IOException
    {
        int len = s.length() ;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            
            // \\ Escape always possible.
            if (c == '\\') 
            {
                out.write('\\') ;
                out.write(c) ;
                continue ;
            }
            if ( singleLineString )
            {
                if ( c == '"' )         { out.write("\\\""); continue ; }
                else if (c == '\n')     { out.write("\\n");  continue ; }
                else if (c == '\t')     { out.write("\\t");  continue ; }
                else if (c == '\r')     { out.write("\\r");  continue ; }
                else if (c == '\f')     { out.write("\\f");  continue ; }
            }
            // Not \-style esacpe. 
            if ( c >= 32 && c < 127 )
                out.write(c);
            else if ( !asciiOnly )
                out.write(c);
            else
            {
                // Outside the charset range.
                // Does not cover beyond 16 bits codepoints directly
                // (i.e. \U escapes) but Java keeps these as surrogate
                // pairs and will print as characters
                out.write( "\\u") ;
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
