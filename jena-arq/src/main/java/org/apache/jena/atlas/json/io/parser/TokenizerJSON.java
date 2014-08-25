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

import static org.apache.jena.atlas.lib.Chars.* ;

import java.io.IOException ;
import java.util.NoSuchElementException ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.PeekReader ;
import org.apache.jena.atlas.json.JsonParseException ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.Tokenizer ;



/** Tokenizer for all sorts of things JSON-ish */

public class TokenizerJSON implements Tokenizer
{
    private Token token = null ; 
    private final StringBuilder sb = new StringBuilder() ;
    private final PeekReader reader ;
    private boolean finished = false ;
    
    public TokenizerJSON(PeekReader reader)
    {
        this.reader = reader ;
    }
    
    @Override
    public final boolean hasNext()
    {
        if ( finished )
            return false ;
        if ( token != null )
            return true ;
        skip() ;
        if (reader.eof())
            return false ;
        token = parseToken() ;
        return token != null ;
    }
    
    @Override
    public final boolean eof()
    {
        return hasNext() ;
    }

    /** Move to next token */
    @Override
    public final Token next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException() ;
        Token t = token ;
        token = null ;
        return t ;
    }

    
    @Override
    public final Token peek()
    {
        if ( ! hasNext() ) return null ;
        return token ; 
    }
    
    @Override
    public void remove()
    { throw new UnsupportedOperationException() ; }

    // ---- Machinary
    
    // ""-string, ''-string, *X, 
    // various single characters . , : ; 
    // (), [], {}, <>
    // Numbers (integer, decimal, double)
    // Keys (restricted strings, used as keys in maps)
    //  ALPHA (ALPHA,NUMERIC,_,...)
    
    private Token parseToken()
        {
            token = new Token(getLine(), getColumn()) ;
            
            int ch = reader.peekChar() ;
    
            // ---- String
            // Support both "" and '' strings (only "" is legal JSON)
            if ( ch == CH_QUOTE1 || ch == CH_QUOTE2 )
            {
                reader.readChar() ;
                int ch2 = reader.peekChar() ;
                if (ch2 == ch )
                {
                    // Maybe """-strings/'''-strings
                    reader.readChar() ; // Read potential second quote.
                    int ch3 = reader.peekChar() ;
                    if ( ch3 == ch )
                    {
                        // """-strings/'''-strings
                        reader.readChar() ;
                        token.setImage(readLong(ch, false)) ;
                        TokenType tt = (ch == CH_QUOTE1) ? TokenType.LONG_STRING1 : TokenType.LONG_STRING2 ;
                        token.setType(tt) ;
                        return token ;
                    }
                    // Two quotes then a non-quote.
                    // Must be '' or ""
    
                    // No need to pushback characters as we know the lexical form is the empty string.
                    //if ( ch2 != EOF ) reader.pushbackChar(ch2) ;
                    //if ( ch1 != EOF ) reader.pushbackChar(ch1) ;    // Must be '' or ""
                    token.setImage("") ;
                }
                else
                    // Single quote character.
                    token.setImage(allBetween(ch, ch, true, false)) ;
                // Single quoted string.
                token.setType( (ch == CH_QUOTE1) ? TokenType.STRING1 : TokenType.STRING2 ) ;
                return token ;
            }
    
            switch(ch)
            { 
                // DOT can't start a decimal in JSON.  Check for digit.
                case CH_DOT:    
    //                reader.readChar() ;
    //                ch = reader.peekChar() ;
    //                if ( range(ch, '0', '9') )
    //                {
    //                    // Not a DOT after all.
    //                    reader.pushbackChar(CH_DOT) ;
    //                    // Drop through to number code.
    //                    break ;
    //                }
                    token.setType(TokenType.DOT) ;
                    return token ;
                    
                
                case CH_SEMICOLON:  reader.readChar() ; token.setType(TokenType.SEMICOLON) ; return token ;
                case CH_COMMA:      reader.readChar() ; token.setType(TokenType.COMMA) ;     return token ;
                case CH_LBRACE:     reader.readChar() ; token.setType(TokenType.LBRACE) ;    return token ;
                case CH_RBRACE:     reader.readChar() ; token.setType(TokenType.RBRACE) ;    return token ;
                case CH_LPAREN:     reader.readChar() ; token.setType(TokenType.LPAREN) ;    return token ;
                case CH_RPAREN:     reader.readChar() ; token.setType(TokenType.RPAREN) ;    return token ;
                case CH_LBRACKET:   reader.readChar() ; token.setType(TokenType.LBRACKET) ;  return token ;
                case CH_RBRACKET:   reader.readChar() ; token.setType(TokenType.RBRACKET) ;  return token ;
    
                // Some interesting characters
                case CH_COLON:      reader.readChar() ; token.setType(TokenType.COLON) ; return token ;
    //            case CH_UNDERSCORE: reader.readChar() ; token.setType(TokenType.UNDERSCORE) ; return token ;
                case CH_LT:         reader.readChar() ; token.setType(TokenType.LT) ; return token ;
                case CH_GT:         reader.readChar() ; token.setType(TokenType.GT) ; return token ;
                // GE, LE
            }
            
            if ( ch == CH_PLUS || ch == CH_MINUS || range(ch, '0', '9'))
            {
                readNumber() ;
                return token ;
            }
    
            // Plain words and prefixes.
            //   Can't start with a number due to numeric test above.
            //   Can start with a '_' (no blank node test above)
    
            readKeyWord(token) ;
            return token ;
        }

    private void skip()
    {
        int ch = EOF ;
        for ( ;; )
        {
            if ( reader.eof() )
                return ;
    
            ch = reader.peekChar() ;
            if ( ch == CH_HASH )
            {
                reader.readChar() ;
                // Comment.  Skip to NL
                for ( ;; )
                {
                    ch = reader.peekChar() ;
                    if ( ch == EOF || isNewlineChar(ch) )
                        break ;
                    reader.readChar() ;
                }
            }
            
            // Including excess newline chars from comment.
            if ( ! isWhitespace(ch) )
                break ;
            reader.readChar() ;
        }
    }

    private void readKeyWord(Token token2)
    {
        long posn = reader.getPosition() ;
        token2.setImage(readWord(false)) ;
        token2.setType(TokenType.KEYWORD) ;
        int ch = reader.peekChar() ;

        // If we made no progress, nothing found, not even a keyword -- it's an error.
        if ( posn == reader.getPosition() )  
            exception(String.format("Unknown char: %c(%d)",ch,ch)) ;
    }
    
    private String readLong(int quoteChar, boolean endNL)
    {
        sb.setLength(0) ;
        for ( ;; )
        {
            int ch = reader.readChar() ;
            if ( ch == EOF )
            {
                if ( endNL ) return sb.toString() ; 
                exception("Broken long string") ;
            }
            
            if ( ch == quoteChar )
            {
                if ( threeQuotes(quoteChar) )
                    return sb.toString() ;
            }
            
            if ( ch == '\\' )
                ch = readLiteralEscape() ;
            insertLiteralChar(sb, ch) ;
        }
    }

    // Need "readCharOrEscape"
    
    // Assume have read the first quote char.
    // On return:
    //   If false, have moved over no more characters (due to pushbacks) 
    //   If true, at end of 3 quotes
    private boolean threeQuotes(int ch)
    {
        //reader.readChar() ;         // Read first quote.
        int ch2 = reader.peekChar() ;
        if (ch2 != ch )
        {
            //reader.pushbackChar(ch2) ;
            return false ;
        }
        
        reader.readChar() ;         // Read second quote.
        int ch3 = reader.peekChar() ;
        if ( ch3 != ch )
        {
            //reader.pushbackChar(ch3) ;
            reader.pushbackChar(ch2) ;
            return false ;
        }
            
        // Three quotes.
        reader.readChar() ;         // Read third quote.
        return true ;
    }
    
    // Read a "word": alphanumerics, "_", ".", "-"
    private String readWord(boolean leadingDigitAllowed)
    {
        sb.setLength(0) ;
        int idx = 0 ;
        if ( ! leadingDigitAllowed )
        {
            int ch = reader.peekChar() ;
            if ( Character.isDigit(ch) )
                return "" ;
        }
        
        for ( ;; idx++ )
        {
            int ch = reader.peekChar() ;
            
            if ( Character.isLetterOrDigit(ch) || ch == '_' || ch == '.' || ch == '-' )
            {
                reader.readChar() ;
                sb.append((char)ch) ;
                continue ;
            }
            else
                break ;
            
        }
        
//        // Trailing DOT?
//        // BAD : assumes pushbackChar is infinite.
//        // Check is ends in "."
//        while ( idx > 0 && sb.charAt(idx-1) == CH_DOT )
//        {
//            // Push back the dot.
//            reader.pushbackChar(CH_DOT) ;
//            sb.setLength(idx-1) ;
//            idx -- ;
//        }
        return sb.toString() ;
    }

    // Make better!
    /*
    [16]    integer         ::=     ('-' | '+') ? [0-9]+
    [17]    double          ::=     ('-' | '+') ? ( [0-9]+ '.' [0-9]* exponent | '.' ([0-9])+ exponent | ([0-9])+ exponent )
                                    0.e0, .0e0, 0e0
    [18]    decimal         ::=     ('-' | '+')? ( [0-9]+ '.' [0-9]* | '.' ([0-9])+ | ([0-9])+ )
                                    0.0 .0
    [19]    exponent        ::=     [eE] ('-' | '+')? [0-9]+
    []      hex             ::=     0x0123456789ABCDEFG
    
    */
    private void readNumber()
    {
        // One entry, definitely a number.
        // Beware of '.' as a (non) decimal.
        /*
        maybeSign()
        digits()
        if dot ==> decimal, digits
        if e   ==> double, maybeSign, digits
        else
            check not "." for decimal.
        */
        boolean isDouble = false ;
        boolean isDecimal = false ;
        sb.setLength(0) ;
        
        int x = 0 ; // Digits before a dot.
        int ch = reader.peekChar() ;
        if ( ch == '0' )
        {
            x++ ;
            reader.readChar() ;
            sb.append((char)ch) ;
            ch = reader.peekChar() ;
            if ( ch == 'x' || ch == 'X' )
            {
                reader.readChar() ;
                sb.append((char)ch) ;
                readHex(reader, sb) ;
                token.setImage(sb.toString()) ;
                token.setType(TokenType.HEX) ;
                return ;
            }
        }
        else if ( ch == '-' || ch == '+' )
        {
            readPossibleSign(sb) ;
        }
        
        
        x += readDigits(sb) ;
//        if ( x == 0 )
//        {
//            
//        }
        ch = reader.peekChar() ;
        if ( ch == CH_DOT )
        {
            reader.readChar() ;
            sb.append(CH_DOT) ;
            isDecimal = true ;  // Includes things that will be doubles.
            readDigits(sb) ;
        }
        
        if ( x == 0 && ! isDecimal )
            // Possible a tokenizer error - should not have entered readNumber in the first place.
            exception("Unrecognized as number") ;
        
        if ( exponent(sb) )
        {
            isDouble = true ;
            isDecimal = false ;
            
        }
        
        token.setImage(sb.toString()) ;
        if ( isDouble )
            token.setType(TokenType.DOUBLE) ;
        else if ( isDecimal )
            token.setType(TokenType.DECIMAL) ;
        else
            token.setType(TokenType.INTEGER) ;
    }

    
    private static void readHex(PeekReader reader, StringBuilder sb)
    {
        // Just after the 0x, which are in sb
        int x = 0 ;
        for(;;)
        {
            int ch = reader.peekChar() ;

            if ( ! range(ch, '0', '9') && ! range(ch, 'a', 'f') && ! range(ch, 'A', 'F') )
                break ;
            reader.readChar() ;
            sb.append((char)ch) ;
            x++ ;
        }
        if ( x == 0 )
            exception(reader, "No hex characters after "+sb.toString()) ;
    }

    private boolean exponent(StringBuilder sb)
    {
        int ch = reader.peekChar() ;
        if ( ch != 'e' && ch != 'E' )
            return false ;
        reader.readChar() ;
        sb.append((char)ch) ;
        readPossibleSign(sb) ;
        int x = readDigits(sb) ;
        if ( x == 0 )
            exception("Malformed double: "+sb) ;
        return true ;
    }

    private void readPossibleSign(StringBuilder sb)
    {
        int ch = reader.peekChar() ;
        if ( ch == '-' || ch == '+' )
        {
            reader.readChar() ;
            sb.append((char)ch) ;
        }
    }

    private int readDigits(StringBuilder buffer)
    {
        int count = 0 ;
        for(;;)
        {
            int ch = reader.peekChar() ;
            if ( ! range(ch, '0', '9' ) )
                break ;
            reader.readChar() ;
            buffer.append((char)ch) ;
            count ++ ;
        }
        return count ;
    }
    
    private String langTag()
    {
        sb.setLength(0) ;
        a2z(sb) ;
        if ( sb.length() == 0 )
            exception("Bad language tag") ;
        for ( ;; )
        {
            int ch = reader.peekChar() ;
            if ( ch == '-' )
            {
                reader.readChar() ;
                sb.append('-') ;
                int x = sb.length();
                a2zN(sb) ;
                if ( sb.length() == x )
                    exception("Bad language tag") ;
            }
            else
                break ;
        }
        return sb.toString();
    }
    
    private void a2z(StringBuilder sb2)
    {
        for ( ;; )
        {
            int ch = reader.peekChar() ;
            if ( isA2Z(ch) )
            {
                reader.readChar() ;
                sb.append((char)ch) ;
            }
            else
                return ;
        }
    }
    
    private void a2zN(StringBuilder sb2)
    {
        for ( ;; )
        {
            int ch = reader.peekChar() ;
            if ( isA2ZN(ch) )
            {
                reader.readChar() ;
                sb.append((char)ch) ;
            }
            else
                return ;
        }
    }

    // Blank node label: A-Z,a-z0-9 and '-'
    private String blankNodeLabel()
    {
        sb.setLength(0) ;
        boolean seen = false ;
        for(;;)
        {
            int ch = reader.readChar() ;
            if ( ch == EOF )
                break ;
            if ( ! isA2ZN(ch) && ch != '-' )
                break ;
            sb.append((char)ch) ;
            seen = true ;
        }
        if ( ! seen )
            exception("Blank node label missing") ;
        return sb.toString() ; 
    }

    
    // Get characters between two markers.
    // strEscapes may be processed
    // endNL end of line as an ending is OK
    private String allBetween(int startCh, int endCh,
                              boolean strEscapes, boolean endNL)
    {
        long y = getLine() ;
        long x = getColumn() ;
        sb.setLength(0) ;

        // Assumes first char read already.
//        int ch0 = reader.readChar() ;
//        if ( ch0 != startCh )
//            exception("Broken parser", y, x) ;

        
        for(;;)
        {
            int ch = reader.readChar() ;
            if ( ch == EOF )
            {
                if ( endNL ) return sb.toString() ; 
                exception("Broken token: "+sb.toString(), y, x) ;
            }

            if ( ch == '\n' )
                exception("Broken token (newline): "+sb.toString(), y, x) ;
            
            if ( ch == endCh )
            {
                //sb.append(((char)ch)) ;
                return sb.toString() ;
            }
            
            if ( ch == '\\' )
            {
                if ( strEscapes )
                    ch = readLiteralEscape() ;
                else
                {
                    ch = reader.readChar() ;
                    if ( ch == EOF )
                    {
                        if ( endNL ) return sb.toString() ; 
                        exception("Broken token: "+sb.toString(), y, x) ;
                    }
    
                    switch (ch)
                    {
                        case 'u': ch = readUnicode4Escape(); break ;
                        case 'U': ch = readUnicode4Escape(); break ;
                        default:
                            exception(String.format("illegal escape sequence value: %c (0x%02X)", ch, ch));
                            break ;
                    }
                }
            }
            insertLiteralChar(sb, ch) ;
        }
    }
    
    private void insertLiteralChar(StringBuilder buffer, int ch)
    {
        if ( Character.charCount(ch) == 1 )
            buffer.append((char)ch) ;
        else
        {
            // Convert to UTF-16.  Note that the rest of any systemn this is used
            // in must also respect codepoints and surrogate pairs. 
            if ( ! Character.isDefined(ch) && ! Character.isSupplementaryCodePoint(ch) )
                exception(String.format("Illegal codepoint: 0x%04X", ch)) ;
            char[] chars = Character.toChars(ch) ;
            buffer.append(chars) ;
        }
    }

    @Override
    public long getColumn()
    {
        return reader.getColNum() ;
    }

    @Override
    public long getLine()
    {
        return reader.getLineNum() ;
    }

    // ---- Character classes 
    
    @Override
    public void close()
    {
        try { reader.close() ; }
        catch (IOException ex) { IO.exception(ex) ; }
    }

    private boolean isA2Z(int ch)
    {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z') ;
    }

    private boolean isA2ZN(int ch)
    {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z') || range(ch, '0', '9') ;
    }

    private boolean isNumeric(int ch)
    {
        return range(ch, '0', '9') ;
    }
    
    private static boolean isWhitespace(int ch)
    {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n' || ch == '\f' ;    
    }
    
    private static boolean isNewlineChar(int ch)
    {
        return ch == '\r' || ch == '\n' ;
    }

    // ---- Escape sequences
    
    private final
    int readLiteralEscape()
    {
        int c = reader.readChar();
        if ( c==EOF )
            exception("Escape sequence not completed") ;

        switch (c)
        {
            case 'n':   return NL ; 
            case 'r':   return CR ;
            case 't':   return '\t' ;
            case 'b':   return '\b' ;
            case '"':   return '"' ;
            case '/':   return '/' ;    // JSON requires / escapes.
            case '\'':  return '\'' ;
            case '\\':  return '\\' ;
            case 'u':   return readUnicode4Escape();
            case 'U':   return readUnicode8Escape();
            default:
                exception(String.format("illegal escape sequence value: %c (0x%02X)", c, c));
                return 0 ;
        }
    }
    
    
    private final
    int readUnicodeEscape()
    {
        int ch = reader.readChar() ;
        if ( ch == EOF )
            exception("Broken escape sequence") ;

        switch (ch)
        {
            case 'u': return readUnicode4Escape(); 
            case 'U': return readUnicode8Escape(); 
            default:
                exception(String.format("illegal escape sequence value: %c (0x%02X)", ch, ch));
        }
        return 0 ;
    }
    
    private final
    int readUnicode4Escape() { return readUnicodeEscape(4) ; }
    
    private final
    int readUnicode8Escape()
    {
        int ch8 = readUnicodeEscape(8) ;
        if ( ch8 > Character.MAX_CODE_POINT )
            exception(String.format("illegal code point in \\U sequence value: 0x%08X", ch8));
        return ch8 ;
    }
    
    private final
    int readUnicodeEscape(int N)
    {
        int x = 0 ;
        for ( int i = 0 ; i < N ; i++ )
        {
            int d = readHexChar() ;
            if ( d < 0 )
                return -1 ;
            x = (x<<4)+d ;
        }
        return x ; 
    }
    
    private final
    int readHexChar()
    {
        int ch = reader.readChar() ;
        if ( ch == EOF )
            exception("Not a hexadecimal character (end of file)") ;

        if ( range(ch, '0', '9') )
            return ch-'0' ;
        if ( range(ch, 'a', 'f') )
            return ch-'a'+10 ;
        if ( range(ch, 'A', 'F') )
            return ch-'A'+10 ;
        
        exception("Not a hexadecimal character: "+(char)ch) ;
        return -1 ; 
    }
    
    private static boolean range(int ch, char a, char b)
    {
        return ( ch >= a && ch <= b ) ;
    }

    private boolean expect(String str) {
        for (int i = 0; i < str.length(); i++) {
            char want = str.charAt(i);
            if (reader.eof())
            {
                exception("End of input during expected string: "+str) ;
                return false ;
            }
            int inChar = reader.readChar();
            if (inChar != want) {
                //System.err.println("N-triple reader error");
                exception("expected \"" + str + "\"");
                return false;
            }
        }
        return true;
    }

    private void exception(String message)
    {
        exception(message, reader.getLineNum(), reader.getColNum()) ;
    }
    
    private static void exception(PeekReader reader, String message)
    {
        exception(message, reader.getLineNum(), reader.getColNum()) ;
    }

    private static void exception(String message, long line, long col)
    {
        throw new JsonParseException(message, (int)line, (int)col) ;
    }
}
