/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json;

import java.io.FileNotFoundException ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.OutputStream ;
import java.io.Reader ;
import java.io.StringReader ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.io.PeekReader ;
import org.openjena.atlas.json.io.JSONMaker ;
import org.openjena.atlas.json.io.JsonWriter ;
import org.openjena.atlas.json.io.parser.JSONParser ;
import org.openjena.atlas.json.io.parserjavacc.JSONParserJavaCC ;

/** A class that is the front door to the JSON subsystem */
public class JSON
{
    /** Parse a complete JSON object */ 
    public static JsonObject parse(InputStream input)
    {
        JSONMaker maker = new JSONMaker(); 
        _parse(input, maker) ;
        return (JsonObject)maker.jsonValue() ;
    }
    
    /** Parse a complete JSON object */ 
    public static JsonObject parse(String string)
    {
        return _parse(new StringReader(string)) ;
    }

    /** Parse any JSON value, not just an object, from an input stream */ 
    public static JsonValue parseAny(InputStream input)
    {
        JSONMaker maker = new JSONMaker(); 
        _parseAny(input, maker) ;
        return maker.jsonValue() ;
    }

    /** Parse any JSON value, not just an object, from a file */ 
    public static JsonValue parseAny(String string)
    {
        return _parseAny(new StringReader(string)) ;
    }

    /** Read a JSON object from a file */ 
    public static JsonObject read(String filename)
    {
        try
        {
            InputStream in = IO.openFileEx(filename) ;
            PeekReader r = PeekReader.makeUTF8(in) ;
            return _parse(r) ;
        }
        catch (FileNotFoundException ex)
        {
            throw new RuntimeException("File not found: "+filename, ex) ;
        }
        catch (IOException ex)
        {
            throw new RuntimeException("IOException: "+filename, ex) ;
        }
    }
    
    /** Read any JSON value, not just an object, from a file */ 
    public static JsonValue readAny(String filename)
    {
        try
        {
            InputStream in = IO.openFileEx(filename) ;
            PeekReader r = PeekReader.makeUTF8(in) ;
            return _parseAny(r) ;
        }
        catch (FileNotFoundException ex)
        {
            throw new RuntimeException("File not found: "+filename, ex) ;
        }
        catch (IOException ex)
        {
            throw new RuntimeException("IOException: "+filename, ex) ;
        }

    }
    
    // Hide the reader versions - not encouraged due to charset problems. 

    private static JsonObject _parse(Reader r)
    {
        JSONMaker maker = new JSONMaker(); 
        _parse(r, maker) ;
        return (JsonObject)maker.jsonValue() ;
    }
    
    private static JsonValue _parseAny(Reader r)
    {
        JSONMaker maker = new JSONMaker(); 
        _parseAny(r, maker) ;
        return maker.jsonValue() ;
    }
    
    // PARSER CHOICES
    // Switch on parser choice.
    private static final boolean useJavaCC = false ; 

    private static void _parse(Reader r, JSONMaker maker)
    {
        if ( useJavaCC )
            JSONParserJavaCC.parse(r, maker) ;
        else
            JSONParser.parse(r, maker) ;
    }

    private static void _parseAny(Reader r, JSONMaker maker)
    {
        if ( useJavaCC )
            JSONParserJavaCC.parseAny(r, maker) ;
        else
            JSONParser.parseAny(r, maker) ;
    }

    private static void _parse(InputStream r, JSONMaker maker)
    {
        if ( useJavaCC )
            JSONParserJavaCC.parse(r, maker) ;
        else
            JSONParser.parse(r, maker) ;
    }

    private static void _parseAny(InputStream r, JSONMaker maker)
    {
        if ( useJavaCC )
            JSONParserJavaCC.parseAny(r, maker) ;
        else
            JSONParser.parseAny(r, maker) ;
    }

    /** Write out a JSON value - pass a JSON Object to get legal exchangeable JSON */
    public static void write(OutputStream output, JsonValue jValue)
    {
        JsonWriter w = new JsonWriter(output) ;
        jValue.visit(w) ;
    }
    
    /** Write out a JSON value - pass a JSON Object to get legal exchangeable JSON */
    public static void write(IndentedWriter output, JsonValue jValue)
    {
        JsonWriter w = new JsonWriter(output) ;
        jValue.visit(w) ;
    }

    /** Write out a JSON value to - pass a JSON Object to get legal exchangeable JSON */
    public static void write(JsonValue jValue)
    {
        write(IndentedWriter.stdout, jValue) ;
        IndentedWriter.stdout.flush() ;
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