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

package org.seaborne.dboe.trans.data;

import org.apache.jena.util.FileUtils ;

import org.apache.jena.atlas.lib.FileOps ;
import org.junit.* ;
import org.seaborne.dboe.base.file.BufferChannel ;
import org.seaborne.dboe.base.file.BufferChannelFile ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.jenax.Txn;
import org.seaborne.dboe.transaction.Transactional ;
import org.seaborne.dboe.transaction.TransactionalFactory ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;

public class TestTransBlobPersistent extends Assert {
    private final static String DIR  = "target/blobtest" ;
    private final static String JRNL = DIR+"/journal.jrnl" ;
    private final static String DATA = DIR+"/blob.data" ;
    
    @BeforeClass public static void beforeClass() {
        FileOps.ensureDir(DIR);
    }
    
    @Before public void before() {
        FileOps.deleteSilent(JRNL) ;
        FileOps.deleteSilent(DATA) ;
    }
    
    @AfterClass public static void afterClass() {
        FileOps.deleteSilent(JRNL) ;
        FileOps.deleteSilent(DATA) ;
    }
    
    @Test public void transBlobFile_1() throws Exception {
        Journal journal = Journal.create(Location.create(DIR)) ;
        BufferChannel chan = BufferChannelFile.create(DATA) ;
        ComponentId cid = ComponentId.allocLocal() ;
        TransBlob transBlob = new TransBlob(cid, chan) ;
        Transactional transactional = TransactionalFactory.createTransactional(journal, transBlob) ;   
        String str = "Hello" ; 

        TestTransBlob.write(transactional, transBlob, str) ;
        
        chan.close() ;
        journal.close() ;
        String s = FileUtils.readWholeFileAsUTF8(DATA) ;
        assertEquals(str, s); 
    }

    @Test public void transBlobFile_2() throws Exception {
        Journal journal = Journal.create(Location.create(DIR)) ;
        BufferChannel chan = BufferChannelFile.create(DATA) ;
        ComponentId cid = ComponentId.allocLocal() ;
        TransBlob transBlob = new TransBlob(cid, chan) ;
        Transactional transactional = TransactionalFactory.createTransactional(journal, transBlob) ;   
        String str = "Hello1" ; 

        Txn.executeWrite(transactional, ()->{
            transBlob.setString("one");
        }) ;
        
        Txn.executeWrite(transactional, ()->{
            transBlob.setString("two");
        }) ;
        
        chan.close() ;
        journal.close() ;
        String s = FileUtils.readWholeFileAsUTF8(DATA) ;
        assertEquals("two", s); 
    }
    
    // restart.
    @Test public void transBlobFile_3() throws Exception {
        String str = "Hello World" ; 
        // Write out.
        {
            Journal journal = Journal.create(Location.create(DIR)) ;
            BufferChannel chan = BufferChannelFile.create(DATA) ;
            ComponentId cid = ComponentId.allocLocal() ;
            TransBlob transBlob = new TransBlob(cid, chan) ;
            Transactional transactional = TransactionalFactory.createTransactional(journal, transBlob) ;   
            TestTransBlob.write(transactional, transBlob, str) ;
            chan.close();
            journal.close(); 
        }
        // Restart
        {
            Journal journal = Journal.create(Location.create(DIR)) ;
            BufferChannel chan = BufferChannelFile.create(DATA) ;
            ComponentId cid = ComponentId.allocLocal() ;
            TransBlob transBlob = new TransBlob(cid, chan) ;
            Transactional transactional = TransactionalFactory.createTransactional(journal, transBlob) ;   
            String s = TestTransBlob.read(transactional, transBlob) ;
            assertEquals(str, s); 
        }
    }
}

