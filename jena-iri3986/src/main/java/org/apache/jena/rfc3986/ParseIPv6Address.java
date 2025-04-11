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

/**
 * Parse IPv6 addresses
 */

public class ParseIPv6Address {
    /*
<pre>
    IP-literal    = "[" ( IPv6address / IPvFuture  ) "]"

    IPvFuture     = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )

    IPv6address   =                            6( h16 ":" ) ls32
                  /                       "::" 5( h16 ":" ) ls32
                  / [               h16 ] "::" 4( h16 ":" ) ls32
                  / [ *1( h16 ":" ) h16 ] "::" 3( h16 ":" ) ls32
                  / [ *2( h16 ":" ) h16 ] "::" 2( h16 ":" ) ls32
                  / [ *3( h16 ":" ) h16 ] "::"    h16 ":"   ls32
                  / [ *4( h16 ":" ) h16 ] "::"              ls32
                  / [ *5( h16 ":" ) h16 ] "::"              h16
                  / [ *6( h16 ":" ) h16 ] "::"

    h16           = 1*4HEXDIG
    ls32          = ( h16 ":" h16 ) / IPv4address
</pre>
    "HEXDIG" is '0' to '9 , 'A' to 'F' together with lower case, (RFC3986 - the normalized form is uppercase).
     */

    // We parse an IPv6 by
    //   look for repeated "(h16 ':')",
    //   look for another ":",
    //   look for repeated (h16 ':')
    //   look for a IPv4 address or another h16
    //   check the whole char sequence was parsed
    //   check the numbers of h16 units does not exceed the grammar restrictions.

    // IPv6address   =                            6( h16 ":" ) ls32
    //               /                       "::" 5( h16 ":" ) ls32
    //               / [               h16 ] "::" 4( h16 ":" ) ls32
    //               / [ *1( h16 ":" ) h16 ] "::" 3( h16 ":" ) ls32
    //               / [ *2( h16 ":" ) h16 ] "::" 2( h16 ":" ) ls32
    //               / [ *3( h16 ":" ) h16 ] "::"    h16 ":"   ls32
    //               / [ *4( h16 ":" ) h16 ] "::"              ls32
    //               / [ *5( h16 ":" ) h16 ] "::"              h16
    //               / [ *6( h16 ":" ) h16 ] "::"
    // h16           = 1*4HEXDIG
    // ls32          = ( h16 ":" h16 ) / IPv4address

    // RFC 6874 adds:
    // IP-literal = "[" ( IPv6address / IPv6addrz / IPvFuture ) "]"
    //
    // ZoneID = 1*( unreserved / pct-encoded )
    //
    // IPv6addrz = IPv6address "%25" ZoneID

    /** Check an IPv6 address (including any delimiting []) */
    public static void checkIPv6(CharSequence string) {
        checkIPv6(string, 0, string.length());
    }

    public static void checkIPv6(CharSequence string, int start, int end) {
        int length = string.length();
        if ( start < 0 || end < 0 || end > length )
            throw new IllegalArgumentException();
        if ( length == 0 || start >= end )
            throw ParseErrorIRI3986.parseError(string, "Empty IPv6 address");
        parseIPv6(string, start, end);
    }

    private static int parseIPv6(CharSequence string, int start, int end) {
        if ( charAt(string, start) != '[' || charAt(string, end-1) != ']' )
            throw ParseErrorIRI3986.parseError(string, "IPv6 (or later) address not properly delimited");
        // end must be > start+1 by the above and checkIPv6 so no risk of missing here.
        if ( charAt(string, start+1) == 'v' ) {
            // IPvFuture  = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )
            return parseIPFuture(string, start+2, end-1);
        }
        int idx =  parseIPv6Sub(string, start+1, end-1);
        // Optional :: %25zone
        idx = parseIPv6OptionalZone(string, idx, end-1);

        // Did parsing end at the last character before the ']'?
        if ( idx != end-1 )
            throw ParseErrorIRI3986.parseError(string, "Bad end of IPv6X address");
        return idx;
    }

    /** Parse option zoneId after IPv6 address. RFC 6874 */
    private static int parseIPv6OptionalZone(CharSequence string, int idx, int end) {
        if ( idx >= end )
            return idx;
        char chPercent = string.charAt(idx);
        if ( chPercent == '%' ) {
            // Check '2', '5'
            if ( idx+3 >= end )
                throw ParseErrorIRI3986.parseError(string, "Bad IPv6 zone id");
            char ch1 = string.charAt(idx+1);
            char ch2 = string.charAt(idx+2);
            if ( ch1 != '2' || ch2 != '5' )
                throw ParseErrorIRI3986.parseError(string, "Bad IPv6 zone id (must be '%25...'");
            idx = idx + 3;
            // ZoneID = 1*( unreserved / pct-encoded )
            int zIdx = idx;
            for ( ; zIdx < end ; zIdx++ ) {
                char ch =  string.charAt(zIdx);
                if ( Chars3986.unreserved(ch) )
                    continue;
                if ( Chars3986.isPctEncoded(ch, string, zIdx) )
                    continue;
                throw ParseErrorIRI3986.parseError(string, "Bad character in IPv6 zone id");
            }
            if ( zIdx - idx < 1 )
                throw ParseErrorIRI3986.parseError(string, "No IPv6 zone id after '%25'");
            idx = zIdx;
        }
        return idx;
    }

