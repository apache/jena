/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.transaction.ObjectFileTrans ;
import com.hp.hpl.jena.tdb.transaction.Transaction ;

import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.Pair ;
import org.openjena.atlas.lib.StrUtils ;

public abstract class AbstractTestObjectFileTrans extends BaseTest
{
    static long count = 0 ;
    ObjectFile file1 ;
    ObjectFile file2 ;
    ObjectFileTrans file ;
    Transaction txn ;
    
    abstract ObjectFile createFile(String basename) ;
    abstract void deleteFile(String basename) ;
    
    @Before
    public void setup()
    {
        txn = new Transaction(null, null, ++count, null) ;
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
    

    private void init() { file = new ObjectFileTrans(null, file1, file2) ; } 
    
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
        contains(file2) ;
        file.commit(txn) ;
        contains(file1, "ABC") ;
    }

    @Test public void objFileTrans_03()
    {
        fill(file1, "ABC") ;
        init() ;
        file.begin(txn) ; 
        write(file, "X") ;
        file.commit(txn) ;
        contains(file1, "ABC", "X") ;
    }

    @Test public void objFileTrans_04()
    {
        fill(file1, "ABC", "ABC") ;
        init() ;
        file.begin(txn) ; 
        write(file, "ABCDEFGHIJKLMNOPQRSTUVWXYZ") ;
        file.commit(txn) ;
        contains(file1, "ABC", "ABC", "ABCDEFGHIJKLMNOPQRSTUVWXYZ") ;
    }

    @Test public void objFileTrans_05()
    {
        fill(file1, "ABC") ;
        init() ;
        file.begin(txn) ; 
        write(file, "ABCDEF") ;
        file.abort(txn) ;
        contains(file1, "ABC") ;
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

/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */