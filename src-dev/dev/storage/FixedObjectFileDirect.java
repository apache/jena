/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import atlas.lib.AtlasException;
import atlas.lib.ByteBufferLib;
import atlas.logging.Log;

import com.hp.hpl.jena.tdb.base.block.BlockException;
import com.hp.hpl.jena.tdb.base.file.FileBase;
import com.hp.hpl.jena.tdb.base.file.FileException;

/** Direct fixed object file map.
 *  Beware that small-sized object may become rather inefficient. 
 */

public class FixedObjectFileDirect extends FileBase implements FixedObjectFile 
{
    private final int objectSize ;
    private long nextId ;
    public FixedObjectFileDirect(String filename, int objectSize)
    {
        super(filename) ;
        this.objectSize = objectSize ;
        try { 
            long filesize = out.length() ;
            if ( filesize % objectSize != 0 )
            {
                Log.fatal(this, "Object size = "+objectSize+" : Filesize = "+filesize+" : Do not match") ;
                throw new AtlasException("FixedObject: Object size = "+objectSize+" : Filesize = "+filesize+" : Do not match" ) ;
            }
            nextId = filesize / objectSize ;
        } catch (IOException ex) { throw new BlockException("Failed to get filesize", ex) ; } 
    }
    
    public List<byte[]> all()
    {
        return null ;
    }

    public ByteBuffer read(long offset)
    {
        ByteBuffer bytes = ByteBuffer.allocate(objectSize) ;
        _read(offset, bytes) ;
        return bytes ;
    }

    public void read(long idx, ByteBuffer bytes)
    {
        if ( idx < 0 || idx > nextId )
            throw new IllegalArgumentException("index out of range [0,"+nextId+"]"+idx) ;
        _read(idx, bytes) ;
    }

    // Internal - no checking
    private void _read(long idx, ByteBuffer bytes)
    {
        long x = idx*objectSize ;
        try
        {
            channel.position(x) ;
            channel.read(bytes) ;
        } catch (IOException ex)
        { throw new FileException("FixedObjectFileDirect.read", ex) ; }
    }
    
    public long write(ByteBuffer bytes)
    {
        long x = nextId ;
        _write(x, bytes) ;
        nextId++ ;
        return nextId ;
    }
    
    public void write(long idx, ByteBuffer bytes)
    {
        if ( idx < 0 || idx > nextId )
            throw new IllegalArgumentException("index out of range [0,"+nextId+"]"+idx) ;
        _write(idx, bytes) ;
    }
    
    // Internal - no checking
    private void _write(long idx, ByteBuffer bytes)
    {
        if ( idx == nextId )
        {
            // Write to end of file.
            write(bytes) ;
            return ;
        }
        
        long nextByte = objectSize*idx ;
        try
        {
            channel.position(nextByte) ;
            channel.write(bytes) ;
        } catch (IOException ex)
        { throw new FileException("FixedObjectFileDirect.write", ex) ; }
    }

    public void dump()
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(objectSize) ;
        for ( long i = 0 ; i < nextId ; i++ )
        {
            read(i, byteBuffer) ;
            ByteBufferLib.print(byteBuffer) ;
            byteBuffer.clear() ;
        }
    }


    
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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