/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.record;

import static com.hp.hpl.jena.tdb.base.record.RecordLib.intToRecord;
import static com.hp.hpl.jena.tdb.base.record.RecordLib.recordToInt;

import com.hp.hpl.jena.tdb.base.record.Record;

import org.junit.Test;
import org.openjena.atlas.junit.BaseTest ;

public class TestRecord extends BaseTest
{
    static final public int RecLen = 4 ;
    
    @Test public void int1()
    {
        Record r = intToRecord(1234, 4) ;
        int v = recordToInt(r) ;
        assertEquals(v , 1234) ;
    }
    
    @Test public void int2()
    {
        // Negative numbers only work for length 4.
        Record r = intToRecord(-99, 4) ;
        int v = recordToInt(r) ;
        assertEquals(v , -99) ;
    }
    
    @Test public void record1()
    {
        Record r1 = intToRecord(1, RecLen) ;
        Record r2 = intToRecord(1, RecLen) ;
        assertTrue(Record.keyEQ(r1,r2)) ;
        assertTrue(Record.keyGE(r1,r2)) ;
        assertTrue(Record.keyLE(r1,r2)) ;
        assertFalse(Record.keyLT(r1,r2)) ;
        assertFalse(Record.keyGT(r1,r2)) ;
    }
    
    @Test public void record2()
    {
        Record r1 = intToRecord(1000, RecLen) ;
        Record r2 = intToRecord(2222, RecLen) ;
        assertFalse(Record.keyEQ(r1,r2)) ;
        assertFalse(Record.keyGE(r1,r2)) ;
        assertTrue(Record.keyLE(r1,r2)) ;
        assertTrue(Record.keyLT(r1,r2)) ;
        assertFalse(Record.keyGT(r1,r2)) ;
    }

    @Test public void record3()
    {
        Record r1 = intToRecord(1000, RecLen)  ;
        Record r2 = intToRecord(0, RecLen) ;
        assertFalse(Record.keyEQ(r1,r2)) ;
        assertTrue(Record.keyGE(r1,r2)) ;
        assertFalse(Record.keyLE(r1,r2)) ;
        assertFalse(Record.keyLT(r1,r2)) ;
        assertTrue(Record.keyGT(r1,r2)) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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