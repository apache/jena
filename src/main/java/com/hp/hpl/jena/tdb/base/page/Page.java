/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.page;

import org.openjena.atlas.io.Printable ;

import com.hp.hpl.jena.tdb.base.block.Block ;

/** Abstract of a bytebuffer, a block type, an id and a count */
public interface Page extends Printable
{
    public static final int NO_ID   = -1 ;
    
    /** Return the nodes id */ 
//    public final int getId()                { return id ; }
//    public final void setId(int id)         { this.id = id ; }
//    /** Return the ByteBuffer backing this page */ 
//    public final ByteBuffer getByteBuffer() { return byteBuffer ; }
//    
//    public final BlockType getBlockType()   { return blockType ; }
    
    // [TxTDB:PATCH-UP] Remove
    public int getId() ;
    
    // [TxTDB:PATCH-UP] Remove
    public void setId(int id) ;

    /** Return the block associated with this page */ 
    public Block getBackingBlock() ;
    
//    /** Return the ByteBuffer that covers the data area of this page - not the byte buffer of the block */   
//    public ByteBuffer getBackingByteBuffer() ;

    public int getCount() ; 
    public void setCount(int count) ;
    
    public int getMaxSize() ;

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