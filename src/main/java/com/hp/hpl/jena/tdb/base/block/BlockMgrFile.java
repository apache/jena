/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import static java.lang.String.format;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;

import org.openjena.atlas.lib.FileOps ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** Abstract class for block managers over a file */
public abstract class BlockMgrFile extends BlockMgrBase
{
    // Would like multiple inheritiance to reuse FileBase
    private static Logger log = LoggerFactory.getLogger(BlockMgrFile.class) ;
    protected String filename ;
    protected String label ;
    protected FileChannel channel ;
    protected RandomAccessFile out ;
    protected long numFileBlocks = -1 ;             // Don't overload use of this!
    protected AtomicLong seq = new AtomicLong() ;   // Id (future)
    protected boolean isEmpty = false ;
    
    // Better for ids?
    //private final AtomicLong numFileBlocks ;
    
    public BlockMgrFile(String filename, int blockSize)
    {
        super(blockSize) ;
//      if ( blockSize != 4*1024 )
//      System.err.println("Odd block size = "+blockSize) ;

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
            //numFileBlocks = new AtomicLong(numFileBlocks) ;
            
            if ( numFileBlocks > Integer.MAX_VALUE )
                log.warn(format("File size (%d) exceeds tested block number limits (%d)", filesize, blockSize)) ;
            
            if ( filesize%longBlockSize != 0 )
                throw new BlockException(format("File size (%d) not a multiple of blocksize (%d)", filesize, blockSize)) ;

            channel = out.getChannel() ;
            if ( channel.size() == 0 )
                isEmpty = true ;
        } catch (IOException ex) { throw new BlockException("Failed to create BlockMgrFile", ex) ; }    
    }

    final public boolean isEmpty() { return isEmpty ; }
    
    final protected void putNotification(int id, ByteBuffer block) { isEmpty = false ; }
    
    //@Override 
    final synchronized
    public int allocateId()
    {
        checkIfClosed() ;
        //return numFileBlocks.getAndIncrement() ;
        
        int id = (int)numFileBlocks ;
        numFileBlocks ++ ;

//        if ( getLog().isDebugEnabled() ) 
//            getLog().debug(format("allocateId(%d)", id)) ;
        return id ;
    }
    
    //@Override
    final synchronized
    public boolean valid(int id)
    {
        // Access to numFileBlocks not synchronized - it's only a check
        if ( id >= numFileBlocks )
            return false ;
        if ( id < 0 )
            return false ;
        return true ; 
    }

    final
    protected void check(int id)
    {
        // Access to numFileBlocks not synchronized - it's only a check
        if ( id < 0 || id >= numFileBlocks )
        {
            // Do it properly!
            synchronized(this)
            {
                if ( id < 0 || id >= numFileBlocks )
                    throw new BlockException(format("BlockMgrFile: Bounds exception: %s: (%d,%d)", filename, id,numFileBlocks)) ;
            }
        }
    }
    
    final protected void check(int id, ByteBuffer bb)
    {
        check(id) ;
        if ( bb.capacity() != blockSize )
            throw new BlockException(format("BlockMgrFile: Wrong size block.  Expected=%d : actual=%d", blockSize, bb.capacity())) ;
        if ( bb.order() != SystemTDB.NetworkOrder )
            throw new BlockException("BlockMgrFile: Wrong byte order") ;
    }

    protected void force()
    {
        if ( getLog().isDebugEnabled() )
            getLog().debug("force") ;
        try
        {
            channel.force(true) ;
        } catch (IOException ex)
        { throw new BlockException("Channel.force failed", ex) ; }
    }
    
    //@Override 
    final public boolean isClosed() { return channel == null ; }  
    
    protected final void checkIfClosed() 
    { 
        if ( isClosed() ) 
            ALog.fatal(this, "Block manager has been closed") ;
    }
    
    protected abstract void _close() ; 
    //@Override
    final public void close()
    {
        _close() ;
        if ( out != null )
        {
            try {
                force() ;
                channel.close();
                out.close();
                channel = null ;
                out = null ;
            } catch (IOException ex)
            { throw new BlockException(ex) ; }
        }
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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