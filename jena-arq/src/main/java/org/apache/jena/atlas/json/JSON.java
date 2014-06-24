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

package org.apache.jena.atlas.json;

import java.io.* ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.json.io.JSONMaker ;
import org.apache.jena.atlas.json.io.JsonWriter ;
import org.apache.jena.atlas.json.io.parser.JSONParser ;
import org.apache.jena.atlas.json.io.parserjavacc.JSONParserJavaCC ;

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
            try (InputStream in = IO.openFileEx( filename ))
            {
                return JSON.parse( in );
            }
        }
        catch (FileNotFoundException ex)
        {
            throw new RuntimeException("File not found: "+filename, ex) ;
        }
        catch (IOException ex)
        {
            IO.exception("IOException: "+filename, ex);
            return null ;
        }
    }
    
    /** Read any JSON value, not just an object, from a file */ 
    public static JsonValue readAny(String filename)
    {
        try
        {
            try (InputStream in = IO.openFileEx( filename ))
            {
                return JSON.parseAny( in );
            }
        }
        catch (FileNotFoundException ex)
        {
            throw new RuntimeException("File not found: "+filename, ex) ;
        }
        catch (IOException ex)
        {
            IO.exception("IOException: "+filename, ex);
            return null ;
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
        IndentedWriter iOut = new IndentedWriter(output) ;
        write(iOut, jValue) ;
        iOut.flush() ;
    }
    
    /** Write out a JSON value - pass a JSON Object to get legal exchangeable JSON */
    public static void write(IndentedWriter output, JsonValue jValue)
    {
        JsonWriter w = new JsonWriter(output) ;
        w.startOutput() ;
        jValue.visit(w) ;
        w.finishOutput() ;
    }

    /** Write out a JSON value to - pass a JSON Object to get legal exchangeable JSON */
    public static void write(JsonValue jValue)
    {
        write(IndentedWriter.stdout, jValue) ;
    }
}
