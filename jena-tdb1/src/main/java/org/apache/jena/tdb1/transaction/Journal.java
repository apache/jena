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

package org.apache.jena.tdb1.transaction;

import static org.apache.jena.tdb1.sys.SystemTDB.SizeOfInt;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.zip.Adler32;

import org.apache.jena.atlas.iterator.IteratorSlotted;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.tdb1.base.block.Block;
import org.apache.jena.tdb1.base.file.BufferChannel;
import org.apache.jena.tdb1.base.file.BufferChannelFile;
import org.apache.jena.tdb1.base.file.BufferChannelMem;
import org.apache.jena.tdb1.base.file.Location;
import org.apache.jena.tdb1.sys.FileRef;
import org.apache.jena.tdb1.sys.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The Journal is slightly odd - it is append-only for write but random read.
 *  The write performance is more important than read; reads only happen
 *  if the journal grows to the point where it needs to free up cache.
 */
public final
class Journal implements Closeable
{
    private static Logger log = LoggerFactory.getLogger(Journal.class);

    // Why synchronized?
    // Object handling - avoid length twice.

    // We want random access AND stream efficiency to write.  Opps.
    // Also means we can't use DataOutputStream etc.

    private Location location = null;
    private BufferChannel channel;
    private long position;
    // -- Journal write cycle used during Transaction.writerPrepareCommit.

    /*
    journal.startWrite();
    try {
        journal.write
        journal.commitWrite();
    } catch (Thowable ex) { journal.abortWrite(); }
    finally { journal.endWrite(); }
    */
    private long journalWriteStart = -1;
    private boolean journalWriteEnded = false;

    // Length, type, fileRef, [block id]
    // Length is length of variable part.
    private static int Overhead = 4*SizeOfInt;
    private static final int NoId = 5;

    private ByteBuffer header = ByteBuffer.allocate(Overhead);
    private static int SizeofCRC = SizeOfInt;
    private ByteBuffer crcTrailer = ByteBuffer.allocate(SizeofCRC);    // Adler: 32 bit.

    public static boolean exists(Location location)
    {
        if ( location.isMem() ) return false;
        return FileOps.exists(journalFilename(location));
    }

    public static Journal create(Location location)
    {
        return new Journal(location);
    }

    private static String journalFilename(Location location) { return location.absolute(Names.journalFile); }

    private Journal(Location location) {
        this(openFromLocation(location));
        this.location = location;
    }

    public /*testing*/ Journal(BufferChannel chan) {
        this.channel = chan;
        this.position = 0;
        this.location = null;
    }

    /** Forced reopen - thread.interrupt causes java to close file.
     * Attempt to close, open, and position.
     */
    public void reopen() {
        if ( location == null )
            // Can't reopen.
            return;
        if ( channel != null ) {
            try { channel.close(); }
            catch (Exception ex) { /*ignore*/ }
        }
        channel = openFromLocation(location);
        long posn = writeStartPosn();
        if ( posn >= 0 ) {
            truncate(posn);
            position = posn;
            sync();
        } else {
            position = channel.size();
        }
        writeReset();
    }

    private static BufferChannel openFromLocation(Location location) {
        String channelName = journalFilename(location);
        if ( location.isMem() )
            return BufferChannelMem.create(channelName);
        else
            return BufferChannelFile.create(channelName);
    }

    synchronized
    public long writeJournal(JournalEntry entry) {
        long posn = write(entry.getType(), entry.getFileRef(), entry.getBlock());
        if ( entry.getPosition() < 0 ) {
            entry.setPosition(posn);
            entry.setEndPosition(position);
        }
        return posn;
    }

    synchronized
    public long write(JournalEntryType type, FileRef fileRef, Block block) {
        // log.info("@"+position()+" -- "+type+","+fileRef+", "+buffer+", "+block);

        ByteBuffer buffer = (block == null) ? null : block.getByteBuffer();

        long posn = position;
        int bufferCapacity = 0;
        int len = 0;

        if ( buffer != null )
        {
            bufferCapacity = buffer.capacity();
            len = buffer.remaining();
        }

        header.clear();
        header.putInt(type.id);
        //header.putInt(len);
        header.putInt(bufferCapacity);     // Write whole buffer.
        header.putInt(fileRef.getId());
        int blkId = (block==null) ? NoId : block.getId().intValue();
        header.putInt(blkId);
        header.flip();
        channel.write(header);

        Adler32 adler = new Adler32();
        adler.update(header.array());

        if ( len > 0 )
        {
            // Make buffer include it's full length.
            // This is the full buffer, junk and all.
            // This makes the system able to check block sizes (BlockAccess checking).

            int bufferLimit = buffer.limit();
            int bufferPosition = buffer.position();
            buffer.position(0);
            buffer.limit(bufferCapacity);
            // Clear top.
            for ( int i = len; i < bufferCapacity; i++ )
                buffer.put(i, (byte)0);

            // Write all bytes
            channel.write(buffer);
            if ( buffer.hasArray() ) {
                adler.update(buffer.array());
            } else {
                byte[] data = new byte[bufferCapacity];
                buffer.position(0);
                buffer.limit(bufferCapacity);
                buffer.get(data);
                adler.update(data);
            }

            buffer.position(bufferPosition);
            buffer.limit(bufferLimit);
        }

        // checksum
        crcTrailer.clear();
        Bytes.setInt((int)adler.getValue(), crcTrailer.array());
        channel.write(crcTrailer);

        position += Overhead + len + SizeofCRC; // header + payload + checksum
        return posn;
    }

