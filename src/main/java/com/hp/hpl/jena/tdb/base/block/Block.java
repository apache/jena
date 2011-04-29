/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.nio.ByteBuffer ;

import tx.base.FileRef ;

import com.hp.hpl.jena.tdb.base.page.Page ;

public final class Block
{
    /* Layout on disk:
     * TYPE
     * Bytes
     * 
     * and different block types define their layout. 
     * e.g.
     * FREE, 4bytes for the free chain 
     * 
     * 
     */

    private final Integer id ;          // Keep as objects.  It's the cache key.
    private final boolean readOnly = false ;
    private final FileRef fileRef ;
    
    private BlockType type ;
    private final ByteBuffer byteBuffer ;

    public Block(int id, BlockType type, ByteBuffer byteBuffer)
    { 
        // ByteBuffer is whole disk space from byte 0 for this disk unit. 
        this.id = id ; 
        this.byteBuffer = byteBuffer ;
        
        this.fileRef = null ;
        this.type = type ;
    }
    
    public void reset(BlockType blockType) 
    {
        type = blockType ;
    }
        
    public <T extends Page> T convert(BlockConverter<T> converter)
    {
        // converter.checkType(type) ; 
        return converter.fromBlock(this) ;
    }
        
    
    public final Integer getId()
    {
        return id ;
    }
    
    
    public boolean isReadonly()
    {
        return readOnly ;
    }

    public final ByteBuffer getByteBuffer()
    {
        return byteBuffer ;
    }
 
    public boolean isReadOnly()
    {
        return readOnly ;
    }

    public FileRef getFileRef()
    {
        return fileRef ;
    }

    public BlockType getType()
    {
        return type ;
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