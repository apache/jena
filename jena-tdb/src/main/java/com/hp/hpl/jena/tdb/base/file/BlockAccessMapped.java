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

import static java.lang.String.format;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import static java.nio.channels.FileChannel.MapMode ;
import java.util.Arrays;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** FileAccess for a file, using memory mapped I/O */
final
public class BlockAccessMapped extends BlockAccessBase
{
    /* Blocks are addressed by positive ints - 
     * Is that a limit?
     * One billion is 2^30
     * If a block is 8K, the 2^31*2^13 =  2^44 bits or 2^14 billion = 16K Billion. = 16 trillion bytes.
     * No limit at the moment - later performance tuning will see what the cost of 48 or 63 bit addresses would be.    
     */
    
    private static Logger log = LoggerFactory.getLogger(BlockAccessMapped.class) ;
    private enum CopyContents { Overwrite, NoCopy } 
    
    // Segmentation avoids over-mapping; allows file to grow (in chunks) 
    private final int GrowthFactor = 2 ;
    private final int SegmentSize = SystemTDB.SegmentSize ;
    private final int blocksPerSegment ;                              
    
    private int initialNumSegements = 1 ;
    private MappedByteBuffer[] segments = new MappedByteBuffer[initialNumSegements] ;  
    
    // Unflushed segments.
    private int segmentDirtyCount = 0 ;
    private boolean[] segmentDirty = new boolean[initialNumSegements] ; 
    
    public BlockAccessMapped(String filename, int blockSize)
    {
        super(filename, blockSize) ;
        blocksPerSegment = SegmentSize/blockSize ;
        if ( SegmentSize%blockSize != 0 )
            getLog().warn(format("%s: Segment size(%d) not a multiple of blocksize (%d)", filename, SegmentSize, blockSize)) ;
        
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
    public Block read(long id)
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
        write(block, CopyContents.NoCopy) ;
    }
    
    @Override
    public void overwrite(Block block)
    {
        overwriteNotification(block) ;
        write(block, CopyContents.Overwrite) ;
    }

    private void write(Block block, CopyContents copyContents)
    {
        check(block) ;
        checkIfClosed() ;
        int id = block.getId().intValue() ;
        
        if ( copyContents == CopyContents.Overwrite )
        {
            ByteBuffer bbDst = getByteBuffer(id) ;
            bbDst.position(0) ;
            ByteBuffer bbSrc = block.getByteBuffer() ;
            bbSrc.rewind() ;
            bbDst.put(bbSrc) ;
        }
        
        // Assumed MRSW - no need to sync as we are the only Writer
        segmentDirty[segment(id)] = true ;
        writeNotification(block) ;
    }
    
    @Override
    public void sync()
    {
        checkIfClosed() ;
        force() ;
    }

    private ByteBuffer getByteBuffer(long _id)
    {
        // Limitation: ids must be integers.
        // ids are used to index into []-arrays.
        int id = (int)_id ;
        
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
        
        // Only allocSegment(seg) and flushDirtySegements() and close()
        // directly access segments[] 

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
        
        MappedByteBuffer segBuffer = segments[seg] ;
        if ( segBuffer == null )
        {
            try {
                segBuffer = file.channel().map(MapMode.READ_WRITE, offset, SegmentSize) ;
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
    
    @Override
    public String toString()
    {
        return super.getLabel() ;
    }
}
