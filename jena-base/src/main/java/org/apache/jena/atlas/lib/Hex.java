/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.atlas.lib;

import static java.lang.String.format;

import org.apache.jena.atlas.AtlasException ;

/** Working in hex ... */
public class Hex
{
    /** Format a long value into a byte array as hex digits */
    public static void formatUnsignedLongHex(byte[] b, int start, long value, int width) {
        int idx = start;
        for ( int i = 0 ; i < width ; i++) {
            int j = 64-(i*4);
            long x = unpack(value, j-4, j);
            int d = (int)x;
            byte ch = Bytes.hexDigitsUC[d] ;
            b[idx] = ch;
            idx++;
        }
    }

    private static final int LongLen = Long.SIZE ;
    private static final long unpack(long bits, int start, int finish) {
        // BitsLong without the checking.
        // Remove top bits by moving up.  Clear bottom bits by them moving down.
        return (bits<<(LongLen-finish)) >>> ((LongLen-finish)+start) ;
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
    public static int hexDigitToInt(int c, int marker) {
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
            int k = hexDigitToInt(ch, -1);
            if ( k == -1 )
                throw new AtlasException("Illegal hex escape: "+ch) ;
            x = (x<<4)+k ;
        }
        return x ;
    }

    public static boolean isHexChar(char ch) {
        return hexDigitToInt(ch, -1) >= 0;
    }
}
