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

import java.nio.ByteBuffer ;

import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.After ;
import org.junit.Assert ;
import org.junit.Before ;
import org.junit.Test ;
import org.seaborne.dboe.base.file.BufferChannel ;
import org.seaborne.dboe.base.file.FileFactory ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.objectfile.ObjectFile ;
import org.seaborne.dboe.jenax.Txn ;
import org.seaborne.dboe.transaction.Transactional ;
import org.seaborne.dboe.transaction.TransactionalFactory ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.TransactionalBase ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;
import org.seaborne.dboe.transaction.txn.journal.JournalEntry ;
import org.seaborne.dboe.transaction.txn.journal.JournalEntryType ;

public class TestTransObjectFile extends Assert {
    private Journal journal ;
    private ObjectFile baseObjectFile ;
    private TransObjectFile transObjectFile ;
    private Transactional transactional ;
    
    @Before public void before() {
        journal = Journal.create(Location.mem()) ;
        baseObjectFile = FileFactory.createObjectFileMem("ObjectFile") ;
        BufferChannel chan = FileFactory.createBufferChannelMem() ;
        ComponentId cid = ComponentId.allocLocal() ;
        transObjectFile = new TransObjectFile(baseObjectFile, cid, chan) ;
        transactional = TransactionalFactory.createTransactional(journal, transObjectFile) ;    
    }

    @After public void after() { }
    
    private static ByteBuffer str2bb(String x) {
        byte[] d = StrUtils.asUTF8bytes(x) ;
        ByteBuffer bb = ByteBuffer.wrap(d) ;
        return bb ;
    }
    
    private static long writeOne(Transactional transactional, TransObjectFile transObjectFile, String data) {
        return 
            Txn.execWriteRtn(transactional, ()->{
                ByteBuffer bb = str2bb(data) ;
                return transObjectFile.write(bb) ;
        }) ;
    }
    
    private static String readOne(Transactional transactional, TransObjectFile transObjectFile, long posn) {
        return Txn.execReadRtn(transactional, ()->{
            ByteBuffer bb = transObjectFile.read(posn) ;
            return Bytes.fromByteBuffer(bb) ;
        }) ;
    }
    
    @Test public void transObjectFile_1() {
        String str = "Hello World" ; 
        long x = writeOne(transactional, transObjectFile, str) ;
        String str2 = readOne(transactional, transObjectFile, x) ;
        assertEquals(str, str2) ;
    }

    @Test public void transObjectFile_2() {
        String str1 = "" ; 
        String str2 = "$" ; 
        long x1 = writeOne(transactional, transObjectFile, str1) ;
        long x2 = writeOne(transactional, transObjectFile, str2) ;
        assertNotEquals(x1, x2) ;
        
        String out2 = readOne(transactional, transObjectFile, x2) ;
        String out1 = readOne(transactional, transObjectFile, x1) ;
        assertEquals(str1, out1) ;
        assertEquals(str2, out2) ;
    }
    
    @Test public void transObjectFile_3() {
        String str = "Hello World" ; 
        long x = writeOne(transactional, transObjectFile, str) ;
        // Assume journal not truncated.
    }

    @Test public void transObjectFile_4() {
        String str1 = "Test4" ; 
        String str2 = "TheNext" ;
        long x1 = writeOne(transactional, transObjectFile, str1) ;
        ByteBuffer bb = str2bb(str2) ;
        long x2 = baseObjectFile.write(bb) ;
        baseObjectFile.sync();
        long x3 = Txn.execReadRtn(transactional, ()->transObjectFile.length()) ;
        assertEquals(x2, x3);
        assertNotEquals(x3, baseObjectFile.length());

        // Fake recovery.
        ByteBuffer bbj = ByteBuffer.allocate(2*Long.BYTES) ;
        bbj.putLong(x3) ;
        bbj.putLong(0) ;
        bbj.rewind() ;
        journal.write(JournalEntryType.REDO, transObjectFile.getComponentId(), bbj) ;
        journal.writeJournal(JournalEntry.COMMIT) ;
        // Recovery.
        //transObjectFile.recover(bb);
        TransactionalBase transBase = (TransactionalBase)TransactionalFactory.createTransactional(journal, transObjectFile) ;
        ByteBuffer bb1 = Txn.execReadRtn(transBase, ()->transObjectFile.read(x3)) ;
        String s1 = Bytes.fromByteBuffer(bb1) ;
        assertEquals(str2, s1);
        // Woot!
        transBase.getTxnMgr().shutdown();
    }
}

