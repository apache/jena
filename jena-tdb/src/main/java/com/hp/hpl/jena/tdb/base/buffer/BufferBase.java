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

package com.hp.hpl.jena.tdb.base.buffer;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.NetworkOrder ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.NullOut ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.FillByte;
import static java.lang.String.format ;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.base.record.RecordException ;

import org.apache.jena.atlas.lib.ByteBufferLib ;


/** ByteBuffer specialization - Common operations for RecordBuffer and PtrBuffer */  
abstract class BufferBase
{
    public static boolean CheckBuffer = false ;
    
    protected ByteBuffer bb ;
    protected int slotLen ;
    protected int numSlot ;                 // Number of records in use
    protected int maxSlot ;                 // Maximum number of records

    protected BufferBase(ByteBuffer bb, int slotLen, int num)
    {
        if ( CheckBuffer )
        {
            if ( ! bb.order().equals(NetworkOrder) || !bb.hasArray() )
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
        if ( len == 0 )
            return ;
        
        // Check end index is inside the buffer.
        checkBounds(srcIdx+len-1, maxSlot) ;
     
        BufferBase src = this ;   // Clarity

        if ( dst.numSlot < dstIdx-1 )
            // Allow copy to be just off the end of dst.
            throw new IllegalArgumentException(format("copy: Out of bounds: dstIdx=%d, dst size=%d", dstIdx, dst.numSlot)) ;
        if ( src.slotLen != dst.slotLen )
            throw new RecordException(format("copy: records of differnt sizes: %d, %d",src.slotLen, dst.slotLen)) ;
        
        // How do we set the numRec in dst? max(dstIdx+len, old count)  
        ByteBufferLib.bbcopy(src.bb, srcIdx, dst.bb, dstIdx, len, slotLen) ;
        dst.numSlot = Math.max(dstIdx+len, dst.numSlot) ; 
    }
    
    final
    public void copyToTop(BufferBase dst)
    { 
        copy(0, dst, dst.numSlot, numSlot) ; } 
    
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
            ByteBufferLib.bbfill(bb, idx, (idx+len), FillByte, slotLen) ;
    }

    /** Does not reset the size */
    final public void clear()           { clear(0, maxSlot) ; numSlot = 0 ; }
    
    /** Does not reset the size */
    final public void clear(int idx)    { clear(idx, 1) ; }
    
    /** Is the record at idx set clear or not?
        This is done without regard to buffer size.
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
            {
//                byte b = bb.get(i) ;
//                lib.ByteBufferLib.print(bb) ;
                return false ;
            }
        return true ;
    }

    final public boolean isFull()
    {
        return numSlot >= maxSlot ; 
    }

    final public boolean isEmpty()
    {
        return numSlot == 0 ; 
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
    
    final public int slotLen()      { return slotLen ; }
    
    final public int getSize()      { return numSlot ; } 
    
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
        
        ByteBufferLib.bbcopy(bb, idx, idx+num, (numSlot-idx), slotLen) ;       // src, dst 
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

        ByteBufferLib.bbcopy(bb, idx+num, idx, (numSlot-num-idx), slotLen) ;       // src, dst

        if ( NullOut )
            clear(numSlot-num, num) ;
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

    /** Move the element from the high end of this to the low end of other */
    public void shiftRight(BufferBase other)
    {
        if ( other.numSlot >= other.maxSlot )
            throw new BufferException("No space in destination buffer") ;
        if ( numSlot <= 0 )
            throw new BufferException("Empty buffer") ;

        if ( other.numSlot > 0 )
            other.shiftUp(0) ;
        else
            other.numSlot++ ;
        // Copy high to low slot.
        ByteBufferLib.bbcopy(bb, (numSlot-1), other.bb, 0, 1, slotLen) ;
        removeTop() ;
    }

    /** Move the element from the low end of other to the high end of this */
    public void shiftLeft(BufferBase other)
    {
        if ( numSlot >= maxSlot )
            throw new BufferException("No space in destination buffer") ;
        if ( other.numSlot <= 0 )
            throw new BufferException("Empty buffer") ;

        // Copy low to above high slot.
        ByteBufferLib.bbcopy(other.bb, 0, bb, numSlot, 1, slotLen) ;
        // Correct length.
        numSlot ++ ;
        other.shiftDown(0) ;
    }

    final private static void checkBounds(int idx, int len)
    {
        if ( idx < 0 || idx >= len )
            throw new BufferException(format("Out of bounds: idx=%d, size=%d", idx, len)) ;
    }
}
