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

package org.apache.jena.atlas.json.io.parserjavacc;

import java.io.InputStream ;
import java.io.Reader ;

import org.apache.jena.atlas.json.JsonParseException ;
import org.apache.jena.atlas.json.io.JSONHandler ;
import org.apache.jena.atlas.json.io.parserjavacc.javacc.JSON_Parser ;
import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException ;
import org.apache.jena.atlas.json.io.parserjavacc.javacc.TokenMgrError ;

public class JSONParserJavaCC
{    
    /** Parse to get a Json object */ 
    public static void parse(InputStream input, JSONHandler handler)
    {
        JSON_Parser p = new JSON_Parser(input) ;
        parse(p, handler) ;
    }

    /** Parse to get a Json object */ 
    public static void parse(Reader reader, JSONHandler handler)
    {
        JSON_Parser p = new JSON_Parser(reader) ;
        parse(p, handler) ;
    }
    
    private static void parse(JSON_Parser p, JSONHandler handler)
    {
        p.setHandler(handler) ;
        try
        {
            p.unit() ;
            // Checks for EOF 
            //        //<EOF> test : EOF is always token 0.
            //        if ( p.token_source.getNextToken().kind != 0 )
            //            throw new JSONParseException("Trailing characters after "+item, item.getLine(), item.getColumn()) ;
        } 
        catch (ParseException ex)
        { throw new JsonParseException(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginColumn) ; }
        catch (TokenMgrError tErr)
        { 
            // Last valid token : not the same as token error message - but this should not happen
            int col = p.token.endColumn ;
            int line = p.token.endLine ;
            throw new JsonParseException(tErr.getMessage(), line, col) ;
        }
    }

    /** Parse to get a Json object */ 
    public static void parseAny(InputStream input, JSONHandler handler)
    { 
        JSON_Parser p = new JSON_Parser(input) ;
        parseAny(p, handler) ;
    }

    /** Parse to get a Json object */ 
    public static void parseAny(Reader reader, JSONHandler handler)
    {
        JSON_Parser p = new JSON_Parser(reader) ;
        parseAny(p, handler) ;
    }
        
    private static void parseAny(JSON_Parser p, JSONHandler handler)
    {
        p.setHandler(handler) ;
        try
        {
            p.any() ;
        } 
        catch (ParseException ex)
        { throw new JsonParseException(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginColumn) ; }
        catch (TokenMgrError tErr)
        { 
            // Last valid token : not the same as token error message - but this should not happen
            int col = p.token.endColumn ;
            int line = p.token.endLine ;
            throw new JsonParseException(tErr.getMessage(), line, col) ;
        }
    }

}
