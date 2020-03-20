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

import java.util.concurrent.atomic.AtomicReference;

import org.apache.jena.dboe.base.file.BufferChannel;
import org.apache.jena.dboe.base.file.BufferChannelMem;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.system.Txn;
import org.apache.jena.dboe.transaction.Transactional;
import org.apache.jena.dboe.transaction.TransactionalFactory;
import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.journal.Journal;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.system.ThreadAction;
import org.apache.jena.system.ThreadTxn;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestTransBlob extends Assert {
    private Journal         journal;
    private TransBlob       transBlob;
    private Transactional   transactional;

    @Before public void before() {
        journal = Journal.create(Location.mem());

        BufferChannel chan = BufferChannelMem.create("TestTransBlob");
        ComponentId cid = ComponentId.allocLocal();
        transBlob = new TransBlob(cid, chan);
        transactional = TransactionalFactory.createTransactional(journal, transBlob);
    }

    @After public void after() { }

    public static void write(Transactional transactional, TransBlob transBlob, String data) {
        Txn.executeWrite(transactional, ()->{
            transBlob.setString(data);
        });
    }

    public static String read(Transactional transactional, TransBlob transBlob) {
        return Txn.calculateRead(transactional, ()->{
            return transBlob.getString();
        });
    }

    void threadRead(String expected) {
        AtomicReference<String> result = new AtomicReference<>();
        ThreadTxn.threadTxnRead(transactional, ()-> {
            String s = transBlob.getString();
            result.set(s);
        }).run();
        Assert.assertEquals(expected, result.get());
    }

    // testing with real files in TestTransBlobPersistent

    @Test public void transBlob_1() {
        String str = "Hello World";
        write(transactional, transBlob, str);
        String str2 = read(transactional, transBlob);
        assertEquals(str, str2);
        String str3 = transBlob.getString();
        assertEquals(str, str3);

    }

    // Verify visibility and transactions.
    @Test public void transBlob_2() {
        String str1 = "one";
        String str2 = "two";
        write(transactional, transBlob, str1);
        transactional.begin(ReadWrite.WRITE);
        transBlob.setString(str2);

        // Difefrent therad and transaction.
        threadRead(str1);

        transactional.commit();
        transactional.end();
        threadRead(str2);
    }

    // Verify visibility and transactions.
    @Test public void transBlob_3() {
        String str1 = "one";
        String str2 = "two";
        write(transactional, transBlob, str1);
        String s1 = transBlob.getString();
        assertEquals(str1, s1);
        String s2 = read(transactional, transBlob);
        assertEquals(str1, s2);

        // Start now.
        ThreadAction tt = ThreadTxn.threadTxnRead(transactional, ()-> {
            String sr = transBlob.getString();
            Assert.assertEquals(str1, sr);
        });

        write(transactional, transBlob, str2);

        Txn.executeWrite(transactional, ()->{
            transBlob.setString(str2);
            String s = transBlob.getString();
            assertEquals(str2, s);
        });
        // Run later, after W transaction.
        tt.run();
    }
}

