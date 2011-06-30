/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
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
 */

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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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