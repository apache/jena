/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.tokens;

public enum TokenType
{
    NODE,
    IRI,
    PREFIXED_NAME, 
    BNODE,
    //BOOLEAN,
    // One kind of string?
    STRING,         // Tokne created programmatically and superclass of ...
    STRING1, STRING2,
    LONG_STRING1, LONG_STRING2,
    
    LITERAL_LANG, LITERAL_DT,
    INTEGER, DECIMAL, DOUBLE,
    
    // Not RDF
    KEYWORD, CNTRL, VAR, HEX,
    
    // Syntax
    // COLON is only visible if prefix names are not being processed.
    DOT, COMMA, SEMICOLON, COLON, DIRECTIVE,
    // LT, GT, LE, GE are only visible if IRI processing is not enabled.
    // TODO Fix this.
    LT, GT, LE, GE,
    // In RDF, UNDERSCORE is only visible if BNode processing is not enabled.
    UNDERSCORE, 
    LBRACE, RBRACE,     // {} 
    LPAREN, RPAREN,     // ()
    LBRACKET, RBRACKET, // []
    EQUALS, PLUS, MINUS, STAR, SLASH, RSLASH,
    EOF
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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