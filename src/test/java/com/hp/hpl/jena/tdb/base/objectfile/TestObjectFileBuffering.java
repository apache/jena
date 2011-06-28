/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.objectfile;

import static com.hp.hpl.jena.tdb.base.BufferTestLib.sameValue ;
import static com.hp.hpl.jena.tdb.base.objectfile.AbstractTestObjectFile.fill ;

import java.nio.ByteBuffer ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.file.BufferChannel ;
import com.hp.hpl.jena.tdb.base.file.BufferChannelMem ;

public class TestObjectFileBuffering extends BaseTest
{
    protected ObjectFile make(int bufferSize)
    {
        BufferChannel chan = new BufferChannelMem() ;
        return new ObjectFileStorage(chan, bufferSize) ;
    }

    private void write(int sizeOfBuffer, int... sizes)
    {
        ObjectFile file = make(sizeOfBuffer) ;
        int N = sizes.length ;
        ByteBuffer bb[] = new ByteBuffer[N] ;
        long loc[] = new long[N] ;
        ByteBuffer read[] = new ByteBuffer[N] ;
        
        for ( int i = 0 ; i < N ; i++ )
        {
            bb[i] = ByteBuffer.allocate(sizes[i]) ;
            fill(bb[i]) ;
            loc[i] = file.write(bb[i]) ;
        }
        //file.sync() ;
        for ( int i = 0 ; i < N ; i++ )
        {
            read[i] = file.read(loc[i]) ;
            assertNotSame(bb[i], read[i]) ;
            sameValue(bb[i], read[i]) ;
        }
    }
    
    private void writePrealloc(int sizeOfBuffer, int... sizes)
    {
        ObjectFile file = make(sizeOfBuffer) ;
        int N = sizes.length ;
        Block blocks[] = new Block[N] ;
        ByteBuffer read[] = new ByteBuffer[N] ;
        
        for ( int i = 0 ; i < N ; i++ )
        {
            blocks[i] = file.allocWrite(sizes[i]) ;
            fill(blocks[i].getByteBuffer()) ;
            file.completeWrite(blocks[i]) ;
        }

        for ( int i = 0 ; i < N ; i++ )
        {
            read[i] = file.read(blocks[i].getId()) ;
            assertNotSame(blocks[i].getByteBuffer(), read[i]) ;
            sameValue(blocks[i].getByteBuffer(), read[i]) ;
        }
    }

    
    @Test public void objectfile_50()       { write(5, 10) ; }
    @Test public void objectfile_51()       { writePrealloc(5, 10) ; }
    @Test public void objectfile_52()       { write(12, 10) ; }
    @Test public void objectfile_53()       { writePrealloc(12, 10) ; }
    @Test public void objectfile_54()       { write(12, 10, 8) ; }          // 10 is too big
    @Test public void objectfile_55()       { writePrealloc(12, 10, 8) ; }  // 10 is too big
    @Test public void objectfile_56()       { write(12, 6, 10) ; }
    @Test public void objectfile_57()       { writePrealloc(12, 6, 10) ; }
    @Test public void objectfile_58()       { write(20, 6, 10, 5) ; }
    @Test public void objectfile_59()       { writePrealloc(20, 6, 10, 5) ; }

    @Test public void objectfile_60()       { write(20, 4, 4, 8) ; }
    @Test public void objectfile_61()       { writePrealloc(20, 4, 4, 8) ; }

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