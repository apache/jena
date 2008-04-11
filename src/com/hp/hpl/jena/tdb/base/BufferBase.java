/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base;

import static com.hp.hpl.jena.tdb.base.BaseConfig.NullOut;
import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.util.Arrays.fill;

import java.nio.ByteBuffer;

import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.base.record.RecordException;


/** ByteBuffer specialization - Common operations for RecordBuffer and PtrBuffer */  
public abstract class BufferBase
{
    public static boolean CheckBuffer = false ;
    
    static final byte FillByte = (byte)0xFF ;
        
    protected ByteBuffer bb ;
    protected int slotLen ;
    protected int numSlot ;                 // Number of records in use
    protected int maxSlot ;                 // Maximum number of records

    protected BufferBase(ByteBuffer bb, int slotLen, int num)
    {
        if ( CheckBuffer )
        {
            if ( ! bb.order().equals(Const.NetworkOrder) || !bb.hasArray() )
                throw new RecordException("Duff buffer (byte order is not network order)") ;
            if ( bb.limit() == 0 )
                throw new RecordException("Duff buffer (zero length byte buffer)") ;
            int size = bb.limit() ;
            int slots = size/slotLen  ;
            if ( size%slotLen != 0 )
                throw new RecordException(format("Misalinged buffer: size=%d, keyLen=%d",size, slotLen)) ; 
            if ( slots < num )
                throw new RecordException(format("Wrong size: slots=%d, len=%d", slots, num)) ;
        }
        this.bb = bb ; 
        this.slotLen = slotLen ;
        this.numSlot = num ;
        this.maxSlot = bb.limit()/slotLen ;
        if ( NullOut )
            clear(numSlot, maxSlot-numSlot) ;
    }
    
    //private BufferBase() {}

    // Why so many final methods?  This code is performace critical and "final" methods
    // can be inlined by the JIT - or just not have object dispatch done each time
    // because the destination is fixed.
    // (and here - there are quite a few assumed relationships of the ints). 
    
    final
    public void copy(int srcIdx, BufferBase dst, int dstIdx, int len)
    {
        checkBounds(srcIdx+len-1, maxSlot) ;
     
        BufferBase src = this ;   // Clarity

        if ( dst.numSlot < dstIdx-1 )
            // Allow copy to be just off the end of dst.
            throw new IllegalArgumentException(format("copy: Out of bounds: dstIdx=%d, dst size=%d", dstIdx, dst.numSlot)) ;
        if ( src.slotLen != dst.slotLen )
            throw new RecordException(format("copy: records of differnt sizes: %d, %d",src.slotLen, dst.slotLen)) ;
        
        // How do we set the numRec in dst? max(dstIdx+len, old count)  
        int recLen = src.slotLen ;
        bbcopy(src.bb, srcIdx*recLen, dst.bb, dstIdx*recLen, len*recLen) ;
        dst.numSlot = Math.max(dstIdx+len, dst.numSlot) ; 
    }
    
    /** Remove top element */
    final public void removeTop()
    {
        if ( numSlot == 0 )
            throw new IndexOutOfBoundsException("removeTop: empty buffer") ; 
        clear(numSlot-1) ;
        numSlot-- ;
    }
    
    final public void remove(int idx) { shiftDown(idx) ; }
    
    /** Does not reset the size */
    final
    public void clear(int idx, int len)
    {
        if ( NullOut )
            bbfill(bb, idx*slotLen, (idx+len)*slotLen, FillByte) ;
    }

    /** Does not reset the size */
    final public void clear()           { clear(0, maxSlot) ; numSlot = 0 ; }
    
    /** Does not reset the size */
    final public void clear(int idx)    { clear(idx, 1) ; }
    
    /** Is the record at idx set clear or not?
        This is done without regrad to buffer size.
        Requires NullOut to be accurate.
        Testing.
     */
    final public boolean isClear(int idx)
    {
        checkBounds(idx, maxSlot) ;
        int x = idx*slotLen ;
        int y = (idx+1)*slotLen ;
        for ( int i = x ; i < y ; i++ )
            if ( bb.get(i) != FillByte )
                return false ;
        return true ;
    }
    
    final public void incSize() { incSize(1) ; }
    final public void incSize(int n)
    { 
        if ( numSlot+n > maxSlot )
            throw new IllegalArgumentException(format("inc(%d): out of range: max=%d", n, maxSlot)) ;
        numSlot += n ;
    }

    final public void decSize() { decSize(1) ; }
    final public void decSize(int n)
    { 
        if ( numSlot-n < 0 )
            throw new IllegalArgumentException(format("dec(%d): out of range: max=%d", n, maxSlot)) ;
        numSlot -= n ;
    }
    
    final public int slotLen()    { return slotLen ; }
    
    final public void setSize(int n)
    {
        if ( n < 0 || n > maxSlot )
            throw new IllegalArgumentException(format("size(%d): out of range: max=%d", n, maxSlot)) ;
        numSlot = n ;
    }
    
    final public int size()       { return numSlot ; }
    final public int maxSize()    { return maxSlot ; }
    
