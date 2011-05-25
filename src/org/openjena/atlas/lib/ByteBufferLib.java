/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import static java.lang.System.arraycopy ;

import java.io.PrintStream ;
import java.nio.ByteBuffer ;
import java.util.Arrays ;

public class ByteBufferLib
{
    public static boolean allowArray  = false ;

    private ByteBufferLib() {}
    
    public static void fill(ByteBuffer bb, byte v)
    { fill(bb, bb.position(), bb.limit(), v) ; }
    
    public static void fill(ByteBuffer bb, int start, int finish, byte v)
    {
        for ( int i = start ; i < finish ; i++ )
            bb.put(i, v) ;
    }

    public static void print(ByteBuffer byteBuffer)
    {
        print(System.out, byteBuffer) ; 
    }

    public static void print(PrintStream out, ByteBuffer byteBuffer)
    {
        byteBuffer = byteBuffer.duplicate() ;
        
        out.printf("ByteBuffer[pos=%d lim=%d cap=%d]", byteBuffer.position(), byteBuffer.limit(), byteBuffer.capacity()) ;
        
        // Print bytes.
        int i = 0 ;
        int maxBytes = 3*20 ;
        for ( ; i < maxBytes && i < byteBuffer.limit() ; i++ )
        {
            if ( i%20 == 0 )
                out.println() ;
            out.printf(" 0x%02X", byteBuffer.get(i)) ;  // Does not move position
        }
        if ( i < byteBuffer.limit() )
        {
            if ( i%24 == 0 )
                out.println() ;
            out.print(" ...") ;
        }
        // Print as 4-byte ints
//        int maxSlots = 8 ;
//        int i = 0 ;
//        for ( ; i < maxSlots && 4*i < byteBuffer.limit() ; i++ )
//            out.printf(" 0x%04X", byteBuffer.getInt(4*i)) ;
//        if ( i < maxSlots )
//            out.print(" ...") ;
        out.println();
    }
    
    public static boolean sameValue(ByteBuffer bb1, ByteBuffer bb2)
    {
        if ( bb1.capacity() != bb2.capacity() ) return false ;
        
        for ( int i = 0 ; i < bb1.capacity() ; i++ )
            if ( bb1.get(i) != bb2.get(i) ) return false ;
        return true ;
    }

    /** Copy of a ByteBuffer - the contents are copied (unlike ByteBuffer.duplicate) */
    final public static ByteBuffer duplicate(ByteBuffer bb)
    {
        ByteBuffer bb2 = ByteBuffer.allocate(bb.limit()-bb.position()) ;
        int x = bb.position() ;
        bb2.put(bb) ;
        bb.position(x) ;
        
        bb2.position(0) ;
        bb2.limit(bb2.capacity()) ;
        return bb2 ;
    }
    
    // For non-array versions : beware of overlaps.
    final public static void bbcopy(ByteBuffer bb, int src, int dst, int length, int slotLen)
    {
        if ( allowArray && bb.hasArray() )
        {
            acopyArray(bb, src, dst, length, slotLen) ;
            return ;
        }
        
        if ( src == dst )
            return ;
        
        if ( src < dst ) 
            bbcopy1(bb, src, dst, length, slotLen) ;
        else
            bbcopy2(bb, src, dst, length, slotLen) ;
    }

    public final static void bbcopy1(ByteBuffer bb, int src, int dst, int length, int slotLen)
    {
        int bDst = dst*slotLen ;
        int bSrc = src*slotLen ;
        int bLen = length*slotLen ;
    
        for ( int i = bLen-1 ; i >= 0 ; i-- )
            bb.put(bDst+i, bb.get(bSrc+i)) ;
    }

    public final static void bbcopy2(ByteBuffer bb, int src, int dst, int length, int slotLen)
    {
        int bDst = dst*slotLen ;
        int bSrc = src*slotLen ;
        int bLen = length*slotLen ;
        
        // src > dst so dst[0] is not in the overlap 
        for ( int i = 0 ; i < bLen ; i++ )
            bb.put(bDst+i, bb.get(bSrc+i)) ;
    }

    public final static void bbcopy(ByteBuffer bb1, int src, ByteBuffer bb2, int dst, int length, int slotLen)
    {
        // Assume bb1 and bb2 are different and do not overlap.
        if ( allowArray && bb1.hasArray() && bb2.hasArray() )
        {
            acopyArray(bb1, src, bb2, dst, length, slotLen) ;
            return ;
        }
        // One or both does not have an array.
        
        int bSrc = src*slotLen ;
        int bDst = dst*slotLen ;
        int bLen = length*slotLen ;
        
        for ( int i = 0 ; i < bLen ; i++ )
            bb2.put(bDst+i, bb1.get(bSrc+i)) ;
    }

    final public static void bbfill(ByteBuffer bb, int fromIdx, int toIdx, byte fillValue, int slotLen)
    {
        if ( allowArray && bb.hasArray() )
        {
            afillArray(bb, fromIdx, toIdx, fillValue, slotLen) ;
            return ;
        }
        
        int bStart = fromIdx*slotLen ;
        int bFinish = toIdx*slotLen ;
        
        for ( int i = bStart ; i < bFinish ; i++ )
            bb.put(i, fillValue) ;
    }

    // To ArrayOps?
    
    final private static void acopyArray(ByteBuffer bb, int src, int dst, int length, int slotLen)
    {
        byte[] b = bb.array();
        
        int OFFSET = bb.arrayOffset() ;
        
        int bSrc = src*slotLen ;
        int bDst = dst*slotLen ;
        int bLen = length*slotLen ;
        
        arraycopy(b, OFFSET+bSrc, b, OFFSET+bDst, bLen) ;
    }

    final private static void acopyArray(ByteBuffer bb1, int src, ByteBuffer bb2, int dst, int length, int slotLen)
    {
        byte[] b1 = bb1.array();
        byte[] b2 = bb2.array();
        int OFFSET1 = bb1.arrayOffset() ;
        int OFFSET2 = bb2.arrayOffset() ;
        
        int bSrc = src*slotLen ;
        int bDst = dst*slotLen ;
        int bLen = length*slotLen ;
        
        arraycopy(b1, OFFSET1+bSrc, b2, OFFSET2+bDst, bLen) ;
    }

    final private static void afillArray(ByteBuffer bb, int fromIdx, int toIdx, byte fillValue, int slotLen)
    {
        Arrays.fill(bb.array(), fromIdx+bb.arrayOffset(), toIdx+bb.arrayOffset(), fillValue) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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