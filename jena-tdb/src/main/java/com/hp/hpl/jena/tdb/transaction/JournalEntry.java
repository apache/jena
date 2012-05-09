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
    private final Block block ;
    private final FileRef fileRef ;
    
    private JournalEntry(JournalEntryType type)
    {
        this(type, null, (Block)null) ;
    }
    
//    public JournalEntry(JournalEntryType type, FileRef fileRef, ByteBuffer bytes)
//    {
//        this(type, fileRef, bytes, null) ;
//    }
//    
//    public JournalEntry(FileRef fileRef, Block block)
//    {
//        this(JournalEntryType.Block, fileRef, null, block) ;
//    }

    JournalEntry(JournalEntryType type, FileRef fileRef, ByteBuffer bytes)
    {
        this(type, fileRef, new Block(0, bytes)) ;
    }
    
    JournalEntry(JournalEntryType type, FileRef fileRef, Block block)
    {
        this.type = type ;
        this.block = block ;
        this.fileRef = fileRef ;
    }

    void setPosition(long posn)             { position = posn ; }
    void setEndPosition(long endPosn)       { endPosition = endPosn ; }

    public long getPosition()               { return position ; }
    long getEndPosition()                   { return endPosition ; }
    
    public JournalEntryType getType()       { return type ; }
    public ByteBuffer getByteBuffer()       { return block.getByteBuffer() ; }
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
