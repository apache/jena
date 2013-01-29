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

package org.apache.jena.atlas.lib;

import java.nio.charset.Charset ;
import java.nio.charset.CharsetDecoder ;
import java.nio.charset.CharsetEncoder ;
import java.nio.charset.CodingErrorAction ;

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
    
    // Pools for encoders/decoder.
    // Better? use a ThreadLocal.
    // Initial pool size. Any additional encoder/decoder are later
    // placed in the pool - it's an infinite, reusing, growing pool.
    
    // Better?  If so, use these!
    
    private static final ThreadLocal<CharsetEncoder> threadCharsetEncoder =
        new ThreadLocal<CharsetEncoder>() {
        @Override protected CharsetEncoder initialValue() {
            return createEncoder() ;
        }
    };

    private static final ThreadLocal<CharsetDecoder> threadCharsetDecoder =
        new ThreadLocal<CharsetDecoder>() {
        @Override protected CharsetDecoder initialValue() {
            return createDecoder() ;
        }
    };

    /** Return a per-thread CharsetEncoder */ 
    public static CharsetEncoder getThreadEncoder() { return threadCharsetEncoder.get() ; }

    /** Return a per-thread CharsetDecoder */ 
    public static CharsetDecoder getThreadDecoder() { return threadCharsetDecoder.get() ; }

    private static final int PoolSize = 2 ;
    private static Pool<CharsetEncoder> encoders = PoolSync.create(new PoolBase<CharsetEncoder>()) ;
    private static Pool<CharsetDecoder> decoders = PoolSync.create(new PoolBase<CharsetDecoder>()) ;

    static {
        ThreadLocal<CharsetEncoder> t ;
        
        // Fill the pool.
        for ( int i = 0 ; i < PoolSize ; i++ )
        {
            putEncoder(createEncoder()) ;
            putDecoder(createDecoder()) ;
        }
    }
    
    /** Create a UTF-8 encoder */
    public static CharsetEncoder createEncoder()    { return charsetUTF8.newEncoder() ; }
    /** Create a UTF-8 decoder */
    public static CharsetDecoder createDecoder()    { return charsetUTF8.newDecoder() ; }

    /** Get a UTF-8 encoder from the pool (null if pool empty) */ 
    public static CharsetEncoder getEncoder()       { return encoders.get() ; }
    /** Add/return a UTF-8 encoder to the pool */
    public static void putEncoder(CharsetEncoder encoder) { encoders.put(encoder) ; }

    /** Get a UTF-8 decoder from the pool (null if pool empty) */ 
    public static CharsetDecoder getDecoder()       { return decoders.get() ; }
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
        enc
          .onMalformedInput(CodingErrorAction.REPLACE)
          .onUnmappableCharacter(CodingErrorAction.REPLACE)
          .reset() ;
        
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
        dec
          .onMalformedInput(CodingErrorAction.REPLACE)
          .onUnmappableCharacter(CodingErrorAction.REPLACE)
          .reset() ;
        
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
    public static final char BSPACE          = '\b' ;
    
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
    public static final char CH_DASH         = '-' ; // Alt name
    public static final char CH_SLASH        = '/' ;
    public static final char CH_RSLASH       = '\\' ;
    public static final char CH_PERCENT      = '%' ;
    
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
    public static final byte B_DASH          = '-' ; // Alt name
    public static final byte B_SLASH         = '/' ;
    public static final byte B_RSLASH        = '\\' ;
    public static final byte B_PERCENT       = '%' ;
    

}
