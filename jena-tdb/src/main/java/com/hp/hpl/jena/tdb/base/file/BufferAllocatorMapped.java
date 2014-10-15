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

import java.io.File ;
import java.io.IOException ;
import java.nio.ByteBuffer ;
import java.nio.MappedByteBuffer ;
import java.nio.channels.FileChannel.MapMode ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.UUID ;

import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/**
 * ByteBuffer access to a temporary file, using memory mapped I/O.  The file will
 * grow in chunks as necessary by the SystemTDB.SegmentSize.
 * <p/>
 * This class is not thread-safe.
 */
final public class BufferAllocatorMapped implements BufferAllocator
{
    private final List<MappedByteBuffer> segments;

    private final int segmentSize = SystemTDB.SegmentSize;
    private final int blockSize;
    private final int blocksPerSegment;
    
    private final File tmpFile;
    private FileBase file;
    private int seq = 0;
    
    public BufferAllocatorMapped(int blockSize)
    {
        if (blockSize == 0 || blockSize > segmentSize)
            throw new IllegalArgumentException("Illegal block size: " + blockSize);
        if (segmentSize % blockSize != 0)
            throw new IllegalArgumentException(String.format("BufferAllocatorMapped: Segement size(%d) not a multiple of blocksize (%d)", segmentSize, blockSize)) ;
        
        this.blockSize = blockSize;
        blocksPerSegment = segmentSize/blockSize ;
        segments = new ArrayList<>();
        
        tmpFile = getNewTemporaryFile();
        tmpFile.deleteOnExit();
    }
    
    /**
     * Returns a handle to a temporary file.  Does not actually create the file on disk.
     */
    private final File getNewTemporaryFile()
    {
        File sysTempDir = new File(System.getProperty("java.io.tmpdir")) ;
        File tmpFile = new File(sysTempDir, "JenaTempByteBuffer-" + UUID.randomUUID().toString() + ".tmp") ;
        return tmpFile ;
    }
    
    private final int segment(int id)                   { return id/blocksPerSegment ; }
    private final int byteOffset(int id)                { return (id%blocksPerSegment)*blockSize ; }
    private final long fileLocation(long segmentNumber) { return segmentNumber*segmentSize ; }
    
    @Override
    public ByteBuffer allocate(int blkSize)
    {
        if ( blkSize != this.blockSize )
            throw new FileException("Fixed blocksize only: request= "+blkSize+"fixed size="+this.blockSize) ;
        
        // Create the file lazily
        if (null == file)
            file = FileBase.create(tmpFile.getPath());
        
        // Get and increment the id
        int id = seq++;
        int seg = segment(id);
        int segOff = byteOffset(id);
        
        MappedByteBuffer segBuffer;
        // See if we need to grow the file
        if (seg >= segments.size())
        {
            try
            {
                long offset = fileLocation(seg);
                segBuffer = file.channel().map(MapMode.READ_WRITE, offset, segmentSize) ;
                segments.add(segBuffer);
            }
            catch (IOException e)
            {
                throw new FileException("MappedFile.allocate: Segment= " + seg, e);
            }
        }
        else
        {
            segBuffer = segments.get(seg);
        }
        
        segBuffer.position(segOff);
        segBuffer.limit(segOff + blockSize);
        
        ByteBuffer toReturn = segBuffer.slice();
        
        segBuffer.limit(segBuffer.capacity());
        
        return toReturn;
    }
    
    @Override
    public void clear()
    {
        // Just reset to the start of the file, we'll allocate overtop of the old memory
        seq = 0;
    }
    
    @Override
    public void close()
    {
        // There is no unmap operation for MappedByteBuffers.
        // Sun Bug id bug_id=4724038
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4724038
        clear();
        segments.clear();
        file.close();
        file = null;
        
        // May not delete on Windows :/
        tmpFile.delete();
    }
}
