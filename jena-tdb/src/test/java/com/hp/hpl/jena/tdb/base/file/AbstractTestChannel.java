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

package com.hp.hpl.jena.tdb.base.file;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.base.file.BufferChannel ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

public abstract class AbstractTestChannel extends BaseTest
{
    protected abstract BufferChannel open() ;
    
    private BufferChannel store ;
    @Before public void before() { store = open() ; }
    @After  public void after()  { store.close() ; }
    
    static final int blkSize = 100 ;
    
    protected static ByteBuffer data(int len)
    {
        ByteBuffer b = ByteBuffer.allocate(len) ;
        for (int i = 0 ; i < len ; i++ )
            b.put((byte)(i&0xFF)) ;
        b.clear() ;
        return b ;
    }
    
    protected static boolean same(ByteBuffer bb1, ByteBuffer bb2)
    {
        if ( bb1.capacity() != bb2.capacity() ) return false ;
        bb1.clear() ;
        bb2.clear() ;
        for ( int i = 0 ; i < bb1.capacity() ; i++ )
            if ( bb1.get(i) != bb2.get(i) ) return false ;
        return true ;
    }

    @Test public void storage_01() 
    {
        assertEquals(0, store.size()) ;
    }
    
    @Test public void storage_02()
    {
        ByteBuffer b = data(blkSize) ;
        store.write(b) ;
        long x = store.size() ;
        assertEquals(blkSize, x) ;
    }

    @Test public void storage_03()
    {
        ByteBuffer b1 = data(blkSize) ;
        long posn = store.position() ; 
        store.write(b1) ;
        ByteBuffer b9 = ByteBuffer.allocate(blkSize) ;
        int r = store.read(b9, posn) ;
        assertEquals(blkSize, r) ;
        assertTrue(same(b1, b9)) ;
    }
    
    @Test public void storage_04()
    {
        ByteBuffer b1 = data(blkSize) ;
        ByteBuffer b2 = data(blkSize/2) ;

        store.write(b2, 0) ;
        store.write(b1, 0) ;
        
        assertEquals(blkSize, store.size()) ;
        ByteBuffer b9 = ByteBuffer.allocate(5) ;
        int z = store.read(b9) ;
        assertEquals(5, z) ;
    }
    
    @Test public void storage_05()
    {
        ByteBuffer b1 = data(blkSize) ;
        ByteBuffer b1a = ByteBuffer.allocate(blkSize) ;
        ByteBuffer b2 = data(blkSize/2) ;
        ByteBuffer b2a = ByteBuffer.allocate(blkSize/2) ;
        store.write(b1) ;
        store.write(b2) ;
        store.position(0) ;
        store.read(b1a) ;
        assertTrue(same(b1, b1a)) ;
        store.read(b2a) ;
        assertTrue(same(b2, b2a)) ;
    }
    
    @Test public void storage_06()
    {
        ByteBuffer b1 = data(blkSize) ;
        store.write(b1) ;
        store.truncate(0) ;
        assertEquals(0, store.size()) ;
        // Check for:
        // http://bugs.sun.com/view_bug.do?bug_id=6191269
        assertEquals(0, store.position()) ;
    }
    
    @Test public void storage_07()
    {
        ByteBuffer b1 = data(blkSize) ;
        store.write(b1) ;
        store.position(10) ;
        b1.rewind() ;
        store.write(b1) ;
        assertEquals(blkSize+10, store.size()) ;
    }    

    
    @Test public void storage_08()
    {
        ByteBuffer b1 = data(blkSize) ;
        ByteBuffer b2 = data(blkSize) ;
        store.write(b1) ;
        store.write(b2) ;
        store.position(10) ;
        b1.rewind() ;
        store.write(b1) ;
        assertEquals(2*blkSize, store.size()) ;
    }    

}
