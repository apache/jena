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

package org.apache.jena.dboe.trans.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.dboe.base.file.*;
import org.apache.jena.system.Txn;
import org.apache.jena.dboe.trans.data.TransBinaryDataFile;
import org.apache.jena.dboe.transaction.Transactional;
import org.apache.jena.dboe.transaction.TransactionalFactory;
import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.journal.Journal;
import org.apache.jena.system.ThreadTxn;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

public class TestTransBinaryDataFile {
    private Journal journal ;
    private BinaryDataFile baseBinData ;
    private TransBinaryDataFile transBinData ;
    private Transactional transactional ;
    
    @Before public void before() {
        // XXX Builder.
        journal = Journal.create(Location.mem()) ;
        baseBinData = new BinaryDataFileMem() ;
        BufferChannel chan = FileFactory.createBufferChannelMem() ;
        ComponentId cid = ComponentId.allocLocal() ;
        transBinData = new TransBinaryDataFile(baseBinData, cid, chan) ;
        transBinData.open();
        transactional = TransactionalFactory.createTransactional(journal, transBinData) ;    
    }

    @After public void after() { }
    
//    private static ByteBuffer str2bb(String x) {
//        byte[] d = StrUtils.asUTF8bytes(x) ;
//        ByteBuffer bb = ByteBuffer.wrap(d) ;
//        return bb ;
//    }
    
    private static long writeOne(Transactional transactional, TransBinaryDataFile transBinaryFile, String data) {
        return 
            Txn.calculateWrite(transactional, ()->{
                byte[] bytes = StrUtils.asUTF8bytes(data) ;
                int len = bytes.length ;
                byte[] lenBytes = new byte[4] ;
                Bytes.setInt(len, lenBytes);
                // To work with strings, we write length,bytes.
                long x = transBinaryFile.write(lenBytes);
                transBinaryFile.write(bytes);
                return x ;
        }) ;
    }
    
    private static String readOne(Transactional transactional, TransBinaryDataFile transBinaryFile, long posn) {
        return Txn.calculateRead(transactional, ()->{
            byte[] lenBytes = new byte[4] ;
            long x = posn ;
            int got = transBinaryFile.read(x, lenBytes) ;
            x += got ;
            
            int len = Bytes.getInt(lenBytes) ;
            byte[] bytes = new byte[len] ;
            got = transBinaryFile.read(x, bytes) ;
            x += got ;
            return StrUtils.fromUTF8bytes(bytes) ;
        }) ;
    }
    
    @Test public void transObjectFile_str1() {
        String str = "Hello World" ; 
        long x = writeOne(transactional, transBinData, str) ;
        String str2 = readOne(transactional, transBinData, x) ;
        assertEquals(str, str2) ;
    }

    @Test public void transObjectFile_str2() {
        String str1 = "" ; 
        String str2 = "$" ; 
        long x1 = writeOne(transactional, transBinData, str1) ;
        long x2 = writeOne(transactional, transBinData, str2) ;
        assertNotEquals("("+x1+","+x2+")", x1, x2) ;
        
        String out2 = readOne(transactional, transBinData, x2) ;
        String out1 = readOne(transactional, transBinData, x1) ;
        assertEquals(str1, out1) ;
        assertEquals(str2, out2) ;
    }

    static byte[] bytes0 = new byte[0] ;
    static int len0 = 0 ;
    
    static byte[] bytes1 = new byte[] {(byte)1,(byte)2,(byte)3} ;
    static int len1 = bytes1.length ;
    
    static byte[] bytes2 = StrUtils.asUTF8bytes("TheNext") ;
    static int len2 = bytes2.length ;


    @Test public void transObjectFile_1() {
        Txn.executeWrite(transactional, ()->{
            long x = transBinData.write(bytes1) ;
            assertEquals(0L, x) ;
            assertEquals(len1, transBinData.length()) ;
        }) ;
    }
    
    @Test public void transObjectFile_2() {
        Txn.executeWrite(transactional, ()->{
            long x = transBinData.write(bytes1) ;
            assertEquals(0L, x) ;
            assertEquals(len1, transBinData.length()) ;
            byte[] bytes1a = new byte[len1] ;  
            int len = transBinData.read(0, bytes1a) ;
            assertEquals(len1, len) ;
            assertArrayEquals(bytes1, bytes1a);
        }) ;
    }

    @Test public void transObjectFile_3() {
        Txn.executeWrite(transactional, ()->{
            long x1 = transBinData.write(bytes1) ;
            long x2 = transBinData.write(bytes2) ;
            byte[] bytes1a = new byte[len1] ;  
            byte[] bytes2a = new byte[len2] ;
            int len1a = transBinData.read(0, bytes1a) ;
            int len2a = transBinData.read(len1a, bytes2a) ;
            assertEquals(len1, len1a) ;
            assertArrayEquals(bytes1, bytes1a);
            assertEquals(len2, len2a) ;
            assertArrayEquals(bytes2, bytes2a);
        }) ;
    }

    @Test public void transObjectFile_4() {
        Txn.executeWrite(transactional, ()->{
            long x1 = transBinData.write(bytes1) ;
            long x2 = transBinData.write(bytes2) ;
        }) ;
        Txn.executeRead(transactional, ()->{
            byte[] bytes1a = new byte[len1] ;  
            byte[] bytes2a = new byte[len2] ;
            int len1a = transBinData.read(0, bytes1a) ;
            int len2a = transBinData.read(len1a, bytes2a) ;
            assertEquals(len1, len1a) ;
            assertArrayEquals(bytes1, bytes1a);
            assertEquals(len2, len2a) ;
            assertArrayEquals(bytes2, bytes2a);
        }) ;
    }
    
    // As above but reverse the read order
    @Test public void transObjectFile_5() {
        Txn.executeWrite(transactional, ()->{
            long x1 = transBinData.write(bytes1) ;
            long x2 = transBinData.write(bytes2) ;
        }) ;
        Txn.executeRead(transactional, ()->{
            byte[] bytes1a = new byte[len1] ;  
            byte[] bytes2a = new byte[len2] ;
            
            int len2a = transBinData.read(len1, bytes2a) ;
            int len1a = transBinData.read(0, bytes1a) ;
            
            assertEquals(len1, len1a) ;
            assertArrayEquals(bytes1, bytes1a);
            assertEquals(len2, len2a) ;
            assertArrayEquals(bytes2, bytes2a);
        }) ;
    }


    @Test public void transObjectFile_6() {
        Txn.executeWrite(transactional, ()->{
            long x1 = transBinData.write(bytes1) ;
            long x2 = transBinData.write(bytes2) ;
        }) ;
        Txn.executeRead(transactional, ()->{
            byte[] bytes2a = new byte[len2] ;
            int len2a = transBinData.read(len1, bytes2a) ;
            assertEquals(len2, len2a) ;
            assertArrayEquals(bytes2, bytes2a);
        }) ;
    }
    
    @Test public void transObjectFile_7() {
        ThreadTxn.threadTxnWriteAbort(transactional, ()->{
            long x1 = transBinData.write(bytes1) ;
        }).run() ;
        
        Txn.executeRead(transactional, ()->{
            assertEquals(0L, transBinData.length()) ;
        }) ;
    }

}

