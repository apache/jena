/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import static java.lang.String.format ;

import java.io.IOException ;
import java.nio.ByteBuffer ;

import org.openjena.atlas.lib.FileOps ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.block.Block ;

public class BlockAccessDirect extends BlockAccessBase
{
    // Maybe layer BlockAccess on BufferChannel - retrofitting.
    // but need separate for memory mapped files anyway.

    private static Logger log = LoggerFactory.getLogger(BlockAccessDirect.class) ;
    
    public BlockAccessDirect(String filename, int blockSize)
    {
        super(filename, blockSize) ;
    }

    @Override
    public Block allocate(int blkSize)
    {
        if ( blkSize > 0 && blkSize != this.blockSize )
            throw new FileException("Fixed blocksize only: request= "+blkSize+"fixed size="+this.blockSize) ;
        int x = allocateId() ;
        ByteBuffer bb = ByteBuffer.allocate(blkSize) ;
        Block block = new Block(x, bb) ;
        return block;
    }
    
    @Override
    public Block read(long id)
    {
        check(id) ;
        checkIfClosed() ;
        ByteBuffer bb = ByteBuffer.allocate(blockSize) ;
        readByteBuffer(id, bb) ;
        Block block = new Block(id, bb) ;
        return block ;
    }
    
    private void readByteBuffer(long id, ByteBuffer dst)
    {
        try {
            int len = channel.read(dst, filePosition(id)) ;
            if ( len != blockSize )
                throw new FileException(format("get: short read (%d, not %d)", len, blockSize)) ;   
        } catch (IOException ex)
        { throw new FileException("FileAccessDirect", ex) ; }
    }
    
    private final long filePosition(long id)
    {
        return id*blockSize ;
    }

    @Override
    public void write(Block block)
    {
        check(block) ;
        checkIfClosed() ;
        ByteBuffer bb = block.getByteBuffer() ;
        bb.position(0) ;
        bb.limit(bb.capacity()) ;
        try {
            int len = channel.write(bb, filePosition(block.getId())) ;
            if ( len != blockSize )
                throw new FileException(format("put: short write (%d, not %d)", len, blockSize)) ;   
        } catch (IOException ex)
        { throw new FileException("FileAccessDirect", ex) ; }
        writeNotification(block) ;
    }

    @Override
    public void sync()
    {
        force() ;
    }

    @Override
    protected void _close()
    { super.force() ; }

    @Override
    protected Logger getLog()
    {
        return log ;
    }
    
    @Override
    public String toString() { return "Direct:"+FileOps.basename(filename) ; }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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