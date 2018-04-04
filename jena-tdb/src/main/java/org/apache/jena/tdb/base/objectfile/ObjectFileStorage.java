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

package org.apache.jena.tdb.base.objectfile;

import static org.apache.jena.tdb.sys.SystemTDB.ObjectFileWriteCacheSize;
import static org.apache.jena.tdb.sys.SystemTDB.SizeOfInt;

import java.nio.ByteBuffer;
import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorSlotted;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.tdb.base.file.BufferChannel;
import org.apache.jena.tdb.base.file.FileException;
import org.apache.jena.tdb.sys.SystemTDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Variable length ByteBuffer file on disk. 
 *  Buffering for delayed writes.
 */  

public class ObjectFileStorage implements ObjectFile 
{
    private static Logger log = LoggerFactory.getLogger(ObjectFileStorage.class);
    public static boolean logging = false;
    private void log(String fmt, Object... args) { 
        if ( ! logging ) return;
        log.debug(state()+" "+String.format(fmt, args));
    }
    
    /* 
     * No synchronization excpet for the write buffer.
     * This code assumes that the caller has some appropriate lock
     * because the combination of file and cache operations needs to be thread safe.
     * 
     * The position of the channel is assumed to be the end of the file always.
     * Read operations are done with absolute channel calls, 
     * which do not reset the position.
     * 
     * Writing is buffered.
     */
    
    private final Object lockWriteBuffer = new Object();
    private final ByteBuffer writeBuffer;
    
    private final BufferChannel file;              // Access to storage
    private volatile long filesize;                // Size of on-disk. 
    
    public ObjectFileStorage(BufferChannel file) {
        this(file, ObjectFileWriteCacheSize);
    }

    public ObjectFileStorage(BufferChannel file, int bufferSize) {
        this.file = file;
        filesize = file.size();
        this.file.position(filesize);  // End of file.
        log("File size: 0x%X, posn: 0x%X", filesize, file.position());
        writeBuffer = (bufferSize >= 0) ? ByteBuffer.allocate(bufferSize) : null;
    }

    @Override
    synchronized public long write(ByteBuffer bb) {
        log("W");

        if ( writeBuffer == null ) {
            long x = rawWrite(bb);
            log("W -> 0x%X", x);
            return x;
        }

        int len = bb.limit() - bb.position();
        int spaceNeeded = len + SizeOfInt;

        synchronized (lockWriteBuffer) {
            if ( writeBuffer.position() + spaceNeeded > writeBuffer.capacity() )
                // No room - flush.
                flushOutputBuffer();
            if ( writeBuffer.position() + spaceNeeded > writeBuffer.capacity() ) {
                long x = rawWrite(bb);
                if ( logging )
                    log("W -> 0x%X", x);
                return x;
            }

            long loc = writeBuffer.position() + filesize;
            writeBuffer.putInt(len);
            writeBuffer.put(bb);
            if ( logging )
                log("W -> 0x%X", loc);
            return loc;
        }
    }

    // The object length slot.
    private ByteBuffer writeLengthBuffer = ByteBuffer.allocate(SizeOfInt);

    private long rawWrite(ByteBuffer bb) {
        if ( logging )
            log("RW %s", bb);
        int len = bb.limit() - bb.position();
        writeLengthBuffer.rewind();
        writeLengthBuffer.putInt(len);
        writeLengthBuffer.flip();
        long location = file.position();
        file.write(writeLengthBuffer);
        int x = file.write(bb);
        if ( x != len )
            throw new FileException();
        filesize = filesize + x + SizeOfInt;

        if ( logging ) {
            log("Posn: %d", file.position());
            log("RW ->0x%X", location);
        }
        return location;
    }

    private void flushOutputBuffer() {
        if ( logging )
            log("Flush");

        if ( writeBuffer == null )
            return;
        if ( writeBuffer.position() == 0 )
            return;
        long location = filesize;
        writeBuffer.flip();
        int x = file.write(writeBuffer);
        filesize += x;
        writeBuffer.clear();
    }

    @Override
    public void reposition(long posn) {
        if ( posn < 0 || posn > length() )
            throw new IllegalArgumentException("reposition: Bad location: " + posn);
        flushOutputBuffer();
        file.truncate(posn);
        filesize = posn;
    }

    @Override
    public void truncate(long size) {
        // System.out.println("truncate: "+size+"
        // ("+filesize+","+writeBuffer.position()+")");
        reposition(size);
    }

