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

package com.hp.hpl.jena.tdb.base.objectfile;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.ObjectFileWriteCacheSize ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfInt ;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.file.BufferChannel ;
import com.hp.hpl.jena.tdb.base.file.FileException ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** Variable length ByteBuffer file on disk. 
 *  Buffering for delayed writes.
 */  

public class ObjectFileStorage implements ObjectFile 
{
    private static Logger log = LoggerFactory.getLogger(ObjectFileStorage.class) ;
    public static boolean logging = false ;
    private void log(String fmt, Object... args)
    { 
        if ( ! logging ) return ;
        log.debug(state()+" "+String.format(fmt, args)) ;
    }
    
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
    
    // The object length slot.  
    private ByteBuffer lengthBuffer = ByteBuffer.allocate(SizeOfInt) ;
    
    // Delayed write buffer.
    private final ByteBuffer writeBuffer ;
    
    private final BufferChannel file ;              // Access to storage
    private long filesize ;                         // Size of on-disk. 
    
    // Two-step write - alloc, write
    private boolean inAllocWrite = false ;
    private Block allocBlock = null ;
    private long allocLocation = -1 ;
    
    // Old values for abort.
    int oldBufferPosn = -1 ;
    int oldBufferLimit = -1 ;


    public ObjectFileStorage(BufferChannel file)
    {
        this(file, ObjectFileWriteCacheSize) ;
    }
    
    public ObjectFileStorage(BufferChannel file, int bufferSize)
    {
        this.file = file ;
        filesize = file.size() ;
        this.file.position(filesize) ;  // End of file.
        log("File size: 0x%X, posn: 0x%X", filesize, file.position()) ;
        writeBuffer = (bufferSize >= 0) ? ByteBuffer.allocate(bufferSize) : null ;
    }
    
    @Override
    public long write(ByteBuffer bb)
    {
        log("W") ;
        
        if ( inAllocWrite )
            Log.fatal(this, "In the middle of an alloc-write") ;
        inAllocWrite = false ;
        if ( writeBuffer == null )
        {
            long x = rawWrite(bb) ;
            log("W -> 0x%X", x);
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
            if ( logging ) 
                log("W -> 0x%X", x);
            return x ;
        }
        
        long loc = writeBuffer.position()+filesize ;
        writeBuffer.putInt(len) ;
        writeBuffer.put(bb) ;
        if ( logging ) 
            log("W -> 0x%X", loc);
        return loc ;
    }
    
    private long rawWrite(ByteBuffer bb)
    {
        if ( logging ) 
            log("RW %s", bb) ;
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
        
        if ( logging )
        {
            log("Posn: %d", file.position());
            log("RW ->0x%X",location) ;
        }
        return location ;
    }
    
    @Override
    public Block allocWrite(int bytesSpace)
    {
        //log.info("AW("+bytesSpace+"):"+state()) ;
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
            //log.info("AW:"+state()+"-> ----") ;
            return allocBlock ;
        }
        
        // Will fit.
        inAllocWrite = true ;
        int start = writeBuffer.position() ;
        // Old values for restoration
        oldBufferPosn = start ;
        oldBufferLimit = writeBuffer.limit() ;
        
        // id (but don't tell the caller yet).
        allocLocation = filesize+start ;
        
        // Slice it.
        writeBuffer.putInt(bytesSpace) ;
        writeBuffer.position(start + SizeOfInt) ;
        writeBuffer.limit(start+spaceRequired) ;
        ByteBuffer bb = writeBuffer.slice() ;

        allocBlock = new Block(allocLocation, bb) ;

