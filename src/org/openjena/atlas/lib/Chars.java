/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import java.nio.ByteBuffer ;
import java.nio.charset.Charset ;
import java.nio.charset.CharsetDecoder ;
import java.nio.charset.CharsetEncoder ;

public class Chars
{
    private Chars() {}
    
 // So also Bytes.hexDigits to get bytes.
    final public static char[] digits10 = {
        '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' , '9'
    } ;
    
    /** Hex digits : upper case **/ 
    final public static char[] hexDigitsUC = {
        '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' ,
        '9' , 'A' , 'B' , 'C' , 'D' , 'E' , 'F' } ;

    /** Hex digits : lower case **/ 
    final public static char[] hexDigitsLC = {
        '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' ,
        '9' , 'a' , 'b' , 'c' , 'd' , 'e' , 'f' } ;

    
//         , 'g' , 'h' ,
//        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
//        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
//        'u' , 'v' , 'w' , 'x' , 'y' , 'z'
    
    /** Java name for UTF-8 encoding */
    private static final String encodingUTF8     = "utf-8" ;
    /** Java name for ASCII encoding */
    private static final String encodingASCII    = "ascii" ;
    
    public static final Charset charsetUTF8 = Charset.forName(encodingUTF8) ;
    public static final Charset charsetASCII = Charset.forName(encodingASCII) ;
    
    // Pools for encoders/decoder.  Paolo says that creating an encopder or decoder is not that cheap.
    // Initial pool size. Any additional encoder/decoder are later
    // placed in the pool - it's an infinite, reusing, growing pool.
    
    private static final int PoolSize = 2 ;
    private static Pool<CharsetEncoder> encoders = PoolSync.create(new PoolBase<CharsetEncoder>()) ;
    private static Pool<CharsetDecoder> decoders = PoolSync.create(new PoolBase<CharsetDecoder>()) ;
    
    static {
        // Fill the pool.
        for ( int i = 0 ; i < PoolSize ; i++ )
        {
            putEncoder(createEncoder()) ;
            putDecoder(createDecoder()) ;
        }
    }
    
    // In Modified UTF-8,[15] the null character (U+0000) is encoded as 0xC0,0x80; this is not valid UTF-8[16]
    // Char to bytes.
    /* http://en.wikipedia.org/wiki/UTF-8
Bits    Last code point     Byte 1  Byte 2  Byte 3  Byte 4  Byte 5  Byte 6
  7   U+007F  0xxxxxxx
  11  U+07FF  110xxxxx    10xxxxxx
  16  U+FFFF  1110xxxx    10xxxxxx    10xxxxxx
  21  U+1FFFFF    11110xxx    10xxxxxx    10xxxxxx    10xxxxxx
  26  U+3FFFFFF   111110xx    10xxxxxx    10xxxxxx    10xxxxxx    10xxxxxx
  31  U+7FFFFFFF  1111110x    10xxxxxx    10xxxxxx    10xxxxxx    10xxxxxx    10xxxxxx     */
    
    //@SuppressWarnings("cast")
    static public int toUTF8(char ch)
    {
        if ( ch != 0 && ch < 127 ) return (int)ch ;
        if ( ch == 0 ) return 0xC080 ;
        
        if ( ch <= 0x07FF ) ; 
        if ( ch <= 0xFFFF ) ;
        
        // Not java, whare chars are 16 bit.
        if ( ch <= 0x1FFFFF ) ; 
        if ( ch <= 0x3FFFFFF ) ; 
        if ( ch <= 0x7FFFFFFF ) ;
        
        return -1 ;
        
    }
    
    /** Encode a char as UTF-8, using Java's built-in encoders - may be slow */
    static public int toUTF8_test(char ch)
    {
        byte[] bytes = new byte[4] ;
        ByteBuffer bb = ByteBuffer.wrap(bytes) ;
        String s = ""+ch ;
        Bytes.toByteBuffer(s, bb) ;
        int x = Bytes.getInt(bytes) ;
        return x ;
    }
    
    static public char fromUTF8(int x)
    {
        //char[] chars = Character.toChars(ch) ;
        return ' ' ;
    }

