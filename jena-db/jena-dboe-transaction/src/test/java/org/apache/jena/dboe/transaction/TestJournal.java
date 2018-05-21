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

package org.apache.jena.dboe.transaction;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertNotEquals ;
import static org.junit.Assert.assertNotNull ;
import static org.junit.Assert.assertTrue ;
import static org.junit.Assert.fail ;

import java.nio.ByteBuffer ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.lib.ByteBufferLib ;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.ComponentIds;
import org.apache.jena.dboe.transaction.txn.journal.Journal;
import org.apache.jena.dboe.transaction.txn.journal.JournalEntry;
import org.apache.jena.dboe.transaction.txn.journal.JournalEntryType;
import org.junit.Test ;

/** Journal tests spearate from the transaction coordinator */  
public class TestJournal {
    // For testing recovery, we need something to recover!
    // See tests in TestRecovery in dboe-trans-data 
    
    @Test public void journal_01() {
        Journal jrnl = Journal.create(Location.mem()) ;
        assertNotNull(jrnl) ;
        assertTrue(jrnl.isEmpty()) ;
    }
    
    @Test public void journal_02() {
        Journal jrnl = Journal.create(Location.mem()) ;
        assertNotNull(jrnl) ;
        JournalEntry e = JournalEntry.COMMIT ;
        jrnl.writeJournal(e) ;
        assertFalse(jrnl.isEmpty()) ;
        assertNotEquals(0, jrnl.position()) ;
    }

    @Test public void journal_03() {
        Journal jrnl = Journal.create(Location.mem()) ;
        assertNotNull(jrnl) ;
        jrnl.writeJournal(JournalEntry.COMMIT) ;
        assertNotEquals(0, jrnl.position()) ;
        JournalEntry e = jrnl.readJournal(0) ;
        check(JournalEntry.COMMIT, e) ;
        assertEquals(ComponentIds.idSystem, e.getComponentId());
        assertEquals(JournalEntryType.COMMIT, e.getType());
        check(JournalEntry.COMMIT, e) ;
    }

    @Test public void journal_04() {
        Journal jrnl = Journal.create(Location.mem()) ;
        jrnl.writeJournal(JournalEntry.COMMIT) ;
        jrnl.writeJournal(JournalEntry.ABORT) ;
        jrnl.writeJournal(JournalEntry.ABORT) ;
        jrnl.writeJournal(JournalEntry.COMMIT) ;
        assertFalse(jrnl.isEmpty()) ;
        
        Iterator<JournalEntry> iter = jrnl.entries(0) ;
        
        List<JournalEntry> expected = Arrays.asList(JournalEntry.COMMIT,
                                                    JournalEntry.ABORT,
                                                    JournalEntry.ABORT,
                                                    JournalEntry.COMMIT) ;
    }

    @Test public void journal_05() {
        Journal jrnl = Journal.create(Location.mem()) ;
        jrnl.writeJournal(JournalEntry.COMMIT) ;
        jrnl.writeJournal(JournalEntry.ABORT) ;
        long x = jrnl.writeJournal(JournalEntry.COMMIT) ;
        jrnl.writeJournal(JournalEntry.COMMIT) ;
        assertFalse(jrnl.isEmpty()) ;
        
        Iterator<JournalEntry> iter = jrnl.entries(x) ;
        
        List<JournalEntry> expected = Arrays.asList(JournalEntry.COMMIT,
                                                    JournalEntry.COMMIT) ;
    }
    
    @Test public void journal_06() {
        ByteBuffer bb = ByteBuffer.allocateDirect(100) ;
        ByteBufferLib.fill(bb, (byte)0XA5);
        Journal jrnl = Journal.create(Location.mem()) ;
        JournalEntry e = new JournalEntry(JournalEntryType.REDO, ComponentId.allocLocal(), bb) ;
        jrnl.writeJournal(e) ;
        jrnl.sync() ;
        JournalEntry e2 = jrnl.readJournal(0) ;
        check(e, e2);
    }

    @Test public void journal_07() {
        ByteBuffer bb = ByteBuffer.allocateDirect(100) ;
        ByteBufferLib.fill(bb, (byte)0XA5);
        Journal jrnl = Journal.create(Location.mem()) ;
        JournalEntry e = new JournalEntry(JournalEntryType.REDO, ComponentId.allocLocal(), bb) ;
        
        jrnl.writeJournal(JournalEntry.COMMIT) ;
        long locn = jrnl.position() ;
        jrnl.writeJournal(e) ;
        
        assertNotEquals(0, locn);
        jrnl.sync() ;
        JournalEntry e2 = jrnl.readJournal(locn) ;
        check(e, e2);
    }
    
    private void check(List<JournalEntry> expected, Iterator<JournalEntry> iter) {
        Iterator<JournalEntry> iter2 = expected.iterator() ;
        for(;;) {
            if ( ! iter.hasNext() || ! iter2.hasNext() )
                break ;
            check(iter2.next(), iter.next()) ;
        }
        if ( iter.hasNext() )
            fail("Journal iterator longer") ;
        if ( iter2.hasNext() )
            fail("Expected iterator longer") ;
    }

    private void check(JournalEntry expected, JournalEntry actual) {
        assertEquals(expected.getType(), actual.getType()) ;
        assertEquals(expected.getComponentId(), actual.getComponentId()) ; 
        assertEquals(expected.getByteBuffer(), actual.getByteBuffer()) ;  
    }
}

