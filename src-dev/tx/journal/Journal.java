/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx.journal;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfInt ;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import tx.IteratorSlotted ;

import com.hp.hpl.jena.tdb.base.file.BufferChannel ;

/** The Journal is slightly odd - it is append-only for write but random read.
 *  The write performance is more important than read; reads only happen
 *  if the journal grows to the point where it needs to free up cache. 
 */
public final
class Journal implements Iterable<JournalEntry>
{
    // Version 1 : issue might be excessive copying
    // [TxTDB:TODO] Caching
    
    // Why synchronized?
    // Object handling - avoid length twice. 
    
    // We want random access AND stream efficiency to write.  Opps.
    
    BufferChannel channel ;
    private long position ;
    // Length, type
    public static int Overhead = 2*SizeOfInt ;
    
    byte[] buffer = new byte[Overhead] ;
    ByteBuffer header = ByteBuffer.wrap(buffer) ;
    
    public Journal(BufferChannel channel)
    {
        this.channel = channel ;
        position = 0 ;
    }
    
    synchronized
    public long writeJournal(JournalEntry entry)
    {
        return  writeJournal(entry.getType(), entry.getByteBuffer()) ;
    }
    
    synchronized
    public long writeJournal(JournalEntryType type, ByteBuffer buffer)
    {
        long posn = position ;
        // [TxDEV:TODO] CRC

        // ?? buffer.position(0) ;
        int len = buffer.remaining() ; 
        
        header.clear() ;
        header.putInt(type.id) ;
        header.putInt(len) ;
        header.flip() ;
        
//        switch (type)
//        {
//            case Block:
//            case Object:
//            case Commit:
//            case Checkpoint:
//            default:
//        }
        
        channel.write(header) ;
        // Write bytes
        channel.write(buffer) ;
        
        position += len+Overhead ;
        return posn ;
    }
    
    synchronized
    public JournalEntry readJournal(long id)
    {
        long x = channel.position() ;
        channel.position(id) ;
        JournalEntry entry = read() ;
        channel.position(x) ;
        return entry ;
    }
    
    // read one entry at the channel position.
    // Move position to end of read.
    private JournalEntry read()
    {
        // [TxTDB:TODO] Make robust against partial read.
        header.clear() ;
        channel.read(header) ;
        header.rewind() ;
        int typeId = header.getInt() ; 
        int len = header.getInt() ;
        ByteBuffer bb = ByteBuffer.allocate(len) ;
        channel.read(bb) ;
        JournalEntryType type = JournalEntryType.type(typeId) ;
        return new JournalEntry(type, bb) ;
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
            return read() ;
        }

        @Override
        protected boolean hasMore()
        {
            return channel.position() < endPoint  ;
        }
        
//        @Override
//        public boolean hasNext()
//        {
//            if ( finished ) return false ;
//            if ( slot != null ) return true ;
//            if ( channel.position() >= endPoint )
//            {
//                finished = true ;
//                return false ;
//            }
//            
//            slot = read() ;
//            return true ;
//        }
//
//        @Override
//        public JournalEntry next()
//        {
//            if ( ! hasNext() )
//                throw new NoSuchElementException() ;
//            JournalEntry entry = slot ;
//            slot = null ;
//            return entry ;
//        }
//        
//        @Override
//        public void remove()
//        { throw new UnsupportedOperationException("remove") ; }
    }
    
    synchronized
    public Iterator<JournalEntry> entries()
    {
        channel.position(0) ;
        return new IteratorEntries() ;
    }
    
    @Override
    public Iterator<JournalEntry> iterator() { return entries() ; }
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