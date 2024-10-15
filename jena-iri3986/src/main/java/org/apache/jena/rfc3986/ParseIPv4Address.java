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

package org.apache.jena.rfc3986;

import static org.apache.jena.rfc3986.Chars3986.charAt;
import static org.apache.jena.rfc3986.ParseErrorIRI3986.parseError;


/**
 * Parse IPv4 addresses
 */

public class ParseIPv4Address {

//    IPv4address   = dec-octet "." dec-octet "." dec-octet "." dec-octet
//
//            dec-octet     = DIGIT                 ; 0-9
//                          / %x31-39 DIGIT         ; 10-99
//                          / "1" 2DIGIT            ; 100-199
//                          / "2" %x30-34 DIGIT     ; 200-249
//                          / "25" %x30-35          ; 250-255
//

    /** Check an IPv6 address (including any delimiting []) */
    public static void checkIPv4(CharSequence string) {
        checkIPv4(string, 0, string.length());
    }

    public static void checkIPv4(CharSequence string, int start, int end) {
        int length = string.length();
        if ( start < 0 || end < 0 || end > length )
            throw new IllegalArgumentException();
        if ( length == 0 || start >= end )
            throw parseError(string, "Empty IPv4 address");
        parseIPv4(string, start, end);
    }

    private static int parseIPv4(CharSequence string, int start, int end) {
        int q = ipv4(string, start, end);
        if ( q != end )
            throw parseError(string, "IPV4 address too long (final dec-octet too long)");
        return q;
    }

    /** Match exactly an IPv4 address. */
    /*package*/ static int ipv4(CharSequence string, int start, int end) {
        // Used by ParseIPv6Address
        int p = start;
        // 3* "NNN." then "NNN"
        for ( int i = 0 ; i < 4 ; i++ ) {
            int x = ipv4_digits(string, p, end);
            if ( x < 0 || x == p )
                throw parseError(string, "Bad IPv4 address (no digits)");
            // Check for in 0-255.
            if ( x-p == 3 )
                checkIPv4Value(string, p);
            if ( i != 3 ) {
                char ch = charAt(string, x);
                if ( ch != '.' )
                    throw parseError(string, "Bad IPv4 address (dot not found after 3 digits)");
                x++;
            }
            p = x;
        }
        return p;
    }

    /** 1 to 3 digits. */
    private static int ipv4_digits(CharSequence string, int start, int end) {
        int p = start;
        for (int i = 0 ; i < 3 ; i++ ) {
            if ( p+i >= string.length() )
                return p+i;
            char ch = charAt(string, p+i);
            if ( ! Chars3986.range(ch, '0', '9') )
                return p+i;
        }
        // 3 digits
        return p+3;
    }

    private static void checkIPv4Value(CharSequence string, int p) {
        // 3 digits. Check for 255. Rather that "parse", we calculate the value.
        // Known to be ASCII digits.
        char ch1 = charAt(string, p);
        char ch2 = charAt(string, p+1);
        char ch3 = charAt(string, p+2);
        int v = (ch1-'0')*100 + (ch2-'0')*10 + (ch3-'0');
        if ( v > 255 )
            throw parseError(string, "IPv4 number out of range 0-255.");
    }

    /** Look at the end of the character sequence for an IPv4 address. */
    private static int peekForIPv4(CharSequence string, int start, int end) {
        //IPv4address   = dec-octet "." dec-octet "." dec-octet "." dec-octet
        //dec-octet     = DIGIT                 ; 0-9
        //              / %x31-39 DIGIT         ; 10-99
        //              / "1" 2DIGIT            ; 100-199
        //              / "2" %x30-34 DIGIT     ; 200-249
        //              / "25" %x30-35          ; 250-255
        boolean isIPv4 = false;
        int countDot = 0;
        int firstDot = 0;
        // Max length of an IPv4 address is  3+1+3+1+3+1+3 = 15.

        int p = -1;
        for ( int i = 0; i < 15 ; i++ ) {
            p = end-i-1;
            if ( p < 0 )
                break;
            char ch = charAt(string, p);
            if ( ch == '.' ) {
                isIPv4 = true;
                countDot ++;
                firstDot = p;
                if ( countDot == 3 )
                    // Yes!
                    break;
            } else if ( ! Chars3986.range(ch,'0', '9') )
                break;

        }

        if ( ! isIPv4 )
            return -1;
        if ( countDot != 3 )
            throw parseError(string, "Malformed IPv4 address as part of IPv6 []");

        // Move to start of IPv4 address. => function.
        for ( int i = 0 ; i < 3 ; i++ ) {
            p = firstDot-i-1;
            if ( p < 0 )
                break;
            char ch = charAt(string, p);
            if ( ! Chars3986.range(ch,'0', '9') )
                break;
        }

        // check p .
        char ch = charAt(string, p-1);
        if ( ch != ':' )
            throw parseError(string, "Malformed IPv4 address as part of IPv6; can't find ':' separator");
        // Location of last :
        return p;
    }
}
