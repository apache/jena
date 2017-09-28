/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.trans.recovery;

import java.nio.ByteBuffer ;
import java.util.Arrays ;

import org.apache.jena.atlas.lib.FileOps ;
import org.junit.* ;
import org.seaborne.dboe.base.file.BufferChannel ;
import org.seaborne.dboe.base.file.BufferChannelFile ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.migrate.L ;
import org.seaborne.dboe.trans.data.TransBlob ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;
import org.seaborne.dboe.transaction.txn.journal.JournalEntry ;
import org.seaborne.dboe.transaction.txn.journal.JournalEntryType ;

// We need something to recover io order to test recovery.

public class TestRecovery extends Assert {
    private final static String DIR   = "target/recovery" ;
    private final static String JRNL  = DIR + "/journal.jrnl" ;
    private final static String DATA  = DIR + "/blob.data" ;
    private final static String DATA1 = DIR + "/blob.data-1" ;
    private final static String DATA2 = DIR + "/blob.data-2" ;

    @BeforeClass public static void beforeClass() {
        FileOps.ensureDir(DIR);
    }
    
    @Before public void before() {
        FileOps.deleteSilent(JRNL) ;
        FileOps.deleteSilent(DATA) ;
        FileOps.deleteSilent(DATA1) ;
        FileOps.deleteSilent(DATA2) ;
    }
    
    @AfterClass public static void afterClass() {
        FileOps.deleteSilent(JRNL) ;
        FileOps.deleteSilent(DATA) ;
    }
    
    // Fake journal recovery.
    @Test public void recoverBlobFile_1() throws Exception {
        String str = "Hello Journal" ; 
        ComponentId cid = ComponentId.allocLocal() ;
//        ComponentIdRegistry registry = new ComponentIdRegistry() ;
//        registry.register(cid, "Blob", 1) ;
        
        // Write out a journal.
        {
            Journal journal = Journal.create(Location.create(DIR)) ;
            journal.write(JournalEntryType.REDO, cid, L.stringToByteBuffer(str)) ;
            journal.writeJournal(JournalEntry.COMMIT) ;
            journal.close(); 
        }
        
        TransactionCoordinator coord = new TransactionCoordinator(Location.create(DIR)) ;
        BufferChannel chan = BufferChannelFile.create(DATA) ;
        TransBlob tBlob = new TransBlob(cid, chan) ;
        coord.add(tBlob) ;
        coord.start();
        
        ByteBuffer blob = tBlob.getBlob() ;
        assertNotNull(blob); 
        String s = L.byteBufferToString(blob) ;
        assertEquals(str,s) ;
        coord.shutdown();
    }

    @Test public void recoverBlobFile_2() throws Exception {
        String str1 = "Recovery One" ; 
        String str2 = "Recovery Two" ; 
        ComponentId cid1 = ComponentId.allocLocal() ;
        ComponentId cid2 = ComponentId.allocLocal() ;
        
        // Write out a journal for two components.
        {
            Journal journal = Journal.create(Location.create(DIR)) ;
            journal.write(JournalEntryType.REDO, cid1, L.stringToByteBuffer(str1)) ;
            journal.write(JournalEntryType.REDO, cid2, L.stringToByteBuffer(str2)) ;
            journal.writeJournal(JournalEntry.COMMIT) ;
            journal.close(); 
        }
        
        Journal journal = Journal.create(Location.create(DIR)) ;
        BufferChannel chan = BufferChannelFile.create(DATA) ;
        TransBlob tBlob1 = new TransBlob(cid1, chan) ;
        TransBlob tBlob2 = new TransBlob(cid2, chan) ;

        TransactionCoordinator coord = new TransactionCoordinator(journal, Arrays.asList(tBlob1, tBlob2)) ;
        coord.start();
        
        ByteBuffer blob1 = tBlob1.getBlob() ;
        assertNotNull(blob1); 
        String s1 = L.byteBufferToString(blob1) ;
        assertEquals(str1,s1) ;
        
        ByteBuffer blob2 = tBlob2.getBlob() ;
        assertNotNull(blob2); 
        String s2 = L.byteBufferToString(blob2) ;
        assertEquals(str2,s2) ;

        assertNotEquals(str1,str2) ;
        coord.shutdown();
    }
}

