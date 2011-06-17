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

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.Pair ;
import org.openjena.atlas.logging.Log ;
import tx.IteratorSlotted ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.file.BufferChannel ;
import com.hp.hpl.jena.tdb.base.file.FileException ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

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
    
    private final BufferChannel file ;              // Access to storage
    private long filesize ;                         // Size of on-disk. 
    
    // Two-step write - alloc, write
    private boolean inAllocWrite = false ;
    private Block allocBlock = null ;
    private long allocLocation = -1 ;

    public ObjectFileStorage(BufferChannel file)
    {
        this(file, ObjectFileWriteCacheSize) ;
    }
    
    public ObjectFileStorage(BufferChannel file, int bufferSize)
    {
        this.file = file ;
        filesize = file.size() ;
        writeBuffer = (bufferSize >= 0) ? ByteBuffer.allocate(bufferSize) : null ;
    }
    
    @Override
    public long write(ByteBuffer bb)
    {
        if ( inAllocWrite )
            Log.fatal(this, "In the middle of an alloc-write") ;
        inAllocWrite = false ;
        if ( writeBuffer == null )
        {
            long x = rawWrite(bb) ;
            return x ;
        }
        
        int len = bb.limit() - bb.position() ;
        int spaceNeeded = len + SizeOfInt ;
        
        if ( writeBuffer.position()+spaceNeeded > writeBuffer.capacity() )
            // No room - flush.
            flushOutputBuffer() ;
        if ( writeBuffer.position()+spaceNeeded > writeBuffer.capacity() )
        {
            long x = rawWrite(bb) ;
            return x ;
        }
        
        long loc = writeBuffer.position()+filesize ;
        writeBuffer.putInt(len) ;
        writeBuffer.put(bb) ;
        return loc ;
    }
    
    private long rawWrite(ByteBuffer bb)
    {
        int len = bb.limit() - bb.position() ;
        lengthBuffer.rewind() ;
        lengthBuffer.putInt(len) ;
        lengthBuffer.flip() ;
        long location = file.position() ; 
        file.write(lengthBuffer) ;
        int x = file.write(bb) ;
        if ( x != len )
            throw new FileException() ;
        filesize = filesize+x+SizeOfInt ;
        return location ;
    }
    
    @Override
    public Block allocWrite(int bytesSpace)
    {
        if ( inAllocWrite )
            Log.fatal(this, "In the middle of an alloc-write") ;
        
        // Include space for length.
        int spaceRequired = bytesSpace + SizeOfInt ;
        
        // Find space.
        if (  writeBuffer != null && spaceRequired > writeBuffer.remaining() )
            flushOutputBuffer() ;
        
        if ( writeBuffer == null || spaceRequired > writeBuffer.remaining() )
        {
            // Too big. Have flushed buffering if buffering.
            inAllocWrite = true ;
            ByteBuffer bb = ByteBuffer.allocate(bytesSpace) ;
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
        writeBuffer.putInt(bytesSpace) ;
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
        if ( allocBlock != null && ( allocBlock.getByteBuffer() != block.getByteBuffer() ) )
            throw new FileException("Wrong byte buffer in an allocated write operation pair") ;

        inAllocWrite = false ;
        
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
        allocBlock = null ;
        int newLen = idx+actualLength+4 ;
        writeBuffer.position(newLen);
        writeBuffer.limit(writeBuffer.capacity()) ;
    }

    private void flushOutputBuffer()
    {
        if ( writeBuffer == null ) return ;
        if ( writeBuffer.position() == 0 ) return ;
        long location = filesize ;
        writeBuffer.flip();
        int x = file.write(writeBuffer) ;
        filesize += x ;
        writeBuffer.clear() ;
    }

    @Override
    public void reposition(long id)
    {
        if ( inAllocWrite )
            throw new FileException("In the middle of an alloc-write") ;
        if ( id < 0 || id > length() )
            throw new IllegalArgumentException("reposition: Bad location: "+id) ;
        flushOutputBuffer() ;
        file.truncate(id) ;
        filesize = id ;
    }

    @Override
    public ByteBuffer read(long loc)
    {
        if ( inAllocWrite )
            throw new FileException("In the middle of an alloc-write") ;
        if ( loc < 0 )
            throw new IllegalArgumentException("ObjectFile.read: Bad read: "+loc) ;
        
        // Maybe it's in the in the write buffer.
        // Maybe the write buffer should keep more structure? 
        if ( loc >= filesize )
        {
            if ( loc >= filesize+writeBuffer.position() )
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
        lengthBuffer.clear() ;
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
        if ( writeBuffer == null ) return filesize ; 
        return filesize+writeBuffer.position() ;
    }

    @Override
    public void close()                 { flushOutputBuffer() ; file.close() ; }

    @Override
    public void sync()                  { flushOutputBuffer() ; file.sync() ; }

    @Override
    public Iterator<Pair<Long, ByteBuffer>> all()
    {
        flushOutputBuffer() ;
        file.position(0) ; 
        ObjectIterator iter = new ObjectIterator(0, filesize) ;
        //return iter ;
        
        if ( writeBuffer == null || writeBuffer.position() == 0 ) return iter ;
        return Iter.concat(iter, new BufferIterator(writeBuffer)) ;
    }
    
    private class BufferIterator extends IteratorSlotted<Pair<Long, ByteBuffer>> implements Iterator<Pair<Long, ByteBuffer>>
    {
        private ByteBuffer buffer ;
        private int posn ;

        public BufferIterator(ByteBuffer buffer)
        {
            this.buffer = buffer ;
            this.posn = 0 ;
        }

        @Override
        protected Pair<Long, ByteBuffer> moveToNext()
        {
            if ( posn >= buffer.limit() )
                return null ;
            
            int x = buffer.getInt(posn) ;
            posn += SystemTDB.SizeOfInt ;
            ByteBuffer bb = ByteBuffer.allocate(x) ;
            int p = buffer.position() ;
            buffer.position(posn) ;
            buffer.get(bb.array()) ;
            buffer.position(p);
            posn += x ;
            return new Pair<Long, ByteBuffer>((long)x, bb) ;
        }

        @Override
        protected boolean hasMore()
        {
            return posn < buffer.limit();
        }

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