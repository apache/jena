/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

public class RiotChars
{
    /** End of file - not a Unicode codepoint */
    public static final int EOF             = -1 ;
    
    /** undefined character (exact maning depends on use) - not a Unicode codepoint */
    public static final int UNSET           =  -2 ;
    
    
    public static final char NL              = '\n' ;
    public static final char CR              = '\r' ;
    
    public static final char CH_LBRACKET     = '[' ;
    public static final char CH_RBRACKET     = ']' ;
    
    public static final char CH_LBRACE       = '{' ;
    public static final char CH_RBRACE       = '}' ;

    public static final char CH_LPAREN       = '(' ;
    public static final char CH_RPAREN       = ')' ;

    public static final char CH_LT           = '<' ;
    public static final char CH_GT           = '>' ;
    public static final char CH_UNDERSCORE   = '_' ;

    public static final char CH_QUOTE1       = '\'' ;
    public static final char CH_QUOTE2       = '"' ;

    public static final char CH_EQUALS       = '=' ;
    public static final char CH_STAR         = '*' ;
    public static final char CH_DOT          = '.' ;
    public static final char CH_COMMA        = ',' ;
    public static final char CH_SEMICOLON    = ';' ;
    public static final char CH_COLON        = ':' ;
    public static final char CH_AT           = '@' ;
    public static final char CH_QMARK        = '?' ;
    public static final char CH_HASH         = '#' ;
    public static final char CH_PLUS         = '+' ;
    public static final char CH_MINUS        = '-' ;
    public static final char CH_SLASH        = '/' ;
    public static final char CH_RSLASH       = '\\' ;
    
    // Byte versions of the above
    public static final byte B_NL            = NL ;
    public static final byte B_CR            = CR ;
    
    public static final byte B_LBRACKET      = '[' ;
    public static final byte B_RBRACKET      = ']' ;
    
    public static final byte B_LBRACE        = '{' ;
    public static final byte B_RBRACE        = '}' ;

    public static final byte B_LPAREN        = '(' ;
    public static final byte B_RPAREN        = ')' ;

    public static final byte B_LT            = '<' ;
    public static final byte B_GT            = '>' ;
    public static final byte B_UNDERSCORE    = '_' ;

    public static final byte B_QUOTE1        = '\'' ;
    public static final byte B_QUOTE2        = '"' ;

    public static final byte B_EQUALS        = '=' ;
    public static final byte B_STAR          = '*' ;
    public static final byte B_DOT           = '.' ;
    public static final byte B_COMMA         = ',' ;
    public static final byte B_SEMICOLON     = ';' ;
    public static final byte B_COLON         = ':' ;
    public static final byte B_AT            = '@' ;
    public static final byte B_QMARK         = '?' ;
    public static final byte B_HASH          = '#' ;
    public static final byte B_PLUS          = '+' ;
    public static final byte B_MINUS         = '-' ;
    public static final byte B_SLASH         = '/' ;
    public static final byte B_RSLASH        = '\\' ;
    
    
    // ---- Character classes 
    
    public static boolean isAlpha(int codepoint)
    {
        return Character.isLetter(codepoint) ;
    }
    
    public static boolean isAlphaNumeric(int codepoint)
    {
        return Character.isLetterOrDigit(codepoint) ;
    }
    
    /** ASCII A-Z */
    public static boolean isA2Z(int ch)
    {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z') ;
    }

    /** ASCII A-Z or 0-9 */
    public static boolean isA2ZN(int ch)
    {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z') || range(ch, '0', '9') ;
    }

    /** ASCII 0-9 */
    public static boolean isDigit(int ch)
    {
        return range(ch, '0', '9') ;
    }
    
    public static boolean isWhitespace(int ch)
    {
        // ch = ch | 0xFF ;
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n' || ch == '\f' ;    
    }
    
    public static boolean isNewlineChar(int ch)
    {
        return ch == '\r' || ch == '\n' ;
    }

    public static int valHexChar(int ch)
    {
        if ( range(ch, '0', '9') )
            return ch-'0' ;
        if ( range(ch, 'a', 'f') )
            return ch-'a'+10 ;
        if ( range(ch, 'A', 'F') )
            return ch-'A'+10 ;
        return -1 ;
    }

    
    public static boolean range(int ch, char a, char b)
    {
        return ( ch >= a && ch <= b ) ;
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