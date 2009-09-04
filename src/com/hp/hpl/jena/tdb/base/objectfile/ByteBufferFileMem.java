/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.objectfile;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import atlas.lib.ByteBufferLib;

/** In-memory ByteBufferFile (for testing) - copies bytes in and out
 * to ensure no implicit modification.  
 */

public class ByteBufferFileMem implements ByteBufferFile 
{
    List<ByteBuffer> buffers = new ArrayList<ByteBuffer>() ;
    boolean closed = false ;

    public ByteBufferFileMem(String label)
    { }

    public ByteBufferFileMem()
    { }

    
    public long length()
    {
        if ( closed )
            throw new IllegalStateException("Closed") ;
        return buffers.size() ;
    }

    public ByteBuffer read(long id)
    {
        if ( id < 0 || id >= buffers.size() )
            throw new IllegalArgumentException() ;
        
        if ( closed )
            throw new IllegalStateException("Closed") ;
        
        ByteBuffer bb1 = buffers.get((int)id) ;
        ByteBuffer bb2 = ByteBufferLib.duplicate(bb1) ;
        return bb2 ;
    }

    public long write(ByteBuffer bb)
    {
        if ( closed )
            throw new IllegalStateException("Closed") ;
        ByteBuffer bb2 = ByteBufferLib.duplicate(bb) ;
        buffers.add(bb2) ;
        return buffers.size() ; 
    }

    public void sync(boolean force)
    {}

    public void close()
    {
        closed = true ;
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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