/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx.blockstream;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.nio.ByteBuffer ;
import java.util.Arrays ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.Lib ;
import tx.base.BlockRef ;

public class TestJournalEntryStreams extends BaseTest
{
    // bb1 and bb2 have the same contents and different contents to bb3.
    static ByteBuffer bb1 = ByteBuffer.allocate(4) ;
    static ByteBuffer bb2 = ByteBuffer.allocate(4) ;
    static ByteBuffer bb3 = ByteBuffer.allocate(4) ;
    
    static BlockRef fileref1 = BlockRef.create("xyz", 10) ;
    static BlockRef fileref2 = BlockRef.create("xyz", 10) ;

    static BlockRef fileref3 = BlockRef.create("xyz", 20) ;
    static BlockRef fileref4 = BlockRef.create("abc", 10) ;
    
    static JournalEntry je1 = new JournalEntry(10, fileref1, bb1) ;
    static JournalEntry je2 = new JournalEntry(10, fileref2, bb2) ;
    static JournalEntry je3 = new JournalEntry(10, fileref3, bb3) ;
    static JournalEntry je4 = new JournalEntry(20, fileref1, bb1) ;
    
    static
    {
        Bytes.setInt(0xABCD1234, bb1.array()) ;
        Bytes.setInt(0xABCD1234, bb2.array()) ;
        Bytes.setInt(0x11111111, bb3.array()) ;
    }
    
    @Test public void fileref_01()
    {
        assertEquals("Fileref hashcode", fileref1.hashCode(), fileref2.hashCode()) ;  
        assertEquals("Fileref equality", fileref1, fileref2) ;  
    }
    
    @Test public void fileref_02()
    {
        assertNotEquals("Fileref hashcode", fileref1.hashCode(), fileref3.hashCode()) ;  
        assertNotEquals("Fileref equality", fileref1, fileref3) ;  

        assertNotEquals("Fileref hashcode", fileref1.hashCode(), fileref4.hashCode()) ;  
        assertNotEquals("Fileref equality", fileref1, fileref4) ;  
    }
    
    @Test public void memStruct_01()
    {
        JournalEntryStreamMem.Output out = new JournalEntryStreamMem.Output() ;
        JournalEntryStreamMem.Input in = out.reverse() ;
        assertNull(in.read()) ;
    }
    
    @Test public void memStruct_02()
    {
        JournalEntryStreamMem.Output out = new JournalEntryStreamMem.Output() ;
        out.write(je1) ;
        JournalEntryStreamMem.Input in = out.reverse() ;
        JournalEntry je = in.read() ;
        assertTrue(equals(je1, je)) ;
        assertTrue(equals(je2, je)) ;
        assertFalse(equals(je3, je)) ;
        assertFalse(equals(je4, je)) ;
        
        assertNull(in.read()) ;
    }
    
    @Test public void memStruct_03()
    {
        JournalEntryStreamMem.Output out = new JournalEntryStreamMem.Output() ;
        out.write(je1) ;
        out.write(je3) ;
        out.close() ;
        
        JournalEntryStreamMem.Input in = out.reverse() ;
        
        JournalEntry je_1 = in.read() ;
        JournalEntry je_2 = in.read() ;
        assertTrue(equals(je1, je_1)) ;
        assertTrue(equals(je3, je_2)) ;
        
        assertNull(in.read()) ;
    }
    
    @Test public void mem_01()
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
        JournalEntryOutput out = new JournalEntryOutputStream(bout) ;
        JournalEntryInput in = new JournalEntryInputStream(new ByteArrayInputStream(bout.toByteArray())) ;
        assertNull(in.read()) ;
    }
    
    @Test public void mem_02()
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
        JournalEntryOutput out = new JournalEntryOutputStream(bout) ;
        out.write(je1) ;
        out.close();
        
        JournalEntryInput in = new JournalEntryInputStream(new ByteArrayInputStream(bout.toByteArray())) ;
        JournalEntry je = in.read() ;
        assertTrue(equals(je1, je)) ;
        assertTrue(equals(je2, je)) ;
        assertFalse(equals(je3, je)) ;
        assertFalse(equals(je4, je)) ;
        
        assertNull(in.read()) ;
    }
    
    @Test public void mem_03()
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
        JournalEntryOutput out = new JournalEntryOutputStream(bout) ;
        out.write(je1) ;
        out.write(je3) ;
        out.close() ;
        
        JournalEntryInput in = new JournalEntryInputStream(new ByteArrayInputStream(bout.toByteArray())) ;
        
        JournalEntry je_1 = in.read() ;
        JournalEntry je_2 = in.read() ;
        assertTrue(equals(je1, je_1)) ;
        assertTrue(equals(je3, je_2)) ;
        
        assertNull(in.read()) ;
    }
    
    private static boolean equals(JournalEntry jEntry1, JournalEntry jEntry2)
    {
        if ( jEntry1.getType() != jEntry2.getType() ) 
            return false ;
        if ( ! Lib.equal(jEntry1.getFileRef(), jEntry2.getFileRef()) )
            return false ;
        if ( ! equalsContent(jEntry1.getByteBuffer(), jEntry2.getByteBuffer()) )
            return false ;
        return true ;
    }

    private static boolean equalsContent(ByteBuffer bb1, ByteBuffer bb2)
    {
        byte[] b1 = bb1.array() ;
        byte[] b2 = bb1.array() ;
        return Arrays.equals(b1, b2) ;
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