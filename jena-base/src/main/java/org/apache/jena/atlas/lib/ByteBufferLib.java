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

package org.apache.jena.atlas.lib ;

import static java.lang.System.arraycopy ;

import java.io.PrintStream ;
import java.nio.ByteBuffer ;
import java.util.Arrays ;

public class ByteBufferLib {
    public static boolean allowArray = true ;

    private ByteBufferLib() {}

    public static void fill(ByteBuffer bb, byte v) {
        fill(bb, bb.position(), bb.limit(), v) ;
    }

    public static void fill(ByteBuffer bb, int start, int finish, byte v) {
        for ( int i = start ; i < finish ; i++ )
            bb.put(i, v) ;
    }

    public static String details(ByteBuffer byteBuffer) {
        // Like ByteBuffer.toString but without the class.
        return "[pos=" + byteBuffer.position() + " lim=" + byteBuffer.limit() + " cap=" + byteBuffer.capacity() + "]" ;
    }

    public static void print(ByteBuffer byteBuffer) {
        print(System.out, byteBuffer) ;
    }

    public static void print(PrintStream out, ByteBuffer byteBuffer) {
        byteBuffer = byteBuffer.duplicate() ;

        out.printf("ByteBuffer[pos=%d lim=%d cap=%d]", byteBuffer.position(), byteBuffer.limit(), byteBuffer.capacity()) ;

        // Print bytes.
        int i = 0 ;
        int maxBytes = 3 * 20 ;
        for ( ; i < maxBytes && i < byteBuffer.limit() ; i++ ) {
            if ( i % 20 == 0 )
                out.println() ;
            out.printf(" 0x%02X", byteBuffer.get(i)) ; // Does not move position
        }
        if ( i < byteBuffer.limit() ) {
            if ( i % 24 == 0 )
                out.println() ;
            out.print(" ...") ;
        }
        // Print as 4-byte ints
        // int maxSlots = 8 ;
        // int i = 0 ;
        // for ( ; i < maxSlots && 4*i < byteBuffer.limit() ; i++ )
        // out.printf(" 0x%04X", byteBuffer.getInt(4*i)) ;
        // if ( i < maxSlots )
        // out.print(" ...") ;
        out.println() ;
    }

    public static boolean sameValue(ByteBuffer bb1, ByteBuffer bb2) {
        if ( bb1.capacity() != bb2.capacity() )
            return false ;

        for ( int i = 0 ; i < bb1.capacity() ; i++ )
            if ( bb1.get(i) != bb2.get(i) )
                return false ;
        return true ;
    }

    /**
     * Copy of a ByteBuffer - the contents are copied (unlike
     * ByteBuffer.duplicate)
     */
    final public static ByteBuffer duplicate(ByteBuffer bb) {
        ByteBuffer bb2 = ByteBuffer.allocate(bb.limit() - bb.position()) ;
        int x = bb.position() ;
        bb2.put(bb) ;
        bb.position(x) ;

        bb2.position(0) ;
        bb2.limit(bb2.capacity()) ;
        return bb2 ;
    }

    /** Copy from a byte buffer */
    final public static byte[] bb2array(ByteBuffer bb, int start, int finish) {
        byte[] b = new byte[finish - start] ;
        bb2array(bb, start, finish, b) ;
        return b ;
    }

    private static void bb2array(ByteBuffer bb, int start, int finish, byte[] b) {
        for ( int j = 0, i = start ; i < finish ; i++ )
            b[j] = bb.get(i) ;
    }

    // For non-array versions : beware of overlaps.
    final public static void bbcopy(ByteBuffer bb, int src, int dst, int length, int slotLen) {
        if ( src == dst )
            return ;

        if ( allowArray && bb.hasArray() ) {
            acopyArray(bb, src, dst, length, slotLen) ;
            return ;
        }

        if ( src < dst )
            bbcopy1(bb, src, dst, length, slotLen) ;
        else
            bbcopy2(bb, src, dst, length, slotLen) ;
    }

    private final static void bbcopy1(ByteBuffer bb, int src, int dst, int length, int slotLen) {
        int bDst = dst * slotLen ;
        int bSrc = src * slotLen ;
        int bLen = length * slotLen ;
        // src < dst so top dst is not in the overlap : work backwards
        for ( int i = bLen - 1 ; i >= 0 ; i-- )
            bb.put(bDst + i, bb.get(bSrc + i)) ;
    }

    private final static void bbcopy2(ByteBuffer bb, int src, int dst, int length, int slotLen) {
        int bDst = dst * slotLen ;
        int bSrc = src * slotLen ;
        int bLen = length * slotLen ;
        // src > dst so dst[0] is not in the overlap
        for ( int i = 0 ; i < bLen ; i++ )
            bb.put(bDst + i, bb.get(bSrc + i)) ;
    }

    public final static void bbcopy(ByteBuffer bb1, int src, ByteBuffer bb2, int dst, int length, int slotLen) {
        // Assume bb1 and bb2 are different and do not overlap.
        if ( allowArray && bb1.hasArray() && bb2.hasArray() ) {
            acopyArray(bb1, src, bb2, dst, length, slotLen) ;
            return ;
        }
        // One or both does not have an array.

        int bSrc = src * slotLen ;
        int bDst = dst * slotLen ;
        int bLen = length * slotLen ;

        for ( int i = 0 ; i < bLen ; i++ )
            bb2.put(bDst + i, bb1.get(bSrc + i)) ;
    }

    final public static void bbfill(ByteBuffer bb, int fromIdx, int toIdx, byte fillValue, int slotLen) {
        if ( allowArray && bb.hasArray() ) {
            afillArray(bb, fromIdx, toIdx, fillValue, slotLen) ;
            return ;
        }

        int bStart = fromIdx * slotLen ;
        int bFinish = toIdx * slotLen ;

        for ( int i = bStart ; i < bFinish ; i++ )
            bb.put(i, fillValue) ;
    }

    // To ArrayOps?

    final private static void acopyArray(ByteBuffer bb, int src, int dst, int length, int slotLen) {
        byte[] b = bb.array() ;

        int offset = bb.arrayOffset() ;

        int bSrc = src * slotLen ;
        int bDst = dst * slotLen ;
        int bLen = length * slotLen ;

        arraycopy(b, offset + bSrc, b, offset + bDst, bLen) ;
    }

    final private static void acopyArray(ByteBuffer bb1, int src, ByteBuffer bb2, int dst, int length, int slotLen) {
        byte[] b1 = bb1.array() ;
        byte[] b2 = bb2.array() ;
        int offset1 = bb1.arrayOffset() ;
        int offset2 = bb2.arrayOffset() ;

        int bSrc = src * slotLen ;
        int bDst = dst * slotLen ;
        int bLen = length * slotLen ;

        arraycopy(b1, offset1 + bSrc, b2, offset2 + bDst, bLen) ;
    }

    final private static void afillArray(ByteBuffer bb, int fromIdx, int toIdx, byte fillValue, int slotLen) {
        int offset = bb.arrayOffset() ;
        int bStart = fromIdx * slotLen ;
        int bFinish = toIdx * slotLen ;
        Arrays.fill(bb.array(), bStart + offset, bFinish + offset, fillValue) ;
    }
}
