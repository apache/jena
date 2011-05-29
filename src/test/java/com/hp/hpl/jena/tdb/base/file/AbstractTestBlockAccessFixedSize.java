/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import static com.hp.hpl.jena.tdb.base.BlockLib.sameValue ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.tdb.base.block.Block ;

public abstract class AbstractTestBlockAccessFixedSize extends BaseTest
{
    // Fixed block tests.
    
    int blkSize ;
    
    protected AbstractTestBlockAccessFixedSize(int blkSize)
    {
        this.blkSize = blkSize ;
    }
    
    protected abstract BlockAccess make() ;
    protected static Block data(BlockAccess file, int len)
    {
        Block b = file.allocate(len) ;
        for (int i = 0 ; i < len ; i++ )
            b.getByteBuffer().put((byte)(i&0xFF)) ;
        return b ;
    }

    @Test public void fileaccess_01()
    {
        BlockAccess file = make() ;
        assertTrue(file.isEmpty()) ;
    }
    
    @Test public void fileaccess_02()
    {
        BlockAccess file = make() ;
        Block b = data(file, blkSize) ;
        file.write(b) ;
    }

    @Test public void fileaccess_03()
    {
        BlockAccess file = make() ;
        Block b1 = data(file, blkSize) ;
        file.write(b1) ;
        long x = b1.getId() ;
        Block b9 = file.read(x) ;
        assertNotSame(b1, b9) ;
        assertTrue(sameValue(b1, b9)) ;
        b9 = file.read(x) ;
        assertNotSame(b1, b9) ;
        assertTrue(sameValue(b1, b9)) ;
    }
    
    @Test public void fileaccess_04()
    {
        BlockAccess file = make() ;
        Block b1 = data(file, blkSize) ;
        Block b2 = data(file, blkSize) ;
        file.write(b1) ;
        file.write(b2) ;
        
        long x = b1.getId() ;
        Block b8 = file.read(b1.getId()) ;
        Block b9 = file.read(b1.getId()) ;
        assertNotSame(b8, b9) ;
        assertTrue(b8.getId() == b9.getId()) ;
    }
    
    @Test(expected=FileException.class)
    public void fileaccess_05()
    {
        BlockAccess file = make() ;
        Block b1 = data(file, 10) ;
        Block b2 = data(file, 20) ;
        file.write(b1) ;
        
        // Should not work. b2 not written.   
        Block b2a = file.read(b2.getId()) ;
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