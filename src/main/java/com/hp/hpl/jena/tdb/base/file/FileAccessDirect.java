/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import static java.lang.String.format ;

import java.io.IOException ;
import java.io.RandomAccessFile ;
import java.nio.ByteBuffer ;
import java.nio.channels.FileChannel ;
import java.util.concurrent.atomic.AtomicLong ;

import org.openjena.atlas.lib.FileOps ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockException ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFile ;

public class FileAccessDirect /*extends FileBase*/ implements FileAccess
{

    final private int blockSize ;
    private static Logger log = LoggerFactory.getLogger(BlockMgrFile.class) ;
    protected final String filename ;
    protected final String label ;
    protected final FileChannel channel ;
    protected final RandomAccessFile out ;
    protected long numFileBlocks = -1 ;             // Don't overload use of this!
    protected final AtomicLong seq ;   // Id (future)
    protected boolean isEmpty = false ;

    public FileAccessDirect(String filename, int blockSize)
    {
        //super(filename) ;
        this.blockSize = blockSize ;
        
        // COMMON CODE!!!!!!
        // [TxTDB:PATCH-UP]
        try {
            this.filename = filename ;
            this.label = FileOps.basename(filename) ;
            // "rwd" - Syncs only the file contents
            // "rws" - Syncs the file contents and metadata
            // "rw" - cached?

            out = new RandomAccessFile(filename, "rw") ;
            long filesize = out.length() ;
            isEmpty = (filesize==0) ;
            long longBlockSize = blockSize ;
            
            numFileBlocks = filesize/longBlockSize ;
            seq = new AtomicLong(numFileBlocks) ;
            
            if ( numFileBlocks > Integer.MAX_VALUE )
                log.warn(format("File size (%d) exceeds tested block number limits (%d)", filesize, blockSize)) ;
            
            if ( filesize%longBlockSize != 0 )
                throw new BlockException(format("File size (%d) not a multiple of blocksize (%d)", filesize, blockSize)) ;

            channel = out.getChannel() ;
            if ( channel.size() == 0 )
                isEmpty = true ;
        } catch (IOException ex) { throw new BlockException("Failed to create BlockMgrFile", ex) ; }    
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
    
    protected int allocateId()
    {
        //checkIfClosed() ;
        long id = seq.getAndIncrement() ;
        numFileBlocks ++ ;
        return (int)id ;
    }

    @Override
    public Block read(int id)
    {
        return null ;
    }

    @Override
    public void write(Block block)
    {}

    @Override
    public boolean valid(int id)
    {
        return false ;
    }

    @Override
    public boolean isEmpty()
    {
        return false ;
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