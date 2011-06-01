/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.junit.Before ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.Bytes ;
import tx.base.FileRef ;

import com.hp.hpl.jena.tdb.base.file.BufferChannel ;
import com.hp.hpl.jena.tdb.base.file.BufferChannelMem ;

public class TestJournal extends BaseTest
{
    static {
        FileRef.file("xyz") ;
        FileRef.file("abc") ;
    }
    
    // bb1 and bb2 have the same contents and different contents to bb3.
    static ByteBuffer bb1 = ByteBuffer.allocate(4) ;
    static ByteBuffer bb2 = ByteBuffer.allocate(4) ;
    static ByteBuffer bb3 = ByteBuffer.allocate(8) ;

    static
    {
        Bytes.setInt(0xFFABCD12, bb1.array()) ;
        Bytes.setInt(0x1234ABCD, bb2.array()) ;
        Bytes.setLong(0x2222222211111111L, bb3.array()) ;
    }

    
//    static FileRef fileref1 = FileRef.create("xyz") ;
//    static FileRef fileref2 = FileRef.create("abc") ;
//
//    
//    static BlockRef blockref1 = BlockRef.create(fileref1, 10) ;
//    static BlockRef blockref2 = BlockRef.create(fileref1, 10) ;
//
//    static BlockRef blockref3 = BlockRef.create(fileref1, 20) ;
//    static BlockRef blockref4 = BlockRef.create(fileref2, 10) ;
//    
//    // [TxTDB:TODO] Use these 
//    static JournalEntry je1 = new JournalEntry(10, blockref1, bb1) ;
//    static JournalEntry je2 = new JournalEntry(10, blockref2, bb2) ;
//    static JournalEntry je3 = new JournalEntry(10, blockref3, bb3) ;
//    static JournalEntry je4 = new JournalEntry(20, blockref1, bb1) ;
    
    
    Journal journal ;
    @Before public void before()
    {
        BufferChannel mem = new BufferChannelMem("journal") ;
        journal = new Journal(mem) ;
    }
    
    @Test public void journal_01()
    {
        assertFalse(journal.entries().hasNext()) ;
    }
    
    @Test public void journal_02()
    {
        JournalEntry entry1 = new JournalEntry(JournalEntryType.Block, bb1) ;
        long x = journal.writeJournal(entry1) ;
        assertEquals(0, x) ;
        JournalEntry entry9 = journal.readJournal(x) ;
        assertTrue(equal(entry1, entry9)) ;
    }

    @Test public void journal_03()
    {
        JournalEntry entry1 = new JournalEntry(JournalEntryType.Block, bb1) ;
        JournalEntry entry2 = new JournalEntry(JournalEntryType.Object, bb1) ;
        
        long x1 = journal.writeJournal(entry1) ;
        long x2 = journal.writeJournal(entry2) ;
        assertEquals(0, x1) ;
        assertNotEquals(0, x2) ;
        
        JournalEntry entry1a = journal.readJournal(x1) ;
        JournalEntry entry2a = journal.readJournal(x2) ;
        
        assertNotSame(entry1, entry1a) ;
        assertNotSame(entry2, entry2a) ;
        assertTrue(equal(entry1, entry1a)) ;
        assertTrue(equal(entry2, entry2a)) ;
        assertFalse(equal(entry1a, entry2a)) ;
    }
    
    @Test public void journal_04()
    {
        JournalEntry entry1 = new JournalEntry(JournalEntryType.Object, bb1) ;
        JournalEntry entry2 = new JournalEntry(JournalEntryType.Object, bb3) ;
        
        long x1 = journal.writeJournal(entry1) ;
        long x2 = journal.writeJournal(entry2) ;
        
        Iterator<JournalEntry> iter = journal.entries() ;
        JournalEntry entry1a = iter.next();
        JournalEntry entry2a = iter.next();
        assertFalse(iter.hasNext()) ;
        
    }

    private static boolean equal(JournalEntry entry1, JournalEntry entry2)
    {
        if ( entry1.getType() != entry2.getType()) return false ;
        return sameValue(entry1.getByteBuffer(), entry2.getByteBuffer()) ;
    }
    
    // In ByteBufferLib - remove/migrate
    public static boolean sameValue(ByteBuffer bb1, ByteBuffer bb2)
    {
        if ( bb1.capacity() != bb2.capacity() ) return false ;
        
        for ( int i = 0 ; i < bb1.capacity() ; i++ )
            if ( bb1.get(i) != bb2.get(i) ) return false ;
        return true ;
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