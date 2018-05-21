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

package org.apache.jena.dboe.trans.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer ;
import java.util.Arrays ;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.dboe.base.file.BufferChannel;
import org.apache.jena.dboe.base.file.BufferChannelFile;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.migrate.L;
import org.apache.jena.dboe.trans.data.TransBlob;
import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.dboe.transaction.txn.journal.Journal;
import org.apache.jena.dboe.transaction.txn.journal.JournalEntry;
import org.apache.jena.dboe.transaction.txn.journal.JournalEntryType;
import org.junit.* ;
import org.junit.rules.TemporaryFolder;

// We need something to recover io order to test recovery.

public class TestRecovery {

    @Rule
    public TemporaryFolder dir = new TemporaryFolder();

    private String journal ;
    private String data ;
    private String data1 ;
    private String data2 ;

    @Before public void before() {
        journal  = dir.getRoot().getAbsolutePath() + "/journal.jrnl" ;
        data  = dir.getRoot().getAbsolutePath() + "/blob.data" ;
        data1 = dir.getRoot().getAbsolutePath() + "/blob.data-1" ;
        data2 = dir.getRoot().getAbsolutePath() + "/blob.data-2" ;
        FileOps.ensureDir(dir.getRoot().getAbsolutePath());
        FileOps.deleteSilent(journal) ;
        FileOps.deleteSilent(data) ;
        FileOps.deleteSilent(data1) ;
        FileOps.deleteSilent(data2) ;
    }
    
    @After public void after() {
        FileOps.deleteSilent(journal) ;
        FileOps.deleteSilent(data) ;
    }
    
    // Fake journal recovery.
    @Test public void recoverBlobFile_1() throws Exception {
        String str = "Hello Journal" ; 
        ComponentId cid = ComponentId.allocLocal() ;
//        ComponentIdRegistry registry = new ComponentIdRegistry() ;
//        registry.register(cid, "Blob", 1) ;
        
        // Write out a journal.
        {
            Journal journal = Journal.create(Location.create(dir.getRoot().getAbsolutePath())) ;
            journal.write(JournalEntryType.REDO, cid, L.stringToByteBuffer(str)) ;
            journal.writeJournal(JournalEntry.COMMIT) ;
            journal.close(); 
        }
        
        TransactionCoordinator coord = new TransactionCoordinator(Location.create(dir.getRoot().getAbsolutePath())) ;
        BufferChannel chan = BufferChannelFile.create(data) ;
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
            Journal journal = Journal.create(Location.create(dir.getRoot().getAbsolutePath())) ;
            journal.write(JournalEntryType.REDO, cid1, L.stringToByteBuffer(str1)) ;
            journal.write(JournalEntryType.REDO, cid2, L.stringToByteBuffer(str2)) ;
            journal.writeJournal(JournalEntry.COMMIT) ;
            journal.close(); 
        }
        
        Journal journal = Journal.create(Location.create(dir.getRoot().getAbsolutePath())) ;
        BufferChannel chan = BufferChannelFile.create(data) ;
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