    final public void shiftUp(int idx) { shiftUpN(idx, 1) ; }
    final public void shiftUpN(int idx, int num)
    {
        checkBounds(idx, numSlot) ;
        if ( numSlot + num > maxSlot )
            throw new IllegalArgumentException(format("Shift up(%d): out of range: len=%d max=%d", num, num, maxSlot)) ;
        
        // In bytes ...
        int srcIdx = idx*slotLen ;
        int gap = num*slotLen ;
        int len = (numSlot-idx)*slotLen ;
        
        bbcopy(bb, srcIdx, srcIdx+gap, len) ;                // src, dst 
        if ( NullOut )
            clear(idx, num) ;

        numSlot += num ;
    }
    
    final public void shiftDown(int idx) { shiftDownN(idx, 1) ; }
    final public void shiftDownN(int idx, int num)
    {
        checkBounds(idx, numSlot) ;
        if ( idx+num > numSlot )
            throw new IllegalArgumentException(format("Shift down(%d,%d): out of range: len=%d", idx, num, num)) ;

        // In bytes ...
        int srcIdx = idx*slotLen ;
        int gap = num*slotLen ;
        int len = (numSlot-num-idx)*slotLen ;
        
        bbcopy(bb, srcIdx+gap, srcIdx, len) ;      // src, dst)

        if ( NullOut )
        {
            clear(numSlot-num, num) ;
        }
        numSlot -= num ;
    }
    
//    @Override
//    final public String toString()
//    {
//        StringBuilder str = new StringBuilder() ;
//        str.append(format("Len=%d Count=%d ", bb.limit()/recLen, num)) ;
//        
//        for ( int i = 0 ; i < max*recLen ; i++ )
//        {
//            if ( i != 0 && i%recLen == 0 )
//                str.append(" ") ;
//            byte b = bb.get(i) ;
//            str.append(format("%02x", b)) ;
//        }
//        return str.toString() ;
//    }
    
    static boolean allowArray  = false ;

    // For non-array versions : beware of overlaps.
    final public static void bbcopy(ByteBuffer bb, int src, int dst, int length)
    {
        if ( allowArray && bb.hasArray() )
        {
            acopyArray(bb, src, dst, length) ;
            return ;
        }
        
        if ( src == dst )
            return ;
        
        if ( src < dst ) 
            bbcopy1(bb, src, dst, length ) ;
        else
            bbcopy2(bb, src, dst, length ) ;
    }
    
    final private static void bbcopy1(ByteBuffer bb, int src, int dst, int length)
    {
        // TODO Is creating a byte buffer solely to call bb.put(ByteBuffer) more efficient?
        // Are there overlap problems?
        // src < dst so dst[top] is not in the overlap
        for ( int i = length-1 ; i >= 0 ; i-- )
            bb.put(dst+i, bb.get(src+i)) ;
    }
    
    final private static void bbcopy2(ByteBuffer bb, int src, int dst, int length)
    {
        // src > dst so dst[0] is not in the overlap 
        for ( int i = 0 ; i < length ; i++ )
            bb.put(dst+i, bb.get(src+i)) ;
    }

    
    final private static void bbcopy(ByteBuffer bb1, int src, ByteBuffer bb2, int dst, int length)
    {
        // Assume bb1 and bb2 are different and do not overlap.
        if ( allowArray && bb1.hasArray() && bb2.hasArray() )
        {
            acopyArray(bb1, src, bb2, dst, length) ;
            return ;
        }
        // One or both does not have an array.
        
        for ( int i = 0 ; i < length ; i++ )
            bb2.put(dst+i, bb1.get(src+i)) ;
    }

    final public static void bbfill(ByteBuffer bb, int fromIdx, int toIdx, byte fillValue)
    {
        if ( ! NullOut )
            return ;
        if ( allowArray && bb.hasArray() )
        {
            afillArray(bb, fromIdx, toIdx, fillValue) ;
            return ;
        }
        
        for ( int i = fromIdx; i < toIdx; i++ )
            bb.put(i, fillValue) ;
    }
   
    final private static void checkBounds(int idx, int len)
    {
        if ( idx < 0 || idx >= len )
            throw new IllegalArgumentException(format("Out of bounds: idx=%d, size=%d", idx, len)) ;
    }

    //    @Override
    //    final public String toString()
    //    {
    //        StringBuilder str = new StringBuilder() ;
    //        str.append(format("Len=%d Count=%d ", bb.limit()/recLen, num)) ;
    //        
    //        for ( int i = 0 ; i < max*recLen ; i++ )
    //        {
    //            if ( i != 0 && i%recLen == 0 )
    //                str.append(" ") ;
    //            byte b = bb.get(i) ;
    //            str.append(format("%02x", b)) ;
    //        }
    //        return str.toString() ;
    //    }
    
    // Array versions
    
    final public static void acopyArray(ByteBuffer bb, int src, int dst, int length)
    {
        byte[] b = bb.array();
        int OFFSET = bb.arrayOffset() ;
        arraycopy(b, OFFSET+src, b, OFFSET+dst, length) ;
    }

    final public static void acopyArray(ByteBuffer bb1, int src, ByteBuffer bb2, int dst, int length)
    {
        byte[] b1 = bb1.array();
        byte[] b2 = bb2.array();
        int OFFSET1 = bb1.arrayOffset() ;
        int OFFSET2 = bb2.arrayOffset() ;
        arraycopy(b1, OFFSET1+src, b2, OFFSET2+dst, length) ;
    }

    final private static void afillArray(ByteBuffer bb, int fromIdx, int toIdx, byte fillValue)
    {
        if ( NullOut )
            fill(bb.array(), fromIdx+bb.arrayOffset(), toIdx+bb.arrayOffset(), fillValue) ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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