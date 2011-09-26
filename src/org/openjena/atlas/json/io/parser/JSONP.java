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

package org.openjena.atlas.json.io.parser;

import static org.openjena.riot.tokens.TokenType.COLON ;
import static org.openjena.riot.tokens.TokenType.COMMA ;
import static org.openjena.riot.tokens.TokenType.KEYWORD ;
import static org.openjena.riot.tokens.TokenType.LBRACE ;
import static org.openjena.riot.tokens.TokenType.RBRACE ;
import static org.openjena.riot.tokens.TokenType.RBRACKET ;
import org.openjena.atlas.json.io.JSONHandler ;

/** Json (extended) parser
 * Extensions:
 *   Use of ', """ and ''' for strings
 *   Bare words for strings in maps (JSON objects)
 *   Hex numbers
 * Objects with duplicate keys are a warning (keeps last)
 */
public class JSONP extends ParserBase
{
    private JSONHandler maker ;

    public JSONP(TokenizerJSON tokens, JSONHandler maker)
    {
        super(tokens) ;
        // Disable prefixnames, enable COLON
        this.maker = maker ;
    }
    
    public void parse()
    {
        if ( ! token().hasType(LBRACE) )
            exception("Not a JSON object START: "+token()) ;
        parseObject() ;
    }
    
    private void parseObject()
    {
        // JSON Object
        maker.startObject() ;
        move() ;
        if ( lookingAt(RBRACE) )
        {
            move() ;
            maker.finishObject() ;
            return ;
        }
        // ** Read pairs until the cows come home.  Or a } occurs.
        for(;;)
        {
            maker.startPair() ;
            if ( ! lookingAt(KEYWORD) && ! lookingAtString() )
                exception("Not a key for a JSON object: "+token()) ;
            String key = token().getImage() ;
            move() ;
            maker.valueString(key) ;
            if ( ! lookingAt(COLON) )
                exception("Not a colon: "+token()) ;
            move() ;
            maker.keyPair() ;
            
            // One parse.
            parseAny() ;
            maker.finishPair() ;
            if ( ! lookingAt(COMMA) )
                break ;
            move() ;
        }
        
        if ( ! lookingAt(RBRACE) )
            exception("Illegal: "+token()) ;
        move() ;
        maker.finishObject() ;
    }

    /** Parse one element into the JSONMaker (includes nesting) */
    public void parseAny()
    {
        switch(token().getType())
        {
            case LBRACE:    { parseObject() ; return ; }
            case LBRACKET:  { parseArray() ; return ; }
            
            // Number
            case INTEGER:   { maker.valueInteger(token().getImage()) ; move() ; return ; }
            case DECIMAL:   { maker.valueDecimal(token().getImage()) ; move() ; return ; }
            case DOUBLE:    { maker.valueDouble(token().getImage()) ; move() ; return ; }

            // String - liberal
            case STRING1:
            case STRING2:
            case LONG_STRING1:
            case LONG_STRING2:
            {
                maker.valueString(token().getImage()) ;
                move() ; 
                return ;
            }
                
            case KEYWORD:
            { 
                String image = token().getImage() ;
                if ( image.equalsIgnoreCase("true") )    { maker.valueBoolean(true) ; move() ; return ; }
                if ( image.equalsIgnoreCase("false") )   { maker.valueBoolean(false) ; move() ; return ; }
                if ( image.equalsIgnoreCase("null") )    { maker.valueNull() ; move() ; return ; }
                //exception("Unrecognized keyword: "+token()) ;
                // Very liberal
                maker.valueString(image) ;
                break ;
            }

            default:
                exception("Unrecognized token: "+token()) ;
        }
    }

    private void parseArray()
    {
        maker.startArray() ;
        move() ;
        if ( lookingAt(RBRACKET) )
        {
            move() ;
            maker.finishArray() ;
            return ;
        }

        for(;;)
        {
            parseAny() ;
            maker.element() ;
            if ( ! lookingAt(COMMA) )
                break ;
            move() ;
        }
        if ( ! lookingAt(RBRACKET) )
            exception("Illegal: "+token()) ;
        move() ;
        maker.finishArray() ;
    }
}
