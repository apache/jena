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

package org.openjena.riot.tokens;

import static org.openjena.atlas.lib.Chars.* ;
import static org.openjena.riot.system.RiotChars.isA2Z ;
import static org.openjena.riot.system.RiotChars.isA2ZN ;
import static org.openjena.riot.system.RiotChars.isAlphaNumeric ;
import static org.openjena.riot.system.RiotChars.isNewlineChar ;
import static org.openjena.riot.system.RiotChars.isWhitespace ;
import static org.openjena.riot.system.RiotChars.range ;
import static org.openjena.riot.system.RiotChars.valHexChar ;

import java.io.IOException ;
import java.util.NoSuchElementException ;

import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.io.IO ;
import org.openjena.atlas.io.PeekInputStream ;
import org.openjena.atlas.io.InStreamUTF8 ;
import org.openjena.riot.RiotParseException ;


/** Tokenizer for all sorts of things RDF-ish */

public final class TokenizerBytes implements Tokenizer
{
    // UNFINISHED
    // Almost certainly out of date.
    
    // This class works directly on bytes but can't handle some multi-byte
    // cases (e.g. start of a prefixed name).  Better to work in character space
    // (conversion has to be done anyway) even if it's over a very simple
    // a simple UTF-8 decoder or the standard Java one (which is quite 
    // efficient if done in large blocks of bytes).
    
    /* Better - TokenizerBase
     * Abstract InputSource  
     *   Iterator
     *     abstract "int nextCharOrByte()" and "char nextChar"
     *     CharConvert(inputstream) // CharConvert(chByte, inputstream) 
     *     accumulate(stringBuffer,   
     *     TokenizerBase works in int or char
     *     abstract scanners.
     *  insertLiteralChar
     */
    
    // Byte-based tokenizer.
    // Assumes that any marker chars are bytes (code points less than 128)
    
    // Currently, this code is only ASCII because it does byte->char 1:1 
    
    // Space for CURIEs, stricter Turtle QNames, sane Turtle (i.e. leading digits in local part).
    public static final int CTRL_CHAR = B_STAR ;
    public static boolean Checking = false ;
    private Token token = null ; 
    private final StringBuilder stringBuilder = new StringBuilder(200) ;
    private final PeekInputStream inputStream ;
    
    private boolean finished = false ;
    private TokenChecker checker = null ; // new CheckerBase()  ;
    
    /*package*/ TokenizerBytes(PeekInputStream inputStream)
    {
        this.inputStream = inputStream ;
    }
    
    // Share with TokenizerText
    //@Override
    public final boolean hasNext()
    {
        if ( finished )
            return false ;
        if ( token != null )
            return true ;
        
        try {
            skip() ;
            if (inputStream.eof())
            {
                //close() ;
                finished = true ;
                return false ;
            }
            token = parseToken() ;
            if ( token == null )
            {
                //close() ;
                finished = true ;
                return false ;
            }
            return true ;
        } catch (AtlasException ex)
        {
            if ( ex.getCause().getClass() == java.nio.charset.MalformedInputException.class )
                throw new RiotParseException("Bad character encoding", inputStream.getLineNum(), inputStream.getColNum()) ;
            throw new RiotParseException("Bad input stream", inputStream.getLineNum(), inputStream.getColNum()) ;
        }
    }
    
