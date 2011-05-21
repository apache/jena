/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.base.StorageException ;

public class BufferChannelMem implements BufferChannel
{
    private ByteBuffer bytes ;      // Position is our file position.
    private long length ;           // Bytes in use: 0 to length-1
    private String name ;
    private static int INIT_SIZE = 1024 ;
    private static int INC_SIZE = 1024 ;
    
    public BufferChannelMem(String name)
    {
        bytes = ByteBuffer.allocate(1024) ;
        length = 0 ; 
        this.name = name ;
    }

    @Override
    public long position()
    {
        checkIfClosed() ;
        return bytes.position() ;
    }

    @Override
    public void position(long pos)
    { 
        checkIfClosed() ;
        if ( pos < 0 || pos > bytes.capacity() )
            throw new StorageException("Out of range: "+pos) ;
        bytes.position((int)pos) ;
    }

    @Override
    public int read(ByteBuffer buffer)
    {
        checkIfClosed() ;
        int x = bytes.position();
        
        int len = buffer.limit()-buffer.position() ;
        if ( len > bytes.remaining() )
            len = bytes.remaining() ;
        // Copy out, moving the position of the bytes of stroage. 
        for (int i = 0; i < len; i++)
            buffer.put(bytes.get());
        return len ;
    }
    
    @Override
    public int read(ByteBuffer buffer, long loc)
    {
        checkIfClosed() ;
        if ( loc < 0 || loc > length )
            throw new StorageException("Out of range: "+loc) ;
        int x = buffer.position() ;
        bytes.position((int)loc) ;
        int len = read(buffer) ;
        bytes.position(x) ;
        return len ;
    }

    @Override
    public int write(ByteBuffer buffer)
    {
        checkIfClosed() ;
        int len = buffer.limit()-buffer.position() ;
        int posn = bytes.position() ;

        if ( len > bytes.remaining() )
        {
            int inc = len-bytes.remaining() ;
            inc += INC_SIZE ;
            ByteBuffer bb2 = ByteBuffer.allocate(bytes.capacity()+inc) ;
            bytes.clear() ;
            // Copy contents.
            bb2.put(bytes) ;
            bytes.position(posn) ;
        }
        bytes.put(buffer) ;
        length = Math.max(length, posn+len) ;
        return len ;
    }
    
    @Override
    public int write(ByteBuffer buffer, long loc)
    {
        checkIfClosed() ;
        if ( loc < 0 || loc > length )
            throw new StorageException("Out of range: "+loc) ;
        int x = bytes.position() ; 
        bytes.position((int)loc) ;
        int len = write(buffer) ;
        bytes.position(x) ;
        return len ;
    }

    @Override
    public long length()
    {
        checkIfClosed() ;
        return length ;
    }
    
    @Override
    public void sync()
    { 
        checkIfClosed() ;
    }

    @Override
    public void close()
    { checkIfClosed() ; bytes = null ; }
    
    private void checkIfClosed()
    {
        if ( bytes == null )
            throw new StorageException("Closed: "+name) ;
    }
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