/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.sys.FileRef ;


public class JournalEntry
{
    static public final JournalEntry Commit = new JournalEntry(JournalEntryType.Commit) ;
    static public final JournalEntry Abort = new JournalEntry(JournalEntryType.Abort) ;
    static public final JournalEntry CheckPoint = new JournalEntry(JournalEntryType.Checkpoint) ;
    
    private long  position = -1 ;           // Location in the Journal (if known).
    private long  endPosition = -1 ;        // End location in the Journal: offset of next entry start.
    private final JournalEntryType type ;
    private final ByteBuffer byteBuffer ;   // One or other must be null - or both.
    private final Block block ;
    private final FileRef fileRef ;
    
    private JournalEntry(JournalEntryType type)
    {
        this(type, null, null, null) ;
    }
    
    public JournalEntry(JournalEntryType type, FileRef fileRef, ByteBuffer bytes)
    {
        this(type, fileRef, bytes, null) ;
    }
    
    public JournalEntry(FileRef fileRef, Block block)
    {
        this(JournalEntryType.Block, fileRef, null, block) ;
    }

    JournalEntry(JournalEntryType type, FileRef fileRef, ByteBuffer bytes, Block block)
    {
        if ( bytes != null && block != null )
            throw new TDBTransactionException("buffer != null and block != null") ;
        this.type = type ;
        this.byteBuffer = bytes ;
        this.block = block ;
        this.fileRef = fileRef ;
    }

    void setPosition(long posn)             { position = posn ; }
    void setEndPosition(long endPosn)       { endPosition = endPosn ; }

    public long getPosition()               { return position ; }
    long getEndPosition()                   { return endPosition ; }
    
    public JournalEntryType getType()       { return type ; }
    public ByteBuffer getByteBuffer()       { return byteBuffer ; }
    public Block getBlock()                 { return block ; }
    public FileRef getFileRef()             { return fileRef ; }
    
    @Override
    public String toString()
    {
        return "JournalEntry: "+type+" "+fileRef ;
    }
    
    static public String format(JournalEntry entry)
    {
        return format(entry.getType(), entry.getByteBuffer(), entry.getBlock(), entry.getFileRef()) ;
    }

    static public String format(JournalEntryType type, ByteBuffer byteBuffer, Block block, FileRef fileRef)
    {
        StringBuilder sbuff = new StringBuilder() ;
        
        sbuff.append("Entry: \n") ;
        if ( byteBuffer != null )
            sbuff.append("  "+byteBuffer) ;
        if ( block != null )
            sbuff.append("  "+block) ;
        sbuff.append("  "+fileRef) ;
        sbuff.append("  "+type) ;
        return sbuff.toString() ;
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