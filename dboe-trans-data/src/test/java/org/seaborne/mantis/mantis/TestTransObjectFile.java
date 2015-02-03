/**
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

package org.seaborne.mantis.mantis;

import java.nio.ByteBuffer ;

import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.After ;
import org.junit.Assert ;
import org.junit.Before ;
import org.junit.Test ;
import org.seaborne.dboe.base.file.FileFactory ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.objectfile.ObjectFile ;
import org.seaborne.dboe.transaction.Transactional ;
import org.seaborne.dboe.transaction.Txn ;
import org.seaborne.dboe.transaction.txn.TransactionalBase ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;

public class TestTransObjectFile extends Assert {
    private Journal journal ;
    private ObjectFile baseObjectFile ;
    private TransObjectFile transObjectFile ;
    private Transactional transactional ;
    
    @Before public void before() {
        journal = Journal.create(Location.mem()) ;
        baseObjectFile = FileFactory.createObjectFileMem("ObjectFile") ;
        transObjectFile = new TransObjectFile(baseObjectFile, 9) ;
        transactional = new TransactionalBase(journal, transObjectFile) ;    
    }

    @After public void after() { }
    
    private static long writeOne(Transactional transactional, TransObjectFile transObjectFile, String data) {
        return 
            Txn.executeWriteReturn(transactional, ()->{
                byte[] d = StrUtils.asUTF8bytes(data) ;
                ByteBuffer bb = ByteBuffer.wrap(d) ;
                return transObjectFile.write(bb) ;
        }) ;
    }
    
    private static String readOne(Transactional transactional, TransObjectFile transObjectFile, long posn) {
        return Txn.executeReadReturn(transactional, ()->{
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


}