        if ( logging )
            log("AW: %s->0x%X", state(), allocLocation) ;
        return allocBlock ;
    }

    @Override
    public void completeWrite(Block block)
    {
        if ( logging ) 
            log("CW: %s @0x%X",block, allocLocation) ;
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
        allocLocation = -1 ;
        oldBufferPosn = -1 ;
        oldBufferLimit = -1 ;
    }

    @Override
    public void abortWrite(Block block)
    {
        allocBlock = null ;
        int oldstart = (int)(allocLocation-filesize) ;
        if ( oldstart != oldBufferPosn)
            throw new FileException("Wrong reset point: calc="+oldstart+" : expected="+oldBufferPosn) ;        
        
        writeBuffer.position(oldstart) ;
        writeBuffer.limit(oldBufferLimit) ;
        allocLocation = -1 ;
        oldBufferPosn = -1 ;
        oldBufferLimit = -1 ;
        inAllocWrite = false ;
    }

    private void flushOutputBuffer()
    {
        if ( logging )
            log("Flush") ;
        
        if ( writeBuffer == null ) return ;
        if ( writeBuffer.position() == 0 ) return ;

        if ( false )
        {
            String x = getLabel() ;
            if ( x.contains("nodes") ) 
            {
                long x1 = filesize ;
                long x2 = writeBuffer.position() ;
                long x3 = x1 + x2 ;
                System.out.printf("Flush(%s) : %d/0x%04X (%d/0x%04X) %d/0x%04X\n", getLabel(), x1, x1, x2, x2, x3, x3) ;
            }
        }
        
        long location = filesize ;
        writeBuffer.flip();
        int x = file.write(writeBuffer) ;
        filesize += x ;
        writeBuffer.clear() ;
    }

    @Override
    public void reposition(long posn)
    {
        if ( inAllocWrite )
            throw new FileException("In the middle of an alloc-write") ;
        if ( posn < 0 || posn > length() )
            throw new IllegalArgumentException("reposition: Bad location: "+posn) ;
        flushOutputBuffer() ;
        file.truncate(posn) ;
        filesize = posn ;
    }

    @Override
    public void truncate(long size)
    {
        //System.out.println("truncate: "+size+" ("+filesize+","+writeBuffer.position()+")") ;
        reposition(size) ;
    }

    @Override
    public ByteBuffer read(long loc)
    {
        if ( logging ) 
            log("R(0x%X)", loc) ;
        
        if ( inAllocWrite )
            throw new FileException("In the middle of an alloc-write") ;
        if ( loc < 0 )
            throw new IllegalArgumentException("ObjectFile.read["+file.getLabel()+"]: Bad read: "+loc) ;
        
        // Maybe it's in the in the write buffer.
        // Maybe the write buffer should keep more structure? 
        if ( loc >= filesize )
        {
            if ( loc >= filesize+writeBuffer.position() )
                throw new IllegalArgumentException("ObjectFileStorage.read["+file.getLabel()+"]: Bad read: location="+loc+" >= max="+(filesize+writeBuffer.position())) ;
            
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
            throw new FileException("ObjectFileStorage.read["+file.getLabel()+"]("+loc+")[filesize="+filesize+"][file.size()="+file.size()+"]: Failed to read the length : got "+x+" bytes") ;
        int len = lengthBuffer.getInt(0) ;
        // Sanity check. 
        if ( len > filesize-(loc+SizeOfInt) )
        {
            String msg = "ObjectFileStorage.read["+file.getLabel()+"]("+loc+")[filesize="+filesize+"][file.size()="+file.size()+"]: Impossibly large object : "+len+" bytes > filesize-(loc+SizeOfInt)="+(filesize-(loc+SizeOfInt)) ;
            SystemTDB.errlog.error(msg) ;
            throw new FileException(msg) ;
        }
        
        ByteBuffer bb = ByteBuffer.allocate(len) ;
        if ( len == 0 )
            // Zero bytes.
            return bb ;
        x = file.read(bb, loc+SizeOfInt) ;
        bb.flip() ;
        if ( x != len )
            throw new FileException("ObjectFileStorage.read: Failed to read the object ("+len+" bytes) : got "+x+" bytes") ;
        return bb ;
    }
    
    @Override
    public long length()
    {
        if ( writeBuffer == null ) return filesize ; 
        return filesize+writeBuffer.position() ;
    }
    
    @Override
    public boolean isEmpty()
    {
        if ( writeBuffer == null ) return filesize == 0  ;
        return writeBuffer.position() == 0 &&  filesize == 0 ; 
    }


    @Override
    public void close()                 { flushOutputBuffer() ; file.close() ; }

    @Override
    public void sync()                  { flushOutputBuffer() ; file.sync() ; }

    @Override
    public String getLabel()            { return file.getLabel() ; }
    
    @Override
    public String toString()            { return file.getLabel() ; }

    @Override
    public Iterator<Pair<Long, ByteBuffer>> all()
    {
        flushOutputBuffer() ;
        //file.position(0) ; 
        ObjectIterator iter = new ObjectIterator(0, filesize) ;
        //return iter ;
        
        if ( writeBuffer == null || writeBuffer.position() == 0 ) return iter ;
        return Iter.concat(iter, new BufferIterator(writeBuffer)) ;
    }
    
    private String state()
    {
        if ( writeBuffer == null )
            return String.format(getLabel()+": filesize=0x%X, file=(0x%X, 0x%X)", filesize, file.position(), file.size()) ;
        else
            return String.format(getLabel()+": filesize=0x%X, file=(0x%X, 0x%X), writeBuffer=(0x%X,0x%X)", filesize, file.position(), file.size(), writeBuffer.position(), writeBuffer.limit()) ;
        
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
            return new Pair<>((long)x, bb) ;
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
            // read, but reserving the file position.
            long x = current ;
            long filePosn = file.position() ;
            ByteBuffer bb = read(current) ;
            file.position(filePosn) ;
            current = current + bb.limit() + 4 ; 
            return new Pair<>(x, bb) ;
        }

        @Override
        public void remove()
        { throw new UnsupportedOperationException() ; }
    }
}
