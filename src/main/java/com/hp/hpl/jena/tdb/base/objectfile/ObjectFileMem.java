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

import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.tdb.base.StorageException ;
import com.hp.hpl.jena.tdb.base.block.Block ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.IteratorInteger ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.Pair ;

import org.openjena.atlas.lib.ByteBufferLib ;


/** In-memory ByteBufferFile (for testing) - copies bytes in and out
 * to ensure no implicit modification.  
 * @deprecated Use new ObjectFileStorage(BufferChannelMem.create(filename)) (see FileFcatory)
 */
@Deprecated
public class ObjectFileMem implements ObjectFile 
{
    private List<ByteBuffer> buffers = new ArrayList<ByteBuffer>() ;
    private boolean closed = false ;

    private final String label ;

    public ObjectFileMem(String label)
    { this.label = label ; }

    public ObjectFileMem()
    { this("ObjectFileMem") ; }

    // Could have been ObjectFileStorage + a byte array.  
    
    @Override
    public long length()
    {
        if ( closed )
            throw new IllegalStateException("Closed") ;
        return buffers.size() ;
    }

    @Override
    public ByteBuffer read(long id)
    {
        if ( id < 0 || id >= buffers.size() )
            return null ;
        
        if ( closed )
            throw new IllegalStateException("Closed") ;
        
        ByteBuffer bb1 = buffers.get((int)id) ;
        ByteBuffer bb2 = ByteBufferLib.duplicate(bb1) ;
        return bb2 ;
    }

    @Override
    public long write(ByteBuffer bb)
    {
        if ( closed )
            throw new IllegalStateException("Closed") ;
        int id = buffers.size() ;
        buffers.add(null) ;
        write(id, bb) ;
        return id ; 
    }

    private void write(long id, ByteBuffer bb)
    {
        if ( closed )
            throw new IllegalStateException("Closed") ;
        ByteBuffer bb2 = ByteBufferLib.duplicate(bb) ;
        buffers.set((int)id, bb2) ;
    }
    
    private Block allocBlock = null ;
    @Override
    public Block allocWrite(int bytesSpace)
    {
        long id = buffers.size() ;
        buffers.add(null) ;
        Block b = new Block(id, ByteBuffer.allocate(bytesSpace)) ;
        allocBlock = b ;
        return b ;
    }

    @Override
    public void completeWrite(Block block)
    {
        if ( block.getId() != buffers.size()-1 )
            throw new StorageException() ;
        if ( block != allocBlock )
            throw new StorageException() ;
        allocBlock = null ;
        write(block.getId(), block.getByteBuffer()) ;
    }

    @Override
    public void reposition(long id)
    {
        if ( allocBlock != null )
            throw new StorageException("In the middle of an alloc-write") ;
        int newSize = (int)id ;
        if ( newSize < 0 || newSize > buffers.size() )
            // Can reposition to the end of the file.
            throw new StorageException() ;
        if ( newSize == buffers.size() )
            return ;
        
        List<ByteBuffer> buffers2 = new ArrayList<ByteBuffer>(newSize) ;
        for ( int i = 0 ; i < id ; i++ )
            buffers2.add(buffers.get(i)) ;
        buffers = buffers2 ;
    }

    @Override
    public void truncate(long size)
    {
        reposition(size) ;
    }

    @Override
    public Iterator<Pair<Long, ByteBuffer>> all()
    {
        int N = buffers.size() ;
        Iterator<Long> iter = new IteratorInteger(0,N) ;
        Transform<Long, Pair<Long, ByteBuffer>> transform = new Transform<Long, Pair<Long, ByteBuffer>>() {
            @Override
            public Pair<Long, ByteBuffer> convert(Long item)
            {
                ByteBuffer bb = buffers.get(item.intValue()) ;
                return new Pair<Long, ByteBuffer>(item, bb) ;
            }
        } ;
        return Iter.map(iter, transform) ;
    }

    @Override
    public void sync()
    {}
    
    public void sync(boolean force)
    {}

    @Override
    public void close()
    {
        closed = true ;
    }

    @Override
    public String getLabel()            { return label ; }

}