    private static int parseIPFuture(CharSequence string, int start, int end) {
        int p = start;
        if ( p >= end )
            throw ParseErrorIRI3986.parseError(string, "Short IPFuture");
        char ch = string.charAt(p);
        if ( ! Chars3986.isHexDigit(ch) )
            throw ParseErrorIRI3986.parseError(string, "IPFuture: no version hexdigit");
        p++;
        ch = string.charAt(p);
        if ( ch != '.' )
            throw ParseErrorIRI3986.parseError(string, "IPFuture: no dot after version hexdigit");
        p++;
        // One or more.
        while (p < end) {
            ch = string.charAt(p);
            if ( ch == ']' )
                break;
            if ( ! Chars3986.unreserved(ch) && !Chars3986.subDelims(ch) && ch != ':' )
                break;
            p++;
        }
        if ( p != end )
            // Only one ']' at index end.
            throw ParseErrorIRI3986.parseError(string, "IPFuture: extra ']'");
        return p;
    }

    private static int parseIPv6Sub(CharSequence string, int start, int end) {
        // start-end Without "[...]";
        int p = start;

        // Before the ::
        int h16c1 = -1;
        int h16c2 = -1;
        int h16c = 0;

        //starting ::
        boolean b = LibParseIRI.peekFor(string, p, ':',  ':');
        if ( b ) {
            h16c1 = h16c;
            h16c = 0 ;
            p += 2;
        }

        // Move forward over h16:
        for (;;) {
            int x = ipv6_h16(string, p, end);
            if ( x == p )
                break;
            if ( x >= end )
                break;
            h16c++;
            char ch = charAt(string, x);
            if ( ch == ':' ) { // "::"
                //System.out.printf("h16 %d\n", h16c);
                h16c1 = h16c;
                h16c = 0 ;
                x++;
            }
            p = x;
        }
        if ( h16c1 >= 0 )
            // After ::
            h16c2 = h16c;
        else
            h16c1 = h16c;

        //h16c2 == -1 => Didn't see ::
        //System.out.printf("(%d, %d)\n", h16c1, h16c2);

        // Lookahead
        boolean IPv4 = false;
        for ( int i = 0 ; i < 4 ; i++ ) {
            int z = p  + i ;
            if ( z >= end )
                // End.
                break;
            char ch = charAt(string, z);
            if ( Chars3986.range(ch, 'a', 'f') || Chars3986.range(ch, 'A', 'F') )
                break;
            if ( ch == '.' ) {
                IPv4 = true;
                break;
            }
            // Unsure yet - loop
        }
        if ( IPv4 ) {
            // Seen "NNN."

            // ":" Validity rule.
            if ( h16c2 == -1 ) {
                // h16c1 must be 6
                if ( h16c1 != 6 )
                    throw ParseErrorIRI3986.parseError(string, "Malformed IPv6 address with IPv4 part [case 1]");
            } else {
                // h16c1+h16c2 <= 4
                if ( h16c1+h16c2 > 4 )
                    throw ParseErrorIRI3986.parseError(string, "Malformed IPv6 address with IPv4 part [case 2]");
            }
            int x = ipv4(string, p, end);
            p = x ;
//            if ( p != end )
//                throw ErrorIRI3986.parseError(string, "Bad end of IPv4 address");
        } else {
            // ":" Validity rule.
            if ( h16c2 == -1 ) {
                // h16c1 must be 7
                if ( h16c1 != 7 )
                    throw ParseErrorIRI3986.parseError(string, "Malformed IPv6 address [case 1]");
            } else {
                // h16c1+h16c2 <= 5
                // or h16c1 <= 6, and h16c2 = 0
                if ( h16c2 == 0 ) {
                    if ( h16c1 > 6 )
                        throw ParseErrorIRI3986.parseError(string, "Malformed IPv6 address [case 2]");
                }
                else if ( h16c1+h16c2 > 5 )
                    throw ParseErrorIRI3986.parseError(string, "Malformed IPv6 address [case 3]");
            }
            int x = ipv6_hex4(string, p, end);
            p = x;
//            if ( p != end )
//                throw ErrorIRI3986.parseError(string, "Bad end of IPv6 address");
        }
        return p;
    }

    // (h16 ":")*
    // Returns index of just after the ":"
    // Does not accept h16 , no ":"
    private static int ipv6_h16(CharSequence string, int start, int end) {
        int p = start;
        int x = ipv6_hex4(string, p, end);
        if ( x < 0 )
            throw ParseErrorIRI3986.parseError(string, "hex4 error at "+p);
        if ( x == p )
            // No progress.
            return p;
        if ( x >= end )
            // No ":"
            return p;
        char ch1 = string.charAt(x);
        if ( ch1 != ':' )
            return p;
        x++;
        // New "start".
        p = x;
        return p;
    }

    /** h16 - 1 to 4 hex digits.
     * Return character position after the digits or the start position if no hex digits seen.
     * That is, it may make no progress so in effect it is lookign for 0 to 4 hex digits.
     */
    private static int ipv6_hex4(CharSequence string, int start, int end) {
        int p = start;
        for (int i = 0 ; i < 4 ; i++ ) {
            if ( p+i >= end )
                return p+i;
            char ch = charAt(string, p+i);
            if ( ! Chars3986.isHexDigit(ch) )
                return p+i;
        }
        return p+4;
    }

    /** Match exactly an IPv4 address. */
    private static int ipv4(CharSequence string, int start, int end) {
        return ParseIPv4Address.ipv4(string, start, end);
    }
}
