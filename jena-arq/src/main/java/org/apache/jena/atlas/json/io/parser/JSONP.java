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

package org.apache.jena.atlas.json.io.parser;

import static org.apache.jena.riot.tokens.TokenType.COLON ;
import static org.apache.jena.riot.tokens.TokenType.COMMA ;
import static org.apache.jena.riot.tokens.TokenType.KEYWORD ;
import static org.apache.jena.riot.tokens.TokenType.LBRACE ;
import static org.apache.jena.riot.tokens.TokenType.RBRACE ;
import static org.apache.jena.riot.tokens.TokenType.RBRACKET ;
import org.apache.jena.atlas.json.io.JSONHandler ;

/** Json (extended) parser
 * Extensions:
 *   Use of ', """ and ''' for strings
 *   Bare words for strings in maps (JSON objects)
 *   Hex numbers
 * Objects with duplicate keys are a warning (keeps last)
 */
public class JSONP extends JSONParserBase
{
    private JSONHandler handler ;

    public JSONP(TokenizerJSON tokens, JSONHandler handler)
    {
        super(tokens) ;
        this.handler = handler ;
    }
    
    public void parse()
    {
        if ( ! peekToken().hasType(LBRACE) )
            exception("Not a JSON object START: "+peekToken()) ;
        parseObject() ;
    }
    
    private void parseObject()
    {
        // JSON Object
        nextToken() ;
        handler.startObject(currLine, currCol) ;
        if ( lookingAt(RBRACE) )
        {
            nextToken() ;
            handler.finishObject(currLine, currCol) ;
            return ;
        }
        // ** Read pairs until the cows come home.  Or a } occurs.
        for(;;)
        {
            handler.startPair(currLine, currCol) ;
            if ( ! lookingAt(KEYWORD) && ! lookingAtString() )
                exception("Not a key for a JSON object: "+peekToken()) ;
            String key = peekToken().getImage() ;
            nextToken() ;
            handler.valueString(key, currLine, currCol) ;
            if ( ! lookingAt(COLON) )
                exception("Not a colon: "+peekToken()) ;
            nextToken() ;
            handler.keyPair(currLine, currCol) ;
            
            // One parse.
            parseAny() ;
            handler.finishPair(currLine, currCol) ;
            if ( ! lookingAt(COMMA) )
                break ;
            nextToken() ;
        }
        
        if ( ! lookingAt(RBRACE) )
            exception("Illegal: "+peekToken()) ;
        nextToken() ;
        handler.finishObject(currLine, currCol) ;
    }

    /** Parse one element into the JSONhandler (includes nesting) */
    public void parseAny()
    {
        switch(peekToken().getType())
        {
            case LBRACE:    { parseObject() ; return ; }
            case LBRACKET:  { parseArray() ; return ; }
            
            // Number
            case INTEGER:   { handler.valueInteger(peekToken().getImage(), currLine, currCol) ; nextToken() ; return ; }
            case DECIMAL:   { handler.valueDecimal(peekToken().getImage(), currLine, currCol) ; nextToken() ; return ; }
            case DOUBLE:    { handler.valueDouble(peekToken().getImage(), currLine, currCol) ; nextToken() ; return ; }

            // String - liberal
            case STRING1:
            case STRING2:
            case LONG_STRING1:
            case LONG_STRING2:
            {
                handler.valueString(peekToken().getImage(), currLine, currCol) ;
                nextToken() ; 
                return ;
            }
                
            case KEYWORD:
            { 
                String image = peekToken().getImage() ;
                if ( image.equalsIgnoreCase("true") )    { handler.valueBoolean(true, currLine, currCol) ; nextToken() ; return ; }
                if ( image.equalsIgnoreCase("false") )   { handler.valueBoolean(false, currLine, currCol) ; nextToken() ; return ; }
                if ( image.equalsIgnoreCase("null") )    { handler.valueNull(currLine, currCol) ; nextToken() ; return ; }
                //exception("Unrecognized keyword: "+token()) ;
                // Very liberal
                handler.valueString(image, currLine, currCol) ;
                break ;
            }

            default:
                exception("Unrecognized token: "+peekToken()) ;
        }
    }

    private void parseArray()
    {
        handler.startArray(currLine, currCol) ;
        nextToken() ;
        if ( lookingAt(RBRACKET) )
        {
            nextToken() ;
            handler.finishArray(currLine, currCol) ;
            return ;
        }

        for(;;)
        {
            parseAny() ;
            handler.element(currLine, currCol) ;
            if ( ! lookingAt(COMMA) )
                break ;
            nextToken() ;
        }
        if ( ! lookingAt(RBRACKET) )
            exception("Illegal: "+peekToken()) ;
        nextToken() ;
        handler.finishArray(currLine, currCol) ;
    }
}
