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

package com.hp.hpl.jena.tdb.base.objectfile;

import static com.hp.hpl.jena.tdb.base.BufferTestLib.sameValue ;
import static com.hp.hpl.jena.tdb.base.objectfile.AbstractTestObjectFile.fill ;

import java.nio.ByteBuffer ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.file.BufferChannel ;
import com.hp.hpl.jena.tdb.base.file.BufferChannelMem ;

public class TestObjectFileBuffering extends BaseTest
{
    protected ObjectFile make(int bufferSize)
    {
        BufferChannel chan = BufferChannelMem.create() ;
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
