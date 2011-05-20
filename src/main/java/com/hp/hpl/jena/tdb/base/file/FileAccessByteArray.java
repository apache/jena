/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfInt ;

/** 
 *  FileAccess interface backed by a byte array.
 */
public class FileAccessByteArray implements FileAccess
{
    private ByteBuffer bytes ;
    private long length ;           // Bytes in use: 0 to length-1 
    private long alloc ;           // Bytes in use: 0 to length-1
    
    public FileAccessByteArray()
    {
        bytes = ByteBuffer.allocate(1024) ;
        length = 0 ; 
        alloc = 0 ;
    }
    
    @Override
    public Block allocate(int size)
    {
        long addr = alloc ;
        ByteBuffer bb = ByteBuffer.allocate(size) ;
        alloc += size + SizeOfInt ;
        return new Block((int)addr, bb) ; 
    }

    @Override
    public Block read(int id)
    {
        if ( id < 0 || id >= length || id >= bytes.capacity() )
            throw new FileException("Bad id (read): "+id) ;
        bytes.position(id) ;
        int len = bytes.getInt() ;
        ByteBuffer bb = ByteBuffer.allocate(len) ;
        // Copy out the bytes - copy for safety.
        bytes.get(bb.array(), 0, len) ;
        return new Block(id, bb) ; 
    }

    @Override
    public void write(Block block)
    {
        int loc = block.getId() ;
        if ( loc < 0 || loc > length )  // Can be equal => append.
            throw new FileException("Bad id (write): "+loc+" ("+alloc+","+length+")") ;
        ByteBuffer bb = block.getByteBuffer() ; 
        int len = bb.capacity() ;
        
        if ( loc == length )
        {
            if ( bytes.capacity()-length < len )
            {
                int cap2 = bytes.capacity()+1024 ;
                while(bytes.capacity()-length < len)
                    cap2 += 1024 ; 
                
                ByteBuffer bytes2 = ByteBuffer.allocate(cap2) ;
                bytes2.position(0) ;
                bytes2.put(bytes) ;
            }
            length += len +SizeOfInt ;
        }
        bytes.position(loc) ;
        bytes.putInt(len) ;
        bytes.put(bb.array(), 0, bb.capacity()) ;
    }

    @Override
    public boolean isEmpty()
    {
        return length == 0  ;
    }

    @Override
    public boolean valid(int id)
    {
        return ( id >= 0 && id < length ) ;
    }

    @Override
    public void sync()
    {}

    @Override
    public void close()
    {}
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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