/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** FileAccess for a file, using memory mapped I/O */
final
public class FileAccessMapped extends FileAccessBase
{
    /* Blocks are addressed by positive ints - 
     * Is that a limit?
     * One billion is 2^30
     * If a block is 8K, the 2^31*2^13 =  2^44 bits or 2^14 billion = 16K Billion. = 16 trillion bytes.
     * No limit at the moment - later performance tuning will see what the cost of 48 or 63 bit addresses would be.    
     */
    
    private static Logger log = LoggerFactory.getLogger(FileAccessMapped.class) ;

    // Segmentation avoids over-mapping; allows file to grow (in chunks) 
    private final int GrowthFactor = 2 ;
    private final int SegmentSize = SystemTDB.SegmentSize ;
    private final int blocksPerSegment ;                              
    
    private int initialNumSegements = 1 ;
    private MappedByteBuffer[] segments = new MappedByteBuffer[initialNumSegements] ;  
    
    // Unflushed segments.
    private int segmentDirtyCount = 0 ;
    private boolean[] segmentDirty = new boolean[initialNumSegements] ; 
    
    public FileAccessMapped(String filename, int blockSize)
    {
        super(filename, blockSize) ;
        blocksPerSegment = SegmentSize/blockSize ;
        if ( SegmentSize%blockSize != 0 )
            getLog().warn(format("%s: Segement size(%d) not a multiple of blocksize (%d)", filename, SegmentSize, blockSize)) ;
        
        for ( int i = 0 ; i < initialNumSegements ; i++ )
            // Not strictly necessary - default value is false.
            segmentDirty[i] = false ;
        segmentDirtyCount = 0 ;
        
        if ( getLog().isDebugEnabled() )
            getLog().debug(format("Segment:%d  BlockSize=%d  blocksPerSegment=%d", SegmentSize, blockSize, blocksPerSegment)) ;
    }
    
    
    @Override
    public Block allocate(int blkSize)
    {
        if ( blkSize > 0 && blkSize != this.blockSize )
            throw new FileException("Fixed blocksize only: request= "+blkSize+"fixed size="+this.blockSize) ;
        int id = allocateId() ;
        ByteBuffer bb = getByteBuffer(id) ;
        bb.position(0) ;
        Block block = new Block(id, bb) ;
        return block ;
    }
    
    @Override
    public Block read(int id)
    {
        check(id) ;
        checkIfClosed() ;
        ByteBuffer bb = getByteBuffer(id) ;
        bb.position(0) ;
        Block block = new Block(id, bb) ;
        return block ;
    }

    @Override
    public void write(Block block)
    {
        check(block) ;
        checkIfClosed() ;
        int id = block.getId() ;
        // Assumed MRSW - no need to sync as we are the only W
        segmentDirty[segment(id)] = true ;
        // No other work.
        writeNotification(block) ;
    }


    @Override
    public void sync()
    {
        checkIfClosed() ;
        force() ;
    }


    private ByteBuffer getByteBuffer(int id)
    {
        int seg = segment(id) ;                 // Segment.
        int segOff = byteOffset(id) ;           // Byte offset in segment

        if ( getLog().isTraceEnabled() ) 
            getLog().trace(format("%d => [%d, %d]", id, seg, segOff)) ;

        synchronized (this) {
            try {
                // Need to put the alloc AND the slice/reset inside a sync.
                ByteBuffer segBuffer = allocSegment(seg) ;
                // Now slice the buffer to get the ByteBuffer to return
                segBuffer.position(segOff) ;
                segBuffer.limit(segOff+blockSize) ;
                ByteBuffer dst = segBuffer.slice() ;
                
                // And then reset limit to max for segment.
                segBuffer.limit(segBuffer.capacity()) ;
                // Extend block count when we allocate above end. 
                numFileBlocks = Math.max(numFileBlocks, id+1) ;
                return dst ;
            } catch (IllegalArgumentException ex) {
                // Shouldn't (ha!) happen because the second "limit" resets 
                log.error("Id: "+id) ;
                log.error("Seg="+seg) ;
                log.error("Segoff="+segOff) ;
                log.error(ex.getMessage(), ex) ;
                throw ex ;
            }
        }
    }
    
    private final int segment(int id)                   { return id/blocksPerSegment ; }
    private final int byteOffset(int id)                { return (id%blocksPerSegment)*blockSize ; }
    private final long fileLocation(long segmentNumber) { return segmentNumber*SegmentSize ; }
    
    // Even for MultipleReader this needs to be sync'ed.
    private MappedByteBuffer allocSegment(int seg)
    {
        // Auxiliary function for get - which holds the lock needed here.
        // The MappedByteBuffer must be sliced and reset once found/allocated
        // so as not to mess up the underlying MappedByteBuffer in segments[].
        
        if ( seg < 0 )
        {
            getLog().error("Segment negative: "+seg) ;
            throw new FileException("Negative segment: "+seg) ;
        }

        while ( seg >= segments.length )
        {
            // More space needed.
            MappedByteBuffer[] segments2 = new MappedByteBuffer[GrowthFactor*segments.length] ;
            System.arraycopy(segments, 0, segments2, 0, segments.length) ;
            boolean[] segmentDirty2 = new boolean[GrowthFactor*segmentDirty.length] ;
            System.arraycopy(segmentDirty, 0, segmentDirty2, 0, segmentDirty.length) ;

            segmentDirty = segmentDirty2 ;
            segments = segments2 ;
        }
        
        long offset = fileLocation(seg) ;
        
        if ( offset < 0 )
        {
            getLog().error("Segment offset gone negative: "+seg) ;
            throw new FileException("Negative segment offset: "+seg) ;
        }
        
        // This, the relocation code above, and flushDirtySegements(), 
        // are the only places to directly access segments[] while running. 
        MappedByteBuffer segBuffer = segments[seg] ;
        if ( segBuffer == null )
        {
            try {
                segBuffer = channel.map(FileChannel.MapMode.READ_WRITE, offset, SegmentSize) ;
                if ( getLog().isDebugEnabled() )
                    getLog().debug(format("Segment: %d", seg)) ;
                segments[seg] = segBuffer ;
            } catch (IOException ex)
            {
                if ( ex.getCause() instanceof java.lang.OutOfMemoryError )
                    throw new FileException("BlockMgrMapped.segmentAllocate: Segment = "+seg+" : Offset = "+offset) ;
                throw new FileException("BlockMgrMapped.segmentAllocate: Segment = "+seg, ex) ;
            }
        }
        //segmentDirty[seg] = true ; // Old - why was it ever here?
        return segBuffer ;
    }

    private synchronized void flushDirtySegments()
    {
        // This does not force dirty segments to disk.
        super.force() ;
        
        // A linked list (with uniqueness) of dirty segments may be better.
        for ( int i = 0 ; i < segments.length ; i++ )
        {
            if ( segments[i] != null && segmentDirty[i] )
            {
                // Can we "flush" them all at once?
                segments[i].force() ;
                segmentDirty[i] = false ;
                segmentDirtyCount-- ;
            }
        }
    }

    @Override
    protected void _close()
    {
        force() ;
        // There is no unmap operation for MappedByteBuffers.
        // Sun Bug id bug_id=4724038
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4724038
       Arrays.fill(segments, null) ;
       Arrays.fill(segmentDirty, false) ;
       segmentDirtyCount = 0 ;
    }
    
    @Override
    protected void force()
    {
        flushDirtySegments() ;
        super.force() ;
    }
    
    @Override
    protected Logger getLog()
    {
        return log ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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