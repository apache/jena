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
        
        for ( int i = 0 ; i < numSlot ; i++ )
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
