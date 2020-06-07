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

import static java.lang.String.format;

import org.apache.jena.atlas.AtlasException ;

/** Working in hex ... */
public class Hex
{
    // No checking, fixed width.
    public static int formatUnsignedLongHex(final byte[] b, final int start, final long value, final int width) {
        // Insert from low value end to high value end.
        int idx = start + width - 1;
        int w = width;
        long x = value;

        while (w > 0) {
            int d = (int)(x & 0xF) ;
            x = x>>>4 ; // Unsigned shift.
            byte ch = Bytes.hexDigitsUC[d] ; 
            b[idx] = ch ;
            w-- ;
            idx-- ;

            if ( x == 0 )
                break ;
        }

        if ( x != 0 )
            throw new AtlasException("formatUnsignedLongHex: overflow") ;

        while (w > 0) {
            b[idx] = '0' ;
            idx-- ;
            w-- ;
        }
        return width ;
    }
    
    // No checking, fixed width.
    public static long getLong(byte[] arr, int idx) {
        long x = 0;
        for ( int i = 0 ; i < 16 ; i++ ) {
            byte c = arr[idx];
            int v = hexByteToInt(c);
            x = x << 4 | v;
            idx++;
        }
        return x;
    }

    public static int hexByteToInt(int c) {
        if ( '0' <= c && c <= '9' )
            return c - '0';
        else if ( 'A' <= c && c <= 'F' )
            return c - 'A' + 10;
        else if ( 'a' <= c && c <= 'f' )
            return c - 'a' + 10;
        else {
            String msg = format("Bad hex char : %d (0x%02X)", c, c);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Return the value of the hex digit, or the marker value if not a hex
     * digit.
     */
    public static int hexByteToInt(int c, int marker) {
        if ( '0' <= c && c <= '9' )
            return c - '0';
        else if ( 'A' <= c && c <= 'F' )
            return c - 'A' + 10;
        else if ( 'a' <= c && c <= 'f' )
            return c - 'a' + 10;
        else
            return marker;
    }

    /** Hex string to value */
    public static int hexStringToInt(String s, int i, int len) {
        int x = 0;
        for ( int j = i ; j < i + len ; j++ ) {
            char ch = s.charAt(j);
            int k = 0;
            switch (ch) {
                case '0': k = 0 ; break ; 
                case '1': k = 1 ; break ;
                case '2': k = 2 ; break ;
                case '3': k = 3 ; break ;
                case '4': k = 4 ; break ;
                case '5': k = 5 ; break ;
                case '6': k = 6 ; break ;
                case '7': k = 7 ; break ;
                case '8': k = 8 ; break ;
                case '9': k = 9 ; break ;
                case 'A': case 'a': k = 10 ; break ;
                case 'B': case 'b': k = 11 ; break ;
                case 'C': case 'c': k = 12 ; break ;
                case 'D': case 'd': k = 13 ; break ;
                case 'E': case 'e': k = 14 ; break ;
                case 'F': case 'f': k = 15 ; break ;
                default:
                    throw new AtlasException("Illegal hex escape: "+ch) ;
            }
            x = (x<<4)+k ;
        }
        return x ;
    }
}
