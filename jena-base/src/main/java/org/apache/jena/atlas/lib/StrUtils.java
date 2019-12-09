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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream ;
import static java.util.stream.Collectors.toList;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Some functions that act on strings */
public class StrUtils //extends StringUtils
{
    private StrUtils() {}
    
    /** strjoin with a newline as the separator */
    public static String strjoinNL(String... args) {
        return String.join("\n", args);
    }

    /** strjoin with a newline as the separator */
    public static String strjoinNL(List<String> args) {
        return String.join("\n", args);
    }

    /** 
     * Concatentate strings, using a separator
     * 
     * This function will be removed - do not use.
     * 
     * @deprecated Prefer String.join(sep, args)
     */
    @Deprecated
    public static String strjoin(String sep, String... args) {
        return String.join(sep, args);
    }

    /**
     * Concatentate strings, using a separator
     * 
     * This function will be removed - do not use.
     * 
     * @deprecated Prefer String.join(sep, args)
     */
    @Deprecated
    public static String strjoin(String sep, List<String> args) {
        return String.join(sep, args);
    }

    public static final int CMP_GREATER  = +1 ;
    public static final int CMP_EQUAL    =  0 ;
    public static final int CMP_LESS     = -1 ;
    
    public static final int CMP_UNEQUAL  = -9 ;
    public static final int CMP_INDETERMINATE  = 2 ;
    
    public static int strCompare(String s1, String s2) {
        // Value is the difference of the first differing chars
        int x = s1.compareTo(s2) ;
        if ( x < 0 ) return CMP_LESS ;
        if ( x > 0 ) return CMP_GREATER ;
        if ( x == 0 ) return CMP_EQUAL ;
        throw new InternalErrorException("String comparison failure") ;
    }
    
    public static int strCompareIgnoreCase(String s1, String s2) {
        // x is the difference of the first differing chars
        int x = s1.compareToIgnoreCase(s2) ;
        if ( x < 0 ) return CMP_LESS ;
        if ( x > 0 ) return CMP_GREATER ;
        if ( x == 0 ) return CMP_EQUAL ;
        throw new InternalErrorException("String comparison failure") ;
    }

    public static byte[] asUTF8bytes(String s) {
        return s.getBytes(UTF_8) ; 
    }

    public static String fromUTF8bytes(byte[] bytes) {
        return new String(bytes, UTF_8) ; 
    }
    
    /**
     * @param x
     * @return &lt;null&gt; if x == null, otherwise, x.toString()
     */
    public static String str(Object x) {
        return Objects.toString(x, "<null>");
    }

    /** Split but also trim whiespace. */
    public static String[] split(String s, String splitStr) {
        return stream(s.split(splitStr)).map(String::trim).toArray(String[]::new) ;
    }
    
    /**
     * Does one string contain another string?
     * 
     * @param str1
     * @param str2
     * @return true if str1 contains str2
     */
    public final static boolean contains(String str1, String str2) {
        return str1.contains(str2) ;
    }
    
    public final static String replace(String string, String target, String replacement) {
        return string.replace(target, replacement) ;
    }

    public static String substitute(String str, Map<String, String> subs) {
        for ( Map.Entry<String, String> e : subs.entrySet() ) {
            String param = e.getKey() ;
            if ( str.contains(param) ) 
                str = str.replace(param, e.getValue()) ;
        }
        return str ;
    }
    
    public static String strform(Map<String, String> subs, String... args) {
        return substitute(strjoinNL(args), subs) ;
    }

    public static String chop(String x) {
        return x.isEmpty() ? x : x.substring(0, x.length() - 1) ;
    }

    public static String noNewlineEnding(String x) {
        while (x.endsWith("\n") || x.endsWith("\r"))
            x = StrUtils.chop(x) ;
        return x ;
    }
    
    public static List<Character> toCharList(String str) {
        return str.codePoints().mapToObj(i -> (char) i).map(Character::new)
                  .collect(toList());
    }
    
    // ==== Encoding and decoding strings based on a marker character (e.g. %)
    // and then the hexadecimal representation of the character.  
    // Only characters 0-255 can be encoded.
    
    /**
     * Encode a string using hex values e.g. %20
     * 
     * @param str String to encode
     * @param marker Marker character
     * @param escapees Characters to encode (must include the marker)
     * @return Encoded string (returns input object if no change)
     */
    public static String encodeHex(String str, char marker, char[] escapees) {
        // We make a first pass to see if there is anything to do.
        // This is assuming
        // (1) the string is shortish (e.g. fits in L1)
        // (2) necessary escaping is not common

        int N = str.length() ;
        int idx = 0 ;
        // Scan stage.
        for ( ; idx < N ; idx++ ) {
            char ch = str.charAt(idx) ;
            if ( Chars.charInArray(ch, escapees) )
                break ;
        }
        if ( idx == N )
            return str ;

        // At least one char to convert
        StringBuilder buff = new StringBuilder() ;
        buff.append(str, 0, idx) ;  // Insert first part.
        for ( ; idx < N ; idx++ ) {
            char ch = str.charAt(idx) ;
            if ( Chars.charInArray(ch, escapees) ) {
                Chars.encodeAsHex(buff, marker, ch) ;
                continue ;
            }
            buff.append(ch) ;
        }
        return buff.toString() ;
    }

    /**
     * Decode a string using marked hex values e.g. %20
     * 
     * @param str String to decode : characters should be ASCII (<127)
     * @param marker The marker character
     * @return Decoded string (returns input object on no change)
     */
    public static String decodeHex(String str, char marker) {
        if ( str.indexOf(marker) < 0 ) 
            return str;
        // This function does work if input str is not pure ASCII.
        // The tricky part is if an %-encoded part is a UTF-8 sequence.
        // An alternative algorithm is to work in chars from the string, and handle
        // that case %-endocded when value has the high bit set.
        byte[] strBytes = StrUtils.asUTF8bytes(str);
        final int N = strBytes.length;
        // Max length
        byte[] bytes = new byte[strBytes.length];
        int i = 0;
        for ( int j = 0 ; j < N ; j++ ) {
            byte b = strBytes[j];
            if ( b != marker ) {
                bytes[i++] = b;
                continue;
            }
            // Marker.
            char hi = str.charAt(j + 1);
            char lo = str.charAt(j + 2);
            j += 2;
            int x1 = hexDecode(hi);
            int x2 = hexDecode(lo);
            int ch2 = (hexDecode(hi) << 4 | hexDecode(lo));
            bytes[i++] = (byte)ch2;
        }
        return new String(bytes, 0, i, StandardCharsets.UTF_8); 
    }

    // Encoding is table-driven but for decode, we use code.
    static private int hexDecode(char ch) {
        if ( ch >= '0' && ch <= '9' )
            return ch - '0';
        if ( ch >= 'A' && ch <= 'F' )
            return ch - 'A' + 10;
        if ( ch >= 'a' && ch <= 'f' )
            return ch - 'a' + 10;
        return -1 ;
    }

    public static String escapeString(String x) {
        return EscapeStr.stringEsc(x);
    }

    public static String unescapeString(String x) {
        return EscapeStr.unescapeStr(x);
    }
}
