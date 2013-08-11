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

package org.apache.jena.riot.tokens ;

public enum TokenType {
    NODE, IRI, PREFIXED_NAME, BNODE,
    // BOOLEAN,
    // One kind of string?
    STRING, // Token created programmatically and superclass of ...
    STRING1, STRING2, LONG_STRING1, LONG_STRING2,

    LITERAL_LANG, LITERAL_DT, INTEGER, DECIMAL, DOUBLE,

    // Not RDF
    KEYWORD, VAR, HEX, CNTRL,   // Starts with *
    UNDERSCORE,                 // In RDF, UNDERSCORE is only visible if BNode processing is not enabled.


    // COLON is only visible if prefix names are not being processed.
    DOT, COMMA, SEMICOLON, COLON, DIRECTIVE,
    // LT, GT, LE, GE are only visible if IRI processing is not enabled.
    LT, GT, LE, GE, LOGICAL_AND, LOGICAL_OR, // && and ||
    VBAR, AMPHERSAND,

    LBRACE, RBRACE,     // {}
    LPAREN, RPAREN,                 // ()
    LBRACKET, RBRACKET,             // []
    // = == + - * / \
    EQUALS, EQUIVALENT, PLUS, MINUS, STAR, SLASH, RSLASH,
    // Whitespace, any comment, (one line comment, multiline comment)
    NL, WS, COMMENT, COMMENT1, COMMENT2, EOF
}
