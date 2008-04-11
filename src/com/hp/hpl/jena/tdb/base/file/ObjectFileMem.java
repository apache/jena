/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.tdb.pgraph.NodeId;

import lib.Bytes;

/** A file for writing serialized objects to
 *  The file is currently "read/append"
 *  Allocates an id (actually the array index)
 *  Limited to 2billion nodes by array size being an int. :-)
 *  
 * @author Andy Seaborne
 */

public class ObjectFileMem implements ObjectFile 
{
    List<ByteBuffer> buffers = new ArrayList<ByteBuffer>() ;

    public ObjectFileMem()
    {
    }
    
    @Override
    public NodeId write(String str)
    { 
        ByteBuffer bb = ByteBuffer.allocate(4*str.length()) ;   // Worst case
        Bytes.toByteBuffer(str, bb) ;
        int len = bb.position() ;
        bb.limit(len) ;
        int x = buffers.size();
        buffers.add(bb) ;
        return NodeId.create(x) ;
    }
    
    private ByteBuffer readBytes(NodeId id)
    { 
        int x = (int)id.getId() ;
        ByteBuffer bb = buffers.get(x) ;
        bb.position(0) ;
        ByteBuffer bb2 = ByteBuffer.allocate(bb.limit()) ;
        bb2.put(bb) ;
        bb2.position(0);
        return bb2 ;
    }

    @Override
    public String read(NodeId id)
    {
        ByteBuffer bb = readBytes(id) ;
        return Bytes.fromByteBuffer(bb) ;
    }
    
    public List<String> all()
    {
        List<String> strings = new ArrayList<String>() ;
        for ( int i = 0 ; i < buffers.size(); i++ )
        {
            String str = read(NodeId.create((long)i)) ;
            strings.add(str) ;
        }
        return strings ;
    }

    @Override
    public void close()
    {}

    @Override
    public void sync(boolean force)
    {}
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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