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

package org.apache.jena.tdb1.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.tdb1.base.objectfile.ObjectFile;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

/** Clone of AbstractTestObjectFileTrans that understands 2-file ObjectFileTransComplex */
public abstract class AbstractTestObjectFileTransComplex
{
    static long count = 0 ;
    ObjectFile file1 ;
    ObjectFile file2 ;
    ObjectFileTransComplex file ;
    Transaction txn ;
    
    abstract ObjectFile createFile(String basename) ;
    abstract void deleteFile(String basename) ;
    
    TransactionManager tm = null ;
    
    @Before
    public void setup()
    {
        txn = new Transaction(null, 5, TxnType.WRITE, ReadWrite.WRITE, ++count, TxnType.WRITE, null, tm) ;
        file1 = createFile("base") ;
        file2 = createFile("log") ;
    }

    @After
    public void teardown()
    {
        deleteFile("base") ;
        deleteFile("log") ;
    }
    
    static void write(ObjectFile file, String str)
    {
        byte b[] = StrUtils.asUTF8bytes(str) ;
        ByteBuffer bb = ByteBuffer.wrap(b) ;
        file.write(bb) ;
    }
    
    private static void contains(ObjectFile f, String... contents) 
    {
        Iterator<Pair<Long, ByteBuffer>> iter = f.all() ; 
        for ( String s : contents )
        {
            assertTrue(iter.hasNext()) ;
            Pair<Long, ByteBuffer> p = iter.next() ;
            String s2 = StrUtils.fromUTF8bytes(p.cdr().array()) ;
            assertEquals(s, s2) ;
        }
        
        assertFalse(iter.hasNext()) ;
    }
    

    private void init() { 
        file = new ObjectFileTransComplex(null, file1, file2) ;
    }
    
    static void fill(ObjectFile file, String... contents)
    {
        for ( String s : contents )
            write(file, s) ;
    }
    
    @Test public void objFileTrans_01()
    {
        init() ;
        contains(file) ;
    }
    
    @Test public void objFileTrans_02()
    {
        fill(file1, "ABC") ;
        init() ;
        
        file.begin(txn) ; 
        // Test empty. 
        contains(file2) ;
        file.commitPrepare(txn) ;
        file.enactCommitted(txn) ;
        contains(file1, "ABC") ;
        file.clearupCommitted(txn) ;
    }

    @Test public void objFileTrans_03()
    {
        fill(file1, "ABC") ;
        init() ;
        file.begin(txn) ; 
        write(file, "X") ;
        file.commitPrepare(txn) ;
        file.enactCommitted(txn) ;
        contains(file1, "ABC", "X") ;
        file.clearupCommitted(txn) ;
    }

    @Test public void objFileTrans_04()
    {
        fill(file1, "ABC", "ABC") ;
        init() ;
        file.begin(txn) ; 
        write(file, "ABCDEFGHIJKLMNOPQRSTUVWXYZ") ;
        file.commitPrepare(txn) ;
        file.enactCommitted(txn) ;
        contains(file1, "ABC", "ABC", "ABCDEFGHIJKLMNOPQRSTUVWXYZ") ;
        file.clearupCommitted(txn) ;
    }

    @Test public void objFileTrans_05()
    {
        fill(file1, "ABC") ;
        init() ;
        file.begin(txn) ; 
        write(file, "ABCDEF") ;
        file.abort(txn) ;
        contains(file1, "ABC") ;
        file.clearupCommitted(txn) ;
    }

    @Test public void objFileTrans_06()
    {
        fill(file1, "ABC", "123") ;
        init() ;
        file.begin(txn) ; 
        write(file, "ABCDEFGHIJKLMNOPQRSTUVWXYZ") ;
        file.abort(txn) ;
        contains(file1, "ABC", "123") ;
    }
}
