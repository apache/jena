/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.objectfile;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.ObjectFileWriteCacheSize ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfInt ;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.Pair ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.file.BufferChannel ;
import com.hp.hpl.jena.tdb.base.file.FileException ;

/** Variable length ByteBuffer file on disk. 
 *  Buffering for delayed writes.
 */  

public class ObjectFileStorage implements ObjectFile 
{
    /* 
     * No synchronization - assumes that the caller has some appropriate lock
     * because the combination of file and cache operations needs to be thread safe.
     * 
     * The position of the channel is assumed to be the end of the file always.
     * Read operations are done with absolute channel calls, 
     * which do not reset the position.
     * 
     * Writing is buffered.
     */
    
    // One disk file size.
    
    // This adds to the length of the file  
    private ByteBuffer lengthBuffer = ByteBuffer.allocate(SizeOfInt) ;
    
    // Delayed write buffer.
    private final ByteBuffer writeBuffer ;
    private int bufferSize ;
    
    private final BufferChannel file ;                // Access to storage
    private long filesize ;                     // Size of on-disk. 
    
    // Two-step write - alloc, write
    private boolean inAllocWrite = true ;
    private Block allocBlock = null ;
    private long allocLocation = -1 ;

    public ObjectFileStorage(BufferChannel file)
    {
        this(file, ObjectFileWriteCacheSize) ;
    }
    
    public ObjectFileStorage(BufferChannel file, int bufferSize)
    {
        this.file = file ;
        this.bufferSize = bufferSize ;
        filesize = file.size() ;
        writeBuffer = ByteBuffer.allocate(bufferSize) ;
    }
    
    @Override
    public long write(ByteBuffer bb)
    {
        inAllocWrite = false ;
        int len = bb.limit() - bb.position() ;
        
        if ( writeBuffer.limit()+len > writeBuffer.capacity() )
            // No room - flush.
            flushOutputBuffer() ;
        if ( writeBuffer.limit()+len > writeBuffer.capacity() )
        {
            long x = rawWrite(bb) ;
            return x ;
        }
        
        writeBuffer.put(bb) ;
        return len ;
    }
    
    private long rawWrite(ByteBuffer bb)
    {
        long location = filesize ;
        int x = file.write(bb, location) ;
        long loc2 = location+SizeOfInt ;
        x += file.write(bb, loc2) ;
        filesize = filesize+x ;
        return location ;
        
    }
    
    @Override
    public Block allocWrite(int maxBytes)
    {
        // Include space for length.
        int spaceRequired = maxBytes + SizeOfInt ;
        // Find space.
        if ( spaceRequired > writeBuffer.remaining() )
            flushOutputBuffer() ;
        
        if ( spaceRequired > writeBuffer.remaining() )
        {
            // Too big. have flushed buffering.
            inAllocWrite = true ;
            ByteBuffer bb = ByteBuffer.allocate(spaceRequired) ;
            allocBlock = new Block(filesize, bb) ;  
            allocLocation = -1 ;
            return allocBlock ;
        }
        
        // Will fit.
        inAllocWrite = true ;
        int start = writeBuffer.position() ;
        // id (but don't tell the caller yet).
        allocLocation = filesize+start ;
        
        // Slice it.
        writeBuffer.position(start + SizeOfInt) ;
        writeBuffer.limit(start+spaceRequired) ;
        ByteBuffer bb = writeBuffer.slice() ; 

        allocBlock = new Block(allocLocation, bb) ;
        return allocBlock ;
    }

    @Override
    public void completeWrite(Block block)
    {
        if ( ! inAllocWrite )
            throw new FileException("Not in the process of an allocated write operation pair") ;
        if ( allocBlock != block )
            throw new FileException("Wrong byte buffer in an allocated write operation pair") ;

        ByteBuffer buffer = block.getByteBuffer() ;
        
        if ( allocLocation == -1 )
        {
            // It was too big to use the buffering.
            rawWrite(buffer) ;
            return ;
        }
        
        int actualLength = buffer.limit()-buffer.position() ;
        // Insert object length
        int idx = (int)(allocLocation-filesize) ;
        writeBuffer.putInt(idx, actualLength) ;
        // And bytes to idx+actualLength+4 are used
        inAllocWrite = false;
        allocBlock = null ;
        int newLen = idx+actualLength+4 ;
        writeBuffer.position(newLen);
        writeBuffer.limit(writeBuffer.capacity()) ;
    }

