/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json.io.parser;

import java.io.InputStream ;
import java.io.Reader ;

import org.openjena.atlas.io.PeekReader ;
import org.openjena.atlas.json.io.JSONHandler ;
import org.openjena.atlas.json.io.parserjavacc.JSONPrinter ;

/** JSON parser */
public class JSONParser
{    
    /** Parse from an input stream to get a Json object */ 
    public static void parse(InputStream input)
    { parse(input, new JSONPrinter()) ; }

    /** Parse to get a Json object */ 
    public static void parse(InputStream input, JSONHandler handler)
    {
        PeekReader r = PeekReader.makeUTF8(input) ;
        TokenizerJSON t = new TokenizerJSON(r) ;
        parse(t, handler) ;
    }

    /** Parse from a reader to get a Json object */ 
    public static void parse(Reader reader)
    { parse(reader, new JSONPrinter()) ; }
    
    /** Parse to get a Json object */ 
    public static void parse(Reader reader, JSONHandler handler)
    {
        PeekReader r = PeekReader.make(reader) ;
        TokenizerJSON t = new TokenizerJSON(r) ;
        parse(t, handler) ;
    }
    
    private static void parse(TokenizerJSON t, JSONHandler handler)
    {
        JSONP p = new JSONP(t , handler) ;
        p.parse() ;
    }

    /** Parse from a reader to get an Json value */ 
    public static void parseAny(Reader reader)
    { parseAny(reader, new JSONPrinter()) ; }

    /** Parse to get a Json primitive */ 
    public static void parseAny(Reader reader, JSONHandler handler)
    {
        PeekReader r = PeekReader.make(reader) ;
        TokenizerJSON t = new TokenizerJSON(r) ;
        parseAny(t, handler) ;
    }
        
    /** Parse from a reader to get an Json value */ 
    public static void parseAny(InputStream input)
    { parseAny(input, new JSONPrinter()) ; }

    /** Parse to get a Json primitive */ 
    public static void parseAny(InputStream input, JSONHandler handler)
    {
        PeekReader r = PeekReader.makeUTF8(input) ;
        TokenizerJSON t = new TokenizerJSON(r) ;
        parseAny(t, handler) ;
    }
        
    private static void parseAny(TokenizerJSON t, JSONHandler handler)
    {
        JSONP p = new JSONP(t , handler) ;
        p.parseAny() ;
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
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