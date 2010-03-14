/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json.io.parser;

import static com.hp.hpl.jena.riot.tokens.TokenType.COLON ;
import static com.hp.hpl.jena.riot.tokens.TokenType.COMMA ;
import static com.hp.hpl.jena.riot.tokens.TokenType.KEYWORD ;
import static com.hp.hpl.jena.riot.tokens.TokenType.LBRACE ;
import static com.hp.hpl.jena.riot.tokens.TokenType.RBRACE ;
import static com.hp.hpl.jena.riot.tokens.TokenType.RBRACKET ;
import org.openjena.atlas.json.io.JSONHandler ;

/** Json (extended) parser
 * Extensions:
 *   Use of ', """ and ''' for strings
 *   Bare words for strings in maps (JSON objects)
 *   Hex numbers
 */
public class JSONPP extends ParserBase
{
    private JSONHandler maker ;

    public JSONPP(TokenizerJSON tokens, JSONHandler maker)
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