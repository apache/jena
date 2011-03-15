/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package blockstream;

import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.List ;

import org.openjena.atlas.lib.ByteBufferLib ;

import com.hp.hpl.jena.tdb.TDBException ;

/** In-memory testing implementation of a JournalEntryStream (in and out)
 * using in-memory copy storage.
 * Better to use a ByteArray stream for tetsing, but his code can be useful in
 * looking at the log. 
 * Does a lot of copying to make the semantics exactly like a disk. 
 */

public class JournalEntryStreamMem
{
    
    public JournalEntryStreamMem() {}
    
    private static JournalEntry deepCopy(JournalEntry entry)
    {
        ByteBuffer bb = ByteBufferLib.duplicate(entry.getByteBuffer()) ;
        FileRef rf = copy(entry.getFileRef()) ;
        return new JournalEntry(entry.getType(), rf, bb) ;
    }
 
    private static FileRef copy(FileRef fileRef)
    {
        return new FileRef(fileRef.getFilename(), fileRef.getBlockId()) ;
    }
    
    public static class Input implements JournalEntryInput
    {
        private final List<JournalEntry> entries ;
        private int idx ;
        private boolean closed = false ;

        Input(List<JournalEntry> entries)
        {
            this.entries = entries ;
            idx = 0 ;
        }
        public void close()
        {
            if ( closed ) throw new TDBException("JournalEntryOutputStream has already been closed") ;
            closed = true ;
        }
        
        public JournalEntry read()
        {
            if ( idx >= entries.size() )
                return null ;
            JournalEntry e = entries.get(idx) ;
            idx++ ;
            // Modification to this do not effect the journal copy.
            e = deepCopy(e) ;
            return e ;
        }
    }
    
    public static class Output implements JournalEntryOutput
    {
        private List<JournalEntry> entries = new ArrayList<JournalEntry>() ;
        private boolean closed = false ;

        public Output() {}
        
        public void close()
        {
            if ( closed ) throw new TDBException("JournalEntryOutputStream has already been closed") ;
            closed = true ;
        }
        
        public void sync() {}

        public void write(JournalEntry entry)
        {
            if ( closed ) throw new TDBException("JournalEntryOutputStream has been closed") ;
            // Journal copy can't be modified
            entry = deepCopy(entry) ;
            entries.add(entry) ;
        }
        
        public Input reverse()
        {
            return new Input(entries) ;
        }

    }
    
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