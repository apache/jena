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
import java.util.Iterator ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Bytes ;
import org.junit.Before ;
import org.junit.Test ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.file.BufferChannel ;
import com.hp.hpl.jena.tdb.base.file.BufferChannelMem ;
import com.hp.hpl.jena.tdb.sys.FileRef ;

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

    static Block blk1 = new Block(1, bb1) ;
    static Block blk2 = new Block(2, bb2) ;
    static Block blk3 = new Block(3, bb3) ;
    
    static FileRef testRef = FileRef.create("TEST") ;
    static FileRef testRef1 = FileRef.create("TEST1") ;
    static FileRef testRef2 = FileRef.create("TEST2") ;
    
    Journal journal ;
    @Before public void before()
    {
        BufferChannel mem = BufferChannelMem.create("journal") ;
        journal = new Journal(mem) ;
        bb1.clear() ;
        bb2.clear() ;
        bb3.clear() ;
    }
    
    @Test public void journal_01()
    {
        assertFalse(journal.entries().hasNext()) ;
    }
    
    @Test public void journal_02()
    {
        JournalEntry entry1 = new JournalEntry(JournalEntryType.Buffer, testRef, bb1) ;
        long x = journal.writeJournal(entry1) ;
        assertEquals(0, x) ;
        JournalEntry entry9 = journal.readJournal(x) ;
        assertTrue(equal(entry1, entry9)) ;
    }

    @Test public void journal_03()
    {
        JournalEntry entry1 = new JournalEntry(JournalEntryType.Buffer, testRef, bb1) ;
        JournalEntry entry2 = new JournalEntry(JournalEntryType.Object, testRef, bb1) ;
        
        long x1 = journal.writeJournal(entry1) ;
        bb1.clear() ;
        long x2 = journal.writeJournal(entry2) ;
        bb1.clear() ;
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
        JournalEntry entry1 = new JournalEntry(JournalEntryType.Object, testRef, bb1) ;
        JournalEntry entry2 = new JournalEntry(JournalEntryType.Object, testRef, bb3) ;
        
        long x1 = journal.writeJournal(entry1) ;
        long x2 = journal.writeJournal(entry2) ;
        
        Iterator<JournalEntry> iter = journal.entries() ;
        JournalEntry entry1a = iter.next();
        JournalEntry entry2a = iter.next();
        assertFalse(iter.hasNext()) ;
    }

    @Test public void journal_05()
    {
        JournalEntry entry1 = new JournalEntry(JournalEntryType.Buffer, testRef, bb1) ;
        JournalEntry entry2 = new JournalEntry(JournalEntryType.Buffer, testRef1, bb1) ;
        
        long x1 = journal.writeJournal(entry1) ;
        bb1.clear();
        long x2 = journal.writeJournal(entry2) ;
        bb1.clear();
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

    @Test public void journal_06()
    {
        JournalEntry entry1 = new JournalEntry(JournalEntryType.Block, testRef, blk1) ;
        JournalEntry entry2 = new JournalEntry(JournalEntryType.Block, testRef, blk2) ;
        
        long x1 = journal.writeJournal(entry1) ;
        bb1.clear();
        long x2 = journal.writeJournal(entry2) ;
        bb1.clear();
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

    private static boolean equal(JournalEntry entry1, JournalEntry entry2)
    {
        if ( entry1.getType() != entry2.getType())
            return false ;
        if ( ! entry1.getFileRef().equals(entry2.getFileRef()) )
            return false ;
        if ( entry1.getByteBuffer() == null && entry2.getByteBuffer() != null )
            return false ;
        if ( entry1.getByteBuffer() != null && entry2.getByteBuffer() == null )
            return false ; 
        if ( entry1.getBlock() == null && entry2.getBlock() != null )
            return false ;
        if ( entry1.getBlock() != null && entry2.getBlock() == null )
            return false ; 
        if ( entry1.getBlock() != null )
            return sameValue(entry1.getBlock(), entry2.getBlock()) ;
        else
            return sameValue(entry1.getByteBuffer(), entry2.getByteBuffer()) ;
    }
    
    // In ByteBufferLib - remove/migrate
    public static boolean sameValue(ByteBuffer bb1, ByteBuffer bb2)
    {
        if ( bb1 == null && bb2 == null ) return true ;
        if ( bb1 == null ) return false ;
        if ( bb2 == null ) return false ;
        
        if ( bb1.capacity() != bb2.capacity() ) return false ;
        
        int x1 = bb1.position() ; 
        int x2 = bb1.position() ;

        try {
            for ( int i = 0 ; i < bb1.capacity() ; i++ )
                if ( bb1.get(i) != bb2.get(i) ) return false ;
            return true ;
        } finally { bb1.position(x1) ; bb2.position(x2) ; }  
    }
    
    // In ByteBufferLib - remove/migrate
    public static boolean sameValue(Block bb1, Block bb2)
    {
        if ( bb1 == null && bb2 == null ) return true ;
        if ( bb1 == null ) return false ;
        if ( bb2 == null ) return false ;
        
        if ( bb1.getId() != bb2.getId() ) return false ;
        return  sameValue(bb1.getByteBuffer(), bb2.getByteBuffer()) ;
    }

}
