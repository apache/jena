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

package org.seaborne.dboe.trans.data;

import java.nio.ByteBuffer ;
import java.util.concurrent.Semaphore ;
import java.util.concurrent.atomic.AtomicReference ;

import com.hp.hpl.jena.query.ReadWrite ;

import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.After ;
import org.junit.Assert ;
import org.junit.Before ;
import org.junit.Test ;
import org.seaborne.dboe.base.file.BufferChannel ;
import org.seaborne.dboe.base.file.BufferChannelMem ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.transaction.Transactional ;
import org.seaborne.dboe.transaction.TransactionalFactory ;
import org.seaborne.dboe.transaction.Txn ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;

public class TestTransBlob extends Assert {
    private Journal         journal ;
    private TransBlob       transBlob ;
    private Transactional   transactional ;
    
    @Before public void before() {
        journal = Journal.create(Location.mem()) ;
        
        BufferChannel chan = BufferChannelMem.create("TestTransBlob") ;
        ComponentId cid = ComponentId.allocLocal() ;
        transBlob = new TransBlob(cid, chan) ;
        transactional = TransactionalFactory.create(journal, transBlob) ;    
    }

    @After public void after() { }
    
    public static void write(Transactional transactional, TransBlob transBlob, String data) {
        Txn.executeWrite(transactional, ()->{
            byte[] d = StrUtils.asUTF8bytes(data) ;
            ByteBuffer bb = ByteBuffer.wrap(d) ;
            transBlob.setBlob(bb); 
        }) ;
    }
    
    public static String read(Transactional transactional, TransBlob transBlob) {
        return Txn.executeReadReturn(transactional, ()->{
            ByteBuffer bb = transBlob.getBlob() ;
            if ( bb == null )
                return null ;
            return Bytes.fromByteBuffer(bb) ;
        }) ;
    }

    
    void threadRead(String expected) {
        AtomicReference<String> result = new AtomicReference<>() ;
        Semaphore testSemaImmediate = new Semaphore(0, true) ;
        new Thread( ()-> {
            String s = Txn.executeReadReturn(transactional, ()-> StrUtils.fromUTF8bytes(transBlob.getBlob().array())) ;
            result.set(s);
            testSemaImmediate.release(1) ;
        }).start() ;
        testSemaImmediate.acquireUninterruptibly();
        Assert.assertEquals(expected, result.get());
    }
    
    // testing with real files in TestTransBlobPersistent
    
    @Test public void transBlob_1() {
        String str = "Hello World" ; 
        write(transactional, transBlob, str) ;
        String str2 = read(transactional, transBlob) ;
        assertEquals(str, str2) ;
    }

    @Test public void transBlob_3() {
        String str1 = "one" ; 
        String str2 = "two" ;
        write(transactional, transBlob, str1) ;
        transactional.begin(ReadWrite.WRITE);
        byte[] d = StrUtils.asUTF8bytes(str2) ;
        ByteBuffer bb = ByteBuffer.wrap(d) ;
        transBlob.setBlob(bb); 
        threadRead(str1) ;
        transactional.commit() ;
        transactional.end() ;
        threadRead(str2) ;
    }
}

