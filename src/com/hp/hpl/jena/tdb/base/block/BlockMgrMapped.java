/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Block manager for a file, using memory mapped I/O */
final
public class BlockMgrMapped extends BlockMgrFile
{
    /* Blocks are addressed by postive ints - 
     * Is that a limit?
     * One billion is 2^30
     * If a block is 8K, the 2^31*2^13 =  2^44 bits or 2^14 billion = 16K Billion. = 16 trillion bytes.
     * No limit at the moment - later performance tuning will see what the cost of 48 or 63 bit addresses would be.    
     */
    
    private static Logger log = LoggerFactory.getLogger(BlockMgrMapped.class) ;
    
    // Consider: having one file per segment or a few segments, not one large file.
    // May make no difference - will need structured disk pointers.
    private int segmentSize = 8 * 1024 * 1024 ;
    private int blocksPerSegment ;                              
    
    private MappedByteBuffer[] segments = new MappedByteBuffer[1000] ;  // 8G - need tuning
    private boolean[] segmentDirty = new boolean[1000] ; 
    
    
    BlockMgrMapped(String filename, int blockSize)
    {
        super(filename, blockSize) ;
        blocksPerSegment = segmentSize/blockSize ;
        for ( int i = 0 ; i < segmentDirty.length ; i++ )
            segmentDirty[i] = false ;
        
        if ( log.isDebugEnabled() )
            log.debug(format("Segment:%d  BlockSize=%d  blocksPerSegment=%d", segmentSize, blockSize, blocksPerSegment)) ;
    }
    
    @Override
    public ByteBuffer allocateBuffer(int id)
    {
        if ( getLog().isDebugEnabled() ) 
            getLog().debug(format("allocateBuffer(%d)", id)) ;
        ByteBuffer bb = getSilent(id) ;
        // clear out bb ;
        bb.position(0) ;
        for ( int i = 0 ; i < bb.limit(); i ++ )
            bb.put(0, (byte)0xFF) ;

        bb.putInt(0,0) ;
        bb.position(0) ;
        return bb ;
    }
    
    @Override
    public ByteBuffer get(int id)
    {
        if ( log.isDebugEnabled() ) 
            log.debug(format("get(%d)", id)) ;
        return getSilent(id) ;
    }

    @Override
    public ByteBuffer getSilent(int id)
    {
        check(id) ;
        int seg = id/blocksPerSegment ;                     // Segment.
        int segOff = (id%blocksPerSegment)*blockSize ;      // Byte offset in segement

        if ( log.isDebugEnabled() ) 
            log.debug(format("%d => [%d, %d]", id, seg, segOff)) ;

        ByteBuffer segBuffer = allocSegment(seg) ;
        segBuffer.position(segOff) ;
        segBuffer.limit(segOff+blockSize) ;
        ByteBuffer dst = segBuffer.slice() ;
        segBuffer.limit(segBuffer.capacity()) ;
        numFileBlocks = Math.max(numFileBlocks, id+1) ;
        return dst ;
    }
    
    private MappedByteBuffer allocSegment(int seg)
    {
        if ( seg < 0 )
        {
            log.error("Segment negative: "+seg) ;
            throw new BlockException("Negative segment: "+seg) ;
        }
        long longSeg  = seg ;
        
        // Note : do long arthimetic, not int, then extended to long.
        long offset = longSeg*segmentSize ;
        if ( offset < 0 )
        {
            log.error("Segment offset gone negative: "+seg) ;
            throw new BlockException("Negative segment offset: "+seg) ;
        }
        
        MappedByteBuffer segBuffer = segments[seg] ;
        if ( segBuffer == null )
        {
            try {
                segBuffer = channel.map(FileChannel.MapMode.READ_WRITE, offset, segmentSize) ;
                if ( log.isDebugEnabled() )
                    log.debug(format("Segment: %d", seg)) ;
                segments[seg] = segBuffer ;
            } catch (IOException ex)
            {
                throw new BlockException("BlockMgrMapped.segmentAllocate: "+seg, ex) ;
            }
        }
        segmentDirty[seg] = true ;
        return segBuffer ;
    }

    @Override
    public void put(int id, ByteBuffer block)
    {
        check(id, block) ;
    }
    
    @Override
    public void release(int id)
    { 
        check(id) ;
        int seg = id/blocksPerSegment ; 
        segmentDirty[seg] = false ;
        if ( log.isDebugEnabled() ) 
            log.debug(format("release(%d)", id)) ;
    }
    
    @Override
    public void sync(boolean force)
    {
        if ( force )
            force() ;
    }

    @Override
    protected void force()
    {
        for ( int i = 0 ; i < segments.length ; i++ )
            if ( segments[i] != null && segmentDirty[i] )
                segments[i].force() ;
        super.force() ;
    }
    
//    @Override
//    public void finishUpdate()
//    {}
//
//    @Override
//    public void startUpdate()
//    {}
//
//    @Override
//    public void startRead()
//    {}
//
//    @Override
//    public void finishRead()
//    {}

    @Override
    protected Logger getLog()
    {
        return log ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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