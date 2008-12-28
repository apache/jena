/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.buffer;

import static java.lang.String.format;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.hp.hpl.jena.tdb.base.record.RecordException;
import com.hp.hpl.jena.tdb.sys.SystemTDB;


/** An IntBuffer with extra operations */

final 
public class PtrBuffer extends BufferBase
{
    // Is Int big enough?
    // 2^^31 4Kb blocks => 2e9 * 4e3 => 8e12 
    private IntBuffer iBuff ;
    
    private PtrBuffer(int maxRec)
    {
        this(ByteBuffer.allocate(SystemTDB.SizeOfPointer*maxRec), 0) ;
    }
    
    public PtrBuffer(ByteBuffer bb, int num)
    { 
        super(bb, SystemTDB.SizeOfPointer, num) ;
        iBuff = bb.asIntBuffer() ;

        if ( CheckBuffer )
        {
            // It is a IntBuffer with associated ByteBuffer
            if ( iBuff.position() != 0 || bb.order() != SystemTDB.NetworkOrder )
                throw new RecordException("Duff pointer buffer") ;
        }
    }
    
    
    public int get(int idx)
    {
        checkBounds(idx, numSlot) ;
        return _get(idx) ;
    }

    public int getHigh()
    {
        if ( numSlot == 0 )
            throw new IllegalArgumentException("Empty PtrBuffer") ;
        return _get(numSlot-1) ;
    }

    public int getLow()
    {
        if ( numSlot == 0 )
            throw new IllegalArgumentException("Empty PtrBuffer") ;
        return _get(0) ;
    }
    
    public void add(int val)
    { add(numSlot, val) ; }
    
    
    public void add(int idx, int val)
    {  
        if ( idx != numSlot )
        {
            checkBounds(idx, numSlot) ;
            shiftUp(idx) ; 
        }
        else
        {
            if ( numSlot >= maxSlot )
                throw new BufferException(format("Out of bounds: idx=%d, ptrs=%d", idx, maxSlot)) ;
            numSlot++ ;
        }
        // Add right at the top.
        _set(idx, val) ;
    }
    
    public void set(int idx, int val)
    { checkBounds(idx, numSlot) ; _set(idx, val) ; }
    
    private final int _get(int idx)
    {
        return iBuff.get(idx) ;
    }

    private final void _set(int idx, int val) 
    { 
        iBuff.put(idx, val) ;
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder() ;
        str.append(format("Len=%d Max=%d ", numSlot, maxSlot)) ;
        
        for ( int i = 0 ; i < maxSlot ; i++ )
        {
            if ( i != 0 )
                str.append(" ") ;
            int x = _get(i) ;
            str.append(format("%04d", x)) ;
        }
        return str.toString() ;
    }

    private static void checkBounds(int idx, int len)
    {
        if ( idx < 0 || idx >= len )
            throw new BufferException(format("Out of bounds: idx=%d, ptrs=%d", idx, len)) ;
    }
    
    /** A duplicate which does not share anything with the original - for testing */
    public PtrBuffer duplicate()
    {
        PtrBuffer n = new PtrBuffer(maxSlot) ;
        copy(0, n, 0, maxSlot) ;    // numSlot
        n.numSlot = numSlot ;       // Reset
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