    private void flushOutputBuffer()
    {
        long location = filesize ;
        writeBuffer.flip();
        int x = file.write(writeBuffer) ;
        filesize += x ;
        writeBuffer.clear() ;
    }


    @Override
    public ByteBuffer read(long loc)
    {
        inAllocWrite = false ;
        if ( loc < 0 )
            throw new IllegalArgumentException("ObjectFile.read: Bad read: "+loc) ;
        
        // Maybe it's in the in the write buffer.
        // Maybe the write buffer should keep more structure? 
        if ( loc >= filesize )
        {
            if ( loc > filesize+writeBuffer.capacity() )
                throw new IllegalArgumentException("ObjectFile.read: Bad read: "+loc) ;
            
            int x = writeBuffer.position() ;
            int y = writeBuffer.limit() ;
            
            int offset = (int)(loc-filesize) ;
            int len = writeBuffer.getInt(offset) ;
            int posn = offset + SizeOfInt ;
            // Slice the data bytes,
            writeBuffer.position(posn) ;
            writeBuffer.limit(posn+len) ;
            ByteBuffer bb = writeBuffer.slice() ;
            writeBuffer.limit(y) ;
            writeBuffer.position(x) ;
            return bb ; 
        }
        
        // No - it's in the underlying file storage.
        lengthBuffer.position(0) ;
        int x = file.read(lengthBuffer, loc) ;
        if ( x != 4 )
            throw new FileException("ObjectFile.read: Failed to read the length : got "+x+" bytes") ;
        int len = lengthBuffer.getInt(0) ;
        ByteBuffer bb = ByteBuffer.allocate(len) ;
        x = file.read(bb, loc+SizeOfInt) ;
        bb.flip() ;
        if ( x != len )
            throw new FileException("ObjectFile.read: Failed to read the object ("+len+" bytes) : got "+x+" bytes") ;
        return bb ;
    }
    
    @Override
    public long length()
    {
        return filesize ;
    }

    @Override
    public void close()                 { flushOutputBuffer() ; file.close() ; }

    @Override
    public void sync()                  { flushOutputBuffer() ; file.sync() ; }

    @Override
    public Iterator<Pair<Long, ByteBuffer>> all()
    {
        file.position(0) ; 
        ObjectIterator iter = new ObjectIterator(0, filesize) ;
        return iter ;
    }
    
    private class ObjectIterator implements Iterator<Pair<Long, ByteBuffer>>
    {
        final private long start ;
        final private long finish ;
        private long current ;

        public ObjectIterator(long start, long finish)
        {
            this.start = start ;
            this.finish = finish ;
            this.current = start ;
        }
        
        @Override
        public boolean hasNext()
        {
            return ( current < finish ) ;
        }

        @Override
        public Pair<Long, ByteBuffer> next()
        {
            long x = current ;
            ByteBuffer bb = read(current) ;
            current = current + bb.limit() + 4 ; 
            return new Pair<Long, ByteBuffer>(x, bb) ;
        }

        @Override
        public void remove()
        { throw new UnsupportedOperationException() ; }
    }
    
    // ---- Dump
    public void dump() { dump(handler) ; }

    public interface DumpHandler { void handle(long fileIdx, String str) ; }  
    
    public void dump(DumpHandler handler)
    {
        file.position(0) ; 
        long fileIdx = 0 ;
        while ( fileIdx < filesize )
        {
            ByteBuffer bb = read(fileIdx) ;
            String str = Bytes.fromByteBuffer(bb) ;
            handler.handle(fileIdx, str) ;
            fileIdx = fileIdx + bb.limit() + 4 ;
        }
    }
    
    static DumpHandler handler = new DumpHandler() {
        @Override
        public void handle(long fileIdx, String str)
        {
            System.out.printf("0x%08X : %s\n", fileIdx, str) ;
        }
    } ;
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