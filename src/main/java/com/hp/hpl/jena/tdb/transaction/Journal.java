/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfInt ;
import static com.hp.hpl.jena.tdb.transaction.JournalEntryType.* ;
import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.openjena.atlas.iterator.IteratorSlotted ;
import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sync ;


import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.file.BufferChannel ;
import com.hp.hpl.jena.tdb.sys.FileRef ;

/** The Journal is slightly odd - it is append-only for write but random read.
 *  The write performance is more important than read; reads only happen
 *  if the journal grows to the point where it needs to free up cache. 
 */
public final
class Journal implements Iterable<JournalEntry>, Sync, Closeable
{
    // Version 1 : issue might be excessive copying
    // [TxTDB:TODO] Caching
    
    // Why synchronized?
    // Object handling - avoid length twice. 
    
    // We want random access AND stream efficiency to write.  Opps.
    // Also means we can't use DataOutputStream etc.
    
    BufferChannel channel ;
    private long position ;
    // Length, type, fileRef, [block id]
    // Length is length of variable part.
    public static int Overhead = 4*SizeOfInt ;
    public static final int NoId = 5 ;
    
//    byte[] _buffer = new byte[Overhead] ;
//    ByteBuffer header = ByteBuffer.wrap(_buffer) ;
    ByteBuffer header = ByteBuffer.allocate(Overhead) ;
    
    public Journal(BufferChannel channel)
    {
        this.channel = channel ;
        position = 0 ;
    }
    
    synchronized
    public long writeJournal(JournalEntry entry)
    {
        return  _write(entry.getType(), entry.getFileRef(), entry.getByteBuffer(), entry.getBlock()) ;
    }
    
    synchronized
    public long writeJournal(JournalEntryType type, FileRef fileRef, ByteBuffer buffer)
    {
        return _write(type, fileRef, buffer, null) ;
    }
    
    synchronized
    public long writeJournal(FileRef fileRef, Block block)
    {
        return _write(Block, fileRef, null, block) ;
    }
     
    synchronized
    private long _write(JournalEntryType type, FileRef fileRef, ByteBuffer buffer, Block block)
    {
        if ( buffer != null && block != null )
            throw new TDBTransactionException("Buffer and block to write") ;
        if ( block != null )
            buffer = block.getByteBuffer() ;

        if ( block != null && type != Block )
            throw new TDBTransactionException("Block to write but not block type") ;
        
        long posn = position ;
        int len = 0 ;
        
        // [TxDEV:TODO] CRC
        // [TxDEV:TODO] compress
        
        if ( buffer != null )
            len = buffer.remaining() ; 
        
        header.clear() ;
        header.putInt(type.id) ;
        header.putInt(len) ;
        header.putInt(fileRef.getId()) ;
        int blkId = (block==null) ? NoId : block.getId().intValue() ;
        header.putInt(blkId) ;
        header.flip() ;
        channel.write(header) ;
        
        if ( len > 0 )
        {
            int x = buffer.position() ;
            // Write bytes
            channel.write(buffer) ;
            buffer.position(x) ;
        }
        
        position += len+Overhead ;
        return posn ;
    }
    
    synchronized
    public JournalEntry readJournal(long id)
    {
        long x = channel.position() ;
        channel.position(id) ;
        JournalEntry entry = _read() ;
        channel.position(x) ;
        return entry ;
    }
    
    // read one entry at the channel position.
    // Move position to end of read.
    private JournalEntry _read()
    {
        // UGLY Maybe better to leave some space in the block's byte buffer.
        // [TxTDB:TODO] Make robust against partial read.
        header.clear() ;
        channel.read(header) ;
        header.rewind() ;
        int typeId  = header.getInt() ; 
        int len     = header.getInt() ;
        int ref     = header.getInt() ;
        int blockId = header.getInt() ;
        
        JournalEntryType type = JournalEntryType.type(typeId) ;
        FileRef fileRef = FileRef.get(ref) ;
        ByteBuffer bb = ByteBuffer.allocate(len) ;
        Block block = null ;
        channel.read(bb) ;
        bb.rewind() ;
        if ( type == Block )
        {
            block = new Block(blockId, bb) ;
            bb = null ;
        }
        else
            blockId = NoId ;
        return new JournalEntry(type, fileRef, bb, block) ;
    } 

    /** Iterator of entries from current point in Journal, going forward. Must be JournalEntry aligned at start. */
    private class IteratorEntries extends IteratorSlotted<JournalEntry>
    {
        JournalEntry slot = null ;
        boolean finished = false ;
        final long endPoint ;

        // MUST abstract out his Iterator pattern and leave
        // MoveToNext / Hasnext
        // See QueryIteratorBase
        public IteratorEntries() 
        {
            endPoint = channel.size() ;
        }
        
        @Override
        protected JournalEntry moveToNext()
        {
            return _read() ;
        }

        @Override
        protected boolean hasMore()
        {
            return channel.position() < endPoint  ;
        }
    }
    
    synchronized
    public Iterator<JournalEntry> entries()
    {
        channel.position(0) ;
        return new IteratorEntries() ;
    }
    
    @Override
    public Iterator<JournalEntry> iterator() { return entries() ; }

    @Override
    public void sync()  { channel.sync() ; }

    @Override
    public void close() { channel.close() ; }
}

/*
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