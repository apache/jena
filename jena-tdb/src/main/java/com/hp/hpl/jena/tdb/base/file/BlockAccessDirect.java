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

package com.hp.hpl.jena.tdb.base.file;

import static java.lang.String.format ;

import java.io.IOException ;
import java.nio.ByteBuffer ;

import org.apache.jena.atlas.lib.FileOps ;
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
        bb.rewind() ;
        Block block = new Block(id, bb) ;
        return block ;
    }
    
    private void readByteBuffer(long id, ByteBuffer dst)
    {
        try {
            int len = file.channel().read(dst, filePosition(id)) ;
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
        // This .clear() except the javadoc suggests this is not the correct use of .clear()
        // and the name does 
        bb.limit(bb.capacity()) ;   // It shouldn't have been changed.
        bb.rewind() ;
        try {
            int len = file.channel().write(bb, filePosition(block.getId())) ;
            if ( len != blockSize )
                throw new FileException(format("write: short write (%d, not %d)", len, blockSize)) ;   
        } catch (IOException ex)
        { throw new FileException("FileAccessDirect", ex) ; }
        writeNotification(block) ;
    }
    
    @Override
    public void overwrite(Block block)
    {
        overwriteNotification(block) ;
        write(block) ;
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
    public String toString() { return "Direct:"+FileOps.basename(file.filename) ; }
}
