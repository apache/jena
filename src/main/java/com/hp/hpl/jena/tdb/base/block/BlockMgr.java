/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.util.Iterator ;

import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sync ;


public interface BlockMgr extends Sync, Closeable /*UnitMgr<Block>*/
{
    /** Allocate an uninitialized block - writable - call only inside a update sequence. 
     *  If blockSize is -1, means "default/fixed size" for this BlockMgr
     */
    public Block allocate(int blockSize) ;
    
    /** Answer whether there are any blocks in the collection being managed */
    public boolean isEmpty() ; 
    
    /** Fetch a block, use for read only */
    public Block getRead(long id);
    
    /** Fetch a block, use for read only in an iterator */
    public Block getReadIterator(long id);

    /** Fetch a block, use for write and read - only inside "update" */
    public Block getWrite(long id);

    /** Release a block, unmodified. */
    public void release(Block block) ;

    /** Promote to writeable : it's OK to promote an already writeable block */ 
    public Block promote(Block block);

    // Bad name?  "endWrite", "put" -- for a mapped block, the changes are made directly, not on the write() */   
    /** Write a block back - it still needs releasing. */ 
    public void write(Block block) ;
    
    /** Replace the contents of a block slot with new contents. Block does not need releasing.
     * The write() operation may not do real work if the block is mapped - this operation
     * really does replace the contents with the new contents.  
     */ 
    public void overwrite(Block blk) ;
    
   /** Announce a block is no longer in use (i.e it's now freed) */ 
    public void free(Block block);
  
    /** Is this a valid block id? (may be a free block)*/
    public boolean valid(int id) ;
    
    /** Close the block manager */
    @Override
    public void close() ;
    
    /** Is this block manager still usable?  Closed block managers can not perform any operations except this one. */  
    public boolean isClosed() ; 
    
    /** Sync the block manager */
    @Override
    public void sync() ;
    
    // This is not Session interface which si more an application facing
    // coarser granularity interface.  We also add iterator tracking.
    
    /** Start of update */
    public void beginUpdate() ;
    
    /** Completion of update */
    public void endUpdate() ;

    /** Start of read */
    public void beginRead() ;

    /** Completion of read */
    public void endRead() ;

    /** Start of iterator */
    public void beginIterator(Iterator<?> iterator) ;

    /** Completion of iterator */
    public void endIterator(Iterator<?> iterator) ;

    /* Label for helping trace which BlockMgr is which */
    public String getLabel() ;
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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