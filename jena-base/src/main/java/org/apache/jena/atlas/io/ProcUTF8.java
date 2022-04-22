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

package org.apache.jena.atlas.io;

import java.util.function.IntConsumer;

/**
 * Convert UTF-8 encoded data.
 * This class implements the "Modified UTF8" encoding rules (null {@literal ->} C0 80)
 */
public final class ProcUTF8  {
    /**
     * Convert to UTF-8, calling an action on each byte.
     * Unicode ends at 0x10FFFF (1,114,112 code points)
     * <pre>
     * Bits
     * 7    U+007F      1 to 127              0xxxxxxx
     * 11   U+07FF      128 to 2,047          110xxxxx 10xxxxxx
     * 16   U+FFFF      2,048 to 65,535       1110xxxx 10xxxxxx 10xxxxxx
     * 21   U+1FFFFF    65,536                11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
     * 26   U+3FFFFFF                         111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
     * 31   U+7FFFFFFF                        1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
     * </pre>
     */
    public static void convert(int ch, IntConsumer action) {
        if ( ch != 0 && ch <= 127 ) {
            // 7 bits
            action.accept(ch);
            return;
        }

        if ( ch == 0 ) {
            // Modified UTF-8.
            action.accept(0xC0);
            action.accept(0x80);
            return;
        }

        if ( ch <= 0x07FF ) {
            // 11 bits : 110yyyyy 10xxxxxx
            // int x1 = ( ((ch>>(11-5))&0x7) | 0xC0 ) ; outputBytes(out, x1, 2, ch) ;
            // return ;
            int x1 = (((ch >> (11 - 5)) & 0x01F) | 0xC0);
            int x2 = ((ch & 0x3F) | 0x80);
            action.accept(x1);
            action.accept(x2);
            return;
        }
        if ( ch <= 0xFFFF ) {
            // 16 bits : 1110aaaa 10bbbbbb 10cccccc
            // int x1 = ( ((ch>>(16-4))&0x7) | 0xE0 ) ; outputBytes(out, x1, 3, ch) ;
            // return ;
            int x1 = (((ch >> (16 - 4)) & 0x0F) | 0xE0);
            int x2 = (((ch >> 6) & 0x3F) | 0x80);
            int x3 = ((ch & 0x3F) | 0x80);
            action.accept(x1);
            action.accept(x2);
            action.accept(x3);
            return;
        }

        // Unicode ends at 0x10FFFF
        if ( ch <= 0x1FFFFF ) {
            // 21 bits : 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            int x1 = (((ch >> (21 - 3)) & 0x7) | 0xF0);
            outputBytes(x1, 4, ch, action);
            return;
        }

        if ( ch <= 0x3FFFFFF ) {
            // 26 bits : 111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
            int x1 = (((ch >> (26 - 2)) & 0x3) | 0xF8);
            outputBytes(x1, 5, ch, action);
            return;
        }

        if ( ch <= 0x7FFFFFFF ) {
            // 32 bits : 1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
            int x1 = (((ch >> (32 - 1)) & 0x1) | 0xFC);
            outputBytes(x1, 6, ch, action);
            return;
        }
    }

    /**
     * Write a total of {@code byteLength bytes}; first, value {@code x1}, then the
     * remaining bytes of {@code ch}.
     *
     * <pre>
     *   byte x1
     *   10xxxxxx
     *   10xxxxxx
     *   ...
     * </pre>
     */
    private static void outputBytes(int x1, int byteLength, int ch, IntConsumer action) {
        // ByteLength = 3 => 2 byteLength => shift=6 and shift=0
        action.accept(x1);
        byteLength--; // remaining bytes
        for ( int i = 0 ; i < byteLength ; i++ ) {
            // 6 Bits, loop from high to low
            int shift = 6 * (byteLength - i - 1);
            int x = (ch >> shift) & 0x3F;
            x = x | 0x80;  // 10xxxxxx
            action.accept(x);
        }
    }
}