    static public char fromUTF8_test(int x)
    {
        char[] chars = Character.toChars(x) ;
        return chars[0] ;
    }
    
    /** Create a UTF-8 encoder */
    public static CharsetEncoder createEncoder() { return charsetUTF8.newEncoder() ; }
    /** Create a UTF-8 decoder */
    public static CharsetDecoder createDecoder() { return charsetUTF8.newDecoder() ; }

    /** Get a UTF-8 encoder from the pool (null if pool empty) */ 
    public static CharsetEncoder getEncoder() { return encoders.get() ; }
    /** Add/return a UTF-8 encoder to the pool */
    public static void putEncoder(CharsetEncoder encoder) { encoders.put(encoder) ; }

    /** Get a UTF-8 decoder from the pool (null if pool empty) */ 
    public static CharsetDecoder getDecoder() { return decoders.get() ; }
    /** Add/return a UTF-8 decoder to the pool */
    public static void putDecoder(CharsetDecoder decoder) { decoders.put(decoder) ; }
    
    /** Allocate a CharsetEncoder, creating as necessary */
    public static CharsetEncoder allocEncoder()
    {
        CharsetEncoder enc = Chars.getEncoder();
        // Blocking finite Pool - does not happen.
        // Plain Pool (sync wrapped) - might - allocate an extra one. 
        if ( enc == null ) 
            enc = Chars.createEncoder() ;
        return enc ;
    }
    /** Deallocate a CharsetEncoder, may increase pool size */
    public static void deallocEncoder(CharsetEncoder enc) { putEncoder(enc) ; }
        
    /** Allocate a CharsetDecoder, creating as necessary */
    public static CharsetDecoder allocDecoder()
    {
        CharsetDecoder dec = Chars.getDecoder();
        // Blocking finite Pool - does not happen.
        // Plain Pool (sync wrapped) - might - allocate an extra one. 
        if ( dec == null ) 
            dec = Chars.createDecoder() ;
        return dec ;
    }
    /** Deallocate a CharsetDecoder, may increase pool size */
    public static void deallocDecoder(CharsetDecoder dec) { putDecoder(dec) ; }
    

    public static void encodeAsHex(StringBuilder buff, char marker, char ch)
    {
        if ( ch < 256 )
        {
            buff.append(marker) ;
            int lo = ch & 0xF ;
            int hi = (ch >> 4) & 0xF ;
            buff.append(Chars.hexDigitsUC[hi]) ;                
            buff.append(Chars.hexDigitsUC[lo]) ;
            return ;
        }
        int n4 = ch & 0xF ;
        int n3 = (ch >> 4) & 0xF ;
        int n2 = (ch >> 8) & 0xF ;
        int n1 = (ch >> 12) & 0xF ;
        buff.append(marker) ;
        buff.append(Chars.hexDigitsUC[n1]) ;                
        buff.append(Chars.hexDigitsUC[n2]) ;
        buff.append(marker) ;
        buff.append(Chars.hexDigitsUC[n3]) ;
        buff.append(Chars.hexDigitsUC[n4]) ;
        
    }

    /** End of file - not a Unicode codepoint */
    public static final int EOF             = -1 ;
    // BOM  : U+FEFF encoded in bytes as xEF,0xBB,0xBF
    public static final char BOM            = 0xFEFF ;  
    
    /** undefined character (exact meaning depends on use) - not a Unicode codepoint */
    public static final int  UNSET           =  -2 ;
    public static final char NL              = '\n' ;
    public static final char CR              = '\r' ;
    
    public static final char CH_ZERO         =  (char)0 ;

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
    public static final char CH_AMPHERSAND   = '&' ;
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
    public static final byte B_AMPHERSAND    = '&' ;
    public static final byte B_QMARK         = '?' ;
    public static final byte B_HASH          = '#' ;
    public static final byte B_PLUS          = '+' ;
    public static final byte B_MINUS         = '-' ;
    public static final byte B_SLASH         = '/' ;
    public static final byte B_RSLASH        = '\\' ;
    

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