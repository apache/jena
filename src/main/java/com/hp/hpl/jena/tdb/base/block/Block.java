/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.nio.ByteBuffer ;

import tx.base.BlockRef ;

import com.hp.hpl.jena.tdb.base.page.Page ;

public final class Block
{
    private final Integer id ;          // Keep as object.  It's the cache key.
    private final boolean readOnly ;
    private final BlockRef blockRef ;
    private boolean modified = false ;
    
    private final ByteBuffer byteBuffer ;
//    private BlockType type ;

    public Block(int id, ByteBuffer byteBuffer)
    { 
        // ByteBuffer is whole disk space from byte 0 for this disk unit. 
        this.id = id ; 
        this.byteBuffer = byteBuffer ;
        //this.type = BlockType.UNDEF ;
        this.blockRef = null ;
        this.readOnly = false ;
        // Initially a block is not modified.
        this.modified = false ;
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
    
    
    public final ByteBuffer getByteBuffer()
    {
        return byteBuffer ;
    }
 
    public boolean isReadOnly()
    {
        return readOnly ;
    }

    public boolean isModified()
    {
        return modified ;
    }

    public void setModified(boolean modified)
    {
        if ( readOnly && modified )
            throw new BlockException("Attempt to mark a readonly block as modified") ;
        this.modified = modified ;
    }

    public BlockRef getFileRef()
    {
        return blockRef ;
    }

//    public void setType(BlockType blockType) 
//    {
//        type = blockType ;
//    }
//        
//    public BlockType getType()
//    {
//        return type ;
//    }
    
    @Override
    public String toString()
    {
        return "Block: "+id+" : Length: "+byteBuffer.capacity() ;
    }
    
    public Block replicate()
    {
        ByteBuffer dstBuffer = replicate(getByteBuffer()) ;
        Block b = new Block(getId(), dstBuffer) ;
        return b ;
    }  

    public static void replicate(Block srcBlock, Block dstBlock)
    {
        if ( ! srcBlock.getId().equals(dstBlock.getId()) )
            throw new BlockException("FileAccessMem: Attempt to copy across blocks: "+srcBlock.getId()+" => "+dstBlock.getId()) ;
        replicate(srcBlock.getByteBuffer(), dstBlock.getByteBuffer()) ;
    }  

    private static ByteBuffer replicate(ByteBuffer srcBlk)
    {
        ByteBuffer dstBlk = ByteBuffer.allocate(srcBlk.capacity()) ;
        System.arraycopy(srcBlk.array(), 0, dstBlk.array(), 0, srcBlk.capacity()) ;
        return dstBlk ; 
    }  

    private static void replicate(ByteBuffer srcBlk, ByteBuffer dstBlk)
    {
        srcBlk.position(0) ;
        dstBlk.position(0) ;
        dstBlk.put(srcBlk) ;
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