    //@Override
    public final Token next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException() ;
        Token t = token ;
        token = null ;
        return t ;
    }
    
    
    public final Token peek()
    {
        if ( ! hasNext() ) return null ;
        return token ;
    }
    
    public final boolean eof()
    {
        return hasNext() ;
    }
    
    //@Override
    public void remove()
    { throw new UnsupportedOperationException() ; }

    public TokenChecker getChecker() { return checker ; }
    public void setChecker(TokenChecker checker) { this.checker = checker ; }

    //@Override
    public void close()
    { 
        try { inputStream.close() ; }
        catch (IOException ex) { IO.exception(ex) ; }
    }

    // ---- Machinary
    
    private void skip()
    {
        int ch = EOF ;
        for ( ;; )
        {
            if ( inputStream.eof() )
                return ;
    
            ch = inputStream.peekByte() ;
            if ( ch == B_HASH )
            {
                inputStream.readByte() ;
                // Comment.  Skip to NL
                for ( ;; )
                {
                    ch = inputStream.peekByte() ;
                    if ( ch == EOF || isNewlineChar(ch) )
                        break ;
                    inputStream.readByte() ;
                }
            }
            
            // Including excess newline chars from comment.
            if ( ! isWhitespace(ch) )
                break ;
            inputStream.readByte() ;
        }
    }

    private Token parseToken()
    {
        token = new Token(getLine(), getColumn()) ;
        
        int chByte = inputStream.peekByte() ;

        // ---- IRI
        if ( chByte == B_LT )
        {
            inputStream.readByte() ;
            token.setImage(allBetween(B_LT, B_GT, false, false)) ;
            token.setType(TokenType.IRI) ;
            if ( Checking ) checkURI(token.getImage()) ;
            return token ;
        }

        // ---- Literal
        if ( chByte == B_QUOTE1 || chByte == B_QUOTE2 )
        {
            inputStream.readByte() ;
            int ch2 = inputStream.peekByte() ;
            if (ch2 == chByte )
            {
                inputStream.readByte() ; // Read second quote.
                int ch3 = inputStream.peekByte() ;
                if ( ch3 == chByte )
                {
                    inputStream.readByte() ;
                    token.setImage(readLongString(chByte, false)) ;
                    TokenType tt = (chByte == B_QUOTE1) ? TokenType.LONG_STRING1 : TokenType.LONG_STRING2 ;
                    token.setType(tt) ;
                }
                else
                {
                    // Two quotes then a non-quote.
                    // Must be '' or ""
                    // No need to pushback characters as we know the lexical form is the empty string.
                    //if ( ch2 != EOF ) inputStream.pushbackChar(ch2) ;
                    //if ( ch1 != EOF ) inputStream.pushbackChar(ch1) ;    // Must be '' or ""
                    token.setImage("") ;
                    token.setType( (chByte == B_QUOTE1) ? TokenType.STRING1 : TokenType.STRING2 ) ;
                }
            }
            else
            {
                // Single quote character.
                token.setImage(allBetween(chByte, chByte, true, false)) ;
                // Single quoted string.
                token.setType( (chByte == B_QUOTE1) ? TokenType.STRING1 : TokenType.STRING2 ) ;
            }
            
            // Literal.  Is it @ or ^^
            if ( inputStream.peekByte() == B_AT )
            {
                inputStream.readByte() ;
                token.setImage2(langTag()) ;
                token.setType(TokenType.LITERAL_LANG) ;
                if ( Checking ) checkLiteralLang(token.getImage(), token.getImage2()) ;
            }
            else if ( inputStream.peekByte() == '^' )
            {
                expect("^^") ;
                
                // Recursive call!
                // Check no whitespace.
                int nextCh = inputStream.peekByte() ;
                if ( isWhitespace(nextCh) )
                    exception("No whitespace after ^^ in literal with datatype") ;
                // Stash current token.
                Token mainToken = token ;
                Token subToken = parseToken() ;
                if ( ! subToken.isIRI() )
                    exception("Datatype URI required after ^^ - URI or prefixed name expected") ;
                token = mainToken ;
                token.setSubToken(subToken) ;
                token.setType(TokenType.LITERAL_DT) ;
                if ( Checking ) checkLiteralDT(token.getImage(), subToken) ;
            }
            else
            {
                // Was a simple string.
                if ( Checking ) checkString(token.getImage()) ;
            }
            return token ;
        }

        if ( chByte == B_UNDERSCORE )        // Blank node :label must be at least one char
        {
            expect("_:") ;
            token.setImage(readBlankNodeLabel()) ;
            token.setType(TokenType.BNODE) ;
            if ( Checking ) checkBlankNode(token.getImage()) ;
            return token ;
        }

        // Control
        if ( chByte == CTRL_CHAR )
        {
            inputStream.readByte() ;
            token.setType(TokenType.CNTRL) ;
            chByte = inputStream.readByte() ;
            if ( chByte == EOF )
                exception("EOF found after "+CTRL_CHAR) ;
            token.cntrlCode = (char)chByte ;
            if ( Checking ) checkControl(token.cntrlCode) ;
            return token ;
        }

        if ( chByte == B_AT )
        {
            inputStream.readByte() ;
            token.setType(TokenType.DIRECTIVE) ;
            token.setImage(readWord(false)) ; 
            if ( Checking ) checkDirective(token.cntrlCode) ;
            return token ;
        }
        
        if ( chByte == B_QMARK )
        {
            inputStream.readByte() ;
            token.setType(TokenType.VAR) ;
            // Character set?
            token.setImage(readWord(true)) ; 
            if ( Checking ) checkVariable(token.getImage()) ;
            return token ;
        }
        
        // Number?
        switch(chByte)
        { 
            // DOT can start a decimal.  Check for digit.
            case B_DOT:
                inputStream.readByte() ;
                chByte = inputStream.peekByte() ;
                if ( range(chByte, '0', '9') )
                {
                    // Not a DOT after all.
                    inputStream.pushbackByte(B_DOT) ;
                    readNumber() ;
                    return token ;
                }
                token.setType(TokenType.DOT) ;
                return token ;
            
            case B_SEMICOLON:  inputStream.readByte() ; token.setType(TokenType.SEMICOLON) ; token.setImage(";") ; return token ;
            case B_COMMA:      inputStream.readByte() ; token.setType(TokenType.COMMA) ;     token.setImage(",") ; return token ;
            case B_LBRACE:     inputStream.readByte() ; token.setType(TokenType.LBRACE) ;    token.setImage("{") ; return token ;
            case B_RBRACE:     inputStream.readByte() ; token.setType(TokenType.RBRACE) ;    token.setImage("}") ; return token ;
            case B_LPAREN:     inputStream.readByte() ; token.setType(TokenType.LPAREN) ;    token.setImage("(") ; return token ;
            case B_RPAREN:     inputStream.readByte() ; token.setType(TokenType.RPAREN) ;    token.setImage(")") ; return token ;
            case B_LBRACKET:   inputStream.readByte() ; token.setType(TokenType.LBRACKET) ;  token.setImage("[") ; return token ;
            case B_RBRACKET:   inputStream.readByte() ; token.setType(TokenType.RBRACKET) ;  token.setImage("]") ; return token ;

            // Specials (if processing off) -- FIX ME
            //case B_COLON:      inputStream.readByte() ; token.setType(TokenType.COLON) ; return token ;
            case B_UNDERSCORE: inputStream.readByte() ; token.setType(TokenType.UNDERSCORE) ; token.setImage("_") ; return token ;
            case B_LT:         inputStream.readByte() ; token.setType(TokenType.LT) ; token.setImage("<") ; return token ;
            case B_GT:         inputStream.readByte() ; token.setType(TokenType.GT) ; token.setImage(">") ; return token ;
            // GE, LE
            // Single character symbols for * / + -

//            case B_PLUS:
//            case B_MINUS:
//            case B_STAR:
//            case B_SLASH:
//            case B_RSLASH:
                
        }
        
        
        if ( chByte == B_PLUS || chByte == B_MINUS || range(chByte, '0', '9'))
        {
            readNumber() ;
            if ( Checking ) checkNumber(token.getImage(), token.getImage2() ) ;
            return token ;
        }

        // Plain words and prefixes.
        //   Can't start with a number due to numeric test above.
        //   Can't start with a '_' due to blank node test above.
        // If we see a :, the first time it means a prefixed name else it's a token break.

        readPrefixedNameOrKeyWord(token) ;
        
        if ( Checking ) checkKeyword(token.getImage()) ;
        return token ;
    }

    
    private void readPrefixedNameOrKeyWord(Token token2)
    {
        long posn = inputStream.getPosition() ;
        token2.setImage(readWord(false)) ;
        token2.setType(TokenType.KEYWORD) ;
        int ch = inputStream.peekByte() ;
        if ( ch == B_COLON )
        {
            inputStream.readByte() ;
            token2.setType(TokenType.PREFIXED_NAME) ;
            String ln = readLocalPart() ;
            token2.setImage2(ln) ;
            if ( Checking ) checkPrefixedName(token2.getImage(), token2.getImage2()) ;
        }

        // If we made no progress, nothing found, not even a keyword -- it's an error.
        if ( posn == inputStream.getPosition() )  
            exception(String.format("Unknown char: %c(%d)",ch,ch)) ;

        if ( Checking ) checkKeyword(token2.getImage()) ;
        
    }
    
    private String readLongString(int quoteChar, boolean endNL)
    {
        stringBuilder.setLength(0) ;
        for ( ;; )
        {
            int ch = inputStream.readByte() ;
            if ( ch == EOF )
            {
                if ( endNL ) return stringBuilder.toString() ; 
                exception("Broken long string") ;
            }
            
            if ( ch == quoteChar )
            {
                if ( threeQuotes(quoteChar) )
                    return stringBuilder.toString() ;
            }
            
            if ( ch == '\\' )
                ch = readLiteralEscape() ;
            insertCodepoint(stringBuilder, ch) ;
        }
    }

    // Need "readByteOrEscape"
    
    private String readLocalPart()
    { return readWordSub(true, true) ; }
    
    private String readWord(boolean leadingDigitAllowed)
    { return readWordSub(leadingDigitAllowed, false) ; }
    
    private String readWordSub(boolean leadingDigitAllowed, boolean leadingSignAllowed)
    {
        stringBuilder.setLength(0) ;
        int idx = 0 ;
        if ( ! leadingDigitAllowed )
        {
            int ch = inputStream.peekByte() ;
            if ( Character.isDigit(ch) )
                return "" ;
        }
        if ( ! leadingSignAllowed )
        {
            int ch = inputStream.peekByte() ;
            if ( ch == '-' || ch == '+' )
                return "" ;
        }
        
        for ( ;; idx++ )
        {
            int ch = inputStream.peekByte() ;
            
            if ( Character.isLetterOrDigit(ch) || ch == '_' || ch == '.' || ch == '-'  ) // ||  ch == '#' || ch == '/' )
            {
                inputStream.readByte() ;
                // UTF-8
                int ch2 = InStreamUTF8.advance(inputStream.getInput()) ;
                stringBuilder.append((char)ch2) ;
                continue ;
            }
            else
                break ;
            
        }
        // BAD : assumes pushbackChar is infinite.
        // Check is ends in "."
        while ( idx > 0 && stringBuilder.charAt(idx-1) == B_DOT )
        {
            // Push back the dot.
            inputStream.pushbackByte(B_DOT) ;
            stringBuilder.setLength(idx-1) ;
            idx -- ;
        }
        return stringBuilder.toString() ;
    }

    // Need "readByteOrEscape"
    
    // Assume have read the first quote char.
    // On return:
    //   If false, have moved over no more characters (due to pushbacks) 
    //   If true, at end of 3 quotes
    private boolean threeQuotes(int ch)
    {
        //inputStream.readByte() ;         // Read first quote.
        int ch2 = inputStream.peekByte() ;
        if (ch2 != ch )
        {
            //inputStream.pushbackChar(ch2) ;
            return false ;
        }
        
        inputStream.readByte() ;         // Read second quote.
        int ch3 = inputStream.peekByte() ;
        if ( ch3 != ch )
        {
            inputStream.pushbackByte(ch2) ;
            return false ;
        }
            
        // Three quotes.
        inputStream.readByte() ;         // Read third quote.
        return true ;
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
        stringBuilder.setLength(0) ;
        
        int x = 0 ; // Digits before a dot.
        int ch = inputStream.peekByte() ;
        if ( ch == '0' )
        {
            x++ ;
            inputStream.readByte() ;
            // Digit 0
            stringBuilder.append((char)ch) ;
            ch = inputStream.peekByte() ;
            if ( ch == 'x' || ch == 'X' )
            {
                inputStream.readByte() ;
                stringBuilder.append((char)ch) ;
                readHex(inputStream, stringBuilder) ;
                token.setImage(stringBuilder.toString()) ;
                token.setType(TokenType.HEX) ;
                return ;
            }
        }
        else if ( ch == '-' || ch == '+' )
        {
            readPossibleSign(stringBuilder) ;
        }
        
        
        x += readDigits(stringBuilder) ;
//        if ( x == 0 )
//        {
//            
//        }
        ch = inputStream.peekByte() ;
        if ( ch == B_DOT )
        {
            inputStream.readByte() ;
            stringBuilder.append(B_DOT) ;
            isDecimal = true ;  // Includes things that will be doubles.
            readDigits(stringBuilder) ;
        }
        
        if ( x == 0 && ! isDecimal )
            // Possible a tokenizer error - should not have entered readNumber in the first place.
            exception("Unrecognized as number") ;
        
        if ( exponent(stringBuilder) )
        {
            isDouble = true ;
            isDecimal = false ;
            
        }
        
        token.setImage(stringBuilder.toString()) ;
        if ( isDouble )
            token.setType(TokenType.DOUBLE) ;
        else if ( isDecimal )
            token.setType(TokenType.DECIMAL) ;
        else
            token.setType(TokenType.INTEGER) ;
    }

    
    private static void readHex(PeekInputStream inputStream, StringBuilder sb)
    {
        // Just after the 0x, which are in sb
        int x = 0 ;
        for(;;)
        {
            int ch = inputStream.peekByte() ;

            if ( ! range(ch, '0', '9') && ! range(ch, 'a', 'f') && ! range(ch, 'A', 'F') )
                break ;
            inputStream.readByte() ;
            // Less than codepoint 128
            sb.append((char)ch) ;
            x++ ;
        }
        if ( x == 0 )
            exception(inputStream, "No hex characters after "+sb.toString()) ;
    }

    private boolean exponent(StringBuilder sb)
    {
        int ch = inputStream.peekByte() ;
        if ( ch != 'e' && ch != 'E' )
            return false ;
        inputStream.readByte() ;
        sb.append((char)ch) ;
        readPossibleSign(sb) ;
        int x = readDigits(sb) ;
        if ( x == 0 )
            exception("Malformed double: "+sb) ;
        return true ;
    }

    private void readPossibleSign(StringBuilder sb)
    {
        int ch = inputStream.peekByte() ;
        if ( ch == '-' || ch == '+' )
        {
            inputStream.readByte() ;
            sb.append((char)ch) ;
        }
    }

    private int readDigits(StringBuilder buffer)
    {
        int count = 0 ;
        for(;;)
        {
            int ch = inputStream.peekByte() ;
            if ( ! range(ch, '0', '9' ) )
                break ;
            inputStream.readByte() ;
            // Less than code point 128
            buffer.append((char)ch) ;
            count ++ ;
        }
        return count ;
    }
    
    private String langTag()
    {
        stringBuilder.setLength(0) ;
        a2z(stringBuilder) ;
        if ( stringBuilder.length() == 0 )
            exception("Bad language tag") ;
        for ( ;; )
        {
            int ch = inputStream.peekByte() ;
            if ( ch == '-' )
            {
                inputStream.readByte() ;
                stringBuilder.append('-') ;
                int x = stringBuilder.length();
                a2zN(stringBuilder) ;
                if ( stringBuilder.length() == x )
                    exception("Bad language tag") ;
            }
            else
                break ;
        }
        return stringBuilder.toString();
    }
    
    private void a2z(StringBuilder sb2)
    {
        for ( ;; )
        {
            int ch = inputStream.peekByte() ;
            if ( isA2Z(ch) )
            {
                inputStream.readByte() ;
                // Less than codepoint 128
                stringBuilder.append((char)ch) ;
            }
            else
                return ;
        }
    }
    
    private void a2zN(StringBuilder sb2)
    {
        for ( ;; )
        {
            int ch = inputStream.peekByte() ;
            if ( isA2ZN(ch) )
            {
                inputStream.readByte() ;
                stringBuilder.append((char)ch) ;
            }
            else
                return ;
        }
    }

    // Blank node label: A-Z,a-z0-9 and '-'
    // Also possible: skip to space or EOF
    private String readBlankNodeLabel()
    {
        stringBuilder.setLength(0) ;
        // First character.
        {
            int ch = inputStream.peekByte() ;
            if ( ch == EOF )
                exception("Blank node label missing (EOF found)") ;
            if ( isWhitespace(ch))
                exception("Blank node label missing") ;
            //if ( ! isAlpha(ch) && ch != '_' )
            // Not strict
            if ( ! isAlphaNumeric(ch) && ch != '_' )
                exception("Blank node label does not start with alphabetic or _ :"+(char)ch) ;
            inputStream.readByte() ;
            stringBuilder.append((char)ch) ;
        }
        // Remainder.
        for(;;)
        {
            int ch = inputStream.peekByte() ;
            if ( ch == EOF )
                break ;
            if ( ! isAlphaNumeric(ch) && ch != '-' && ch != '_' )
                break ;
            inputStream.readByte() ;
            stringBuilder.append((char)ch) ;
        }
//        if ( ! seen )
//            exception("Blank node label missing") ;
        return stringBuilder.toString() ; 
    }

    
    // Get characters between two markers.
    // strEscapes may be processed
    // endNL end of line as an ending is OK
    private String allBetween(int startCh, int endCh,
                              boolean strEscapes, boolean endNL)
    {
        long y = getLine() ;
        long x = getColumn() ;
        stringBuilder.setLength(0) ;

        // Assumes first char read already.
//        int ch0 = inputStream.readByte() ;
//        if ( ch0 != startCh )
//            exception("Broken parser", y, x) ;

        
        for(;;)
        {
            int ch = inputStream.readByte() ;
            if ( ch == EOF )
            {
                if ( endNL ) return stringBuilder.toString() ; 
                exception("Broken token: "+stringBuilder.toString(), y, x) ;
            }

            if ( ch == '\n' )
                exception("Broken token (newline): "+stringBuilder.toString(), y, x) ;
            
            if ( ch == endCh )
            {
                //sb.append(((char)ch)) ;
                return stringBuilder.toString() ;
            }
            
            if ( ch == '\\' )
            {
                if ( strEscapes )
                    ch = readLiteralEscape() ;
                else
                    ch = readUnicodeEscape() ;
                insertCodepoint(stringBuilder, ch) ;
                continue ;
            }
            // Not special.
            insertChar(stringBuilder, ch) ;
        }
    }
    
    // Insert character, knowing the first byte.
    private void insertChar(StringBuilder buffer, int first)
    {
        int ch2 = InStreamUTF8.advance(inputStream.getInput(), first) ;
        insertCodepoint(buffer, ch2) ;
    }

    // ch is already a unicode codepoint
    private void insertCodepoint(StringBuilder buffer, int ch)
    {
        if ( Character.charCount(ch) == 1 )
            buffer.append((char)ch) ;
        else
        {
            // Convert to UTF-16.  Note that the rest of any system this is used
            // in must also respect codepoints and surrogate pairs. 
            if ( ! Character.isDefined(ch) && ! Character.isSupplementaryCodePoint(ch) )
                exception(String.format("Illegal codepoint: 0x%04X", ch)) ;
            char[] chars = Character.toChars(ch) ;
            buffer.append(chars) ;
        }
    }

    
    
    public long getColumn()
    {
        return inputStream.getColNum() ;
    }

    public long getLine()
    {
        return inputStream.getLineNum() ;
    }

    // ---- Routines to check tokens
    
    private void checkBlankNode(String blankNodeLabel)
    { 
        if ( checker != null ) checker.checkBlankNode(blankNodeLabel) ;
    }

    private void checkLiteralLang(String lexicalForm, String langTag)
    {
       if ( checker != null ) checker.checkLiteralLang(lexicalForm, langTag) ;
    }

    private void checkLiteralDT(String lexicalForm, Token datatype)
    {
       if ( checker != null ) checker.checkLiteralDT(lexicalForm, datatype) ;
    }

    private void checkString(String string)
    {
       if ( checker != null ) checker.checkString(string) ;
    }

    private void checkURI(String uriStr)
    {
       if ( checker != null ) checker.checkURI(uriStr) ;
    }

    private void checkNumber(String image, String datatype)
    {
       if ( checker != null ) checker.checkNumber(image, datatype) ;
    }

    private void checkVariable(String tokenImage)
    {
       if ( checker != null ) checker.checkVariable(tokenImage) ;
    }

    private void checkDirective(int cntrlCode)
    {
       if ( checker != null ) checker.checkDirective(cntrlCode) ;
    }

    private void checkKeyword(String tokenImage)
    {
       if ( checker != null ) checker.checkKeyword(tokenImage) ;
    }

    private void checkPrefixedName(String tokenImage, String tokenImage2)
    {
       if ( checker != null ) checker.checkPrefixedName(tokenImage, tokenImage2) ;
    }

    private void checkControl(int code)
    {
       if ( checker != null ) checker.checkControl(code) ;
    }

    // ---- Escape sequences
    
    private final
    int readLiteralEscape()
    {
        int c = inputStream.readByte();
        if ( c==EOF )
            exception("Escape sequence not completed") ;

        switch (c)
        {
            case 'n':   return NL ; 
            case 'r':   return CR ;
            case 't':   return '\t' ;
            case '"':   return '"' ;
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
        int ch = inputStream.readByte() ;
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
        int ch = inputStream.readByte() ;
        if ( ch == EOF )
            exception("Not a hexadecimal character (end of file)") ;

        int x =  valHexChar(ch) ;
        if ( x != -1 )
            return x ; 
        exception("Not a hexadecimal character: "+(char)ch) ;
        return -1 ; 
    }
    
    private boolean expect(String str) {
        for (int i = 0; i < str.length(); i++) {
            char want = str.charAt(i);
            if (inputStream.eof())
            {
                exception("End of input during expected string: "+str) ;
                return false ;
            }
            int inChar = inputStream.readByte();
            if (inChar != want) {
                //System.err.println("N-triple inputStream error");
                exception("expected \"" + str + "\"");
                return false;
            }
        }
        return true;
    }

    private void exception(String message)
    {
        exception(message, inputStream.getLineNum(), inputStream.getColNum()) ;
    }
    
    private static void exception(PeekInputStream inputStream, String message)
    {
        exception(message, inputStream.getLineNum(), inputStream.getColNum()) ;
    }

    private static void exception(String message, long line, long col)
    {
        throw new RiotParseException(message, line, col) ;
    }
}
