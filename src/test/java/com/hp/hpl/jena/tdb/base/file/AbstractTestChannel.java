/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.base.file.Channel ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

public abstract class AbstractTestChannel extends BaseTest
{
    protected abstract Channel make() ;
    static final int blkSize = 100 ;
    
    @Test public void storage_01() 
    {
        Channel store = make() ;
        assertEquals(0, store.length()) ;
    }
    
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

    @Test public void storage_02()
    {
        Channel store = make() ;
        ByteBuffer b = data(blkSize) ;
        store.write(b) ;
        long x = store.length() ;
        assertEquals(blkSize, x) ;
    }

    @Test public void storage_03()
    {
        Channel store = make() ;
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
        Channel store = make() ;
        ByteBuffer b1 = data(blkSize) ;
        ByteBuffer b2 = data(blkSize/2) ;

        store.write(b2, 0) ;
        store.write(b1, 0) ;
        
        assertEquals(blkSize, store.length()) ;
        ByteBuffer b9 = ByteBuffer.allocate(5) ;
        int z = store.read(b9) ;
        assertEquals(5, z) ;
    }
    
    @Test public void storage_05()
    {
        Channel store = make() ;
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