    synchronized
    public JournalEntry readJournal(long id) {
        return _readJournal(id);
    }

    private JournalEntry _readJournal(long id) {
        long x = channel.position();
        if ( x != id )
            channel.position(id);
        JournalEntry entry = _read();
        long x2 = channel.position();
        entry.setPosition(id);
        entry.setEndPosition(x2);

        if ( x != id )
            channel.position(x);
        return entry;
    }

    // read one entry at the channel position.
    // Move position to end of read.
    private JournalEntry _read() {
        header.clear();
        int lenRead = channel.read(header);
        if ( lenRead == -1 ) {
            // probably broken file.
            throw new TDBTransactionException("Read off the end of a journal file");
            // return null;
        }
        header.rewind();
        int typeId  = header.getInt();
        int len     = header.getInt();
        int ref     = header.getInt();
        int blockId = header.getInt();

        Adler32 adler = new Adler32();
        adler.update(header.array());

        ByteBuffer bb = ByteBuffer.allocate(len);
        lenRead = channel.read(bb);
        if ( lenRead != len)
            throw new TDBTransactionException("Failed to read the journal entry: wanted "+len+" bytes, got "+lenRead);
        adler.update(bb.array());
        bb.rewind();
        // checksum
        crcTrailer.clear();
        lenRead = channel.read(crcTrailer);
        if ( lenRead != SizeofCRC )
            throw new TDBTransactionException("Failed to read block checksum (got "+lenRead+" bytes, not "+SizeofCRC+").");
        int checksum = Bytes.getInt(crcTrailer.array());
        if ( checksum != (int)adler.getValue() )
        	throw new TDBTransactionException("Checksum error reading from the Journal.");

        JournalEntryType type = JournalEntryType.type(typeId);
        FileRef fileRef = FileRef.get(ref);

        Block block = new Block(blockId, bb);
        return new JournalEntry(type, fileRef, block);
    }

    /** Iterator of entries from current point in Journal, going forward. Must be JournalEntry aligned at start. */
    private class IteratorEntries extends IteratorSlotted<JournalEntry> {
        JournalEntry slot = null;
        final long endPoint;
        long iterPosn;

        public IteratorEntries(long startPosition) {
            iterPosn = startPosition;
            endPoint = channel.size();
        }

        @Override
        protected JournalEntry moveToNext() {
            synchronized (Journal.this) {
                if ( iterPosn >= endPoint )
                    return null;
                JournalEntry e = _readJournal(iterPosn);
                iterPosn = e.getEndPosition();
                return e;
            }
        }

        @Override
        protected boolean hasMore() {
            return iterPosn < endPoint;
        }
    }

    public Iterator<JournalEntry> entries()         { return new IteratorEntries(0); }

    synchronized
    public Iterator<JournalEntry> entries(long startPosition) {
        return new IteratorEntries(startPosition);
    }

    // -- Journal write cycle used during Transaction.writerPrepareCommit.

    /**
     * <pre>
     * journal.startWrite();
     * try {
     *         journal.write
     *         journal.commitWrite();
     * } catch (Thowable ex) { journal.abortWrite(); }
     * finally { journal.endWrite(); }
     * </pre>
     */

    public void startWrite() {
        journalWriteStart = this.position;
        journalWriteEnded = false;
    }

    public long writeStartPosn() { return journalWriteStart; }

    public void commitWrite() {
        journalWriteStart = -1;
        journalWriteEnded = true;
        channel.sync();
    }

    // Idempotent. Safe to call multiple times and after commit (when it has no effect).
    public void abortWrite() {
        if ( !journalWriteEnded && journalWriteStart > 0 ) {
            truncate(journalWriteStart);
            sync();
        }
        journalWriteEnded = true;
    }

    public void endWrite() {
        if ( ! journalWriteEnded )
            abortWrite();
        writeReset();
    }

    private void writeReset() {
        journalWriteStart = -1;
        journalWriteEnded = false;
    }
    // -- Journal write cycle.

    public void sync()  { channel.sync(); }

    @Override
    public void close() { channel.close(); }

    public long size()  { return channel.size(); }

    public boolean isEmpty()  { return channel.size() == 0; }

    public void truncate(long size) { channel.truncate(size); }

    public void append()    { position(size()); }

    public long position() { return channel.position(); }

    public void position(long posn) { channel.position(posn); }

    public String getFilename() { return channel.getFilename(); }
}
