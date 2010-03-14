/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json.io;

import java.io.OutputStream ;
import java.util.Set ;
import java.util.SortedSet ;
import java.util.TreeSet ;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.json.* ;
import org.openjena.atlas.lib.BitsInt ;


import com.hp.hpl.jena.riot.RiotChars ;

public class JsonWriter implements JsonVisitor
{
    // Legal in Javascript but not strict JSON.
    static boolean writeJavaScript = false ;
    
    IndentedWriter out ;
    
    public JsonWriter() { this(IndentedWriter.stdout) ; }
    public JsonWriter(OutputStream ps) { this(new IndentedWriter(ps)) ; }
    public JsonWriter(IndentedWriter ps) { out = ps ; }
    
    public void startOutput()   {  }
    public void finishOutput()  {  out.flush()  ; }
    
    //@Override
    public void visit(JsonObject jsonObject)
    { 
        out.print("{ ") ;
        out.incIndent() ;
        boolean first = true ; 
        boolean multiLine = false ;
        
        // Sort keys.
        Set<String> x = jsonObject.keySet() ;
        SortedSet<String> y = new TreeSet<String>(x) ;
        
        for ( String k : y )
        {
            if ( ! first )
            {
                out.print(",") ;
                out.println() ;
                multiLine = true ; 
            }
            first =  false ;
            outputQuotedString(k, true) ;
            out.print(" : ") ;
            out.incIndent() ;
            jsonObject.get(k).visit(this) ;
            out.decIndent() ;
        }
        out.decIndent() ;
        if ( multiLine )
            out.ensureStartOfLine() ;
        else
            out.print(" ") ;
        out.print("} ") ;
        
    }

    /* \"  \\ \/ \b \f \n \r \t
     * control characters (def?) 
     * \ u four-hex-digits (if
     *  you don't know why the comment writes "\ u", 
     *  and not without space then ... */
    private void outputQuotedString(String string,  boolean allowBareWords)
    { 
        char quoteChar = RiotChars.CH_QUOTE2 ;
        int len = string.length() ;
        
        if ( writeJavaScript )
        {
            boolean safeBareWord = true ;
            if ( len != 0 )
                safeBareWord = isA2Z(string.charAt(0)) ;

            if ( safeBareWord )
            {
                for (int i = 1; i < len; i++)
                {
                    char ch = string.charAt(i);
                    if ( isA2ZN(ch) ) continue ;
                    safeBareWord = false ;
                    break ;
                }
            }
            if ( safeBareWord )
            {
                // It's safe as a bare word in JavaScript.
                out.print(string) ;
                return ;
            }
        }

        if ( writeJavaScript )
            quoteChar = RiotChars.CH_QUOTE1 ;
        
        out.print(quoteChar) ;
        for (int i = 0; i < len; i++)
        {
            char ch = string.charAt(i);
            if ( ch == quoteChar )
            {
                esc(out, quoteChar) ;
                continue ;
            }
            
            switch (ch)
            {
                case '"':   esc(out, '"') ; break ;
                case '\'':   esc(out, '\'') ; break ;
                case '\\':  esc(out, '\\') ; break ;
                case '/':
                    // Avoid </ which confuses if it's in HTML (this is from json.org)
                    if ( i > 0 && string.charAt(i-1) == '<' )
                        esc(out, '/') ;
                    else
                        out.print(ch) ;
                    break ;
                case '\b':  esc(out, 'b') ; break ;
                case '\f':  esc(out, 'f') ; break ;
                case '\n':  esc(out, 'n') ; break ;
                case '\r':  esc(out, 'r') ; break ;
                case '\t':  esc(out, 't') ; break ;
                default:
                    
                    //Character.isISOControl(ch) ; //00-1F, 7F-9F
                    // This is more than Character.isISOControl
                    
                    if (ch < ' ' || 
                        (ch >= '\u007F' && ch <= '\u009F') ||
                        (ch >= '\u2000' && ch < '\u2100'))
                    {
                        out.print("\\u") ;
                        int x = ch ;
                        x = oneHex(out, x, 3) ;
                        x = oneHex(out, x, 2) ;
                        x = oneHex(out, x, 1) ;
                        x = oneHex(out, x, 0) ;
                        break ;
                    }
                        
                    out.print(ch) ;
                    break ;
            }
        }
        out.print(quoteChar) ;
    }
    
    
    private boolean isA2Z(int ch)
    {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z') ;
    }

    private boolean isA2ZN(int ch)
    {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z') || range(ch, '0', '9') ;
    }

    private boolean isNumeric(int ch)
    {
        return range(ch, '0', '9') ;
    }
    
    private static boolean isWhitespace(int ch)
    {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n' || ch == '\f' ;    
    }
    
    private static boolean isNewlineChar(int ch)
    {
        return ch == '\r' || ch == '\n' ;
    }
    
    private static boolean range(int ch, char a, char b)
    {
        return ( ch >= a && ch <= b ) ;
    }
    
    private static void esc(IndentedWriter out, char ch)
    {
        out.print('\\') ; out.print(ch) ; 
    }
    
    private static int oneHex(IndentedWriter out, int x, int i)
    {
        int y = BitsInt.unpack(x, 4*i, 4*i+4) ;
        char charHex = org.openjena.atlas.lib.Chars.hexDigits[y] ;
        out.print(charHex) ; 
        return BitsInt.clear(x, 4*i, 4*i+4) ;
    }
    
    //@Override
    public void visit(JsonArray jsonArray)
    {
        boolean multiLine = (jsonArray.size() > 1 ) ;
        if ( multiLine )
            out.ensureStartOfLine() ;

        out.print("[ ") ;
        out.incIndent() ;
        boolean first = true ; 

        for ( JsonValue elt : jsonArray )
        {
            if ( ! first )
            {
                out.print(",") ;
                out.println() ;
                multiLine = true ; 
            }
            first = false ;
            elt.visit(this) ;
        }
        out.decIndent() ;
        if ( multiLine )
            out.ensureStartOfLine() ;
        else
            out.print(" ") ;
        out.print("] ") ;
    }

    //@Override
    public void visit(JsonString jsonString)
    {
        outputQuotedString(jsonString.value(), false) ;
    }

    //@Override
    public void visit(JsonNumber jsonNumber)
    {
        out.print(jsonNumber.value().toString()) ;
    }

    //@Override
    public void visit(JsonBoolean jsonBoolean)
    {
        String x = jsonBoolean.value() ? "true" : "false" ; 
        out.print(x) ;
    }

    //@Override
    public void visit(JsonNull jsonNull)
    { out.print("null") ; }

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