    @Override
    public ByteBuffer read(long loc) {
        if ( logging )
            log("R(0x%X)", loc);

        if ( loc < 0 )
            throw new IllegalArgumentException("ObjectFile.read[" + file.getLabel() + "]: Bad read: " + loc);

        // Maybe it's in the in the write buffer.
        if ( loc >= filesize ) {
            // This path should be uncommon.
            synchronized (lockWriteBuffer) {
                if ( loc >= filesize + writeBuffer.position() )
                    throw new IllegalArgumentException("ObjectFileStorage.read[" + file.getLabel() + "]: Bad read: location=" + loc
                                                       + " >= max=" + (filesize + writeBuffer.position()));
                int offset = (int)(loc - filesize);
                int len = writeBuffer.getInt(offset);
                int posn = offset + SizeOfInt;
                ByteBuffer bb1 = ByteBuffer.allocate(len);
                for ( int i = 0; i < len; i++ )
                    bb1.put(i, writeBuffer.get(posn + i));
                return bb1;
            }
        }

        // No - it's in the underlying file storage.
        ByteBuffer lengthBuffer = ByteBuffer.allocate(SizeOfInt);

        lengthBuffer.clear();
        int x = file.read(lengthBuffer, loc);
        if ( x != 4 ) {
            String msg = "ObjectFileStorage.read[" + file.getLabel() + "](" + loc + ")[filesize=" + filesize + "]"
                         + "[file.size()=" + file.size() + "]: Failed to read the length : got " + x + " bytes";
            lengthBuffer.clear();
            int x1 = file.read(lengthBuffer, loc);
            throw new FileException(msg);
        }
        int len = lengthBuffer.getInt(0);
        // Sanity check.
        if ( len > filesize - (loc + SizeOfInt) ) {
            String msg = "ObjectFileStorage.read[" + file.getLabel() + "](" + loc + ")[filesize=" + filesize + "][file.size()="
                         + file.size() + "]: Impossibly large object : " + len + " bytes > filesize-(loc+SizeOfInt)="
                         + (filesize - (loc + SizeOfInt));
            throw new FileException(msg);
        }

        ByteBuffer bb = ByteBuffer.allocate(len);
        if ( len == 0 )
            // Zero bytes.
            return bb;
        x = file.read(bb, loc + SizeOfInt);
        bb.flip();
        if ( x != len )
            throw new FileException("ObjectFileStorage.read: Failed to read the object (" + len + " bytes) : got " + x + " bytes");
        return bb;
    }

    @Override
    public long length() {
        if ( writeBuffer == null )
            return filesize;
        return filesize + writeBuffer.position();
    }

    @Override
    public boolean isEmpty() {
        if ( writeBuffer == null )
            return filesize == 0;
        return writeBuffer.position() == 0 && filesize == 0;
    }

    @Override
    public void close()                 { flushOutputBuffer(); file.close(); }

    @Override
    public void sync()                  { flushOutputBuffer(); file.sync(); }

    @Override
    public String getLabel()            { return file.getLabel(); }
    
    @Override
    public String toString()            { return file.getLabel(); }

    @Override
    public Iterator<Pair<Long, ByteBuffer>> all() {
        flushOutputBuffer();
        // file.position(0);
        ObjectIterator iter = new ObjectIterator(0, filesize);
        if ( writeBuffer == null || writeBuffer.position() == 0 )
            return iter;
        return Iter.concat(iter, new BufferIterator(writeBuffer));
    }

    private String state() {
        if ( writeBuffer == null )
            return String.format(getLabel() + ": filesize=0x%X, file=(0x%X, 0x%X)", filesize, file.position(), file.size());
        else
            return String.format(getLabel() + ": filesize=0x%X, file=(0x%X, 0x%X), writeBuffer=(0x%X,0x%X)", filesize, file.position(),
                file.size(), writeBuffer.position(), writeBuffer.limit());

    }

    private class BufferIterator extends IteratorSlotted<Pair<Long, ByteBuffer>> implements Iterator<Pair<Long, ByteBuffer>> {
        private ByteBuffer buffer;
        private int        posn;

        public BufferIterator(ByteBuffer buffer) {
            this.buffer = buffer;
            this.posn = 0;
        }

        @Override
        protected Pair<Long, ByteBuffer> moveToNext() {
            if ( posn >= buffer.limit() )
                return null;

            int x = buffer.getInt(posn);
            posn += SystemTDB.SizeOfInt;
            ByteBuffer bb = ByteBuffer.allocate(x);
            int p = buffer.position();
            buffer.position(posn);
            buffer.get(bb.array());
            buffer.position(p);
            posn += x;
            return new Pair<>((long)x, bb);
        }

        @Override
        protected boolean hasMore() {
            return posn < buffer.limit();
        }
    }

    private class ObjectIterator implements Iterator<Pair<Long, ByteBuffer>> {
        final private long start;
        final private long finish;
        private long       current;

        public ObjectIterator(long start, long finish) {
            this.start = start;
            this.finish = finish;
            this.current = start;
        }

        @Override
        public boolean hasNext() {
            return (current < finish);
        }

        @Override
        public Pair<Long, ByteBuffer> next() {
            // read, but reserving the file position.
            long x = current;
            long filePosn = file.position();
            ByteBuffer bb = read(current);
            file.position(filePosn);
            current = current + bb.limit() + 4;
            return new Pair<>(x, bb);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
