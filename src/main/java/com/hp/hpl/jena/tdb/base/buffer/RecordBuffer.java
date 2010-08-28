/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.buffer;

import static java.lang.String.format;
import static org.openjena.atlas.lib.Alg.encodeIndex ;

import java.nio.ByteBuffer;
import java.util.Iterator;

import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;



final
public class RecordBuffer extends BufferBase
{
    private RecordFactory factory ;

    // Need own specialized binary search :-(
    
    public RecordBuffer(RecordFactory recFactory, int maxRec)
    {
        this(ByteBuffer.allocate(recFactory.recordLength()*maxRec), recFactory, 0) ;
    }
    
    public RecordBuffer(ByteBuffer bb, RecordFactory recFactory, int num)
    {
        super(bb, recFactory.recordLength(), num) ;
        this.factory = recFactory ;
    }
    
    public Record get(int idx)
    { 
        checkBounds(idx, numSlot) ;
        return _get(idx) ;
    }
    
    public Record getLow()
    { 
        if ( numSlot == 0 )
            throw new IllegalArgumentException("Empty RecordBuffer") ;
        return _get(0) ;
    }
    
    public Record getHigh()
    { 
        if ( numSlot == 0 )
            throw new IllegalArgumentException("Empty RecordBuffer") ;
        return _get(numSlot-1) ;
    }
    
    // Inserts at top.
    public void add(Record record) { add(numSlot, record) ; }
    
    // Inserts at slot idx
    public void add(int idx, Record record)
    { 
        if ( idx != numSlot )
        {
            checkBounds(idx, numSlot) ;
            shiftUp(idx) ;      // Changes count. 
        }
        else
        {
            if ( numSlot >= maxSlot )
                throw new BufferException(format("Out of bounds: idx=%d, ptrs=%d", idx, maxSlot)) ;
            numSlot++ ;
        }
        _set(idx, record) ;
    }
    
    // Overwrites the contents of slot idx
    public void set(int idx, Record record)
    {
        if ( idx == numSlot )
        {
            add(idx, record) ;
            return ;
        }
        else
            checkBounds(idx, numSlot) ;
        _set(idx, record) ;
    }

    // No checking bound : careful use only!
    
    public Record _get(int idx)
    {
        return factory.buildFrom(bb, idx) ;
    }

    // No bounds checking : careful use only!
    void _set(int idx, Record rec)
    {
        factory.insertInto(rec, bb, idx) ;
//        byte[] data = rec.getKey() ; 
//        if ( data.length != slotLen )
//            throw new RecordException(format("Wrong length: actual=%d, expected=%d", data.length, slotLen)) ;
//        bb.position(idx*slotLen) ;
//        bb.put(data, 0, slotLen) ;
    }
    
    // Linear search for testing.
    int find1(byte[] data)
    { 
        for ( int i = 0 ; i < numSlot ; i++ )
        {
            int x = compare(i, data) ;
            if ( x == 0 )
                return i ;
            if ( x > 0 )
                return encodeIndex(i) ;
        }
        return encodeIndex(numSlot) ;
    }
    
    // Binary search
    public int find(Record k)
    {
        return find(k, 0, numSlot) ;
    }
    
    public Iterator<Record> iterator() { return new RecordBufferIterator(this) ; } 

    /** Iterator over a range from min (inclusive) to max(exclusive) */
    public Iterator<Record> iterator(Record min, Record max) { return new RecordBufferIterator(this, min, max) ; } 
    
    public Record findGet(Record k)
    {
        int x = find(k) ;
        if ( x >= 0 )
            return get(x) ; 
        return null ;
    }
    
    /** return true is removed anything */
    public boolean removeByKey(Record k)
    {
        int x = find(k) ;
        if ( x < 0 )
            return false ;
        super.remove(x) ;
        return true ;
    }

    /** Search for key in range fromIndex (inclusive) to toIndex (exclusive) */
    public int find(Record rec, int fromIndex, int toIndex)
    { 
        int low = fromIndex ;
        int high = toIndex-1 ;
        
        byte[] key = rec.getKey() ; 
        // http://en.wikipedia.org/wiki/Binary_search
            
        while (low <= high)
        {
            int mid = (low + high) >>> 1 ;  // int divide by 2
            
            int x = compare(mid, key) ;
            //System.out.printf("Compare: %d(%s) %s ==> %d\n", mid, Record.str(get(mid)), Record.str(data), x) ;

            if ( x < 0 )
                low = mid + 1 ;
            else if ( x > 0 )
                high = mid - 1 ;
            else
                return mid ;
        }
        // On exit, when not finding, low is the least value
        // above, including off the end of the array.  
        return encodeIndex(low) ;
    }

    // Record compareByKey except we avoid touching bytes by exiting as soon as possible.
    // No record created as would be by using compareByKey(RecordBuffer.get(idx), record)  
    // Compare the slot at idx with value.
    private int compare(int idx, byte[] value)
    { 
        idx = idx*slotLen ;
        
        for ( int i = 0 ; i < value.length ; i++ )
        {
            byte b1 = bb.get(idx+i) ;
            byte b2 = value[i] ;
            if ( b1 == b2 )
                continue ;
            return (b1&0xFF) - (b2&0xFF) ;  
        }
        return  0 ;
    }
    
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder(40000) ;
        str.append(format("Len=%d Max=%d: ", numSlot, bb.limit()/slotLen)) ;
        
        // Print active slots as records.
        for ( int i = 0 ; i < numSlot ; i++ )
        {
            if ( i != 0 )
                str.append(" ") ;
            Record r = _get(i) ;
            str.append(r.toString()) ;
        }
        
//        // Print empty slots
//        for ( int i = numSlot*slotLen ; i < maxSlot*slotLen ; i++ )
//        {
//            if ( i != 0 && i%slotLen == 0 )
//                str.append(" ") ;
//            byte b = bb.get(i) ;
//            str.append(format("%02x", b)) ;
//        }
        String s = str.toString() ;
        return s ;
    }

    private static void checkBounds(int idx, int len)
    {
        if ( idx < 0 || idx >= len )
            throw new IllegalArgumentException(format("Out of bounds: idx=%d, size=%d", idx, len)) ;
    }

    /** A duplicate which does not share anything with the original - for testing */
    public RecordBuffer duplicate()
    {
        RecordBuffer n = new RecordBuffer(factory, maxSlot) ;
        copy(0, n, 0, maxSlot) ;    // numSlot
        n.numSlot = numSlot ;       // reset the allocated length
        return n ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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